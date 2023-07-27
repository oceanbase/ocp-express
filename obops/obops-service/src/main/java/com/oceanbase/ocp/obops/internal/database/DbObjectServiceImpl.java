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

package com.oceanbase.ocp.obops.internal.database;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.obsdk.ObAccessorFactory;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obops.database.DbObjectService;
import com.oceanbase.ocp.obsdk.accessor.ObAccessor;
import com.oceanbase.ocp.obsdk.accessor.user.model.DbObject;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DbObjectServiceImpl implements DbObjectService {

    @Resource
    private ObAccessorFactory obAccessorFactory;

    @Resource
    private TenantDaoManager tenantDaoManager;

    @Override
    public List<DbObject> listObjects(Long obTenantId) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        return obAccessor.user().listAllObjects();
    }

    private void checkOraclePrivilegeManagementSupported(ObTenantEntity tenantEntity) {
        ExceptionUtils.illegalArgs(tenantEntity.isOracleMode(), ErrorCodes.OB_USER_MYSQL_MODE_NOT_SUPPORTED);
    }
}
