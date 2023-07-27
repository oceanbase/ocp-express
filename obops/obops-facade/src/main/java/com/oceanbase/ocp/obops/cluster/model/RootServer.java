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

package com.oceanbase.ocp.obops.cluster.model;

import com.oceanbase.ocp.obops.constant.RootServerRole;
import com.oceanbase.ocp.obsdk.operator.cluster.model.RootService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RootServer {

    private String ip;

    private Integer svrPort;

    private RootServerRole role;

    public static RootServer fromRootService(RootService rootService) {
        return RootServer.builder()
                .ip(rootService.getSvrIp())
                .svrPort(rootService.getSvrPort())
                .role(RootServerRole.fromValue(rootService.getRole().getValue()))
                .build();
    }
}
