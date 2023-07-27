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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.oceanbase.ocp.common.util.ListUtils;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObParameter;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObTenantParameter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClusterParameterValue {


    Set<String> values;

    boolean singleValueInCluster;

    List<ServerParameterValue> serverValues;

    List<TenantParameterValue> tenantValues;

    Collection<ObParameter> obParameters;

    @SuppressWarnings("unchecked")
    public ClusterParameterValue(ClusterParameterType type, Collection<ObParameter> obParameters) {
        this.obParameters = obParameters;
        this.values = obParameters.stream().map(ObParameter::getValue).collect(Collectors.toSet());
        this.singleValueInCluster = this.values.size() <= 1;
        if (type == ClusterParameterType.OB_TENANT_PARAMETER) {
            List<ObTenantParameter> obTenantParameters = ListUtils
                    .removeDuplicate((List<ObTenantParameter>) (List<?>) obParameters, ObTenantParameter::getTenantId);
            this.tenantValues = obTenantParameters.stream()
                    .map(p -> new TenantParameterValue(p.getTenantId(), p.getTenantName(), p.getValue()))
                    .collect(Collectors.toList());
        } else {
            this.serverValues = obParameters.stream()
                    .map(p -> new ServerParameterValue(p.getSvrIp(), p.getSvrPort(), p.getValue()))
                    .collect(Collectors.toList());
        }
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantParameterValue {

        Long tenantId;
        String tenantName;
        String value;
    }
}
