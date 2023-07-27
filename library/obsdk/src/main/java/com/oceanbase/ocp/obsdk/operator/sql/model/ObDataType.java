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

package com.oceanbase.ocp.obsdk.operator.sql.model;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

@Getter
public enum ObDataType {

    NULL(0, Arrays.asList("null"), false, false),
    TINYINT(1, Arrays.asList("tinyint"), true, false),
    SMALLINT(2, Arrays.asList("smallint"), true, false),
    MEDIUMINT(3, Arrays.asList("mediumint"), true, false),
    INT(4, Arrays.asList("int"), true, false),
    BIGINT(5, Arrays.asList("bigint"), true, false),
    TINYINT_UNSIGNED(6, Arrays.asList("tinyint"), true, false),
    SMALLINT_UNSIGNED(7, Arrays.asList("smallint"), true, false),
    MEDIUMINT_UNSIGNED(8, Arrays.asList("mediumint"), true, false),
    INT_UNSIGNED(9, Arrays.asList("int"), true, false),
    BIGINT_UNSIGNED(10, Arrays.asList("bigint"), true, false),
    FLOAT(11, Arrays.asList("float", "BINARY_FLOAT"), true, false),
    DOUBLE(12, Arrays.asList("double", "BINARY_DOUBLE"), true, false),
    FLOAT_UNSIGNED(13, Arrays.asList("float"), true, false),
    DOUBLE_UNSIGNED(14, Arrays.asList("double"), true, false),
    DECIMAL(15, Arrays.asList("decimal", "number"), true, false),
    DECIMAL_UNSIGNED(16, Arrays.asList("decimal"), true, false),
    DATETIME(17, Arrays.asList("datetime", "DATE"), false, true),
    TIMESTAMP(18, Arrays.asList("timestamp"), false, true),
    DATE(19, Arrays.asList("date"), false, true),
    TIME(20, Arrays.asList("time"), false, true),
    YEAR(21, Arrays.asList("year"), false, true),
    VARCHAR(22, Arrays.asList("varchar", "varchar2", "varbinary"), false, false),
    CHAR(23, Arrays.asList("char", "binary"), false, false),
    HEX_STRING(24, Arrays.asList(""), false, false),
    EXT(25, Arrays.asList("ext"), false, false),
    UNKNOWN(26, Arrays.asList("unknown"), false, false),
    TINYTEXT(27, Arrays.asList("tinytext", "tinyblob"), false, false),
    TEXT(28, Arrays.asList("text", "blob"), false, false),
    MEDIUMTEXT(29, Arrays.asList("mediumtext", "mediumblob"), false, false),
    LONGTEXT(30, Arrays.asList("longtext", "longblob"), false, false),
    BIT(31, Arrays.asList("bit"), false, false),
    ENUM(32, Arrays.asList("enum"), false, false),
    SET(33, Arrays.asList("set"), false, false),
    ENUM_INNER(34, Arrays.asList("ENUM_INNER"), false, false),
    SET_INNER(35, Arrays.asList("SET_INNER"), false, false),
    TIMESTAMP_WITH_TIME_ZONE(36, Arrays.asList("TIMESTAMP(.*) WITH TIME ZONE"), false, true),
    TIMESTAMP_WITH_LOCAL_TIME_ZONE(37, Arrays.asList("TIMESTAMP(.*) WITH LOCAL TIME ZONE"), false, true),
    TIMESTAMP_2(38, Arrays.asList("timestamp(*?)"), false, true),
    RAW(39, Arrays.asList("RAW"), false, false),
    INTERVAL_YEAR_TO_MONTH(40, Arrays.asList("INTERVAL YEAR(2) TO MONTH"), false, true),
    INTERVAL_DAY_TO_SECOND(41, Arrays.asList("INTERVAL DAY(2) TO SECOND(6)"), false, true),
    NUMBER_FLOAT(42, Arrays.asList("float"), true, false),
    NVARCHAR2(43, Arrays.asList("NVARCHAR2"), false, false),
    NCHAR(44, Arrays.asList("NCHAR"), false, false),
    ROWID(45, Arrays.asList("UROWID"), false, false);

    private int value;
    private List<String> names;
    public boolean numeric;
    public boolean typeOfTime;

    ObDataType(int value, List<String> names, boolean numeric, boolean typeOfTime) {
        this.value = value;
        this.numeric = numeric;
        this.names = names;
        this.typeOfTime = typeOfTime;
    }

    public static ObDataType of(Integer dataType) {
        if (dataType == null) {
            return UNKNOWN;
        }
        for (ObDataType type : ObDataType.values()) {
            if (type.value == dataType) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public static ObDataType of(String name) {
        if (name == null) {
            return UNKNOWN;
        }
        for (ObDataType type : ObDataType.values()) {
            if (type.names.contains(name)) {
                return type;
            }
        }
        return UNKNOWN;
    }

}
