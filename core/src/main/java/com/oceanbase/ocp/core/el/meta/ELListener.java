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

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ELParser}.
 */
public interface ELListener extends ParseTreeListener {

    /**
     * Enter a parse tree produced by the {@code IsEmpty} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterIsEmpty(ELParser.IsEmptyContext ctx);

    /**
     * Exit a parse tree produced by the {@code IsEmpty} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitIsEmpty(ELParser.IsEmptyContext ctx);

    /**
     * Enter a parse tree produced by the {@code Std} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterStd(ELParser.StdContext ctx);

    /**
     * Exit a parse tree produced by the {@code Std} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitStd(ELParser.StdContext ctx);

    /**
     * Enter a parse tree produced by the {@code Max} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterMax(ELParser.MaxContext ctx);

    /**
     * Exit a parse tree produced by the {@code Max} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitMax(ELParser.MaxContext ctx);

    /**
     * Enter a parse tree produced by the {@code In} labeled alternative in .
     *
     * @param ctx the parse tree
     */
    void enterIn(ELParser.InContext ctx);

    /**
     * Exit a parse tree produced by the {@code In} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitIn(ELParser.InContext ctx);

    /**
     * Enter a parse tree produced by the {@code Var} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterVar(ELParser.VarContext ctx);

    /**
     * Exit a parse tree produced by the {@code Var} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitVar(ELParser.VarContext ctx);

    /**
     * Enter a parse tree produced by the {@code String} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterString(ELParser.StringContext ctx);

    /**
     * Exit a parse tree produced by the {@code String} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitString(ELParser.StringContext ctx);

    /**
     * Enter a parse tree produced by the {@code Count} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterCount(ELParser.CountContext ctx);

    /**
     * Exit a parse tree produced by the {@code Count} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitCount(ELParser.CountContext ctx);

    /**
     * Enter a parse tree produced by the {@code NotNull} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterNotNull(ELParser.NotNullContext ctx);

    /**
     * Exit a parse tree produced by the {@code NotNull} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitNotNull(ELParser.NotNullContext ctx);

    /**
     * Enter a parse tree produced by the {@code MulDivMod} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterMulDivMod(ELParser.MulDivModContext ctx);

    /**
     * Exit a parse tree produced by the {@code MulDivMod} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitMulDivMod(ELParser.MulDivModContext ctx);

    /**
     * Enter a parse tree produced by the {@code Distinct} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterDistinct(ELParser.DistinctContext ctx);

    /**
     * Exit a parse tree produced by the {@code Distinct} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitDistinct(ELParser.DistinctContext ctx);

    /**
     * Enter a parse tree produced by the {@code Number} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterNumber(ELParser.NumberContext ctx);

    /**
     * Exit a parse tree produced by the {@code Number} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitNumber(ELParser.NumberContext ctx);

    /**
     * Enter a parse tree produced by the {@code Compare} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterCompare(ELParser.CompareContext ctx);

    /**
     * Exit a parse tree produced by the {@code Compare} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitCompare(ELParser.CompareContext ctx);

    /**
     * Enter a parse tree produced by the {@code Boolean} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterBoolean(ELParser.BooleanContext ctx);

    /**
     * Exit a parse tree produced by the {@code Boolean} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitBoolean(ELParser.BooleanContext ctx);

    /**
     * Enter a parse tree produced by the {@code Parentheses} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterParentheses(ELParser.ParenthesesContext ctx);

    /**
     * Exit a parse tree produced by the {@code Parentheses} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitParentheses(ELParser.ParenthesesContext ctx);

    /**
     * Enter a parse tree produced by the {@code Opposite} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterOpposite(ELParser.OppositeContext ctx);

    /**
     * Exit a parse tree produced by the {@code Opposite} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitOpposite(ELParser.OppositeContext ctx);

    /**
     * Enter a parse tree produced by the {@code AddSub} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterAddSub(ELParser.AddSubContext ctx);

    /**
     * Exit a parse tree produced by the {@code AddSub} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitAddSub(ELParser.AddSubContext ctx);

    /**
     * Enter a parse tree produced by the {@code contextField} labeled alternative
     * in {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterContextField(ELParser.ContextFieldContext ctx);

    /**
     * Exit a parse tree produced by the {@code contextField} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitContextField(ELParser.ContextFieldContext ctx);

    /**
     * Enter a parse tree produced by the {@code Sum} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterSum(ELParser.SumContext ctx);

    /**
     * Exit a parse tree produced by the {@code Sum} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitSum(ELParser.SumContext ctx);

    /**
     * Enter a parse tree produced by the {@code FuncCall} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterFuncCall(ELParser.FuncCallContext ctx);

    /**
     * Exit a parse tree produced by the {@code FuncCall} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitFuncCall(ELParser.FuncCallContext ctx);

    /**
     * Enter a parse tree produced by the {@code Avg} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterAvg(ELParser.AvgContext ctx);

    /**
     * Exit a parse tree produced by the {@code Avg} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitAvg(ELParser.AvgContext ctx);

    /**
     * Enter a parse tree produced by the {@code Min} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterMin(ELParser.MinContext ctx);

    /**
     * Exit a parse tree produced by the {@code Min} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitMin(ELParser.MinContext ctx);

    /**
     * Enter a parse tree produced by the {@code MemberAccess} labeled alternative
     * in {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterMemberAccess(ELParser.MemberAccessContext ctx);

    /**
     * Exit a parse tree produced by the {@code MemberAccess} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitMemberAccess(ELParser.MemberAccessContext ctx);

    /**
     * Enter a parse tree produced by the {@code Alias} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterAlias(ELParser.AliasContext ctx);

    /**
     * Exit a parse tree produced by the {@code Alias} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitAlias(ELParser.AliasContext ctx);

    /**
     * Enter a parse tree produced by the {@code Pow} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterPow(ELParser.PowContext ctx);

    /**
     * Exit a parse tree produced by the {@code Pow} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitPow(ELParser.PowContext ctx);

    /**
     * Enter a parse tree produced by the {@code IsNull} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterIsNull(ELParser.IsNullContext ctx);

    /**
     * Exit a parse tree produced by the {@code IsNull} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitIsNull(ELParser.IsNullContext ctx);

    /**
     * Enter a parse tree produced by the {@code NotEmpty} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterNotEmpty(ELParser.NotEmptyContext ctx);

    /**
     * Exit a parse tree produced by the {@code NotEmpty} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitNotEmpty(ELParser.NotEmptyContext ctx);

    /**
     * Enter a parse tree produced by the {@code AndOr} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterAndOr(ELParser.AndOrContext ctx);

    /**
     * Exit a parse tree produced by the {@code AndOr} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitAndOr(ELParser.AndOrContext ctx);

    /**
     * Enter a parse tree produced by {@link ELParser#exprList}.
     *
     * @param ctx the parse tree
     */
    void enterExprList(ELParser.ExprListContext ctx);

    /**
     * Exit a parse tree produced by {@link ELParser#exprList}.
     *
     * @param ctx the parse tree
     */
    void exitExprList(ELParser.ExprListContext ctx);
}
