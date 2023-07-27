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

package com.oceanbase.ocp.bootstrap.core.def;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.oceanbase.ocp.bootstrap.core.Keys;

import lombok.Data;

@Data
public class Migration {

    private final String name;
    private String sourceSql;
    private String targetTable;
    private boolean ignoreSourceSqlError;
    private List<String> onDuplicateUpdate;
    private String condition;
    private Map<String, String> with;
    private String expr;
    private List<String> deleteBy;
    private List<String> updateBy;
    private List<String> rawSqls;
    private String since;
    private boolean isDelayed;

    @SuppressWarnings("unchecked")
    public static Migration fromConfig(String name, Map<String, Object> items) {
        String sourceSql = (String) items.get(Keys.SOURCE_SQL);
        boolean ignoreSourceSqlError = (Boolean) items.getOrDefault(Keys.IGNORE_SOURCE_SQL_ERROR, Boolean.TRUE);

        String targetTable = (String) items.get(Keys.TARGET_TABLE);
        Map<String, String> with = (Map<String, String>) items.get(Keys.WITH);
        String condition = (String) items.get(Keys.CONDITION);
        String expr = (String) items.get(Keys.EXPR);
        String since = (String) items.get(Keys.SINCE);
        boolean delayed = (Boolean) items.getOrDefault(Keys.DELAY, Boolean.FALSE);

        List<String> onDuplicateUpdate = (List<String>) items.get(Keys.ON_DUPLICATE_UPDATE);
        List<String> deleteBy = (List<String>) items.get(Keys.DELETE_BY);
        List<String> updateBy = (List<String>) items.get(Keys.UPDATE_BY);
        List<String> rawSqls = (List<String>) items.get(Keys.RAW_SQLS);
        if (countNotNull(onDuplicateUpdate, deleteBy, updateBy, rawSqls) > 1) {
            throw new IllegalArgumentException("on_duplicate_update, delete_by, update_by, raw_sqls is exclusive");
        }
        if (countNotNull(sourceSql, rawSqls) != 1) {
            throw new IllegalArgumentException("must use one of source_sql or raw_sqls");
        }
        if (rawSqls != null && countNotNull(targetTable, expr) > 0) {
            throw new IllegalArgumentException("target_table and expr is invalid for raw_sqls");
        }

        Migration ret = new Migration(name);
        ret.setSourceSql(sourceSql);
        ret.setTargetTable(targetTable);
        ret.setSince(since);
        ret.setDelayed(delayed);
        ret.setCondition(condition);
        ret.setIgnoreSourceSqlError(ignoreSourceSqlError);
        ret.setWith(with);
        ret.setExpr(expr);
        ret.setOnDuplicateUpdate(onDuplicateUpdate);
        ret.setDeleteBy(deleteBy);
        ret.setUpdateBy(updateBy);
        ret.setRawSqls(rawSqls);
        return ret;
    }

    private static int countNotNull(Object... objects) {
        int n = 0;
        for (Object obj : objects) {
            if (obj != null) {
                n++;
            }
        }
        return n;
    }

    public Map<String, Object> toConfigItems() {
        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put(Keys.SOURCE_SQL, sourceSql);
        ret.put(Keys.IGNORE_SOURCE_SQL_ERROR, ignoreSourceSqlError);
        ret.put(Keys.TARGET_TABLE, targetTable);
        if (condition != null) {
            ret.put(Keys.CONDITION, condition);
        }
        if (onDuplicateUpdate != null) {
            ret.put(Keys.ON_DUPLICATE_UPDATE, onDuplicateUpdate);
        }
        if (with != null) {
            ret.put(Keys.WITH, with);
        }
        if (expr != null) {
            ret.put(Keys.EXPR, expr);
        }
        if (deleteBy != null) {
            ret.put(Keys.DELETE_BY, true);
        }
        if (updateBy != null) {
            ret.put(Keys.UPDATE_BY, updateBy);
        }
        if (rawSqls != null) {
            ret.put(Keys.RAW_SQLS, rawSqls);
        }
        if (since != null) {
            ret.put(Keys.SINCE, since);
        }
        if (isDelayed) {
            ret.put(Keys.DELAY, true);
        }
        return ret;
    }
}
