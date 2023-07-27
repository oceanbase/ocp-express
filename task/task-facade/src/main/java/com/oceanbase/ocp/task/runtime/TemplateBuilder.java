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

package com.oceanbase.ocp.task.runtime;

import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.constants.SplitMethod;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TemplateBuilder {

    private Template template;
    private Node lastNode;

    public TemplateBuilder() {
        this.template = new Template();
    }

    public TemplateBuilder addEdge(Node fromNode, Node toNode) {
        this.template.addEdge(fromNode, toNode);
        this.lastNode = toNode;
        return this;
    }

    public TemplateBuilder addNode(Subtask subtask) {
        Node node = new Node(subtask);
        return addNode(node);
    }

    public TemplateBuilder addNode(Node node) {
        this.template.addNode(node);
        this.lastNode = node;
        return this;
    }

    public TemplateBuilder andThen(Subtask subtask) {
        Node node = new Node(subtask);
        return andThen(node);
    }

    public TemplateBuilder andThen(Subtask subtask, String splitKey) {
        Node node = new Node(subtask, splitKey);
        return andThen(node);
    }

    public TemplateBuilder andThen(Subtask subtask, ContextKey splitKey) {
        return andThen(subtask, splitKey.getValue());
    }

    public TemplateBuilder andThen(Subtask subtask, String splitKey, SplitMethod splitMethod) {
        Node node = new Node(subtask, splitKey, splitMethod);
        return andThen(node);
    }

    public TemplateBuilder andThen(Subtask subtask, ContextKey splitKey, SplitMethod splitMethod) {
        return andThen(subtask, splitKey.getValue(), splitMethod);
    }

    public TemplateBuilder andThen(Node node) {
        if (this.lastNode == null) {
            this.addNode(node);
        } else {
            this.addEdge(this.lastNode, node);
        }
        return this;
    }

    public TemplateBuilder name(String name) {
        this.template.setName(name);
        return this;
    }

    public TemplateBuilder prohibitRollback(boolean prohibitRollback) {
        this.template.setProhibitRollback(prohibitRollback);
        return this;
    }

    public Template build() {
        return this.template;
    }
}
