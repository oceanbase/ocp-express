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

/**
 * Response object builder.
 */
public class ResponseBuilder {

    /**
     * Empty response.
     */
    public static NoDataResponse noData() {
        return new NoDataResponse();
    }

    /**
     * Success response with single model.
     */
    public static <T> SuccessResponse<T> single(T data) {
        return new SuccessResponse<>(data);
    }

    /**
     * Success response with iterable model (without pagination).
     */
    public static <T> IterableResponse<T> iterable(Iterable<T> iterable) {
        return new IterableResponse<T>(iterable);
    }

    /**
     * Success response with paged model.
     */
    public static <T> PaginatedResponse<T> paginated(Page<T> page) {
        CustomPage customPage = CustomPage.from(page);
        PaginatedResponse.PaginatedData<T> data = new PaginatedResponse.PaginatedData<>(page.getContent(), customPage);
        return new PaginatedResponse<>(data);
    }

}
