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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import lombok.Data;

@Data
public class OcpPrometheusMeasurement {

    private String metric;
    private String labelStr;
    private Map<String, String> labels = new HashMap<>();

    public int getHash() {
        return Objects.hashCode(this.labelStr);
    }

    public OcpPrometheusMeasurement(String labelStr, Map<String, String> labels) {
        this.metric = "default";
        this.labelStr = labelStr;
        this.labels = labels;
    }

    public OcpPrometheusMeasurement() {
        this.metric = "default";
        this.labelStr = "";
        this.labels = new HashMap<>();
    }

    public OcpPrometheusMeasurement(String measurement) {
        if (null != measurement && !"".equalsIgnoreCase(measurement)) {
            String[] strs = measurement.split("\\|");
            if (strs.length > 1) {
                this.metric = strs[0];
                int index = measurement.indexOf("|");
                this.labelStr = measurement.substring(index + 1);
                for (int i = 1; i < strs.length; i++) {
                    String[] label = strs[i].split("=");
                    String labelKey = label[0];
                    int startIdx = labelKey.length() + 1;
                    if (startIdx <= strs[i].length()) {
                        String labelValue = strs[i].substring(startIdx);
                        labels.put(labelKey, labelValue);
                    }
                }
            }
        } else {
            this.metric = measurement;
        }
    }
}
