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

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * A wrapper class for REST api return results, including successful results,
 * and trace info.
 *
 * @param <T>
 */
@Getter
@Setter
public class SuccessResponse<T> extends BaseResponse {

    /**
     * result data
     */
    private final T data;

    SuccessResponse(HttpStatus status, T data) {
        setSuccessful(true);
        setStatus(status.value());
        setTimestamp(OffsetDateTime.now());
        this.data = data;
    }

    SuccessResponse(T data) {
        this(HttpStatus.OK, data);
    }

}
