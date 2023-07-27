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
package com.oceanbase.ocp.monitor.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;

/**
 * MetricTopData
 */
@Validated
public class TopMetricValue extends LabelValues {

    private String metric;
    private Double data;

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public Double getData() {
        return data;
    }

    public void setData(Double data) {
        this.data = data;
    }

    public Map<Object, Object> toMap() {
        Map<Object, Object> result = new HashMap<>(this);
        result.put("metric", this.metric);
        result.put("data", this.data);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    public String getObClusterId() {
        String obClusterId = this.get("ob_cluster_id");
        if (StringUtils.isEmpty(obClusterId)) {
            throw new IllegalArgumentException("No such label in TopMetricValue: ob_cluster_id.");
        }
        return obClusterId;
    }

    public String getTenantName() {
        String tenantName = this.get("tenant_name");
        if (StringUtils.isEmpty(tenantName)) {
            throw new IllegalArgumentException("No such label in TopMetricValue: tenant_name.");
        }
        return tenantName;
    }
}
