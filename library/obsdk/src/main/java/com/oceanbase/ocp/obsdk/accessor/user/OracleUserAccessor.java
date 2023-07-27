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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;

import com.oceanbase.ocp.obsdk.accessor.UserAccessor;
import com.oceanbase.ocp.obsdk.accessor.user.model.DbObject;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObRole;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObUser;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObjectPrivilege;
import com.oceanbase.ocp.obsdk.accessor.user.model.UserObjectPrivilege;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.enums.DbPrivType;
import com.oceanbase.ocp.obsdk.enums.GlobalPrivilege;
import com.oceanbase.ocp.obsdk.enums.ObjectPrivilegeType;
import com.oceanbase.ocp.obsdk.enums.ObjectType;
import com.oceanbase.ocp.obsdk.enums.OracleSystemPrivilege;

public class OracleUserAccessor implements UserAccessor {

    private static final String CREATE_USER = "CREATE USER ? IDENTIFIED BY \"%s\"";
    private static final String DROP_USER = "DROP USER ? CASCADE";
    private static final String SELECT_ALL_USER =
            "SELECT USERNAME, CREATED, (CASE ACCOUNT_STATUS WHEN 'OPEN' THEN 0 ELSE 1 END) AS IS_LOCKED FROM DBA_USERS";
    private static final String SELECT_ALL_USERNAME = "SELECT USERNAME FROM DBA_USERS";

    private static final String ALTER_PASSWORD = "ALTER USER ? IDENTIFIED BY \"%s\"";
    private static final String LOCK_USER = "ALTER USER ? ACCOUNT LOCK";
    private static final String UNLOCK_USER = "ALTER USER ? ACCOUNT UNLOCK";

    private static final String SELECT_ALL_ROLE =
            "SELECT ROLE, NULL AS GMT_CREATE, NULL AS GMT_MODIFIED FROM DBA_ROLES";
    private static final String CREATE_ROLE = "CREATE ROLE ?";
    private static final String DROP_ROLE = "DROP ROLE ?";

    private static final String GRANT_PRIVILEGE = "GRANT %s TO ?";
    private static final String REVOKE_PRIVILEGE = "REVOKE %s FROM ?";

    private static final String GRANT_OBJECT_PRIVILEGE = "GRANT %s ON \"%s\".\"%s\" TO ?";
    private static final String REVOKE_OBJECT_PRIVILEGE = "REVOKE %s ON \"%s\".\"%s\" FROM ?";

    private static final String SELECT_SYSTEM_PRIVILEGE =
            "SELECT PRIVILEGE FROM DBA_SYS_PRIVS WHERE GRANTEE = ?";
    private static final String SELECT_GRANTED_ROLES =
            "SELECT GRANTED_ROLE FROM DBA_ROLE_PRIVS WHERE GRANTEE = ?";
    private static final String SELECT_USER_GRANTEES =
            "SELECT GRANTEE FROM DBA_ROLE_PRIVS WHERE GRANTED_ROLE = ?"
                    + " AND GRANTEE IN (SELECT USERNAME FROM DBA_USERS)";
    private static final String SELECT_ROLE_GRANTEES =
            "SELECT GRANTEE FROM DBA_ROLE_PRIVS WHERE GRANTED_ROLE = ?"
                    + " AND GRANTEE IN (SELECT ROLE FROM DBA_ROLES)";

    private static final String SELECT_ALL_OBJECTS = "SELECT"
            + " OBJECT_TYPE, OBJECT_NAME, OWNER AS SCHEMA_NAME FROM DBA_OBJECTS"
            + " WHERE OBJECT_TYPE IN ('TABLE', 'VIEW', 'PROCEDURE')"
            + " AND OWNER NOT IN ('SYS', 'oceanbase')";

    private static final String SELECT_ALL_OBJECT_PRIVILEGES = "SELECT"
            + " P.GRANTEE, P.OWNER, O.OBJECT_TYPE, O.OBJECT_NAME, P.PRIVILEGE"
            + " FROM DBA_TAB_PRIVS P JOIN DBA_OBJECTS O"
            + " ON P.OWNER = O.OWNER AND P.TABLE_NAME = O.OBJECT_NAME";

    private final ObConnectTemplate connectTemplate;

