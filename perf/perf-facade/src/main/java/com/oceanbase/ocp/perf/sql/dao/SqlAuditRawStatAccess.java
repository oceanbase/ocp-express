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

package com.oceanbase.ocp.perf.sql.dao;

import java.util.List;

import com.oceanbase.ocp.obsdk.operator.sql.entity.SqlAuditRawStatEntity;
import com.oceanbase.ocp.obsdk.operator.sql.entity.TenantSlowSqlCounter;
import com.oceanbase.ocp.obsdk.operator.sql.param.QuerySlowSqlRankParam;
import com.oceanbase.ocp.obsdk.operator.sql.param.QueryTopSqlRawStatParam;

public interface SqlAuditRawStatAccess {

    List<SqlAuditRawStatEntity> topSql(QueryTopSqlRawStatParam param);

    List<SqlAuditRawStatEntity> slowSql(QueryTopSqlRawStatParam param);

    List<TenantSlowSqlCounter> getTopSlowSql(QuerySlowSqlRankParam param);
}
