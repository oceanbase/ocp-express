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

package com.oceanbase.ocp.obparser;

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public final class Grammars {

    private static Cache<Class, Grammar> CACHE = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    public synchronized static Grammar.MysqlSQL getMysqlSQL() {
        Grammar grammar = CACHE.getIfPresent(Grammar.MysqlSQL.class);
        if (grammar != null) {
            return (Grammar.MysqlSQL) grammar;
        }
        Grammar.MysqlSQL mysqlGrammar = new Grammar.MysqlSQL();
        CACHE.put(Grammar.MysqlSQL.class, mysqlGrammar);
        return mysqlGrammar;
    }

    public synchronized static Grammar.OracleSQL getOracleSQL() {
        Grammar grammar = CACHE.getIfPresent(Grammar.OracleSQL.class);
        if (grammar != null) {
            return (Grammar.OracleSQL) grammar;
        }
        Grammar.OracleSQL oracleGrammar = new Grammar.OracleSQL();
        CACHE.put(Grammar.OracleSQL.class, oracleGrammar);
        return oracleGrammar;
    }
}
