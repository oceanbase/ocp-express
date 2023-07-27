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

package com.oceanbase.ocp.obsdk.accessor.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import com.oceanbase.ocp.obsdk.accessor.UserAccessor;
import com.oceanbase.ocp.obsdk.accessor.user.model.DbObject;
import com.oceanbase.ocp.obsdk.accessor.user.model.DbPrivilege;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObRole;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObUser;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObjectPrivilege;
import com.oceanbase.ocp.obsdk.accessor.user.model.UserDbPriv;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.enums.DbPrivType;
import com.oceanbase.ocp.obsdk.enums.GlobalPrivilege;
import com.oceanbase.ocp.obsdk.enums.MysqlGlobalPrivilege;
import com.oceanbase.ocp.obsdk.enums.ObjectPrivilegeType;
import com.oceanbase.ocp.obsdk.util.ObSdkUtils;

public class MysqlUserAccessor implements UserAccessor {

    private static final String CREATE_USER = "CREATE USER IF NOT EXISTS ? IDENTIFIED BY ?";
    private static final String DROP_USER = "DROP USER ?";
    private static final String ALTER_PASSWORD = "ALTER USER ? IDENTIFIED BY ?";
    private static final String SELECT_MYSQL_USER_OB4 =
            "SELECT `user`, (CASE `account_locked` WHEN 'Y' THEN 1 ELSE 0 END) AS account_locked, "
                    + "`select_priv`, `insert_priv`, `update_priv`, `delete_priv`, `create_priv`, `drop_priv`, "
                    + "`process_priv`, `grant_priv`, `index_priv`, `alter_priv`, `show_db_priv`, `super_priv`, "
                    + "`create_view_priv`, `show_view_priv`, `create_user_priv`, `password` FROM `mysql`.`user`";
    private static final String SELECT_DB_PRIV =
            "SELECT `db`, `user`, `select_priv`, `insert_priv`, `update_priv`, `delete_priv`, `create_priv`, "
                    + "`drop_priv`, `index_priv`, `alter_priv`, `create_view_priv`, `show_view_priv` FROM `mysql`.`db`";
    private static final String SELECT_DB_PRIV_BY_USER = SELECT_DB_PRIV + " WHERE `user` = ?";
    private static final String GRANT_GLOBAL_PRIVILEGE = "GRANT %s ON *.* TO ?";
    private static final String REVOKE_GLOBAL_PRIVILEGE = "REVOKE %s ON *.* FROM ?";
    private static final String GRANT_DB_PRIVILEGE = "GRANT %s ON `%s`.* TO ?";
    private static final String REVOKE_DB_PRIVILEGE = "REVOKE %s ON `%s`.* FROM ?";
    private static final String LOCK_USER = "ALTER USER ? ACCOUNT LOCK";
    private static final String UNLOCK_USER = "ALTER USER ? ACCOUNT UNLOCK";
    private static final String SHOW_DATABASE = "SHOW DATABASES";

    private final ObConnectTemplate connectTemplate;

