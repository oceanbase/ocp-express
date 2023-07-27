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

package com.oceanbase.ocp.obsdk.connector.impl;

import java.text.MessageFormat;

import com.oceanbase.ocp.obsdk.connector.ConnectProperties;

public class DefaultConnector extends AbstractConnector {

    private static final String DRIVER_CLASS_NAME = "com.oceanbase.jdbc.Driver";
    private static final MessageFormat URL_PATTERN =
            new MessageFormat("jdbc:oceanbase://{0}:{1}/{2}?useUnicode=true&characterEncoding=UTF8");

    public DefaultConnector(ConnectProperties connectProperties) {
        super(connectProperties);
    }

    @Override
    protected String getUrl() {
        return URL_PATTERN.format(new String[] {connectProperties.getAddress(),
                String.valueOf(connectProperties.getPort()), connectProperties.getDatabase()});
    }

    @Override
    protected String getDriverClassName() {
        return DRIVER_CLASS_NAME;
    }
}
