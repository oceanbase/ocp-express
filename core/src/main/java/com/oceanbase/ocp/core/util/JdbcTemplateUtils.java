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

package com.oceanbase.ocp.core.util;

import java.sql.Statement;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcTemplateUtils {

    public static int batchInsertAffectRows(int[] rowsAffectedArray) {
        if (rowsAffectedArray == null) {
            return 0;
        }
        int affectRows = 0;
        for (int rowsAffected : rowsAffectedArray) {
            if (rowsAffected > 0 || rowsAffected == Statement.SUCCESS_NO_INFO) {
                affectRows++;
            }
        }
        return affectRows;
    }

}
