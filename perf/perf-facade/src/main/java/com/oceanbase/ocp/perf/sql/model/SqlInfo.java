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

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.obparser.SqlType;
import com.oceanbase.ocp.perf.sql.util.SqlStatUtils;
import com.oceanbase.ocp.perf.sql.util.SqlStatUtils.Table;

import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public class SqlInfo {

    public Long clusterId;

    public String clusterName;

    public Long tenantId;

    public Long serverId;

    public String server;

    public Long dbId;

    public String dbName;

    public Long userId;

    public String userName;

    public String sqlId;

    public String sqlText;

    public volatile String statement;

    public String sqlTextShort;

    public volatile SqlType sqlType;

    public volatile List<Table> tableList;

    public SqlType getSqlType() {
        if (sqlType != null) {
            return sqlType;
        }
        sqlType = SqlStatUtils.safeParseSqlType(this.mode == null ? TenantMode.MYSQL : this.mode, this.sqlText);
        return sqlType;
    }

    public String getStatement() {
        if (StringUtils.isNotBlank(statement)) {
            return statement;
        }
        statement = SqlStatUtils.safeParameterizeSqlLiteral(Optional.ofNullable(this.mode).orElse(TenantMode.MYSQL),
                this.sqlText);
        return statement;
    }

    public Timestamp createTime;

    public volatile Boolean sensitive;

    public TenantMode mode;

    public boolean isSensitive() {
        if (sensitive == null) {
            return sensitive = SqlStatUtils.isSensitive(sqlText);
        }
        return sensitive;
    }

    public boolean isNotSensitive() {
        return !isSensitive();
    }
}
