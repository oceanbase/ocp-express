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

import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.obsdk.operator.cluster.model.ObParameter;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClusterParameter {

    private String name;

    private ClusterParameterType parameterType;

    private ClusterParameterValue currentValue;

    private String section;

    private String description;

    private Boolean needRestart;

    private Boolean readonly;

    public ClusterParameter(String name, ClusterParameterType type, List<ObParameter> obParameters,
            ObParameterStaticInfo info) {
        Validate.notEmpty(obParameters);
        Validate.notNull(info);

        this.name = name;
        this.parameterType = type;
        this.currentValue = new ClusterParameterValue(type, obParameters);

        ObParameterInfo obParameterInfo = new ObParameterInfo(type.toObParameterScope(), obParameters.get(0), info);
        fillParamInfo(obParameterInfo);
    }

    private void fillParamInfo(ObParameterInfo info) {
        this.section = info.getSection();
        this.description = info.getDescription();
        this.needRestart = info.getNeedRestart();
        this.readonly = info.getReadonly();
    }
}
