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

package com.oceanbase.ocp.obsdk.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.oceanbase.ocp.obsdk.model.Table;
import com.oceanbase.ocp.obsdk.model.Table.Item;
import com.oceanbase.ocp.obsdk.model.Table.Row;

public class JdbcUtils {

    public static Table extractRowSet(SqlRowSet rowSet) {
        int columnCount = rowSet.getMetaData().getColumnCount();
        List<String> columnNames = IntStream.range(1, columnCount + 1)
                .boxed()
                .map(i -> rowSet.getMetaData().getColumnName(i))
                .collect(Collectors.toList());
        Map<Integer, String> columnNameMap = IntStream.range(1, columnCount + 1)
                .boxed()
                .collect(Collectors.toMap(i -> i, i -> rowSet.getMetaData().getColumnName(i)));

        List<Row> rows = new ArrayList<>();
        while (rowSet.next()) {
            List<Item> items = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = columnNameMap.get(i);
                Object value = rowSet.getObject(i);
                Item item = new Item(columnName, value);
                items.add(item);
            }
            Row row = new Row(items);
            rows.add(row);
        }
        return new Table(columnNames, rows);
    }
}
