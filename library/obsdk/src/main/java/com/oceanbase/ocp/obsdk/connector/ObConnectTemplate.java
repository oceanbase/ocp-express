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

package com.oceanbase.ocp.obsdk.connector;

import org.apache.commons.lang3.StringUtils;

import com.oceanbase.ocp.obsdk.util.ObSdkUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObConnectTemplate extends ConnectTemplate {

    private static final String SYS_TENANT_SELECT_OB_VERSION =
            "select max(value) value from oceanbase.__all_virtual_sys_parameter_stat where name = 'min_observer_version'";
    private static final String USER_TENANT_SELECT_OB_VERSION = "SHOW VARIABLES LIKE 'version_comment'";

    private static final String OB_VERSION_FOR_OB_4 =
            "SELECT MIN(VALUE) FROM GV$OB_PARAMETERS WHERE NAME = 'min_observer_version'";


    private String obVersion;

    public ObConnectTemplate(ConnectProperties connectProperties) {
        super(connectProperties);
        this.obVersion = getObVersion();
    }

    public String getObVersion() {
        if (StringUtils.isBlank(this.obVersion)) {
            getCurrentObVersion();
        }
        return this.obVersion;
    }

    public String getCurrentObVersion() {
        try {
            if ("sys".equalsIgnoreCase(connectProperties.getTenantName())) {
                log.debug("[obsdk] try get version of target ob, sql:{}", SYS_TENANT_SELECT_OB_VERSION);
                this.obVersion = queryForObject(SYS_TENANT_SELECT_OB_VERSION, String.class);
            } else {
                log.debug("[obsdk] try get version of target ob, sql:{}", USER_TENANT_SELECT_OB_VERSION);
                String versionComment =
                        queryForObject(USER_TENANT_SELECT_OB_VERSION, (rs, rowNum) -> rs.getString("Value"));
                this.obVersion = ObSdkUtils.parseVersionComment(versionComment);
            }
            log.debug("[obsdk] the ob version is {}", this.obVersion);
        } catch (Exception ex) {
            log.warn("[obsdk] failed to query ob version use inner table, errMsg:{}, cause:{}", ex.getMessage(),
                    ex.getCause());
            log.debug("[obsdk] try get version of target ob, sql:{}", OB_VERSION_FOR_OB_4);
            this.obVersion = queryForObject(OB_VERSION_FOR_OB_4, String.class);
        }
        fmtObVersion();
        return this.obVersion;
    }

    public ConnectProperties getConnectProperties() {
        return connectProperties;
    }

    private void fmtObVersion() {
        if (StringUtils.isNotBlank(this.obVersion)) {
            this.obVersion = this.obVersion.split("-")[0];
        }
    }

    public HintQueryer weakRead() {
        return new HintQueryer(this).weakRead();
    }

    public HintQueryer timeout(long timeoutMillis) {
        return new HintQueryer(this).timeout(timeoutMillis);
    }
}
