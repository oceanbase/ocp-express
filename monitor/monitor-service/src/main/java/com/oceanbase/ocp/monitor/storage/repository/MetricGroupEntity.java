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

import java.io.Serializable;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.context.i18n.LocaleContextHolder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Entity
@Table(name = "metric_group")
public class MetricGroupEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "class_key")
    private String classKey;

    @Column(name = "key")
    private String key;

    @Getter(AccessLevel.NONE)
    @Column(name = "name")
    private String name;

    @Getter(AccessLevel.NONE)
    @Column(name = "name_en")
    private String nameEn;

    @Getter(AccessLevel.NONE)
    @Column(name = "description")
    private String description;

    @Getter(AccessLevel.NONE)
    @Column(name = "description_en")
    private String descriptionEn;

    @Column(name = "with_label")
    private Boolean withLabel;

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
        return "MetricGroupEntity{" +
                "id=" + id +
                ", classKey='" + classKey + '\'' +
                ", key='" + key + '\'' +
                ", name='" + name + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", description='" + description + '\'' +
                ", descriptionEn='" + descriptionEn + '\'' +
                ", withLabel=" + withLabel +
                '}';
    }

}
