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

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.oceanbase.ocp.monitor.constants.MonitorConstants;
import com.oceanbase.ocp.monitor.entity.SeriesKeyId;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SeriesKeyIdPersistent {

    @Autowired
    @Qualifier("jdbcTemplate")
    private JdbcTemplate monitorJdbcTemplate;

    @Autowired
    @Qualifier("namedJdbcTemplate")
    private NamedParameterJdbcTemplate monitorNamedJdbcTemplate;

    public Long querySeriesId(String seriesKey) {
        Validate.notEmpty(seriesKey, "seriesKey is null or empty");

        String sql = "select series_id from `metric_series_key_id` where series_key = ?";
        List<Long> seriesIds = monitorJdbcTemplate.queryForList(sql, new Object[] {seriesKey}, Long.class);

        return CollectionUtils.isEmpty(seriesIds) ? null : seriesIds.get(0);
    }

    /**
     * <pre>
     * CREATE TABLE metric_series_key_id(
     * series_id BIGINT,
     * series_key VARCHAR(1024)
     * )
     * </pre>
     */
    public int replaceSeriesKeyId(String seriesKey, Long seriesId) {
        Validate.notNull(seriesId, "seriesId is null");
        Validate.notEmpty(seriesKey, "seriesKey is null or empty");

        String sql = "replace into `metric_series_key_id` (`series_id`, `series_key`) values (?, ?)";

        return monitorJdbcTemplate.update(sql, seriesId, seriesKey);
    }

    /**
     * restore cachedKey when ocp start
     */
    public List<SeriesKeyId> fetchAfterId(long seriesId, int limit) {
        String sql = String.format(
                "SELECT /*+ QUERY_TIMEOUT(%d) */ series_id, series_key FROM `metric_series_key_id` " +
                        "WHERE series_id > %d ORDER BY series_id LIMIT %d",
                MonitorConstants.INITIAL_LOAD_QUERY_TIMEOUT_US, seriesId, limit);
        return monitorJdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SeriesKeyId.class));
    }

}
