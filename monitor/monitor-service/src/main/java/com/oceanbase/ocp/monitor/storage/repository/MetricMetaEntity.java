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

package com.oceanbase.ocp.monitor.storage.repository;

import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.context.i18n.LocaleContextHolder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Entity
@Table(name = "metric_meta")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetricMetaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "key", nullable = false)
    private String key;

    @Getter(AccessLevel.NONE)
    @Column(name = "name")
    private String name;

    @Getter
    @Column(name = "group_key")
    private String groupKey;

    @Getter
    @Column(name = "class_key")
    private String classKey;

    @Getter(AccessLevel.NONE)
    @Column(name = "name_en")
    private String nameEn;

    @Getter(AccessLevel.NONE)
    @Column(name = "description")
    private String description;

    @Getter(AccessLevel.NONE)
    @Column(name = "description_en")
    private String descriptionEn;

    @Column(name = "unit")
    private String unit;

    @Column(name = "display_by_default")
    private boolean displayByDefault;

    @Getter(AccessLevel.NONE)
    @Column(name = "is_built_in")
    private Boolean isBuiltIn;

    @Column(name = "min_ob_version")
    private String minObVersion;

    @Column(name = "max_ob_version")
    private String maxObVersion;

    public String getName() {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale != null && locale.getLanguage().contains("en")) {
            return this.nameEn;
        }
        return this.name;
    }

    public String getNameEn() {
        return this.nameEn;
    }


    public String getDescription() {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale != null && locale.getLanguage().contains("en")) {
            return this.descriptionEn;
        }
        return this.description;
    }

    public String getDescriptionEn() {
        return this.descriptionEn;
    }

    @Override
    public String toString() {
        return "MetricMetaEntity{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", groupKey='" + groupKey + '\'' +
                ", classKey='" + classKey + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", description='" + description + '\'' +
                ", descriptionEn='" + descriptionEn + '\'' +
                ", unit='" + unit + '\'' +
                ", displayByDefault=" + displayByDefault +
                ", isBuiltIn=" + isBuiltIn +
                ", minObVersion='" + minObVersion + '\'' +
                ", maxObVersion='" + maxObVersion + '\'' +
                '}';
    }

}
