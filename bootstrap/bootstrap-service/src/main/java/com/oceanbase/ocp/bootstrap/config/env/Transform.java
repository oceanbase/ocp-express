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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.Event.ID;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.parser.Parser;
import org.yaml.snakeyaml.resolver.Resolver;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import com.oceanbase.ocp.bootstrap.core.Keys;

/**
 * !transform { with: {a:list(), b:list()} expr: expanexpand("..", expand("ss",
 * data)) data: ..... }
 */
public class Transform extends TypeDescription {

    private final Yaml yaml;
    private final Resolver resolver = new Resolver();
    private final LoaderOptions loaderOptions = new LoaderOptions();

    public Transform() {
        super(TransformConfig.class, "!transform");
        Yaml yaml = new Yaml();
        yaml.addTypeDescription(new ResourceFile());
        yaml.addTypeDescription(new Const());
        yaml.addTypeDescription(new ELExpr(new JavaxELEnv()));
        this.yaml = yaml;
    }

    public static class TransformConfig {

        public Map<String, String> with;
        public String expr;
        public Object data;
    }

    static class NodeParser implements Parser {

        private final PeekingIterator<Event> peekingIterator;

        public NodeParser(Yaml yaml, Node node) {
            List<Event> events = yaml.serialize(node);
            this.peekingIterator = Iterators.peekingIterator(events.iterator());
        }

        @Override
        public boolean checkEvent(ID choice) {
            Event current = peekingIterator.peek();
            return current != null && current.is(choice);
        }

        @Override
        public Event peekEvent() {
            return peekingIterator.peek();
        }

        @Override
        public Event getEvent() {
            return peekingIterator.next();
        }
    }

    TransformConfig toTransformConfig(MappingNode node) {
        List<NodeTuple> tuples = node.getValue();
        TransformConfig ret = new TransformConfig();
        for (NodeTuple tuple : tuples) {
            String key = ((ScalarNode) tuple.getKeyNode()).getValue();
            switch (key) {
                case Keys.EXPR:
                    ret.expr = ((ScalarNode) tuple.getValueNode()).getValue();
                    break;
                case Keys.WITH:
                    ret.with = toStringMap((MappingNode) tuple.getValueNode());
                    break;
                case Keys.DATA:
                    ret.data = toDataMap(tuple.getValueNode());
                    break;
                default:
                    throw new IllegalArgumentException("bad transform node");
            }
        }
        return ret;
    }

    static Map<String, String> toStringMap(MappingNode node) {
        List<NodeTuple> tuples = node.getValue();
        Map<String, String> ret = new HashMap<>();
        for (NodeTuple tuple : tuples) {
            String key = ((ScalarNode) tuple.getKeyNode()).getValue();
            String value = ((ScalarNode) tuple.getValueNode()).getValue();
            ret.put(key, value);
        }
        return ret;
    }

    Object toDataMap(Node node) {
        NodeParser nodeParser = new NodeParser(yaml, node);
        Composer composer = new Composer(nodeParser, resolver, loaderOptions);
        Constructor constructor = new Constructor(new LoaderOptions());
        constructor.setComposer(composer);
        return constructor.getData();
    }

    @Override
    public Object newInstance(Node node) {
        ELEnv elEnv = new JavaxELEnv();
        if (!(node instanceof MappingNode)) {
            throw new IllegalArgumentException("!transform must be a transform config");
        }
        TransformConfig config = toTransformConfig((MappingNode) node);
        elEnv.set(Keys.DATA, config.data);
        if (config.with != null) {
            config.with.forEach((name, expr) -> {
                try {
                    Object withValue = elEnv.eval(expr);
                    elEnv.set(name, withValue);
                } catch (Exception e) {
                    throw new IllegalStateException("transform eval with " + name + " failed", e);
                }
            });
        }
        Object ret = elEnv.eval(config.expr);
        if (ret == null) {
            throw new IllegalStateException("result of expr is null");
        }
        return ret;
    }

    @Override
    public boolean setProperty(Object targetBean, String propertyName, Object value) throws Exception {
        return true;
    }
}
