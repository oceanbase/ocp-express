package com.oceanbase.ocp.obsdk.accessor.object;

import static com.oceanbase.ocp.obsdk.accessor.object.MysqlObjectAccessor.FindRangePartitionName;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.RowMapper;

import com.oceanbase.ocp.obsdk.accessor.object.model.AlterTableInput;
import com.oceanbase.ocp.obsdk.accessor.object.model.AlterTableInput.AddRangePartition;
import com.oceanbase.ocp.obsdk.accessor.object.model.ObTablePartition;
import com.oceanbase.ocp.obsdk.connector.ConnectProperties;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;

@RunWith(MockitoJUnitRunner.class)
public class MysqlObjectAccessorTest {

    private static ObConnectTemplate template;

    @Before
    public void setUp() throws Exception {
        template = mock(ObConnectTemplate.class);

        ConnectProperties connectProperties = ConnectProperties.builder().build();
        when(template.getConnectProperties()).thenReturn(connectProperties);
    }

    @Test
    public void test() {
        String createTableSql = "CREATE TABLE `metric_data_second` (\n"
                + "  `series_id` bigint(20) NOT NULL,\n"
                + "  `timestamp` bigint(20) NOT NULL,\n"
                + "  `data` varbinary(65535) NOT NULL,\n"
                + "  `interval` tinyint(4) DEFAULT '1' COMMENT '秒级别监控采集间隔',\n"
                + "  PRIMARY KEY (`series_id`, `timestamp`)\n"
                + ") DEFAULT CHARSET = utf8mb4 ROW_FORMAT = DYNAMIC COMPRESSION = 'zstd_1.3.8' REPLICA_NUM = 1 BLOCK_SIZE"
                + " = 16384 USE_BLOOM_FILTER = FALSE TABLET_SIZE = 134217728 PCTFREE = 0\n"
                + " partition by range columns(`timestamp`) subpartition by hash(series_id) subpartition template (\n"
                + "subpartition p0,\n"
                + "subpartition p1)\n"
                + "(partition `DUMMY` values less than (0))";
        Matcher matcher = FindRangePartitionName.matcher(createTableSql);
        List<ObTablePartition> partitions = new ArrayList<>();
        while (matcher.find()) {
            String partitionName = matcher.group(1);
            partitions.add(new ObTablePartition(partitionName));
        }
        assertNotNull(partitions);
        assertEquals("DUMMY", partitions.get(0).getName());
    }

    @Test
    public void alterTable_dropPartition() {
        MysqlObjectAccessor accessor = new MysqlObjectAccessor(template);

        AlterTableInput input = AlterTableInput.builder()
                .table("table1")
                .dropPartitions(Arrays.asList("p1", "p2", "p3"))
                .build();
        accessor.alterTable(input);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).execute(sqlCaptor.capture());
        String sql = sqlCaptor.getValue();
        assertThat(sql, containsString("p1,p2,p3"));
        assertThat(sql, containsString("drop partition"));
    }

    @Test
    public void alterTable_addPartition() {
        MysqlObjectAccessor accessor = new MysqlObjectAccessor(template);

        AlterTableInput input = AlterTableInput.builder()
                .table("table1")
                .addPartitions(Arrays.asList(AddRangePartition.builder().partition("p1").highValue("10000").build(),
                        AddRangePartition.builder().partition("p2").highValue("20000").build()))
                .build();
        accessor.alterTable(input);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).execute(sqlCaptor.capture());
        String sql = sqlCaptor.getValue();
        System.out.println(sql);
        assertThat(sql, containsString("partition p1 values less than (10000)"));
        assertThat(sql, containsString("partition p2 values less than (20000)"));
    }

    @Test
    public void listTablePartition() {
        MysqlObjectAccessor accessor = new MysqlObjectAccessor(template);

        String createTableSql = "CREATE TABLE `table1` (  `end_interval_time` bigint NOT NULL,"
                + "  PRIMARY KEY (`end_interval_time`)"
                + ")"
                + " partition by range(end_interval_time)\n"
                + "(partition DUMMY values less than (0),\n"
                + "partition P202206 values less than (1656633600000000),\n"
                + "partition P202207 values less than (1659312000000000),\n"
                + "partition P202208 values less than (1661990400000000),\n"
                + "partition P202209 values less than (1664582400000000),\n"
                + "partition P202210 values less than (1667260800000000));";
        when(template.queryForObject(anyString(), any(RowMapper.class))).thenReturn(createTableSql);

        List<ObTablePartition> partitions = accessor.listTablePartition("table1");
        assertEquals(6, partitions.size());
    }
}
