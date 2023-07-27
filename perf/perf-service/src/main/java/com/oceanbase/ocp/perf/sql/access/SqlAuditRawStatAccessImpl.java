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

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.obsdk.operator.SqlAuditOperator;
import com.oceanbase.ocp.obsdk.operator.sql.entity.SqlAuditRawStatEntity;
import com.oceanbase.ocp.obsdk.operator.sql.entity.TenantSlowSqlCounter;
import com.oceanbase.ocp.obsdk.operator.sql.param.QuerySlowSqlRankParam;
import com.oceanbase.ocp.obsdk.operator.sql.param.QueryTopSqlRawStatParam;
import com.oceanbase.ocp.perf.sql.dao.SqlAuditRawStatAccess;

@Component
public class SqlAuditRawStatAccessImpl implements SqlAuditRawStatAccess {

    @Resource
    ObOperatorFactory obOperatorFactory;

    @Override
    public List<SqlAuditRawStatEntity> topSql(QueryTopSqlRawStatParam query) {
        SqlAuditOperator sqlAuditOperator = obOperatorFactory.createObOperator().sqlAudit();
        return sqlAuditOperator.queryTopSql(query);
    }

    @Override
    public List<TenantSlowSqlCounter> getTopSlowSql(QuerySlowSqlRankParam param) {
        SqlAuditOperator sqlAuditOperator = obOperatorFactory.createObOperator().sqlAudit();
        return sqlAuditOperator.getTopSlowSql(param);
    }

    @Override
    public List<SqlAuditRawStatEntity> slowSql(QueryTopSqlRawStatParam param) {
        SqlAuditOperator sqlAuditOperator = obOperatorFactory.createObOperator().sqlAudit();
        return sqlAuditOperator.querySlowSqlRawStat(param);
    }

}
