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

package com.oceanbase.ocp.bootstrap.core;

public class Keys {

    public static final String DATA_SOURCE = "data_source",
            TABLE_DEFINITIONS = "table_definitions",
            DATA_DEFINITIONS = "data_definitions",
            MIGRATIONS = "migrations",
            FROM_CONFIG_FILE = "from_config_file",
            TABLE_NAME = "table_name",
            SINCE = "since",
            ON_DUPLICATE_UPDATE = "on_duplicate_update",
            UPDATE_BY = "update_by", DELETE_BY = "delete_by", RAW_SQLS = "raw_sqls",
            ROWS = "rows", DELETE = "delete",
            EXPR = "expr", WITH = "with", DATA = "data", CONDITION = "condition",
            SOURCE_SQL = "source_sql", IGNORE_SOURCE_SQL_ERROR = "ignore_source_sql_error",
            TARGET_TABLE = "target_table",

            COMMENT = "comment", FIELDS = "fields", INDEXES = "indexes", PRIMARY_KEY = "primary_key",
            PARTITION = "partition", SUBPARTITION = "subpartition", RANGE_PARTITIONS = "range_partitions",
            BY_EXPR = "by_expr", HASH_PARTITION_COUNT = "hash_partition_count",
            AUTO_INCREMENT = "auto_increment", TYPE = "type", NULLABLE = "nullable", DEFAULT_VALUE = "default_value",
            ON_UPDATE = "on_update",
            DEFAULT_CHARSET = "default_charset", UNIQUE = "unique", LOCAL = "local",
            RENAMED_FROM = "renamed_from", DROP = "drop", DELAY = "delay";
}
