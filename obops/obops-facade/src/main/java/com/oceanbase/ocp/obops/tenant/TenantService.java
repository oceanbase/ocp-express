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

package com.oceanbase.ocp.obops.tenant;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.oceanbase.ocp.core.ob.tenant.QueryTenantParam;
import com.oceanbase.ocp.obops.tenant.model.ObproxyAndConnectionString;
import com.oceanbase.ocp.obops.tenant.model.TenantInfo;
import com.oceanbase.ocp.obops.tenant.model.TenantPreCheckResult;

public interface TenantService {

    Page<TenantInfo> listTenant(QueryTenantParam param, Pageable pageable);

    TenantInfo getTenantInfo(Long obTenantId);

    void deleteTenantInfo(String tenantName);

    void deleteTenantInfo(Long tenantId);

    void deleteTenantRelatedInfo(String tenantName);

    void deleteTenantRelatedInfo(Long tenantId);

    List<ObproxyAndConnectionString> getConnectionStringTemplates(Long tenantId);

    List<ObproxyAndConnectionString> getConnectionUrlTemplates(Long tenantId);

    void modifyDescription(Long tenantId, String description);

    TenantPreCheckResult tenantPreCheck(Long tenantId);
}
