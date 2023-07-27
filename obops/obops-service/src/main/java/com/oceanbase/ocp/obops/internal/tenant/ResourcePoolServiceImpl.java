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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.common.lang.Pair;
import com.oceanbase.ocp.core.exception.UnexpectedException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.obops.tenant.ResourcePoolService;
import com.oceanbase.ocp.obops.tenant.model.ResourcePool;
import com.oceanbase.ocp.obops.tenant.model.Unit;
import com.oceanbase.ocp.obops.tenant.model.UnitConfig;
import com.oceanbase.ocp.obops.tenant.param.CreateResourcePoolParam;
import com.oceanbase.ocp.obops.tenant.param.UnitSpecParam;
import com.oceanbase.ocp.obsdk.exception.OceanBaseException;
import com.oceanbase.ocp.obsdk.operator.ObOperator;
import com.oceanbase.ocp.obsdk.operator.ResourceOperator;
import com.oceanbase.ocp.obsdk.operator.resource.model.CreateResourcePoolInput;
import com.oceanbase.ocp.obsdk.operator.resource.model.CreateUnitConfigInput;
import com.oceanbase.ocp.obsdk.operator.resource.model.ListUnitInput;
import com.oceanbase.ocp.obsdk.operator.resource.model.ObResourcePool;
import com.oceanbase.ocp.obsdk.operator.resource.model.ObUnit;
import com.oceanbase.ocp.obsdk.operator.resource.model.ObUnitConfig;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ResourcePoolServiceImpl implements ResourcePoolService {

    private static final String RESOURCE_POOL_MARK = "pool";
    private static final String UNIT_CONFIG_MARK = "config";

    @Autowired
    private TenantDaoManager tenantDaoManager;

    @Autowired
    private ObOperatorFactory obOperatorFactory;

    @Override
    public List<ResourcePool> createResourcePoolList(List<CreateResourcePoolParam> paramList) {
        ObOperator operator = obOperatorFactory.createObOperator();
        List<Pair<RuntimeException, ResourcePool>> eitherList = paramList.parallelStream()
                .map(param -> createResourcePoolReturnEither(operator, param))
                .collect(Collectors.toList());

        List<ResourcePool> poolList = eitherList.stream()
                .map(Pair::getRight)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<RuntimeException> exceptionList = eitherList.stream()
                .map(Pair::getLeft)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(exceptionList)) {
            log.error("create resource pool list failed, error message:{}", exceptionList.get(0).getMessage());
            deleteResourcePoolList(poolList);
            throw exceptionList.get(0);
        }
        return poolList;
    }

    @Override
    public void deleteResourcePoolList(List<ResourcePool> poolList) {
        ObOperator operator = obOperatorFactory.createObOperator();
        poolList.parallelStream().forEach(pool -> deleteResourcePoolQuietly(operator, pool));
    }

    @Override
    public ResourcePool addResourcePool(Long tenantId, CreateResourcePoolParam param) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(tenantId);
        ObOperator operator = obOperatorFactory.createObOperator();

        ResourcePool resourcePool = createResourcePool(operator, param);

        try {
            attachResourcePool(operator, entity, resourcePool.getName());
        } catch (Exception ex) {
            deleteResourcePoolQuietly(operator, resourcePool);
            log.error("attach resource pool to tenant failed, error message:{}", ex.getMessage());
            throw ex;
        }
        return resourcePool;
    }

    @Override
    public synchronized void reduceResourcePool(Long tenantId, String zoneName) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(tenantId);
        ObOperator operator = obOperatorFactory.createObOperator();

        ObResourcePool obResourcePool = getTargetPool(operator, entity, zoneName);

        detachResourcePool(operator, entity, obResourcePool.getName());

        deleteResourcePool(operator, obResourcePool);
    }

    @Override
    public List<ResourcePool> listResourcePool(ObTenantEntity entity) {
        ResourceOperator operator = obOperatorFactory.createObOperator().resource();
        List<ObResourcePool> poolList = operator.listResourcePool(entity.getObTenantId());
        return poolList.stream().map(this::mapToResourcePool).collect(Collectors.toList());
    }

    private Pair<RuntimeException, ResourcePool> createResourcePoolReturnEither(ObOperator operator,
            CreateResourcePoolParam param) {
        Pair<RuntimeException, ResourcePool> pair = new Pair<>();
        try {
            ResourcePool pool = createResourcePool(operator, param);
            pair = Pair.right(pool);
        } catch (OceanBaseException e) {
            pair = Pair.left(e);
        } catch (Exception e) {
            pair = Pair.left(new UnexpectedException(ErrorCodes.COMMON_OB_OPERATION_FAILED, e.getMessage()));
        }
        return pair;
    }

    @Override
    public List<Unit> listUnit(Long obTenantId) {
        ResourceOperator operator = obOperatorFactory.createObOperator().resource();
        ListUnitInput input = ListUnitInput.builder().tenantId(obTenantId).build();
        List<ObUnit> unitList = operator.listUnit(input);
        return unitList.stream().map(this::mapToUnit).collect(Collectors.toList());
    }

    @Override
    public void splitResourcePool(Long tenantId) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(tenantId);
        ObOperator operator = obOperatorFactory.createObOperator();

        List<ObResourcePool> poolList = operator.resource().listResourcePool(entity.getObTenantId());
        for (ObResourcePool pool : poolList) {
            splitResourcePool(operator, entity.getName(), pool);
        }
    }

    @Override
    public void modifyResourcePool(Long tenantId, String zoneName, UnitSpecParam unitSpec) {
        Validate.notNull(tenantId, "tenantId can not be null");
        Validate.notNull(zoneName, "zoneName can not be null");
        Validate.notNull(unitSpec, "unitSpec can not be null");

        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(tenantId);

        ObOperator operator = obOperatorFactory.createObOperator();
        ObResourcePool obResourcePool = getTargetPool(operator, entity, zoneName);

        log.info("current unit config: {}, target unit spec: {}", obResourcePool.getObUnitConfig(), unitSpec);
        boolean needToChange = needToChange(unitSpec, obResourcePool.getObUnitConfig());
        if (!needToChange) {
            log.info("modify resource pool unit spec skipped, no need to change unit spec,"
                    + " tenantId={}, zoneName={}, resourcePool={}, unitSpec={}",
                    tenantId, zoneName, obResourcePool.getName(), unitSpec);
            return;
        }
        String unitConfigName = genUnitConfigName(entity.getName(), zoneName, unitSpec);
        CreateUnitConfigInput input = buildCreateUnitConfigInput(unitConfigName, unitSpec);
        ObUnitConfig obUnitConfig = operator.resource().createUnitConfig(input);
        operator.resource().modifyResourcePoolUnitConfig(obResourcePool.getName(), obUnitConfig.getName());
        deleteUnitConfig(operator, obResourcePool.getObUnitConfig().getName());
        log.info("modify resource pool unit spec done, tenantId={}, zoneName={}, resourcePool={}, unitSpec={}",
                tenantId, zoneName, obResourcePool.getName(), unitSpec);
    }

    @Override
    public void modifyTenantUnitCount(Long tenantId, Long unitCount) {
        Validate.notNull(tenantId, "tenantId can not be null");
        Validate.notNull(unitCount, "unitCount can not be null");

        ObTenantEntity entity = tenantDaoManager.nullSafeGetTenant(tenantId);

        ObOperator operator = obOperatorFactory.createObOperator();
        List<ObResourcePool> obResourcePoolList = getTargetPoolList(operator, entity);
        List<String> resourcePoolNames = obResourcePoolList.stream()
                .map(ObResourcePool::getName)
                .collect(Collectors.toList());

        if (obResourcePoolList.stream().allMatch(pool -> Objects.equals(pool.getUnitCount(), unitCount))) {
            log.info("modify tenant unit count skipped, no need to change unit count,"
                    + " tenantId={}, resourcePoolList={}, unitCount={}",
                    tenantId, resourcePoolNames, unitCount);
            return;
        }

        operator.resource().modifyTenantUnitCount(entity.getName(), unitCount);
        log.info("modify tenant unit count done, tenantId={}, resourcePoolList={}, unitCount={}",
                tenantId, resourcePoolNames, unitCount);
    }

    private boolean needToChange(UnitSpecParam unitSpec, ObUnitConfig obUnitConfig) {
        return !StringUtils.equals(unitSpec.getCpuCore().toString(), obUnitConfig.getMaxCpu().toString())
                || !StringUtils.equals(unitSpec.getCpuCore().toString(), obUnitConfig.getMinCpu().toString())
                || !StringUtils.equals(unitSpec.getMemoryBytes().toString(), obUnitConfig.getMaxMemory().toString())
                || !StringUtils.equals(unitSpec.getMemoryBytes().toString(), obUnitConfig.getMinMemory().toString());
    }

    private ResourcePool createResourcePool(ObOperator operator, CreateResourcePoolParam param) {
        String randomStr = genRandomStr();

        String unitConfigName =
                genUnitConfigName(param.getTenantName(), param.getZoneName(), param.getUnitSpec(), randomStr);
        CreateUnitConfigInput unitConfigInput = buildCreateUnitConfigInput(unitConfigName, param.getUnitSpec());
        ObUnitConfig obUnitConfig = operator.resource().createUnitConfig(unitConfigInput);

        try {
            String poolName = genResourcePoolName(param.getTenantName(), param.getZoneName(), randomStr);
            CreateResourcePoolInput poolInput = buildCreateResourcePoolInput(poolName, obUnitConfig.getName(), param);
            ObResourcePool obResourcePool = operator.resource().createResourcePool(poolInput);
            return mapToResourcePool(obResourcePool);
        } catch (Exception ex) {
            log.error("create resource pool failed, error message:{}", ex.getMessage());
            deleteUnitConfig(operator, unitConfigName);
            throw ex;
        }
    }

    private void deleteResourcePoolQuietly(ObOperator operator, ResourcePool pool) {
        try {
            deleteResourcePool(operator, pool);
            log.info("resource pool deleted, poolName={}, poolId={}", pool.getName(), pool.getId());
        } catch (Exception ex) {
            log.error("delete resource pool error, poolName={}, poolId={}, error message:{}", pool.getName(),
                    pool.getId(), ex.getMessage());
        }
    }

    private void deleteResourcePool(ObOperator operator, ObResourcePool pool) {
        ResourcePool resourcePool = mapToResourcePool(pool);
        deleteResourcePool(operator, resourcePool);
    }

    private void deleteResourcePool(ObOperator operator, ResourcePool pool) {
        operator.resource().deleteResourcePool(pool.getName());
        deleteUnitConfig(operator, pool.getUnitConfig().getName());
    }

    private void deleteUnitConfig(ObOperator operator, String unitConfigName) {
        try {
            operator.resource().deleteUnitConfig(unitConfigName);
        } catch (RuntimeException ex) {
            log.warn("delete resource unit config failed. unitConfigName={}, error message:{}", unitConfigName,
                    ex.getMessage());
        }
    }

    private ObResourcePool findObResourcePool(ObOperator operator, Long obTenantId, String zoneName) {
        List<ObResourcePool> poolList = operator.resource().listResourcePool(obTenantId);
        return poolList.stream()
                .filter(pool -> pool.getZoneList().contains(zoneName))
                .findFirst()
                .orElseThrow(() -> new UnexpectedException(ErrorCodes.OB_TENANT_RESOURCE_POOL_NOT_FOUND, zoneName));
    }

    private ObResourcePool getTargetPool(ObOperator operator, ObTenantEntity entity, String zoneName) {
        ObResourcePool obResourcePool = findObResourcePool(operator, entity.getObTenantId(), zoneName);

        if (splitResourcePool(operator, entity.getName(), obResourcePool)) {
            obResourcePool = findObResourcePool(operator, entity.getObTenantId(), zoneName);
        }
        return obResourcePool;
    }

    private List<ObResourcePool> getTargetPoolList(ObOperator operator, ObTenantEntity entity) {
        List<ObResourcePool> poolList = operator.resource().listResourcePool(entity.getObTenantId());
        boolean split = false;
        for (ObResourcePool obResourcePool : poolList) {
            if (splitResourcePool(operator, entity.getName(), obResourcePool)) {
                split = true;
            }
        }
        if (split) {
            poolList = operator.resource().listResourcePool(entity.getObTenantId());
        }
        return poolList;
    }

    private boolean splitResourcePool(ObOperator operator, String tenantName, ObResourcePool obResourcePool) {
        if (CollectionUtils.size(obResourcePool.getZoneList()) > 1) {
            List<String> poolNameList = genResourcePoolName(tenantName, obResourcePool.getZoneList());
            operator.resource().splitResourcePool(obResourcePool.getName(), poolNameList, obResourcePool.getZoneList());
            return true;
        }
        return false;
    }

    private void attachResourcePool(ObOperator operator, ObTenantEntity entity, String poolName) {

        List<ObResourcePool> poolList = operator.resource().listResourcePool(entity.getObTenantId());

        List<String> poolNameList = poolList.stream().map(ObResourcePool::getName).collect(Collectors.toList());
        poolNameList.add(poolName);

        operator.tenant().modifyResourcePoolList(entity.getName(), poolNameList);
    }

    private void detachResourcePool(ObOperator operator, ObTenantEntity entity, String poolName) {

        List<ObResourcePool> poolList = operator.resource().listResourcePool(entity.getObTenantId());

        List<String> poolNameList = poolList.stream()
                .map(ObResourcePool::getName)
                .filter(name -> !StringUtils.equals(name, poolName))
                .collect(Collectors.toList());

        operator.tenant().modifyResourcePoolList(entity.getName(), poolNameList);
    }

    private String genRandomStr() {
        return RandomStringUtils.randomAlphabetic(3).toLowerCase();
    }

    private String genUnitConfigName(String tenantName, String zoneName, UnitSpecParam unitSpec) {
        String randomStr = genRandomStr();
        return genUnitConfigName(tenantName, zoneName, unitSpec, randomStr);
    }

    private String genUnitConfigName(String tenantName, String zoneName, UnitSpecParam unitSpec, String randomStr) {
        return String.join("_", UNIT_CONFIG_MARK, tenantName, zoneName, formatUnitSpec(unitSpec), randomStr);
    }

    private String formatUnitSpec(UnitSpecParam unitSpec) {
        return String.format("%dC%dG", (int) (double) unitSpec.getCpuCore(), unitSpec.getMemorySize());
    }

    private List<String> genResourcePoolName(String tenantName, List<String> zoneList) {
        return zoneList.stream().map(zone -> genResourcePoolName(tenantName, zone)).collect(Collectors.toList());
    }

    private String genResourcePoolName(String tenantName, String zoneName) {
        String randomStr = genRandomStr();
        return genResourcePoolName(tenantName, zoneName, randomStr);
    }

    private String genResourcePoolName(String tenantName, String zoneName, String randomStr) {
        return String.join("_", RESOURCE_POOL_MARK, tenantName, zoneName, randomStr);
    }

    private CreateUnitConfigInput buildCreateUnitConfigInput(String unitConfigName, UnitSpecParam unitSpec) {
        return CreateUnitConfigInput.builder()
                .name(unitConfigName)
                .maxCpu(unitSpec.getCpuCore())
                .minCpu(unitSpec.getCpuCore())
                .maxMemoryByte(unitSpec.getMemoryBytes())
                .minMemoryByte(unitSpec.getMemoryBytes())
                .build();
    }

    private CreateResourcePoolInput buildCreateResourcePoolInput(String poolName, String unitConfigName,
            CreateResourcePoolParam param) {
        return CreateResourcePoolInput.builder()
                .name(poolName)
                .zoneList(Collections.singletonList(param.getZoneName()))
                .unitConfigName(unitConfigName)
                .unitCount(param.getUnitCount())
                .build();
    }

    private ResourcePool mapToResourcePool(ObResourcePool obResourcePool) {
        return ResourcePool.builder()
                .id(obResourcePool.getResourcePoolId())
                .name(obResourcePool.getName())
                .unitCount(obResourcePool.getUnitCount())
                .zoneList(obResourcePool.getZoneList())
                .unitConfig(mapToUnitConfig(obResourcePool.getObUnitConfig()))
                .build();
    }

    private UnitConfig mapToUnitConfig(ObUnitConfig obUnitConfig) {
        return UnitConfig.builder()
                .maxCpuCoreCount(obUnitConfig.getMaxCpu())
                .minCpuCoreCount(obUnitConfig.getMinCpu())
                .maxMemoryByte(obUnitConfig.getMaxMemory())
                .minMemoryByte(obUnitConfig.getMinMemory())
                .maxDiskSizeByte(obUnitConfig.getMaxDiskSize())
                .maxIops(obUnitConfig.getMaxIops())
                .minIops(obUnitConfig.getMinIops())
                .maxSessionNum(obUnitConfig.getMaxSessionNum())
                .name(obUnitConfig.getName())
                .build();
    }

    private Unit mapToUnit(ObUnit unit) {
        return Unit.builder()
                .id(unit.getUnitId())
                .resourcePoolId(unit.getResourcePoolId())
                .serverIp(unit.getSvrIp())
                .serverPort(unit.getSvrPort())
                .zoneName(unit.getZone())
                .status(unit.getStatus())
                .build();
    }
}
