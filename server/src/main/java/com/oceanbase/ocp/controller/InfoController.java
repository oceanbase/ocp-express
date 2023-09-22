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
package com.oceanbase.ocp.controller;

import com.oceanbase.ocp.common.util.HostUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class InfoController {

    @Autowired
    private BuildProperties buildProperties;

    @GetMapping("/")
    public String home() {
        return "index.html";
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> clientInfo = new HashMap<>(8);
        clientInfo.put("status", "ok");
        clientInfo.put("version", buildProperties.getVersion());
        clientInfo.put("server", HostUtils.getLocalIp());
        return clientInfo;
    }

    @GetMapping("/time")
    public OffsetDateTime time() {
        return OffsetDateTime.now();
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("name", buildProperties.getName());
        infoMap.put("group", buildProperties.getGroup());
        infoMap.put("artifact", buildProperties.getArtifact());
        infoMap.put("buildTime", OffsetDateTime.ofInstant(buildProperties.getTime(), ZoneId.systemDefault()));
        infoMap.put("buildVersion", buildProperties.getVersion());
        infoMap.put("buildJavaVersion", buildProperties.get("java.version"));
        infoMap.put("springBootVersion", buildProperties.get("spring-boot.version"));
        infoMap.put("server", HostUtils.getLocalIp());
        return infoMap;
    }

}
