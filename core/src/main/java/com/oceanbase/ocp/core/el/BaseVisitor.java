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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.oceanbase.ocp.common.util.MathUtils;
import com.oceanbase.ocp.core.el.enums.BinaryOperation;
import com.oceanbase.ocp.core.el.enums.UnaryOperation;
import com.oceanbase.ocp.core.el.exception.AliasDuplicatedException;
import com.oceanbase.ocp.core.el.exception.DivideZeroException;
import com.oceanbase.ocp.core.el.exception.IllegalAccessException;
import com.oceanbase.ocp.core.el.exception.IllegalArgumentException;
import com.oceanbase.ocp.core.el.exception.InvocationTargetException;
import com.oceanbase.ocp.core.el.exception.NoSuchContextValueException;
import com.oceanbase.ocp.core.el.exception.NoSuchFieldException;
import com.oceanbase.ocp.core.el.exception.UnsupportedTypeOfBinaryOperationException;
import com.oceanbase.ocp.core.el.exception.UnsupportedTypeOfUnaryOperationException;
import com.oceanbase.ocp.core.el.meta.ELBaseVisitor;
import com.oceanbase.ocp.core.el.meta.ELParser.AddSubContext;
import com.oceanbase.ocp.core.el.meta.ELParser.AliasContext;
import com.oceanbase.ocp.core.el.meta.ELParser.AndOrContext;
import com.oceanbase.ocp.core.el.meta.ELParser.AvgContext;
import com.oceanbase.ocp.core.el.meta.ELParser.BooleanContext;
import com.oceanbase.ocp.core.el.meta.ELParser.CompareContext;
import com.oceanbase.ocp.core.el.meta.ELParser.ContextFieldContext;
import com.oceanbase.ocp.core.el.meta.ELParser.CountContext;
import com.oceanbase.ocp.core.el.meta.ELParser.DistinctContext;
import com.oceanbase.ocp.core.el.meta.ELParser.ExprContext;
import com.oceanbase.ocp.core.el.meta.ELParser.ExprListContext;
import com.oceanbase.ocp.core.el.meta.ELParser.FuncCallContext;
import com.oceanbase.ocp.core.el.meta.ELParser.InContext;
import com.oceanbase.ocp.core.el.meta.ELParser.IsEmptyContext;
import com.oceanbase.ocp.core.el.meta.ELParser.IsNullContext;
import com.oceanbase.ocp.core.el.meta.ELParser.MaxContext;
import com.oceanbase.ocp.core.el.meta.ELParser.MemberAccessContext;
import com.oceanbase.ocp.core.el.meta.ELParser.MinContext;
import com.oceanbase.ocp.core.el.meta.ELParser.MulDivModContext;
import com.oceanbase.ocp.core.el.meta.ELParser.NotEmptyContext;
import com.oceanbase.ocp.core.el.meta.ELParser.NotNullContext;
import com.oceanbase.ocp.core.el.meta.ELParser.NumberContext;
import com.oceanbase.ocp.core.el.meta.ELParser.OppositeContext;
import com.oceanbase.ocp.core.el.meta.ELParser.ParenthesesContext;
import com.oceanbase.ocp.core.el.meta.ELParser.PowContext;
import com.oceanbase.ocp.core.el.meta.ELParser.StdContext;
import com.oceanbase.ocp.core.el.meta.ELParser.StringContext;
import com.oceanbase.ocp.core.el.meta.ELParser.SumContext;
import com.oceanbase.ocp.core.el.meta.ELParser.VarContext;
import com.oceanbase.ocp.core.el.util.ObjectUtils;

public class BaseVisitor extends ELBaseVisitor<Object> {

    Map<String, Object> context;

    Map<String, Object> alias;

    public synchronized void addAttribute(String name, Object object) {
        if (context == null) {
            context = new HashMap<>(8);
        }
        if (alias == null) {
            alias = new HashMap<>(8);
        }
        context.put(name, object);
    }

