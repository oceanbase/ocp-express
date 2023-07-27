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

package com.oceanbase.ocp.task.hook;

import java.util.function.Consumer;

public class TaskHook<T> implements Comparable<TaskHook<T>> {

    private final Integer order;
    private final Consumer<T> consumer;

    public TaskHook(Integer order, Consumer<T> consumer) {
        this.order = order;
        this.consumer = consumer;
    }

    public Integer getOrder() {
        return this.order;
    }

    public Consumer<T> getConsumer() {
        return this.consumer;
    }

    @Override
    public int compareTo(TaskHook<T> h) {
        return this.getOrder().compareTo(h.getOrder());
    }
}
