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
package com.oceanbase.ocp.monitor.query.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.monitor.param.OcpPrometheusLabel;
import com.oceanbase.ocp.monitor.util.MonitorPropertyUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueryParamAdapter {

    private final String expression;
    private final Map<String, String> labelMap;
    private final long interval;
    private final List<String> gbLabels;
    private final long startTime;
    private final long endTime;
    private final long minStep;

    private static final Long SECOND_IN_HOUR = 3600L;

    private final List<OcpPrometheusLabel> prometheusLabels;

    private final long maxPoints;

    public QueryParamAdapter(String expression, Map<String, String> labelMap, List<OcpPrometheusLabel> prometheusLabels,
            long interval, List<String> gbLabels, long startTime, long endTime, long minStep,
            Long maxPoints) {
        this.expression = expression;
        this.labelMap = labelMap;
        this.prometheusLabels = prometheusLabels;
        this.interval = interval;
        this.gbLabels = gbLabels;
        this.startTime = startTime;
        this.endTime = endTime;
        this.minStep = minStep;
        this.maxPoints = maxPoints;
    }

    public AdaptedParams adapt() {
        long adaptedStep = getAdaptedStep();
        long adaptedInterval = MonitorPropertyUtils.getAdaptedIntervalOrStep(interval);
        if (adaptedInterval < adaptedStep) {
            adaptedInterval = adaptedStep;
        }
        String expression = getFilledExpression(adaptedInterval);
        long startTime = getStartTime(adaptedStep);
        return new AdaptedParams(expression, startTime, endTime, adaptedStep);
    }

    private long getAdaptedStep() {
        long adaptedStep;
        long desireStep = MonitorPropertyUtils.getAdaptedIntervalOrStep((endTime - startTime) / maxPoints);
        if (desireStep < SECOND_IN_HOUR) {
            adaptedStep = desireStep > 30 && desireStep % 60 != 0 ? desireStep + 60 - desireStep % 60 : desireStep;
        } else {
            adaptedStep = desireStep > SECOND_IN_HOUR / 2 && desireStep % SECOND_IN_HOUR != 0
                    ? desireStep + SECOND_IN_HOUR - desireStep % SECOND_IN_HOUR
                    : desireStep;
        }
        adaptedStep = Math.max(adaptedStep, this.minStep);
        if (adaptedStep != MonitorPropertyUtils.getStandardSecondCollectInterval()) {
            log.info("using adapt step {} for expression: {}", adaptedStep, expression);
        }
        return adaptedStep;
    }

    private long getStartTime(long step) {
        long adaptedStartTime = startTime % step == 0 ? startTime : startTime + step - startTime % step;
        if (adaptedStartTime != startTime) {
            log.debug("align startTime, given={}, alignTo={}", startTime, adaptedStartTime);
        }
        Validate.isTrue(adaptedStartTime <= endTime,
                String.format("startTime cannot after endTime, startTime=%d, alignedStartTime=%d, endTime=%d",
                        startTime, adaptedStartTime, endTime));
        return adaptedStartTime;
    }

    private String getLabelStr() {
        List<String> labels = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(prometheusLabels)) {
            for (OcpPrometheusLabel pLabel : prometheusLabels) {
                labels.add(String.format("%s%s\"%s\"", pLabel.getLabelKey(), pLabel.getLabelMatch().getValue(),
                        pLabel.getLabelValue()));
            }
        } else {
            for (String k : labelMap.keySet()) {
                labels.add(String.format("%s=\"%s\"", k, labelMap.get(k)));
            }
        }
        return String.join(",", labels);
    }

    private String getFilledExpression(long adaptedInterval) {
        String labelStr = getLabelStr();
        String filledExpression;
        if (StringUtils.isNotEmpty(labelStr)) {
            filledExpression = expression.replace("@LABELS", labelStr);
        } else {
            filledExpression = expression.replace("{@LABELS}", "")
                    .replace(",@LABELS}", "}")
                    .replace(", @LABELS}", "}");
        }
        String gbLabelStr = String.join(",", gbLabels);
        return filledExpression.replace("@GBLABELS", gbLabelStr).replace("@INTERVAL",
                Long.toString(adaptedInterval));
    }

    @Getter
    public static final class AdaptedParams {

        private final String expression;

        private final long startTime;

        private final long endTime;

        private final long step;

        public AdaptedParams(String expression, long startTime, long endTime, long step) {
            this.expression = expression;
            this.startTime = startTime;
            this.endTime = endTime;
            this.step = step;
        }

    }
}
