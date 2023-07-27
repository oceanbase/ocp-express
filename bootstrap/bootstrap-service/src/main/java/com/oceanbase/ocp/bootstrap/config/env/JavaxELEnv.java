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

package com.oceanbase.ocp.bootstrap.config.env;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.el.ELProcessor;

public class JavaxELEnv implements ELEnv {

    private final ELProcessor elProcessor;

    public JavaxELEnv() {
        elProcessor = new ELProcessor();
        addFunctions(Functions.class);
        addFunctions(OcpFunctions.class);
    }

    private void addFunctions(Class<?> funcClass) {
        for (Method method : funcClass.getMethods()) {
            int modifiers = method.getModifiers();
            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)) {
                elProcessor.getELManager().mapFunction("", method.getName(), method);
            }
        }
    }

    @Override
    public void set(String name, Object o) {
        elProcessor.setValue(name, o);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T eval(String expr) {
        try {
            return (T) elProcessor.eval(expr);
        } catch (Exception e) {
            throw new IllegalStateException("eval expr " + expr + " failed", e);
        }
    }
}
