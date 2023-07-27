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
import java.util.stream.Collectors;

import com.oceanbase.ocp.bootstrap.core.Keys;

import lombok.Data;

@Data
public class DataDefinition {

    private final String name;
    private final String tableName;
    private List<String> onDuplicateUpdate;
    private List<Row> rows;
    private List<Row> delete;

    @SuppressWarnings("unchecked")
    public static DataDefinition fromConfig(String name, Map<String, Object> items) {
        String tableName = (String) items.get(Keys.TABLE_NAME);
        DataDefinition ret = new DataDefinition(name, tableName);
        ret.setOnDuplicateUpdate((List<String>) items.get(Keys.ON_DUPLICATE_UPDATE));
        List<Map<String, Object>> rowMaps = (List<Map<String, Object>>) items.get(Keys.ROWS);
        if (rowMaps != null) {
            ret.setRows(rowMaps.stream().map(Row::new).collect(Collectors.toList()));
        }
        List<Map<String, Object>> deleteMaps = (List<Map<String, Object>>) items.get(Keys.DELETE);
        if (deleteMaps != null) {
            ret.setDelete(deleteMaps.stream().map(Row::new).collect(Collectors.toList()));
        }
        return ret;
    }

    public Map<String, Object> toConfigItems() {
        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put(Keys.TABLE_NAME, tableName);
        ret.put(Keys.ON_DUPLICATE_UPDATE, onDuplicateUpdate);
        if (rows != null) {
            ret.put(Keys.ROWS, rows.stream().map(Row::getData).collect(Collectors.toList()));
        }
        if (delete != null) {
            ret.put(Keys.DELETE, delete.stream().map(Row::getData).collect(Collectors.toList()));
        }
        return ret;
    }
}
