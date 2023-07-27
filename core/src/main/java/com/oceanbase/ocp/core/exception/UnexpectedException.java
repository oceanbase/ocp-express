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
import com.oceanbase.ocp.core.i18n.ErrorCodes;

/**
 * This is a generic exception that could possibly used by all search methods.
 */
public class UnexpectedException extends OcpException {

    public UnexpectedException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.COMMON_UNEXPECTED);
    }

    public UnexpectedException(Throwable cause) {
        super(cause, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.COMMON_UNEXPECTED);
    }

    public UnexpectedException(ErrorCode code, Object... args) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, code, args);
    }

    public UnexpectedException(Throwable cause, ErrorCode code, Object... args) {
        super(cause, HttpStatus.INTERNAL_SERVER_ERROR, code, args);
    }
}
