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

package com.oceanbase.ocp.obops.internal.tenant.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Splitter;

import com.oceanbase.ocp.obops.parameter.param.TenantParameterParam;

public class TenantParameterUtils {

    @SuppressWarnings("UnstableApiUsage")
    public static Map<String, String> contextStringToParams(String contextString) {
        if (StringUtils.isEmpty(contextString)) {
            return Collections.emptyMap();
        }
        return Splitter.on('$').withKeyValueSeparator('=').split(contextString);
    }

    public static String paramsToContextString(List<TenantParameterParam> paramList) {
        return paramList.stream()
                .map(p -> String.format("%s=%s", p.getName(), p.getValue()))
                .collect(Collectors.joining("$"));
    }
}
