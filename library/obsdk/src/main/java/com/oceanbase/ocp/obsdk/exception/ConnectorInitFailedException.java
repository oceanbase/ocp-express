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

import com.oceanbase.ocp.obsdk.connector.ConnectProperties;

import lombok.Getter;

public class ConnectorInitFailedException extends RuntimeException {

    private static final long serialVersionUID = 1820915942465442204L;

    @Getter
    private final ConnectProperties connectProperties;

    public ConnectorInitFailedException(ConnectProperties connectProperties, String message, Throwable cause) {
        super(message, cause);
        this.connectProperties = connectProperties;
    }
}
