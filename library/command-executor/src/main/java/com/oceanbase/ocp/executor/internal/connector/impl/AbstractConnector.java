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

package com.oceanbase.ocp.executor.internal.connector.impl;

import com.oceanbase.ocp.executor.exception.ConnectorNotInitException;
import com.oceanbase.ocp.executor.internal.connector.ConnectProperties;
import com.oceanbase.ocp.executor.internal.connector.Connector;

public abstract class AbstractConnector<CLI> implements Connector<CLI> {

    private final ConnectProperties connectProperties;

    public AbstractConnector(ConnectProperties connectProperties) {
        this.connectProperties = connectProperties;
    }

    protected ConnectProperties getConnectProperties() {
        return connectProperties;
    }

    protected void checkInited() {
        if (connector() == null) {
            throw new ConnectorNotInitException(
                    "[Connector]: failed to operator connector because connector is not inited!");
        }
    }
}
