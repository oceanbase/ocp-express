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

package com.oceanbase.ocp.obsdk.operator.sql.execute;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.operator.SqlPlanExplainOperator;
import com.oceanbase.ocp.obsdk.operator.sql.entity.PlanExplainEntity;
import com.oceanbase.ocp.obsdk.operator.sql.param.QueryExplainByPlan;

public class SqlPlanExplainOperatorImpl implements SqlPlanExplainOperator {

    private static String SELECT_SQL_PLAN_EXPLAIN_AFTER_V4 =
            " select S.tenant_id as obTenantId, S.plan_id as planId, S.operator, S.name, "
                    + " S.rows, S.cost, S.property, T.id as obServerId from "
                    + " (select tenant_id, svr_ip, svr_port, plan_id, operator, name, rows, cost, property "
                    + " from GV$OB_PLAN_CACHE_PLAN_EXPLAIN  where tenant_id = ? and plan_id = ?) S"
                    + " join DBA_OB_SERVERS T "
                    + " on S.svr_ip = T.svr_ip and S.svr_port = T.svr_port ";

    private final ObConnectTemplate connectTemplate;

    public SqlPlanExplainOperatorImpl(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public List<PlanExplainEntity> queryByPlan(QueryExplainByPlan query) {
        String sql = SELECT_SQL_PLAN_EXPLAIN_AFTER_V4;
        Object[] array = new Object[] {query.getObTenantId(), query.getPlanId()};
        return connectTemplate.query(sql, array, new BeanPropertyRowMapper<>(PlanExplainEntity.class));
    }
}
