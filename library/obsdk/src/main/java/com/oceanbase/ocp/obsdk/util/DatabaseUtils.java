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

package com.oceanbase.ocp.obsdk.util;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import com.oceanbase.ocp.obsdk.accessor.database.model.ObDatabase;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;

public class DatabaseUtils {

    private static final String SHOW_DATABASE = "SHOW DATABASES";

    private static final String SELECT_OB_DATABASES = "SELECT"
            + " o.CREATED AS GMT_CREATE, o.OBJECT_ID AS DATABASE_ID, d.DATABASE_NAME AS NAME, c.ID AS COLLATION_TYPE, NULL AS PRIMARY_ZONE,"
            + " (CASE READ_ONLY WHEN 'YES' THEN 1 ELSE 0 END) AS READ_ONLY"
            + " FROM oceanbase.DBA_OB_DATABASES d JOIN oceanbase.DBA_OBJECTS o JOIN information_schema.collations c"
            + " ON d.DATABASE_NAME = o.OBJECT_NAME AND d.COLLATION = c.COLLATION_NAME"
            + " WHERE o.OBJECT_TYPE = 'DATABASE'";

    public static List<ObDatabase> listDatabases(ObConnectTemplate connectTemplate) {
        List<String> dbNames = connectTemplate.query(SHOW_DATABASE, new SingleColumnRowMapper<>(String.class));
        List<ObDatabase> dbList =
                connectTemplate.query(SELECT_OB_DATABASES, new BeanPropertyRowMapper<>(ObDatabase.class));
        return dbList.stream().filter(db -> dbNames.contains(db.getName())).collect(Collectors.toList());
    }

    public static ObDatabase getDatabase(ObConnectTemplate connectTemplate, String dbName) {
        String sql = SELECT_OB_DATABASES + " AND database_name = ?";
        return connectTemplate.queryForObject(sql, new Object[] {dbName},
                new BeanPropertyRowMapper<>(ObDatabase.class));
    }

}
