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

package com.oceanbase.ocp.obops.parameter;

import java.util.List;

import com.oceanbase.ocp.obops.parameter.model.TenantParameter;
import com.oceanbase.ocp.obops.parameter.model.TenantParameterInfo;
import com.oceanbase.ocp.obops.parameter.param.TenantParameterParam;

public interface TenantParameterService {

    List<TenantParameterInfo> listParameterInfo();

    List<TenantParameter> listParameters(Long obTenantId);

    void updateParameters(Long obTenantId, List<TenantParameterParam> paramList);
}
