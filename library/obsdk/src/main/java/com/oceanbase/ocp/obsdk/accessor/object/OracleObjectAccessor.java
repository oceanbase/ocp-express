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

package com.oceanbase.ocp.obsdk.accessor.object;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.oceanbase.ocp.obsdk.accessor.ObjectAccessor;
import com.oceanbase.ocp.obsdk.accessor.database.model.ObDatabase;
import com.oceanbase.ocp.obsdk.accessor.object.model.AlterTableInput;
import com.oceanbase.ocp.obsdk.accessor.object.model.ObTable;
import com.oceanbase.ocp.obsdk.accessor.object.model.ObTablePartition;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;

public class OracleObjectAccessor implements ObjectAccessor {

    private static final String SELECT_ALL_DATABASE =
            "select object_name as database_name, object_id as databaseId, CREATED gmtCreate from DBA_OBJECTS where object_type = 'DATABASE'";

    private final ObConnectTemplate connectTemplate;

    public OracleObjectAccessor(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public List<ObDatabase> listDatabases() {
        return connectTemplate.query(SELECT_ALL_DATABASE, new BeanPropertyRowMapper<>(ObDatabase.class));
    }

    @Override
    public ObTable getTable(String tableName) {
        throw new NotImplementedException("not implemented");
    }

    @Override
    public ObTable alterTable(AlterTableInput input) {
        throw new NotImplementedException("not implemented");
    }

    @Override
    public List<ObTablePartition> listTablePartition(String tableName) {
        throw new NotImplementedException("not implemented");
    }
}
