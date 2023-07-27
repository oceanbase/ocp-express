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

package com.oceanbase.ocp.executor.internal.constant;


public class AgentApiPath {

    private static final String PREFIX = "/api/v1";

    /**
     * Get OB agent time.
     */
    public static final String GET_AGENT_TIME = PREFIX + "/time";

    /**
     * Restart OB agent.
     */
    public static final String RESTART_AGENT = PREFIX + "/agent/restart";

    /**
     * Get OB agent status.
     */
    public static final String AGENT_STATUS = PREFIX + "/agent/status";

    /**
     * Get host system info.
     */
    public static final String GET_HOST_INFO = PREFIX + "/system/hostInfo";

    /**
     * Get real path of symbolic link.
     */
    public static final String GET_REAL_PATH = PREFIX + "/file/getRealPath";

    /**
     * query agent async task status.
     */
    public static final String GET_TASK_STATUS = PREFIX + "/task/status";

    /**
     * Query log
     */
    public static final String QUERY_LOG = PREFIX + "/log/query";

    /**
     * Download host log
     */
    public static final String DOWNLOAD_LOG = PREFIX + "/log/download";

}
