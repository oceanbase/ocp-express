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

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

public class DataType {

    private static final Pattern TYPE_PATTERN =
            Pattern.compile("(\\w+)\\s*(?:\\(\\s*(\\d+)(?:\\s*,\\s*(\\d+))?\\s*\\))?(?:\\s*([\\w ]+))?");
    private static final Map<String, Integer> DEFAULT_INT_SIZE = ImmutableMap.of(
            "bigint unsigned", 20,
            "bigint", 20,
            "int unsigned", 10,
            "int", 11,
            "mediumint unsigned", 8,
            "mediumint", 9,
            "smallint unsigned", 5,
            "smallint", 6,
            "tinyint unsigned", 3,
            "tinyint", 4);

    private final String base;
    private final Integer length;
    private final Integer scale;
    private final String extra;

    public DataType(String base, Integer length, Integer scale, String extra) {
        base = base.trim().toLowerCase();
        extra = extra == null ? "" : extra.trim().toLowerCase();
        extra = extra.replaceAll(" +", " ");
        if ("boolean".equals(base) || "bool".equals(base)) {
            base = "tinyint";
            length = 1;
            extra = "";
        }
        if ("integer".equals(base)) {
            base = "int";
        }
        if ("numeric".equals(base)) {
            base = "decimal";
        }
        if (length == null) {
            String k = base + (extra.startsWith("unsigned") ? " unsigned" : "");
            length = DEFAULT_INT_SIZE.get(k);
        }
        if ("double".equals(base) || "float".equals(base)) {
            length = null;
            scale = null;
        }
        if ("timestamp".equals(base) || "datetime".equals(base) || "time".equals(base)) {
            if (length != null && length == 0) {
                length = null;
            }
        }
        if ("decimal".equals(base)) {
            if (scale == null) {
                scale = 0;
            }
        }
        this.base = base;
        this.length = length;
        this.scale = scale;
        this.extra = extra;
    }

    public DataType(String base, Integer length, String extra) {
        this(base, length, null, extra);
    }

    public static DataType fromString(String typeStr) {
        if (typeStr == null) {
            return null;
        }
        Matcher matcher = TYPE_PATTERN.matcher(typeStr.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("invalid type string " + typeStr);
        }
        String base = matcher.group(1).toLowerCase();
        Integer size = null;
        if (matcher.group(2) != null) {
            size = Integer.parseInt(matcher.group(2));
        }
        Integer scale = null;
        if (matcher.group(3) != null) {
            scale = Integer.parseInt(matcher.group(3));
        }
        String extra = null;
        if (matcher.group(4) != null) {
            extra = matcher.group(4).trim().toLowerCase();
        }
        return new DataType(base, size, scale, extra);
    }

    public String getBase() {
        return base;
    }

    public Integer getLength() {
        return length;
    }

    public Integer getScale() {
        return scale;
    }

    public String getExtra() {
        return extra;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(base);
        if (length != null) {
            sb.append('(').append(length);
            if (scale != null) {
                sb.append(',').append(scale);
            }
            sb.append(')');
        }
        if (!extra.isEmpty()) {
            sb.append(' ').append(extra);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataType dataType = (DataType) o;
        return base.equals(dataType.base) && Objects.equals(length, dataType.length)
                && Objects.equals(scale, dataType.scale) && extra.equals(dataType.extra);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, length, scale, extra);
    }

    public boolean canChangeFrom(DataType oldField) {
        return canChangeFrom(oldField, SqlCompatible.OCEANBASE);
    }

    public boolean canChangeFrom(DataType oldField, SqlCompatible sqlCompatible) {
        if (!base.equals(oldField.base)) {
            Integer s1 = DEFAULT_INT_SIZE.get(oldField.base);
            Integer s2 = DEFAULT_INT_SIZE.get(base);
            if (s1 == null || s2 == null) {
                return false;
            }
            if (sqlCompatible.haveCapability(SqlCompatible.Capability.CHANGE_TYPE)) {
                return s2 > s1;
            }
            return false;
        }
        if (extra.startsWith("unsigned") != oldField.extra.startsWith("unsigned")) {
            return "decimal".equals(base);
        }
        if (Objects.equals(length, oldField.length)) {
            return true;
        }
        if ("timestamp".equals(base) || "datetime".equals(base) || "time".equals(base)) {
            if (length == null) {
                return false;
            }
            if (oldField.length == null) {
                return true;
            }
            if (length < oldField.length) {
                return false;
            }
        }
        return true;
    }

}
