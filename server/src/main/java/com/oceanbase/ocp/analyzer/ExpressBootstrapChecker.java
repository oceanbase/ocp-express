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
package com.oceanbase.ocp.analyzer;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.oceanbase.ocp.common.util.HostUtils;
import com.oceanbase.ocp.core.server.OcpInstanceUpdater;
import com.oceanbase.ocp.core.server.model.OcpInstance;

@Component
public class ExpressBootstrapChecker {

    @Value("${ocp.bootstrap.check:true}")
    private boolean bootstrapCheck;

    @Value("${server.port:8080}")
    private int serverPort;

    @Autowired
    private OcpInstanceUpdater ocpInstanceUpdater;

    @PostConstruct
    public void init() throws Exception {
        if (!bootstrapCheck) {
            return;
        }
        checkSingletonRunning();
    }

    private void checkSingletonRunning() {
        List<OcpInstance> allInstance = ocpInstanceUpdater.findAllInstance();
        Predicate<OcpInstance> sameInstance = ocpInstance -> {
            String ip = HostUtils.getLocalIp();
            int port = serverPort;
            return ocpInstance != null && ocpInstance.getIp().equalsIgnoreCase(ip) && ocpInstance.getPort() == port;
        };
        Predicate<OcpInstance> recordDeprecated = ocpInstance -> {
            long seconds = Duration.between(ocpInstance.getHeartBeatTime(), OffsetDateTime.now()).getSeconds();
            return seconds > 3 * OcpInstanceUpdater.HEART_BEAT_INTERVAL_SECS;
        };
        if (allInstance.isEmpty() || allInstance.stream()
                .allMatch(ocpInstance -> sameInstance.or(recordDeprecated).test(ocpInstance))) {
            ocpInstanceUpdater.update();
            ocpInstanceUpdater.setDaemon(true);
            return;
        }
        throw new RuntimeException(
                "Data source is occupied. Please check whether there is another OCP Express running.");
    }

}
