package com.oceanbase.ocp.bootstrap.core.def;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.google.gson.Gson;

import com.oceanbase.ocp.bootstrap.core.def.TableDefinition.Partition;

public class TableDefinitionTest {

    static Gson gson = new Gson();

    @Test
    public void testTableDefinition() {
        String defJson = "{\"fields\":{\"id\":{\"type\":\"bigint(20)\",\"nullable\":false,\"auto_increment\":true},"
                + "\"name\":{\"type\":\"varchar(128)\",\"nullable\":false,\"comment\":\"区域名称\"},"
                + "\"description\":{\"type\":\"varchar(256)\",\"nullable\":true,\"comment\":\"地域描述信息\"}"
                + "},"
                + "\"indexes\":{\"uk_compute_region_name\":{\"fields\":[\"name\"],\"unique\":true,\"local\":false,\"delay\":true}},"
                + "\"primary_key\":{\"fields\":[\"id\"]},"
                + "\"comment\":\"OCP计算资源模块-区域表\","
                + "\"default_charset\":\"utf8mb4\"}";
        LinkedHashMap<String, Object> confMap = gson.fromJson(defJson, LinkedHashMap.class);
        TableDefinition tableDefinition = TableDefinition.fromConfig("test", confMap);
        Map<String, Object> outConfMap = tableDefinition.toConfigItems();
        TableDefinition tableDefinition1 = TableDefinition.fromConfig("test", outConfMap);
        assertEquals(tableDefinition, tableDefinition1);
    }

    @Test
    public void testPartition() {
        assertNull(Partition.fromConfig(null));

        String defJson = "{\n"
                + "    \"type\":\"RANGE\",\n"
                + "    \"fields\":[ \"timestamp\" ],\n"
                + "    \"range_partitions\": [{ \"DUMMY\": 0 }],\n"
                + "    \"subpartition\": { \"type\": \"HASH\", \"hash_partition_count\": 30, \"by_expr\": \"series_id\" }\n"
                + "}";
        LinkedHashMap<String, Object> confMap = gson.fromJson(defJson, LinkedHashMap.class);
        Partition partition = Partition.fromConfig(confMap);
        Map<String, Object> map2 = partition.toConfigItems();
        assertEquals(confMap.get("type"), map2.get("type"));
        assertEquals(confMap.get("fields"), map2.get("fields"));
        assertEquals(confMap.get("range_partitions"), map2.get("range_partitions"));
        assertEquals(((Map) confMap.get("subpartition")).get("type"), ((Map) map2.get("subpartition")).get("type"));
        assertEquals(((Map) confMap.get("subpartition")).get("by_expr"),
                ((Map) map2.get("subpartition")).get("by_expr"));
        assertEquals(((Number) ((Map) confMap.get("subpartition")).get("hash_partition_count")).intValue(),
                ((Number) ((Map) map2.get("subpartition")).get("hash_partition_count")).intValue());
    }

}
