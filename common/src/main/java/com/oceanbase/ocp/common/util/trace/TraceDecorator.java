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

package com.oceanbase.ocp.common.util.trace;

import java.util.Map;
import java.util.concurrent.Callable;

public class TraceDecorator {

    public Runnable decorate(Runnable runnable) {
        // Right now we are inside the Web thread context !
        // (Grab the current thread context data)
        Map<String, String> context = TraceUtils.getTraceContext();
        return () -> {
            try {
                // Right now we are inside @Async thread context !
                // (Restore the Web thread context data)
                TraceUtils.span(context);
                runnable.run();
            } finally {
                TraceUtils.clear();
            }
        };
    }

    public <V> Callable<V> decorate(Callable<V> callable) {
        Map<String, String> context = TraceUtils.getTraceContext();
        return () -> {
            try {
                TraceUtils.span(context);
                return callable.call();
            } finally {
                TraceUtils.clear();
            }
        };
    }
}
