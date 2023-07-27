/*
 * Copyright (c) 2023 OceanBase
 * OCP Express is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.oceanbase.ocp.perf.sql;

import static com.oceanbase.ocp.common.util.time.TimeUtils.toUs;
import static com.oceanbase.ocp.core.i18n.ErrorCodes.PERF_SQL_QUERY_TIMEOUT;
import static com.oceanbase.ocp.perf.sql.util.SqlStatUtils.avg2;
import static com.oceanbase.ocp.perf.sql.util.SqlStatUtils.info;
import static com.oceanbase.ocp.perf.sql.util.SqlStatUtils.isTimeoutException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.core.ob.cluster.ManagedCluster;
import com.oceanbase.ocp.obparser.SqlType;
import com.oceanbase.ocp.obsdk.operator.sql.entity.SqlAuditRawStatEntity;
import com.oceanbase.ocp.obsdk.operator.sql.entity.TenantSlowSqlCounter;
import com.oceanbase.ocp.obsdk.operator.sql.param.QuerySlowSqlRankParam;
import com.oceanbase.ocp.obsdk.operator.sql.param.QueryTopSqlRawStatParam;
import com.oceanbase.ocp.perf.sql.dao.SqlAuditRawStatAccess;
import com.oceanbase.ocp.perf.sql.model.SlowSqlRankInfo;
import com.oceanbase.ocp.perf.sql.model.SqlAuditStatSummary;
import com.oceanbase.ocp.perf.sql.model.SqlWaitEvent;
import com.oceanbase.ocp.perf.sql.param.QueryTopSqlParam;
import com.oceanbase.ocp.perf.sql.util.SqlStatMapper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SqlAuditRawStatServiceImpl implements SqlAuditRawStatService {

    @Autowired
    private SqlStatProperties properties;

    @Autowired
    private SqlStatContextService contextService;

    @Autowired
    private SqlDiagnoseExpressionChecker expressionChecker;

    @Autowired
    private ManagedCluster managedCluster;

    @Autowired
    private SqlAuditRawStatAccess sqlAuditRawStatDao;

    @Getter
    @Autowired
    private SqlStatAttributeService sqlStatAttributeService;


    public static String FAKE_DB_NAME = "NO_DATABASE";

    public static Integer SQL_TEXT_LIMIT = 2000;

    private String env(QueryTopSqlParam param, SqlStatDataContext context) {
        return String.format("clusterId=%d(%s:%d), tenantId=%d(%d)", managedCluster.obClusterId(),
                context.clusterName, context.obClusterId, param.getTenantId(), context.obTenantId, context.obServerId);
    }

    @Override
    public List<SqlAuditStatSummary> topSql(QueryTopSqlParam param) {
        expressionChecker.checkFilterExpressionIfSampled(SqlAuditStatSummary.class.getCanonicalName(),
                param.getFilterExpression());
        SqlStatDataContext context = contextService.getContext(param.getTenantId());
        context.parseSqlType = param.parseSqlType;
        if (log.isDebugEnabled()) {
            log.debug("Top sql query info: {}\r\n{}", env(param, context), info(param.interval, 1));
        }
        Predicate<SqlAuditStatSummary> searchFilter = param.createSearchFilter(SqlAuditRawStatServiceImpl.this);
        List<SqlAuditRawStatEntity> data;
        try {
            QueryTopSqlRawStatParam query = buildQueryTopSql(param, context);
            log.info("Query sql audit with param:{}", query);
            data = sqlAuditRawStatDao.topSql(query);
            log.info("Query out {} by {}", data, param);
        } catch (Exception e) {
            if (isTimeoutException(e)) {
                throw PERF_SQL_QUERY_TIMEOUT.exception();
            }
            throw e;
        }
        List<SqlAuditStatSummary> res = mapToModels(data, searchFilter);
        expressionChecker.sampleListIfAbsent(SqlAuditStatSummary.class.getCanonicalName(), res);
        expressionChecker.checkFilterExpressionIfSampled(SqlAuditStatSummary.class.getCanonicalName(),
                param.getFilterExpression());
        log.debug("Finished mapping");
        return res.parallelStream()
                .filter(new SqlAuditStatSummeryFilter(param.getFilterExpression()))
                .filter(s -> !StringUtils.equals(FAKE_DB_NAME, s.getDbName()))
                .sorted(Comparator.comparingDouble(it -> -it.getSumElapsedTime()))
                .limit(param.limit != null ? param.limit : properties.getTopSqlLimit())
                .collect(Collectors.toList());
    }

    QueryTopSqlRawStatParam buildQueryTopSql(QueryTopSqlParam param,
            SqlStatDataContext context) {
        QueryTopSqlRawStatParam.Builder builder = QueryTopSqlRawStatParam.builder()
                .timeout(properties.getQueryTimeout())
                .obTenantId(context.obTenantId)
                .startTimeUs(toUs(param.getInterval().start))
                .endTimeUs(toUs(param.getInterval().end));
        if (StringUtils.isNotEmpty(param.sqlText)) {
            builder.sqlText(param.getSqlText());
        }
        if (param.limit != null) {
            builder.limit(param.limit);
        } else {
            long limit = properties.getTopSqlLimit();
            if (limit != 0) {
                builder.limit(limit);
            }
        }
        if (Objects.nonNull(param.inner)) {
            builder.includeInner(param.inner);
        }
        if (param.server != null) {
            String[] array = param.server.split(":");
            if (array.length == 2) {
                String serverIp = array[0];
                Integer serverPort = Integer.parseInt(array[1]);
                builder.serverIp(serverIp);
                builder.serverPort(serverPort);
            }
        }
        if (param.search != null && param.search.searchSqlId()) {
            builder.sqlId(param.search.value);
        }
        return builder.build();
    }

    public List<SqlAuditStatSummary> mapToModels(List<SqlAuditRawStatEntity> data,
            Predicate<SqlAuditStatSummary> searchFilter) {
        List<SqlAuditStatSummary> results = new LinkedList<>();
        data.forEach(entity -> {
            SqlAuditStatSummary detail = new SqlAuditStatSummary();
            Double innerValue = avg2(entity.innerSqlCount, entity.exec);
            detail.setInner(innerValue != null && innerValue > properties.getInnerSqlThreshold());
            detail.setWaitEvent(SqlWaitEvent.maxTimeCost(entity).text);
            SqlStatMapper.mapToModel(entity, detail);
            detail.setObDbId(entity.getObDbId());
            detail.setDbName(getDbName(entity.getDbName()));
            detail.setObUserId(entity.getObUserId());
            detail.setUserName(entity.getUserName());
            detail.setSqlId(entity.getSqlId());
            detail.setServer(entity.getServerIp() + ":" + entity.getServerPort());
            detail.setServerIp(entity.getServerIp());
            detail.setServerPort(entity.getServerPort());
            String statement = getSqlStatement(entity);
            detail.setSqlTextShort(statement.substring(0, Math.min(statement.length(), SQL_TEXT_LIMIT)));
            detail.setSqlType(SqlType.quickParse(statement).name());
            detail.setIsComplete(statement.length() < SQL_TEXT_LIMIT);
            if (searchFilter.test(detail)) {
                results.add(detail);
            }
        });
        return results;
    }

    @Override
    public List<SqlAuditStatSummary> slowSql(QueryTopSqlParam param) {
        SqlStatDataContext context = contextService.getContext(param.getTenantId());
        expressionChecker.checkFilterExpressionIfSampled(SqlAuditStatSummary.class.getCanonicalName(),
                param.getFilterExpression());
        Predicate<SqlAuditStatSummary> searchFilter = param.createSearchFilter(SqlAuditRawStatServiceImpl.this);
        List<SqlAuditRawStatEntity> data = new ArrayList<>();
        QueryTopSqlRawStatParam query = buildQueryTopSql(param, context);
        data = sqlAuditRawStatDao.slowSql(query);
        log.info("Query sql into from view");
        List<SqlAuditStatSummary> res = mapToModels(data, searchFilter);
        expressionChecker.sampleListIfAbsent(SqlAuditStatSummary.class.getCanonicalName(), res);
        expressionChecker.checkFilterExpressionIfSampled(SqlAuditStatSummary.class.getCanonicalName(),
                param.getFilterExpression());
        log.debug("Finished mapping");
        res = res.stream()
                .filter(new SqlAuditStatSummeryFilter(param.getFilterExpression()))
                .filter(s -> !StringUtils.equals(FAKE_DB_NAME, s.getDbName()))
                .sorted(Comparator.comparingDouble(it -> -it.getSumElapsedTime()))
                .collect(Collectors.toList());
        return res;
    }

    @Override
    public List<SlowSqlRankInfo> getSlowSqlRank(QuerySlowSqlRankParam param) {
        List<TenantSlowSqlCounter> topSlowSqlEntities = sqlAuditRawStatDao.getTopSlowSql(param);
        List<SlowSqlRankInfo> res = new ArrayList<>();
        topSlowSqlEntities.forEach(entity -> {
            SlowSqlRankInfo info = new SlowSqlRankInfo();
            info.count = entity.getCount();
            info.tenantName = entity.getTenantName();
            info.tenantId = entity.getTenantId();
            res.add(info);
        });
        return res;
    }

    public String getDbName(String rawName) {
        return StringUtils.isNotEmpty(rawName) ? rawName : FAKE_DB_NAME;
    }

    public String getSqlStatement(SqlAuditRawStatEntity entity) {
        return StringUtils.isNotEmpty(entity.getStatement()) ? entity.getStatement() : entity.getSqlText();
    }
}
