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

package com.oceanbase.ocp.vault.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oceanbase.ocp.vault.entity.CredentialEntity;

@Repository
public interface CredentialRepository extends JpaRepository<CredentialEntity, Long> {

    /**
     * find all credentials by resource type and secret.
     *
     * @param namespace the namespace of secret
     * @param fuzzyLabels a fuzzy string which contains '%'.<br>
     *        eg. "%a=foo|b=%|c=bar%"
     *
     * @return credential entity
     */
    List<CredentialEntity> findAllByNamespaceAndLabelsLike(String namespace, String fuzzyLabels);

    Optional<CredentialEntity> findByNamespaceAndLabels(String namespace, String labels);
}
