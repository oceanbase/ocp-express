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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;

import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obops.parameter.ClusterParameterService;
import com.oceanbase.ocp.obops.parameter.model.ClusterParameter;
import com.oceanbase.ocp.obops.parameter.model.ClusterParameterType;
import com.oceanbase.ocp.obops.parameter.param.UpdateClusterParameterParam;
import com.oceanbase.ocp.obsdk.operator.ObOperator;
import com.oceanbase.ocp.obsdk.operator.ParameterOperator;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObParameter;
import com.oceanbase.ocp.obsdk.operator.parameter.model.SetObParameter;
import com.oceanbase.ocp.obsdk.operator.parameter.model.SetObParameter.SetObParameterBuilder;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ClusterParameterServiceImpl implements ClusterParameterService {

    @Autowired
    private ObOperatorFactory obOperatorFactory;

    @Autowired
    private ObParameterService obParameterService;

    @Override
    public List<ClusterParameter> listParameters() {
        ObOperator operator = obOperatorFactory.createObOperator();
        List<ClusterParameter> obClusterParameters = listObClusterParameters(operator.parameter());
        List<ClusterParameter> obTenantParameters = listObTenantParameters(operator.parameter());
        return ListUtils.union(obClusterParameters, obTenantParameters);
    }

    private List<ClusterParameter> listObClusterParameters(ParameterOperator parameterOperator) {
        List<ObParameter> obClusterParameters = parameterOperator.listClusterParameters();
        return mergeParameters(ClusterParameterType.OB_CLUSTER_PARAMETER, obClusterParameters);
    }

    @SuppressWarnings("unchecked")
    private List<ClusterParameter> listObTenantParameters(ParameterOperator parameterOperator) {
        List<ObParameter> obTenantParameters = (List<ObParameter>) (List<?>) parameterOperator.listTenantParameters();
        return mergeParameters(ClusterParameterType.OB_TENANT_PARAMETER, obTenantParameters);
    }

    private List<ClusterParameter> mergeParameters(ClusterParameterType type, List<ObParameter> obParameters) {
        ImmutableListMultimap<String, ObParameter> map = Multimaps.index(obParameters, ObParameter::getName);
        return map.keySet().stream()
                .map(name -> new ClusterParameter(name, type, map.get(name),
                        obParameterService.getObParameterStaticInfo(name)))
                .collect(Collectors.toList());
    }

    @Override
    public void updateParameters(List<UpdateClusterParameterParam> params) {
        Validate.notEmpty(params);
        params.forEach(this::validateUpdateClusterParameterParam);
        params.forEach(this::updateParameterInternal);
    }

    private void validateUpdateClusterParameterParam(UpdateClusterParameterParam param) {
        // `zones` and `servers` cannot be both specified.
        ExceptionUtils.require(param.getZones() == null || param.getServers() == null,
                ErrorCodes.COMMON_ILLEGAL_ARGUMENT, "zones, servers");

        // For ob tenant parameters, `tenants` and `allTenant` should be specified one
        // and only one.
        if (param.getParameterType() == ClusterParameterType.OB_TENANT_PARAMETER) {
            ExceptionUtils.require(param.getTenants() != null || param.getAllTenants() != null,
                    ErrorCodes.COMMON_ILLEGAL_ARGUMENT, "tenants, allTenants");
            ExceptionUtils.require(param.getTenants() == null || param.getAllTenants() == null,
                    ErrorCodes.COMMON_ILLEGAL_ARGUMENT, "tenants, allTenants");
        }
    }

    private void updateParameterInternal(UpdateClusterParameterParam param) {
        ClusterParameterType parameterType = param.getParameterType();
        List<SetObParameter> setObParameterList = buildSetObParameter(param);
        ParameterOperator parameterOperator = obOperatorFactory.createObOperator().parameter();
        if (parameterType == ClusterParameterType.OB_TENANT_PARAMETER) {
            for (SetObParameter setObParameter : setObParameterList) {
                parameterOperator.setTenantParameter(setObParameter);
            }
        } else {
            for (SetObParameter setObParameter : setObParameterList) {
                parameterOperator.setClusterParameter(setObParameter);
            }
        }
        log.info("update cluster parameter done, parameterType={}, setObParameterList={}", parameterType,
                setObParameterList);
    }

    // Since only one zone/server/tenant can be specified in `alter system set
    // parameter` at a time, we need to construct multiple `SetObParameter`
    // parameters, with the number of the Cartesian product of zones/servers ✕
    // tenants.
    private List<SetObParameter> buildSetObParameter(UpdateClusterParameterParam param) {
        String name = param.getName();
        String value = param.getValue();
        List<String> zones = param.getZones();
        List<String> servers = param.getServers();
        List<String> tenants = param.getTenants();
        boolean allTenants = param.getAllTenants() != null && param.getAllTenants();

        List<SetObParameterItem> zoneOrServerItems;
        if (CollectionUtils.isNotEmpty(zones)) {
            zoneOrServerItems = zones.stream()
                    .map(zone -> new SetObParameterItem(SetObParameterItemType.ZONE, zone))
                    .collect(Collectors.toList());
        } else if (CollectionUtils.isNotEmpty(servers)) {
            zoneOrServerItems = servers.stream()
                    .map(server -> new SetObParameterItem(SetObParameterItemType.SERVER, server))
                    .collect(Collectors.toList());
        } else {
            zoneOrServerItems = Collections.singletonList(new SetObParameterItem(SetObParameterItemType.EMPTY, null));
        }

        List<SetObParameterItem> tenantItems;
        if (allTenants) {
            tenantItems = Collections.singletonList(new SetObParameterItem(SetObParameterItemType.ALL_TENANT, null));
        } else if (CollectionUtils.isNotEmpty(tenants)) {
            tenantItems = tenants.stream()
                    .map(tenant -> new SetObParameterItem(SetObParameterItemType.TENANT, tenant))
                    .collect(Collectors.toList());
        } else {
            tenantItems = Collections.singletonList(new SetObParameterItem(SetObParameterItemType.EMPTY, null));
        }

        return com.oceanbase.ocp.common.util.ListUtils.cartesianProduct(zoneOrServerItems, tenantItems)
                .stream()
                .map(pair -> buildSetObParameter(name, value, pair.getLeft(), pair.getRight()))
                .collect(Collectors.toList());
    }

    private SetObParameter buildSetObParameter(String name, String value, SetObParameterItem zoneOrServerItem,
            SetObParameterItem tenantItem) {
        SetObParameterBuilder builder = SetObParameter.builder();
        builder = builder.name(name).value(value);
        if (zoneOrServerItem.type == SetObParameterItemType.ZONE) {
            builder = builder.zone(zoneOrServerItem.value);
        } else if (zoneOrServerItem.type == SetObParameterItemType.SERVER) {
            builder = builder.server(zoneOrServerItem.value);
        }
        if (tenantItem.type == SetObParameterItemType.ALL_TENANT) {
            builder = builder.allTenant(true);
        } else if (tenantItem.type == SetObParameterItemType.TENANT) {
            builder = builder.tenant(tenantItem.value);
        }
        return builder.build();
    }

    private enum SetObParameterItemType {
        EMPTY,
        ZONE,
        SERVER,
        TENANT,
        ALL_TENANT,
    }

    @AllArgsConstructor
    private static class SetObParameterItem {

        SetObParameterItemType type;
        String value;
    }
}
