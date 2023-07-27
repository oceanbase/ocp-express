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

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.oceanbase.ocp.core.constants.OcpConstants;
import com.oceanbase.ocp.core.ob.cluster.BasicCluster;
import com.oceanbase.ocp.core.ob.cluster.ManagedCluster;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.obsdk.ObAccessorFactory;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.obsdk.accessor.SessionAccessor;
import com.oceanbase.ocp.obsdk.operator.SessionOperator;
import com.oceanbase.ocp.obsdk.operator.session.model.ObSession;
import com.oceanbase.ocp.obsdk.operator.session.model.SessionStats;
import com.oceanbase.ocp.obsdk.util.ObSdkUtils;
import com.oceanbase.ocp.perf.sql.model.CloseSessionParam;
import com.oceanbase.ocp.perf.sql.model.TenantSession;
import com.oceanbase.ocp.perf.sql.param.QueryTenantSessionParam;
import com.oceanbase.ocp.perf.sql.util.TenantSessionQueryHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TenantSessionServiceImpl implements TenantSessionService {

    @Resource
    private ObOperatorFactory obOperatorFactory;

    @Resource
    private ObAccessorFactory obAccessorFactory;

    @Resource
    private TenantDaoManager tenantDaoManager;

    @Resource
    private ManagedCluster managedCluster;

    @Override
    public SessionStats getSessionStats(Long obTenantId) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        SessionAccessor sessionAccessor =
                obAccessorFactory.createObAccessor(entity.getName(), entity.getMode()).session();
        return sessionAccessor.getSessionStats(entity.getName());
    }

    @Override
    public void killSession(Long tenantId, CloseSessionParam param) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(tenantId);
        SessionAccessor sessionAccessor =
                obAccessorFactory.createObAccessor(entity.getName(), entity.getMode()).session();
        param.getSessionIds().forEach(sessionAccessor::killSession);
    }


    @Override
    public void killQuery(Long tenantId, CloseSessionParam param) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetTenant(tenantId);
        SessionAccessor sessionAccessor =
                obAccessorFactory.createObAccessor(entity.getName(), entity.getMode()).session();
        param.getSessionIds().forEach(sessionAccessor::killQuery);
    }

    @Override
    public TenantSession getTenantSession(QueryTenantSessionParam param) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(param.getTenantId());
        BasicCluster basicCluster = managedCluster.getClusterInfo();
        ObSession obSession;
        if (ObSdkUtils.versionBefore(basicCluster.getObVersion(),
                OcpConstants.MIN_VERSION_QUERY_SESSION_INFO_FROM_NORMAL_TENANT_VIEW)) {
            SessionOperator operator = obOperatorFactory.createObOperator().session();
            obSession = operator.getSession(entity.getName(), param.getSessionId());
        } else {
            SessionAccessor sessionAccessor = obAccessorFactory.createObAccessor(param.getTenantId()).session();
            obSession = sessionAccessor.getSession(entity.getName(), param.getSessionId());
        }
        return obSession == null ? null : TenantSessionQueryHelper.mapToTenantSession(obSession);
    }
}
