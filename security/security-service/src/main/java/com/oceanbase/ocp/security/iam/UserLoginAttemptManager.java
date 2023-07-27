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
package com.oceanbase.ocp.security.iam;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import com.oceanbase.ocp.core.exception.TooManyAttemptException;
import com.oceanbase.ocp.core.property.PropertyManager;
import com.oceanbase.ocp.core.security.model.AuthenticatedUser;
import com.oceanbase.ocp.core.util.WebRequestUtils;
import com.oceanbase.ocp.security.iam.constants.LoginResult;
import com.oceanbase.ocp.security.iam.dao.UserRepository;
import com.oceanbase.ocp.security.iam.entity.UserEntity;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserLoginAttemptManager {

    private final HttpServletRequest request;

    private final PropertyManager propertyManager;

    private final UserRepository userRepository;

    private final Cache<String, Integer> loginFailedCache;

    public UserLoginAttemptManager(HttpServletRequest request, PropertyManager propertyManager,
            UserRepository userRepository) {
        this.request = request;
        this.propertyManager = propertyManager;
        this.userRepository = userRepository;
        this.loginFailedCache = Caffeine.newBuilder()
                .expireAfterWrite(getLockMinutes(), TimeUnit.MINUTES)
                .build();
    }

    public void handleLoginSuccess(AuthenticatedUser user) {
        if (WebRequestUtils.isBasicAuth(request)) {
            return;
        }
        saveUserAttempt(user.getUsername(), LoginResult.SUCCESS);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void handleLoginFail(String reqUsername, Throwable rootCause) {
        if (WebRequestUtils.isBasicAuth(request)) {
            return;
        }
        LoginResult result;
        if (rootCause instanceof LockedException) {
            result = LoginResult.LOCKED;
        } else if (rootCause instanceof BadCredentialsException) {
            result = LoginResult.BAD_CREDENTIALS;
        } else if (rootCause instanceof UsernameNotFoundException) {
            result = LoginResult.USER_NOT_FOUND;
        } else if (rootCause instanceof TooManyAttemptException) {
            result = LoginResult.TOO_MANY_ATTEMPTS;
        } else {
            result = LoginResult.OTHER;
        }
        saveUserAttempt(reqUsername, result);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public int getRemainingAttemptsByUser(UserEntity userEntity) {
        int remainCounts = getMaxAttempts() - currentLoginFailedCount(userEntity.getUsername());
        int remainingAttempts = Math.max(remainCounts, 0);
        if (remainingAttempts == 0) {
            userEntity.setAccountLocked(true);
            userEntity.setLockExpiredTime(OffsetDateTime.now().plusMinutes(getLockMinutes()));
            userRepository.saveAndFlush(userEntity);
        }
        return remainingAttempts;
    }

    public int getRemainAttemptsByUsername(String username) {
        int remainCounts = getMaxAttempts() - currentLoginFailedCount(username);
        return Math.max(remainCounts, 0);
    }

    private int currentLoginFailedCount(String username) {
        Integer attemptCount = loginFailedCache.getIfPresent(username);
        return attemptCount == null ? 0 : attemptCount;
    }

    private void saveUserAttempt(String username, LoginResult result) {
        if (LoginResult.SUCCESS == result || LoginResult.LOCKED == result || LoginResult.TOO_MANY_ATTEMPTS == result) {
            return;
        }
        loginFailedCache.put(username, currentLoginFailedCount(username) + 1);
    }

    private long getLockMinutes() {
        return propertyManager.getUserLoginLockoutMinutes();
    }

    private int getMaxAttempts() {
        return propertyManager.getUserLoginMaxAttempts();
    }

}
