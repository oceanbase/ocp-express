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

package com.oceanbase.ocp.monitor.model.metric;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class MetricLabels extends TreeMap<String, String> {

    public MetricLabels() {
        super(String.CASE_INSENSITIVE_ORDER);
    }

    public MetricLabels addLabel(String labelKey, String labelValue) {
        if (StringUtils.isNotEmpty(labelKey)) {
            super.put(labelKey, labelValue == null ? "" : labelValue);
        }
        return this;
    }

    @JsonIgnore
    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @JsonIgnore
    public String getSeriesKeyPostfix() {
        List<String> keyValues = new LinkedList<>();
        for (Map.Entry<String, String> entry : super.entrySet()) {
            keyValues.add(entry.getKey() + "=" + entry.getValue());
        }
        return String.join("|", keyValues);
    }

}
