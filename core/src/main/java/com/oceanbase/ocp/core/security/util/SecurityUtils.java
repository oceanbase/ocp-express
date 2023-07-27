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
package com.oceanbase.ocp.core.security.util;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.oceanbase.ocp.core.security.model.AuthenticatedUser;

/**
 * Utility class for Spring Security.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Get the user details of the current authenticated user.
     *
     * @return the user details object of the current user.
     */
    public static Optional<AuthenticatedUser> getCurrentUserDetails() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication()).map(authentication -> {
            if (authentication.getPrincipal() instanceof AuthenticatedUser) {
                return (AuthenticatedUser) authentication.getPrincipal();
            }

            return null;
        });
    }

    /**
     * Get the user details of the current authenticated user.
     *
     * @return the user details object of the current user.
     */
    public static Optional<Long> getCurrentUserId() {
        return getCurrentUserDetails().map(AuthenticatedUser::getId);
    }

    /**
     * Get the username of the current authenticated user.
     *
     * @return the username of the current user.
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUserDetails().map(AuthenticatedUser::getUsername);
    }

}
