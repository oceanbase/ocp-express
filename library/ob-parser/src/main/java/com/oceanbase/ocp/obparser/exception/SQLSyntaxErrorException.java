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

package com.oceanbase.ocp.obparser.exception;

import org.antlr.v4.runtime.RecognitionException;
import org.apache.commons.lang3.StringUtils;

public class SQLSyntaxErrorException extends RuntimeException {

    public final String sql;
    public final int line;
    public final int pos;
    public final String message;

    public SQLSyntaxErrorException(String sql) {
        super("SQL syntax error, sql=" + sql);
        this.sql = sql;
        this.line = -1;
        this.pos = -1;
        this.message = null;
    }

    public SQLSyntaxErrorException(String sql, int line, int pos, String message, RecognitionException cause) {
        super("SQL syntax error, line=" + line + ", pos=" + pos + ", message=" + message + ", sql="
                + StringUtils.substring(sql, 0, 200),
                cause);
        this.sql = sql;
        this.line = line;
        this.pos = pos;
        this.message = message;
    }
}
