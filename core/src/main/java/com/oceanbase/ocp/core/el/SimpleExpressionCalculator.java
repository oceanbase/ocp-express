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

package com.oceanbase.ocp.core.el;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.oceanbase.ocp.core.el.meta.ELLexer;
import com.oceanbase.ocp.core.el.meta.ELParser;
import com.oceanbase.ocp.core.el.util.ObjectUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleExpressionCalculator {

    public static Object nullSafeEval(String expression, Map<String, Object> contextVariable) throws IOException {
        ANTLRInputStream inputStream = new ANTLRInputStream(
                new ByteArrayInputStream(expression.getBytes()));
        ELLexer lexer = new ELLexer(inputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ELParser parser = new ELParser(tokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(new SyntaxErrorListener());
        ParseTree parseTree = parser.expr();
        final BaseVisitor visitor = new BaseVisitor();
        contextVariable.forEach(visitor::addAttribute);
        return visitor.visit(parseTree);
    }

    public static Object evalWithObject(String expression, Object obj) throws IOException {
        return nullSafeEval(expression, ObjectUtils.getFieldMap(obj));
    }

    public static Double evalToDouble(Object obj, String expression) {
        Object value = null;
        try {
            value = evalWithObject(expression, obj);
        } catch (Exception e) {
            log.info("Error in evaluating expr:{} with object: {}", expression, obj, e);
        }
        return value == null ? null : Double.valueOf(value.toString());
    }

    public static Object evalToObject(Object obj, String expression) {
        Object value = null;
        try {
            value = evalWithObject(expression, obj);
        } catch (Exception e) {
            log.info("Error in evaluating expr:{} with object: {}", expression, obj, e);
        }
        return value;
    }

    public static Boolean evalToBoolean(Object obj, String expression, Boolean defaultValue) {
        Object value = null;
        try {
            value = evalWithObject(expression, obj);
        } catch (Exception e) {
            log.info("Error in evaluating expr:{} with object: {}", expression, obj, e);
        }
        return value == null ? defaultValue : Boolean.parseBoolean(value.toString());
    }
}
