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

package com.oceanbase.ocp.bootstrap.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidDataSource;

import com.oceanbase.ocp.bootstrap.core.def.Const;
import com.oceanbase.ocp.bootstrap.core.def.Row;

public class SQLUtils {

    public interface ResultSetConsumer {

        void accept(ResultSet t) throws SQLException;
    }

    public static void query(DataSource dataSource, ResultSetConsumer resultSetConsumer, String sql)
            throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            resultSetConsumer.accept(resultSet);
        }
    }

    public static void query(DataSource dataSource, ResultSetConsumer resultSetConsumer, String sql, Object... args)
            throws SQLException {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 1; i <= args.length; i++) {
                statement.setObject(i, args[i - 1]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSetConsumer.accept(resultSet);
            }
        }
    }

    public static List<Row> queryRows(DataSource dataSource, String sql) throws SQLException {
        List<Row> ret = new ArrayList<>();
        query(dataSource, rs -> resultSetToRows(rs, ret), sql);
        return ret;
    }

    public static List<Row> queryRows(DataSource dataSource, String sql, Object... args) throws SQLException {
        List<Row> ret = new ArrayList<>();
        query(dataSource, rs -> resultSetToRows(rs, ret), sql, args);
        return ret;
    }

    static void resultSetToRows(ResultSet resultSet, List<Row> ret) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (resultSet.next()) {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                map.put(metaData.getColumnLabel(i), resultSet.getObject(i));
            }
            ret.add(new Row(map));
        }
    }

    public static int execute(DataSource dataSource, String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(sql);
            return statement.getUpdateCount();
        }
    }

    public static void executeBatch(DataSource dataSource, Collection<String> sqls) throws SQLException {
        if (sqls.isEmpty()) {
            return;
        }
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            for (String sql : sqls) {
                statement.addBatch(sql);
            }
            statement.executeBatch();
        }
    }

    public static int execute(DataSource dataSource, String sql, Object... args) throws SQLException {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 1; i <= args.length; i++) {
                statement.setObject(i, args[i - 1]);
            }
            statement.execute();
            return statement.getUpdateCount();
        }
    }

    private static final Predicate<String> NAME_TESTER =
            Pattern.compile("^[a-z_]\\w*$", Pattern.CASE_INSENSITIVE).asPredicate();

    public static boolean isValidName(String name) {
        return NAME_TESTER.test(name);
    }


    public static String escape(String s) {
        int length = s.length();
        int newLength = length;
        // first check for characters that might be dangerous and calculate a length of
        // the string that has escapes.
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                case '\"':
                case '\'':
                case '\0':
                    newLength += 1;
                    break;
                default:
            }
        }
        if (length == newLength) {
            // nothing to escape in the string
            return s;
        }
        StringBuilder sb = new StringBuilder(newLength);
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                case '\"':
                case '\'':
                    sb.append("\\").append(c);
                    break;
                case '\0':
                    sb.append("\\0");
                    break;
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public static String valueToString(Object o) {
        if (o == null) {
            return "NULL";
        }
        if (o instanceof Number || o instanceof Boolean) {
            return o.toString();
        }
        if (o instanceof String) {
            return "'" + escape((String) o) + "'";
        }
        if (o instanceof Const) {
            return ((Const) o).getValue();
        }
        if (o instanceof Date) {
            return "'" + new Timestamp(((Date) o).getTime()) + "'";
        }
        if (o instanceof Instant) {
            return "'" + new Timestamp(((Instant) o).toEpochMilli()) + "'";
        }

        throw new IllegalArgumentException("invalid type " + o.getClass().getName());
    }

    public static Object stringToValue(String s) {
        if (s == null) {
            return null;
        }
        if ("NULL".equalsIgnoreCase(s)) {
            return null;
        }
        if (s.toUpperCase().startsWith("CURRENT_TIMESTAMP")) {
            return Const.valueOf(s);
        }
        if ("true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)) {
            return Boolean.parseBoolean(s);
        }
        if (s.startsWith("'") || s.startsWith("\"")) {
            return unescape(s.substring(1, s.length() - 1));
        } else {
            try {
                return NumberFormat.getInstance().parse(s);
            } catch (Exception e) {
                return s;
            }
        }
    }

    public static String unescape(String s) {
        int realLen = s.length();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\\') {
                i++;
                realLen--;
            }
        }
        if (realLen == s.length()) {
            // nothing to do
            return s;
        }
        StringBuilder sb = new StringBuilder(realLen);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                char next = s.charAt(i + 1);
                if (next == '0') {
                    sb.append('\0');
                } else if (next == 'n') {
                    sb.append('\n');
                } else if (next == 'r') {
                    sb.append('\r');
                } else if (next == 't') {
                    sb.append('\t');
                } else if (next == 'b') {
                    sb.append('\b');
                } else if (next == '"' || next == '\'' || next == '\\') {
                    sb.append(next);
                }
                i++;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String wrapBackQuotes(String s) {
        return StringUtils.wrap(s, "`");
    }

    public static DataSource buildDataSource(String address, String database, String username, String password) {
        String url = dataSourceUrl(address, database);
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        dataSource.setValidationQuery("select 1 from dual");
        dataSource.setInitialSize(2);
        dataSource.setMinIdle(2);
        dataSource.setMaxActive(16);
        return dataSource;
    }

    static String dataSourceUrl(String address, String database) {
        String urlTpl = "jdbc:oceanbase://%s/%s?" +
                "useUnicode=true&characterEncoding=UTF8&connectTimeout=2000&socketTimeout=600000&rewriteBatchedStatements=true";
        return String.format(urlTpl, address, database);
    }

}
