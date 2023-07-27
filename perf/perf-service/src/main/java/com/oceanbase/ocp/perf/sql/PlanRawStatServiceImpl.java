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
import static com.oceanbase.ocp.common.util.time.TimeUtils.usToUtc;
import static com.oceanbase.ocp.perf.sql.SqlAuditRawStatServiceImpl.FAKE_DB_NAME;
import static com.oceanbase.ocp.perf.sql.util.SqlStatUtils.info;
import static com.oceanbase.ocp.perf.sql.util.SqlStatUtils.isTimeoutException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.core.exception.IllegalArgumentException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.cluster.ManagedCluster;
import com.oceanbase.ocp.obsdk.operator.sql.entity.PlanRawStatEntity;
import com.oceanbase.ocp.obsdk.operator.sql.model.PlanType;
import com.oceanbase.ocp.obsdk.operator.sql.param.QueryPlanRawStatBySql;
import com.oceanbase.ocp.perf.sql.dao.PlanRawStatAccess;
import com.oceanbase.ocp.perf.sql.model.PlanStatDetail;
import com.oceanbase.ocp.perf.sql.model.PlanStatGroup;
import com.oceanbase.ocp.perf.sql.param.QueryTopPlanParam;
import com.oceanbase.ocp.perf.sql.util.SqlStatMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Data
public class PlanRawStatServiceImpl implements PlanRawStatService {

    @Autowired
    private SqlStatProperties properties;

    @Autowired
    private SqlStatContextServiceImpl sqlStatContextService;


    @Autowired
    public PlanExplainServiceImpl planExplainService;

    @Autowired
    private PlanRawStatAccess planRawStatDao;

    @Autowired
    private ManagedCluster managedCluster;

    private String env(QueryTopPlanParam param, SqlStatDataContext context) {
        Long clusterId = managedCluster.getClusterInfo().getObClusterId();
        return String.format("clusterId=%d(%s:%d), tenantId=%d(%d), serverId=%d(%d)", clusterId,
                context.clusterName, context.obClusterId, param.tenantId, context.obTenantId,
                param.serverId, context.obServerId);
    }

    public List<PlanStatDetail> topPlan(QueryTopPlanParam param) {
        SqlStatDataContext context = sqlStatContextService.getContext(param.tenantId);
        log.info("Top plan query info: {}, sql={}\r\n{}", env(param, context), param.sqlId,
                info(param.interval, 1));
        QueryPlanRawStatBySql query = buildQueryTopPlan(param, context);
        List<PlanRawStatEntity> data;
        try {
            data = planRawStatDao.queryListBySql(query);
            if (data.size() == 0) {
                log.info("get plan from view is null ,tenantId:{}, sqlId{}", query.getObTenantId(), query.getSqlId());
            }
        } catch (Exception e) {
            if (isTimeoutException(e)) {
                throw new IllegalArgumentException(ErrorCodes.PERF_SQL_QUERY_TIMEOUT);
            }
            throw e;
        }
        return data.stream()
                .map(it -> {
                    PlanStatDetail planStatDetail = mapToModel(it);
                    if (param.isAttachPlanExplain()) {
                        planStatDetail.setPlanExplain(planExplainService.getPlanExplain(context, planStatDetail));
                    }
                    return planStatDetail;
                }).filter(Objects::nonNull)
                .filter(p -> p.getTimeoutCount() + p.getExecutions() > 0)
                .sorted(Comparator.comparingLong(PlanStatDetail::getFirstLoadTimeUs).reversed())
                .collect(Collectors.toList());
    }

    private QueryPlanRawStatBySql buildQueryTopPlan(QueryTopPlanParam param, SqlStatDataContext context) {
        String dbName = param.getDbName();
        Long obDbId = sqlStatContextService.getObDatabaseId(param.getTenantId(), dbName);
        properties.checkTopPlanMaxQueryRange(param.interval);
        return QueryPlanRawStatBySql.builder()
                .timeout(properties.getQueryTimeout())
                .startTimeUs(toUs(param.interval.start))
                .endTimeUs(toUs(param.interval.end))
                .obTenantId(context.obTenantId)
                .sqlId(param.sqlId)
                .obServerId(context.obServerId)
                .obDbId(obDbId)
                .timeout(properties.getQueryTimeout())
                .build();
    }

    @Override
    public List<PlanStatGroup> topPlanGroup(QueryTopPlanParam param) {
        String dbName = param.getDbName();
        if (dbName.equals(FAKE_DB_NAME)) {
            param.dbName = null;
        }
        Long obDbId = sqlStatContextService.getObDatabaseId(param.getTenantId(), dbName);
        SqlStatDataContext context =
                sqlStatContextService.getContext(param.getTenantId());
        List<PlanStatDetail> planStatDetails = topPlan(param);
        log.debug("Got {} plans", planStatDetails.size());
        Map<Object, List<PlanStatDetail>> planMap =
                planStatDetails.stream()
                        .filter(plan -> Objects.equals(plan.getObDbId(), obDbId))
                        .collect(Collectors
                                .groupingBy(
                                        p -> StringUtils.isNotEmpty(p.planUnionHash) ? p.planUnionHash : p.planHash));
        List<PlanStatGroup> planStatGroups =
                planMap.values().stream().map(PlanStatGroup::of).collect(Collectors.toList());
        planStatGroups.forEach(planStatGroup -> {
            planStatGroup.setPlanExplain(planExplainService.getPlanExplain(context, planStatGroup.getPlans().get(0)));
        });
        log.debug("Got {} plan groups", planStatGroups);
        return planStatGroups;
    }

    private PlanStatDetail mapToModel(PlanRawStatEntity cur) {
        PlanStatDetail detail = new PlanStatDetail();
        SqlStatMapper.mapToModel(cur, detail);
        detail.uid = cur.getUid().toString();
        detail.planId = cur.planId;
        detail.serverId = cur.obServerId;
        detail.server = cur.serverIp + ":" + cur.serverPort;
        detail.obServerId = cur.obServerId;
        detail.firstLoadTime = usToUtc(cur.firstLoadTimeUs);
        detail.firstLoadTimeUs = cur.firstLoadTimeUs;
        detail.planType = PlanType.of(cur.planType).name();
        detail.planHash = cur.planHash;
        detail.planUnionHash = cur.planUnionHash;
        detail.planSize = cur.planSize;
        detail.schemaVersion = cur.schemaVersion;
        detail.mergedVersion = cur.mergedVersion;
        detail.outlineData = cur.outLineData;
        detail.outlineId = cur.outlineId;
        detail.obDbId = cur.obDbId;
        return detail;
    }

}
