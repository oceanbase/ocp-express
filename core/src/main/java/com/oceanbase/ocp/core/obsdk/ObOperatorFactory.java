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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.core.exception.ConnectFailedException;
import com.oceanbase.ocp.core.exception.TenantInfoTarget;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.obsdk.ConnectPropertiesBuilder.ConnectTarget;
import com.oceanbase.ocp.obsdk.ObSdkContext;
import com.oceanbase.ocp.obsdk.connector.ConnectProperties;
import com.oceanbase.ocp.obsdk.exception.ConnectorInitFailedException;
import com.oceanbase.ocp.obsdk.operator.CompactionOperator;
import com.oceanbase.ocp.obsdk.operator.ObOperator;
import com.oceanbase.ocp.obsdk.operator.ObOperators;
import com.oceanbase.ocp.obsdk.operator.ParameterOperator;
import com.oceanbase.ocp.obsdk.operator.ResourceOperator;
import com.oceanbase.ocp.obsdk.operator.TenantOperator;

import lombok.extern.slf4j.Slf4j;

/**
 * ObOperator factory.
 */
@Slf4j
@Service
public class ObOperatorFactory {

    private static final String DB_USER = "root";

    @Autowired
    private ConnectPropertiesBuilder connectPropertiesBuilder;

    /**
     * Create ObOperator instance.
     *
     * @return ObOperator
     */
    public ObOperator createObOperator() {
        ConnectProperties connectProperties = buildConnectProperties();
        return getObOperator(connectProperties);
    }

    public TenantOperator createTenantOperator() {
        return createObOperator().tenant();
    }

    public ParameterOperator createParameterOperator() {
        return createObOperator().parameter();
    }

    public ResourceOperator createResourceOperator() {
        return createObOperator().resource();
    }

    public CompactionOperator createCompactionOperator() {
        return createObOperator().compaction();
    }

    private ConnectProperties buildConnectProperties() {
        return connectPropertiesBuilder.buildConnectProperties(ConnectTarget.sys(DB_USER));
    }

    private ObOperator getObOperator(ConnectProperties connectProperties) {
        if (ObSdkContext.connectFailed(connectProperties)) {
            log.info("get connect state from obsdk context, state: false, connectProperties:{}", connectProperties);
            throw new ConnectFailedException(ErrorCodes.OB_TENANT_CONNECT_FAILED,
                    new TenantInfoTarget(connectProperties.getTenantName()), connectProperties.getTenantName());
        }
        try {
            return ObOperators.newObOperator(connectProperties);
        } catch (ConnectorInitFailedException e) {
            ObSdkContext.setConnectState(connectProperties, Boolean.FALSE);
            log.error("[ObOperatorFactory] create operator failed, error message:{}", e.getMessage());
            throw new ConnectFailedException(ErrorCodes.OB_TENANT_CONNECT_FAILED,
                    new TenantInfoTarget(connectProperties.getTenantName()), connectProperties.getTenantName());
        }
    }
}
