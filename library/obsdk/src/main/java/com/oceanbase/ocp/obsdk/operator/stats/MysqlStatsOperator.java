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

package com.oceanbase.ocp.obsdk.operator.stats;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.operator.StatsOperator;
import com.oceanbase.ocp.obsdk.operator.stats.model.ObServerStats;
import com.oceanbase.ocp.obsdk.operator.stats.model.ObUnitStats;

public class MysqlStatsOperator implements StatsOperator {

    private static final String SELECT_GV_OB_SERVERS = "SELECT"
            + " SVR_IP, SVR_PORT, ZONE,"
            + " CPU_CAPACITY, CPU_CAPACITY_MAX, CPU_ASSIGNED, CPU_ASSIGNED_MAX,"
            + " MEM_CAPACITY, MEM_CAPACITY AS MEM_CAPACITY_MAX, MEM_ASSIGNED, MEM_ASSIGNED AS MEM_ASSIGNED_MAX,"
            + " DATA_DISK_CAPACITY AS DISK_CAPACITY, DATA_DISK_IN_USE AS DISK_IN_USE"
            + " FROM oceanbase.GV$OB_SERVERS";

    private static final String SELECT_GV_OB_UNITS_PART = "SELECT"
            + " T1.UNIT_ID, T1.SVR_IP, T1.SVR_PORT, T1.ZONE, T2.TENANT_ID,"
            + " T1.MIN_CPU, T1.MAX_CPU, T1.MIN_MEMORY, T1.MAX_MEMORY,"
            + " T1.MIN_IOPS, T1.MAX_IOPS, T1.IOPS_WEIGHT,"
            + " T1.DATA_DISK_IN_USE, T1.LOG_DISK_IN_USE"
            + " FROM (SELECT"
            + "  UNIT_ID, SVR_IP, SVR_PORT, ZONE,"
            + "  SUM(MIN_CPU) AS MIN_CPU, SUM(MAX_CPU) AS MAX_CPU, SUM(MEMORY_SIZE) AS MIN_MEMORY, SUM(MEMORY_SIZE) AS MAX_MEMORY,"
            + "  SUM(MIN_IOPS) AS MIN_IOPS, SUM(MAX_IOPS) AS MAX_IOPS, SUM(IOPS_WEIGHT) AS IOPS_WEIGHT,"
            + "  SUM(DATA_DISK_IN_USE) AS DATA_DISK_IN_USE, SUM(LOG_DISK_IN_USE) AS LOG_DISK_IN_USE"
            + "  FROM oceanbase.GV$OB_UNITS"
            + "  GROUP BY UNIT_ID, SVR_IP, SVR_PORT"
            + " ) T1"
            + " JOIN oceanbase.DBA_OB_UNITS T2"
            + " ON T1.UNIT_ID = T2.UNIT_ID AND T1.SVR_IP = T2.SVR_IP AND T1.SVR_PORT = T2.SVR_PORT";

    private static final String SELECT_ALL_UNIT_STAT_OB4 = "SELECT"
            + " UNIT_ID, SVR_IP, SVR_PORT, ZONE, TENANT_ID,"
            + " MIN_CPU, MAX_CPU, MIN_MEMORY, MAX_MEMORY, MIN_IOPS, MAX_IOPS, IOPS_WEIGHT, DATA_DISK_IN_USE, LOG_DISK_IN_USE "
            + " FROM (" + SELECT_GV_OB_UNITS_PART + ")";
    private final ObConnectTemplate connectTemplate;

    public MysqlStatsOperator(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public List<ObServerStats> allServerStats() {
        return connectTemplate.query(SELECT_GV_OB_SERVERS, new BeanPropertyRowMapper<>(ObServerStats.class));
    }

    @Override
    public List<ObUnitStats> allUnitStats() {
        return connectTemplate.query(SELECT_ALL_UNIT_STAT_OB4, new BeanPropertyRowMapper<>(ObUnitStats.class));
    }
}
