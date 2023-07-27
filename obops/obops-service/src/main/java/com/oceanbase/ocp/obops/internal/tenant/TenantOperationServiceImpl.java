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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import com.oceanbase.ocp.common.util.json.JsonUtils;
import com.oceanbase.ocp.core.constants.OcpConstants;
import com.oceanbase.ocp.core.credential.operator.ObCredentialOperator;
import com.oceanbase.ocp.core.exception.IllegalArgumentException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.cluster.ManagedCluster;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.core.ob.tenant.TenantStatus;
import com.oceanbase.ocp.core.obsdk.ObAccessorFactory;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.core.property.SystemInfo;
import com.oceanbase.ocp.core.security.AuthenticationFacade;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obops.database.DbUserService;
import com.oceanbase.ocp.obops.database.param.ModifyDbUserPasswordParam;
import com.oceanbase.ocp.obops.internal.tenant.task.CreateTenantTask;
import com.oceanbase.ocp.obops.internal.tenant.task.PrepareCreateTenantTask;
import com.oceanbase.ocp.obops.internal.tenant.task.SetObTenantParametersTask;
import com.oceanbase.ocp.obops.internal.tenant.task.SetSuperUserPasswordTask;
import com.oceanbase.ocp.obops.internal.tenant.task.SetSystemVariablesTask;
import com.oceanbase.ocp.obops.internal.tenant.task.SetWhitelistTask;
import com.oceanbase.ocp.obops.internal.tenant.task.SyncTenantInfoTask;
import com.oceanbase.ocp.obops.internal.tenant.task.UpdateTenantStatusTask;
import com.oceanbase.ocp.obops.internal.tenant.util.TenantParameterUtils;
import com.oceanbase.ocp.obops.parameter.model.TenantParameterType;
import com.oceanbase.ocp.obops.parameter.param.TenantParameterParam;
import com.oceanbase.ocp.obops.tenant.ResourcePoolService;
import com.oceanbase.ocp.obops.tenant.TenantOperationService;
import com.oceanbase.ocp.obops.tenant.TenantService;
import com.oceanbase.ocp.obops.tenant.model.CheckTenantPasswordResult;
import com.oceanbase.ocp.obops.tenant.model.ResourcePool;
import com.oceanbase.ocp.obops.tenant.param.CreatePasswordInVaultParam;
import com.oceanbase.ocp.obops.tenant.param.CreateResourcePoolParam;
import com.oceanbase.ocp.obops.tenant.param.CreateTenantParam;
import com.oceanbase.ocp.obops.tenant.param.CreateTenantParam.ZoneParam;
import com.oceanbase.ocp.obops.tenant.param.TenantChangePasswordParam;
import com.oceanbase.ocp.obsdk.accessor.ObAccessor;
import com.oceanbase.ocp.obsdk.accessor.variable.model.SetVariableInput;
import com.oceanbase.ocp.obsdk.enums.VariableValueType;
import com.oceanbase.ocp.obsdk.operator.TenantOperator;
import com.oceanbase.ocp.obsdk.operator.tenant.model.CreateTenantInput;
import com.oceanbase.ocp.obsdk.operator.tenant.model.ObTenant;
import com.oceanbase.ocp.task.TaskInstanceManager;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.model.Argument;
import com.oceanbase.ocp.task.model.TaskInstance;
import com.oceanbase.ocp.task.runtime.Template;
import com.oceanbase.ocp.task.runtime.TemplateBuilder;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TenantOperationServiceImpl implements TenantOperationService {

    // Variables to be filtered when creating a tenant
    private static final Set<String> FILTERED_VARIABLES =
            Sets.newHashSet("ob_tcp_invited_nodes", "ob_compatibility_mode");

    // Variables that must be put in the CREATE TENANT statement to take effect.
    private static final Set<String> VARIABLES_WHEN_CREATE_TENANT = Sets.newHashSet(
            // Charset related variables for Oracle mode
            "nls_characterset", "nls_nchar_characterset", "nls_comp", "nls_sort",
            // GLOBAL READONLY variable for MySQL mode
            "lower_case_table_names");

    @Autowired
    private ManagedCluster managedCluster;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private TenantDaoManager tenantDaoManager;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @Autowired
    private TaskInstanceManager taskInstanceManager;

    @Autowired
    private ObOperatorFactory obOperatorFactory;

    @Autowired
    private ObAccessorFactory obAccessorFactory;

    @Autowired
    private ResourcePoolService resourcePoolService;

    @Autowired
    private DbUserService dbUserService;

    @Autowired
    private ObCredentialOperator obCredentialOperator;

    @Autowired
    private SystemInfo systemInfo;

    @Override
    public TaskInstance createTenant(CreateTenantParam param) {
        validateParam(param);
        saveTenantInfo(param);
        return submitCreateTenantTask(param);
    }

    @Override
    public List<ResourcePool> createResourcePoolForTenant(CreateTenantParam param) {
        List<CreateResourcePoolParam> paramList = buildCreateResourcePoolParam(param.getName(), param.getZones());
        return resourcePoolService.createResourcePoolList(paramList);
    }

    @Override
    public ObTenant createTenantOnResourcePool(CreateTenantParam param, List<ResourcePool> resourcePoolList) {
        TenantOperator operator = obOperatorFactory.createObOperator().tenant();
        return createTenantOnResourcePool(operator, param, resourcePoolList);
    }

    private List<CreateResourcePoolParam> buildCreateResourcePoolParam(String tenantName, List<ZoneParam> zoneList) {
        return zoneList.stream()
                .map(zone -> buildCreateResourcePoolParam(tenantName, zone))
                .collect(Collectors.toList());
    }

    private CreateResourcePoolParam buildCreateResourcePoolParam(String tenantName, ZoneParam zone) {
        return CreateResourcePoolParam.builder()
                .tenantName(tenantName)
                .zoneName(zone.getName())
                .unitSpec(zone.getResourcePool().getUnitSpec())
                .unitCount(zone.getResourcePool().getUnitCount())
                .build();
    }

    private ObTenant createTenantOnResourcePool(TenantOperator operator, CreateTenantParam param,
            List<ResourcePool> resourcePoolList) {
        CreateTenantInput input = buildCreateTenantInput(param, resourcePoolList);
        return operator.createTenant(input);
    }

    private CreateTenantInput buildCreateTenantInput(CreateTenantParam param, List<ResourcePool> resourcePoolList) {
        String mode = param.getMode() == null ? null : param.getMode().toString();
        String locality = buildLocality(param.getZones());
        List<String> poolNameList = resourcePoolList.stream()
                .map(ResourcePool::getName)
                .collect(Collectors.toList());
        List<SetVariableInput> variables = param.getParameters().stream()
                .filter(p -> p.getParameterType() == TenantParameterType.OB_SYSTEM_VARIABLE)
                .filter(p -> !FILTERED_VARIABLES.contains(p.getName()))
                .filter(p -> VARIABLES_WHEN_CREATE_TENANT.contains(p.getName()))
                .map(p -> SetVariableInput.builder()
                        .name(p.getName())
                        .value(p.getValue())
                        .type(VariableValueType.INT)
                        .build())
                .collect(Collectors.toList());
        return CreateTenantInput.builder()
                .name(param.getName())
                .resourcePoolList(poolNameList)
                .primaryZone(param.getPrimaryZone())
                .locality(locality)
                .mode(mode)
                .charset(param.getCharset())
                .collation(param.getCollation())
                .variables(variables)
                .build();
    }

    private String buildLocality(List<ZoneParam> zoneList) {
        List<String> replicas = zoneList.stream()
                .map(z -> z.getReplicaType().toString() + "@" + z.getName())
                .collect(Collectors.toList());
        return StringUtils.join(replicas, ",");
    }

    @Override
    public void deleteTenant(Long obTenantId) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        requireNotMetadb(entity);
        ExceptionUtils.illegalArgs(StringUtils.equals(TenantStatus.NORMAL.toString(), entity.getStatus().toString()),
                ErrorCodes.OB_TENANT_STATUS_NOT_NORMAL, entity.getName());
        ExceptionUtils.illegalArgs(!StringUtils.equals(entity.getName(), OcpConstants.TENANT_SYS),
                ErrorCodes.OB_TENANT_DELETE_NOT_ALLOWED, entity.getName());

        List<ResourcePool> poolList = resourcePoolService.listResourcePool(entity);

        TenantOperator operator = obOperatorFactory.createObOperator().tenant();
        operator.deleteTenant(entity.getName());

        resourcePoolService.deleteResourcePoolList(poolList);

        tenantService.deleteTenantRelatedInfo(obTenantId);
        tenantService.deleteTenantInfo(obTenantId);
    }

    @Override
    public void deleteObTenant(String tenantName) {
        requireNotMetadb(tenantName);
        TenantOperator tenantOperator = obOperatorFactory.createObOperator().tenant();
        tenantOperator.deleteTenant(tenantName);
    }

    @Override
    public void lockTenant(Long tenantId) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetTenant(tenantId);
        requireNotMetadb(entity);

        TenantOperator operator = obOperatorFactory.createTenantOperator();
        operator.lockTenant(entity.getName());

        tenantDaoManager.updateLockStatus(tenantId, true);
    }

    @Override
    public void unlockTenant(Long tenantId) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetTenant(tenantId);
        requireNotMetadb(entity);

        TenantOperator operator = obOperatorFactory.createTenantOperator();
        operator.unlockTenant(entity.getName());

        tenantDaoManager.updateLockStatus(tenantId, false);
    }

    @Override
    public void modifyPrimaryZone(Long tenantId, String primaryZone) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetTenant(tenantId);
        requireNotMetadb(entity);
        validatePrimaryZone(primaryZone, entity.getZoneList());
        if (StringUtils.equals(entity.getName(), OcpConstants.TENANT_SYS)) {
            validateSysPrimaryZone(primaryZone);
        }

        TenantOperator operator = obOperatorFactory.createTenantOperator();
        operator.modifyPrimaryZone(entity.getName(), primaryZone);

        tenantDaoManager.updatePrimaryZone(tenantId, primaryZone);
    }

    @Override
    public void changePassword(Long tenantId, TenantChangePasswordParam param) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetTenant(tenantId);
        requireNotMetadb(entity);
        ModifyDbUserPasswordParam resetParam = new ModifyDbUserPasswordParam();
        resetParam.setNewPassword(param.getNewPassword());
        dbUserService.modifyPassword(tenantId, entity.getMode().getSuperUser(), resetParam);
    }

    @Override
    public void createOrReplacePassword(CreatePasswordInVaultParam param) {
        log.info("Begin to create or replace tenant password in vault, ob tenant:{}", param.getTenantName());
        ObTenantEntity tenant = tenantDaoManager.nullSafeGetTenant(param.getTenantName());
        CheckTenantPasswordResult checkResult = checkTenantPassword(param);
        ExceptionUtils.require(checkResult.isSuccessful(), ErrorCodes.OB_TENANT_CONNECT_FAILED, tenant.getName());
        String username = TenantMode.MYSQL.equals(tenant.getMode()) ? "root" : "SYS";
        obCredentialOperator.saveObCredential(managedCluster.clusterName(), tenant.getName(), username,
                param.getNewPassword());
        log.info("Success create to replace or update tenant password, ob tenant{}", tenant.getName());
    }

    @Override
    public CheckTenantPasswordResult checkTenantPassword(CreatePasswordInVaultParam param) {
        log.info("Begin to check tenant password, ob tenant:{}", param.getTenantName());
        ObTenantEntity tenant = tenantDaoManager.nullSafeGetTenant(param.getTenantName());
        try {
            ObAccessor obAccessor = obAccessorFactory.createObAccessor(tenant.getName(), tenant.getMode(),
                    param.getNewPassword());
            obAccessor.info().now();
            return CheckTenantPasswordResult.builder().successful(true).build();
        } catch (Throwable th) {
            log.warn("Failed to check tenant password, ob tenant:{}", tenant.getName(), th);
            return CheckTenantPasswordResult.builder().successful(false).failedReason(th.getMessage()).build();
        }
    }

    private void validateParam(CreateTenantParam param) {
        ExceptionUtils.illegalArgs(tenantDaoManager.isTenantNotExist(param.getName()),
                ErrorCodes.OB_TENANT_NAME_EXIST, managedCluster.clusterName(), param.getName());

        // TODO more checks
    }

    private void validatePrimaryZone(String primaryZone, List<String> zoneList) {
        if (StringUtils.isEmpty(primaryZone)) {
            return;
        }
        if ("RANDOM".equals(primaryZone)) {
            return;
        }
        Stream.of(primaryZone.split(";"))
                .flatMap(pz -> Stream.of(pz.split(",")))
                .forEach(zone -> ExceptionUtils.illegalArgs(zoneList.contains(zone),
                        ErrorCodes.OB_TENANT_ZONE_NOT_FOUND, zone));
    }

    /**
     * Check primary zone for sys tenant: <br>
     * 1. RANDOM not allowed <br>
     * 2. there can only be one zone in the first priority
     */
    private void validateSysPrimaryZone(String primaryZone) {
        if (StringUtils.isEmpty(primaryZone)) {
            return;
        }
        if (StringUtils.equalsIgnoreCase("RANDOM", primaryZone)) {
            throw new IllegalArgumentException(ErrorCodes.OB_CLUSTER_PRIMARY_ZONE_INVALID,
                    "sys tenant primary zone RANDOM not supported");
        }
        String firstPriority = primaryZone.split(";")[0];
        String[] zoneArr = firstPriority.split(",");
        if (zoneArr.length > 1) {
            throw new IllegalArgumentException(ErrorCodes.OB_CLUSTER_PRIMARY_ZONE_INVALID,
                    "more than 1 zone in first priority");
        }
    }

    private ObTenantEntity saveTenantInfo(CreateTenantParam param) {
        ObTenantEntity entity = new ObTenantEntity();
        entity.setCreator(authenticationFacade.currentUserName());
        entity.setName(param.getName());
        // As tenant is not created yet, obTenantId is not available, fill it with a
        // random negative value to satisfy the unique constraint of ob_tenant table.
        entity.setObTenantId(-RandomUtils.nextLong(1001, 10000));
        entity.setMode(param.getMode());
        entity.setStatus(TenantStatus.CREATING);
        entity.setPrimaryZone(param.getPrimaryZone());
        List<String> zoneList = param.getZones().stream().map(ZoneParam::getName).collect(Collectors.toList());
        String locality = param.getZones().stream()
                .map(zoneParam -> String.format("%s{%d}@%s", zoneParam.getReplicaType().getValue(),
                        zoneParam.getResourcePool().getUnitCount(), zoneParam.getName()))
                .collect(Collectors.joining(","));
        entity.setZoneList(zoneList);
        entity.setLocality(locality);
        entity.setDescription(param.getDescription());
        saveTenantPasswordToCredential(param.getName(), param.getMode(), param.getRootPassword());
        return tenantDaoManager.saveTenant(entity);
    }

    private void saveTenantPasswordToCredential(String tenantName, TenantMode tenantMode, String password) {
        String clusterName = managedCluster.clusterName();
        String username = tenantMode.getSuperUser();
        obCredentialOperator.saveObCredential(clusterName, tenantName, username, password);
    }

    private TaskInstance submitCreateTenantTask(CreateTenantParam param) {
        Argument args = new Argument();

        args.put(ContextKey.TENANT_NAME, param.getName());
        args.put(ContextKey.TENANT_MODE, param.getMode().toString());
        // rootPassword will be masked in JSON serialization, but it is ok as
        // rootPassword is not used in task.
        args.put(ContextKey.CREATE_TENANT_PARAM_JSON, JsonUtils.toJsonString(param));
        args.put(ContextKey.WHITELIST, param.getWhitelist());

        List<TenantParameterParam> obSystemVariables = param.getParameters().stream()
                .filter(p -> p.getParameterType() == TenantParameterType.OB_SYSTEM_VARIABLE)
                .filter(p -> !FILTERED_VARIABLES.contains(p.getName()))
                .filter(p -> !VARIABLES_WHEN_CREATE_TENANT.contains(p.getName()))
                .collect(Collectors.toList());
        List<TenantParameterParam> obTenantParameters =
                param.getParameters().stream()
                        .filter(p -> p.getParameterType() == TenantParameterType.OB_TENANT_PARAMETER)
                        .collect(Collectors.toList());
        args.put(ContextKey.SYSTEM_VARIABLE_MAP,
                TenantParameterUtils.paramsToContextString(obSystemVariables));
        args.put(ContextKey.OB_TENANT_PARAMETER_MAP,
                TenantParameterUtils.paramsToContextString(obTenantParameters));

        TemplateBuilder templateBuilder = new TemplateBuilder();
        templateBuilder.name("Create tenant");
        templateBuilder.andThen(new PrepareCreateTenantTask())
                .andThen(new CreateTenantTask())
                .andThen(new SyncTenantInfoTask());
        if (StringUtils.isNotEmpty(param.getRootPassword())) {
            args.put(ContextKey.OLD_PASSWORD.toString(), "");
            args.put(ContextKey.NEW_PASSWORD.toString(), param.getRootPassword());
            templateBuilder.andThen(new SetSuperUserPasswordTask());
        }
        if (StringUtils.isNotEmpty(param.getWhitelist())) {
            templateBuilder.andThen(new SetWhitelistTask());
        }
        templateBuilder.andThen(new SetSystemVariablesTask())
                .andThen(new SetObTenantParametersTask());
        templateBuilder.andThen(new UpdateTenantStatusTask());

        Template template = templateBuilder.build();
        return taskInstanceManager.submitManualTask(template, args);
    }

    private void requireNotMetadb(ObTenantEntity entity) {
        requireNotMetadb(entity.getName());
    }

    private void requireNotMetadb(String tenantName) {
        ExceptionUtils.require(!StringUtils.equals(systemInfo.getMetaTenantName(), tenantName),
                ErrorCodes.OB_TENANT_METADB_OPERATION_RESTRICTED);
    }
}
