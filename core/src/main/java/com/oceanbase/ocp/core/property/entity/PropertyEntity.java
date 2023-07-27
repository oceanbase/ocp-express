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
package com.oceanbase.ocp.core.property.entity;

import java.time.OffsetDateTime;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.context.i18n.LocaleContextHolder;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
@Entity
@Table(name = "config_properties")
public class PropertyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "application", length = 128, nullable = false)
    private String application;

    @Column(name = "profile", length = 128, nullable = false)
    private String profile;

    @Column(name = "label", length = 128, nullable = false)
    private String label;

    @Column(name = "value", length = 1024)
    private String value;

    @Column(name = "default_value", length = 1024)
    private String defaultValue;

    @Column(name = "need_restart", nullable = false)
    private Boolean needRestart;

    @Column(name = "fatal", nullable = false)
    private Boolean fatal;

    @Column(name = "visible_level", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private VisibleLevel visibleLevel;

    @Getter(AccessLevel.NONE)
    @Column(name = "description")
    private String description;

    @Getter(AccessLevel.NONE)
    @Column(name = "description_en")
    private String descriptionEn;

    @Column(name = "create_time", columnDefinition = "DATETIME", insertable = false, updatable = false)
    private OffsetDateTime createTime;

    @Column(name = "update_time", columnDefinition = "DATETIME", insertable = false, updatable = false)
    private OffsetDateTime updateTime;

    public String getDescription() {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale.getLanguage().contains("en")) {
            return this.descriptionEn;
        }
        return this.description;
    }

    public boolean needRestart() {
        return this.getNeedRestart() != null && this.getNeedRestart();
    }

}
