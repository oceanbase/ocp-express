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

package com.oceanbase.ocp.obops.internal.tenant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.common.util.sql.SqlUtils;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.QueryTenantParam;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.ob.tenant.TenantStatus;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obops.internal.tenant.repository.ObTenantRepository;

@Service
public class TenantDaoManagerImpl implements TenantDaoManager {

    @Autowired
    private ObTenantRepository obTenantRepository;

    @Override
    public ObTenantEntity nullSafeGetObTenant(String tenantName) {
        return obTenantRepository.findByName(tenantName).orElseThrow(
                () -> ExceptionUtils.newException(ErrorCodes.OB_TENANT_NAME_NOT_FOUND, tenantName));
    }

    @Override
    public ObTenantEntity nullSafeGetObTenant(Long obTenantId) {
        return obTenantRepository.findByObTenantId(obTenantId).orElseThrow(
                () -> ExceptionUtils.newException(ErrorCodes.OB_TENANT_ID_NOT_FOUND, obTenantId));
    }

    @Override
    public Optional<ObTenantEntity> getTenant(String tenantName) {
        return obTenantRepository.findByName(tenantName);
    }

    @Override
    public Optional<ObTenantEntity> getTenant(Long obTenantId) {
        return obTenantRepository.findByObTenantId(obTenantId);
    }

    @Override
    public boolean isTenantExist(String tenantName) {
        return obTenantRepository.findByName(tenantName).isPresent();
    }

    @Override
    public boolean isTenantNotExist(String tenantName) {
        return !isTenantExist(tenantName);
    }

    @Override
    public ObTenantEntity saveTenant(ObTenantEntity entity) {
        return obTenantRepository.saveAndFlush(entity);
    }

    @Override
    public void deleteTenant(ObTenantEntity entity) {
        obTenantRepository.delete(entity);
    }

    @Override
    public int updateName(Long obTenantId, String name) {
        return obTenantRepository.updateName(obTenantId, name);
    }

    @Override
    public int updateLockStatus(Long tenantId, Boolean locked) {
        return obTenantRepository.updateLockStatus(tenantId, locked);
    }

    @Override
    public int updateReadonlyStatus(Long tenantId, Boolean readonly) {
        return obTenantRepository.updateReadonlyStatus(tenantId, readonly);
    }

    @Override
    public int updatePrimaryZone(Long tenantId, String primaryZone) {
        return obTenantRepository.updatePrimaryZone(tenantId, primaryZone);
    }

    @Override
    public int updateZoneList(Long tenantId, String zoneListStr) {
        return obTenantRepository.updateZoneList(tenantId, zoneListStr);
    }

    @Override
    public int updateLocality(Long tenantId, String locality) {
        return obTenantRepository.updateLocality(tenantId, locality);
    }

    @Override
    public int updateDescription(Long tenantId, String description) {
        return obTenantRepository.updateDescription(tenantId, description);
    }

    @Override
    public int updateStatus(Long obTenantId, TenantStatus status) {
        return obTenantRepository.updateStatus(obTenantId, status);
    }

    @Override
    public Page<ObTenantEntity> queryTenant(QueryTenantParam queryParam, Pageable pageable) {
        Specification<ObTenantEntity> specification = buildSpecification(queryParam);
        return obTenantRepository.findAll(specification, pageable);
    }

    @Override
    public List<ObTenantEntity> queryAllTenant() {
        return obTenantRepository.findAll();
    }

    private Specification<ObTenantEntity> buildSpecification(QueryTenantParam queryParam) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicatesList = new ArrayList<>();
            if (StringUtils.isNotEmpty(queryParam.getName())) {
                if (IS_BATCH_QUERY.test(queryParam.getName())) {
                    List<String> tenantNames =
                            Arrays.asList(queryParam.getName().split(BATCH_QUERY_SPLIT_REGEX));
                    predicatesList.add(root.get("name").in(tenantNames));
                } else {
                    String tenantName = SqlUtils.escapeWildcards(queryParam.getName().trim());
                    predicatesList.add(criteriaBuilder.like(root.get("name"), '%' + tenantName + '%'));
                }
            }
            if (queryParam.getModeList() != null) {
                predicatesList.add(root.get("mode").in(queryParam.getModeList()));
            }
            if (queryParam.getLocked() != null) {
                predicatesList.add(criteriaBuilder.equal(root.get("locked"), queryParam.getLocked()));
            }
            if (queryParam.getReadonly() != null) {
                predicatesList.add(criteriaBuilder.equal(root.get("readonly"), queryParam.getReadonly()));
            }
            if (queryParam.getStatusList() != null) {
                predicatesList.add(root.get("status").in(queryParam.getStatusList()));
            }
            if (queryParam.getTenantIdSet() != null) {
                predicatesList.add(root.get("id").in(queryParam.getTenantIdSet()));
            }
            Predicate[] predicates = new Predicate[predicatesList.size()];
            return criteriaBuilder.and(predicatesList.toArray(predicates));
        };
    }

    private static final java.util.function.Predicate<String> IS_BATCH_QUERY = s -> {
        if (s == null || s.isEmpty()) {
            return false;
        }
        boolean isBatch = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            isBatch = Character.isWhitespace(c) || ',' == c;
            if (isBatch) {
                break;
            }
        }
        return isBatch;
    };

    private static final String BATCH_QUERY_SPLIT_REGEX = "(,|\\s)+";
}
