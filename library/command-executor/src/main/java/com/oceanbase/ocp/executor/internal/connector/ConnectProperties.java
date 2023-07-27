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

package com.oceanbase.ocp.executor.internal.connector;

import com.oceanbase.ocp.executor.internal.auth.Authentication;
import com.oceanbase.ocp.executor.internal.util.ValidateUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@With
public class ConnectProperties {

    /**
     * Host ip address.
     */
    private String hostAddress;

    /**
     * mgr_agent http port.
     */
    private Integer httpPort;

    /**
     * Agent auth info.
     */
    private Authentication authentication;

    public void validate() {
        ValidateUtils.requireNotNull(authentication, "authentication");
        ValidateUtils.requireNotBlank(hostAddress, "hostAddress");
        ValidateUtils.requirePositive(httpPort, "httpPort");
    }

}
