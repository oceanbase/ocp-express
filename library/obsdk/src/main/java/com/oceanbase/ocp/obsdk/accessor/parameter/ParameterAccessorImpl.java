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

package com.oceanbase.ocp.obsdk.accessor.parameter;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.google.common.collect.Lists;

import com.oceanbase.ocp.obsdk.accessor.ParameterAccessor;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObParameter;
import com.oceanbase.ocp.obsdk.operator.parameter.model.SetObParameter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParameterAccessorImpl implements ParameterAccessor {

    private static final String SHOW_PARAMETERS =
            "SHOW PARAMETERS WHERE scope = 'TENANT'";

    private static final String SET_PARAMETER = "ALTER SYSTEM SET %s = ?";

    private final ObConnectTemplate connectTemplate;

    public ParameterAccessorImpl(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public List<ObParameter> listParameters() {
        return connectTemplate.query(SHOW_PARAMETERS, new BeanPropertyRowMapper<>(ObParameter.class));
    }

    @Override
    public void setParameter(SetObParameter input) {
        Validate.notNull(input, "input cannot be null");
        Validate.notEmpty(input.getName(), "input parameter name cannot be empty");
        Validate.notNull(input.getValue(), "input parameter value cannot be null");
        Validate.isTrue(input.getZone() == null || input.getServer() == null,
                "input zone and server cannot be set at the same time");
        Validate.isTrue(input.getTenant() == null, "input tenant should be null in ParameterAccessor");
        Validate.isTrue(input.getAllTenant() == null, "input allTenant should be null in ParameterAccessor");

        String sql = String.format(SET_PARAMETER, input.getName());
        List<Object> args = Lists.newArrayList(input.getValue());
        if (input.getZone() != null) {
            sql += " ZONE = ?";
            args.add(input.getZone());
        } else if (input.getServer() != null) {
            sql += " SERVER = ?";
            args.add(input.getServer());
        }
        connectTemplate.update(sql, args.toArray());
    }
}
