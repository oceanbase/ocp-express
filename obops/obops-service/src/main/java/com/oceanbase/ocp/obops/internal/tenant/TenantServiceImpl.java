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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.core.credential.operator.ObCredentialOperator;
import com.oceanbase.ocp.core.ob.cluster.ManagedCluster;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.QueryTenantParam;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.obops.cluster.ClusterCharsetService;
import com.oceanbase.ocp.obops.internal.tenant.util.LocalityUtils;
import com.oceanbase.ocp.obops.tenant.ResourcePoolService;
import com.oceanbase.ocp.obops.tenant.TenantService;
import com.oceanbase.ocp.obops.tenant.TenantWhitelistService;
import com.oceanbase.ocp.obops.tenant.event.DeleteTenantEvent;
import com.oceanbase.ocp.obops.tenant.model.ObproxyAndConnectionString;
import com.oceanbase.ocp.obops.tenant.model.ResourcePool;
import com.oceanbase.ocp.obops.tenant.model.Tenant;
import com.oceanbase.ocp.obops.tenant.model.TenantInfo;
import com.oceanbase.ocp.obops.tenant.model.TenantPreCheckResult;
import com.oceanbase.ocp.obops.tenant.model.TenantZone;
import com.oceanbase.ocp.obops.tenant.model.Unit;
import com.oceanbase.ocp.obsdk.operator.ObOperator;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObCollation;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServer;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServerKey;
import com.oceanbase.ocp.obsdk.operator.tenant.model.ObTenant;
import com.oceanbase.ocp.vault.model.Credential;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TenantServiceImpl implements TenantService {

    private static final String CONNECTION_STRING_TEMPLATE = "%s -h%s -P%d -u%s@%s -p";
    private static final String CONNECTION_URL_TEMPLATE = "jdbc:%s://%s:%d/%s";

    @Autowired
    private ManagedCluster managedCluster;

    @Autowired
    private TenantDaoManager tenantDaoManager;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private ObOperatorFactory obOperatorFactory;

    @Autowired
    private ResourcePoolService resourcePoolService;

    @Autowired
    private TenantWhitelistService tenantWhitelistService;

    @Autowired
    private ClusterCharsetService clusterCharsetService;

    @Autowired
    private ObCredentialOperator credentialOperator;

    @Override
    public Page<TenantInfo> listTenant(QueryTenantParam param, Pageable pageable) {
        return tenantDaoManager.queryTenant(param, pageable).map(this::buildTenantInfo);
    }

    @Override
    public TenantInfo getTenantInfo(Long obTenantId) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        return buildTenantInfo(entity);
    }

    private TenantInfo buildTenantInfo(ObTenantEntity entity) {
        TenantInfo tenantInfo = TenantInfo.fromTenant(mapToModel(entity));
        try {
            ObOperator operator = obOperatorFactory.createObOperator();
            // zones
            List<ResourcePool> poolList = resourcePoolService.listResourcePool(entity);
            List<Unit> unitList = resourcePoolService.listUnit(entity.getObTenantId());
            List<TenantZone> zones =
                    buildTenantZoneList(entity.getZoneList(), entity.getLocality(), poolList, unitList);
            tenantInfo.setZones(zones);

            // primary zone, charset
            ObTenant obTenant = operator.tenant().getTenant(entity.getObTenantId());
            attachPrimaryZone(tenantInfo, obTenant);
            attachCharset(tenantInfo, obTenant);

            // whitelist
            Optional<String> whitelist = tenantWhitelistService.getWhitelist(entity.getObTenantId());
            attachWhitelist(tenantInfo, whitelist);

            // connection string
            String connectionString = buildDirectConnectionString(entity, zones, entity.getMode().getSuperUser());
            List<ObproxyAndConnectionString> connectionStringList =
                    Collections.singletonList(ObproxyAndConnectionString.direct(connectionString));
            tenantInfo.setObproxyAndConnectionStrings(connectionStringList);

        } catch (Exception ex) {
            log.warn("Can not get tenant extra info from ob. exception:", ex);
        }

        return tenantInfo;
    }

    private List<TenantZone> buildTenantZoneList(List<String> zoneList, String locality, List<ResourcePool> poolList,
            List<Unit> unitList) {
        Map<String, String> replicaMap = LocalityUtils.buildReplicaTypeMap(locality);
        return zoneList.stream()
                .map(zone -> {
                    String replicaType = replicaMap.get(zone);
                    ResourcePool pool = findResourcePool(poolList, zone);
                    List<Unit> units = filterUnit(unitList, zone);
                    return TenantZone.builder()
                            .name(zone)
                            .replicaType(replicaType)
                            .resourcePool(pool)
                            .units(units)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private ResourcePool findResourcePool(List<ResourcePool> poolList, String zoneName) {
        return poolList.stream()
                .filter(pool -> pool.getZoneList().contains(zoneName))
                .findAny()
                .orElse(null);
    }

    private List<Unit> filterUnit(List<Unit> unitList, String zoneName) {
        return unitList.stream()
                .filter(unit -> StringUtils.equals(unit.getZoneName(), zoneName))
                .collect(Collectors.toList());
    }

    private void attachPrimaryZone(TenantInfo tenantInfo, ObTenant obTenant) {
        String primaryZone = obTenant.getPrimaryZone();
        tenantInfo.setPrimaryZone(primaryZone);
    }

    private void attachCharset(TenantInfo tenantInfo, ObTenant obTenant) {
        Map<Long, ObCollation> collationMap = clusterCharsetService.getCollationMap();
        ObCollation collation = collationMap.get(obTenant.getCollationType());
        if (collation != null) {
            tenantInfo.setCharset(collation.getCharset());
            tenantInfo.setCollation(collation.getCollation());
        }
    }

    private void attachWhitelist(TenantInfo tenantInfo, Optional<String> whitelist) {
        whitelist.ifPresent(tenantInfo::setWhitelist);
    }

    @Override
    public void deleteTenantInfo(String tenantName) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(tenantName);
        deleteTenantInfo(entity);
    }

    @Override
    public void deleteTenantInfo(Long obTenantId) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        deleteTenantInfo(entity);
    }

    private void deleteTenantInfo(ObTenantEntity entity) {
        tenantDaoManager.deleteTenant(entity);
    }

    @Override
    public void deleteTenantRelatedInfo(String tenantName) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(tenantName);
        deleteTenantRelatedInfo(entity);
    }

    @Override
    public void deleteTenantRelatedInfo(Long obTenantId) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        deleteTenantRelatedInfo(entity);
    }

    @Override
    public List<ObproxyAndConnectionString> getConnectionStringTemplates(Long tenantId) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetTenant(tenantId);
        TenantInfo tenantInfo = getTenantInfo(tenantId);
        String connectionString = buildDirectConnectionString(entity, tenantInfo.getZones(), "%s");
        return Collections.singletonList(ObproxyAndConnectionString.direct(connectionString));
    }

    @Override
    public List<ObproxyAndConnectionString> getConnectionUrlTemplates(Long tenantId) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetTenant(tenantId);
        TenantInfo tenantInfo = getTenantInfo(tenantId);
        Optional<ObServer> serverEntity = getConnectionServer(tenantInfo.getZones());
        String connectionUrlTemplate =
                String.format(CONNECTION_URL_TEMPLATE, entity.getMode().toString().toLowerCase(),
                        serverEntity.map(ObServer::getSvrIp).orElse("xxx"),
                        serverEntity.map(ObServer::getInnerPort).orElse(0), "%s");
        return Collections.singletonList(ObproxyAndConnectionString.direct(connectionUrlTemplate));
    }

    @Override
    public void modifyDescription(Long tenantId, String description) {
        tenantDaoManager.updateDescription(tenantId, description);
    }

    @Override
    public TenantPreCheckResult tenantPreCheck(Long tenantId) {
        // check if superuser password is empty
        ObTenantEntity tenant = tenantDaoManager.nullSafeGetObTenant(tenantId);
        Credential credential = credentialOperator.findObCredential(managedCluster.clusterName(),
                tenant.getName(), tenant.getMode().getSuperUser()).orElse(null);

        return TenantPreCheckResult.builder()
                .emptySuperUserPassword(credential != null && "".equals(credential.getPassphrase()))
                .build();
    }

    private String buildDirectConnectionString(ObTenantEntity tenant, List<TenantZone> zones, String username) {
        Optional<ObServer> serverEntity = getConnectionServer(zones);
        return String.format(CONNECTION_STRING_TEMPLATE, "obclient",
                serverEntity.map(ObServer::getSvrIp).orElse("xxx"),
                serverEntity.map(ObServer::getInnerPort).orElse(0),
                username, tenant.getName());
    }

    private Optional<ObServer> getConnectionServer(List<TenantZone> zones) {
        if (CollectionUtils.isEmpty(zones)) {
            return Optional.empty();
        }
        List<ObServerKey> unitServers = zones.stream()
                .flatMap(zone -> zone.getUnits().stream())
                .map(unit -> new ObServerKey(unit.getServerIp(), unit.getServerPort()))
                .collect(Collectors.toList());
        return managedCluster.servers().stream()
                .filter(server -> unitServers.contains(server.getKey()))
                .findFirst();
    }

    private void deleteTenantRelatedInfo(ObTenantEntity entity) {
        DeleteTenantEvent event = new DeleteTenantEvent(entity);
        applicationEventPublisher.publishEvent(event);
    }

    private Tenant mapToModel(ObTenantEntity entity) {
        return Tenant.builder()
                .name(entity.getName())
                .obTenantId(entity.getObTenantId())
                .mode(entity.getMode())
                .clusterName(managedCluster.clusterName())
                .obClusterId(managedCluster.obClusterId())
                .obVersion(managedCluster.obVersion())
                .createTime(entity.getCreateTime())
                .primaryZone(entity.getPrimaryZone())
                .zoneList(entity.getZoneListStr())
                .locality(entity.getLocality())
                .status(entity.getStatus())
                .locked(entity.getLocked())
                .readonly(entity.getReadonly())
                .description(entity.getDescription())
                .build();
    }
}
