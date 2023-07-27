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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import com.oceanbase.ocp.core.obsdk.ObAccessorFactory;
import com.oceanbase.ocp.obops.parameter.model.CompatibleType;
import com.oceanbase.ocp.obops.parameter.model.ObSystemVariableInfo;
import com.oceanbase.ocp.obops.parameter.model.ObSystemVariableStaticInfo;
import com.oceanbase.ocp.obsdk.accessor.ObAccessor;
import com.oceanbase.ocp.obsdk.accessor.VariableAccessor;
import com.oceanbase.ocp.obsdk.accessor.variable.model.ObTenantVariable;
import com.oceanbase.ocp.obsdk.accessor.variable.model.SetVariableInput;
import com.oceanbase.ocp.obsdk.enums.VariableValueType;
import com.oceanbase.ocp.obsdk.exception.OceanBaseException;

@Service
public class ObSystemVariableService {

    @Autowired
    private ObAccessorFactory obAccessorFactory;

    private static final String[] READONLY_VARIABLE_ARRAY = new String[] {
            "plugin_dir",
            "version_comment",
            "ob_tcp_invited_nodes",
            "ob_compatibility_mode",
            "version",
            "system_time_zone",
            "license",
            "character_set_system",
            "lower_case_table_names",
            "datadir",
            "nls_characterset",
    };

    private static final Set<String> READONLY_VARIABLES = Sets.newHashSet(READONLY_VARIABLE_ARRAY);

    private static final String[] ORACLE_ONLY_VARIABLE_ARRAY = new String[] {
            "nls_timestamp_format",
            "nls_nchar_conv_excp",
            "nls_calendar",
            "nls_nchar_characterset",
            "nls_comp",
            "nls_date_language",
            "nls_date_format",
            "nls_length_semantics",
            "nls_characterset",
            "nls_territory",
            "nls_sort",
            "nls_numeric_characters",
            "nls_timestamp_tz_format",
    };

    private static final Set<String> ORACLE_ONLY_VARIABLES = Sets.newHashSet(ORACLE_ONLY_VARIABLE_ARRAY);

    private static final Set<String> MYSQL_ONLY_VARIABLES = Collections.singleton("lower_case_table_names");

    private CompatibleType compatibleType(String name) {
        if (ORACLE_ONLY_VARIABLES.contains(name)) {
            return CompatibleType.ORACLE;
        } else if (MYSQL_ONLY_VARIABLES.contains(name)) {
            return CompatibleType.MYSQL;
        } else {
            return CompatibleType.ALL;
        }
    }

    public ObSystemVariableStaticInfo getObSystemVariableStaticInfo(String name) {
        return ObSystemVariableStaticInfo.builder()
                .name(name)
                .compatibleType(compatibleType(name))
                .readonly(READONLY_VARIABLES.contains(name))
                .build();
    }

    public List<ObSystemVariableInfo> listVariableInfo() {
        // TODO Variables that only exist in Oracle mode are missed.
        ObAccessor accessor = obAccessorFactory.createObAccessor(1L);
        List<ObTenantVariable> variables = accessor.variable().showVariables();
        return variables.stream()
                .map(ObTenantVariable::getVariableName)
                .map(name -> new ObSystemVariableInfo(name, getObSystemVariableStaticInfo(name)))
                .collect(Collectors.toList());
    }

    public void setVariables(Long tenantId, Map<String, String> variables) {
        ObAccessor obAccessor = obAccessorFactory.createObAccessor(tenantId);
        for (Entry<String, String> e : variables.entrySet()) {
            setVariable(obAccessor.variable(), e.getKey(), e.getValue());
        }
    }

    private void setVariable(VariableAccessor variableAccessor, String name, String value) {
        // The type of variable is unknown.
        // Try numeric type first if the value is numeric.
        if (NumberUtils.isCreatable(value)) {
            try {
                setVariable(variableAccessor, name, value, VariableValueType.NUMERIC);
                return;
            } catch (OceanBaseException e) {
                // ignore
            }
        }
        setVariable(variableAccessor, name, value, VariableValueType.STRING);
    }

    private void setVariable(VariableAccessor variableAccessor, String name, String value, VariableValueType type) {
        SetVariableInput input = SetVariableInput.builder().name(name).value(value).type(type).build();
        variableAccessor.setVariable(input);
    }
}
