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

public class ConnectFailedException extends OcpException implements WithTarget {

    private ApiErrorTarget target;

    public ConnectFailedException(ErrorCode code) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, code);
    }

    public ConnectFailedException(ErrorCode code, Object... args) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, code, args);
    }

    public ConnectFailedException(ErrorCode code, ApiErrorTarget target, Object... args) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, code, args);
        this.target = target;
    }

    public ConnectFailedException(Throwable cause, ErrorCode code, Object... args) {
        super(cause, HttpStatus.INTERNAL_SERVER_ERROR, code, args);
    }

    @Override
    public ApiErrorTarget buildTarget() {
        return target;
    }
}
