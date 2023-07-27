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

import java.util.List;
import java.util.Objects;

import com.oceanbase.ocp.obsdk.accessor.variable.model.ObTenantVariable;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObParameter;
import com.oceanbase.ocp.obsdk.operator.parameter.model.ObParameterScope;

import lombok.Data;

@Data
public class TenantParameter {

    private String name;

    private TenantParameterType parameterType;

    private String currentValue;

    private CompatibleType compatibleType;

    private String description;

    private Boolean needRestart;

    private Boolean readonly;

    public TenantParameter(ObTenantVariable variable, ObSystemVariableStaticInfo info) {
        this.name = variable.getVariableName();
        this.parameterType = TenantParameterType.OB_SYSTEM_VARIABLE;
        this.currentValue = variable.getValue();
        ObSystemVariableInfo obSystemVariableInfo = new ObSystemVariableInfo(name, info);
        TenantParameterInfo tenantParameterInfo = new TenantParameterInfo(obSystemVariableInfo);
        fillParamInfo(tenantParameterInfo);
    }

    public TenantParameter(String name, List<ObParameter> obTenantParameters, ObParameterStaticInfo info) {
        this.name = name;
        this.parameterType = TenantParameterType.OB_TENANT_PARAMETER;
        this.currentValue = obTenantParameters.stream().map(ObParameter::getValue).filter(Objects::nonNull).findFirst()
                .orElse(null);
        ObParameterInfo obParameterInfo = new ObParameterInfo(ObParameterScope.TENANT, obTenantParameters.get(0), info);
        TenantParameterInfo tenantParameterInfo = new TenantParameterInfo(obParameterInfo);
        fillParamInfo(tenantParameterInfo);
    }

    private void fillParamInfo(TenantParameterInfo info) {
        this.compatibleType = info.getCompatibleType();
        this.description = info.getDescription();
        this.needRestart = info.getNeedRestart();
        this.readonly = info.getReadonly();
    }
}