    @Override
    public Object visitStd(StdContext ctx) {
        Object value = visit(ctx.expr());
        if (value.getClass().isArray()) {
            return MathUtils.stdOfDoubleArray(value);
        } else if (value instanceof List) {
            return MathUtils.stdOfDoubleList(((List) value));
        } else if (value instanceof Set) {
            return MathUtils.stdOfDoubleSet(((Set) value));
        } else if (value instanceof Number) {
            return 0.0D;
        } else {
            throw new UnsupportedTypeOfUnaryOperationException(UnaryOperation.STD, value.getClass());
        }
    }

    @Override
    public Object visitMulDivMod(MulDivModContext ctx) {
        double left = Double.parseDouble(visit(ctx.expr(0)).toString());
        double right = Double.parseDouble(visit(ctx.expr(1)).toString());
        if (ctx.MULT() != null) {
            return left * right;
        }
        if (ctx.DIV() != null) {
            if (right == 0) {
                throw new DivideZeroException();
            }
            return left / right;
        }
        return left % right;
    }

    @Override
    public Object visitAvg(AvgContext ctx) {
        Object value = visit(ctx.expr());
        if (value.getClass().isArray()) {
            return Arrays.stream((Object[]) value)
                    .mapToDouble(v -> Double.parseDouble(v.toString()))
                    .summaryStatistics()
                    .getAverage();
        } else if (value instanceof List) {
            return ((List) value).stream()
                    .mapToDouble(v -> Double.parseDouble(v.toString()))
                    .summaryStatistics()
                    .getAverage();
        } else if (value instanceof Set) {
            return ((Set) value).stream()
                    .mapToDouble(v -> Double.parseDouble(v.toString()))
                    .summaryStatistics()
                    .getAverage();
        } else if (value instanceof Number) {
            return value;
        } else {
            throw new UnsupportedTypeOfUnaryOperationException(UnaryOperation.AVG, value.getClass());
        }

    }

    @Override
    public Object visitAddSub(AddSubContext ctx) {
        double left = Double.parseDouble(visit(ctx.expr(0)).toString());
        double right = Double.parseDouble(visit(ctx.expr(1)).toString());
        if (ctx.SUB() != null) {
            return left - right;
        }
        return left + right;
    }

    @Override
    public Object visitVar(VarContext ctx) {

        Object value = visit(ctx.expr());
        if (value.getClass().isArray()) {
            return MathUtils.varOfDoubleArray(value);
        } else if (value instanceof List) {
            return MathUtils.varOfDoubleList(((List) value));
        } else if (value instanceof Set) {
            return MathUtils.varOfDoubleSet(((Set) value));
        } else if (value instanceof Number) {
            return 0.0D;
        } else {
            throw new UnsupportedTypeOfUnaryOperationException(UnaryOperation.VAR, value.getClass());
        }
    }

    @Override
    public Object visitPow(PowContext ctx) {
        Object left = visit(ctx.expr(0));
        Object right = visit(ctx.expr(1));
        if (left instanceof Number && right instanceof Number) {
            return Math.pow((Double) left, (Double) right);
        }
        throw new UnsupportedTypeOfBinaryOperationException(BinaryOperation.POW, left.getClass(), right.getClass());
    }

