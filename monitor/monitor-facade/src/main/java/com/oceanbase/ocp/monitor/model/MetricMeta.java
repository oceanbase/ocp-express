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
package com.oceanbase.ocp.monitor.model;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * monitor meta info
 */
@Validated
@Data
public class MetricMeta {

    @JsonProperty("id")
    private Long id = null;

    @JsonProperty("key")
    private String key = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("unit")
    private String unit = null;

    @JsonProperty("displayByDefault")
    private boolean displayByDefault = true;

    @JsonProperty("interval")
    private Integer interval = null;

    @JsonProperty("isBuiltIn")
    private boolean isBuiltIn = true;

    @JsonProperty("minObVersion")
    private String minObVersion = null;

    @JsonProperty("maxObVersion")
    private String maxObVersion = null;

}
