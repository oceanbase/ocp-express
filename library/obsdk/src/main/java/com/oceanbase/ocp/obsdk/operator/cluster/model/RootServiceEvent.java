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

package com.oceanbase.ocp.obsdk.operator.cluster.model;

import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

/**
 * Model class for mapping with OB's internal table
 * '__all_rootservice_event_history'.
 */
@Data
public class RootServiceEvent {

    Timestamp gmtCreate;
    String module;
    String event;
    String name1;
    String value1;
    String name2;
    String value2;
    String name3;
    String value3;
    String name4;
    String value4;
    String name5;
    String value5;
    String name6;
    String value6;
    String extraInfo;
    String rsSvrIp;
    String rsSvrPort;

    public boolean isRollbackUnitMigrate() {
        return StringUtils.equalsIgnoreCase(module, "unit")
                && StringUtils.equalsIgnoreCase(event, "finish_migrate_unit")
                && StringUtils.equalsIgnoreCase(value2, "2");
    }

    public boolean isMigrateUnit() {
        return StringUtils.equalsIgnoreCase(module, "unit")
                && StringUtils.equalsIgnoreCase(event, "migrate_unit");
    }
}
