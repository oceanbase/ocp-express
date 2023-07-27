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

package com.oceanbase.ocp.obops.parameter.param;

import javax.validation.constraints.NotNull;

import com.oceanbase.ocp.obops.parameter.model.TenantParameterType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantParameterParam {

    @NotNull(message = "{error.common.field.value.not.null}")
    private String name;

    @NotNull(message = "{error.common.field.value.not.null}")
    private String value;

    private TenantParameterType parameterType = TenantParameterType.OB_SYSTEM_VARIABLE;

    public static TenantParameterParam tenantParameter(String name, String value) {
        return TenantParameterParam.builder().parameterType(TenantParameterType.OB_TENANT_PARAMETER).name(name)
                .value(value).build();
    }

    public static TenantParameterParam systemVariable(String name, String value) {
        return TenantParameterParam.builder().parameterType(TenantParameterType.OB_SYSTEM_VARIABLE).name(name)
                .value(value).build();
    }
}
