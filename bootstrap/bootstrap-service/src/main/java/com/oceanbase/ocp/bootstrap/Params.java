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

package com.oceanbase.ocp.bootstrap;

import java.util.List;

import com.oceanbase.ocp.bootstrap.core.Action;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class Params {

    @Getter
    @AllArgsConstructor
    public static class PropertyPair {

        private String name;
        private String value;
    }

    boolean enabled;
    Auth auth;
    Action action;
    int port;
    String progressLogPath;
    String metaAddress;
    String metaDatabase;
    String metaUsername;
    String metaPassword;
    String metaPubKey;

    List<PropertyPair> configProperties;
}
