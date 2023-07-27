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

import java.util.HashSet;
import java.util.Set;

import com.oceanbase.ocp.core.exception.UnexpectedException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class Template {

    private String name;
    private Set<Node> nodes;
    private boolean prohibitRollback = false;

    public Template() {
        this.name = "";
        this.nodes = new HashSet<>();
    }

    public Template(String name) {
        this.name = name;
        this.nodes = new HashSet<>();
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }

    public void addEdge(Node fromNode, Node toNode) {
        fromNode.addDownstream(toNode);
        if (toNode.findDownstreams(fromNode)) {
            throw new UnexpectedException(ErrorCodes.TASK_CYCLE_EXISTS);
        }
        this.nodes.add(fromNode);
        this.nodes.add(toNode);
    }

    public boolean isProhibitRollback() {
        return prohibitRollback;
    }

    public void setProhibitRollback(boolean prohibitRollback) {
        this.prohibitRollback = prohibitRollback;
    }

}
