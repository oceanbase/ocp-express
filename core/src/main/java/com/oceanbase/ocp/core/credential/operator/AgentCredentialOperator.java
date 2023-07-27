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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.oceanbase.ocp.core.credential.model.AgentSecretLabels;
import com.oceanbase.ocp.vault.model.Credential;
import com.oceanbase.ocp.vault.store.CredentialStore;

@Component
public class AgentCredentialOperator {

    @Autowired
    private CredentialStore credentialStore;

    public Credential saveAgentCredential(String username, String password) {
        AgentSecretLabels labels = AgentSecretLabels.builder().username(username).build();
        return credentialStore.saveCredential(labels, password);
    }
}
