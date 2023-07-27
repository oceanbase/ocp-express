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

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import com.oceanbase.ocp.core.i18n.ErrorCode;

import lombok.Getter;

/**
 * An abstract base class for all OCP-specific exceptions.
 */
@Getter
public class OcpException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    // Http status for web clients to handle exceptions
    private final HttpStatus status;

    // OCP-specific error code that can be used for localization
    private final ErrorCode errorCode;

    // Extra arguments for formatting the error message
    private final Object[] args;

    private final String message;

    public OcpException(HttpStatus status, ErrorCode errorCode) {
        this(status, errorCode, null);
    }

    public OcpException(HttpStatus status, ErrorCode errorCode, Object... args) {
        this.status = status;
        this.errorCode = errorCode;
        this.args = args;
        this.message = generateMessage();
    }

    public OcpException(Throwable cause, HttpStatus status, ErrorCode errorCode, Object... args) {
        super(cause);
        this.status = status;
        this.errorCode = errorCode;
        this.args = args;
        this.message = generateMessage();
    }

    private String getArgsStr() {
        if (args != null) {
            return StringUtils.join(args, ",");
        }
        return "";
    }

    @Override
    public String getMessage() {
        return message;
    }

    private String generateMessage() {
        return "[OCP " + this.getClass().getSimpleName() + "]: status=" + status + ", errorCode=" + errorCode
                + ", args=" + getArgsStr();
    }

}
