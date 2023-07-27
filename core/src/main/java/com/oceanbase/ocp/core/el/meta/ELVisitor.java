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

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ELParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 *        operations with no return type.
 */
public interface ELVisitor<T> extends ParseTreeVisitor<T> {

    /**
     * Visit a parse tree produced by the {@code IsEmpty} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitIsEmpty(ELParser.IsEmptyContext ctx);

    /**
     * Visit a parse tree produced by the {@code Std} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitStd(ELParser.StdContext ctx);

    /**
     * Visit a parse tree produced by the {@code Max} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitMax(ELParser.MaxContext ctx);

    /**
     * Visit a parse tree produced by the {@code In} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitIn(ELParser.InContext ctx);

    /**
     * Visit a parse tree produced by the {@code Var} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitVar(ELParser.VarContext ctx);

    /**
     * Visit a parse tree produced by the {@code String} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitString(ELParser.StringContext ctx);

    /**
     * Visit a parse tree produced by the {@code Count} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCount(ELParser.CountContext ctx);

    /**
     * Visit a parse tree produced by the {@code NotNull} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitNotNull(ELParser.NotNullContext ctx);

    /**
     * Visit a parse tree produced by the {@code MulDivMod} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitMulDivMod(ELParser.MulDivModContext ctx);

    /**
     * Visit a parse tree produced by the {@code Distinct} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDistinct(ELParser.DistinctContext ctx);

    /**
     * Visit a parse tree produced by the {@code Number} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitNumber(ELParser.NumberContext ctx);

    /**
     * Visit a parse tree produced by the {@code Compare} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCompare(ELParser.CompareContext ctx);

    /**
     * Visit a parse tree produced by the {@code Boolean} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitBoolean(ELParser.BooleanContext ctx);

    /**
     * Visit a parse tree produced by the {@code Parentheses} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitParentheses(ELParser.ParenthesesContext ctx);

    /**
     * Visit a parse tree produced by the {@code Opposite} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitOpposite(ELParser.OppositeContext ctx);

    /**
     * Visit a parse tree produced by the {@code AddSub} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAddSub(ELParser.AddSubContext ctx);

    /**
     * Visit a parse tree produced by the {@code contextField} labeled alternative
     * in {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitContextField(ELParser.ContextFieldContext ctx);

    /**
     * Visit a parse tree produced by the {@code Sum} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSum(ELParser.SumContext ctx);

    /**
     * Visit a parse tree produced by the {@code FuncCall} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitFuncCall(ELParser.FuncCallContext ctx);

    /**
     * Visit a parse tree produced by the {@code Avg} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAvg(ELParser.AvgContext ctx);

    /**
     * Visit a parse tree produced by the {@code Min} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitMin(ELParser.MinContext ctx);

    /**
     * Visit a parse tree produced by the {@code MemberAccess} labeled alternative
     * in {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitMemberAccess(ELParser.MemberAccessContext ctx);

    /**
     * Visit a parse tree produced by the {@code Alias} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAlias(ELParser.AliasContext ctx);

    /**
     * Visit a parse tree produced by the {@code Pow} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitPow(ELParser.PowContext ctx);

    /**
     * Visit a parse tree produced by the {@code IsNull} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitIsNull(ELParser.IsNullContext ctx);

    /**
     * Visit a parse tree produced by the {@code NotEmpty} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitNotEmpty(ELParser.NotEmptyContext ctx);

    /**
     * Visit a parse tree produced by the {@code AndOr} labeled alternative in
     * {@link ELParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAndOr(ELParser.AndOrContext ctx);

    /**
     * Visit a parse tree produced by {@link ELParser#exprList}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitExprList(ELParser.ExprListContext ctx);
}
