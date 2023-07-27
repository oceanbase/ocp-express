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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.core.constants.OcpConstants;
import com.oceanbase.ocp.core.exception.IllegalArgumentException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.cluster.BasicCluster;
import com.oceanbase.ocp.core.ob.cluster.ManagedCluster;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.core.obsdk.ObAccessorFactory;
import com.oceanbase.ocp.core.property.PropertyService;
import com.oceanbase.ocp.core.property.SystemInfo;
import com.oceanbase.ocp.core.security.AuthenticationFacade;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obsdk.accessor.ObAccessor;
import com.oceanbase.ocp.obsdk.accessor.sql.tuning.model.ObOutline;
import com.oceanbase.ocp.obsdk.util.ObSdkUtils;
import com.oceanbase.ocp.perf.sql.enums.OutlineType;
import com.oceanbase.ocp.perf.sql.model.BatchConcurrentLimitResult;
import com.oceanbase.ocp.perf.sql.model.BatchDropOutlineResult;
import com.oceanbase.ocp.perf.sql.model.Outline;
import com.oceanbase.ocp.perf.sql.model.OutlineStatus;
import com.oceanbase.ocp.perf.sql.model.SqlText;
import com.oceanbase.ocp.perf.sql.param.BatchConcurrentLimitRequest;
import com.oceanbase.ocp.perf.sql.param.BatchConcurrentLimitRequest.Sql;
import com.oceanbase.ocp.perf.sql.param.BatchDropOutlineRequest;
import com.oceanbase.ocp.perf.sql.param.CreateOutlineParam;
import com.oceanbase.ocp.perf.sql.param.CreateOutlineParam.CreateOutlineParamBuilder;
import com.oceanbase.ocp.perf.sql.param.QuerySqlTextParam;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OutlineServiceImpl implements OutlineService {

    private static final String CONCURRENT_LIMIT_HINT_TEMPLATE = "/*+ max_concurrent(%d) */";

    private static final String SYS_TENANT_NAME = "sys";

    @Autowired
    private ObAccessorFactory accessorFactory;

    @Autowired
    private SqlTextService sqlTextService;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private TenantDaoManager tenantDaoManager;

    @Autowired
    private SqlStatContextService sqlStatContextService;

    @Autowired
    private SqlStatProperties sqlStatProperties;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private ManagedCluster managedCluster;

    @Autowired
    private SystemInfo systemInfo;

    @Override
    public List<Outline> getOutline(Long tenantId, String dbName, String sqlId,
            OffsetDateTime startTime, OffsetDateTime endTime, Boolean attachPerfData) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(tenantId);
        BasicCluster cluster = managedCluster.getClusterInfo();
        checkObVersion(cluster);
        List<Outline> allObOutline = getAllMergedObOutline(tenantEntity);
        Long obDbId = sqlStatContextService.getObDatabaseId(tenantId, dbName);
        if (StringUtils.isNotEmpty(sqlId)) {
            SqlText sqlText = sqlTextService.getAny(QuerySqlTextParam.builder()
                    .tenantId(tenantId)
                    .sqlId(sqlId)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build());
            allObOutline = allObOutline.stream()
                    .filter(otn -> Objects.equals(otn.getObDbId(), obDbId)
                            && (StringUtils.equals(otn.getSqlId(), sqlId)
                                    || (sqlText != null && StringUtils.equals(sqlText.getStatement(),
                                            otn.getVisibleSignature()))))
                    .collect(Collectors.toList());

        }
        allObOutline.parallelStream().forEach(otn -> {
            otn.setDbName(sqlStatContextService
                    .getDatabaseName(tenantEntity.getObTenantId(), otn.getObDbId()));
        });
        List<Outline> results = allObOutline.stream()
                .sorted(Comparator.comparing(Outline::getCreateTime, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .reversed())
                .collect(Collectors.toList());
        return results;
    }


    @Override
    public BatchDropOutlineResult batchDropOutline(Long tenantId, BatchDropOutlineRequest request) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(tenantId);
        BasicCluster cluster = managedCluster.getClusterInfo();
        checkObVersion(cluster);
        ExceptionUtils.require(tenantEntity != null, ErrorCodes.OB_TENANT_ID_NOT_FOUND, tenantId);
        List<Outline> allMergedObOutline = getAllMergedObOutline(tenantEntity);
        log.info("Get outline list:{} of tenant:{}", allMergedObOutline, tenantEntity);
        Map<BatchDropOutlineRequest.Outline, String> failedOutline = new HashMap<>(8);
        request.getOutlineList().forEach(otn -> {
            try {
                String dbName = otn.getDbName();
                String sqlId = otn.getSqlId();
                String outlineName = otn.getOutlineName();
                log.info("Drop outline:{} of {}.{}.{}", outlineName, tenantId, dbName, sqlId);
                if (TenantMode.MYSQL.equals(tenantEntity.getMode())) {
                    accessorFactory.createObAccessor(tenantEntity.getName(), TenantMode.MYSQL)
                            .sqlTuning().dropOutline(dbName, outlineName);
                } else {
                    log.error("express does not support oracle mode");
                    return;
                }

            } catch (Exception e) {
                log.error("failed to drop outline :{} of tenant:{}", otn, tenantEntity, e);
                failedOutline.put(otn, e.getMessage());
            }
        });
        return BatchDropOutlineResult.of(failedOutline);
    }



    @Override
    public BatchConcurrentLimitResult batchConcurrentLimit(Long tenantId,
            BatchConcurrentLimitRequest param) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(tenantId);
        outlinePreCheck(tenantEntity.getName());
        if (param.getConcurrentNum() == null || param.getConcurrentNum() < 0) {
            ExceptionUtils.throwException(IllegalArgumentException.class,
                    ErrorCodes.SQL_DIAG_OUTLINE_CONCURRENT_NUM_REQUIRED_LARGER_OR_EQUALS_ZERO);
        }
        List<Sql> sqlList = param.getSqlList();
        List<String> sqlWithoutDatabase =
                sqlList.stream().filter(p -> p.getDbName() == null).map(Sql::getSqlId).collect(
                        Collectors.toList());
        ExceptionUtils.require(sqlWithoutDatabase.isEmpty(), ErrorCodes.SQL_DIAG_OUTLINE_DATABASE_NOT_EXISTS,
                sqlWithoutDatabase);
        ObTenantEntity tenant = tenantDaoManager.nullSafeGetObTenant(tenantId);
        BasicCluster cluster = managedCluster.getClusterInfo();
        checkObVersion(cluster);
        List<Outline> tenantOutline = getOutline(tenantId, null, null, null, null, false);

        CreateOutlineParamBuilder builder = CreateOutlineParam.builder()
                .type(OutlineType.CONCURRENT_LIMIT)
                .startTime(param.startTime)
                .endTime(param.endTime)
                .concurrentNum(param.concurrentNum);
        Map<Sql, String> failedSql = new HashMap<>(8);
        for (Sql s : param.sqlList) {
            try {
                String dbName = s.getDbName();
                String sqlId = s.getSqlId();
                Long obDbId = sqlStatContextService.getObDatabaseId(tenantId, dbName);
                ExceptionUtils.require(obDbId != null,
                        ErrorCodes.SQL_DIAG_OUTLINE_DATABASE_NOT_EXISTS, dbName);
                List<Outline> sqlOutline = tenantOutline.stream()
                        .filter(otn -> Objects.equals(otn.getObDbId(), obDbId)
                                && (StringUtils.equals(otn.getSqlId(), sqlId)))
                        .collect(Collectors.toList());
                log.info("Found outlines:{} of sql : {}", sqlOutline, sqlId);
                sqlOutline.forEach(o -> {
                    log.warn("Drop outline {} of {}-{}-{} before creating", o, tenantId,
                            dbName, sqlId);
                    dropOutline(tenantId, dbName, sqlId, o.getOutlineName());
                });
                createConcurrentLimitOutline(tenant, sqlId, builder.dbName(dbName).build());
            } catch (Exception e) {
                log.error("Error in creating concurrent limit outline for:{}", s, e);
                failedSql.put(s, e.getMessage());
            }
        }
        return BatchConcurrentLimitResult.of(failedSql);
    }

    private void createConcurrentLimitOutline(ObTenantEntity tenant, String sqlId, CreateOutlineParam param) {
        if (param.getConcurrentNum() == null || param.getConcurrentNum() < 0) {
            ExceptionUtils.throwException(IllegalArgumentException.class,
                    ErrorCodes.SQL_DIAG_OUTLINE_CONCURRENT_NUM_REQUIRED_LARGER_OR_EQUALS_ZERO);
        }
        String hint = String.format(CONCURRENT_LIMIT_HINT_TEMPLATE, param.getConcurrentNum());
        String outlineName = generateOutlineName(OutlineType.CONCURRENT_LIMIT);
        boolean useSqlId = (param.sqlText == null);
        if (TenantMode.MYSQL.equals(tenant.getMode())) {
            if (useSqlId) {
                accessorFactory.createObAccessorWithDataBase(TenantMode.MYSQL, tenant.getName(), param.dbName)
                        .sqlTuning().createOutline(param.getDbName(), outlineName, sqlId, hint);
            }
        } else {
            log.error("express does not support oracle mode");
            return;
        }
        Optional<Outline> outline =
                getAllMergedObOutline(tenant).stream().filter(otn -> otn.getOutlineName().equals(outlineName))
                        .findAny();
        ExceptionUtils.require(outline.isPresent(), ErrorCodes.SQL_DIAG_OUTLINE_NOT_EXISTS, outlineName);
    }

    private List<Outline> getAllMergedObOutline(ObTenantEntity tenantEntity) {
        ObAccessor obAccessor = accessorFactory.createObAccessor(tenantEntity.getName(), tenantEntity.getMode());
        List<ObOutline> allConcurrentLimitOutline = obAccessor.sqlTuning().getAllConcurrentLimitOutline(
                tenantEntity.getObTenantId());
        List<ObOutline> allOutline;
        allOutline = obAccessor.sqlTuning().getAllOutline(tenantEntity.getObTenantId());
        return merge(allOutline, allConcurrentLimitOutline);
    }


    private List<Outline> merge(List<ObOutline> allOutline, List<ObOutline> concurrentLimitOutline) {
        List<Outline> res = new ArrayList<>();
        allOutline.forEach(otn -> {
            if (res.parallelStream().noneMatch(outline -> outline.getOutlineName().equals(otn.getOutlineName()))) {
                Optional<ObOutline> concurrentOutline = concurrentLimitOutline.parallelStream().filter(
                        o -> o.getOutlineName().equals(otn.getOutlineName()))
                        .findFirst();
                if (concurrentOutline.isPresent()) {
                    if (StringUtils.isNotEmpty(otn.getSqlId())) {
                        otn.setConcurrentNum(concurrentOutline.get().getConcurrentNum());
                        Outline outline = mapToModel(otn);
                        outline.setStatus(OutlineStatus.VALID);
                        res.add(outline);
                    } else {
                        Outline outline = mapToModel(concurrentOutline.get());
                        outline.setStatus(OutlineStatus.VALID);
                        outline.setCreateTime(otn.getCreateTime());
                        res.add(outline);
                    }
                } else {
                    Outline outline = mapToModel(otn);
                    outline.setStatus(OutlineStatus.VALID);
                    res.add(outline);
                }
            }
        });
        Set<String> otns = res.stream().map(Outline::getOutlineName).collect(Collectors.toSet());
        concurrentLimitOutline.stream()
                .filter(o -> !otns.contains(o.getOutlineName()))
                .forEach(o -> {
                    Outline outline = mapToModel(o);
                    outline.setStatus(OutlineStatus.VALID);
                    outline.setCreateTime(o.getCreateTime());
                    res.add(outline);
                });
        return res;
    }

    private Outline mapToModel(ObOutline otn) {
        Outline result = Outline.builder()
                .outlineName(otn.getOutlineName())
                .concurrentNum(otn.getConcurrentNum())
                .limitTarget(otn.getLimitTarget())
                .outlineContent(otn.getOutlineContent())
                .sqlId(otn.getSqlId())
                .visibleSignature(otn.getVisibleSignature())
                .obDbId(otn.getObDbId())
                .dbName(otn.getDbName())
                .outlineId(otn.getOutlineId())
                .sqlText(otn.getSqlText())
                .type(OutlineType.from(otn))
                .createTime(otn.getCreateTime())
                .build();
        return result;
    }

    private void checkObVersion(BasicCluster cluster) {
        String obVersion = cluster.getObVersion();
        if (ObSdkUtils.versionBefore(obVersion, OcpConstants.MIN_VERSION_FOR_OUTLINE)) {
            ExceptionUtils.throwException(ErrorCodes.SQL_DIAG_OUTLINE_NOT_SUPPORTED, obVersion);
        }
    }

    private String generateOutlineName(OutlineType type) {
        return type.name() + "_" + UUID.randomUUID().toString().replaceAll("-", "");
    }

    public void dropOutline(Long tenantId, String dbName, String sqlId, String outlineName) {
        log.info("Drop outline:{} of {}.{}.{}", outlineName, tenantId, dbName, sqlId);
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(tenantId);
        BasicCluster cluster = managedCluster.getClusterInfo();
        checkObVersion(cluster);
        ExceptionUtils.require(tenantEntity != null, ErrorCodes.OB_TENANT_ID_NOT_FOUND, tenantId);
        List<Outline> allMergedObOutline = getAllMergedObOutline(tenantEntity);
        log.info("Get outline list:{} of tenant:{}", allMergedObOutline, tenantEntity);
        if (TenantMode.MYSQL.equals(tenantEntity.getMode())) {
            accessorFactory.createObAccessor(tenantEntity.getName(), TenantMode.MYSQL)
                    .sqlTuning().dropOutline(dbName, outlineName);
        } else {
            log.error("Oracle Mode does not support");
        }
    }

    public void outlinePreCheck(String tenantName) {
        ExceptionUtils.require(!StringUtils.equals(systemInfo.getMetaTenantName(), tenantName),
                ErrorCodes.OB_TENANT_METADB_OPERATION_RESTRICTED);
        ExceptionUtils.require(!StringUtils.equals(SYS_TENANT_NAME, tenantName),
                ErrorCodes.OB_TENANT_SYS_OPERATION_RESTRICTED);
    }

}
