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

import java.util.List;
import java.util.Locale;

import javax.validation.Valid;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import lombok.Data;

/**
 * meta info of metric meta group
 */
@Validated
@Data
public class MetricClass {

    @JsonProperty("id")
    private Long id = null;

    @JsonProperty("key")
    private String key = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("name_en")
    private String nameEn = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("description_en")
    private String descriptionEn = null;

    @JsonProperty("scope")
    private MetricScope scope = null;

    @JsonProperty("type")
    private MetricType type = null;

    @JsonProperty("metricGroups")
    @Valid
    private List<MetricGroup> metricGroups = null;

    public String getName() {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale.getLanguage().contains("en")) {
            return this.nameEn;
        }
        return this.name;
    }

    public String getDescription() {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale.getLanguage().contains("en")) {
            return this.descriptionEn;
        }
        return this.description;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("nameEn", name)
                .add("scope", scope)
                .add("type", type)
                .toString();
    }
}