    public OracleUserAccessor(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public void createUser(String username, String password) {
        Validate.notEmpty(username, "input username is empty");
        Validate.notEmpty(password, "input password is empty");
        connectTemplate.update(String.format(CREATE_USER, password), username);
    }

    @Override
    public void dropUser(String username) {
        Validate.notEmpty(username, "input username is empty");
        connectTemplate.update(DROP_USER, username);
    }

    @Override
    public List<ObUser> listUsers() {
        RowMapper<ObUser> userRowMapper = (rs, rowNum) -> allUserMapRow(rs);
        List<ObUser> obUsers = connectTemplate.query(SELECT_ALL_USER, userRowMapper);
        for (ObUser obUser : obUsers) {
            attachPrivileges(obUser);
        }
        return obUsers;
    }

    @Override
    public List<String> listUsernames() {
        return connectTemplate.weakRead().query(SELECT_ALL_USERNAME, new SingleColumnRowMapper<>());
    }

    @Override
    public ObUser getUser(String username) {
        Validate.notEmpty(username, "input username is empty");
        RowMapper<ObUser> userRowMapper = (rs, rowNum) -> allUserMapRow(rs);
        String sql = SELECT_ALL_USER + " WHERE USERNAME = ?";
        ObUser obUser = connectTemplate.queryForObject(sql, new Object[] {username}, userRowMapper);
        attachPrivileges(obUser);
        return obUser;
    }

    private void attachPrivileges(ObUser obUser) {
        String username = obUser.getUserName();
        obUser.setGlobalPrivileges(listSystemPrivileges(username));
        obUser.setGrantedRoles(listGrantedRoles(username));
        obUser.setObjectPrivileges(listObjectPrivileges(username));
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
        Validate.notEmpty(password, "input password is empty");
        connectTemplate.update(String.format(ALTER_PASSWORD, password), username);
    }

    @Override
    public void alterSuperPassword(String password) {
        alterPassword("SYS", password);
    }

    @Override
    public List<ObRole> listRoles() {
        RowMapper<ObRole> roleRowMapper = (rs, rowNum) -> roleMapRow(rs);
        List<ObRole> obRoles = connectTemplate.query(SELECT_ALL_ROLE, roleRowMapper);
        for (ObRole obRole : obRoles) {
            attachPrivileges(obRole);
        }
        return obRoles;
    }

    @Override
    public ObRole getRole(String roleName) {
        Validate.notEmpty(roleName, "input role name is empty");
        RowMapper<ObRole> roleRowMapper = (rs, rowNum) -> roleMapRow(rs);
        String sql = SELECT_ALL_ROLE + " WHERE ROLE = ?";
        ObRole obRole = connectTemplate.queryForObject(sql, new Object[] {roleName}, roleRowMapper);
        attachPrivileges(obRole);
        return obRole;
    }

    private void attachPrivileges(ObRole obRole) {
        String roleName = obRole.getName();
        obRole.setGlobalPrivileges(listSystemPrivileges(roleName));
        obRole.setGrantedRoles(listGrantedRoles(roleName));
        obRole.setUserGrantees(listUserGrantees(roleName));
        obRole.setRoleGrantees(listRoleGrantees(roleName));
        obRole.setObjectPrivileges(listObjectPrivileges(roleName));
    }

    @Override
    public void createRole(String roleName) {
        Validate.notEmpty(roleName, "input role name is empty");
        connectTemplate.update(CREATE_ROLE, roleName);
    }

    @Override
    public void dropRole(String roleName) {
        Validate.notEmpty(roleName, "input role name is empty");
        connectTemplate.update(DROP_ROLE, roleName);
    }

    @Override
    public void grantGlobalPrivilege(String username, List<GlobalPrivilege> privileges) {
        Validate.notEmpty(username, "input username is empty");
        String privilegesString = privileges.stream()
                .map(GlobalPrivilege::asOracle)
                .map(OracleSystemPrivilege::getValue)
                .collect(Collectors.joining(", "));
        String sql = String.format(GRANT_PRIVILEGE, privilegesString);
        connectTemplate.update(sql, username);
    }

    @Override
    public void revokeGlobalPrivilege(String username, List<GlobalPrivilege> privileges) {
        Validate.notEmpty(username, "input username is empty");
        String privilegesString = privileges.stream()
                .map(GlobalPrivilege::asOracle)
                .map(OracleSystemPrivilege::getValue)
                .collect(Collectors.joining(", "));
        String sql = String.format(REVOKE_PRIVILEGE, privilegesString);
        connectTemplate.update(sql, username);
    }

    /**
     * list system privileges granted to the specified user or role
     *
     * @param username name of user or role
     * @return system privileges
     */
    private List<GlobalPrivilege> listSystemPrivileges(String username) {
        Validate.notEmpty(username, "input username is empty");
        List<String> privileges =
                connectTemplate.weakRead().query(SELECT_SYSTEM_PRIVILEGE, new Object[] {username},
                        new SingleColumnRowMapper<>());
        return privileges.stream()
                .map(OracleSystemPrivilege::fromValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void grantDbPrivilege(String username, String dbName, List<DbPrivType> privileges) {
        throw new UnsupportedOperationException("database privileges not supported in Oracle mode");
    }

    @Override
    public void revokeDbPrivilege(String username, String dbName, List<DbPrivType> privileges) {
        throw new UnsupportedOperationException("database privileges not supported in Oracle mode");
    }

    @Override
    public void grantRole(String username, List<String> roles) {
        Validate.notEmpty(username, "input username is empty");
        Validate.notEmpty(roles, "input roles is empty");
        String sql = String.format(GRANT_PRIVILEGE, roles.stream()
                .map(role -> "'" + role + "'")
                .collect(Collectors.joining(",")));
        connectTemplate.update(sql, username);
    }

    @Override
    public void revokeRole(String username, List<String> roles) {
        Validate.notEmpty(username, "input username is empty");
        Validate.notEmpty(roles, "input roles is empty");
        String sql = String.format(REVOKE_PRIVILEGE, roles.stream()
                .map(role -> "'" + role + "'")
                .collect(Collectors.joining(",")));
        connectTemplate.update(sql, username);
    }

    /**
     * list roles granted to the specified user or role
     *
     * @param username name of user or role
     * @return role names
     */
    private List<String> listGrantedRoles(String username) {
        Validate.notEmpty(username, "input username is empty");
        return connectTemplate.weakRead().query(SELECT_GRANTED_ROLES, new Object[] {username},
                new SingleColumnRowMapper<>());
    }

    /**
     * list users which the specified role are granted to
     *
     * @param roleName name of role
     * @return user names
     */
    private List<String> listUserGrantees(String roleName) {
        Validate.notEmpty(roleName, "input roleName is empty");
        return connectTemplate.weakRead().query(SELECT_USER_GRANTEES, new Object[] {roleName},
                new SingleColumnRowMapper<>());
    }

    /**
     * list roles which the specified role are granted to
     *
     * @param roleName name of role
     * @return role names
     */
    private List<String> listRoleGrantees(String roleName) {
        Validate.notEmpty(roleName, "input roleName is empty");
        return connectTemplate.weakRead().query(SELECT_ROLE_GRANTEES, new Object[] {roleName},
                new SingleColumnRowMapper<>());
    }

    @Override
    public List<DbObject> listAllObjects() {
        RowMapper<DbObject> objectRowMapper = (rs, rowNum) -> objectMapRow(rs);
        return connectTemplate.query(SELECT_ALL_OBJECTS, objectRowMapper);
    }

    @Override
    public List<ObjectPrivilege> listAllObjectPrivileges() {
        RowMapper<UserObjectPrivilege> rowMapper = (rs, rowNum) -> userObjectPrivilegeMapRow(rs);
        List<UserObjectPrivilege> userObjectPrivileges =
                connectTemplate.query(SELECT_ALL_OBJECT_PRIVILEGES, rowMapper);
        return aggregateObjectPrivileges(userObjectPrivileges);
    }

    /**
     * list object privileges granted to the specified user or role
     *
     * @param username name of user or role
     * @return object privileges
     */
    private List<ObjectPrivilege> listObjectPrivileges(String username) {
        RowMapper<UserObjectPrivilege> rowMapper = (rs, rowNum) -> userObjectPrivilegeMapRow(rs);
        String sql = SELECT_ALL_OBJECT_PRIVILEGES + " WHERE P.GRANTEE = ?";
        List<UserObjectPrivilege> userObjectPrivileges = connectTemplate.query(sql, new Object[] {username}, rowMapper);
        return aggregateObjectPrivileges(userObjectPrivileges);
    }

    private List<ObjectPrivilege> aggregateObjectPrivileges(List<UserObjectPrivilege> userObjectPrivileges) {
        Builder<DbObject, ObjectPrivilegeType> multimapBuilder = ImmutableMultimap.builder();
        for (UserObjectPrivilege userObjectPrivilege : userObjectPrivileges) {
            if (userObjectPrivilege.getObjectPrivilege() == null) {
                continue;
            }
            multimapBuilder.put(userObjectPrivilege.getDbObject(), userObjectPrivilege.getObjectPrivilege());
        }
        ImmutableMultimap<DbObject, ObjectPrivilegeType> objectPrivilegesMap = multimapBuilder.build();

        return objectPrivilegesMap.asMap().entrySet().stream()
                .map(entry -> {
                    ObjectPrivilege objectPrivilege = new ObjectPrivilege();
                    objectPrivilege.setObject(entry.getKey());
                    objectPrivilege.setPrivileges(new ArrayList<>(entry.getValue()));
                    return objectPrivilege;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void grantObjectPrivilege(String username, DbObject dbObject, List<ObjectPrivilegeType> privileges) {
        Validate.notEmpty(username, "input username is empty");
        Validate.notNull(dbObject, "input object is null");
        dbObject.validate();
        String privilegesString = privileges.stream()
                .map(ObjectPrivilegeType::getValue)
                .collect(Collectors.joining(", "));
        String sql = String.format(GRANT_OBJECT_PRIVILEGE, privilegesString, dbObject.getSchemaName(),
                dbObject.getObjectName());
        connectTemplate.update(sql, username);
    }

    @Override
    public void revokeObjectPrivilege(String username, DbObject dbObject, List<ObjectPrivilegeType> privileges) {
        Validate.notEmpty(username, "input username is empty");
        Validate.notNull(dbObject, "input object is null");
        dbObject.validate();
        String privilegesString = privileges.stream()
                .map(ObjectPrivilegeType::getValue)
                .collect(Collectors.joining(", "));
        String sql = String.format(REVOKE_OBJECT_PRIVILEGE, privilegesString, dbObject.getSchemaName(),
                dbObject.getObjectName());
        connectTemplate.update(sql, username);
    }

    private ObUser allUserMapRow(ResultSet rs) throws SQLException {
        ObUser obUser = new ObUser();
        obUser.setUserName(rs.getString("USERNAME"));
        obUser.setGmtCreate(rs.getTimestamp("CREATED"));
        obUser.setIsLocked(rs.getLong("IS_LOCKED"));
        return obUser;
    }

    private ObRole roleMapRow(ResultSet rs) throws SQLException {
        ObRole obRole = new ObRole();
        obRole.setName(rs.getString("ROLE"));
        obRole.setCreateTime(rs.getTimestamp("GMT_CREATE"));
        obRole.setUpdateTime(rs.getTimestamp("GMT_MODIFIED"));
        return obRole;
    }

    private UserObjectPrivilege userObjectPrivilegeMapRow(ResultSet rs) throws SQLException {
        UserObjectPrivilege userObjectPrivilege = new UserObjectPrivilege();
        userObjectPrivilege.setUser(rs.getString("GRANTEE"));
        userObjectPrivilege.setSchemaName(rs.getString("OWNER"));
        userObjectPrivilege.setObjectType(ObjectType.fromValue(rs.getString("OBJECT_TYPE")));
        userObjectPrivilege.setObjectName(rs.getString("OBJECT_NAME"));
        userObjectPrivilege.setObjectPrivilege(ObjectPrivilegeType.fromValue(rs.getString("PRIVILEGE")));
        return userObjectPrivilege;
    }

    private DbObject objectMapRow(ResultSet rs) throws SQLException {
        DbObject dbObject = new DbObject();
        dbObject.setObjectType(ObjectType.fromValue(rs.getString("OBJECT_TYPE")));
        dbObject.setObjectName(rs.getString("OBJECT_NAME"));
        dbObject.setSchemaName(rs.getString("SCHEMA_NAME"));
        return dbObject;
    }
}
