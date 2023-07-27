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
package com.oceanbase.ocp.core.property;

import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Data
@Component
@Slf4j
public class SystemInfo {

    @Autowired
    @JsonIgnore
    private PropertyManager propertyManager;

    @Autowired
    @JsonIgnore
    private DataSource dataSource;

    @Getter
    private String metaTenantName;

    @PostConstruct
    public void init() {
        try {
            String username = dataSource.getConnection().getMetaData().getUserName();
            log.info("DataSource username is {}.", username);
            // FORMATTER: username@tenantName[#obClusterName][:obClusterId]
            String[] split = username.split("@");
            if (split.length == 2) {
                this.metaTenantName = split[1].split("#")[0];
                return;
            }
        } catch (SQLException ignore) {
            log.info("Get datasource username failed.");
        }
        this.metaTenantName = StringUtils.EMPTY;
    }

    @JsonProperty("monitorInfo")
    public MonitorInfo getMonitorInfo() {
        return MonitorInfo.builder().collectInterval(propertyManager.getMetricCollectSecondInterval()).build();
    }

    @Data
    @Builder
    public static class MonitorInfo {

        /** Scrape interval of monitor. */
        int collectInterval;
    }

}
