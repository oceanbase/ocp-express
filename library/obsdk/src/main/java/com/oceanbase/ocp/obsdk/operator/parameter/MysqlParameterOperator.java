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

package com.oceanbase.ocp.obsdk.operator.parameter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.google.common.collect.Lists;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.operator.ParameterOperator;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObParameter;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObTenantParameter;
import com.oceanbase.ocp.obsdk.operator.parameter.model.SetObParameter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MysqlParameterOperator implements ParameterOperator {

    private static final String SHOW_PARAMETERS_CLUSTER_SCOPE = "SHOW PARAMETERS WHERE scope = 'cluster'";
    private static final String SHOW_PARAMETERS_TENANT_SCOPE = "SHOW PARAMETERS WHERE scope = 'tenant'";

    public static final String SHOW_PARAMETERS_LIKE_CLUSTER_SCOPE =
            "SHOW PARAMETERS WHERE scope = 'cluster' AND name LIKE ? ";
    private static final String SELECT_OB_PARAMETERS_CLUSTER_SCOPE =
            "SELECT ZONE, SVR_IP, SVR_PORT, NAME, VALUE, DATA_TYPE, SECTION, EDIT_LEVEL, INFO FROM GV$OB_PARAMETERS WHERE SCOPE = 'CLUSTER' AND name = ?";

    private static final String SET_PARAMETER = "ALTER SYSTEM SET %s = ?";

    private static final String SELECT_OB_PARAMETERS_TENANT_SCOPE = "SELECT"
            + " t.TENANT_ID, t.TENANT_NAME, p.ZONE, p.SVR_IP, p.SVR_PORT, p.NAME, p.VALUE, p.DATA_TYPE, p.SECTION, p.EDIT_LEVEL, p.INFO"
            + " FROM GV$OB_PARAMETERS p JOIN DBA_OB_TENANTS t"
            + " ON p.TENANT_ID = t.TENANT_ID"
            + " WHERE SCOPE = 'TENANT' AND TENANT_TYPE IN ('SYS', 'USER')";

    private final ObConnectTemplate connectTemplate;

    public MysqlParameterOperator(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public List<ObParameter> listClusterParameters() {
        return connectTemplate.query(SHOW_PARAMETERS_CLUSTER_SCOPE, new BeanPropertyRowMapper<>(ObParameter.class));
    }

    @Override
    public List<ObParameter> listClusterParametersLike(String like) {
        return connectTemplate.query(SHOW_PARAMETERS_LIKE_CLUSTER_SCOPE, new Object[] {like},
                new BeanPropertyRowMapper<>(ObParameter.class));
    }

    @Override
    public List<ObParameter> getClusterParameter(String name) {
        return listClusterParametersLike(name);
    }

    @Override
    public List<ObParameter> getHiddenClusterParameter(String name) {
        return connectTemplate.query(SELECT_OB_PARAMETERS_CLUSTER_SCOPE, new Object[] {name},
                new BeanPropertyRowMapper<>(ObParameter.class));
    }

    @Override
    public List<ObTenantParameter> listTenantParameters() {
        List<ObParameter> parameters =
                connectTemplate.query(SHOW_PARAMETERS_TENANT_SCOPE, new BeanPropertyRowMapper<>(ObParameter.class));
        Set<String> parameterNames = parameters.stream().map(ObParameter::getName).collect(Collectors.toSet());
        List<ObTenantParameter> allParameters =
                connectTemplate.query(SELECT_OB_PARAMETERS_TENANT_SCOPE,
                        new BeanPropertyRowMapper<>(ObTenantParameter.class));
        return allParameters.stream().filter(p -> parameterNames.contains(p.getName())).collect(Collectors.toList());
    }

    @Override
    public void setClusterParameter(SetObParameter input) {
        Validate.notNull(input, "input cannot be null");
        Validate.notEmpty(input.getName(), "input parameter name cannot be empty");
        Validate.notNull(input.getValue(), "input parameter value cannot be null");
        Validate.isTrue(input.getZone() == null || input.getServer() == null,
                "input zone and server cannot be set at the same time");
        Validate.isTrue(input.getTenant() == null, "input tenant should be null when setting cluster parameter");
        Validate.isTrue(input.getAllTenant() == null, "input allTenant should be null when setting cluster parameter");

        setParameter(input);
    }

    @Override
    public void setTenantParameter(SetObParameter input) {
        Validate.notNull(input, "input cannot be null");
        Validate.notEmpty(input.getName(), "input parameter name cannot be empty");
        Validate.notNull(input.getValue(), "input parameter value cannot be null");
        Validate.isTrue(input.getZone() == null || input.getServer() == null,
                "input zone and server cannot be set at the same time");
        boolean allTenant = input.getAllTenant() != null && input.getAllTenant();
        Validate.isTrue(input.getTenant() != null || allTenant, "one of input tenant and allTenant should be set");
        Validate.isTrue(input.getTenant() == null || !allTenant,
                "input tenant and allTenant cannot be set at the same time");
        setParameter(input);
    }

    private void setParameter(SetObParameter input) {
        String sql = String.format(SET_PARAMETER, input.getName());
        List<Object> args = Lists.newArrayList(input.getValue());
        if (input.getZone() != null) {
            sql += " ZONE = ?";
            args.add(input.getZone());
        } else if (input.getServer() != null) {
            sql += " SERVER = ?";
            args.add(input.getServer());
        }
        if (input.getAllTenant() != null && input.getAllTenant()) {
            sql += " TENANT = all";
        } else if (input.getTenant() != null) {
            sql += " TENANT = ?";
            args.add(input.getTenant());
        }
        connectTemplate.update(sql, args.toArray());
    }
}
