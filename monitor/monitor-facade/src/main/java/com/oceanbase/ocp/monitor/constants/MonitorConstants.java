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
package com.oceanbase.ocp.monitor.constants;

/**
 * Monitor constants
 *
 **/
public class MonitorConstants {

    /**
     * Second-level data storage table
     */
    public static final String TABLE_NAME_SECOND_DATA = "metric_data_second";

    /**
     * Minute-level data storage table
     */
    public static final String TABLE_NAME_MINUTE_DATA = "metric_data_minute";

    public static final int SECOND_READ_CACHE_SIZE = 4;

    public static final long MINUTE_SECONDS = 60L;

    /**
     * 32472144000 means 2999-1-1, if second greater than, <br>
     * means may transfer milliseconds, not seconds
     */
    public static final long MAY_MILLIS_JUDGE_THRESHOLD = 32472144000L;

    /**
     * original metric value must be positive, use -1.0 as invalid value
     */
    public static final double VALUE_NOT_EXIST = -1.0D;

    public static final int PARTITION_CREATE_AHEAD_DAYS = 2;

    /**
     * archive configuration
     */
    public static final int ARCHIVE_MAX_BATCH_SIZE = 1000;

    public static final long INITIAL_LOAD_QUERY_TIMEOUT_US = 100 * 1000 * 1000L;

    public static final long SCAN_SERIES_ID_CACHE_SIZE = 1000L;

    public static final long SCAN_SERIES_ID_CACHE_EXPIRE_MINUTES = 2L;

    /**
     * write queue parameters
     */
    public static final int SECOND_WRITE_QUEUE_CAPACITY = 5_000_000;

}
