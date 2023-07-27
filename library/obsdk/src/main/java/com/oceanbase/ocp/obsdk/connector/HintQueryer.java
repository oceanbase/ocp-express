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

package com.oceanbase.ocp.obsdk.connector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.RowMapper;

import com.oceanbase.ocp.common.sql.Hint;
import com.oceanbase.ocp.common.sql.Hints;
import com.oceanbase.ocp.common.util.sql.SqlHintUtils;
import com.oceanbase.ocp.obsdk.util.ObSdkUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HintQueryer {

    private final ObConnectTemplate obConnectTemplate;
    private final String obVersion;

    private List<Hint> hints = new ArrayList<>();

    public HintQueryer(ObConnectTemplate obConnectTemplate) {
        this.obConnectTemplate = obConnectTemplate;
        this.obVersion = obConnectTemplate.getObVersion();
    }

    public HintQueryer weakRead() {
        if (ObSdkUtils.versionBefore(obVersion, "4.0.0.0")) {
            hints.add(Hints.weakRead());
        }
        return this;
    }

    public HintQueryer timeout(long timeoutMillis) {
        hints.add(Hints.timeout(timeoutMillis));
        return this;
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return obConnectTemplate.query(transform(sql), rowMapper);
    }

    public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) {
        return obConnectTemplate.query(transform(sql), args, rowMapper);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) {
        return obConnectTemplate.queryForObject(transform(sql), rowMapper);
    }

    public <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) {
        return obConnectTemplate.queryForObject(transform(sql), args, rowMapper);
    }

    public <T> T queryForObject(String sql, Class<T> clazz) {
        String realSql = transform(sql);
        return obConnectTemplate.queryForObject(realSql, clazz);
    }

    private String transform(String sql) {
        if (CollectionUtils.isEmpty(hints)) {
            return sql;
        }
        String realSql = SqlHintUtils.addHints(sql, hints);
        log.info("add hints for sql, hints={}, after={}", hints, realSql);
        return realSql;
    }
}
