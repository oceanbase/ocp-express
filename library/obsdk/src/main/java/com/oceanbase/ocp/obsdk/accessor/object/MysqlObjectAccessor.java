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

package com.oceanbase.ocp.obsdk.accessor.object;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.oceanbase.ocp.obsdk.accessor.ObjectAccessor;
import com.oceanbase.ocp.obsdk.accessor.database.model.ObDatabase;
import com.oceanbase.ocp.obsdk.accessor.object.model.AlterTableInput;
import com.oceanbase.ocp.obsdk.accessor.object.model.ObTable;
import com.oceanbase.ocp.obsdk.accessor.object.model.ObTablePartition;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.util.DatabaseUtils;

public class MysqlObjectAccessor implements ObjectAccessor {

    private static final String GET_TABLE =
            "select table_schema databaseName, table_name tableName, create_time gmtCreate " +
                    "from information_schema.tables where table_schema='%s' and table_name='%s' and table_type='BASE TABLE'";
    private static final String SHOW_CREATE_TABLE = "show create table %s";
    private static final String ALTER_TABLE_DROP_PARTITION = "alter table `%s` drop partition (%s)";
    private static final String ALTER_TABLE_ADD_PARTITION = "alter table `%s` add partition(%s)";
    private static final String ALTER_TABLE_ADD_PARTITION_PART = "partition %s values less than (%s)";

    static Pattern FindRangePartitionName =
            Pattern.compile(".*partition\\s`?([a-z0-9]{1,60})`?\\svalues\\sless\\sthan.*", Pattern.CASE_INSENSITIVE);

    private final ObConnectTemplate connectTemplate;

    public MysqlObjectAccessor(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public List<ObDatabase> listDatabases() {
        return DatabaseUtils.listDatabases(connectTemplate);
    }

    @Override
    public ObTable getTable(String tableName) {
        String sql = String.format(GET_TABLE, connectTemplate.getConnectProperties().getDatabase(), tableName);
        List<ObTable> list = connectTemplate.query(sql, new BeanPropertyRowMapper<>(ObTable.class));
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public ObTable alterTable(AlterTableInput input) {
        if (input.getDropPartitions() != null && input.getDropPartitions().size() > 0) {
            String sql = String.format(ALTER_TABLE_DROP_PARTITION, input.getTable(),
                    String.join(",", input.getDropPartitions()));
            connectTemplate.execute(sql);
        } else if (input.getAddPartitions() != null && input.getAddPartitions().size() > 0) {
            String part = input.getAddPartitions().stream()
                    .map(e -> String.format(ALTER_TABLE_ADD_PARTITION_PART, e.getPartition(),
                            String.join(",", e.getHighValues())))
                    .collect(Collectors.joining(","));

            String sql = String.format(ALTER_TABLE_ADD_PARTITION, input.getTable(), part);
            connectTemplate.execute(sql);
        }
        return getTable(input.getTable());
    }

    private String showCreateTable(String tableName) {
        String sql = String.format(SHOW_CREATE_TABLE, tableName);
        return connectTemplate.queryForObject(sql, (rs, rowNum) -> rs.getString(2));
    }

    @Override
    public List<ObTablePartition> listTablePartition(String tableName) {
        String createTableSql = showCreateTable(tableName);
        Matcher matcher = FindRangePartitionName.matcher(createTableSql);
        List<ObTablePartition> partitions = new ArrayList<>();
        while (matcher.find()) {
            String partitionName = matcher.group(1);
            partitions.add(new ObTablePartition(partitionName));
        }
        return partitions;
    }
}
