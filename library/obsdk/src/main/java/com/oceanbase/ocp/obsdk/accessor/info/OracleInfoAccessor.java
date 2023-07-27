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

package com.oceanbase.ocp.obsdk.accessor.info;

import java.sql.Timestamp;

import com.oceanbase.ocp.obsdk.accessor.InfoAccessor;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;

public class OracleInfoAccessor implements InfoAccessor {

    private static final String SELECT_CURRENT_TIMESTAMP = "select current_timestamp() from dual";

    private final ObConnectTemplate connectTemplate;

    public OracleInfoAccessor(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public Timestamp now() {
        return connectTemplate.queryForObject(SELECT_CURRENT_TIMESTAMP, (rs, n) -> rs.getTimestamp(1));
    }
}
