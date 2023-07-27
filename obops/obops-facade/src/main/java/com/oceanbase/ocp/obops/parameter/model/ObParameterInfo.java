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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.obsdk.operator.cluster.model.ObParameter;
import com.oceanbase.ocp.obsdk.operator.parameter.model.ObParameterScope;

import lombok.Data;

@Data
public class ObParameterInfo {

    private String name;

    private ObParameterScope scope;

    private String section;

    private String description;

    private Boolean needRestart;

    private Boolean readonly;

    public ObParameterInfo(ObParameterScope scope, ObParameter obParameter, ObParameterStaticInfo info) {
        Validate.notNull(obParameter);
        Validate.notNull(info);
        Validate.isTrue(StringUtils.equals(obParameter.getName(), info.getName()));
        this.name = obParameter.getName();
        this.scope = scope;
        this.section = obParameter.getSection();
        this.description = obParameter.getInfo();
        this.needRestart = obParameter.needRestart();
        this.readonly = info.getReadonly();
    }

    public static ObParameterInfo cluster(ObParameter obParameter, ObParameterStaticInfo info) {
        return new ObParameterInfo(ObParameterScope.CLUSTER, obParameter, info);
    }

    public static ObParameterInfo tenant(ObParameter obParameter, ObParameterStaticInfo info) {
        return new ObParameterInfo(ObParameterScope.TENANT, obParameter, info);
    }
}
