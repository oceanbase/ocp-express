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

package com.oceanbase.ocp.obops.internal.tenant;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.obops.tenant.TenantWhitelistService;
import com.oceanbase.ocp.obsdk.operator.TenantOperator;

@Service
public class TenantWhitelistServiceImpl implements TenantWhitelistService {

    @Autowired
    private TenantDaoManager tenantDaoManager;

    @Autowired
    private ObOperatorFactory obOperatorFactory;

    @Override
    public Optional<String> getWhitelist(Long tenantId) {
        TenantOperator tenantOperator = obOperatorFactory.createTenantOperator();
        return tenantOperator.getWhitelist(tenantId);
    }

    @Override
    public void modifyWhitelist(Long tenantId, String userWhitelist) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetTenant(tenantId);
        TenantOperator tenantOperator = obOperatorFactory.createTenantOperator();
        tenantOperator.modifyWhitelist(tenantEntity.getName(), userWhitelist);
    }
}
