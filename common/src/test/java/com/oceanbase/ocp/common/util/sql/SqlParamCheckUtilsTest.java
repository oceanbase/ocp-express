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

package com.oceanbase.ocp.common.util.sql;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SqlParamCheckUtilsTest {

    @Test
    public void check() {
        String name = "whatever";
        String name1 = "你好12aA_-,.";
        assertTrue(SqlParamCheckUtils.check(name, null));
        assertTrue(SqlParamCheckUtils.check(name1, ""));

        name = " whatever";
        assertFalse(SqlParamCheckUtils.check(name, null));

        name = "whatever%";
        assertFalse(SqlParamCheckUtils.check(name, null));

        name = "whatever😁";
        assertFalse(SqlParamCheckUtils.check(name, null));

        String rule = "^[A-Za-z0-9\\u4e00-\\u9fa5\\-_,\\.%]*$";
        name = "whatever%";
        assertTrue(SqlParamCheckUtils.check(name, rule));
    }

}
