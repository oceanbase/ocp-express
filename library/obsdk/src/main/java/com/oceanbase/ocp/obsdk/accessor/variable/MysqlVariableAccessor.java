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

package com.oceanbase.ocp.obsdk.accessor.variable;

import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.oceanbase.ocp.obsdk.accessor.VariableAccessor;
import com.oceanbase.ocp.obsdk.accessor.variable.model.ObTenantVariable;
import com.oceanbase.ocp.obsdk.accessor.variable.model.SetVariableInput;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.enums.VariableValueType;

public class MysqlVariableAccessor implements VariableAccessor {

    private static final String SHOW_VARIABLES = "SHOW GLOBAL VARIABLES";

    private static final String SET_VARIABLE = "SET GLOBAL `%s` = ?";

    private final ObConnectTemplate connectTemplate;

    public MysqlVariableAccessor(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public List<ObTenantVariable> showVariables() {
        return connectTemplate.query(SHOW_VARIABLES, new BeanPropertyRowMapper<>(ObTenantVariable.class));
    }

    @Override
    public void setVariable(SetVariableInput input) {
        String sql = String.format(SET_VARIABLE, input.getName());
        Object[] args = new Object[1];
        if (VariableValueType.INT.equals(input.getType()) || VariableValueType.NUMERIC.equals(input.getType())) {
            args[0] = NumberUtils.createNumber(input.getValue());
        } else {
            args[0] = input.getValue();
        }
        connectTemplate.update(sql, args);
    }
}