    public MysqlUserAccessor(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public void createUser(String username, String password) {
        Validate.notEmpty(username, "input username is empty");
        connectTemplate.update(CREATE_USER, username, password);
    }

    @Override
    public void dropUser(String username) {
        Validate.notEmpty(username, "input username is empty");
        connectTemplate.update(DROP_USER, username);
    }

    @Override
    public List<ObUser> listUsers() {
        List<ObUser> obUserList = listObUsers();
        List<String> dbNames = listDbNames();
        List<UserDbPriv> userDbPrivs =
                connectTemplate.weakRead().query(SELECT_DB_PRIV, (rs, rowNum) -> userDbPrivMapRow(rs));
        Map<String, List<DbPrivilege>> userDbPrivMap = userDbPrivs.stream()
                .filter(priv -> dbNames.contains(priv.getDb()))
                .collect(Collectors.groupingBy(UserDbPriv::getUser,
                        Collectors.mapping(this::buildDbPrivilege, Collectors.toList())));
        return obUserList.stream()
                .peek(user -> {
                    List<DbPrivilege> dbPrivileges = Optional.ofNullable(userDbPrivMap.get(user.getUserName()))
                            .orElse(new ArrayList<>());
                    user.setDbPrivileges(dbPrivileges);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listUsernames() {
        List<ObUser> obUserList = listObUsers();
        return obUserList.stream()
                .map(ObUser::getUserName)
                .collect(Collectors.toList());
    }

    private List<ObUser> listObUsers() {
        RowMapper<ObUser> rowMapper = (rs, rowNum) -> mysqlUserMapRow(rs);
        return connectTemplate.weakRead().query(SELECT_MYSQL_USER_OB4, rowMapper);
    }

    @Override
    public ObUser getUser(String username) {
        Validate.notEmpty(username, "input username is empty");
        String sql = SELECT_MYSQL_USER_OB4 + " WHERE `user` = ?";
        RowMapper<ObUser> rowMapper = (rs, rowNum) -> mysqlUserMapRow(rs);
        ObUser obUser = connectTemplate.weakRead().queryForObject(sql, new Object[] {username}, rowMapper);
        obUser.setDbPrivileges(listDbPrivileges(username));
        return obUser;
    }

    private List<DbPrivilege> listDbPrivileges(String username) {
        List<String> dbNames = listDbNames();
        List<UserDbPriv> userDbPrivs = connectTemplate.weakRead().query(SELECT_DB_PRIV_BY_USER, new Object[] {username},
                (rs, rowNum) -> userDbPrivMapRow(rs));
        return userDbPrivs.stream()
                .filter(priv -> dbNames.contains(priv.getDb()))
                .map(this::buildDbPrivilege)
                .collect(Collectors.toList());
    }

    private List<String> listDbNames() {
        return connectTemplate.query(SHOW_DATABASE, new SingleColumnRowMapper<>(String.class));
    }

    @Override
    public void lockUser(String username) {
        Validate.notEmpty(username, "input username is empty");
        connectTemplate.update(LOCK_USER, username);
    }

    @Override
    public void unlockUser(String username) {
        Validate.notEmpty(username, "input username is empty");
        connectTemplate.update(UNLOCK_USER, username);
    }

    @Override
    public void alterPassword(String username, String password) {
        Validate.notEmpty(username, "input username is empty");
        connectTemplate.update(ALTER_PASSWORD, username, password);
    }

    @Override
    public void alterSuperPassword(String password) {
        alterPassword("root", password);
    }

    @Override
    public List<ObRole> listRoles() {
        throw new UnsupportedOperationException("roles not supported in MySQL mode");
    }

    @Override
    public ObRole getRole(String roleName) {
        throw new UnsupportedOperationException("roles not supported in MySQL mode");
    }

    @Override
    public void createRole(String roleName) {
        throw new UnsupportedOperationException("roles not supported in MySQL mode");
    }

    @Override
    public void dropRole(String roleName) {
        throw new UnsupportedOperationException("roles not supported in MySQL mode");
    }

    @Override
    public void grantGlobalPrivilege(String username, List<GlobalPrivilege> privileges) {
        Validate.notEmpty(username, "input username is empty");
        String privilege = ObSdkUtils.toCommaSeparatedString(privileges);
        String sql = String.format(GRANT_GLOBAL_PRIVILEGE, privilege);
        connectTemplate.update(sql, username);
    }

    @Override
    public void revokeGlobalPrivilege(String username, List<GlobalPrivilege> privileges) {
        Validate.notEmpty(username, "input username is empty");
        String privilege = ObSdkUtils.toCommaSeparatedString(privileges);
        String sql = String.format(REVOKE_GLOBAL_PRIVILEGE, privilege);
        connectTemplate.update(sql, username);
    }

    @Override
    public void grantDbPrivilege(String username, String dbName, List<DbPrivType> privileges) {
        Validate.notEmpty(username, "input username is empty");
        Validate.notEmpty(dbName, "input dbName is empty");
        String privilege = ObSdkUtils.toCommaSeparatedString(privileges);
        String sql = String.format(GRANT_DB_PRIVILEGE, privilege, dbName);
        connectTemplate.update(sql, username);
    }

    @Override
    public void revokeDbPrivilege(String username, String dbName, List<DbPrivType> privileges) {
        Validate.notEmpty(username, "input username is empty");
        Validate.notEmpty(dbName, "input dbName is empty");
        String privilege = ObSdkUtils.toCommaSeparatedString(privileges);
        String sql = String.format(REVOKE_DB_PRIVILEGE, privilege, dbName);
        connectTemplate.update(sql, username);
    }

    @Override
    public void grantRole(String username, List<String> roles) {
        throw new UnsupportedOperationException("roles not supported in MySQL mode");
    }

    @Override
    public void revokeRole(String username, List<String> roles) {
        throw new UnsupportedOperationException("roles not supported in MySQL mode");
    }

    @Override
    public List<DbObject> listAllObjects() {
        throw new UnsupportedOperationException("object privileges not supported in MySQL mode");
    }

    @Override
    public List<ObjectPrivilege> listAllObjectPrivileges() {
        throw new UnsupportedOperationException("object privileges not supported in MySQL mode");
    }

    @Override
    public void grantObjectPrivilege(String username, DbObject dbObject, List<ObjectPrivilegeType> privileges) {
        throw new UnsupportedOperationException("object privileges not supported in MySQL mode");
    }

    @Override
    public void revokeObjectPrivilege(String username, DbObject dbObject, List<ObjectPrivilegeType> privileges) {
        throw new UnsupportedOperationException("object privileges not supported in MySQL mode");
    }

    private DbPrivilege buildDbPrivilege(UserDbPriv userDbPriv) {
        DbPrivilege dbPrivilege = new DbPrivilege();
        dbPrivilege.setDbName(userDbPriv.getDb());
        dbPrivilege.setPrivileges(userDbPriv.getDbPrivileges());
        return dbPrivilege;
    }

    private ObUser mysqlUserMapRow(ResultSet rs) throws SQLException {
        List<String> privColumnList = Arrays.asList("select_priv", "insert_priv", "update_priv", "delete_priv",
                "create_priv", "drop_priv", "process_priv", "grant_priv", "index_priv", "alter_priv", "show_db_priv",
                "super_priv", "create_view_priv", "show_view_priv", "create_user_priv");
        ObUser obUser = new ObUser();
        obUser.setUserName(rs.getString("user"));
        obUser.setPassword(rs.getString("password"));
        obUser.setIsLocked(rs.getLong("account_locked"));
        List<GlobalPrivilege> globalPrivTypes = new ArrayList<>();
        for (String column : privColumnList) {
            String value = rs.getString(column);
            if ("Y".equals(value)) {
                globalPrivTypes.add(MysqlGlobalPrivilege.fromValue(column));
            }
        }
        obUser.setGlobalPrivileges(globalPrivTypes);
        return obUser;
    }

    private UserDbPriv userDbPrivMapRow(ResultSet rs) throws SQLException {
        List<String> privColumnList = Arrays.asList("select_priv", "insert_priv", "update_priv", "delete_priv",
                "create_priv", "drop_priv", "index_priv", "alter_priv", "create_view_priv", "show_view_priv");
        UserDbPriv userDbPriv = new UserDbPriv();
        String dbName = ObSdkUtils.deleteEscapeChar(rs.getString("db"));
        userDbPriv.setDb(dbName);
        userDbPriv.setUser(rs.getString("user"));
        List<DbPrivType> dbPrivTypes = new ArrayList<>();
        for (String column : privColumnList) {
            String value = rs.getString(column);
            if ("Y".equals(value)) {
                dbPrivTypes.add(DbPrivType.fromValue(column));
            }
        }
        userDbPriv.setDbPrivileges(dbPrivTypes);
        return userDbPriv;
    }
}
