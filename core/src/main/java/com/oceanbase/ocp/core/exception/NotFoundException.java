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

package com.oceanbase.ocp.core.exception;

import org.springframework.http.HttpStatus;

import com.oceanbase.ocp.core.i18n.ErrorCode;
import com.oceanbase.ocp.core.response.error.ApiErrorTarget;

/**
 * This is a generic exception that could possibly used by all search methods.
 */
public class NotFoundException extends OcpException implements WithTarget {

    private static final long serialVersionUID = -716502918494756797L;

    private ApiErrorTarget target;

    public NotFoundException(ErrorCode code) {
        super(HttpStatus.NOT_FOUND, code);
    }

    public NotFoundException(ErrorCode code, Object... args) {
        super(HttpStatus.NOT_FOUND, code, args);
    }

    public NotFoundException(ErrorCode code, ApiErrorTarget target, Object... args) {
        super(HttpStatus.NOT_FOUND, code, args);
        this.target = target;
    }

    public NotFoundException(Throwable cause, ErrorCode code, Object... args) {
        super(cause, HttpStatus.NOT_FOUND, code, args);
    }

    @Override
    public ApiErrorTarget buildTarget() {
        return target;
    }
}
