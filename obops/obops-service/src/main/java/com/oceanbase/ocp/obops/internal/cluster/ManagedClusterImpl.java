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

package com.oceanbase.ocp.obops.internal.cluster;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.cluster.BasicCluster;
import com.oceanbase.ocp.core.ob.cluster.ManagedCluster;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObClusterStatus;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServer;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObZone;
import com.oceanbase.ocp.obsdk.operator.cluster.model.RootService;

import lombok.Getter;

@Service
public class ManagedClusterImpl implements ManagedCluster {

    // cluster info cache
    private BasicCluster basicCluster;

    @Getter
    private boolean initialize;

    @Override
    public String clusterName() {
        return getClusterInfo().getClusterName();
    }

    @Override
    public Long obClusterId() {
        return getClusterInfo().getObClusterId();
    }

    @Override
    public String obVersion() {
        return getClusterInfo().getObVersion();
    }

    @Override
    public List<ObZone> zones() {
        return getClusterInfo().getObZones();
    }

    @Override
    public List<ObServer> servers() {
        return getClusterInfo().getObServers();
    }

    public ObClusterStatus getStatus() {
        return getClusterInfo().getStatus();
    }

    public void initClusterInfo(BasicCluster basicCluster) {
        // Only save to cache
        this.basicCluster = basicCluster;
        this.initialize = true;
    }


    public void setObVersion(String obVersion) {
        basicCluster.setObVersion(obVersion);
    }

    public void setZones(List<ObZone> zones) {
        getClusterInfo().setObZones(zones);
    }

    public void setServerArchs(Map<String, String> serverArchs) {
        getClusterInfo().setServerArchs(serverArchs);
    }

    public void setServerDataPaths(Map<String, String> serverDataDirs) {
        getClusterInfo().setServerDataPaths(serverDataDirs);
    }

    public void setServerLogPaths(Map<String, String> serverLogDirs) {
        getClusterInfo().setServerLogPaths(serverLogDirs);
    }

    public void setServers(List<ObServer> servers) {
        getClusterInfo().setObServers(servers);
    }

    public void setRootServices(List<RootService> rootServices) {
        getClusterInfo().setRootServices(rootServices);
    }

    public void setStatus(ObClusterStatus status) {
        getClusterInfo().setStatus(status);
    }

    public void setCommunityEdition(Boolean communityEdition) {
        getClusterInfo().setCommunityEdition(communityEdition);
    }

    public BasicCluster getClusterInfo() {
        ExceptionUtils.require(basicCluster != null, ErrorCodes.OB_CLUSTER_IS_NOT_INIT);
        return basicCluster;
    }
}
