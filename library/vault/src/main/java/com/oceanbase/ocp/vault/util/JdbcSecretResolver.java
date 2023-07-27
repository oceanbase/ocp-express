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

package com.oceanbase.ocp.vault.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.google.common.collect.Lists;

import com.oceanbase.ocp.common.lang.Pair;
import com.oceanbase.ocp.vault.annotation.SecretField;
import com.oceanbase.ocp.vault.annotation.SecretType;
import com.oceanbase.ocp.vault.model.SecretLabels;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcSecretResolver {

    private JdbcSecretResolver() {}

    public String fuzzyQuery(SecretLabels secretLabels) {
        Validate.notNull(secretLabels, "param 'secret' can't be null!");
        Class<? extends SecretLabels> clazz = secretLabels.getClass();
        List<Pair<String, String>> secretFields = Lists.newArrayList();
        try {
            SecretField secretField;
            for (Field field : clazz.getDeclaredFields()) {
                // the query use passphrase field is not allowed
                if ((secretField = field.getAnnotation(SecretField.class)) != null) {
                    field.setAccessible(true);
                    String name = secretField.name();
                    Object value = field.get(secretLabels);
                    String realVal = value == null ? "%" : String.valueOf(value);
                    secretFields.add(new Pair<>(name, realVal));
                }
            }
            return "%" + toSecretText(secretFields) + "%";
        } catch (IllegalAccessException ex) {
            log.warn("Failed to build query prefix of secret, error message:{}", ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }


    public SecretLabels convertToSecret(String secretText, String secretKey, Class<? extends SecretLabels> clazz) {
        Validate.notNull(secretText, "param 'secretMap' can't be null!");
        Validate.notEmpty(secretKey, "param 'secretKey' can't be empty!");
        Validate.notNull(clazz, "param 'clazz' can't be null!");
        Map<String, String> secretMap = toSecretMap(secretText);
        try {
            SecretLabels secretLabels = clazz.newInstance();
            SecretField secretField;
            for (Field field : clazz.getDeclaredFields()) {
                if ((secretField = field.getAnnotation(SecretField.class)) != null) {
                    field.setAccessible(true);
                    String name = secretField.name();
                    String value = secretMap.get(name);
                    requireNotNull(name, value);
                    field.set(secretLabels, value);
                }
            }
            return secretLabels;
        } catch (IllegalAccessException | InstantiationException ex) {
            log.warn("Failed to convert secret map to secret object, error message:{}", ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public String convertToLabelsText(SecretLabels secretLabels) {
        Validate.notNull(secretLabels, "param 'secret' can't be null!");
        Class<? extends SecretLabels> clazz = secretLabels.getClass();
        List<Pair<String, String>> secretFields = Lists.newArrayList();
        try {
            SecretField secretField;
            for (Field field : clazz.getDeclaredFields()) {
                if ((secretField = field.getAnnotation(SecretField.class)) != null) {
                    field.setAccessible(true);
                    String name = secretField.name();
                    String value = String.valueOf(field.get(secretLabels));
                    secretFields.add(new Pair<>(name, value));
                }
            }
            return toSecretText(secretFields);
        } catch (IllegalAccessException ex) {
            log.warn("Failed to convert secret map to secret object, error message:{}", ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public String getNamespace(SecretLabels secretLabels) {
        Class<? extends SecretLabels> clazz = secretLabels.getClass();
        SecretType secretType = clazz.getAnnotation(SecretType.class);
        if (secretType == null) {
            throw new RuntimeException("Secret type need @SecretType annotation!");
        }
        String namespace = secretType.namespace();

        if (StringUtils.isEmpty(namespace)) {
            throw new RuntimeException("Secret type namespace can't be empty");
        }
        return namespace;
    }

    private void requireNotNull(String name, String value) {
        if (value == null) {
            throw new RuntimeException("param '" + name + "' can't be null");
        }
    }

    private String toSecretText(List<Pair<String, String>> secretFields) {
        return secretFields.stream().map(pair -> pair.getLeft() + "=" + pair.getRight())
                .collect(Collectors.joining("|"));
    }

    private Map<String, String> toSecretMap(String secretText) {
        Map<String, String> secretMap = new HashMap<>();
        for (String ketValStr : secretText.split("\\|")) {
            String[] keyVal = ketValStr.split("=");
            secretMap.put(keyVal[0], keyVal[1]);
        }
        return secretMap;
    }

    public static JdbcSecretResolver getInstance() {
        return Singleton.INSTANCE.getResolver();
    }

    private enum Singleton {

        INSTANCE(new JdbcSecretResolver());

        private final JdbcSecretResolver resolver;

        Singleton(JdbcSecretResolver resolver) {
            this.resolver = resolver;
        }

        public JdbcSecretResolver getResolver() {
            return resolver;
        }
    }
}
