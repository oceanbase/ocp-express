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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.core.credential.operator.ObCredentialOperator;
import com.oceanbase.ocp.core.exception.ConnectFailedException;
import com.oceanbase.ocp.core.ob.cluster.ManagedCluster;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.core.ob.tenant.TenantStatus;
import com.oceanbase.ocp.core.obsdk.ObAccessorFactory;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.obops.internal.tenant.util.LocalityUtils;
import com.oceanbase.ocp.obops.tenant.TenantSyncService;
import com.oceanbase.ocp.obops.tenant.model.ObTenantStatus;
import com.oceanbase.ocp.obsdk.enums.RootServiceJobType;
import com.oceanbase.ocp.obsdk.operator.ObOperator;
import com.oceanbase.ocp.obsdk.operator.tenant.model.ObTenant;
import com.oceanbase.ocp.obsdk.operator.tenant.model.TenantJobProgress;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TenantSyncServiceImpl implements TenantSyncService {

    @Autowired
    private TenantDaoManager tenantDaoManager;

    @Autowired
    private ObOperatorFactory obOperatorFactory;

    @Autowired
    private ObAccessorFactory obAccessorFactory;

    @Autowired
    private ManagedCluster managedCluster;

    @Autowired
    private ObCredentialOperator credentialOperator;

    @Override
    public void syncTenant(Long tenantId) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetTenant(tenantId);
        ObOperator operator = obOperatorFactory.createObOperator();
        List<ObTenant> obTenants = operator.tenant().listTenant();
        obTenants.stream()
                .filter(tenant -> Objects.equals(tenant.getTenantId(), tenantId))
                .findFirst()
                .ifPresent(obTenant -> updateTenant(entity, obTenant));
        log.info("Sync tenant info done, tenantId={}", tenantId);
    }

    @Override
    public void syncAllTenant() {
        List<ObTenantEntity> tenantList = tenantDaoManager.queryAllTenant();
        ObOperator operator = obOperatorFactory.createObOperator();
        List<ObTenant> obTenantList = operator.tenant().listTenant();
        if (CollectionUtils.isEmpty(obTenantList)) {
            log.warn("No tenant found for cluster");
            return;
        }

        List<Long> obTenantIds = obTenantList.stream()
                .map(ObTenant::getTenantId)
                .collect(Collectors.toList());
        List<ObTenantEntity> tenantsToDelete = tenantList.stream()
                .filter(entity -> entity.getStatus() != TenantStatus.CREATING)
                .filter(entity -> !obTenantIds.contains(entity.getObTenantId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(tenantsToDelete)) {
            log.info("Detect obsolete tenants, delete in ocp: {}", tenantsToDelete);
        }
        tenantsToDelete.forEach(tenantDaoManager::deleteTenant);

        for (ObTenant obTenant : obTenantList) {
            Optional<ObTenantEntity> optional;
            if (ObTenantStatus.CREATING == ObTenantStatus.fromValue(obTenant.getStatus())) {
                optional = tenantDaoManager.getTenant(obTenant.getTenantName());
            } else {
                optional = tenantDaoManager.getTenant(obTenant.getTenantId());
            }
            if (optional.isPresent()) {
                updateTenant(optional.get(), obTenant);
            } else {
                log.info("New tenant detected in ob, obTenant={}", obTenant);
                ObTenantEntity entity = buildEntity(obTenant);
                entity = tenantDaoManager.saveTenant(entity);
                tryAndSetEmptyPassword(entity);
                log.info("New tenant saved, entity={}", entity);
            }
        }
        log.info("Sync all tenant info done");
    }

    private void tryAndSetEmptyPassword(ObTenantEntity entity) {
        try {
            obAccessorFactory.createObAccessor(entity.getName(), entity.getMode(), "");
        } catch (ConnectFailedException ex) {
            log.warn("Failed to connect to tenant {} use empty password", entity.getName());
            return;
        }
        log.info("Success to connect to tenant {} use empty password, now save to vault", entity.getName());
        credentialOperator.saveObCredential(managedCluster.clusterName(), entity.getName(),
                entity.getMode().getSuperUser(), "");
    }

    private void updateTenant(ObTenantEntity entity, ObTenant obTenant) {
        updateTenantProperties(entity, obTenant);
        updateStatusByRootServiceJob(entity, RootServiceJobType.ALTER_TENANT_LOCALITY);
        updateStatusByRootServiceJob(entity, RootServiceJobType.SHRINK_RESOURCE_POOL_UNIT_NUM);
    }

    private void updateStatusByRootServiceJob(ObTenantEntity entity, RootServiceJobType type) {
        ObOperator operator = obOperatorFactory.createObOperator();
        TenantJobProgress job = operator.tenant().getJobProgress(entity.getObTenantId(), type);
        if (job == null) {
            return;
        }
        TenantStatus targetStatus = job.finished() ? TenantStatus.NORMAL : TenantStatus.MODIFYING;
        if (entity.getStatus() != targetStatus) {
            log.info("update tenant status by job, tenantId={}, job={}, oldStatus={}, newStatus={}",
                    entity.getObTenantId(), job, entity.getStatus(), targetStatus);
        }
        tenantDaoManager.updateStatus(entity.getObTenantId(), targetStatus);
    }

    private void updateTenantProperties(ObTenantEntity entity, ObTenant obTenant) {
        if (!StringUtils.equals(entity.getName(), obTenant.getTenantName())) {
            tenantDaoManager.updateName(entity.getObTenantId(), obTenant.getTenantName());
        }
        if (obTenant.isLocked() != null && !Objects.equals(entity.getLocked(), obTenant.isLocked())) {
            tenantDaoManager.updateLockStatus(entity.getObTenantId(), obTenant.isLocked());
        }
        if (obTenant.isReadonly() != null && !Objects.equals(entity.getReadonly(), obTenant.isReadonly())) {
            tenantDaoManager.updateReadonlyStatus(entity.getObTenantId(), obTenant.isReadonly());
        }
        if (!StringUtils.equals(entity.getPrimaryZone(), obTenant.getPrimaryZone())) {
            tenantDaoManager.updatePrimaryZone(entity.getObTenantId(), obTenant.getPrimaryZone());
        }
        List<String> zoneList = LocalityUtils.getZoneList(obTenant.getLocality());
        String zoneListStr = String.join(";", zoneList);
        if (!StringUtils.equals(entity.getZoneListStr(), zoneListStr)) {
            tenantDaoManager.updateZoneList(entity.getObTenantId(), zoneListStr);
        }
        if (!StringUtils.equals(entity.getLocality(), obTenant.getLocality())) {
            tenantDaoManager.updateLocality(entity.getObTenantId(), obTenant.getLocality());
        }
        if (entity.getStatus() == TenantStatus.UNAVAILABLE) {
            log.info("Update tenant status, tenantId={}, oldStatus={}, newStatus={}",
                    entity.getObTenantId(), entity.getStatus(), TenantStatus.NORMAL);
            tenantDaoManager.updateStatus(entity.getObTenantId(), TenantStatus.NORMAL);
        }
    }

    private ObTenantEntity buildEntity(ObTenant obTenant) {
        ObTenantEntity entity = ObTenantEntity.builder()
                .creator("system")
                .name(obTenant.getTenantName())
                .obTenantId(obTenant.getTenantId())
                .mode(TenantMode.fromValue(obTenant.getTenantMode()))
                .status(TenantStatus.NORMAL)
                .primaryZone(obTenant.getPrimaryZone())
                .locality(obTenant.getLocality())
                .locked(obTenant.getLocked())
                .readonly(obTenant.getReadOnly())
                .build();
        entity.setZoneList(LocalityUtils.getZoneList(obTenant.getLocality()));
        return entity;
    }
}
