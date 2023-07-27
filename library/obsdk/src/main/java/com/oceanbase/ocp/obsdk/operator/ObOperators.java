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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.obsdk.connector.ConnectProperties;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.connector.ObConnector;
import com.oceanbase.ocp.obsdk.enums.CompatibilityMode;
import com.oceanbase.ocp.obsdk.operator.cluster.MysqlClusterOperator;
import com.oceanbase.ocp.obsdk.operator.compaction.MysqlCompactionOperator;
import com.oceanbase.ocp.obsdk.operator.object.MysqlObjectOperator;
import com.oceanbase.ocp.obsdk.operator.parameter.MysqlParameterOperator;
import com.oceanbase.ocp.obsdk.operator.resource.MysqlResourceOperator;
import com.oceanbase.ocp.obsdk.operator.session.MysqlSessionOperator;
import com.oceanbase.ocp.obsdk.operator.sql.MysqlSqlTuningOperator;
import com.oceanbase.ocp.obsdk.operator.sql.execute.SqlAuditOperatorImpl;
import com.oceanbase.ocp.obsdk.operator.sql.execute.SqlExecuteOperatorImpl;
import com.oceanbase.ocp.obsdk.operator.sql.execute.SqlPlanExplainOperatorImpl;
import com.oceanbase.ocp.obsdk.operator.sql.execute.SqlPlanOperatorImpl;
import com.oceanbase.ocp.obsdk.operator.stats.MysqlStatsOperator;
import com.oceanbase.ocp.obsdk.operator.tenant.MysqlTenantOperator;

/**
 * Factory and utility class for ob operator Ob operator is designed for the
 * operation in tenant 'sys'
 *
 */
public class ObOperators {

    // Suppresses default constructor, ensuring non-instantiability.
    private ObOperators() {}

    /**
     * Creates a new {@link ObOperator} instance.
     *
     * @param connectProperties the configuration for creating {@link ObConnector}
     * @return {@link ObOperator}
     * @throws NullPointerException if the input {@code connectProperties} is
     *         {@code null}
     * @throws IllegalArgumentException if the input {@code connectProperties} is
     *         not valid
     */
    public static ObOperator newObOperator(ConnectProperties connectProperties) {
        validate(connectProperties);

        ObConnectTemplate obConnectTemplate = new ObConnectTemplate(connectProperties);
        return ObOperator.builder()
                .resourceOperator(newResourceOperator(obConnectTemplate))
                .objectOperator(newObjectOperator(obConnectTemplate))
                .clusterOperator(newClusterOperator(obConnectTemplate))
                .parameterOperator(newParameterOperator(obConnectTemplate))
                .sessionOperator(newSessionOperator(obConnectTemplate))
                .statsOperator(newStatsOperator(obConnectTemplate))
                .tenantOperator(newTenantOperator(obConnectTemplate))
                .sqlTuningOperator(newSqlTuningOperator(obConnectTemplate))
                .sqlExecuteOperator(newSqlExecuteOperator(obConnectTemplate))
                .sqlAuditOperator(newSqlAuditOperator(obConnectTemplate))
                .compactionOperator(newCompactionOperator(obConnectTemplate))
                .sqlPlanOperator(newSqlPlanOperator(obConnectTemplate))
                .sqlPlanExplainOperator(newSqlPlanExplainOperator(obConnectTemplate))
                .build();
    }

    public static ObOperator newMetaOperator(ConnectProperties connectProperties) {
        ObConnectTemplate obConnectTemplate = new ObConnectTemplate(connectProperties);
        return ObOperator.builder()
                .clusterOperator(newClusterOperator(obConnectTemplate))
                .parameterOperator(newParameterOperator(obConnectTemplate))
                .build();
    }

    /**
     * Creates a new {@link ResourceOperator} instance.
     *
     * @param connectProperties the configuration for creating {@link ObConnector}
     * @return {@link ResourceOperator}
     * @throws NullPointerException if the input {@code connectProperties} is
     *         {@code null}
     * @throws IllegalArgumentException if the input {@code connectProperties} is
     *         not valid
     */
    public static ResourceOperator newResourceOperator(ConnectProperties connectProperties) {
        return newObOperator(connectProperties).resource();
    }

    /**
     * Creates a new {@link ClusterOperator} instance.
     *
     * @param connectProperties the configuration for creating {@link ObConnector}
     * @return {@link ClusterOperator}
     * @throws NullPointerException if the input {@code connectProperties} is
     *         {@code null}
     * @throws IllegalArgumentException if the input {@code connectProperties} is
     *         not valid
     */
    public static ClusterOperator newClusterOperator(ConnectProperties connectProperties) {
        return newObOperator(connectProperties).cluster();
    }

