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

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import com.oceanbase.ocp.obparser.exception.SQLSyntaxErrorException;
import com.oceanbase.ocp.sqlparser.oboracle.sql.OBParser;
import com.oceanbase.ocp.sqlparser.oboracle.sql.OBParserBaseVisitor;

public final class OracleParsers {

    /**
     * Parse sql, return root node of sql tree.
     *
     * @param text SQL string
     * @return AST of sql
     * @throws IllegalArgumentException contents empty
     * @throws SQLSyntaxErrorException grammar error
     */
    public static OBParser.Sql_stmtContext parseSql(String text) {
        return Grammars.getOracleSQL().parse(text, new BaseErrorListener() {

            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                    int charPositionInLine, String msg, RecognitionException e) {
                throw new SQLSyntaxErrorException(text.substring(0, Math.min(256, text.length())), line,
                        charPositionInLine, msg, e);
            }
        });
    }

    /**
     * Parse statement and return first statement AST.
     *
     * @param text SQL
     * @return first state ast
     * @throws IllegalArgumentException empty sql
     * @throws SQLSyntaxErrorException grammar error
     */
    public static OBParser.StmtContext parseSqlForStmt(String text) {
        return parseSql(text).stmt_list().stmt();
    }

    /**
     * Parameterize sql
     *
     * @param text SQL
     * @param replacement replacement of parameters
     * @return parameterized sql
     * @throws IllegalArgumentException empty sql
     * @throws SQLSyntaxErrorException grammar error
     */
    public static String parameterizeSql(String text, String replacement) {
        StringBuffer s = new StringBuffer(text);
        OBParser.StmtContext stmt = parseSqlForStmt(text);
        List<OBParser.LiteralContext> occurences = new ArrayList<>();
        new OBParserBaseVisitor<Object>() {

            @Override
            public Object visitLiteral(OBParser.LiteralContext ctx) {
                occurences.add(ctx);
                return super.visitLiteral(ctx);
            }
        }.visit(stmt);
        occurences.sort(Comparator.comparingInt(a -> -a.getStart().getStartIndex()));
        for (OBParser.LiteralContext c : occurences) {
            s.replace(c.getStart().getStartIndex(), c.getStop().getStopIndex() + 1, replacement);
        }
        return s.toString();
    }

    /**
     * Parse type of first statement.
     *
     * @param text SQL
     * @return SQL type of first statement.
     * @throws SQLSyntaxErrorException grammar error
     */
    public static SqlType parseSqlType(String text) {
        if (isBlank(text)) {
            return SqlType.OTHER;
        }
        return new OBParserBaseVisitor<SqlType>() {

            @Override
            public SqlType visitStmt(OBParser.StmtContext ctx) {
                if (ctx.select_stmt() != null) {
                    if (ctx.select_stmt().for_update() != null) {
                        return SqlType.SELECT_FOR_UPDATE;
                    }
                    return SqlType.SELECT;
                } else if (ctx.insert_stmt() != null) {
                    return SqlType.INSERT;
                } else if (ctx.update_stmt() != null) {
                    return SqlType.UPDATE;
                } else if (ctx.delete_stmt() != null) {
                    return SqlType.DELETE;
                } else if (ctx.merge_stmt() != null) {
                    return SqlType.MERGE;
                } else if (ctx.explain_stmt() != null) {
                    return SqlType.EXPLAIN;
                } else if (ctx.commit_stmt() != null) {
                    return SqlType.COMMIT;
                } else if (ctx.rollback_stmt() != null) {
                    return SqlType.ROLLBACK;
                } else if (ctx.variable_set_stmt() != null) {
                    return SqlType.SET;
                } else if (ctx.show_stmt() != null) {
                    return SqlType.SHOW;
                }
                return SqlType.OTHER;
            }
        }.visit(parseSqlForStmt(text));
    }

}
