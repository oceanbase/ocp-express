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
package com.oceanbase.ocp.security.profile;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oceanbase.ocp.core.security.model.AuthenticatedUser;
import com.oceanbase.ocp.core.security.util.SecurityUtils;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.security.iam.UserServiceImpl;
import com.oceanbase.ocp.security.profile.param.ChangePasswordRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProfileService {

    @Autowired
    private UserServiceImpl userService;

    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordRequest request) {
        Optional<Long> optionalUserId = SecurityUtils.getCurrentUserId();
        if (optionalUserId.isPresent()) {
            userService.validateAndChangePassword(optionalUserId.get(), request.getCurrentPassword(),
                    request.getNewPassword());
        } else {
            ExceptionUtils.notAuthenticated(false);
        }
    }

    public AuthenticatedUser getCurrentAuthenticatedUser() {
        return userService.findAuthenticatedUser(SecurityUtils.getCurrentUsername().get());
    }

}
