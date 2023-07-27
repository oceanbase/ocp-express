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
package com.oceanbase.ocp.core.response;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Customized page object.
 */
@Data
public class CustomPage {

    /**
     * Total elements count.
     */
    @JsonProperty("totalElements")
    private Long totalElements = null;

    /**
     * Total pages count.
     */
    @JsonProperty("totalPages")
    private Integer totalPages = null;

    /**
     * Current page number.
     */
    @JsonProperty("number")
    private Integer number = null;

    /**
     * Size of current page.
     */
    @JsonProperty("size")
    private Integer size = null;

    public static CustomPage from(Page<?> page) {
        CustomPage customPage = new CustomPage();
        customPage.setTotalElements(page.getTotalElements());
        customPage.setTotalPages(page.getTotalPages());
        customPage.setNumber(page.getNumber());
        customPage.setSize(page.getSize());
        return customPage;
    }

    public static CustomPage empty() {
        CustomPage customPage = new CustomPage();
        customPage.setTotalElements(0L);
        customPage.setTotalPages(0);
        customPage.setNumber(0);
        customPage.setSize(0);
        return customPage;
    }

}
