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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oceanbase.ocp.common.lang.Pair;
import com.oceanbase.ocp.common.util.json.JsonUtils;
import com.oceanbase.ocp.core.exception.IllegalArgumentException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.cluster.ManagedCluster;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.ob.tenant.TenantStatus;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obops.internal.tenant.task.AlterResourcePoolUnitSpecTask;
import com.oceanbase.ocp.obops.internal.tenant.task.AlterTenantUnitCountTask;
import com.oceanbase.ocp.obops.internal.tenant.task.DeleteResourcePoolTask;
import com.oceanbase.ocp.obops.internal.tenant.task.PrepareAlterTenantTask;
import com.oceanbase.ocp.obops.internal.tenant.task.PrepareResourcePoolTask;
import com.oceanbase.ocp.obops.internal.tenant.task.SyncTenantInfoTask;
import com.oceanbase.ocp.obops.internal.tenant.task.UpdateTenantStatusTask;
import com.oceanbase.ocp.obops.internal.tenant.task.WaitAlterLocalityTask;
import com.oceanbase.ocp.obops.internal.tenant.task.WaitAlterUnitCountTask;
import com.oceanbase.ocp.obops.internal.tenant.util.LocalityUtils;
import com.oceanbase.ocp.obops.tenant.ResourcePoolService;
import com.oceanbase.ocp.obops.tenant.TenantReplicaService;
import com.oceanbase.ocp.obops.tenant.model.ReplicaType;
import com.oceanbase.ocp.obops.tenant.param.AddReplicaParam;
import com.oceanbase.ocp.obops.tenant.param.CreateResourcePoolParam;
import com.oceanbase.ocp.obops.tenant.param.DeleteReplicaParam;
import com.oceanbase.ocp.obops.tenant.param.ModifyReplicaParam;
import com.oceanbase.ocp.obops.tenant.param.UnitSpecParam;
import com.oceanbase.ocp.obsdk.operator.TenantOperator;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServerInnerStatus;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObZone;
import com.oceanbase.ocp.obsdk.operator.tenant.model.ObTenant;
import com.oceanbase.ocp.task.TaskInstanceManager;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.model.Argument;
import com.oceanbase.ocp.task.model.TaskInstance;
import com.oceanbase.ocp.task.runtime.Template;
import com.oceanbase.ocp.task.runtime.TemplateBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TenantReplicaServiceImpl implements TenantReplicaService {

    @Autowired
    private ManagedCluster managedCluster;

    @Resource
    private ObOperatorFactory obOperatorFactory;

    @Resource
    private TenantDaoManager tenantDaoManager;

    @Resource
    private TaskInstanceManager taskInstanceManager;

    @Resource
    private ResourcePoolService resourcePoolService;

    @Override
    public TaskInstance addReplica(Long tenantId, List<AddReplicaParam> paramList) {
        Validate.isTrue(paramList.size() == 1, "param size not valid");
        AddReplicaParam param = paramList.get(0);

        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(tenantId);
        ExceptionUtils.illegalArgs(StringUtils.equals(TenantStatus.NORMAL.toString(), entity.getStatus().toString()),
                ErrorCodes.OB_TENANT_STATUS_NOT_NORMAL, entity.getName());

        validateParam(param);

        TenantOperator operator = obOperatorFactory.createObOperator().tenant();
        ObTenant obTenant = operator.getTenant(entity.getObTenantId());
        ExceptionUtils.illegalArgs(isTenantZoneNotExist(obTenant.getLocality(), param.getZoneName()),
                ErrorCodes.OB_TENANT_ZONE_NOT_VALID, param.getZoneName());

        CreateResourcePoolParam poolParam = buildCreateResourcePoolParam(entity.getName(), param);
        resourcePoolService.addResourcePool(tenantId, poolParam);

        try {
            // Alter locality before task in order to pop error message directly if the
            // locality is not valid.
            String locality = LocalityUtils.scaleOut(obTenant.getLocality(), param.getZoneName(),
                    param.getReplicaType().toString());
            operator.modifyLocality(entity.getName(), locality);
        } catch (Exception ex) {
            resourcePoolService.reduceResourcePool(tenantId, param.getZoneName());
            log.error("modify locality failed, exception:", ex);
            throw ex;
        }

        Argument args = new Argument();
        args.put(ContextKey.OB_TENANT_ID.getValue(), String.valueOf(tenantId));
        args.put(ContextKey.ZONE_NAME.getValue(), param.getZoneName());

        TemplateBuilder templateBuilder = new TemplateBuilder();
        Template template = templateBuilder.name("Add tenant replica")
                .addNode(new PrepareAlterTenantTask())
                .andThen(new WaitAlterLocalityTask())
                .andThen(new SyncTenantInfoTask())
                .andThen(new UpdateTenantStatusTask())
                .build();

        return taskInstanceManager.submitManualTask(template, args);
    }

    private CreateResourcePoolParam buildCreateResourcePoolParam(String tenantName, AddReplicaParam param) {
        return CreateResourcePoolParam.builder()
                .tenantName(tenantName)
                .zoneName(param.getZoneName())
                .unitSpec(param.getResourcePool().getUnitSpec())
                .unitCount(param.getResourcePool().getUnitCount())
                .build();
    }

    @Override
    public TaskInstance deleteReplica(Long tenantId, List<DeleteReplicaParam> paramList) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(tenantId);
        ExceptionUtils.illegalArgs(StringUtils.equals(TenantStatus.NORMAL.toString(), entity.getStatus().toString()),
                ErrorCodes.OB_TENANT_STATUS_NOT_NORMAL, entity.getName());

        validateParam(paramList);

        List<String> zoneNames = paramList.stream()
                .map(DeleteReplicaParam::getZoneName)
                .collect(Collectors.toList());

        TenantOperator operator = obOperatorFactory.createObOperator().tenant();
        ObTenant obTenant = operator.getTenant(entity.getObTenantId());
        for (String zoneName : zoneNames) {
            ExceptionUtils.illegalArgs(isTenantZoneExist(obTenant.getLocality(), zoneName),
                    ErrorCodes.OB_TENANT_ZONE_NOT_VALID, zoneName);
        }

        // Alter locality before task in order to pop error message directly if the
        // locality is not valid.
        String locality = LocalityUtils.scaleIn(obTenant.getLocality(), zoneNames);
        operator.modifyLocality(entity.getName(), locality);

        Argument args = new Argument();
        args.put(ContextKey.OB_TENANT_ID.getValue(), String.valueOf(tenantId));
        args.putList(ContextKey.ZONE_NAMES.getValue(), zoneNames);

        TemplateBuilder templateBuilder = new TemplateBuilder();
        Template template = templateBuilder.name("Delete tenant replica")
                .addNode(new PrepareAlterTenantTask())
                .andThen(new WaitAlterLocalityTask())
                .andThen(new PrepareResourcePoolTask())
                .andThen(new DeleteResourcePoolTask(), ContextKey.ZONE_NAMES.getValue())
                .andThen(new SyncTenantInfoTask())
                .andThen(new UpdateTenantStatusTask())
                .build();

        return taskInstanceManager.submitManualTask(template, args);
    }

    @Override
    @Transactional
    public TaskInstance modifyReplica(Long tenantId, List<ModifyReplicaParam> paramList) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(tenantId);
        ExceptionUtils.illegalArgs(StringUtils.equals(TenantStatus.NORMAL.toString(), entity.getStatus().toString()),
                ErrorCodes.OB_TENANT_STATUS_NOT_NORMAL, entity.getName());

        validateModifyReplicaParam(entity, paramList);

        TenantOperator operator = obOperatorFactory.createObOperator().tenant();
        ObTenant obTenant = operator.getTenant(entity.getObTenantId());

        for (ModifyReplicaParam param : paramList) {
            checkTenantZoneExist(obTenant, param.getZoneName());
        }

        Optional<Pair<String, ReplicaType>> replicaTypeChange = getReplicaTypeChange(obTenant, paramList);
        boolean replicaTypeChanged = replicaTypeChange.isPresent();
        if (replicaTypeChanged) {
            // Alter locality before task in order to pop error message directly if the
            // locality is not valid.
            String zoneName = replicaTypeChange.get().getLeft();
            ReplicaType replicaType = replicaTypeChange.get().getRight();
            log.info("replica changed for zone {}, modify replica type to {}", zoneName, replicaType);
            String locality = LocalityUtils.alterReplicaType(obTenant.getLocality(), zoneName, replicaType.toString());
            operator.modifyLocality(entity.getName(), locality);
        }

        Argument args = new Argument();
        args.put(ContextKey.OB_TENANT_ID.getValue(), String.valueOf(tenantId));

        List<String> zoneNameList = paramList.stream()
                .map(ModifyReplicaParam::getZoneName)
                .collect(Collectors.toList());
        List<UnitSpecParam> unitSpecList = paramList.stream()
                .map(param -> param.getResourcePool() == null ? null : param.getResourcePool().getUnitSpec())
                .collect(Collectors.toList());
        List<String> unitCountList = paramList.stream()
                .map(param -> param.getResourcePool() == null ? "" : param.getResourcePool().getUnitCount())
                .map(unitCount -> unitCount == null ? null : String.valueOf(unitCount))
                .collect(Collectors.toList());

        boolean alterResourcePoolUnitSpec = unitSpecList.stream().anyMatch(Objects::nonNull);
        boolean alterTenantUnitCount = unitCountList.stream().allMatch(StringUtils::isNotEmpty);

        args.putList(ContextKey.ZONE_NAMES, zoneNameList);
        args.putList(ContextKey.UNIT_SPEC_JSON_LIST,
                unitSpecList.stream().map(JsonUtils::toJsonString).collect(Collectors.toList()));
        if (alterTenantUnitCount) {
            args.put(ContextKey.UNIT_COUNT, unitCountList.get(0));
        }

        TemplateBuilder templateBuilder = new TemplateBuilder();
        templateBuilder.name("Modify tenant replica");
        templateBuilder.andThen(new PrepareAlterTenantTask());
        if (replicaTypeChanged) {
            templateBuilder.andThen(new WaitAlterLocalityTask());
        }
        templateBuilder.andThen(new PrepareResourcePoolTask());
        if (alterResourcePoolUnitSpec) {
            templateBuilder.andThen(new AlterResourcePoolUnitSpecTask(), ContextKey.ZONE_NAMES.getValue());
        }
        if (alterTenantUnitCount) {
            templateBuilder.andThen(new AlterTenantUnitCountTask());
        }
        templateBuilder.andThen(new WaitAlterUnitCountTask());
        Template template = templateBuilder
                .andThen(new SyncTenantInfoTask())
                .andThen(new UpdateTenantStatusTask())
                .build();
        return taskInstanceManager.submitManualTask(template, args);
    }

    private Optional<Pair<String, ReplicaType>> getReplicaTypeChange(ObTenant obTenant,
            List<ModifyReplicaParam> paramList) {
        // Replica type is not allowed to change when modifying tenant replica in batch.
        if (paramList.size() != 1) {
            return Optional.empty();
        }
        ModifyReplicaParam param = paramList.get(0);
        if (param.getReplicaType() == null) {
            return Optional.empty();
        }
        Map<String, String> replicaTypeMap = LocalityUtils.buildReplicaTypeMap(obTenant.getLocality());
        String currentReplicaType = replicaTypeMap.get(param.getZoneName());
        boolean replicaTypeChanged = !Objects.equals(ReplicaType.fromValue(currentReplicaType), param.getReplicaType());
        if (replicaTypeChanged) {
            return Optional.of(Pair.of(param.getZoneName(), param.getReplicaType()));
        } else {
            return Optional.empty();
        }
    }

    private void validateParam(AddReplicaParam param) {
        ExceptionUtils.illegalArgs(param.getResourcePool().getUnitCount() > 0,
                ErrorCodes.OB_TENANT_UNIT_COUNT_NOT_VALID);
        long activeServerNum = managedCluster.servers().stream()
                .filter(server -> StringUtils.equals(server.getZone(), param.getZoneName()))
                .filter(server -> StringUtils.equals(server.getStatus(), ObServerInnerStatus.ACTIVE.toString()))
                .count();
        ExceptionUtils.illegalArgs(param.getResourcePool().getUnitCount() <= activeServerNum,
                ErrorCodes.OB_TENANT_UNIT_COUNT_EXCEED_ACTIVE_SERVER);
    }

    private void validateParam(List<DeleteReplicaParam> paramList) {
        List<String> zones = managedCluster.zones().stream().map(ObZone::getZone).collect(Collectors.toList());
        paramList.stream()
                .map(DeleteReplicaParam::getZoneName)
                .forEach(zone -> {
                    ExceptionUtils.illegalArgs(zones.contains(zone), ErrorCodes.OB_ZONE_NAME_NOT_FOUND,
                            managedCluster.obClusterId(), zone);
                });
    }

    private void validateModifyReplicaParam(ObTenantEntity entity, List<ModifyReplicaParam> paramList) {
        ExceptionUtils.require(CollectionUtils.isNotEmpty(paramList), ErrorCodes.COMMON_ILLEGAL_ARGUMENT,
                "List<ModifyReplicaParam>");
        for (ModifyReplicaParam param : paramList) {
            validateParam(param);
        }

        List<String> zoneList = entity.getZoneList();
        List<Long> unitCountList = paramList.stream()
                .filter(param -> param.getResourcePool() != null && param.getResourcePool().getUnitCount() != null)
                .map(param -> param.getResourcePool().getUnitCount())
                .collect(Collectors.toList());
        ExceptionUtils.require(new HashSet<>(unitCountList).size() <= 1,
                ErrorCodes.OB_TENANT_UNIT_COUNT_NOT_SAME_IN_EACH_ZONES);
        ExceptionUtils.require(unitCountList.isEmpty() || unitCountList.size() == zoneList.size(),
                ErrorCodes.OB_TENANT_UNIT_COUNT_PARTIALLY_MODIFIED);
    }

    private void validateParam(ModifyReplicaParam param) {
        if (param.getReplicaType() == null && param.getResourcePool() == null) {
            ExceptionUtils.throwException(IllegalArgumentException.class, ErrorCodes.COMMON_ILLEGAL_ARGUMENT,
                    ModifyReplicaParam.class.getSimpleName());
        }

        if (param.getResourcePool() != null) {
            UnitSpecParam unitSpec = param.getResourcePool().getUnitSpec();
            Long unitCount = param.getResourcePool().getUnitCount();
            if (unitSpec == null && unitCount == null) {
                ExceptionUtils.throwException(IllegalArgumentException.class, ErrorCodes.COMMON_ILLEGAL_ARGUMENT,
                        ModifyReplicaParam.class.getSimpleName());
            }
            if (unitCount != null) {
                ExceptionUtils.illegalArgs(unitCount > 0, ErrorCodes.OB_TENANT_UNIT_COUNT_NOT_VALID);
                long activeServerNum = managedCluster.servers().stream()
                        .filter(server -> StringUtils.equals(server.getZone(), param.getZoneName()))
                        .filter(server -> StringUtils.equals(server.getStatus(), ObServerInnerStatus.ACTIVE.toString()))
                        .count();
                ExceptionUtils.illegalArgs(unitCount <= activeServerNum,
                        ErrorCodes.OB_TENANT_UNIT_COUNT_EXCEED_ACTIVE_SERVER);
            }
        }
    }

    private void checkTenantZoneExist(ObTenant obTenant, String zoneName) {
        ExceptionUtils.require(isTenantZoneExist(obTenant.getLocality(), zoneName), ErrorCodes.OB_TENANT_ZONE_NOT_VALID,
                zoneName);
    }

    private boolean isTenantZoneNotExist(String locality, String zoneName) {
        return !isTenantZoneExist(locality, zoneName);
    }

    private boolean isTenantZoneExist(String locality, String zoneName) {
        List<String> zoneList = LocalityUtils.getZoneList(locality);
        return zoneList.contains(zoneName);
    }
}
