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

package com.oceanbase.ocp.obsdk.accessor;

import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.obsdk.accessor.database.MysqlDatabaseAccessor;
import com.oceanbase.ocp.obsdk.accessor.database.OracleDatabaseAccessor;
import com.oceanbase.ocp.obsdk.accessor.info.MysqlInfoAccessor;
import com.oceanbase.ocp.obsdk.accessor.info.OracleInfoAccessor;
import com.oceanbase.ocp.obsdk.accessor.object.MysqlObjectAccessor;
import com.oceanbase.ocp.obsdk.accessor.object.OracleObjectAccessor;
import com.oceanbase.ocp.obsdk.accessor.parameter.ParameterAccessorImpl;
import com.oceanbase.ocp.obsdk.accessor.session.MysqlSessionAccessor;
import com.oceanbase.ocp.obsdk.accessor.session.OracleSessionAccessor;
import com.oceanbase.ocp.obsdk.accessor.sql.tuning.MysqlSqlTuningAccessor;
import com.oceanbase.ocp.obsdk.accessor.sql.tuning.OracleSqlTuningAccessor;
import com.oceanbase.ocp.obsdk.accessor.user.MysqlUserAccessor;
import com.oceanbase.ocp.obsdk.accessor.user.OracleUserAccessor;
import com.oceanbase.ocp.obsdk.accessor.variable.MysqlVariableAccessor;
import com.oceanbase.ocp.obsdk.accessor.variable.OracleVariableAccessor;
import com.oceanbase.ocp.obsdk.connector.ConnectProperties;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.enums.CompatibilityMode;

/**
 * Factory and utility class for {@link ObAccessor}.
 */
public final class ObAccessors {

    private static ObAccessorHolder holder = new ObAccessorHolder();

    // Suppresses default constructor, ensuring non-instantiability.
    private ObAccessors() {}

    public static ObAccessor newObAccessor(ConnectProperties connectProperties) {
        Validate.notNull(connectProperties, "The input connectProperties is null.");
        connectProperties.validate();
        ObAccessor obAccessor = holder.get(connectProperties);
        if (obAccessor != null) {
            return obAccessor;
        }

        ObConnectTemplate obConnectTemplate = new ObConnectTemplate(connectProperties);
        obAccessor = ObAccessor.builder()
                .infoAccessor(newInfoAccessor(obConnectTemplate))
                .variableAccessor(newVariableAccessor(obConnectTemplate))
                .parameterAccessor(newParameterAccessor(obConnectTemplate))
                .userAccessor(newUserAccessor(obConnectTemplate))
                .databaseAccessor(newDatabaseAccessor(obConnectTemplate))
                .objectAccessor(newObjectAccessor(obConnectTemplate))
                .sessionAccessor(newSessionAccessor(obConnectTemplate))
                .sqlTuningAccessor(newSqlTuningAccessor(obConnectTemplate))
                .build();

        holder.put(connectProperties, obAccessor);
        return obAccessor;
    }

    private static InfoAccessor newInfoAccessor(ObConnectTemplate obConnectTemplate) {
        CompatibilityMode compatibilityMode = obConnectTemplate.getConnectProperties().getCompatibilityMode();
        if (CompatibilityMode.MYSQL == compatibilityMode) {
            return new MysqlInfoAccessor(obConnectTemplate);
        } else if (CompatibilityMode.ORACLE == compatibilityMode) {
            return new OracleInfoAccessor(obConnectTemplate);
        } else {
            throw new IllegalArgumentException("Unexpected tenant mode: " + compatibilityMode);
        }
    }

    private static VariableAccessor newVariableAccessor(ObConnectTemplate obConnectTemplate) {
        CompatibilityMode compatibilityMode = obConnectTemplate.getConnectProperties().getCompatibilityMode();
        if (CompatibilityMode.MYSQL == compatibilityMode) {
            return new MysqlVariableAccessor(obConnectTemplate);
        } else if (CompatibilityMode.ORACLE == compatibilityMode) {
            return new OracleVariableAccessor(obConnectTemplate);
        } else {
            throw new IllegalArgumentException("Unexpected tenant mode: " + compatibilityMode);
        }
    }

    private static ParameterAccessor newParameterAccessor(ObConnectTemplate obConnectTemplate) {
        return new ParameterAccessorImpl(obConnectTemplate);
    }

    private static UserAccessor newUserAccessor(ObConnectTemplate obConnectTemplate) {
        CompatibilityMode compatibilityMode = obConnectTemplate.getConnectProperties().getCompatibilityMode();
        if (CompatibilityMode.MYSQL == compatibilityMode) {
            return new MysqlUserAccessor(obConnectTemplate);
        } else if (CompatibilityMode.ORACLE == compatibilityMode) {
            return new OracleUserAccessor(obConnectTemplate);
        } else {
            throw new IllegalArgumentException("Unexpected tenant mode: " + compatibilityMode);
        }
    }

    private static DatabaseAccessor newDatabaseAccessor(ObConnectTemplate obConnectTemplate) {
        CompatibilityMode compatibilityMode = obConnectTemplate.getConnectProperties().getCompatibilityMode();
        if (CompatibilityMode.MYSQL == compatibilityMode) {
            return new MysqlDatabaseAccessor(obConnectTemplate);
        } else if (CompatibilityMode.ORACLE == compatibilityMode) {
            return new OracleDatabaseAccessor(obConnectTemplate);
        } else {
            throw new IllegalArgumentException("Unexpected tenant mode: " + compatibilityMode);
        }
    }

    private static ObjectAccessor newObjectAccessor(ObConnectTemplate obConnectTemplate) {
        CompatibilityMode compatibilityMode = obConnectTemplate.getConnectProperties().getCompatibilityMode();
        if (CompatibilityMode.MYSQL == compatibilityMode) {
            return new MysqlObjectAccessor(obConnectTemplate);
        } else if (CompatibilityMode.ORACLE == compatibilityMode) {
            return new OracleObjectAccessor(obConnectTemplate);
        } else {
            throw new IllegalArgumentException("Unexpected tenant mode: " + compatibilityMode);
        }
    }

    private static SessionAccessor newSessionAccessor(ObConnectTemplate obConnectTemplate) {
        CompatibilityMode compatibilityMode = obConnectTemplate.getConnectProperties().getCompatibilityMode();
        if (CompatibilityMode.MYSQL == compatibilityMode) {
            return new MysqlSessionAccessor(obConnectTemplate);
        } else if (CompatibilityMode.ORACLE == compatibilityMode) {
            return new OracleSessionAccessor(obConnectTemplate);
        } else {
            throw new IllegalArgumentException("Unexpected tenant mode: " + compatibilityMode);
        }
    }

    private static SqlTuningAccessor newSqlTuningAccessor(ObConnectTemplate obConnectTemplate) {
        CompatibilityMode compatibilityMode = obConnectTemplate.getConnectProperties().getCompatibilityMode();
        if (CompatibilityMode.MYSQL == compatibilityMode) {
            return new MysqlSqlTuningAccessor(obConnectTemplate);
        } else if (CompatibilityMode.ORACLE == compatibilityMode) {
            return new OracleSqlTuningAccessor(obConnectTemplate);
        } else {
            throw new IllegalArgumentException("Unexpected tenant mode: " + compatibilityMode);
        }
    }
}
