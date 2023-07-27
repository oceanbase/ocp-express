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

import com.oceanbase.ocp.task.constants.SplitMethod;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Node {

    private final Subtask subtask;
    private final Set<Node> upstreams;
    private final Set<Node> downstreams;
    private final String splitKey;
    private SplitMethod splitMethod;

    public Node(Subtask subtask) {
        this.subtask = subtask;
        this.upstreams = new HashSet<>();
        this.downstreams = new HashSet<>();
        this.splitKey = "";
    }

    public Node(Subtask subtask, String splitKey) {
        this.subtask = subtask;
        this.upstreams = new HashSet<>();
        this.downstreams = new HashSet<>();
        this.splitKey = splitKey;
        this.splitMethod = SplitMethod.PARALLEL;
    }

    public Node(Subtask subtask, String splitKey, SplitMethod splitMethod) {
        this.subtask = subtask;
        this.upstreams = new HashSet<>();
        this.downstreams = new HashSet<>();
        this.splitKey = splitKey;
        this.splitMethod = splitMethod;
    }

    public void addDownstream(Node n) {
        this.downstreams.add(n);
        n.upstreams.add(this);
    }

    public Set<Node> getUpstreams() {
        return this.upstreams;
    }

    public Set<Node> getDownstreams() {
        return this.downstreams;
    }

    public String getSplitKey() {
        return this.splitKey;
    }

    public SplitMethod getSplitMethod() {
        return this.splitMethod;
    }

    public Subtask getSubtask() {
        return this.subtask;
    }

    public boolean findDownstreams(Node n) {
        boolean exists = false;
        if (this.downstreams.contains(n)) {
            return true;
        } else {
            for (Node nd : this.downstreams) {
                if (nd.findDownstreams(n)) {
                    exists = true;
                    break;
                }
            }
        }
        return exists;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Node)) {
            return false;
        }
        Node s = (Node) obj;
        return subtask == s.getSubtask() &&
                splitMethod == s.getSplitMethod() &&
                splitKey.equals(s.getSplitKey());
    }
}