    /**
     * Creates a new {@link TenantOperator} instance.
     *
     * @param connectProperties the configuration for creating {@link ObConnector}
     * @return {@link TenantOperator}
     * @throws NullPointerException if the input {@code connectProperties} is
     *         {@code null}
     * @throws IllegalArgumentException if the input {@code connectProperties} is
     *         not valid
     */
    public static TenantOperator newTenantOperator(ConnectProperties connectProperties) {
        return newObOperator(connectProperties).tenant();
    }

    /**
     * Creates a new {@link SessionOperator} instance.
     *
     * @param connectProperties the configuration for creating {@link ObConnector}
     * @return {@link SessionOperator}
     * @throws NullPointerException if the input {@code connectProperties} is
     *         {@code null}
     * @throws IllegalArgumentException if the input {@code connectProperties} is
     *         not valid
     */
    public static SessionOperator newSessionOperator(ConnectProperties connectProperties) {
        return newObOperator(connectProperties).session();
    }

    /**
     * Creates a new {@link StatsOperator} instance.
     *
     * @param connectProperties the configuration for creating {@link ObConnector}
     * @return {@link StatsOperator}
     * @throws NullPointerException if the input {@code connectProperties} is
     *         {@code null}
     * @throws IllegalArgumentException if the input {@code connectProperties} is
     *         not valid
     */
    public static StatsOperator newStatsOperator(ConnectProperties connectProperties) {
        return newObOperator(connectProperties).stats();
    }


    private static ResourceOperator newResourceOperator(ObConnectTemplate obConnectTemplate) {
        ConnectProperties connectProperties = obConnectTemplate.getConnectProperties();
        ResourceOperator resourceOperator;
        if (CompatibilityMode.MYSQL == connectProperties.getCompatibilityMode()) {
            resourceOperator = new MysqlResourceOperator(obConnectTemplate);
        } else {
            throw new IllegalArgumentException(
                    "Create ResourceOperator failed due to wrong compatibility mode! compatibilityMode:"
                            + connectProperties.getCompatibilityMode());
        }
        return resourceOperator;
    }

    private static ObjectOperator newObjectOperator(ObConnectTemplate obConnectTemplate) {
        ConnectProperties connectProperties = obConnectTemplate.getConnectProperties();
        ObjectOperator objectOperator;
        if (CompatibilityMode.MYSQL == connectProperties.getCompatibilityMode()) {
            objectOperator = new MysqlObjectOperator(obConnectTemplate);
        } else {
            throw new IllegalArgumentException(
                    "Create ObjectOperator failed due to wrong compatibility mode! compatibilityMode:"
                            + connectProperties.getCompatibilityMode());
        }
        return objectOperator;
    }

    private static ClusterOperator newClusterOperator(ObConnectTemplate obConnectTemplate) {
        ConnectProperties connectProperties = obConnectTemplate.getConnectProperties();
        ClusterOperator clusterOperator;
        if (CompatibilityMode.MYSQL == connectProperties.getCompatibilityMode()) {
            clusterOperator = new MysqlClusterOperator(obConnectTemplate);
        } else {
            throw new IllegalArgumentException(
                    "Create ClusterOperator failed due to wrong compatibility mode! compatibilityMode:"
                            + connectProperties.getCompatibilityMode());
        }
        return clusterOperator;
    }

    private static TenantOperator newTenantOperator(ObConnectTemplate obConnectTemplate) {
        ConnectProperties connectProperties = obConnectTemplate.getConnectProperties();
        TenantOperator tenantOperator;
        if (CompatibilityMode.MYSQL == connectProperties.getCompatibilityMode()) {
            tenantOperator = new MysqlTenantOperator(obConnectTemplate);
        } else {
            throw new IllegalArgumentException(
                    "Create TenantOperator failed due to wrong compatibility mode! compatibilityMode:"
                            + connectProperties.getCompatibilityMode());
        }
        return tenantOperator;
    }

    private static ParameterOperator newParameterOperator(ObConnectTemplate obConnectTemplate) {
        ConnectProperties connectProperties = obConnectTemplate.getConnectProperties();
        ParameterOperator parameterOperator;
        if (CompatibilityMode.MYSQL == connectProperties.getCompatibilityMode()) {
            parameterOperator = new MysqlParameterOperator(obConnectTemplate);
        } else {
            throw new IllegalArgumentException(
                    "Create ParameterOperator failed due to wrong compatibility mode! compatibilityMode:"
                            + connectProperties.getCompatibilityMode());
        }
        return parameterOperator;
    }

