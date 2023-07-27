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

package com.oceanbase.ocp.core.obsdk;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import com.oceanbase.ocp.core.credential.operator.ObCredentialOperator;
import com.oceanbase.ocp.core.exception.ConnectFailedException;
import com.oceanbase.ocp.core.exception.TenantInfoTarget;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.cluster.ManagedCluster;
import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.obsdk.ObSdkContext;
import com.oceanbase.ocp.obsdk.connector.ConnectProperties;
import com.oceanbase.ocp.obsdk.connector.ConnectionMode;
import com.oceanbase.ocp.obsdk.connector.ObServerAddr;
import com.oceanbase.ocp.obsdk.enums.CompatibilityMode;
import com.oceanbase.ocp.obsdk.exception.ConnectorInitFailedException;
import com.oceanbase.ocp.obsdk.operator.ObOperators;
import com.oceanbase.ocp.obsdk.operator.ResourceOperator;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServer;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServerKey;
import com.oceanbase.ocp.obsdk.operator.resource.model.ListUnitInput;
import com.oceanbase.ocp.obsdk.operator.resource.model.ObUnit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConnectPropertiesBuilder {

    @Autowired
    private ManagedCluster managedCluster;

    @Autowired
    private ObCredentialOperator obCredentialOperator;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectTarget {

        private static final String SYS_TENANT_NAME = "sys";
        private static final String ROOT_USER = "root";

        private String tenantName;

        private TenantMode tenantMode;

        private String dbUser;

        public static ConnectTarget sys(String dbUser) {
            return ConnectTarget.builder()
                    .tenantName(SYS_TENANT_NAME)
                    .tenantMode(TenantMode.MYSQL)
                    .dbUser(dbUser)
                    .build();
        }

        public static ConnectTarget rootSys() {
            return ConnectTarget.builder()
                    .tenantName(SYS_TENANT_NAME)
                    .tenantMode(TenantMode.MYSQL)
                    .dbUser(ROOT_USER)
                    .build();
        }

        public boolean isSysTenant() {
            return StringUtils.equals(tenantName, SYS_TENANT_NAME);
        }
    }

    @Data
    @AllArgsConstructor
    public static class ConnectAddress {

        private List<ObServerAddr> obsAddrList;
    }

    public ConnectProperties buildConnectProperties(ConnectTarget connectTarget) {
        String password = acquirePassword(connectTarget);
        return buildConnectProperties(connectTarget, password);
    }

    public ConnectProperties buildConnectProperties(ConnectTarget connectTarget, String password) {
        ConnectAddress connectAddress;
        if (connectTarget.isSysTenant()) {
            connectAddress = buildConnectAddressForSysTenant();
        } else {
            connectAddress = buildConnectAddressForUserTenant(connectTarget.getTenantName());
        }
        return buildConnectProperties(connectTarget, connectAddress, password);
    }

    /**
     * When connecting to sys tenant, prefer connecting to root servers of the
     * cluster.
     */
    private ConnectAddress buildConnectAddressForSysTenant() {
        List<ObServer> obServers = Lists.newArrayList(managedCluster.servers());
        obServers.sort((a, b) -> Boolean.compare(b.getWithRootserver(), a.getWithRootserver()));
        List<ObServerAddr> obServerAddrList = obServers.stream().map(serverInfo -> ObServerAddr.builder()
                .address(serverInfo.getSvrIp()).port(serverInfo.getInnerPort()).build()).collect(Collectors.toList());
        log.info("build connect address for sys tenant, obServerAddrList={}", obServerAddrList);
        return new ConnectAddress(obServerAddrList);
    }

    /**
     * When connecting to user tenants, fetch observer addresses from the unit
     * distribution of this tenant.
     */
    private ConnectAddress buildConnectAddressForUserTenant(String tenantName) {
        List<ObServerAddr> obServerAddrList = getTenantServerList(tenantName);
        log.info("build connect address for user tenant {}, obServerAddrList={}", tenantName, obServerAddrList);
        return new ConnectAddress(obServerAddrList);
    }

    private List<ObServerAddr> getTenantServerList(String tenantName) {
        ConnectProperties connectProperties = buildConnectProperties(ConnectTarget.rootSys());
        ResourceOperator resourceOperator;
        try {
            resourceOperator = ObOperators.newResourceOperator(connectProperties);
        } catch (ConnectorInitFailedException e) {
            log.error("create resource operator failed, error message:{}", e.getMessage());
            throw new ConnectFailedException(ErrorCodes.OB_TENANT_CONNECT_FAILED,
                    new TenantInfoTarget(connectProperties.getTenantName()), connectProperties.getTenantName());
        }
        List<ObUnit> unitList = resourceOperator.listUnit(ListUnitInput.builder().tenantName(tenantName).build());
        Set<ObServerKey> tenantServers = unitList.stream()
                .map(unit -> new ObServerKey(unit.getSvrIp(), unit.getSvrPort()))
                .collect(Collectors.toSet());

        return managedCluster.servers().stream()
                .filter(server -> tenantServers.contains(server.getKey()))
                .map(server -> ObServerAddr.builder().address(server.getSvrIp()).port(server.getInnerPort()).build())
                .collect(Collectors.toList());
    }

    private ConnectProperties buildConnectProperties(ConnectTarget connectTarget, ConnectAddress connectAddress,
            String password) {
        CompatibilityMode compatibilityMode = CompatibilityMode.valueOf(connectTarget.getTenantMode().toString());
        return ConnectProperties.builder()
                .connectionMode(ConnectionMode.DIRECT)
                .obsAddrList(connectAddress.getObsAddrList())
                .tenantName(connectTarget.getTenantName())
                .username(connectTarget.getDbUser())
                .password(password)
                .compatibilityMode(compatibilityMode)
                .build();
    }

    private String acquirePassword(ConnectTarget connectTarget) {
        String clusterName = managedCluster.clusterName();
        return acquirePassword(clusterName, connectTarget.getTenantName(), connectTarget.getDbUser());
    }

    private String acquirePassword(String clusterName, String tenantName, String dbUser) {
        String contextKey = contextKey(clusterName, tenantName, dbUser);
        String password = ObSdkContext.getCredential(contextKey);
        if (StringUtils.isNotEmpty(password)) {
            log.info("get credential from obsdk context, clusterName={}, tenantName={}, dbUser={}",
                    clusterName, tenantName, dbUser);
            return password;
        }
        password = obCredentialOperator.acquireObPassword(clusterName, tenantName, dbUser);
        if (password != null) {
            ObSdkContext.putCredential(contextKey, password);
        }
        return password;
    }

    public static String contextKey(String clusterName, String tenantName, String dbUser) {
        return String.format("%s_%s_%s", clusterName, tenantName, dbUser);
    }
}
