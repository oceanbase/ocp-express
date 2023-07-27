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

package com.oceanbase.ocp.obops.internal.parameter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import com.oceanbase.ocp.core.obsdk.ObAccessorFactory;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.obops.internal.parameter.model.ObParameterValue;
import com.oceanbase.ocp.obops.parameter.model.ObParameterInfo;
import com.oceanbase.ocp.obops.parameter.model.ObParameterStaticInfo;
import com.oceanbase.ocp.obsdk.accessor.ObAccessor;
import com.oceanbase.ocp.obsdk.accessor.ParameterAccessor;
import com.oceanbase.ocp.obsdk.operator.ParameterOperator;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObParameter;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObTenantParameter;
import com.oceanbase.ocp.obsdk.operator.parameter.model.SetObParameter;

@Service
public class ObParameterService {

    @Autowired
    private ObOperatorFactory obOperatorFactory;

    @Autowired
    private ObAccessorFactory obAccessorFactory;

    private static final String[] READONLY_PARAMETER_ARRAY = new String[] {
            "cluster",
            "cluster_id",
            "data_dir",
            "devname",
            "mysql_port",
            "rpc_port",
    };

    public static final Set<String> READONLY_PARAMETERS = Sets.newHashSet(READONLY_PARAMETER_ARRAY);

    public ObParameterStaticInfo getObParameterStaticInfo(String name) {
        return ObParameterStaticInfo.builder()
                .name(name)
                .readonly(READONLY_PARAMETERS.contains(name))
                .build();
    }

    public List<ObParameterInfo> listTenantParameterInfo() {
        ParameterOperator operator = obOperatorFactory.createParameterOperator();

        List<ObTenantParameter> obTenantParameters = operator.listTenantParameters();

        ListMultimap<String, ObTenantParameter> map = Multimaps.index(obTenantParameters, ObParameter::getName);
        return map.keySet().stream()
                .map(name -> ObParameterInfo.tenant(map.get(name).get(0), getObParameterStaticInfo(name)))
                .collect(Collectors.toList());
    }

    public Optional<ObParameterValue> getHiddenClusterParameter(String parameterName) {
        ParameterOperator parameterOperator = obOperatorFactory.createParameterOperator();
        List<ObParameter> obParameters = parameterOperator.getHiddenClusterParameter(parameterName);
        return ObParameterValue.fromObParameters(obParameters);
    }

    public void setTenantParameters(Long tenantId, Map<String, String> parameters) {
        List<SetObParameter> setObParameters = parameters.entrySet().stream()
                .map(e -> SetObParameter.plain(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        setTenantParameters(tenantId, setObParameters);
    }

    public void setTenantParameters(Long tenantId, List<SetObParameter> parameters) {
        ObAccessor obAccessor = obAccessorFactory.createObAccessor(tenantId);
        ParameterAccessor parameterAccessor = obAccessor.parameter();
        for (SetObParameter parameter : parameters) {
            parameterAccessor.setParameter(parameter);
        }
    }
}
