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

package com.oceanbase.ocp.monitor.storage;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.oceanbase.ocp.common.util.encode.Converter;
import com.oceanbase.ocp.common.util.encode.DoubleArrayToByteArrayConverter;
import com.oceanbase.ocp.core.util.JdbcTemplateUtils;
import com.oceanbase.ocp.monitor.entity.ValueNodePersistentRow;
import com.oceanbase.ocp.monitor.model.metric.MetricData;
import com.oceanbase.ocp.monitor.model.storage.ValueNode;
import com.oceanbase.ocp.monitor.model.storage.ValueNodeKey;
import com.oceanbase.ocp.monitor.util.TimestampUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricDataDao implements IRollupMetricDataDao {

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    private Converter<double[], byte[]> double2ByteConverter = new DoubleArrayToByteArrayConverter();

    private final String tableName;

    private static final int NODE_INTERVAL_SECOND = 3600;
    private static final int NODE_STEP_SECOND = 60;


    public MetricDataDao(String tableName, JdbcTemplate jdbcTemplate,
            NamedParameterJdbcTemplate namedJdbcTemplate) {
        this.tableName = tableName;
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = namedJdbcTemplate;
    }

    @Override
    public int write(LinkedList<MetricData> dataList) {
        MetricData e;
        List<ValueNode> nodeList = new ArrayList<>();
        while ((e = dataList.poll()) != null) {
            try {
                long seriesId = e.getSeriesId();
                long timestamp = e.getTimestamp();
                long nodeStart = nodeStartFromPos(timestamp);
                ValueNodeKey key = new ValueNodeKey(seriesId, nodeStart);
                ValueNode valueNode = get(key);
                int offset = (int) ((timestamp - nodeStart) / NODE_STEP_SECOND);
                if (valueNode == null || valueNode.isEmptyNode()) {
                    valueNode = new ValueNode(seriesId, nodeStart, 60, NODE_STEP_SECOND);
                }
                valueNode.setValue(offset, e.getValue());
                nodeList.add(valueNode);
            } catch (Throwable throwable) {
                log.warn("Extract minute data failed.", throwable);
            }
        }
        return write(nodeList);
    }

    public ValueNode get(ValueNodeKey key) {
        String sql = String.format(
                "select `series_id`, `timestamp`, `data` from `%s`" + " where series_id = ? AND timestamp = ? ",
                tableName);
        List<ValueNodePersistentRow> rows =
                jdbcTemplate.query(sql, new Object[] {key.getSeriesId(), key.getEpochSecondStart()},
                        new BeanPropertyRowMapper<>(ValueNodePersistentRow.class));
        if (rows.isEmpty()) {
            return null;
        }
        ValueNodePersistentRow row = rows.get(0);
        return convert(row);
    }

    @Override
    public int write(List<ValueNode> nodeList) {
        Validate.notEmpty(nodeList, "nodeList is null or empty");

        String sql = String.format("REPLACE INTO `%s` (`series_id`, `timestamp`, `data`) VALUES (?, ?, ?)", tableName);
        List<Object[]> batchArgs = new ArrayList<>(nodeList.size());
        nodeList.forEach(node -> {
            byte[] bytes = double2ByteConverter.convertToRight(node.getValues());
            batchArgs.add(new Object[] {node.getSeriesId(), node.getEpochSecondStart(), bytes});
        });

        int[] argTypes = {Types.BIGINT, Types.BIGINT, Types.BINARY};
        int[] affectRows = jdbcTemplate.batchUpdate(sql, batchArgs, argTypes);
        return JdbcTemplateUtils.batchInsertAffectRows(affectRows);
    }

    private ValueNode convert(ValueNodePersistentRow row) {
        double[] values = double2ByteConverter.convertToLeft(row.getData());
        return new ValueNode(row.getSeriesId(), row.getTimestamp(), values, row.getInterval());
    }

    private long nodeStartFromPos(long pos) {
        return TimestampUtils.calcStartByInterval(pos, NODE_INTERVAL_SECOND);
    }

    @Override
    public Map<Long, List<ValueNode>> scan(List<Long> seriesIds, Long startTime, Long endTime) {
        Validate.notEmpty(seriesIds, "seriesIds is null or empty");
        Validate.notNull(startTime, "startTime is null");
        Validate.notNull(endTime, "endTime is null");

        String sql = String
                .format("SELECT /*+ READ_CONSISTENCY(WEAK) */ `series_id`, `timestamp`, `data`, `interval` FROM `%s`"
                        + " WHERE series_id IN (:seriesIds) and timestamp >= :startTime and timestamp <= :endTime "
                        + " ORDER BY series_id", tableName);

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("seriesIds", seriesIds);
        parameters.addValue("startTime", startTime);
        parameters.addValue("endTime", endTime);
        return mapToValueNodes(sql, seriesIds.size(), parameters);
    }

    private Map<Long, List<ValueNode>> mapToValueNodes(String sql, int size, MapSqlParameterSource parameters) {
        List<ValueNodePersistentRow> rows =
                namedJdbcTemplate.query(sql, parameters, new BeanPropertyRowMapper<>(ValueNodePersistentRow.class));
        List<ValueNode> nodes = converts(rows);
        Map<Long, List<ValueNode>> seriesId2nodes = new HashMap<>(size);
        for (ValueNode node : nodes) {
            List<ValueNode> currentNodes = seriesId2nodes.computeIfAbsent(node.getSeriesId(), t -> new ArrayList<>());
            currentNodes.add(node);
        }
        return seriesId2nodes;
    }

    private List<ValueNode> converts(List<ValueNodePersistentRow> rows) {
        return rows.stream().map(this::convert).collect(Collectors.toList());
    }

}
