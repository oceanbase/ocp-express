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
package com.oceanbase.ocp.monitor.config;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.oceanbase.ocp.monitor.constants.MonitorConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MonitorDataRolloverProperties {

    private final Map<String, Integer> retentionDaysConf;

    public MonitorDataRolloverProperties(
            @Value("#{${ocp.monitor.data.retention-days:{}}}") Map<String, Integer> retentionDaysConf) {
        log.info("Monitor retention conf is {}", retentionDaysConf);
        this.retentionDaysConf = Optional.ofNullable(retentionDaysConf).orElse(Collections.emptyMap());
    }

    public int getSecondDataRetentionDays() {
        return retentionDaysConf.getOrDefault(MonitorConstants.TABLE_NAME_SECOND_DATA, 8);
    }

    public int getMinuteDataRetentionDays() {
        return retentionDaysConf.getOrDefault(MonitorConstants.TABLE_NAME_MINUTE_DATA, 31);
    }

}
