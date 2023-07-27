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

package com.oceanbase.ocp.obsdk.operator.sql.execute;

import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.model.Table;
import com.oceanbase.ocp.obsdk.operator.SqlExecuteOperator;
import com.oceanbase.ocp.obsdk.util.JdbcUtils;

public class SqlExecuteOperatorImpl implements SqlExecuteOperator {

    private final ObConnectTemplate connectTemplate;

    public SqlExecuteOperatorImpl(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public Table query(String sql) {
        SqlRowSet rowSet = connectTemplate.queryForRowSet(sql, new Object[] {});
        return JdbcUtils.extractRowSet(rowSet);
    }

    @Override
    public void update(String sql) {
        connectTemplate.update(sql);
    }
}
