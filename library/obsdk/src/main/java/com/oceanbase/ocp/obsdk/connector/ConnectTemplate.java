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

import static com.oceanbase.ocp.common.util.LogContentUtils.maskSql;
import static com.oceanbase.ocp.common.util.LogContentUtils.maskSqlArgs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.oceanbase.ocp.obsdk.ObSdkContext;
import com.oceanbase.ocp.obsdk.config.Configurations;
import com.oceanbase.ocp.obsdk.enums.CompatibilityMode;
import com.oceanbase.ocp.obsdk.enums.ObTenantMode;
import com.oceanbase.ocp.obsdk.exception.OceanBaseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnectTemplate {

    private static long DEFAULT_OB_QUERY_TIME_OUT = 10_000_000; // us
    private static String SET_OB_QUERY_TIMEOUT = "set ob_query_timeout = ?";
    private static String ALTER_SCHEMA = "ALTER SESSION SET CURRENT_SCHEMA = %s";

    protected ConnectProperties connectProperties;

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ConnectTemplate(ConnectProperties connectProperties) {
        this.connectProperties = connectProperties;
        initJdbcTemplate();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        prepare();
        long startMillis = System.currentTimeMillis();
        try {
            printSql(sql);
            return jdbcTemplate.query(sql, rowMapper);
        } catch (DataAccessException ex) {
            log.error("[obsdk] query failed, sql:[{}], error message:[{}]", sql, ex.getMessage());
            throw customizeException(ex, sql);
        } finally {
            printSlowQuery(startMillis, sql);
        }
    }

    public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) {
        prepare();
        long startMillis = System.currentTimeMillis();
        try {
            printSql(sql, args);
            return jdbcTemplate.query(sql, args, rowMapper);
        } catch (DataAccessException ex) {
            log.error("[obsdk] query failed, sql:[{}], error message:[{}]", sql, ex.getMessage());
            throw customizeException(ex, sql, args);
        } finally {
            printSlowQuery(startMillis, sql);
        }
    }

    public <T> List<T> namedQuery(String sql, SqlParameterSource parameters, RowMapper<T> rowMapper) {
        prepare();
        long startMillis = System.currentTimeMillis();
        try {
            printSql(sql, toArgs(parameters));
            return namedParameterJdbcTemplate.query(sql, parameters, rowMapper);
        } catch (DataAccessException ex) {
            log.error("[obsdk] query failed, sql:[{}], error message:[{}]", sql, ex.getMessage());
            throw customizeException(ex, sql, toArgs(parameters));
        } finally {
            printSlowQuery(startMillis, sql);
        }
    }

    private Object[] toArgs(SqlParameterSource parameters) {
        if (Objects.isNull(parameters)) {
            return null;
        }
        String[] parameterNames = parameters.getParameterNames();
        if (Objects.isNull(parameterNames)) {
            return null;
        }
        return Arrays.stream(parameterNames).map(parameters::getValue).toArray();
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) {
        prepare();
        long startMillis = System.currentTimeMillis();
        try {
            printSql(sql);
            return jdbcTemplate.queryForObject(sql, rowMapper);
        } catch (DataAccessException ex) {
            log.error("[obsdk] queryForObject failed, sql:[{}], error message:[{}]", sql, ex.getMessage());
            throw customizeException(ex, sql);
        } finally {
            printSlowQuery(startMillis, sql);
        }
    }

    public <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) {
        prepare();
        long startMillis = System.currentTimeMillis();
        try {
            printSql(sql, args);
            return jdbcTemplate.queryForObject(sql, args, rowMapper);
        } catch (DataAccessException ex) {
            log.error("[obsdk] queryForObject failed, sql:[{}], error message:[{}]", sql, ex.getMessage());
            throw customizeException(ex, sql, args);
        } finally {
            printSlowQuery(startMillis, sql);
        }
    }

    public <T> T queryForObject(String sql, Class<T> clazz) {
        prepare();
        long startMillis = System.currentTimeMillis();
        try {
            printSql(sql);
            return jdbcTemplate.queryForObject(sql, clazz);
        } catch (DataAccessException ex) {
            log.error("[obsdk] queryForObject failed, sql:[{}], error message:[{}]", sql, ex.getMessage());
            throw customizeException(ex, sql);
        } finally {
            printSlowQuery(startMillis, sql);
        }
    }

    public <T> T queryForObject(String sql, Object[] args, Class<T> clazz) {
        prepare();
        long startMillis = System.currentTimeMillis();
        try {
            printSql(sql);
            return jdbcTemplate.queryForObject(sql, args, clazz);
        } catch (DataAccessException ex) {
            log.error("[obsdk] queryForObject failed, sql:[{}], error message:[{}]", sql, ex.getMessage());
            throw customizeException(ex, sql, args);
        } finally {
            printSlowQuery(startMillis, sql);
        }
    }

    public SqlRowSet queryForRowSet(String sql, Object[] args) {
        prepare();
        long startMillis = System.currentTimeMillis();
        try {
            printSql(sql);
            return jdbcTemplate.queryForRowSet(sql, args);
        } catch (DataAccessException ex) {
            log.error("[obsdk] queryForRowSet failed, sql:[{}], error message:[{}]", sql, ex.getMessage());
            throw customizeException(ex, sql, args);
        } finally {
            printSlowQuery(startMillis, sql);
        }
    }

    public int update(String sql, Object... args) {
        prepare();
        return updateInner(sql, args);
    }

    public int update(Long timeout, String sql, Object... args) {
        prepare();
        try {
            long globalTimeout = Configurations.getOperationGlobalTimeoutMillis() * 1000; // change to us
            long maxTimeout = Math.max(globalTimeout, timeout * 1000 * 1000); // change to us
            updateInner(SET_OB_QUERY_TIMEOUT, maxTimeout);
            return updateInner(sql, args);
        } finally {
            updateInner(SET_OB_QUERY_TIMEOUT, DEFAULT_OB_QUERY_TIME_OUT);
        }
    }

    private int updateInner(String sql, Object... args) {
        long startMillis = System.currentTimeMillis();
        try {
            printSql(sql, args);
            return jdbcTemplate.update(sql, args);
        } catch (DataAccessException ex) {
            log.error("[obsdk] update failed, sql:[{}], error message:[{}]", sql, ex.getMessage());
            throw customizeException(ex, sql, args);
        } finally {
            printSlowQuery(startMillis, sql);
        }
    }

    public void execute(String sql) {
        prepare();
        long startMillis = System.currentTimeMillis();
        try {
            printSql(sql);
            jdbcTemplate.execute(sql);
        } catch (DataAccessException ex) {
            log.error("[obsdk] execute failed, sql:[{}], error message:[{}]", sql, ex.getMessage());
            throw customizeException(ex, sql);
        } finally {
            printSlowQuery(startMillis, sql);
        }
    }

    public void executeWithSchema(String schema, String sql) {
        if (CompatibilityMode.MYSQL.equals(connectProperties.getCompatibilityMode())) {
            throw new IllegalStateException("Not implemented on Mysql Mode");
        }
        if (ObTenantMode.ORACLE.getSuperUser().equalsIgnoreCase(connectProperties.getUsername())
                && ObTenantMode.ORACLE.getSuperUser().equalsIgnoreCase(schema)) {
            execute(sql);
        } else {
            prepare();
            long startMillis = System.currentTimeMillis();
            Connection connection = null;
            try {
                connection = jdbcTemplate.getDataSource().getConnection();
                log.info("[obsdk] {} Alter schema to :{}", connectProperties, schema);
                connection.prepareStatement(String.format(ALTER_SCHEMA, schema)).execute();
                new JdbcTemplate(new SingleConnectionDataSource(connection, false)).execute(sql);
                printSql(sql);
            } catch (SQLException ex) {
                log.error("[obsdk] execute failed, sql:[{}], error message:[{}]", sql, ex.getMessage());
                throw customizeException(ex, sql);
            } finally {
                if (connection != null) {
                    try {
                        log.info("[obsdk] {} Alter schema to :{}", connectProperties,
                                ObTenantMode.ORACLE.getSuperUser());
                        connection.prepareStatement(String.format(ALTER_SCHEMA, ObTenantMode.ORACLE.getSuperUser()))
                                .execute();
                    } catch (SQLException ex) {
                        log.error("[obsdk] Failed to alter schema to sys", ex);
                    }
                }
                close(connection);
                printSlowQuery(startMillis, sql);
            }
        }
    }

    private void initJdbcTemplate() {
        DataSource dataSource = getDataSource();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    private void refreshJdbcTemplate() {
        DataSource dataSource = getDataSource();
        this.jdbcTemplate.setDataSource(dataSource);
        this.namedParameterJdbcTemplate.getJdbcTemplate().setDataSource(dataSource);
    }

    private DataSource getDataSource() {
        ObConnector obConnector = ObConnectors.getObConnector(this.connectProperties);
        return obConnector.dataSource();
    }

    private void prepare() {
        refreshJdbcTemplate();
        resetQueryTimeout();
    }

    private void resetQueryTimeout() {
        Long queryTimeoutMs = ObSdkContext.getQueryTimeout();
        if (queryTimeoutMs == null) {
            setQueryTimeout(DEFAULT_OB_QUERY_TIME_OUT);
        } else {
            setQueryTimeout(queryTimeoutMs * 1000L);
        }
    }

    private void setQueryTimeout(long queryTimeoutUs) {
        long globalTimeout = Configurations.getOperationGlobalTimeoutMillis() * 1000; // change to us
        if (queryTimeoutUs > globalTimeout) {
            queryTimeoutUs = globalTimeout;
        }
        updateInner(SET_OB_QUERY_TIMEOUT, queryTimeoutUs);
    }

    private void printSlowQuery(long startMillis, String sql) {
        if (startMillis == 0) {
            // skip if startMillis not set
            return;
        }
        long endMillis = System.currentTimeMillis();
        long durationMillis = endMillis - startMillis;
        if (durationMillis >= Configurations.getSlowQueryThresholdMillis()) {
            log.info("[obsdk] slow query, durationMillis={}, sql={}", durationMillis, sql);
        }
    }

    private void printSql(String sql) {
        if (Configurations.isSqlLogEnabled()) {
            log.info("[obsdk] sql: {}", maskSql(sql));
        }
    }

    private void printSql(String sql, Object[] args) {
        if (Configurations.isSqlLogEnabled()) {
            log.info("[obsdk] sql: {}, args: {}", maskSql(sql), maskSqlArgs(sql, args));
        }
    }

    private RuntimeException customizeException(DataAccessException ex, String sql, Object[] args) {
        String fullSql = String.format("%s; args:%s", maskSql(sql), StringUtils.join(maskSqlArgs(sql, args), ","));
        return customizeException(ex, fullSql);
    }

    private RuntimeException customizeException(DataAccessException ex, String sql) {
        if (ex.getCause() instanceof SQLException) {
            return customizeException((SQLException) ex.getCause(), sql);
        }
        return ex;
    }

    private RuntimeException customizeException(SQLException ex, String sql) {
        String debugMessage = String.format("SQL [%s]; SQL state [%s]; error code [%s]; message [%s]", maskSql(sql),
                ex.getSQLState(), ex.getErrorCode(), ex.getMessage());
        return new OceanBaseException(ex.getMessage(), ex, ex.getErrorCode(), ex.getSQLState(), debugMessage);
    }

    private void close(Connection x) {
        if (x == null) {
            return;
        }
        try {
            if (x.isClosed()) {
                return;
            }
            x.close();
        } catch (Exception e) {
            log.info("close connection error", e);
        }
    }

}
