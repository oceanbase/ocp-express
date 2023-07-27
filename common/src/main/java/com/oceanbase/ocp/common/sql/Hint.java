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

package com.oceanbase.ocp.common.sql;

public interface Hint {

    String text();
}


enum ReadConsistency implements Hint {

    /**
     * Consistency read hint type.
     */
    STRONG("strong"),
    WEAK("weak"),
    ;

    private final String value;

    ReadConsistency(String value) {
        this.value = value;
    }

    @Override
    public String text() {
        return String.format("read_consistency(%s)", value);
    }

    @Override
    public String toString() {
        return text();
    }
}


class QueryTimeout implements Hint {

    private final long timeoutMillis;

    public QueryTimeout(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public String text() {
        return String.format("query_timeout(%d)", timeoutMillis);
    }

    @Override
    public String toString() {
        return text();
    }
}
