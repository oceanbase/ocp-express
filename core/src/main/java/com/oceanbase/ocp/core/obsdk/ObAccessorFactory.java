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
import org.springframework.stereotype.Component;

import com.oceanbase.ocp.core.exception.ConnectFailedException;
import com.oceanbase.ocp.core.exception.TenantInfoTarget;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.core.obsdk.ConnectPropertiesBuilder.ConnectTarget;
import com.oceanbase.ocp.obsdk.ObSdkContext;
import com.oceanbase.ocp.obsdk.accessor.ObAccessor;
import com.oceanbase.ocp.obsdk.accessor.ObAccessors;
import com.oceanbase.ocp.obsdk.connector.ConnectProperties;
import com.oceanbase.ocp.obsdk.exception.ConnectorInitFailedException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ObAccessorFactory {

    @Autowired
    private TenantDaoManager tenantDaoManager;

    @Autowired
    private ConnectPropertiesBuilder connectPropertiesBuilder;

    public ObAccessor createObAccessor(Long obTenantId) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        return createObAccessor(tenantEntity.getName(), tenantEntity.getMode());
    }

    public ObAccessor createObAccessor(String tenantName, TenantMode tenantMode) {
        ConnectTarget connectTarget = ConnectTarget.builder()
                .tenantName(tenantName)
                .tenantMode(tenantMode)
                .dbUser(tenantMode.getSuperUser())
                .build();
        ConnectProperties connectProperties = connectPropertiesBuilder.buildConnectProperties(connectTarget);
        return createObAccessor(connectProperties);
    }

    public ObAccessor createObAccessor(String tenantName, TenantMode tenantMode, String superUserPassword) {
        ConnectTarget connectTarget = ConnectTarget.builder()
                .tenantName(tenantName)
                .tenantMode(tenantMode)
                .dbUser(tenantMode.getSuperUser())
                .build();
        ConnectProperties connectProperties =
                connectPropertiesBuilder.buildConnectProperties(connectTarget, superUserPassword);
        return createObAccessor(connectProperties);
    }

    public ObAccessor createObAccessorWithDataBase(TenantMode tenantMode, String tenantName, String dbName) {
        ConnectTarget connectTarget = ConnectTarget.builder()
                .tenantName(tenantName)
                .tenantMode(tenantMode)
                .dbUser(tenantMode.getSuperUser())
                .build();
        ConnectProperties connectProperties =
                connectPropertiesBuilder.buildConnectProperties(connectTarget).withDatabase(dbName);
        return createObAccessor(connectProperties);
    }

    private ObAccessor createObAccessor(ConnectProperties connectProperties) {
        if (ObSdkContext.connectFailed(connectProperties)) {
            log.info("get connect state from obsdk context, state: false, connectProperties:{}", connectProperties);
            throw new ConnectFailedException(ErrorCodes.OB_TENANT_CONNECT_FAILED,
                    new TenantInfoTarget(connectProperties.getTenantName()), connectProperties.getTenantName());
        }
        try {
            return ObAccessors.newObAccessor(connectProperties);
        } catch (ConnectorInitFailedException e) {
            ObSdkContext.setConnectState(connectProperties, Boolean.FALSE);
            log.error("[ObAccessorFactory] create tenant accessor failed, error message:{}", e.getMessage());
            throw new ConnectFailedException(ErrorCodes.OB_TENANT_CONNECT_FAILED,
                    new TenantInfoTarget(connectProperties.getTenantName()), connectProperties.getTenantName());
        }
    }
}
