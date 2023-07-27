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

package com.oceanbase.ocp.perf.sql.access;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.oceanbase.ocp.core.ob.cluster.ManagedCluster;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.obsdk.operator.SqlAuditOperator;
import com.oceanbase.ocp.obsdk.operator.sql.entity.SqlTextEntity;
import com.oceanbase.ocp.obsdk.operator.sql.param.QuerySqlTextAny;
import com.oceanbase.ocp.perf.sql.dao.SqlTextAccess;

@Component
public class SqlTextAccessImpl implements SqlTextAccess {

    @Resource
    ObOperatorFactory obOperatorFactory;

    @Autowired
    ManagedCluster managedCluster;

    @Override
    public SqlTextEntity querySqlTextAny(QuerySqlTextAny query) {
        SqlAuditOperator sqlAuditOperator = obOperatorFactory.createObOperator().sqlAudit();
        SqlTextEntity res = new SqlTextEntity();
        if (query.getObDbId() != null) {
            res = sqlAuditOperator.querySqlTextAny(query);
        } else {
            res = sqlAuditOperator.querySqlTextAnyWithoutDbId(query);
        }
        return res;
    }

}
