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

package com.oceanbase.ocp.obsdk.exception;

public class OceanBaseException extends RuntimeException {

    private static final long serialVersionUID = -5947131650077637842L;

    private String SQLState;

    private int code;

    private String debugMessage;

    public OceanBaseException(String message) {
        super(message);
    }

    public OceanBaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public OceanBaseException(String message, Throwable cause, int code, String SQLState, String debugMessage) {
        super(message, cause);
        this.code = code;
        this.SQLState = SQLState;
        this.debugMessage = debugMessage;
    }

    public String getSQLState() {
        return this.SQLState;
    }

    public int getErrorCode() {
        return this.code;
    }

    public String getDebugMessage() {
        return this.debugMessage;
    }
}
