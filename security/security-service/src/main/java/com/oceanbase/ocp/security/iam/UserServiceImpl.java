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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oceanbase.ocp.core.exception.NotFoundException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.security.model.AuthenticatedUser;
import com.oceanbase.ocp.core.security.util.CustomBCryptPasswordEncoder;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.security.iam.dao.UserRepository;
import com.oceanbase.ocp.security.iam.entity.UserEntity;
import com.oceanbase.ocp.security.util.RegExChecker;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoginKeyService loginKeyService;

    private CustomBCryptPasswordEncoder encoder;

    @PostConstruct
    public void init() throws Exception {
        encoder = new CustomBCryptPasswordEncoder(loginKeyService.getLoginPrivateKey());
    }

    @Override
    @Transactional
    public AuthenticatedUser findAuthenticatedUser(String username) {
        return nullSafeGet(username).toAuthenticatedUser();
    }

    private void validatePassword(String password) {
        ExceptionUtils.illegalArgs(RegExChecker.checkPassword(password), ErrorCodes.IAM_PASSWORD_NOT_VALID);
    }

    private UserEntity nullSafeGet(long id) throws NotFoundException {
        Optional<UserEntity> optionalUserEntity = userRepository.findById(id);
        ExceptionUtils.notFound(optionalUserEntity.isPresent(), UserEntity.class.getSimpleName(), id);
        return optionalUserEntity.get();
    }

    private UserEntity nullSafeGet(String username) throws NotFoundException {
        Optional<UserEntity> entity = userRepository.findByUsername(username);
        ExceptionUtils.notFound(entity.isPresent(), UserEntity.class.getSimpleName(), username);
        return entity.get();
    }

    @Transactional(rollbackFor = Exception.class)
    public void validateAndChangePassword(long id, String encodedOldPassword, String newPassword) {
        String rawPassword = loginKeyService.decryptByPrivate(newPassword);
        validatePassword(rawPassword);
        UserEntity entity = nullSafeGet(id);
        String currentEncodedPassword = entity.getPassword();
        ExceptionUtils.illegalArgs(encoder.matches(encodedOldPassword, currentEncodedPassword),
                ErrorCodes.IAM_CURRENT_PASSWORD_NOT_MATCH);
        ExceptionUtils.illegalArgs(!encoder.matches(newPassword, currentEncodedPassword),
                ErrorCodes.IAM_NEW_PASSWORD_DUPLICATED);
        // reset password for user
        String newEncodedPassword = encoder.encode(rawPassword);
        entity.setPassword(newEncodedPassword);
        entity.setNeedChangePassword(false);
        userRepository.save(entity);
        log.info("Changed password: {} {}", entity.getId(), entity.getUsername());
    }
}
