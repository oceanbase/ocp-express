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

package com.oceanbase.ocp.perf.sql.model;

import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SqlAuditStatSummary extends SqlAuditStatBase {

    private String sqlId;

    private String server;

    private String serverIp;

    private Integer serverPort;

    private String dbName;

    private String userName;

    private String sqlType;

    private String sqlTextShort;

    private Boolean isComplete;

    private boolean inner;

    private String waitEvent;

    private List<CustomColumn> customColumns;

    @JsonIgnore
    private Long startTimeUs;

    @JsonIgnore
    private Long endTimeUs;

    @JsonIgnore
    private Long obUserId;

    @JsonIgnore
    private BigInteger obDbId;

}
