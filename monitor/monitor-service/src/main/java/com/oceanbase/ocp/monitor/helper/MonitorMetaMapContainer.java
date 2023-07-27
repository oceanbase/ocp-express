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

package com.oceanbase.ocp.monitor.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.oceanbase.ocp.common.util.ExecutorUtils;
import com.oceanbase.ocp.monitor.storage.repository.OcpMetricExprConfigEntity;
import com.oceanbase.ocp.monitor.storage.repository.OcpMetricExprConfigRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MonitorMetaMapContainer {

    private final Map<String, String> metricExprMap = new HashMap<>();

    @Autowired
    private OcpMetricExprConfigRepository metricExprConfigRepository;

    private final ScheduledExecutorService scheduledExecutorService =
            ExecutorUtils.newScheduledPool(1, "schedule-monitor-meta-");

    @PostConstruct
    public void init() {
        // load metric expr, run every 10 minutes
        scheduledExecutorService.scheduleWithFixedDelay(this::loadMetricExpr, 0, 10, TimeUnit.MINUTES);

    }

    public String getExprByMetric(String metric) {
        return metricExprMap.get(metric);
    }

    private void loadMetricExpr() {
        try {
            List<OcpMetricExprConfigEntity> metricExprEntityList = metricExprConfigRepository.findAll();
            Validate.notNull(metricExprEntityList, "Query all metric expr error.");
            metricExprEntityList.stream().filter(meta -> StringUtils.isNotEmpty(meta.getExpr()))
                    .forEach(meta -> metricExprMap.put(meta.getMetric(), meta.getExpr()));
        } catch (Exception e) {
            log.error("loadMetricExpr error.", e);
        }
    }

}
