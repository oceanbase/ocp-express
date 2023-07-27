
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

import java.util.List;

import org.apache.commons.collections4.IterableUtils;

import lombok.Getter;
import lombok.Setter;

public class PaginatedResponse<T> extends SuccessResponse<PaginatedResponse.PaginatedData<T>> {

    PaginatedResponse(PaginatedData<T> data) {
        super(data);
    }

    @Getter
    @Setter
    public static class PaginatedData<T> {

        private CustomPage page;

        private List<T> contents;

        PaginatedData(Iterable<T> contents, CustomPage page) {
            this.contents = IterableUtils.toList(contents);
            this.page = page;
            this.page.setNumber(this.page.getNumber() + 1);
        }

    }
}
