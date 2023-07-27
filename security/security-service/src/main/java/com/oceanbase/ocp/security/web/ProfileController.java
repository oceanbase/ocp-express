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
package com.oceanbase.ocp.security.web;

import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.core.response.SuccessResponse;
import com.oceanbase.ocp.core.security.model.AuthenticatedUser;
import com.oceanbase.ocp.security.profile.ProfileService;
import com.oceanbase.ocp.security.profile.param.ChangePasswordRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/profiles/me")
    public SuccessResponse<AuthenticatedUser> userInfo() {
        return ResponseBuilder.single(profileService.getCurrentAuthenticatedUser());
    }

    @PutMapping("/profiles/me/changePassword")
    public SuccessResponse<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest, HttpServletRequest request) {
        profileService.changePassword(changePasswordRequest);

        try {
            request.logout();
            SecurityContextHolder.getContext().setAuthentication(null);
            SecurityContextHolder.clearContext();
        } catch (ServletException e) {
            log.warn("An error occurred while logging out the current user {}", e.getMessage());
        }
        return ResponseBuilder.single(Collections.singletonMap("location", "/login"));
    }

}
