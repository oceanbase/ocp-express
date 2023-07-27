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
package com.oceanbase.ocp.core.server;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.oceanbase.ocp.common.util.HostUtils;
import com.oceanbase.ocp.core.server.dao.OcpInstanceRepository;
import com.oceanbase.ocp.core.server.entity.OcpInstanceEntity;
import com.oceanbase.ocp.core.server.model.OcpInstance;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OcpInstanceUpdater {

    @Value("${server.port:8080}")
    private int serverPort;

    @Autowired
    private OcpInstanceRepository ocpInstanceRepository;

    private boolean daemon = false;

    public static final int HEART_BEAT_INTERVAL_SECS = 5;

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    @Scheduled(fixedDelay = HEART_BEAT_INTERVAL_SECS, timeUnit = TimeUnit.SECONDS)
    public void update() {
        if (!daemon) {
            return;
        }
        String ip = HostUtils.getLocalIp();
        int port = serverPort;
        OcpInstanceEntity instance = ocpInstanceRepository.findByIpAndPort(ip, port);
        if (instance != null) {
            instance.setHeartbeatTime(OffsetDateTime.now());
        } else {
            instance = OcpInstanceEntity.builder()
                    .ip(ip)
                    .port(port)
                    .heartbeatTime(OffsetDateTime.now())
                    .build();
        }
        ocpInstanceRepository.saveAndFlush(instance);
    }

    public List<OcpInstance> findAllInstance() {
        return ocpInstanceRepository.findAll().stream()
                .map(entity -> OcpInstance.builder()
                        .id(entity.getId())
                        .ip(entity.getIp())
                        .port(entity.getPort())
                        .heartBeatTime(entity.getHeartbeatTime())
                        .createTime(entity.getCreateTime())
                        .updateTime(entity.getUpdateTime())
                        .build())
                .collect(Collectors.toList());
    }

}
