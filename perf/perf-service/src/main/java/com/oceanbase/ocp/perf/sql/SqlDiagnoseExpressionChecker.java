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

package com.oceanbase.ocp.perf.sql;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.oceanbase.ocp.core.el.SimpleExpressionCalculator;
import com.oceanbase.ocp.core.el.exception.ElException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.util.ExceptionUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SqlDiagnoseExpressionChecker {

    private final Map<String, Object> samples = new ConcurrentHashMap<>(8);

    public void checkFilterExpressionIfSampled(String className, String expr) {
        if (samples.containsKey(className)) {
            checkFilterExpression(expr, samples.get(className));
        }
    }

    public void sampleListIfAbsent(String className, List list) {
        if (samples.containsKey(className)) {
            return;
        }
        if (CollectionUtils.isNotEmpty(list)) {
            samples.put(className, list.get(0));
        }
    }

    private void checkFilterExpression(String expr, Object sample) {
        log.info("check filter expressions:{}, with :{}", expr, sample);
        if (StringUtils.isBlank(expr) || sample == null) {
            return;
        }
        Object result = null;
        try {
            result = SimpleExpressionCalculator.evalWithObject(expr, sample);
        } catch (Exception e) {
            handleAndRethrowException(expr, e);
        }
        ExceptionUtils.require(result instanceof Boolean, ErrorCodes.EL_ILLEGAL_BOOLEAN_RESULT, expr, result);
    }

    private void handleAndRethrowException(String expr, Exception e) {
        log.warn("Error in evaluating expr:{}", expr, e);
        if (e instanceof ElException) {
            ((ElException) e).reThrow();
        } else {
            ExceptionUtils.throwException(ErrorCodes.EL_UNEXPECTED_EXCEPTION, expr, e.getMessage());
        }
    }
}
