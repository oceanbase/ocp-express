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

package com.oceanbase.ocp.obops.internal.tenant;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.tenant.ObTenantCompactionEntity;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.ob.tenant.TenantStatus;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obops.internal.tenant.repository.ObTenantCompactionRepository;
import com.oceanbase.ocp.obops.tenant.TenantCompactionCollectService;
import com.oceanbase.ocp.obops.tenant.TenantCompactionService;
import com.oceanbase.ocp.obops.tenant.model.TenantCompaction;
import com.oceanbase.ocp.obops.tenant.model.TenantCompactionHistory;
import com.oceanbase.ocp.obops.tenant.model.TenantCompactionHistory.CompactionDuration;
import com.oceanbase.ocp.obsdk.operator.CompactionOperator;
import com.oceanbase.ocp.obsdk.operator.compaction.model.ObTenantCompaction;
import com.oceanbase.ocp.obsdk.operator.compaction.model.ObTenantCompactionStatus;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TenantCompactionServiceImpl implements TenantCompactionService {

    @Autowired
    private ObOperatorFactory obOperatorFactory;

    @Autowired
    private TenantDaoManager tenantDaoManager;

    @Autowired
    private TenantCompactionCollectService tenantCompactionCollectService;

    @Autowired
    private ObTenantCompactionRepository compactionRepository;

    @Override
    public TenantCompaction getTenantCompaction(Long obTenantId) {
        ExceptionUtils.require(obTenantId != null && obTenantId > 0, ErrorCodes.COMMON_ILLEGAL_ARGUMENT, "obTenantId");
        ObTenantEntity tenant = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        CompactionOperator compactionOperator = obOperatorFactory.createCompactionOperator();
        ObTenantCompaction obTenantCompaction = compactionOperator.getObTenantCompaction(obTenantId);
        TenantCompaction tenantCompaction = mapToModel(obTenantCompaction);
        tenantCompaction.setObTenantId(obTenantId);
        tenantCompaction.setTenantName(tenant.getName());
        return tenantCompaction;
    }

    @Override
    public void triggerTenantCompaction(Long obTenantId) {
        log.info("Begin trigger tenant compaction , obTenantId:{}", obTenantId);
        ExceptionUtils.require(obTenantId != null && obTenantId > 0, ErrorCodes.COMMON_ILLEGAL_ARGUMENT, "obTenantId");
        ObTenantEntity tenant = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkTenantStatusNormal(Collections.singletonList(tenant));
        TenantCompaction compaction = getTenantCompaction(obTenantId);
        ExceptionUtils.require(ObTenantCompactionStatus.IDLE.equals(compaction.getStatus()),
                ErrorCodes.OB_TENANT_INVALID_COMPACTION_STATUS, tenant.getName(), compaction.getStatus());
        CompactionOperator compactionOperator = obOperatorFactory.createCompactionOperator();

        tenantCompactionCollectService.collectMajorCompaction(tenant);
        compactionOperator.triggerTenantsCompaction(Collections.singletonList(tenant.getName()));
        tenantCompactionCollectService.collectMajorCompaction(tenant);
        log.info("Trigger tenant compaction success, obTenantId:{}", obTenantId);
    }

    @Override
    public void clearCompactionError(Long obTenantId) {
        log.info("Begin clear tenant compaction error flag, obTenantId:{}", obTenantId);
        ExceptionUtils.require(obTenantId != null && obTenantId > 0, ErrorCodes.COMMON_ILLEGAL_ARGUMENT, "obTenantId");
        ObTenantEntity tenant = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        CompactionOperator compactionOperator = obOperatorFactory.createCompactionOperator();

        tenantCompactionCollectService.collectMajorCompaction(tenant);
        compactionOperator.clearTenantsCompactionErrorFlag(Collections.singletonList(tenant.getName()));
        tenantCompactionCollectService.collectMajorCompaction(tenant);
        log.info("Clear tenant compaction error flag success, obTenantId:{}", obTenantId);
    }

    @Override
    public List<TenantCompactionHistory> topCompactions(Integer top, Integer times) {
        ExceptionUtils.require(top != null && top > 0, ErrorCodes.COMMON_ILLEGAL_ARGUMENT, "top");
        ExceptionUtils.require(times != null && times > 0, ErrorCodes.COMMON_ILLEGAL_ARGUMENT, "times");
        List<ObTenantEntity> tenants = tenantDaoManager.queryAllTenant();
        List<TenantCompactionHistory> compactionHistories = Lists.newArrayList();
        for (ObTenantEntity tenant : tenants) {
            List<ObTenantCompactionEntity> compactions =
                    compactionRepository.listRecentCompactions(tenant.getObTenantId(), times);
            if (CollectionUtils.isEmpty(compactions)) {
                continue;
            }
            Collections.reverse(compactions);
            TenantCompactionHistory history = new TenantCompactionHistory();
            history.setObTenantId(tenant.getObTenantId());
            history.setTenantName(tenant.getName());
            List<CompactionDuration> durations =
                    compactions.stream().map(this::mapToDuration).collect(Collectors.toList());
            history.setCompactionList(durations);
            compactionHistories.add(history);
        }
        compactionHistories.sort((a, b) -> {
            CompactionDuration durationA = a.getCompactionList().get(a.getCompactionList().size() - 1);
            CompactionDuration durationB = b.getCompactionList().get(b.getCompactionList().size() - 1);
            return durationB.getCostTime().compareTo(durationA.getCostTime());
        });

        top = Math.min(compactionHistories.size(), top);
        return compactionHistories.subList(0, top);
    }

    private CompactionDuration mapToDuration(ObTenantCompactionEntity compaction) {
        CompactionDuration duration = new CompactionDuration();
        duration.setResult(compaction.getStatus());

        OffsetDateTime startTime = compaction.getStartTime();
        OffsetDateTime finishTime = compaction.getLastFinishTime();
        duration.setStartTime(startTime);
        duration.setEndTime(finishTime);

        if (finishTime != null && finishTime.isAfter(startTime)) {
            duration.setCostTime(finishTime.toEpochSecond() - startTime.toEpochSecond());
        } else {
            duration.setCostTime(OffsetDateTime.now().toEpochSecond() - startTime.toEpochSecond());
        }
        return duration;
    }

    private void checkTenantStatusNormal(List<ObTenantEntity> tenants) {
        List<String> invalidTenants =
                tenants.stream().filter(tenant -> !TenantStatus.NORMAL.equals(tenant.getStatus()))
                        .map(ObTenantEntity::getName).collect(Collectors.toList());
        ExceptionUtils.require(CollectionUtils.isEmpty(invalidTenants), ErrorCodes.OB_TENANT_STATUS_NOT_NORMAL,
                invalidTenants);
    }

    private static TenantCompaction mapToModel(ObTenantCompaction compaction) {
        TenantCompaction model = TenantCompaction.builder()
                .broadcastScn(compaction.getBroadcastScn())
                .frozenScn(compaction.getFrozenScn())
                .error(compaction.getError())
                .lastScn(compaction.getLastScn())
                .status(compaction.getStatus())
                .suspend(compaction.getSuspend())
                .obTenantId(compaction.getObTenantId())
                .build();
        OffsetDateTime startTime = compaction.getStartTime();
        OffsetDateTime finishTime = compaction.getLastFinishTime();
        if (startTime != null && startTime.toEpochSecond() != 0) {
            model.setStartTime(startTime);
        }
        if (finishTime != null && finishTime.toEpochSecond() != 0) {
            model.setLastFinishTime(finishTime);
        }
        return model;
    }
}
