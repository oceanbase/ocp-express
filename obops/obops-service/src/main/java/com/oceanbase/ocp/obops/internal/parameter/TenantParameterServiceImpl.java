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
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

import com.oceanbase.ocp.common.util.sql.SqlParamCheckUtils;
import com.oceanbase.ocp.core.obsdk.ObAccessorFactory;
import com.oceanbase.ocp.core.property.PropertyManager;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obops.parameter.TenantParameterService;
import com.oceanbase.ocp.obops.parameter.model.TenantParameter;
import com.oceanbase.ocp.obops.parameter.model.TenantParameterInfo;
import com.oceanbase.ocp.obops.parameter.model.TenantParameterType;
import com.oceanbase.ocp.obops.parameter.param.TenantParameterParam;
import com.oceanbase.ocp.obsdk.accessor.ObAccessor;
import com.oceanbase.ocp.obsdk.accessor.variable.model.ObTenantVariable;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObParameter;
import com.oceanbase.ocp.obsdk.operator.parameter.model.SetObParameter;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TenantParameterServiceImpl implements TenantParameterService {

    @Autowired
    private ObAccessorFactory obAccessorFactory;

    @Autowired
    private ObParameterService obParameterService;

    @Autowired
    private ObSystemVariableService obSystemVariableService;

    @Autowired
    private PropertyManager propertyManager;

    @Override
    public List<TenantParameterInfo> listParameterInfo() {
        List<TenantParameterInfo> obSystemVariableInfos = obSystemVariableService.listVariableInfo().stream()
                .map(TenantParameterInfo::new)
                .collect(Collectors.toList());
        List<TenantParameterInfo> obTenantParameterInfos = obParameterService.listTenantParameterInfo().stream()
                .map(TenantParameterInfo::new)
                .collect(Collectors.toList());
        return ListUtils.union(obSystemVariableInfos, obTenantParameterInfos);
    }

    @Override
    public List<TenantParameter> listParameters(Long obTenantId) {
        ObAccessor accessor = obAccessorFactory.createObAccessor(obTenantId);
        List<TenantParameter> obSystemVariables = listObSystemVariables(accessor);
        List<TenantParameter> obTenantParameters = listObTenantParameters(accessor);
        return ListUtils.union(obSystemVariables, obTenantParameters);
    }

    private List<TenantParameter> listObSystemVariables(ObAccessor accessor) {
        List<ObTenantVariable> variableList = accessor.variable().showVariables();
        return variableList.stream()
                .map(v -> new TenantParameter(v,
                        obSystemVariableService.getObSystemVariableStaticInfo(v.getVariableName())))
                .collect(Collectors.toList());
    }

    private List<TenantParameter> listObTenantParameters(ObAccessor accessor) {
        List<ObParameter> obParameters = accessor.parameter().listParameters();

        ListMultimap<String, ObParameter> map = Multimaps.index(obParameters, ObParameter::getName);
        return map.keySet().stream()
                .map(name -> new TenantParameter(name, map.get(name),
                        obParameterService.getObParameterStaticInfo(name)))
                .collect(Collectors.toList());
    }

    @Override
    public void updateParameters(Long obTenantId, List<TenantParameterParam> paramList) {
        boolean allParamAllowed = paramList.stream()
                .allMatch(p -> SqlParamCheckUtils.check(p.getName(), propertyManager.getSqlParamPattern()));
        ExceptionUtils.illegalArgs(allParamAllowed, "tenantParameterKey");
        ObAccessor accessor = obAccessorFactory.createObAccessor(obTenantId);

        List<TenantParameterParam> systemVariableParams = paramList.stream()
                .filter(param -> param.getParameterType() == TenantParameterType.OB_SYSTEM_VARIABLE)
                .collect(Collectors.toList());
        List<TenantParameterParam> tenantParameterParams = paramList.stream()
                .filter(param -> param.getParameterType() == TenantParameterType.OB_TENANT_PARAMETER)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(systemVariableParams)) {
            Map<String, String> variables = systemVariableParams.stream()
                    .collect(Collectors.toMap(TenantParameterParam::getName, TenantParameterParam::getValue));
            obSystemVariableService.setVariables(obTenantId, variables);
        }

        if (CollectionUtils.isNotEmpty(tenantParameterParams)) {
            List<SetObParameter> inputList = tenantParameterParams.stream()
                    .map(this::buildSetObParameter)
                    .collect(Collectors.toList());
            for (SetObParameter input : inputList) {
                accessor.parameter().setParameter(input);
            }
        }
    }

    private SetObParameter buildSetObParameter(TenantParameterParam param) {
        return SetObParameter.plain(param.getName(), param.getValue());
    }
}
