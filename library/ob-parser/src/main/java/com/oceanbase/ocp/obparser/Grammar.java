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

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.commons.lang3.Validate;

public abstract class Grammar<TLexer extends TokenSource, TParser extends Parser, TRoot extends ParserRuleContext, TListener extends ParseTreeListener, TVisitor extends ParseTreeVisitor<?>> {

    private final Function<CharStream, TLexer> lexerFunc;
    private final Function<TokenStream, TParser> parserFunc;
    private final Function<TParser, TRoot> rootFunc;
    private volatile AtomicLong counter;

    Grammar(Function<CharStream, TLexer> lexerFunc, Function<TokenStream, TParser> parserFunc,
            Function<TParser, TRoot> rootFunc) {
        this.lexerFunc = lexerFunc;
        this.parserFunc = parserFunc;
        this.rootFunc = rootFunc;
        counter = new AtomicLong();
    }

    public final TRoot parse(String text) {
        return parse(text, null, null);
    }

    public final TRoot parse(String text, ANTLRErrorListener errorListener) {
        return parse(text, errorListener, null);
    }

    public final TRoot parse(String text, TListener parseListener) {
        return parse(text, null, parseListener);
    }

    /**
     * Clear the DFA cache used by the current instance. Since the DFA cache may be
     * shared by multiple ATN simulators, this method may affect the performance
     * (but not accuracy) of other parsers which are being used concurrently.
     */
    public final TRoot parse(String text, ANTLRErrorListener errorListener, TListener parseListener) {
        Validate.notBlank(text, "SQL text require non-blank");
        TLexer lex = lexerFunc.apply(CharStreams.fromString(text));
        TParser parser = parserFunc.apply(new CommonTokenStream(lex));
        if (counter.getAndIncrement() % 1000 == 0) {
            parser.getInterpreter().clearDFA();
        }
        if (errorListener != null) {
            parser.addErrorListener(errorListener);
        }
        if (parseListener != null) {
            parser.addParseListener(parseListener);
        }
        return rootFunc.apply(parser);
    }

    public static final class MysqlSQL extends
            Grammar<com.oceanbase.ocp.sqlparser.obmysql.sql.OBLexer, com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser, com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser.Sql_stmtContext, com.oceanbase.ocp.sqlparser.obmysql.sql.OBParserListener, com.oceanbase.ocp.sqlparser.obmysql.sql.OBParserVisitor<?>> {

        public MysqlSQL() {
            super(com.oceanbase.ocp.sqlparser.obmysql.sql.OBLexer::new,
                    com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser::new,
                    com.oceanbase.ocp.sqlparser.obmysql.sql.OBParser::sql_stmt);
        }
    }

    public static final class OracleSQL extends
            Grammar<com.oceanbase.ocp.sqlparser.oboracle.sql.OBLexer, com.oceanbase.ocp.sqlparser.oboracle.sql.OBParser, com.oceanbase.ocp.sqlparser.oboracle.sql.OBParser.Sql_stmtContext, com.oceanbase.ocp.sqlparser.oboracle.sql.OBParserListener, com.oceanbase.ocp.sqlparser.oboracle.sql.OBParserVisitor<?>> {

        public OracleSQL() {
            super(com.oceanbase.ocp.sqlparser.oboracle.sql.OBLexer::new,
                    com.oceanbase.ocp.sqlparser.oboracle.sql.OBParser::new,
                    com.oceanbase.ocp.sqlparser.oboracle.sql.OBParser::sql_stmt);
        }
    }
}
