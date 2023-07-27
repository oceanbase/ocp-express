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

package com.oceanbase.ocp.obsdk.operator;

import lombok.Builder;

@Builder
public class ObOperator {

    private ClusterOperator clusterOperator;

    private ParameterOperator parameterOperator;

    private ResourceOperator resourceOperator;

    private ObjectOperator objectOperator;

    private SessionOperator sessionOperator;

    private StatsOperator statsOperator;

    private TenantOperator tenantOperator;

    private SqlTuningOperator sqlTuningOperator;

    private SqlExecuteOperator sqlExecuteOperator;

    private CompactionOperator compactionOperator;

    private SqlPlanOperator sqlPlanOperator;

    private SqlPlanExplainOperator sqlPlanExplainOperator;

    private SqlAuditOperator sqlAuditOperator;


    public ClusterOperator cluster() {
        return clusterOperator;
    }

    public ParameterOperator parameter() {
        return parameterOperator;
    }

    public ResourceOperator resource() {
        return resourceOperator;
    }

    public ObjectOperator object() {
        return objectOperator;
    }

    public SessionOperator session() {
        return sessionOperator;
    }

    public StatsOperator stats() {
        return statsOperator;
    }

    public TenantOperator tenant() {
        return tenantOperator;
    }

    public SqlTuningOperator sqlTuning() {
        return sqlTuningOperator;
    }

    public SqlExecuteOperator sqlExecute() {
        return sqlExecuteOperator;
    }

    public CompactionOperator compaction() {
        return compactionOperator;
    }

    public SqlPlanOperator sqlPlan() {
        return sqlPlanOperator;
    }

    public SqlPlanExplainOperator sqlPlanExplain() {
        return sqlPlanExplainOperator;
    }

    public SqlAuditOperator sqlAudit() {
        return sqlAuditOperator;
    }

}
