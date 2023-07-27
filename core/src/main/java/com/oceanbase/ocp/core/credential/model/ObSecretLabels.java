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

package com.oceanbase.ocp.core.credential.model;

import com.oceanbase.ocp.vault.annotation.SecretField;
import com.oceanbase.ocp.vault.annotation.SecretType;
import com.oceanbase.ocp.vault.model.SecretLabels;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SecretType(namespace = "OB")
public class ObSecretLabels implements SecretLabels {

    @SecretField(name = "clusterName")
    private String clusterName;

    @SecretField(name = "tenantName")
    private String tenantName;

    @SecretField(name = "username")
    private String username;
}
