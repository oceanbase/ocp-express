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

package com.oceanbase.ocp.obsdk.operator.object;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.operator.ObjectOperator;
import com.oceanbase.ocp.obsdk.operator.resource.model.ObGvDatabase;

public class MysqlObjectOperator implements ObjectOperator {

    private static final String SELECT_CDB_DATABASE =
            "SELECT/*+ QUERY_TIMEOUT(10000000) */ con_id as tenant_id, object_id as database_id, object_name as database_name "
                    + "FROM  oceanbase.cdb_objects where con_id = ? and OBJECT_TYPE = 'DATABASE' ";
    private final ObConnectTemplate connectTemplate;

    public MysqlObjectOperator(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public List<ObGvDatabase> listTenantDatabase(Long obTenantId) {
        return connectTemplate.query(SELECT_CDB_DATABASE, new Object[] {obTenantId},
                new BeanPropertyRowMapper<>(ObGvDatabase.class));
    }
}
