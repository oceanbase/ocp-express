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

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.common.util.encrypt.KeyGenerator;
import com.oceanbase.ocp.core.property.PropertyService;
import com.oceanbase.ocp.core.property.entity.PropertyEntity;
import com.oceanbase.ocp.core.security.model.LoginKey;
import com.oceanbase.ocp.core.security.util.OcpRSAUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LoginKeyService {

    private static final int LOGIN_KEY_SIZE = 1024;

    protected static final String KEY_LOGIN_ENCRYPTION_ENABLED = "ocp.login.encryption.enabled";
    protected static final String KEY_LOGIN_ENCRYPTION_PUBLIC_KEY = "ocp.login.encryption.public-key";
    protected static final String KEY_LOGIN_ENCRYPTION_PRIVATE_KEY = "ocp.login.encryption.private-key";

    private String loginPublicKey;
    @Getter
    private String loginPrivateKey;

    @Autowired
    private PropertyService propertyService;

    @PostConstruct
    public void init() throws InterruptedException {
        boolean loginEncryptionEnabled = Optional.ofNullable(propertyService.getProperty(KEY_LOGIN_ENCRYPTION_ENABLED))
                .map(Boolean::parseBoolean)
                .orElse(true);
        if (!loginEncryptionEnabled) {
            return;
        }

        boolean validated = validateAndSetKeys();
        if (validated) {
            log.info("Login key is legal.");
            return;
        }

        refreshAndSetKeys();
    }

    public LoginKey getLoginPublicKey() {
        if (loginPublicKey != null) {
            return new LoginKey(loginPublicKey);
        }
        return new LoginKey();
    }

    public String decryptByPrivate(String inputStr) {
        if (StringUtils.isNotEmpty(loginPrivateKey)) {
            return OcpRSAUtils.decryptByPrivate(loginPrivateKey, inputStr);
        }
        return inputStr;
    }

    private boolean validateAndSetKeys() {
        Optional<PropertyEntity> publicKeyEntity = propertyService.safeGetByKey(KEY_LOGIN_ENCRYPTION_PUBLIC_KEY);
        Optional<PropertyEntity> privateKeyEntity = propertyService.safeGetByKey(KEY_LOGIN_ENCRYPTION_PRIVATE_KEY);
        String loginPublicKey = publicKeyEntity.map(PropertyEntity::getValue).orElse(null);
        String loginPrivateKey = privateKeyEntity.map(PropertyEntity::getValue).orElse(null);
        boolean valid = validateLoginKey(loginPublicKey, loginPrivateKey);
        if (valid) {
            this.loginPublicKey = loginPublicKey;
            this.loginPrivateKey = loginPrivateKey;
        }
        log.info("Validate and set keys, publicKey={}, valid={}", loginPublicKey, valid);
        return valid;
    }

    private void refreshAndSetKeys() {
        Optional<PropertyEntity> publicKeyEntity = propertyService.safeGetByKey(KEY_LOGIN_ENCRYPTION_PUBLIC_KEY);
        Optional<PropertyEntity> privateKeyEntity = propertyService.safeGetByKey(KEY_LOGIN_ENCRYPTION_PRIVATE_KEY);
        String loginPublicKey = publicKeyEntity.map(PropertyEntity::getValue).orElse(null);
        String loginPrivateKey = privateKeyEntity.map(PropertyEntity::getValue).orElse(null);
        if (!validateLoginKey(loginPublicKey, loginPrivateKey)) {
            KeyGenerator.EncodedKeyPair encodedKeyPair = KeyGenerator.geneRsA(LOGIN_KEY_SIZE);
            loginPublicKey = encodedKeyPair.getPublicKey();
            loginPrivateKey = encodedKeyPair.getPrivateKey();
            log.info("Generate new rsa key, publicKey={}", loginPublicKey);
            publicKeyEntity.ifPresent(propertyEntity -> propertyService.updateProperty(propertyEntity.getId(),
                    encodedKeyPair.getPublicKey(), true));
            privateKeyEntity.ifPresent(propertyEntity -> propertyService.updateProperty(propertyEntity.getId(),
                    encodedKeyPair.getPrivateKey(), true));
        }
        this.loginPublicKey = loginPublicKey;
        this.loginPrivateKey = loginPrivateKey;
        log.info("Refresh and set keys, publicKey={}", loginPublicKey);
    }

    private boolean validateLoginKey(String loginPublicKey, String loginPrivateKey) {
        if (StringUtils.isEmpty(loginPublicKey) || StringUtils.isEmpty(loginPrivateKey)) {
            return false;
        }
        try {
            String str = RandomStringUtils.randomAlphanumeric(10);
            String encrypted = OcpRSAUtils.encrypt(loginPublicKey, str);
            String decrypted = OcpRSAUtils.decryptByPrivate(loginPrivateKey, encrypted);
            if (decrypted == null) {
                return false;
            }
            return str.equals(decrypted);
        } catch (Throwable throwable) {
            log.info("Validate login key failed.", throwable);
        }
        return false;
    }

}
