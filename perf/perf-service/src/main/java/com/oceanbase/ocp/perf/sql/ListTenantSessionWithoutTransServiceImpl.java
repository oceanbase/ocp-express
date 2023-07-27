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
package com.oceanbase.ocp.perf.sql;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.oceanbase.ocp.common.util.sql.SqlParamCheckUtils;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.core.obsdk.ObAccessorFactory;
import com.oceanbase.ocp.core.property.PropertyManager;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obsdk.accessor.SessionAccessor;
import com.oceanbase.ocp.obsdk.operator.session.model.ObSession;
import com.oceanbase.ocp.obsdk.operator.session.model.SessionQueryCondition;
import com.oceanbase.ocp.perf.sql.model.TenantSession;
import com.oceanbase.ocp.perf.sql.param.QueryTenantSessionParam;
import com.oceanbase.ocp.perf.sql.util.TenantSessionQueryHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ListTenantSessionWithoutTransServiceImpl implements ListTenantSessionService {

    @Resource
    private ObAccessorFactory obAccessorFactory;
    @Resource
    private TenantDaoManager tenantDaoManager;
    @Resource
    private PropertyManager propertyManager;

    @Override
    public Page<TenantSession> listTenantSession(QueryTenantSessionParam param, Pageable pageable) {
        validateSqlParam(param);
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(param.getTenantId());
        SessionQueryCondition condition =
                TenantSessionQueryHelper.buildSessionQueryCondition(tenantEntity.getName(), param, pageable);
        long totalSize;
        List<ObSession> obSessionList;
        SessionAccessor sessionAccessor =
                obAccessorFactory.createObAccessor(tenantEntity.getName(), TenantMode.MYSQL).session();
        totalSize = sessionAccessor.countSession(condition);
        obSessionList = sessionAccessor.listSession(condition);
        List<TenantSession> tenantSessionList =
                obSessionList.stream().map(TenantSessionQueryHelper::mapToTenantSession).collect(Collectors.toList());
        return new PageImpl<>(tenantSessionList, pageable, totalSize);
    }

    private void validateSqlParam(QueryTenantSessionParam param) {
        Predicate<String> predicate = s -> SqlParamCheckUtils.check(s, propertyManager.getSqlParamPattern());
        ExceptionUtils.illegalArgs(predicate.test(param.getClientIp()), "clientIp");
        ExceptionUtils.illegalArgs(predicate.test(param.getDbName()), "dbName");
        ExceptionUtils.illegalArgs(predicate.test(param.getDbUser()), "dbUser");
    }

}
