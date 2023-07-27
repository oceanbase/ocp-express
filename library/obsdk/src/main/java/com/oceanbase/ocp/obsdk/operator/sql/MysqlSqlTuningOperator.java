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

package com.oceanbase.ocp.obsdk.operator.sql;

import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.oceanbase.ocp.obsdk.accessor.sql.tuning.model.ObOutline;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.operator.SqlTuningOperator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MysqlSqlTuningOperator implements SqlTuningOperator {

    private static final String GET_OUTLINE =
            "select /*+ READ_CONSISTENCY(WEAK) */ name as outlineName, outline_content from __all_virtual_outline where tenant_id =? and database_id=? and sql_id=?";

    private ObConnectTemplate connectTemplate;

    public MysqlSqlTuningOperator(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public ObOutline getOutline(Long obTenantId, Long obDbId, String sqlId) {
        ObOutline result = null;
        try {
            result = connectTemplate.queryForObject(GET_OUTLINE, new Object[] {obTenantId, obDbId, sqlId},
                    new BeanPropertyRowMapper<>(ObOutline.class));
        } catch (Exception e) {
            log.warn("Error in get outline:{}  {} {}", obTenantId, obDbId, sqlId);
        }
        return result;
    }
}
