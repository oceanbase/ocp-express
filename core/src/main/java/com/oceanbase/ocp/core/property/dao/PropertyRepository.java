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

package com.oceanbase.ocp.core.property.dao;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.oceanbase.ocp.core.property.entity.PropertyEntity;
import com.oceanbase.ocp.core.property.entity.VisibleLevel;

public interface PropertyRepository
        extends JpaRepository<PropertyEntity, Long>, JpaSpecificationExecutor<PropertyEntity> {

    default Page<PropertyEntity> findAllByVisible(String keyLike, Pageable pageable) {
        return findAll(Specification.where(keyLike(keyLike)).and(visible(VisibleLevel.PUBLIC)), pageable);
    }

    Optional<PropertyEntity> findByIdAndFatalFalse(Long id);

    Optional<PropertyEntity> findByKeyAndApplicationAndProfileAndLabel(String key, String application, String profile,
            String label);

    static Specification<PropertyEntity> keyLike(String keyLike) {
        return (root, query, criteriaBuilder) -> StringUtils.isEmpty(keyLike) ? criteriaBuilder.conjunction()
                : criteriaBuilder.like(root.get("key"), "%" + keyLike + "%");
    }

    static Specification<PropertyEntity> visible(VisibleLevel visibleLevel) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("visibleLevel"), visibleLevel);
    }

}
