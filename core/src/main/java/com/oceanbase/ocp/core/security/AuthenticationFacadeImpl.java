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
package com.oceanbase.ocp.core.security;

import org.springframework.stereotype.Component;

import com.oceanbase.ocp.core.exception.AuthenticationException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.security.util.SecurityUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthenticationFacadeImpl implements AuthenticationFacade {

    @Override
    public String currentUserName() {
        return SecurityUtils.getCurrentUsername()
                .orElseThrow(() -> new AuthenticationException(ErrorCodes.IAM_USER_NOT_AUTHENTICATED));
    }

}
