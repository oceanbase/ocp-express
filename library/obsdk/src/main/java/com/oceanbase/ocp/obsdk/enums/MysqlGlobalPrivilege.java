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

package com.oceanbase.ocp.obsdk.enums;

import lombok.Getter;

public enum MysqlGlobalPrivilege implements GlobalPrivilege {

    ALTER("ALTER", "alter_priv", "priv_alter"),
    CREATE("CREATE", "create_priv", "priv_create"),
    DELETE("DELETE", "delete_priv", "priv_delete"),
    DROP("DROP", "drop_priv", "priv_drop"),
    INSERT("INSERT", "insert_priv", "priv_insert"),
    SELECT("SELECT", "select_priv", "priv_select"),
    UPDATE("UPDATE", "update_priv", "priv_update"),
    INDEX("INDEX", "index_priv", "priv_index"),
    CREATE_VIEW("CREATE VIEW", "create_view_priv", "priv_create_view"),
    SHOW_VIEW("SHOW VIEW", "show_view_priv", "priv_show_view"),
    CREATE_USER("CREATE USER", "create_user_priv", "priv_create_user"),
    PROCESS("PROCESS", "process_priv", "priv_process"),
    SUPER("SUPER", "super_priv", "priv_super"),
    SHOW_DATABASES("SHOW DATABASES", "show_db_priv", "priv_show_db"),
    GRANT_OPTION("GRANT OPTION", "grant_priv", "priv_grant_option");

    @Getter
    private String value;

    private String mysqlUserColumn;

    private String allUserColumn;

    MysqlGlobalPrivilege(String value, String mysqlUserColumn, String allUserColumn) {
        this.value = value;
        this.mysqlUserColumn = mysqlUserColumn;
        this.allUserColumn = allUserColumn;
    }

    @Override
    public String toString() {
        return value;
    }

    public static MysqlGlobalPrivilege fromValue(String text) {
        for (MysqlGlobalPrivilege b : MysqlGlobalPrivilege.values()) {
            if (b.value.equals(text) || b.mysqlUserColumn.equals(text) || b.allUserColumn.equals(text)
                    || b.name().equals(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("wrong type of global privilege: " + text);
    }
}
