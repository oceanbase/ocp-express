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

package com.oceanbase.ocp.obsdk.operator.session.model;

import java.math.BigInteger;

import lombok.Data;

@Data
public class ObSession {

    private Long id;

    private String user;

    private String tenant;

    private String host;

    private String db;

    private String command;

    private Long time;

    private String state;

    private String info;

    private String svrIp;

    private Long svrPort;

    private Long sqlPort;

    private BigInteger proxySessid;

    private String userClientIp;

    private String proxyIp;
}
