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

package com.oceanbase.ocp.vault.store;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.oceanbase.ocp.common.util.encrypt.CryptoUtils;
import com.oceanbase.ocp.common.util.encrypt.Encryptors;
import com.oceanbase.ocp.vault.entity.CredentialEntity;
import com.oceanbase.ocp.vault.model.Credential;
import com.oceanbase.ocp.vault.model.SecretLabels;
import com.oceanbase.ocp.vault.repository.CredentialRepository;
import com.oceanbase.ocp.vault.util.JdbcSecretResolver;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JdbcCredentialStore implements CredentialStore {

    @Value("${ocp.express.vault.secret-key:}")
    private String encryptSecretKey;

    @Autowired
    private CredentialRepository credentialRepository;

    @Override
    public List<Credential> findCredential(SecretLabels labels) {
        JdbcSecretResolver resolver = JdbcSecretResolver.getInstance();
        String namespace = resolver.getNamespace(labels);
        String fuzzyQuery = resolver.fuzzyQuery(labels);
        return credentialRepository.findAllByNamespaceAndLabelsLike(namespace, fuzzyQuery).stream()
                .map(credential -> toModel(credential, getGlobalSecretKey(), labels.getClass()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Credential saveCredential(SecretLabels secretLabels, String passphrase) {
        JdbcSecretResolver resolver = JdbcSecretResolver.getInstance();
        String namespace = resolver.getNamespace(secretLabels);
        String labelsText = resolver.convertToLabelsText(secretLabels);
        CredentialEntity entity =
                credentialRepository.findByNamespaceAndLabels(namespace, labelsText).orElse(new CredentialEntity());
        if (entity.getId() != null) {
            log.info("Credential {} is exists, begin updating the passphrase.", secretLabels);
        }
        String secretKey = getGlobalSecretKey();
        entity.setNamespace(namespace);
        entity.setLabels(labelsText);
        if (StringUtils.isEmpty(passphrase)) {
            entity.setPassphrase(passphrase);
        } else {
            entity.setPassphrase(CryptoUtils.encrypt(passphrase, secretKey));
        }
        return toModel(credentialRepository.saveAndFlush(entity), secretKey, secretLabels.getClass());
    }

    private String getGlobalSecretKey() {
        return Encryptors.ocpLegacy().decrypt(encryptSecretKey);
    }

    private static Credential toModel(CredentialEntity entity, String secretKey, Class<? extends SecretLabels> clazz) {
        Credential credential = new Credential();
        credential.setNamespace(entity.getNamespace());
        credential.setCreateTime(entity.getCreateTime());
        credential.setUpdateTime(entity.getUpdateTime());
        credential.setId(entity.getId());
        credential.setSecretLabels(
                JdbcSecretResolver.getInstance().convertToSecret(entity.getLabels(), secretKey, clazz));
        if (StringUtils.isEmpty(entity.getPassphrase())) {
            credential.setPassphrase(entity.getPassphrase());
        } else {
            credential.setPassphrase(CryptoUtils.decrypt(entity.getPassphrase(), secretKey));
        }
        return credential;
    }
}
