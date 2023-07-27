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

package com.oceanbase.ocp.obops.tenant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObproxyAndConnectionString {

    ConnectionStringType type;

    String obProxyAddress;

    Integer obProxyPort;

    String connectionString;

    public static ObproxyAndConnectionString direct(String connectionString) {
        return ObproxyAndConnectionString.builder()
                .type(ConnectionStringType.DIRECT)
                .connectionString(connectionString)
                .build();
    }

    public ObproxyAndConnectionString formatConnectionString(Object... args) {
        return ObproxyAndConnectionString.builder()
                .type(type)
                .obProxyAddress(obProxyAddress)
                .obProxyPort(obProxyPort)
                .connectionString(String.format(connectionString, args))
                .build();
    }
}
