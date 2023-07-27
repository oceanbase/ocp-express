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

package com.oceanbase.ocp.perf.sql.model;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;

import com.oceanbase.ocp.obsdk.operator.sql.entity.SqlAuditRawStatEntity;
import com.oceanbase.ocp.obsdk.operator.sql.entity.SqlAuditStatBaseEntity;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum SqlWaitEvent {

    /**
     * system internal wait
     */
    SYSTEM_INTERNAL_WAIT("event0WaitTimeUs", "system internal wait"),

    /**
     * mysql response wait client
     */
    MYSQL_RESPONSE_WAIT_CLIENT("event1WaitTimeUs", "mysql response wait client"),

    /**
     * sync rpc
     */
    SYNC_RPC("event2WaitTimeUs", "sync rpc"),

    /**
     * db file data read
     */
    DB_FILE_DATA_READ("event3WaitTimeUs", "db file data read"),

    NONE(null, "none");

    private final String fieldName;

    public final String text;

    private final Field field;

    @SneakyThrows
    SqlWaitEvent(String fieldName, String text) {
        this.fieldName = fieldName;
        this.text = text;
        if (fieldName != null) {
            this.field = SqlAuditStatBaseEntity.class.getDeclaredField(fieldName);
        } else {
            this.field = null;
        }
    }

    @SneakyThrows
    private Long getValue(SqlAuditRawStatEntity e) {
        if (field == null) {
            throw new IllegalStateException();
        }
        field.setAccessible(true);
        return (Long) field.get(e);
    }

    @Nonnull
    public static SqlWaitEvent maxTimeCost(SqlAuditRawStatEntity e) {
        try {
            SqlWaitEvent maxEvent = null;
            Long maxValue = null;
            for (SqlWaitEvent event : SqlWaitEvent.values()) {
                if (event == NONE) {
                    continue;
                }
                Long value = event.getValue(e);
                if (value == null || value == 0) {
                    continue;
                } else if (maxValue == null) {
                    maxEvent = event;
                    maxValue = value;
                } else if (value > maxValue) {
                    maxEvent = event;
                    maxValue = value;
                }
            }
            if (maxEvent == null) {
                return NONE;
            }
            return maxEvent;
        } catch (Exception ex) {
            log.error("Calculate event timing error", ex);
            return SqlWaitEvent.NONE;
        }
    }
}
