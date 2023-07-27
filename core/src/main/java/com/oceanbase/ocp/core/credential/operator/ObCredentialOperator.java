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

package com.oceanbase.ocp.core.credential.operator;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.oceanbase.ocp.core.credential.model.ObSecretLabels;
import com.oceanbase.ocp.core.exception.NotFoundException;
import com.oceanbase.ocp.core.exception.TenantInfoTarget;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.vault.model.Credential;
import com.oceanbase.ocp.vault.store.CredentialStore;

@Component
public class ObCredentialOperator {

    @Autowired
    private CredentialStore credentialStore;

    public String acquireObPassword(String clusterName, String tenantName, String username) {
        Optional<Credential> credential = findObCredential(clusterName, tenantName, username);
        if (!credential.isPresent()) {
            throw new NotFoundException(ErrorCodes.OB_TENANT_CREDENTIAL_NOT_FOUND,
                    new TenantInfoTarget(tenantName), tenantName);
        }
        return credential.get().getPassphrase();
    }

    public Optional<Credential> findObCredential(String clusterName, String tenantName, String username) {
        ObSecretLabels secret =
                ObSecretLabels.builder().clusterName(clusterName).tenantName(tenantName).username(username).build();
        return credentialStore.findCredential(secret).stream().findFirst();
    }

    public Credential saveObCredential(String clusterName, String tenantName, String username, String password) {
        ObSecretLabels labels =
                ObSecretLabels.builder().clusterName(clusterName).tenantName(tenantName).username(username).build();
        return credentialStore.saveCredential(labels, password);
    }
}
