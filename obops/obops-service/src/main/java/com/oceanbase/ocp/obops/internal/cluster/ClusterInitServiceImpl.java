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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.core.agent.ObAgentDaoManager;
import com.oceanbase.ocp.core.agent.ObAgentEntity;
import com.oceanbase.ocp.core.credential.operator.ObCredentialOperator;
import com.oceanbase.ocp.core.executor.AgentExecutorFactory;
import com.oceanbase.ocp.core.ob.cluster.BasicCluster;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.executor.executor.AgentExecutor;
import com.oceanbase.ocp.executor.model.file.GetRealPathRequest;
import com.oceanbase.ocp.obops.cluster.ClusterInitService;
import com.oceanbase.ocp.obops.cluster.param.ClusterInitParam;
import com.oceanbase.ocp.obops.tenant.TenantSyncService;
import com.oceanbase.ocp.obsdk.enums.PartitionRole;
import com.oceanbase.ocp.obsdk.operator.ObOperator;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObClusterStatus;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObParameter;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServer;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObZone;
import com.oceanbase.ocp.obsdk.operator.cluster.model.RootService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ClusterInitServiceImpl implements ClusterInitService {

    @Autowired
    private ManagedClusterImpl managedClusterImpl;

    @Autowired
    private TenantDaoManager tenantDaoManager;

    @Autowired
    private ObCredentialOperator obCredentialOperator;

    @Autowired
    private ObOperatorFactory obOperatorFactory;

    @Autowired
    private ObAgentDaoManager obAgentDaoManager;

    @Autowired
    private AgentExecutorFactory agentExecutorFactory;

    @Autowired
    private TenantSyncService tenantSyncService;

    @Scheduled(fixedDelay = 60 * 1000L, initialDelay = 60 * 1000L)
    public void syncClusterInfo() {

        if (!managedClusterImpl.isInitialize()) {
            log.warn("Cluster is still not init, just skip!");
            return;
        }

        ObOperator obOperator;
        try {
            obOperator = obOperatorFactory.createObOperator();
        } catch (Exception ex) {
            log.info("Failed to init ob operator, set cluster status to UNAVAILABLE", ex);
            managedClusterImpl.setStatus(ObClusterStatus.UNAVAILABLE);
            return;
        }

        if (ObClusterStatus.UNAVAILABLE.equals(managedClusterImpl.getStatus())) {
            managedClusterImpl.setStatus(ObClusterStatus.RUNNING);
        }

        try {
            syncClusterInfo(obOperator);
            syncTenantInfo();
        } catch (Exception ex) {
            log.warn("Failed to sync cluster info", ex);
        }
    }

    @Override
    public BasicCluster initCluster(ClusterInitParam param) {
        log.info("init cluster start, param={}", param);

        // 1. Save cluster basic info.
        managedClusterImpl.initClusterInfo(BasicCluster.builder()
                .clusterName(param.getClusterName())
                .obClusterId(param.getObClusterId())
                .build());

        List<ObServer> servers = param.getServerList().stream()
                .map(addr -> ObServer.builder()
                        .svrIp(addr.getAddress())
                        .innerPort(addr.getSqlPort())
                        .withRootserver(addr.isWithRootServer())
                        .build())
                .collect(Collectors.toList());

        managedClusterImpl.setRootServices(buildRootServiceList(servers));
        managedClusterImpl.setServers(servers);
        obCredentialOperator.saveObCredential(param.getClusterName(), "sys", "root", param.getRootSysPassword());
        // 2. Sync cluster info.
        ObOperator operator = obOperatorFactory.createObOperator();
        syncClusterInfo(operator);
        // 3. Sync tenant info.
        syncTenantInfo();
        // 4. Attach server architectures
        attachServerArchs();
        // 5. Attach server data and log dirs
        attachServerDataLogPath();

        BasicCluster basicCluster = managedClusterImpl.getClusterInfo();
        log.info("init cluster done, clusterInfo={}", basicCluster);

        return basicCluster;
    }

    private List<RootService> buildRootServiceList(List<ObServer> servers) {
        return servers.stream().filter(ObServer::getWithRootserver).map(server -> {
            RootService rootService = new RootService();
            rootService.setSvrIp(server.getSvrIp());
            rootService.setSvrPort(server.getSvrPort());
            rootService.setZone(server.getZone());
            rootService.setRole(PartitionRole.UNKNOWN);
            return rootService;
        }).collect(Collectors.toList());
    }

    private void attachServerArchs() {
        List<ObServer> obServers = managedClusterImpl.getClusterInfo().getObServers();
        Map<String, String> server2Arch = new HashMap<>();
        obServers.forEach(server -> {
            try {
                ObAgentEntity agent =
                        obAgentDaoManager.nullSafeGetByIpAndObSvrPort(server.getSvrIp(), server.getSvrPort());
                AgentExecutor agentExecutor = agentExecutorFactory.create(agent.getIp(), agent.getMgrPort());
                server2Arch.put(buildServerId(server.getSvrIp(), server.getSvrPort()),
                        agentExecutor.getRemoteHostInfo().getArchitecture());
            } catch (Exception ex) {
                log.warn("Failed to get server arch info from:{}:{}", server.getSvrIp(), server.getSvrPort(), ex);
            }
        });
        managedClusterImpl.setServerArchs(server2Arch);
    }

    private void attachServerDataLogPath() {
        List<ObParameter> dataDirs;
        try {
            ObOperator operator = obOperatorFactory.createObOperator();
            dataDirs = operator.parameter().getClusterParameter("data_dir");
        } catch (Exception ex) {
            log.warn("Failed to obtain ob data dir", ex);
            return;
        }

        List<ObServer> obServers = managedClusterImpl.getClusterInfo().getObServers();
        Map<String, String> server2Path = dataDirs.stream().collect(Collectors
                .toMap(server -> buildServerId(server.getSvrIp(), server.getSvrPort()), ObParameter::getValue));

        Map<String, String> server2DataPath = new HashMap<>();
        Map<String, String> server2LogPath = new HashMap<>();
        obServers.forEach(server -> {
            try {
                ObAgentEntity agent =
                        obAgentDaoManager.nullSafeGetByIpAndObSvrPort(server.getSvrIp(), server.getSvrPort());
                AgentExecutor agentExecutor = agentExecutorFactory.create(agent.getIp(), agent.getMgrPort());
                String serverId = buildServerId(server.getSvrIp(), server.getSvrPort());
                String obPath = server2Path.get(serverId);
                String dataPath = agentExecutor
                        .getRealPath(GetRealPathRequest.builder().symbolicLink(obPath + "/sstable").build()).getPath();
                String logPath = agentExecutor
                        .getRealPath(GetRealPathRequest.builder().symbolicLink(obPath + "/clog").build()).getPath();
                Function<String, String> pathSplitter = str -> str.substring(0, str.lastIndexOf("/"));
                server2DataPath.put(serverId, pathSplitter.apply(dataPath));
                server2LogPath.put(serverId, pathSplitter.apply(logPath));
            } catch (Exception ex) {
                log.warn("Failed to get server arch info from:{}:{}", server.getSvrIp(), server.getSvrPort(), ex);
            }
        });
        managedClusterImpl.setServerDataPaths(server2DataPath);
        managedClusterImpl.setServerLogPaths(server2LogPath);
    }

    private void syncClusterInfo(ObOperator operator) {
        String obVersion = operator.cluster().getObVersion();
        managedClusterImpl.setObVersion(obVersion);

        List<ObZone> obZones = operator.cluster().listZones();

        managedClusterImpl.setZones(obZones);

        List<ObServer> obServers = operator.cluster().listServers();

        managedClusterImpl.setServers(obServers);

        List<RootService> rootServices = operator.cluster().listRootService();
        managedClusterImpl.setRootServices(rootServices);

        boolean ce = operator.cluster().isCommunityEdition();
        managedClusterImpl.setCommunityEdition(ce);

        log.info("sync cluster info done");
    }

    private void syncTenantInfo() {
        tenantSyncService.syncAllTenant();
    }

    private String buildServerId(String ip, Integer port) {
        return ip + ":" + port;
    }
}
