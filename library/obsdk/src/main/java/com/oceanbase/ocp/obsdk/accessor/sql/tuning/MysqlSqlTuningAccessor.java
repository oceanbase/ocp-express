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

package com.oceanbase.ocp.obsdk.accessor.sql.tuning;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.oceanbase.ocp.common.util.time.TimeUtils;
import com.oceanbase.ocp.obsdk.accessor.sql.tuning.model.ObOutline;
import com.oceanbase.ocp.obsdk.accessor.sql.tuning.model.ObOutline.ObOutlineBuilder;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MysqlSqlTuningAccessor extends AbstractSqlTuningAccessor {

    private static final String CREATE_OUTLINE_USING_SQL_ID = "CREATE OUTLINE `%s` ON '%s' USING HINT %s";

    private static final String DROP_OUTLINE = "DROP OUTLINE `%s`.`%s`";

    private static final String SELECT_ALL_CONCURRENT_LIMIT_OUTLINE_WITH_CREATE_TIME =
            "select outline_name as outlineName,outline_id, outline_content, database_id, database_name, visible_signature, sql_text, "
                    + "concurrent_num, limit_target, create_time as gmt_create, modify_time"
                    + " from DBA_OB_CONCURRENT_LIMIT_SQL";

    private static final String SELECT_ALL_OUTLINE = "select database_id, tenant_id, "
            + "    outline_id, outline_name as outlineName, "
            + "    outline_content, sql_text, sql_id, "
            + "outline_target, create_time as gmt_create, modify_time "
            + " from DBA_OB_OUTLINES";

    public MysqlSqlTuningAccessor(ObConnectTemplate connectTemplate) {
        super(connectTemplate);
    }

    @Override
    protected void doCreateOutline(String databaseName, String outlineName, String sqlId, String hint) {
        Validate.notBlank(databaseName, "DatabaseName is blank");
        Validate.isTrue(connectTemplate.getConnectProperties().getDatabase().equals(databaseName),
                "Database Name not match");
        Validate.notBlank(outlineName, "OutlineName is blank");
        Validate.notBlank(sqlId, "SqlId is blank");
        Validate.notBlank(hint, "Hint is blank");
        connectTemplate.execute(String.format(CREATE_OUTLINE_USING_SQL_ID, outlineName, sqlId, hint));
    }

    @Override
    protected void doDropOutline(String databaseName, String outlineName) {
        Validate.notBlank(databaseName, "DatabaseName is blank");
        Validate.notBlank(outlineName, "OutlineName is blank");
        connectTemplate.execute(String.format(DROP_OUTLINE, databaseName, outlineName));
    }

    @Override
    public List<ObOutline> getAllConcurrentLimitOutline(Long obTenantId) {
        SqlRowSet sqlRowSet =
                connectTemplate.queryForRowSet(SELECT_ALL_CONCURRENT_LIMIT_OUTLINE_WITH_CREATE_TIME, new Object[] {});
        List<ObOutline> res = new ArrayList<>();
        while (sqlRowSet.next()) {
            ObOutlineBuilder builder = ObOutline.builder();
            builder.outlineName(sqlRowSet.getString("outlineName"));
            builder.outlineContent(sqlRowSet.getString("outline_content"));
            builder.obDbId(sqlRowSet.getLong("database_id"));
            builder.outlineId(sqlRowSet.getLong("outline_id"));
            builder.dbName(sqlRowSet.getString("database_name"));
            builder.visibleSignature(sqlRowSet.getString("visible_signature"));
            builder.sqlText(sqlRowSet.getString("sql_text"));
            builder.concurrentNum(sqlRowSet.getLong("concurrent_num"));
            builder.limitTarget(sqlRowSet.getString("limit_target"));
            Optional.ofNullable(sqlRowSet.getTimestamp("gmt_create")).ifPresent(
                    t -> builder.createTime(TimeUtils.toUtc(t.toInstant())));
            res.add(builder.build());
        }
        return res;
    }

    @Override
    public List<ObOutline> getAllOutline(Long obTenantId) {
        SqlRowSet sqlRowSet = connectTemplate.queryForRowSet(SELECT_ALL_OUTLINE, new Object[] {});
        List<ObOutline> res = new ArrayList<>();
        while (sqlRowSet.next()) {
            ObOutlineBuilder builder = ObOutline.builder();
            builder.outlineName(sqlRowSet.getString("outlineName"));
            builder.outlineContent(sqlRowSet.getString("outline_content"));
            builder.sqlText(sqlRowSet.getString("sql_text"));
            builder.outlineTarget(sqlRowSet.getString("outline_target"));
            Optional.ofNullable(sqlRowSet.getObject("sql_id")).ifPresent(sqlId -> {
                if (sqlId instanceof String) {
                    builder.sqlId(sqlRowSet.getString("sql_id"));
                } else {
                    builder.sqlId(new String((byte[]) sqlId));
                }
            });
            Optional.ofNullable(sqlRowSet.getTimestamp("gmt_create")).ifPresent(
                    t -> builder.createTime(TimeUtils.toUtc(t.toInstant())));
            long dbId = sqlRowSet.getLong("database_id");
            long outlineId = sqlRowSet.getLong("outline_id");
            builder.obDbId(dbId);
            builder.outlineId(outlineId);
            res.add(builder.build());
        }
        return res;
    }

}
