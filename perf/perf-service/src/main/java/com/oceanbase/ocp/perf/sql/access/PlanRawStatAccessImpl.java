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
import com.oceanbase.ocp.obsdk.operator.SqlPlanOperator;
import com.oceanbase.ocp.obsdk.operator.sql.entity.PlanRawStatEntity;
import com.oceanbase.ocp.obsdk.operator.sql.param.QueryPlanRawStatBySql;
import com.oceanbase.ocp.perf.sql.dao.PlanRawStatAccess;

@Component
public class PlanRawStatAccessImpl implements PlanRawStatAccess {

    @Resource
    ObOperatorFactory obOperatorFactory;

    @Override
    public List<PlanRawStatEntity> queryListBySql(QueryPlanRawStatBySql query) {
        SqlPlanOperator sqlPlanOperator = obOperatorFactory.createObOperator().sqlPlan();
        return sqlPlanOperator.queryPlanRawStat(query);
    }
}
