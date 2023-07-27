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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.oceanbase.ocp.core.ob.cluster.BasicCluster;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@Builder(builderClassName = "Builder")
@ToString
public final class SqlStatDataContext {

    public final Long obClusterId;
    public final String clusterName;
    public final Long tenantId;
    public final Long obTenantId;
    public final String tenantName;
    public final TenantMode mode;
    public final Long serverId;
    public final Long obServerId;
    public final String obVersion;
    public OffsetDateTime startTime;
    public OffsetDateTime endTime;
    public boolean parseSqlType;
    public boolean parseStatement;
    public boolean parseTable;
    public final Set<Long> obServerIds;
    public Map<Long, String> serverMap = null;

    public SqlStatDataContext(BasicCluster cluster, ObTenantEntity tenant, ObServer ocpServer,
            ObServer obServer) {
        this(cluster, tenant, ocpServer, obServer, null);
    }

    public SqlStatDataContext(BasicCluster cluster, ObTenantEntity tenant, List<ObServer> servers) {
        this(cluster, tenant, null, null, servers);
    }

    public SqlStatDataContext(BasicCluster cluster, List<ObServer> servers) {
        this(cluster, null, null, null, servers);
    }

    public SqlStatDataContext(BasicCluster cluster, ObTenantEntity tenant, ObServer ocpServer,
            ObServer obServer, List<ObServer> servers) {
        obClusterId = cluster.getObClusterId();
        clusterName = cluster.getClusterName();
        tenantId = tenant.getId();
        tenantName = tenant.getName();
        obTenantId = tenant.getObTenantId();
        mode = tenant.getMode();
        obVersion = cluster.getObVersion();
        obServerIds = new HashSet<>();
        serverMap = new HashMap<>(8);
        if (ocpServer != null && obServer != null) {
            this.serverId = ocpServer.getId();
            this.obServerId = obServer.getId();
            this.obServerIds.add(obServer.getId());
            serverMap.put(obServer.getId(), obServer.getAddress());
        } else {
            this.serverId = null;
            this.obServerId = null;
            if (servers != null) {
                servers.forEach(s -> {
                    serverMap.put(s.getId(), s.getAddress());
                });
                this.obServerIds.addAll(serverMap.keySet());
            }
        }
    }
}

