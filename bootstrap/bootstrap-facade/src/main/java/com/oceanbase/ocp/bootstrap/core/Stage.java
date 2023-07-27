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

import java.util.Arrays;
import java.util.List;

public enum Stage {

    /**
     * Upgrade stages.
     */
    RENAME_TABLES,
    CREATE_TABLES,
    ALTER_TABLES,
    DEFAULT_DATA,
    MIGRATIONS,
    CONFIG_PROPERTIES,
    ALTER_TABLES_DELAYED,
    MIGRATIONS_DELAYED,
    DROP_TABLES;

    public static List<Stage> upgradeStages() {
        return Arrays.asList(RENAME_TABLES, CREATE_TABLES, ALTER_TABLES, DEFAULT_DATA, MIGRATIONS,
                CONFIG_PROPERTIES, ALTER_TABLES_DELAYED, DROP_TABLES);
    }

}
