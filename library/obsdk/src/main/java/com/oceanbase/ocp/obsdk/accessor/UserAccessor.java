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

package com.oceanbase.ocp.obsdk.accessor;

import java.util.List;

import com.oceanbase.ocp.obsdk.accessor.user.model.DbObject;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObRole;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObUser;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObjectPrivilege;
import com.oceanbase.ocp.obsdk.enums.DbPrivType;
import com.oceanbase.ocp.obsdk.enums.GlobalPrivilege;
import com.oceanbase.ocp.obsdk.enums.ObjectPrivilegeType;

/**
 * User & privilege management in user tenants.
 */
public interface UserAccessor {

    /**
     * create user
     *
     * @param username username
     * @param password password of user
     */
    void createUser(String username, String password);

    /**
     * drop user
     *
     * @param username username
     */
    void dropUser(String username);

    /**
     * list all users
     *
     * @return list of user info
     */
    List<ObUser> listUsers();

    /**
     * list all usernames
     *
     * @return list of usernames
     */
    List<String> listUsernames();

    /**
     * get user by name
     *
     * @param username name of user
     * @return user info
     */
    ObUser getUser(String username);

    /**
     * lock user
     *
     * @param username name of user
     */
    void lockUser(String username);

    /**
     * unlock user
     *
     * @param username name of user
     */
    void unlockUser(String username);

    /**
     * alter user password
     *
     * @param username username
     * @param password password of user
     */
    void alterPassword(String username, String password);

    /**
     * reset password of super user
     *
     * @param password new password
     */
    void alterSuperPassword(String password);

    /**
     * create role
     *
     * @param roleName role name
     */
    void createRole(String roleName);

    /**
     * drop role
     *
     * @param roleName role name
     */
    void dropRole(String roleName);

    /**
     * list all roles
     *
     * @return user list
     */
    List<ObRole> listRoles();

    /**
     * get role by name
     *
     * @param roleName role name
     * @return user info
     */
    ObRole getRole(String roleName);

    /**
     * grant privilege to user or role
     *
     * @param username name of user or role
     * @param privileges privilege list
     */
    void grantGlobalPrivilege(String username, List<GlobalPrivilege> privileges);

    /**
     * revoke privilege from user or role
     *
     * @param username name of user or role
     * @param privileges privilege list
     */
    void revokeGlobalPrivilege(String username, List<GlobalPrivilege> privileges);

    /**
     * grant db privilege to user
     *
     * @param username db user
     * @param dbName db name
     * @param privileges privilege list
     */
    void grantDbPrivilege(String username, String dbName, List<DbPrivType> privileges);

    /**
     * revoke db privilege to user
     *
     * @param username db user
     * @param dbName db name
     * @param privileges privilege list
     */
    void revokeDbPrivilege(String username, String dbName, List<DbPrivType> privileges);

    /**
     * grant roles to user or role
     *
     * @param username name of user or role
     * @param roles roles to grant
     */
    void grantRole(String username, List<String> roles);

    /**
     * revoke roles from user or role
     *
     * @param username name of user or role
     * @param roles roles to revoke
     */
    void revokeRole(String username, List<String> roles);

    /**
     * list all objects under all users(schemas)
     *
     * @return object list
     */
    List<DbObject> listAllObjects();

    /**
     * list all object privileges
     *
     * @return object privileges
     */
    List<ObjectPrivilege> listAllObjectPrivileges();

    /**
     * grant object privileges to user or role
     *
     * @param username name of user or role
     * @param dbObject the object to grant privilege
     * @param privileges object privileges
     */
    void grantObjectPrivilege(String username, DbObject dbObject, List<ObjectPrivilegeType> privileges);

    /**
     * revoke object privileges to user or role
     *
     * @param username name of user or role
     * @param dbObject the object to grant privilege
     * @param privileges object privileges
     */
    void revokeObjectPrivilege(String username, DbObject dbObject, List<ObjectPrivilegeType> privileges);
}