    private static SessionOperator newSessionOperator(ObConnectTemplate obConnectTemplate) {
        ConnectProperties connectProperties = obConnectTemplate.getConnectProperties();
        SessionOperator sessionOperator;
        if (CompatibilityMode.MYSQL == connectProperties.getCompatibilityMode()) {
            sessionOperator = new MysqlSessionOperator(obConnectTemplate);
        } else {
            throw new IllegalArgumentException(
                    "Create SessionOperator failed due to wrong compatibility mode! compatibilityMode:"
                            + connectProperties.getCompatibilityMode());
        }
        return sessionOperator;
    }

    private static StatsOperator newStatsOperator(ObConnectTemplate obConnectTemplate) {
        ConnectProperties connectProperties = obConnectTemplate.getConnectProperties();
        if (CompatibilityMode.MYSQL != connectProperties.getCompatibilityMode()) {
            throw new IllegalArgumentException(
                    "Create StatOperator failed due to wrong compatibility mode! compatibilityMode:"
                            + connectProperties.getCompatibilityMode());
        }
        return new MysqlStatsOperator(obConnectTemplate);
    }

    private static SqlTuningOperator newSqlTuningOperator(ObConnectTemplate obConnectTemplate) {
        return new MysqlSqlTuningOperator(obConnectTemplate);
    }

    private static SqlExecuteOperator newSqlExecuteOperator(ObConnectTemplate obConnectTemplate) {
        ConnectProperties connectProperties = obConnectTemplate.getConnectProperties();
        if (CompatibilityMode.MYSQL != connectProperties.getCompatibilityMode()) {
            throw new IllegalArgumentException(
                    "Create SqlExecuteOperator failed due to wrong compatibility mode! compatibilityMode:"
                            + connectProperties.getCompatibilityMode());
        }
        return new SqlExecuteOperatorImpl(obConnectTemplate);
    }

    private static CompactionOperator newCompactionOperator(ObConnectTemplate obConnectTemplate) {
        ConnectProperties connectProperties = obConnectTemplate.getConnectProperties();
        CompactionOperator compactionOperator;
        if (CompatibilityMode.MYSQL == connectProperties.getCompatibilityMode()) {
            compactionOperator = new MysqlCompactionOperator(obConnectTemplate);
        } else {
            throw new IllegalArgumentException(
                    "Create ResourceOperator failed due to wrong compatibility mode! compatibilityMode:"
                            + connectProperties.getCompatibilityMode());
        }
        return compactionOperator;
    }

    private static SqlAuditOperator newSqlAuditOperator(ObConnectTemplate obConnectTemplate) {
        ConnectProperties connectProperties = obConnectTemplate.getConnectProperties();
        if (CompatibilityMode.MYSQL != connectProperties.getCompatibilityMode()) {
            throw new IllegalArgumentException(
                    "Create SqlAuditOperator failed due to wrong compatibility mode! compatibilityMode:"
                            + connectProperties.getCompatibilityMode());
        }
        return new SqlAuditOperatorImpl(obConnectTemplate);
    }

    private static void validate(ConnectProperties connectProperties) {
        Validate.notNull(connectProperties, "The input connectProperties is null.");
        connectProperties.validate();

        Validate.isTrue(StringUtils.equalsIgnoreCase("sys", connectProperties.getTenantName()),
                "Tenant 'sys' is required.");
    }

    private static SqlPlanOperator newSqlPlanOperator(ObConnectTemplate obConnectTemplate) {
        ConnectProperties connectProperties = obConnectTemplate.getConnectProperties();
        if (CompatibilityMode.MYSQL != connectProperties.getCompatibilityMode()) {
            throw new IllegalArgumentException(
                    "Create SqlAuditOperator failed due to wrong compatibility mode! compatibilityMode:"
                            + connectProperties.getCompatibilityMode());
        }
        return new SqlPlanOperatorImpl(obConnectTemplate);
    }

    private static SqlPlanExplainOperator newSqlPlanExplainOperator(ObConnectTemplate obConnectTemplate) {
        ConnectProperties connectProperties = obConnectTemplate.getConnectProperties();
        if (CompatibilityMode.MYSQL != connectProperties.getCompatibilityMode()) {
            throw new IllegalArgumentException(
                    "Create SqlAuditOperator failed due to wrong compatibility mode! compatibilityMode:"
                            + connectProperties.getCompatibilityMode());
        }
        return new SqlPlanExplainOperatorImpl(obConnectTemplate);
    }

}
