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
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.core.exception.NotFoundException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.cluster.BasicCluster;
import com.oceanbase.ocp.core.ob.cluster.ManagedCluster;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obsdk.operator.ObjectOperator;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServer;
import com.oceanbase.ocp.obsdk.operator.resource.model.ObGvDatabase;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SqlStatContextServiceImpl implements SqlStatContextService {

    @Autowired
    private ManagedCluster managedCluster;

    @Autowired
    TenantDaoManager tenantDaoManager;

    @Autowired
    private ObOperatorFactory obOperatorFactory;

    @Override
    public SqlStatDataContext getContext(Long obTenantId) {
        BasicCluster cluster = managedCluster.getClusterInfo();
        List<ObTenantEntity> tenants = tenantDaoManager.queryAllTenant();
        ObTenantEntity tenant = tenants.stream()
                .filter(it -> it.getObTenantId().equals(obTenantId)).findFirst()
                .orElseThrow(() -> ExceptionUtils.newException(NotFoundException.class,
                        ErrorCodes.OB_TENANT_ID_NOT_FOUND, obTenantId));
        List<ObServer> servers = cluster.getObServers();
        return new SqlStatDataContext(cluster, tenant, servers);

    }

    @Override
    public Long getObDatabaseId(Long obTenantId, String dbName) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        ObjectOperator objectOperator = obOperatorFactory.createObOperator().object();
        Optional<ObGvDatabase> obDb = objectOperator.listTenantDatabase(tenantEntity.getObTenantId()).stream()
                .filter(db -> db.getDatabaseName().equalsIgnoreCase(dbName))
                .findFirst();
        return obDb.map(ObGvDatabase::getDatabaseId).orElse(null);
    }


    @Override
    public String getDatabaseName(Long obTenantId, Long obDbId) {
        ObjectOperator objectOperator = obOperatorFactory.createObOperator().object();
        Optional<ObGvDatabase> obDb = objectOperator.listTenantDatabase(obTenantId).stream()
                .filter(db -> obDbId.equals(db.getDatabaseId()))
                .findFirst();
        return obDb.map(ObGvDatabase::getDatabaseName).orElse(null);
    }

}
