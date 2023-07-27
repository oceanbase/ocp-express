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

package com.oceanbase.ocp.core.el.meta;

import java.util.List;

import org.antlr.v4.runtime.FailedPredicateException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ELParser extends Parser {

    static {
        RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION);
    }

    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    public static final int T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, NUMBER = 6, OR = 7, AND = 8, EQUALS = 9,
            NOTEQUALS = 10, LT = 11, LTEQ = 12, GT = 13, GTEQ = 14, PLUS = 15, SUB = 16, MULT = 17,
            DIV = 18, MOD = 19, POW = 20, NOT = 21, IN = 22, AS = 23, FALSE = 24, TRUE = 25, MEMBERACCESSOR = 26,
            STD = 27, AVG = 28, VAR = 29, MAX = 30, MIN = 31, SUM = 32, COUNT = 33, DISTINCT = 34,
            ISNULL = 35, NOTNULL = 36, ISEMPTY = 37, NOTEMPTY = 38, ID = 39, STRING = 40, LINE_COMMENT = 41,
            COMMENT = 42, WS = 43, InvalidIdentifier = 44, UnknownToken = 45;
    public static final int RULE_expr = 0, RULE_exprList = 1;

    private static String[] makeRuleNames() {
        return new String[] {
                "expr", "exprList"
        };
    }

    public static final String[] ruleNames = makeRuleNames();

    private static String[] makeLiteralNames() {
        return new String[] {
                null, "'('", "')'", "'$'", "'@'", "','", null, null, null, null, null,
                "'<'", "'<='", "'>'", "'>='", "'+'", "'-'", "'*'", "'/'", "'%'", "'^'"
        };
    }

    private static final String[] _LITERAL_NAMES = makeLiteralNames();

    private static String[] makeSymbolicNames() {
        return new String[] {
                null, null, null, null, null, null, "NUMBER", "OR", "AND", "EQUALS",
                "NOTEQUALS", "LT", "LTEQ", "GT", "GTEQ", "PLUS", "SUB", "MULT", "DIV",
                "MOD", "POW", "NOT", "IN", "AS", "FALSE", "TRUE", "MEMBERACCESSOR", "STD",
                "AVG", "VAR", "MAX", "MIN", "SUM", "COUNT", "DISTINCT", "ISNULL", "NOTNULL",
                "ISEMPTY", "NOTEMPTY", "ID", "STRING", "LINE_COMMENT", "COMMENT", "WS",
                "InvalidIdentifier", "UnknownToken"
        };
    }

    private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;
    static {
        tokenNames = new String[_SYMBOLIC_NAMES.length];
        for (int i = 0; i < tokenNames.length; i++) {
            tokenNames[i] = VOCABULARY.getLiteralName(i);
            if (tokenNames[i] == null) {
                tokenNames[i] = VOCABULARY.getSymbolicName(i);
            }

            if (tokenNames[i] == null) {
                tokenNames[i] = "<INVALID>";
            }
        }
    }

    @Override
    @Deprecated
    public String[] getTokenNames() {
        return tokenNames;
    }

    @Override

    public Vocabulary getVocabulary() {
        return VOCABULARY;
    }

    @Override
    public String getGrammarFileName() {
        return "EL.g4";
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String getSerializedATN() {
        return _serializedATN;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }

    public ELParser(TokenStream input) {
        super(input);
        _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, new PredictionContextCache());
    }

    public static class ExprContext extends ParserRuleContext {

        public ExprContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_expr;
        }

        public ExprContext() {}

        public void copyFrom(ExprContext ctx) {
            super.copyFrom(ctx);
        }
    }
    public static class IsEmptyContext extends ExprContext {

        public TerminalNode ISEMPTY() {
            return getToken(ELParser.ISEMPTY, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public IsEmptyContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterIsEmpty(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitIsEmpty(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitIsEmpty(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class StdContext extends ExprContext {

        public TerminalNode STD() {
            return getToken(ELParser.STD, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public StdContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterStd(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitStd(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitStd(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class MaxContext extends ExprContext {

        public TerminalNode MAX() {
            return getToken(ELParser.MAX, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public MaxContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterMax(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitMax(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitMax(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class InContext extends ExprContext {

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public TerminalNode IN() {
            return getToken(ELParser.IN, 0);
        }

        public InContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterIn(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitIn(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitIn(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class VarContext extends ExprContext {

        public TerminalNode VAR() {
            return getToken(ELParser.VAR, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public VarContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterVar(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitVar(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitVar(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class StringContext extends ExprContext {

        public TerminalNode STRING() {
            return getToken(ELParser.STRING, 0);
        }

        public StringContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterString(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitString(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitString(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class CountContext extends ExprContext {

        public TerminalNode COUNT() {
            return getToken(ELParser.COUNT, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public CountContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterCount(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitCount(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitCount(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class NotNullContext extends ExprContext {

        public TerminalNode NOTNULL() {
            return getToken(ELParser.NOTNULL, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public NotNullContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterNotNull(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitNotNull(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitNotNull(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class MulDivModContext extends ExprContext {

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public TerminalNode MULT() {
            return getToken(ELParser.MULT, 0);
        }

        public TerminalNode DIV() {
            return getToken(ELParser.DIV, 0);
        }

        public TerminalNode MOD() {
            return getToken(ELParser.MOD, 0);
        }

        public MulDivModContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterMulDivMod(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitMulDivMod(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitMulDivMod(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class DistinctContext extends ExprContext {

        public TerminalNode DISTINCT() {
            return getToken(ELParser.DISTINCT, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public DistinctContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterDistinct(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitDistinct(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitDistinct(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class NumberContext extends ExprContext {

        public TerminalNode NUMBER() {
            return getToken(ELParser.NUMBER, 0);
        }

        public NumberContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterNumber(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitNumber(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitNumber(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class CompareContext extends ExprContext {

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public TerminalNode LT() {
            return getToken(ELParser.LT, 0);
        }

        public TerminalNode LTEQ() {
            return getToken(ELParser.LTEQ, 0);
        }

        public TerminalNode GT() {
            return getToken(ELParser.GT, 0);
        }

        public TerminalNode GTEQ() {
            return getToken(ELParser.GTEQ, 0);
        }

        public TerminalNode EQUALS() {
            return getToken(ELParser.EQUALS, 0);
        }

        public TerminalNode NOTEQUALS() {
            return getToken(ELParser.NOTEQUALS, 0);
        }

        public CompareContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterCompare(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitCompare(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitCompare(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class BooleanContext extends ExprContext {

        public TerminalNode TRUE() {
            return getToken(ELParser.TRUE, 0);
        }

        public TerminalNode FALSE() {
            return getToken(ELParser.FALSE, 0);
        }

        public BooleanContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterBoolean(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitBoolean(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitBoolean(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class ParenthesesContext extends ExprContext {

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public ParenthesesContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterParentheses(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitParentheses(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitParentheses(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class OppositeContext extends ExprContext {

        public TerminalNode NOT() {
            return getToken(ELParser.NOT, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public OppositeContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterOpposite(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitOpposite(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitOpposite(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class AddSubContext extends ExprContext {

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public TerminalNode PLUS() {
            return getToken(ELParser.PLUS, 0);
        }

        public TerminalNode SUB() {
            return getToken(ELParser.SUB, 0);
        }

        public AddSubContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterAddSub(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitAddSub(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitAddSub(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class ContextFieldContext extends ExprContext {

        public TerminalNode ID() {
            return getToken(ELParser.ID, 0);
        }

        public ContextFieldContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterContextField(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitContextField(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitContextField(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class SumContext extends ExprContext {

        public TerminalNode SUM() {
            return getToken(ELParser.SUM, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public SumContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterSum(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitSum(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitSum(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class FuncCallContext extends ExprContext {

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public TerminalNode MEMBERACCESSOR() {
            return getToken(ELParser.MEMBERACCESSOR, 0);
        }

        public TerminalNode ID() {
            return getToken(ELParser.ID, 0);
        }

        public ExprListContext exprList() {
            return getRuleContext(ExprListContext.class, 0);
        }

        public FuncCallContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterFuncCall(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitFuncCall(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitFuncCall(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class AvgContext extends ExprContext {

        public TerminalNode AVG() {
            return getToken(ELParser.AVG, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public AvgContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterAvg(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitAvg(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitAvg(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class MinContext extends ExprContext {

        public TerminalNode MIN() {
            return getToken(ELParser.MIN, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public MinContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterMin(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitMin(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitMin(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class MemberAccessContext extends ExprContext {

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public TerminalNode MEMBERACCESSOR() {
            return getToken(ELParser.MEMBERACCESSOR, 0);
        }

        public TerminalNode ID() {
            return getToken(ELParser.ID, 0);
        }

        public MemberAccessContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterMemberAccess(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitMemberAccess(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitMemberAccess(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class AliasContext extends ExprContext {

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public TerminalNode AS() {
            return getToken(ELParser.AS, 0);
        }

        public TerminalNode ID() {
            return getToken(ELParser.ID, 0);
        }

        public AliasContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterAlias(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitAlias(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitAlias(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class PowContext extends ExprContext {

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public TerminalNode POW() {
            return getToken(ELParser.POW, 0);
        }

        public PowContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterPow(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitPow(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitPow(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class IsNullContext extends ExprContext {

        public TerminalNode ISNULL() {
            return getToken(ELParser.ISNULL, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public IsNullContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterIsNull(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitIsNull(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitIsNull(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class NotEmptyContext extends ExprContext {

        public TerminalNode NOTEMPTY() {
            return getToken(ELParser.NOTEMPTY, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public NotEmptyContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterNotEmpty(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitNotEmpty(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitNotEmpty(this);
            else
                return visitor.visitChildren(this);
        }
    }
    public static class AndOrContext extends ExprContext {

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public TerminalNode AND() {
            return getToken(ELParser.AND, 0);
        }

        public TerminalNode OR() {
            return getToken(ELParser.OR, 0);
        }

        public AndOrContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterAndOr(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitAndOr(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitAndOr(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final ExprContext expr() throws RecognitionException {
        return expr(0);
    }

    private ExprContext expr(int _p) throws RecognitionException {
        ParserRuleContext _parentctx = _ctx;
        int _parentState = getState();
        ExprContext _localctx = new ExprContext(_ctx, _parentState);
        ExprContext _prevctx = _localctx;
        int _startState = 0;
        enterRecursionRule(_localctx, 0, RULE_expr, _p);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(77);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case NUMBER: {
                        _localctx = new NumberContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;

                        setState(5);
                        match(NUMBER);
                    }
                        break;
                    case STRING: {
                        _localctx = new StringContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(6);
                        match(STRING);
                    }
                        break;
                    case TRUE: {
                        _localctx = new BooleanContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(7);
                        match(TRUE);
                    }
                        break;
                    case FALSE: {
                        _localctx = new BooleanContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(8);
                        match(FALSE);
                    }
                        break;
                    case T__0: {
                        _localctx = new ParenthesesContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(9);
                        match(T__0);
                        setState(10);
                        expr(0);
                        setState(11);
                        match(T__1);
                    }
                        break;
                    case STD: {
                        _localctx = new StdContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(13);
                        match(STD);
                        setState(14);
                        match(T__0);
                        setState(15);
                        expr(0);
                        setState(16);
                        match(T__1);
                    }
                        break;
                    case AVG: {
                        _localctx = new AvgContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(18);
                        match(AVG);
                        setState(19);
                        match(T__0);
                        setState(20);
                        expr(0);
                        setState(21);
                        match(T__1);
                    }
                        break;
                    case VAR: {
                        _localctx = new VarContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(23);
                        match(VAR);
                        setState(24);
                        match(T__0);
                        setState(25);
                        expr(0);
                        setState(26);
                        match(T__1);
                    }
                        break;
                    case COUNT: {
                        _localctx = new CountContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(28);
                        match(COUNT);
                        setState(29);
                        match(T__0);
                        setState(30);
                        expr(0);
                        setState(31);
                        match(T__1);
                    }
                        break;
                    case DISTINCT: {
                        _localctx = new DistinctContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(33);
                        match(DISTINCT);
                        setState(34);
                        match(T__0);
                        setState(35);
                        expr(0);
                        setState(36);
                        match(T__1);
                    }
                        break;
                    case MIN: {
                        _localctx = new MinContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(38);
                        match(MIN);
                        setState(39);
                        match(T__0);
                        setState(40);
                        expr(0);
                        setState(41);
                        match(T__1);
                    }
                        break;
                    case MAX: {
                        _localctx = new MaxContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(43);
                        match(MAX);
                        setState(44);
                        match(T__0);
                        setState(45);
                        expr(0);
                        setState(46);
                        match(T__1);
                    }
                        break;
                    case SUM: {
                        _localctx = new SumContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(48);
                        match(SUM);
                        setState(49);
                        match(T__0);
                        setState(50);
                        expr(0);
                        setState(51);
                        match(T__1);
                    }
                        break;
                    case ISNULL: {
                        _localctx = new IsNullContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(53);
                        match(ISNULL);
                        setState(54);
                        match(T__0);
                        setState(55);
                        expr(0);
                        setState(56);
                        match(T__1);
                    }
                        break;
                    case NOTNULL: {
                        _localctx = new NotNullContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(58);
                        match(NOTNULL);
                        setState(59);
                        match(T__0);
                        setState(60);
                        expr(0);
                        setState(61);
                        match(T__1);
                    }
                        break;
                    case ISEMPTY: {
                        _localctx = new IsEmptyContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(63);
                        match(ISEMPTY);
                        setState(64);
                        match(T__0);
                        setState(65);
                        expr(0);
                        setState(66);
                        match(T__1);
                    }
                        break;
                    case NOTEMPTY: {
                        _localctx = new NotEmptyContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(68);
                        match(NOTEMPTY);
                        setState(69);
                        match(T__0);
                        setState(70);
                        expr(0);
                        setState(71);
                        match(T__1);
                    }
                        break;
                    case T__2:
                    case T__3: {
                        _localctx = new ContextFieldContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(73);
                        _la = _input.LA(1);
                        if (!(_la == T__2 || _la == T__3)) {
                            _errHandler.recoverInline(this);
                        } else {
                            if (_input.LA(1) == Token.EOF)
                                matchedEOF = true;
                            _errHandler.reportMatch(this);
                            consume();
                        }
                        setState(74);
                        match(ID);
                    }
                        break;
                    case NOT: {
                        _localctx = new OppositeContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(75);
                        match(NOT);
                        setState(76);
                        expr(3);
                    }
                        break;
                    default:
                        throw new NoViableAltException(this);
                }
                _ctx.stop = _input.LT(-1);
                setState(113);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 3, _ctx);
                while (_alt != 2 && _alt != ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (_parseListeners != null)
                            triggerExitRuleEvent();
                        _prevctx = _localctx;
                        {
                            setState(111);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 2, _ctx)) {
                                case 1: {
                                    _localctx = new PowContext(new ExprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(79);
                                    if (!(precpred(_ctx, 8)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 8)");
                                    setState(80);
                                    match(POW);
                                    setState(81);
                                    expr(8);
                                }
                                    break;
                                case 2: {
                                    _localctx = new MulDivModContext(new ExprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(82);
                                    if (!(precpred(_ctx, 7)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 7)");
                                    setState(83);
                                    _la = _input.LA(1);
                                    if (!((((_la) & ~0x3f) == 0
                                            && ((1L << _la) & ((1L << MULT) | (1L << DIV) | (1L << MOD))) != 0))) {
                                        _errHandler.recoverInline(this);
                                    } else {
                                        if (_input.LA(1) == Token.EOF)
                                            matchedEOF = true;
                                        _errHandler.reportMatch(this);
                                        consume();
                                    }
                                    setState(84);
                                    expr(8);
                                }
                                    break;
                                case 3: {
                                    _localctx = new AddSubContext(new ExprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(85);
                                    if (!(precpred(_ctx, 6)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 6)");
                                    setState(86);
                                    _la = _input.LA(1);
                                    if (!(_la == PLUS || _la == SUB)) {
                                        _errHandler.recoverInline(this);
                                    } else {
                                        if (_input.LA(1) == Token.EOF)
                                            matchedEOF = true;
                                        _errHandler.reportMatch(this);
                                        consume();
                                    }
                                    setState(87);
                                    expr(7);
                                }
                                    break;
                                case 4: {
                                    _localctx = new InContext(new ExprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(88);
                                    if (!(precpred(_ctx, 5)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 5)");
                                    setState(89);
                                    match(IN);
                                    setState(90);
                                    expr(6);
                                }
                                    break;
                                case 5: {
                                    _localctx = new CompareContext(new ExprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(91);
                                    if (!(precpred(_ctx, 4)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 4)");
                                    setState(92);
                                    _la = _input.LA(1);
                                    if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EQUALS) | (1L << NOTEQUALS)
                                            | (1L << LT) | (1L << LTEQ) | (1L << GT) | (1L << GTEQ))) != 0))) {
                                        _errHandler.recoverInline(this);
                                    } else {
                                        if (_input.LA(1) == Token.EOF)
                                            matchedEOF = true;
                                        _errHandler.reportMatch(this);
                                        consume();
                                    }
                                    setState(93);
                                    expr(5);
                                }
                                    break;
                                case 6: {
                                    _localctx = new AndOrContext(new ExprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(94);
                                    if (!(precpred(_ctx, 2)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 2)");
                                    setState(95);
                                    _la = _input.LA(1);
                                    if (!(_la == OR || _la == AND)) {
                                        _errHandler.recoverInline(this);
                                    } else {
                                        if (_input.LA(1) == Token.EOF)
                                            matchedEOF = true;
                                        _errHandler.reportMatch(this);
                                        consume();
                                    }
                                    setState(96);
                                    expr(3);
                                }
                                    break;
                                case 7: {
                                    _localctx = new MemberAccessContext(new ExprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(97);
                                    if (!(precpred(_ctx, 10)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 10)");
                                    setState(98);
                                    match(MEMBERACCESSOR);
                                    setState(99);
                                    match(ID);
                                }
                                    break;
                                case 8: {
                                    _localctx = new FuncCallContext(new ExprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(100);
                                    if (!(precpred(_ctx, 9)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 9)");
                                    setState(101);
                                    match(MEMBERACCESSOR);
                                    setState(102);
                                    match(ID);
                                    setState(103);
                                    match(T__0);
                                    setState(105);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__2)
                                            | (1L << T__3) | (1L << NUMBER) | (1L << NOT) | (1L << FALSE) | (1L << TRUE)
                                            | (1L << STD) | (1L << AVG) | (1L << VAR) | (1L << MAX) | (1L << MIN)
                                            | (1L << SUM) | (1L << COUNT) | (1L << DISTINCT) | (1L << ISNULL)
                                            | (1L << NOTNULL) | (1L << ISEMPTY) | (1L << NOTEMPTY)
                                            | (1L << STRING))) != 0)) {
                                        {
                                            setState(104);
                                            exprList();
                                        }
                                    }

                                    setState(107);
                                    match(T__1);
                                }
                                    break;
                                case 9: {
                                    _localctx = new AliasContext(new ExprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(108);
                                    if (!(precpred(_ctx, 1)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 1)");
                                    setState(109);
                                    match(AS);
                                    setState(110);
                                    match(ID);
                                }
                                    break;
                            }
                        }
                    }
                    setState(115);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 3, _ctx);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            unrollRecursionContexts(_parentctx);
        }
        return _localctx;
    }

    public static class ExprListContext extends ParserRuleContext {

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public ExprListContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_exprList;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).enterExprList(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof ELListener)
                ((ELListener) listener).exitExprList(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof ELVisitor)
                return ((ELVisitor<? extends T>) visitor).visitExprList(this);
            else
                return visitor.visitChildren(this);
        }
    }

    public final ExprListContext exprList() throws RecognitionException {
        ExprListContext _localctx = new ExprListContext(_ctx, getState());
        enterRule(_localctx, 2, RULE_exprList);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                {
                    setState(116);
                    expr(0);
                }
                setState(121);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == T__4) {
                    {
                        {
                            setState(117);
                            match(T__4);
                            setState(118);
                            expr(0);
                        }
                    }
                    setState(123);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
        switch (ruleIndex) {
            case 0:
                return expr_sempred((ExprContext) _localctx, predIndex);
        }
        return true;
    }

    private boolean expr_sempred(ExprContext _localctx, int predIndex) {
        switch (predIndex) {
            case 0:
                return precpred(_ctx, 8);
            case 1:
                return precpred(_ctx, 7);
            case 2:
                return precpred(_ctx, 6);
            case 3:
                return precpred(_ctx, 5);
            case 4:
                return precpred(_ctx, 4);
            case 5:
                return precpred(_ctx, 2);
            case 6:
                return precpred(_ctx, 10);
            case 7:
                return precpred(_ctx, 9);
            case 8:
                return precpred(_ctx, 1);
        }
        return true;
    }

    public static final String _serializedATN =
            "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3/\177\4\2\t\2\4\3" +
                    "\t\3\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3" +
                    "\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2" +
                    "\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3" +
                    "\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2" +
                    "\3\2\3\2\3\2\3\2\5\2P\n\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2" +
                    "\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\5\2l\n\2" +
                    "\3\2\3\2\3\2\3\2\7\2r\n\2\f\2\16\2u\13\2\3\3\3\3\3\3\7\3z\n\3\f\3\16\3" +
                    "}\13\3\3\3\2\3\2\4\2\4\2\7\3\2\5\6\3\2\23\25\3\2\21\22\3\2\13\20\3\2\t" +
                    "\n\2\u0099\2O\3\2\2\2\4v\3\2\2\2\6\7\b\2\1\2\7P\7\b\2\2\bP\7*\2\2\tP\7" +
                    "\33\2\2\nP\7\32\2\2\13\f\7\3\2\2\f\r\5\2\2\2\r\16\7\4\2\2\16P\3\2\2\2" +
                    "\17\20\7\35\2\2\20\21\7\3\2\2\21\22\5\2\2\2\22\23\7\4\2\2\23P\3\2\2\2" +
                    "\24\25\7\36\2\2\25\26\7\3\2\2\26\27\5\2\2\2\27\30\7\4\2\2\30P\3\2\2\2" +
                    "\31\32\7\37\2\2\32\33\7\3\2\2\33\34\5\2\2\2\34\35\7\4\2\2\35P\3\2\2\2" +
                    "\36\37\7#\2\2\37 \7\3\2\2 !\5\2\2\2!\"\7\4\2\2\"P\3\2\2\2#$\7$\2\2$%\7" +
                    "\3\2\2%&\5\2\2\2&\'\7\4\2\2\'P\3\2\2\2()\7!\2\2)*\7\3\2\2*+\5\2\2\2+," +
                    "\7\4\2\2,P\3\2\2\2-.\7 \2\2./\7\3\2\2/\60\5\2\2\2\60\61\7\4\2\2\61P\3" +
                    "\2\2\2\62\63\7\"\2\2\63\64\7\3\2\2\64\65\5\2\2\2\65\66\7\4\2\2\66P\3\2" +
                    "\2\2\678\7%\2\289\7\3\2\29:\5\2\2\2:;\7\4\2\2;P\3\2\2\2<=\7&\2\2=>\7\3" +
                    "\2\2>?\5\2\2\2?@\7\4\2\2@P\3\2\2\2AB\7\'\2\2BC\7\3\2\2CD\5\2\2\2DE\7\4" +
                    "\2\2EP\3\2\2\2FG\7(\2\2GH\7\3\2\2HI\5\2\2\2IJ\7\4\2\2JP\3\2\2\2KL\t\2" +
                    "\2\2LP\7)\2\2MN\7\27\2\2NP\5\2\2\5O\6\3\2\2\2O\b\3\2\2\2O\t\3\2\2\2O\n" +
                    "\3\2\2\2O\13\3\2\2\2O\17\3\2\2\2O\24\3\2\2\2O\31\3\2\2\2O\36\3\2\2\2O" +
                    "#\3\2\2\2O(\3\2\2\2O-\3\2\2\2O\62\3\2\2\2O\67\3\2\2\2O<\3\2\2\2OA\3\2" +
                    "\2\2OF\3\2\2\2OK\3\2\2\2OM\3\2\2\2Ps\3\2\2\2QR\f\n\2\2RS\7\26\2\2Sr\5" +
                    "\2\2\nTU\f\t\2\2UV\t\3\2\2Vr\5\2\2\nWX\f\b\2\2XY\t\4\2\2Yr\5\2\2\tZ[\f" +
                    "\7\2\2[\\\7\30\2\2\\r\5\2\2\b]^\f\6\2\2^_\t\5\2\2_r\5\2\2\7`a\f\4\2\2" +
                    "ab\t\6\2\2br\5\2\2\5cd\f\f\2\2de\7\34\2\2er\7)\2\2fg\f\13\2\2gh\7\34\2" +
                    "\2hi\7)\2\2ik\7\3\2\2jl\5\4\3\2kj\3\2\2\2kl\3\2\2\2lm\3\2\2\2mr\7\4\2" +
                    "\2no\f\3\2\2op\7\31\2\2pr\7)\2\2qQ\3\2\2\2qT\3\2\2\2qW\3\2\2\2qZ\3\2\2" +
                    "\2q]\3\2\2\2q`\3\2\2\2qc\3\2\2\2qf\3\2\2\2qn\3\2\2\2ru\3\2\2\2sq\3\2\2" +
                    "\2st\3\2\2\2t\3\3\2\2\2us\3\2\2\2v{\5\2\2\2wx\7\7\2\2xz\5\2\2\2yw\3\2" +
                    "\2\2z}\3\2\2\2{y\3\2\2\2{|\3\2\2\2|\5\3\2\2\2}{\3\2\2\2\7Okqs{";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}
