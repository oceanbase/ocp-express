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

package com.oceanbase.ocp.obops.database;

import java.util.List;

import com.oceanbase.ocp.obops.database.model.Database;
import com.oceanbase.ocp.obops.database.param.CreateDatabaseParam;
import com.oceanbase.ocp.obops.database.param.ModifyDatabaseParam;

public interface DatabaseService {

    List<Database> listDatabases(Long obTenantId);

    Database createDatabase(Long obTenantId, CreateDatabaseParam param);

    Database modifyDatabase(Long obTenantId, String dbName, ModifyDatabaseParam param);

    void deleteDatabase(Long obTenantId, String dbName);
}
