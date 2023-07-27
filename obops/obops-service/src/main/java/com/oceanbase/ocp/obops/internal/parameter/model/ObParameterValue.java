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

package com.oceanbase.ocp.obops.internal.parameter.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.oceanbase.ocp.obops.parameter.model.ServerParameterValue;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObParameter;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ObParameterValue {

    List<String> values;

    List<ServerParameterValue> serverValues;

    List<ObParameter> obParameters;

    public ObParameterValue(List<ObParameter> obParameters) {
        Validate.notEmpty(obParameters, "obParameters should not be empty");
        this.obParameters = obParameters;
        this.values = obParameters.stream()
                .map(ObParameter::getValue)
                .distinct()
                .collect(Collectors.toList());
        this.serverValues = obParameters.stream()
                .map(p -> new ServerParameterValue(p.getSvrIp(), p.getSvrPort(), p.getValue()))
                .collect(Collectors.toList());
    }

    public static Optional<ObParameterValue> fromObParameters(List<ObParameter> obParameters) {
        if (CollectionUtils.isEmpty(obParameters)) {
            return Optional.empty();
        }
        return Optional.of(new ObParameterValue(obParameters));
    }

    @JsonIgnore
    public String getOneValue() {
        Validate.notEmpty(values, "values should not be empty");
        return values.get(0);
    }
}
