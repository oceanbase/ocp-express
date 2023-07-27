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

import java.util.List;

import com.oceanbase.ocp.obsdk.accessor.database.model.ObDatabase;
import com.oceanbase.ocp.obsdk.accessor.object.model.AlterTableInput;
import com.oceanbase.ocp.obsdk.accessor.object.model.ObTable;
import com.oceanbase.ocp.obsdk.accessor.object.model.ObTablePartition;

/**
 * Database object management in user tenants.
 */
public interface ObjectAccessor {

    /**
     * List databases.
     *
     * @return list of {@link ObDatabase}
     */
    List<ObDatabase> listDatabases();

    /**
     * get table
     */
    ObTable getTable(String tableName);

    /**
     * alter table
     */
    ObTable alterTable(AlterTableInput input);

    /**
     * List table partitions.
     *
     * Note: only range partitions supported currently.
     */
    List<ObTablePartition> listTablePartition(String tableName);
}
