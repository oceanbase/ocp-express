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

public enum DbPrivType {

    ALTER("ALTER", "alter_priv"),
    CREATE("CREATE", "create_priv"),
    DELETE("DELETE", "delete_priv"),
    DROP("DROP", "drop_priv"),
    INSERT("INSERT", "insert_priv"),
    SELECT("SELECT", "select_priv"),
    UPDATE("UPDATE", "update_priv"),
    INDEX("INDEX", "index_priv"),
    CREATE_VIEW("CREATE VIEW", "create_view_priv"),
    SHOW_VIEW("SHOW VIEW", "show_view_priv");

    private String value;

    private String column;

    DbPrivType(String value, String column) {
        this.value = value;
        this.column = column;
    }

    @Override
    public String toString() {
        return value;
    }

    public static DbPrivType fromValue(String text) {
        for (DbPrivType b : DbPrivType.values()) {
            if (b.value.equals(text) || b.column.equals(text) || b.name().equals(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("wrong type of db privilege: " + text);
    }
}
