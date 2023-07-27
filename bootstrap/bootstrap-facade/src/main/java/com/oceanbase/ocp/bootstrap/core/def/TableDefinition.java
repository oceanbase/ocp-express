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

package com.oceanbase.ocp.bootstrap.core.def;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.oceanbase.ocp.bootstrap.core.Keys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class TableDefinition {

    @Data
    public static abstract class TableMember {

        protected String name;
        protected boolean isDrop = false;
        protected String renamedFrom;
    }

    @Data
    public static class Field extends TableMember {

        private DataType type;
        private boolean nullable = true;
        private Object defaultValue;
        private Object onUpdate;
        private boolean autoIncrement = false;
        private String comment;

        public static Field fromConfig(String name, Map<String, Object> items) {
            Field ret = new Field();
            ret.name = name;
            if (items.get(Keys.TYPE) != null) {
                ret.type = DataType.fromString((String) items.get(Keys.TYPE));
            }
            ret.comment = (String) items.get(Keys.COMMENT);
            ret.nullable = (Boolean) items.getOrDefault(Keys.NULLABLE, true);
            ret.autoIncrement = (Boolean) items.getOrDefault(Keys.AUTO_INCREMENT, false);
            ret.defaultValue = items.getOrDefault(Keys.DEFAULT_VALUE, null);
            ret.onUpdate = items.getOrDefault(Keys.ON_UPDATE, null);
            ret.isDrop = (Boolean) items.getOrDefault(Keys.DROP, false);
            ret.renamedFrom = (String) items.get(Keys.RENAMED_FROM);
            return ret;
        }

        public Map<String, Object> toConfigItems() {
            Map<String, Object> ret = new LinkedHashMap<>();
            if (isDrop) {
                return Collections.singletonMap(Keys.DROP, true);
            }
            ret.put(Keys.TYPE, type.toString());
            ret.put(Keys.NULLABLE, nullable);
            if (defaultValue != null && !Const.NULL.equals(defaultValue)) {
                ret.put(Keys.DEFAULT_VALUE, defaultValue);
            }
            if (onUpdate != null && !Const.NULL.equals(onUpdate)) {
                ret.put(Keys.ON_UPDATE, onUpdate);
            }
            if (autoIncrement) {
                ret.put(Keys.AUTO_INCREMENT, true);
            }
            if (comment != null) {
                ret.put(Keys.COMMENT, comment);
            }
            if (renamedFrom != null && !renamedFrom.isEmpty()) {
                ret.put(Keys.RENAMED_FROM, renamedFrom);
            }
            return ret;
        }

    }

    @Data
    public static class Index extends TableMember {

        private List<String> fields = new ArrayList<>();
        private boolean unique = false;
        private boolean local = false;
        private boolean isDelayed = false;

        @SuppressWarnings("unchecked")
        public static Index fromConfig(String name, Map<String, Object> items) {
            Index ret = new Index();
            ret.name = name;
            ret.fields = (List<String>) items.get(Keys.FIELDS);
            ret.unique = (Boolean) items.getOrDefault(Keys.UNIQUE, false);
            ret.local = (Boolean) items.getOrDefault(Keys.LOCAL, false);
            ret.isDrop = (Boolean) items.getOrDefault(Keys.DROP, false);
            ret.isDelayed = (Boolean) items.getOrDefault(Keys.DELAY, false);
            return ret;
        }

        public Map<String, Object> toConfigItems() {
            if (isDrop) {
                return Collections.singletonMap(Keys.DROP, true);
            }
            Map<String, Object> ret = new HashMap<>();
            ret.put(Keys.FIELDS, fields);
            ret.put(Keys.UNIQUE, unique);
            ret.put(Keys.LOCAL, local);
            if (isDelayed) {
                ret.put(Keys.DELAY, true);
            }
            return ret;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            Index index = (Index) o;
            return unique == index.unique && local == index.local && Objects.equals(fields, index.fields);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), fields, unique, local);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Primary {

        private List<String> fields = new ArrayList<>();

        @SuppressWarnings("unchecked")
        public static Primary fromConfig(Map<String, Object> items) {
            if (items == null) {
                return null;
            }
            List<String> fields = (List<String>) items.get(Keys.FIELDS);
            return new Primary(fields);
        }

        public Map<String, Object> toConfigItems() {
            return Collections.singletonMap(Keys.FIELDS, fields);
        }
    }

    @Data
    public static class Partition {

        private String type;
        private List<String> fields;
        private List<RangeElement> rangeElements;
        private String byExpr;
        private Integer hashPartitionCount;
        private Partition subPartition;

        @Data
        public static class RangeElement {

            private String name;
            private Object value;

            public static RangeElement fromConfig(Map<String, Object> items) {
                if (items.size() != 1) {
                    throw new IllegalArgumentException("invalid RangeElement");
                }
                Entry<String, Object> entry = items.entrySet().stream().findFirst().get();
                RangeElement ret = new RangeElement();
                ret.name = entry.getKey();
                ret.value = entry.getValue();
                return ret;
            }

            public Map<String, Object> toConfigItems() {
                return Collections.singletonMap(name, value);
            }
        }

        public static Partition fromConfig(Map<String, Object> items) {
            if (items == null) {
                return null;
            }
            String type = ((String) items.get(Keys.TYPE)).toUpperCase();
            Partition ret = new Partition();
            ret.type = type;
            switch (type) {
                case "RANGE":
                    List<Map<String, Object>> rangePartitions =
                            (List<Map<String, Object>>) items.get(Keys.RANGE_PARTITIONS);
                    ret.rangeElements = new ArrayList<>();
                    if (rangePartitions != null) {
                        for (Map<String, Object> elementConfig : rangePartitions) {
                            ret.rangeElements.add(RangeElement.fromConfig(elementConfig));
                        }
                    }
                    break;
                case "HASH":
                    ret.hashPartitionCount = ((Number) items.get(Keys.HASH_PARTITION_COUNT)).intValue();
                    break;
                default:
            }
            ret.fields = ((List<String>) items.get(Keys.FIELDS));
            ret.byExpr = (String) items.get(Keys.BY_EXPR);
            ret.subPartition = Partition.fromConfig((Map<String, Object>) items.get(Keys.SUBPARTITION));
            return ret;
        }

        public Map<String, Object> toConfigItems() {
            Map<String, Object> ret = new LinkedHashMap<>();
            ret.put(Keys.TYPE, type);
            if (fields != null) {
                ret.put(Keys.FIELDS, fields);
            }
            if (rangeElements != null) {
                rangeElements.stream()
                        .filter(p -> {
                            if (p.value instanceof Number) {
                                return "DUMMY".equalsIgnoreCase(p.name) && ((Number) p.value).longValue() == 0;
                            } else {
                                return false;
                            }
                        })
                        .findFirst()
                        .ifPresent(rangeElement -> {
                            ret.put(Keys.RANGE_PARTITIONS,
                                    Collections.singletonList(rangeElement.toConfigItems()));
                        });
            }
            if (hashPartitionCount != null) {
                ret.put(Keys.HASH_PARTITION_COUNT, hashPartitionCount);
            }
            if (byExpr != null) {
                ret.put(Keys.BY_EXPR, byExpr);
            }
            if (subPartition != null) {
                ret.put(Keys.SUBPARTITION, subPartition.toConfigItems());
            }
            return ret;
        }
    }

    private String name;
    private List<Field> fields = new ArrayList<>();
    private Primary primaryKey;
    private List<Index> indexes = new ArrayList<>();
    private Long autoIncrement;
    private String defaultCharset;
    private String comment;
    private Partition partition;
    private boolean isDrop = false;
    private String renamedFrom;
    private String since;
    private String createTableSql;

    @SuppressWarnings("unchecked")
    public static TableDefinition fromConfig(String name, Map<String, Object> items) {
        TableDefinition ret = new TableDefinition();
        ret.name = name;
        Map<String, Map<String, Object>> fields = (Map<String, Map<String, Object>>) items.get(Keys.FIELDS);
        if (fields != null && !fields.isEmpty()) {
            fields.forEach((fieldName, map) -> {
                ret.fields.add(Field.fromConfig(fieldName, map));
            });
        }
        Map<String, Map<String, Object>> indexes = (Map<String, Map<String, Object>>) items.get(Keys.INDEXES);
        if (indexes != null && !indexes.isEmpty()) {
            indexes.forEach((indexName, map) -> {
                ret.indexes.add(Index.fromConfig(indexName, map));
            });
        }

        ret.primaryKey = Primary.fromConfig((Map<String, Object>) items.get(Keys.PRIMARY_KEY));
        ret.partition = Partition.fromConfig((Map<String, Object>) items.get(Keys.PARTITION));

        if (items.get(Keys.AUTO_INCREMENT) != null) {
            ret.autoIncrement = ((Number) items.get(Keys.AUTO_INCREMENT)).longValue();
        }
        ret.comment = (String) items.get(Keys.COMMENT);
        ret.defaultCharset = (String) items.get(Keys.DEFAULT_CHARSET);
        ret.isDrop = (Boolean) items.getOrDefault(Keys.DROP, false);
        ret.renamedFrom = (String) items.get(Keys.RENAMED_FROM);
        ret.since = (String) items.get(Keys.SINCE);
        return ret;
    }

    public Map<String, Object> toConfigItems() {
        if (isDrop) {
            return Collections.singletonMap(Keys.DROP, true);
        }
        Map<String, Object> ret = new LinkedHashMap<>();
        if (this.fields != null) {
            Map<String, Object> fields = new LinkedHashMap<>();
            for (Field field : this.fields) {
                fields.put(field.getName(), field.toConfigItems());
            }
            ret.put(Keys.FIELDS, fields);
        }
        if (this.indexes != null) {
            Map<String, Object> indexes = new LinkedHashMap<>();
            for (Index index : this.indexes) {
                indexes.put(index.getName(), index.toConfigItems());
            }
            ret.put(Keys.INDEXES, indexes);
        }
        if (this.primaryKey != null) {
            ret.put(Keys.PRIMARY_KEY, this.primaryKey.toConfigItems());
        }
        if (this.partition != null) {
            ret.put(Keys.PARTITION, this.partition.toConfigItems());
        }
        if (comment != null) {
            ret.put(Keys.COMMENT, comment);
        }
        if (autoIncrement != null) {
            ret.put(Keys.AUTO_INCREMENT, autoIncrement);
        }
        if (since != null) {
            ret.put(Keys.SINCE, since);
        }
        if (renamedFrom != null && !renamedFrom.isEmpty()) {
            ret.put(Keys.RENAMED_FROM, renamedFrom);
        }
        ret.put(Keys.DEFAULT_CHARSET, defaultCharset);
        return ret;
    }

    public Field getField(String name) {
        return fields.stream()
                .filter(field -> name.equals(field.getName()))
                .findFirst()
                .orElse(null);
    }

    public Index getIndex(String name) {
        return indexes.stream()
                .filter(index -> name.equals(index.getName()))
                .findFirst()
                .orElse(null);
    }
}
