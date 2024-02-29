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
package com.oceanbase.ocp.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.oceanbase.ocp.common.util.json.JsonUtils;
import com.oceanbase.ocp.core.credential.operator.AgentCredentialOperator;
import com.oceanbase.ocp.core.credential.operator.ObCredentialOperator;
import com.oceanbase.ocp.core.executor.AgentExecutorFactory;
import com.oceanbase.ocp.core.property.InitProperties;
import com.oceanbase.ocp.core.property.InitProperties.ClusterProperties.ObCredential;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.monitor.helper.ExporterRequestHelper;
import com.oceanbase.ocp.obops.cluster.ClusterInitService;
import com.oceanbase.ocp.obops.cluster.param.ClusterInitParam;
import com.oceanbase.ocp.obops.host.ObAgentService;
import com.oceanbase.ocp.obops.host.param.InitAgentParam;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OcpExpressInitializer {

    private static final String OCP_INITIAL_PROPERTIES_ENV = "OCP_EXPRESS_INIT_PROPERTIES";

    @Autowired
    private ClusterInitService clusterInitService;

    @Autowired
    private ObAgentService obAgentService;

    @Autowired
    private AgentCredentialOperator agentCredentialOperator;

    @Autowired
    private ObCredentialOperator obCredentialOperator;

    @Autowired
    private ExporterRequestHelper exporterRequestHelper;

    @Autowired
    private AgentExecutorFactory agentExecutorFactory;

    @Autowired(required = false)
    private InitProperties initProperties;

    @PostConstruct
    public void init() throws IOException {
        // First: Init by spring config file.
        boolean initialized = initFromConfigFile();
        if (initialized) {
            return;
        }
        // Second: Init by environment or VM properties.
        initFromEnvOrProperties();
    }

    private boolean initFromConfigFile() {
        if (initProperties == null || initProperties.getCluster() == null) {
            return false;
        }
        if (isPropertyLegal(initProperties)) {
            doInit(initProperties);
            return true;
        }
        throw new RuntimeException("Init by spring config files failed, please check config file format");
    }

    private void initFromEnvOrProperties() {
        String propertiesStr = System.getenv(OCP_INITIAL_PROPERTIES_ENV);
        if (StringUtils.isEmpty(propertiesStr)) {
            propertiesStr = System.getProperty(OCP_INITIAL_PROPERTIES_ENV);
        }
        if (StringUtils.isEmpty(propertiesStr)) {
            throw new RuntimeException("Env or properties not specified, please check env or properties.");
        }
        try {
            InitProperties p = JsonUtils.fromJson(propertiesStr, InitProperties.class);
            if (isPropertyLegal(p)) {
                doInit(p);
                return;
            }
        } catch (Exception e) {
            log.error("Load init properties from env or VM failed. Please check env or VM properties values.", e);
        }
        throw new RuntimeException("Init by env or properties failed, please check env or properties format");
    }

    private boolean isPropertyLegal(InitProperties initProperties) {
        Predicate<InitProperties.ClusterProperties> clusterInfoIllegal =
                c -> StringUtils.isAnyBlank(c.getName()) || (c.getObClusterId() == null || c.getObClusterId() < 0);

        Predicate<InitProperties.ClusterProperties> serverInfoIllegal = c -> c.getServerAddresses() == null
                || c.getServerAddresses().isEmpty()
                || c.getServerAddresses().stream()
                        .anyMatch(s -> s.getAddress() == null || s.getSvrPort() == null || s.getSqlPort() == null);

        Predicate<InitProperties.ClusterProperties> illegalPredicate = clusterInfoIllegal.or(serverInfoIllegal);
        return Optional.ofNullable(initProperties)
                .map(InitProperties::getCluster)
                .filter(c -> illegalPredicate.negate().test(c)).isPresent();
    }

    private void doInit(InitProperties initProperties) {
        validateProperties(initProperties.getCluster());
        initAgents(initProperties);
        initCluster(initProperties.getCluster());
    }

    private void validateProperties(InitProperties.ClusterProperties c) {
        ExceptionUtils.initIllegalArgs(StringUtils.isNotEmpty(c.getName()), "clusterName");
        ExceptionUtils.initIllegalArgs(c.getObClusterId() != null && c.getObClusterId() > 0, "obClusterId");

        Predicate<List<InitProperties.ClusterProperties.ServerAddressInfo>> aPredicate = lst -> lst != null
                && !lst.isEmpty()
                && lst.stream()
                        .noneMatch(s -> s.getAddress() == null || s.getSvrPort() == null || s.getSqlPort() == null);

        ExceptionUtils.initIllegalArgs(aPredicate.test(c.getServerAddresses()), "serverAddresses");
    }

    private void initCluster(InitProperties.ClusterProperties c) {
        ClusterInitParam param = ClusterInitParam.builder()
                .clusterName(c.getName())
                .obClusterId(c.getObClusterId())
                .rootSysPassword(c.getRootSysPassword())
                .serverList(c.getServerAddresses().stream()
                        .map(s -> ClusterInitParam.ObServerAddr.builder()
                                .address(s.getAddress())
                                .sqlPort(s.getSqlPort())
                                .withRootServer(s.isWithRootServer())
                                .build())
                        .collect(Collectors.toList()))
                .build();
        clusterInitService.initCluster(param);
        saveObCredentials(c.getName(), c.getObCredentials());
    }

    private void saveObCredentials(String clusterName, List<ObCredential> credentials) {
        if (CollectionUtils.isEmpty(credentials)) {
            return;
        }
        for (ObCredential c : credentials) {
            if (StringUtils.isNotEmpty(c.getTenantName())
                    && StringUtils.isNotEmpty(c.getUsername())) {
                log.info("Save ob credential, clusterName={}, tenantName={}, username={}",
                        clusterName, c.getTenantName(), c.getUsername());
                obCredentialOperator.saveObCredential(clusterName, c.getTenantName(), c.getUsername(), c.getPassword());
            }
        }
    }

    private void initAgents(InitProperties initProperties) {
        List<InitProperties.ClusterProperties.ServerAddressInfo> serverAddresses =
                initProperties.getCluster().getServerAddresses();
        List<InitAgentParam> initAgentParams = new ArrayList<>();
        for (InitProperties.ClusterProperties.ServerAddressInfo info : serverAddresses) {
            InitAgentParam param = InitAgentParam.builder()
                    .ip(info.getAddress())
                    .obServerSvrPort(info.getSvrPort())
                    .mgrPort(info.getAgentMgrPort())
                    .monPort(info.getAgentMonPort())
                    .build();
            initAgentParams.add(param);
        }
        // Save global agent password to vault
        agentCredentialOperator.saveAgentCredential(initProperties.getAgentUsername(),
                initProperties.getAgentPassword());
        exporterRequestHelper.setDigestAuth(initProperties.getAgentUsername(), initProperties.getAgentPassword());
        agentExecutorFactory.setAuthInfo(initProperties.getAgentUsername(), initProperties.getAgentPassword(),
                initProperties.getAgentAuthType());
        obAgentService.initAgent(initAgentParams);
    }

}