    @Override
    public Object visitParentheses(ParenthesesContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Object visitIn(InContext ctx) {
        Object left = visit(ctx.expr(0));
        Object right = visit(ctx.expr(1));
        if (left == null || right == null) {
            return false;
        }
        return (tryConvertToList(right)).containsAll(tryConvertToList(left));
    }

    @Override
    public Object visitCompare(CompareContext ctx) {
        Object leftValue = visit(ctx.expr(0));
        Object rightValue = visit(ctx.expr(1));
        if (leftValue == null && rightValue == null) {
            if (ctx.EQUALS() != null) {
                return true;
            }
            return false;
        } else if (leftValue == null || rightValue == null) {
            return false;
        } else {
            if (leftValue instanceof String && rightValue instanceof String) {
                String left = leftValue.toString();
                String right = rightValue.toString();
                if (ctx.EQUALS() != null) {
                    return left.equals(right);
                }
                if (ctx.NOTEQUALS() != null) {
                    return !left.equals(right);
                }
                if (ctx.LT() != null) {
                    return left.compareTo(right) < 0;
                }
                if (ctx.LTEQ() != null) {
                    return left.compareTo(right) <= 0;
                }
                if (ctx.GT() != null) {
                    return left.compareTo(right) > 0;
                }
                if (ctx.GTEQ() != null) {
                    return left.compareTo(right) >= 0;
                }
            } else if (leftValue instanceof Number && rightValue instanceof Number) {
                Double left = Double.parseDouble(leftValue.toString());
                Double right = Double.parseDouble(rightValue.toString());
                if (ctx.EQUALS() != null) {
                    return Math.abs(right - left) <= 1e-6;
                }
                if (ctx.NOTEQUALS() != null) {
                    return Math.abs(right - left) > 1e-6;
                }
                if (ctx.LT() != null) {
                    return left.compareTo(right) < 0;
                }
                if (ctx.LTEQ() != null) {
                    return left.compareTo(right) <= 0;
                }
                if (ctx.GT() != null) {
                    return left.compareTo(right) > 0;
                }
                if (ctx.GTEQ() != null) {
                    return left.compareTo(right) >= 0;
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public Object visitAndOr(AndOrContext ctx) {
        return ctx.OR() != null ? (Boolean) visit(ctx.expr(0)) || (Boolean) visit(ctx.expr(1))
                : (Boolean) visit(ctx.expr(0)) && (Boolean) visit(ctx.expr(1));
    }

    @Override
    public Object visitMemberAccess(MemberAccessContext ctx) {
        Object value = visit(ctx.expr());
        if (value == null) {
            return null;
        }
        String fieldName = ctx.ID().getText();
        if (value instanceof List) {
            return ((List) value).stream().map(o -> getField(o, fieldName)).collect(Collectors.toList());
        } else if (value instanceof Set) {
            return ((Set) value).stream().map(o -> getField(o, fieldName)).collect(Collectors.toList());
        } else if (value.getClass().isArray()) {
            return Arrays.stream(((Object[]) value)).map(o -> getField(o, fieldName)).collect(Collectors.toList());
        } else {
            return getField(value, fieldName);
        }
    }

    private Object getField(Object object, String fieldName) {
        Map<String, Object> objectMap = ObjectUtils.getFieldMap(object);
        if (objectMap.containsKey(fieldName)) {
            return objectMap.get(fieldName);
        }
        throw new NoSuchFieldException(object.getClass(), fieldName);
    }

    @Override
    public Object visitFuncCall(FuncCallContext ctx) {
        ExprContext expr = ctx.expr();
        Object right = visit(expr);
        String funcName = ctx.ID().getText();
        Method[] methods = right.getClass().getMethods();
        Method method = null;
        for (Method m : methods) {
            if (m.getName().equals(funcName)) {
                method = m;
                break;
            }
        }
        Object r = null;
        Object[] args = null;
        try {
            if (ctx.exprList() == null) {
                r = method.invoke(right);
            } else {
                List<ExprContext> exprList = ctx.exprList().expr();
                args = new Object[exprList.size()];
                int i = 0;
                for (ExprContext e : exprList) {
                    args[i++] = visit(e);
                }
                r = method.invoke(right, 1);
            }

        } catch (java.lang.IllegalAccessException | IllegalArgumentException
                | java.lang.reflect.InvocationTargetException e) {
            if (e instanceof java.lang.IllegalAccessException) {
                throw IllegalAccessException.builder().clz(right.getClass()).method(funcName).build();
            } else if (e instanceof IllegalArgumentException) {
                throw IllegalArgumentException.builder().clz(right.getClass()).method(funcName).args(args).build();
            } else {
                throw InvocationTargetException.builder().clz(right.getClass()).method(funcName).args(args)
                        .errorMessage(((ReflectiveOperationException) e).getMessage())
                        .build();
            }
        }
        return r;

    }

    @Override
    public Object visitMax(MaxContext ctx) {
        List list = tryConvertToList(visit(ctx.expr()));
        if (list.size() == 0) {
            return null;
        }
        if (list.get(0) instanceof Number) {
            return list.stream().mapToDouble(a -> Double.parseDouble(a.toString())).max().orElse(0.0D);
        }
        throw UnsupportedTypeOfUnaryOperationException.builder().clz(list.get(0).getClass())
                .operation(UnaryOperation.MAX).build();
    }

    @Override
    public Object visitSum(SumContext ctx) {
        List list = tryConvertToList(visit(ctx.expr()));
        if (list.size() == 0) {
            return null;
        }
        if (list.get(0) instanceof Number) {
            return list.stream().mapToDouble(a -> Double.parseDouble(a.toString())).sum();
        }
        throw UnsupportedTypeOfUnaryOperationException.builder().clz(list.get(0).getClass())
                .operation(UnaryOperation.SUM).build();
    }

    @Override
    public Object visitCount(CountContext ctx) {
        List list = tryConvertToList(visit(ctx.expr()));
        return list.size();
    }

    @Override
    public Object visitMin(MinContext ctx) {
        List list = tryConvertToList(visit(ctx.expr()));
        if (list.size() == 0) {
            return null;
        }
        if (list.get(0) instanceof Number) {
            return list.stream().mapToDouble(a -> Double.parseDouble(a.toString())).min().orElse(0.0D);
        }
        throw UnsupportedTypeOfUnaryOperationException.builder().clz(list.get(0).getClass())
                .operation(UnaryOperation.MIN).build();
    }

    @Override
    public Object visitContextField(ContextFieldContext ctx) {
        if (context.containsKey(ctx.getText().substring(1))) {
            return context.get(ctx.getText().substring(1));
        } else if (alias.containsKey(ctx.getText().substring(1))) {
            return alias.get(ctx.getText().substring(1));
        } else {
            throw new NoSuchContextValueException(ctx.getText());
        }
    }

    @Override
    public Object visitString(StringContext ctx) {
        return ctx.getText().substring(1, ctx.getText().length() - 1);
    }

    @Override
    public Object visitNumber(NumberContext ctx) {
        return Double.valueOf(ctx.getText());
    }

    @Override
    public Object visitBoolean(BooleanContext ctx) {
        return Boolean.parseBoolean(ctx.getText().toLowerCase());
    }

    @Override
    public Object visitExprList(ExprListContext ctx) {
        return super.visitExprList(ctx);
    }

    @Override
    public Object visitDistinct(DistinctContext ctx) {
        Object value = visit(ctx.expr());
        return new HashSet(tryConvertToList(value));
    }

    @Override
    public Object visitIsEmpty(IsEmptyContext ctx) {
        Object value = visit(ctx.expr());
        return value == null || tryConvertToList(value).isEmpty();
    }

    @Override
    public Object visitNotNull(NotNullContext ctx) {
        Object value = visit(ctx.expr());
        return value != null;
    }

    @Override
    public Object visitIsNull(IsNullContext ctx) {
        Object value = visit(ctx.expr());
        return value == null;
    }

    @Override
    public Object visitNotEmpty(NotEmptyContext ctx) {
        Object value = visit(ctx.expr());
        return value != null && !tryConvertToList(value).isEmpty();
    }

    @Override
    public Object visitAlias(AliasContext ctx) {
        if (context.containsKey(ctx.ID().getText()) || alias.containsKey(ctx.ID().getText())) {
            throw new AliasDuplicatedException(ctx.ID().getText());
        }
        Object value = visit(ctx.expr());
        alias.put(ctx.ID().getText(), value);
        return value;
    }

    @Override
    public Object visitOpposite(OppositeContext ctx) {
        Object r = visit(ctx.expr());
        return !Boolean.parseBoolean(r.toString());
    }

    private List tryConvertToList(Object value) {
        if (value == null) {
            return null;
        }
        if (value.getClass().isArray()) {
            return Arrays.asList((Object[]) value);
        } else if (value instanceof List) {
            return (List) value;
        } else if (value instanceof Set) {
            return new ArrayList((Set) value);
        } else {
            return Collections.singletonList(value);
        }
    }
}
