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

import com.oceanbase.ocp.obops.tenant.model.TenantCompaction;
import com.oceanbase.ocp.obops.tenant.model.TenantCompactionHistory;

public interface TenantCompactionService {

    TenantCompaction getTenantCompaction(Long obTenantId);

    void triggerTenantCompaction(Long obTenantId);

    void clearCompactionError(Long obTenantId);

    List<TenantCompactionHistory> topCompactions(Integer top, Integer times);
}
