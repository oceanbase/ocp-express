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

/**
 * This is a generic exception that could possibly used by all search methods.
 */
public class AuthenticationException extends OcpException {

    public AuthenticationException(ErrorCode code) {
        super(HttpStatus.UNAUTHORIZED, code);
    }

    public AuthenticationException(ErrorCode code, Object... args) {
        super(HttpStatus.UNAUTHORIZED, code, args);
    }

    public AuthenticationException(Throwable cause, ErrorCode code, Object... args) {
        super(cause, HttpStatus.UNAUTHORIZED, code, args);
    }
}
