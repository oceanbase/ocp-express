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

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.core.exception.TooManyAttemptException;
import com.oceanbase.ocp.core.security.model.AuthenticatedUser;
import com.oceanbase.ocp.security.iam.dao.UserRepository;
import com.oceanbase.ocp.security.iam.entity.UserEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JdbcUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private UserLoginAttemptManager userLoginAttemptManager;

    @Transactional(rollbackOn = Exception.class)
    @Override
    public AuthenticatedUser loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> entityOpt = userRepository.findByUsername(username);
        if (!entityOpt.isPresent()) {
            log.warn("Username not found: username {}", username);
            throw new UsernameNotFoundException(username);
        }
        UserEntity entity = entityOpt.get();
        Long userId = entity.getId();
        request.setAttribute("userId", userId);

        if (entity.isAccountManualLocked()) {
            return entity.toAuthenticatedUser();
        }

        checkUserAttempts(entity);
        return entity.toAuthenticatedUser();
    }

    private void checkUserAttempts(UserEntity userEntity) {
        if (userEntity.isAccountAutoLocked()) {
            request.setAttribute("remainingAttempts", 0);
            throw new TooManyAttemptException(userEntity.getUsername());
        }
        int remainingAttempts = userLoginAttemptManager.getRemainingAttemptsByUser(userEntity);
        request.setAttribute("remainingAttempts", remainingAttempts);
        if (remainingAttempts == 0) {
            throw new TooManyAttemptException(userEntity.getUsername());
        }
    }

}
