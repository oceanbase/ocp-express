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

package com.oceanbase.ocp.obops.parameter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantParameterInfo {

    private String name;

    private TenantParameterType parameterType;

    private CompatibleType compatibleType;

    private String description;

    private Boolean needRestart;

    private Boolean readonly;

    public TenantParameterInfo(ObSystemVariableInfo info) {
        fillParamInfo(info);
    }

    public TenantParameterInfo(ObParameterInfo info) {
        fillParamInfo(info);
    }

    private void fillParamInfo(ObSystemVariableInfo info) {
        this.name = info.getName();
        this.parameterType = TenantParameterType.OB_SYSTEM_VARIABLE;
        this.compatibleType = info.getCompatibleType();
        // TODO description
        // All system variables are dynamic effective.
        this.needRestart = false;
        this.readonly = info.getReadonly();
    }

    private void fillParamInfo(ObParameterInfo info) {
        this.name = info.getName();
        this.parameterType = TenantParameterType.OB_TENANT_PARAMETER;
        // All OB tenant parameters are compatible to both tenant modes.
        this.compatibleType = CompatibleType.ALL;
        this.description = info.getDescription();
        this.needRestart = info.getNeedRestart();
        this.readonly = info.getReadonly();
    }
}
