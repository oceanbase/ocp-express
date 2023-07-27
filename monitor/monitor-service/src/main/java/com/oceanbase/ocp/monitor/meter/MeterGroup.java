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

package com.oceanbase.ocp.monitor.meter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;

public abstract class MeterGroup<TMeter extends Meter> {

    protected final MeterRegistry registry;
    protected final String name;
    protected final String description;
    protected final String baseUnits;
    protected final List<String> labelNames;

    MeterGroup(MeterRegistry registry, String name, String description, String baseUnits, String[] labelNames) {
        this.registry = registry;
        this.name = name;
        this.description = description;
        this.baseUnits = baseUnits;
        this.labelNames = Arrays.asList(labelNames);
    }

    private String[] getTags(String[] labelValues) {
        String[] tags = new String[labelNames.size() * 2];
        for (int i = 0; i < labelNames.size(); i++) {
            tags[i * 2] = labelNames.get(i);
            tags[i * 2 + 1] = labelValues[i];
        }
        return tags;
    }

    protected abstract TMeter construct(String[] tags);

    public TMeter of(Object... labelValues) {
        Validate.notNull(labelValues, "Label values require non-null");
        Validate.isTrue(labelValues.length == labelNames.size(),
                String.format("Expect '%d' label values", labelNames.size()));
        String[] stringValues = new String[labelValues.length];
        for (int i = 0; i < labelValues.length; i++) {
            Validate.notNull(labelValues[i], "Label value require non-null");
            stringValues[i] = labelValues[i].toString();
        }
        Key key = new Key(stringValues);
        return map.computeIfAbsent(key, k -> construct(getTags(stringValues)));
    }

    private final ConcurrentMap<Key, TMeter> map = new ConcurrentHashMap<>();

    private static final class Key {

        final List<String> labelValues;

        Key(String[] labelValues) {
            this.labelValues = Arrays.asList(labelValues);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key) o;
            return Objects.equals(labelValues, key.labelValues);
        }

        @Override
        public int hashCode() {
            return Objects.hash(labelValues);
        }
    }

    public static abstract class Builder<TMeter extends Meter, TGroup extends MeterGroup<TMeter>, TBuilder extends Builder<TMeter, TGroup, TBuilder>> {

        protected final String name;
        protected String description;
        protected String baseUnits;
        protected String[] labelNames;

        Builder(String name) {
            Validate.isTrue(StringUtils.isNotBlank(name), "Metric name require non-blank");
            this.name = name;
        }

        public TBuilder description(String description) {
            this.description = description;
            return (TBuilder) this;
        }

        public TBuilder baseUnits(String baseUnits) {
            this.baseUnits = baseUnits;
            return (TBuilder) this;
        }

        public TBuilder labelNames(String... labelNames) {
            Validate.isTrue(labelNames != null && labelNames.length > 0, "Group metric require label names not-empty");
            for (String l : labelNames) {
                Validate.isTrue(StringUtils.isNotBlank(l), "Metric label name require not-blank");
            }
            this.labelNames = labelNames;
            return (TBuilder) this;
        }

        public abstract TGroup build(MeterRegistry registry);
    }
}
