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

package com.oceanbase.ocp.obops.internal.database;

import static com.oceanbase.ocp.obops.internal.database.DbPrivilegeUtils.checkObjectPrivileges;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.common.time.DefaultOffsetDateTimeConverter;
import com.oceanbase.ocp.core.constants.OcpConstants;
import com.oceanbase.ocp.core.credential.operator.ObCredentialOperator;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.cluster.ManagedCluster;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.core.obsdk.ObAccessorFactory;
import com.oceanbase.ocp.core.property.SystemInfo;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obops.database.DbUserService;
import com.oceanbase.ocp.obops.database.model.DbUser;
import com.oceanbase.ocp.obops.database.model.Grantee;
import com.oceanbase.ocp.obops.database.param.CreateDbUserParam;
import com.oceanbase.ocp.obops.database.param.DbPrivilegeParam;
import com.oceanbase.ocp.obops.database.param.ModifyDbUserPasswordParam;
import com.oceanbase.ocp.obops.tenant.TenantService;
import com.oceanbase.ocp.obops.tenant.model.ObproxyAndConnectionString;
import com.oceanbase.ocp.obsdk.accessor.ObAccessor;
import com.oceanbase.ocp.obsdk.accessor.database.model.ObDatabase;
import com.oceanbase.ocp.obsdk.accessor.user.model.DbPrivilege;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObUser;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObjectPrivilege;
import com.oceanbase.ocp.obsdk.connector.ObConnectors;
import com.oceanbase.ocp.obsdk.enums.DbPrivType;
import com.oceanbase.ocp.obsdk.enums.GlobalPrivilege;
import com.oceanbase.ocp.obsdk.enums.MysqlGlobalPrivilege;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DbUserServiceImpl implements DbUserService {

    private static final String MYSQL_USERNAME_PATTERN = "^[a-z][a-z_0-9]{1,127}$";
    private static final String ORACLE_USERNAME_PATTERN = "^[a-zA-Z][a-zA-Z_0-9]{1,127}$";
    private static final List<String> EXCLUDE_LIST =
            Arrays.asList("PUBLIC", "LBACSYS", "ORAAUDITOR", "__oceanbase_inner_standby_user", "ocp_monitor");

    @Resource
    private DbPrivilegeManager dbPrivilegeManager;

    @Resource
    private ObAccessorFactory obAccessorFactory;

    @Resource
    private ManagedCluster managedCluster;

    @Resource
    private TenantDaoManager tenantDaoManager;

    @Resource
    private ObCredentialOperator obCredentialOperator;

    @Resource
    private TenantService tenantService;

    @Resource
    private SystemInfo systemInfo;

    @Override
    public List<DbUser> listUsers(Long obTenantId) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        List<ObUser> obUsers = obAccessor.user().listUsers();
        List<DbUser> dbUsers = obUsers.stream()
                .filter(user -> !EXCLUDE_LIST.contains(user.getUserName()))
                .map(this::mapToModel)
                .collect(Collectors.toList());
        attachUsers(obAccessor, tenantEntity, dbUsers);
        return dbUsers;
    }

    @Override
    public DbUser getUser(Long obTenantId, String username) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkUserOperationAllowed(tenantEntity, username);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        ObUser obUser = obAccessor.user().getUser(username);
        DbUser dbUser = mapToModel(obUser);
        attachUsers(obAccessor, tenantEntity, Collections.singletonList(dbUser));
        return dbUser;
    }

    @Override
    public DbUser createUser(Long obTenantId, CreateDbUserParam param) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);

        String username = param.getUsername();
        checkUsernamePattern(tenantEntity, username);
        // Usernames are force to be uppercase in creation in Oracle mode.
        if (tenantEntity.isOracleMode()) {
            username = StringUtils.upperCase(username);
        }

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkUserNotExist(obAccessor, username);

        validateDbPrivilege(param.getDbPrivileges());
        obAccessor.user().createUser(username, param.getPassword());

        saveCredential(tenantEntity, username, param.getPassword());

        if (CollectionUtils.isNotEmpty(param.getGlobalPrivileges())) {
            grantGlobalPrivilege(obTenantId, username, param.getGlobalPrivileges());
        }
        if (CollectionUtils.isNotEmpty(param.getDbPrivileges())) {
            grantDbPrivilege(obTenantId, username, param.getDbPrivileges());
        }
        if (CollectionUtils.isNotEmpty(param.getRoles())) {
            grantRole(obTenantId, username, param.getRoles());
        }
        ObUser obUser = obAccessor.user().getUser(username);
        return mapToModel(obUser);
    }

    @Override
    public void deleteUser(Long obTenantId, String username) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkUserOperationAllowed(tenantEntity, username);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        ExceptionUtils.illegalArgs(isUserExist(obAccessor, username), ErrorCodes.OB_USER_NAME_NOT_FOUND, username);
        obAccessor.user().dropUser(username);
    }

    @Override
    public void grantGlobalPrivilege(Long obTenantId, String username, List<String> privilegeStrings) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkUserOperationAllowed(tenantEntity, username);
        List<GlobalPrivilege> privileges =
                DbPrivilegeUtils.parseGlobalPrivileges(privilegeStrings, tenantEntity.getMode());

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkUserExist(obAccessor, username);
        dbPrivilegeManager.grantGlobalPrivilege(obTenantId, Grantee.user(username), privileges);
    }

    @Override
    public void modifyGlobalPrivilege(Long obTenantId, String username, List<String> privilegeStrings) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkUserOperationAllowed(tenantEntity, username);
        List<GlobalPrivilege> privileges =
                DbPrivilegeUtils.parseGlobalPrivileges(privilegeStrings, tenantEntity.getMode());

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkUserExist(obAccessor, username);
        ObUser user = obAccessor.user().getUser(username);
        dbPrivilegeManager.modifyGlobalPrivilege(obTenantId, Grantee.user(username), user.getGlobalPrivileges(),
                privileges);
    }

    @Override
    public void grantDbPrivilege(Long obTenantId, String username, List<DbPrivilegeParam> dbPrivileges) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkMysqlPrivilegeManagementSupported(tenantEntity);
        checkUserOperationAllowed(tenantEntity, username);

        validateDbPrivilege(dbPrivileges);
        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkUserExist(obAccessor, username);
        dbPrivilegeManager.grantDbPrivilege(obTenantId, Grantee.user(username), mapDbPrivilege(dbPrivileges));
    }

    @Override
    public void modifyDbPrivilege(Long obTenantId, String username, List<DbPrivilegeParam> dbPrivileges) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkMysqlPrivilegeManagementSupported(tenantEntity);
        checkUserOperationAllowed(tenantEntity, username);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkUserExist(obAccessor, username);
        ObUser user = obAccessor.user().getUser(username);
        List<DbPrivilege> currentDbPrivileges = user.getDbPrivileges();
        List<DbPrivilege> targetDbPrivileges = mapDbPrivilege(dbPrivileges);
        dbPrivilegeManager.modifyDbPrivilege(obTenantId, Grantee.user(username), currentDbPrivileges,
                targetDbPrivileges);
    }

    @Override
    public void grantRole(Long obTenantId, String username, List<String> roles) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);
        checkUserOperationAllowed(tenantEntity, username);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkUserExist(obAccessor, username);
        dbPrivilegeManager.grantRole(obTenantId, Grantee.user(username), roles);
    }

    @Override
    public void revokeRole(Long obTenantId, String username, List<String> roles) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);
        checkUserOperationAllowed(tenantEntity, username);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkUserExist(obAccessor, username);
        dbPrivilegeManager.revokeRole(obTenantId, Grantee.user(username), roles);
    }

    @Override
    public void modifyRole(Long obTenantId, String username, List<String> roles) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);
        checkUserOperationAllowed(tenantEntity, username);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkUserExist(obAccessor, username);
        ObUser user = obAccessor.user().getUser(username);
        dbPrivilegeManager.modifyRole(obTenantId, Grantee.user(username), user.getGrantedRoles(), roles);
    }

    @Override
    public void grantObjectPrivilege(Long obTenantId, String username, List<ObjectPrivilege> objectPrivileges) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);
        checkUserOperationAllowed(tenantEntity, username);
        checkObjectPrivileges(objectPrivileges);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkUserExist(obAccessor, username);
        dbPrivilegeManager.grantObjectPrivilege(obTenantId, Grantee.user(username), objectPrivileges);
    }

    @Override
    public void revokeObjectPrivilege(Long obTenantId, String username, List<ObjectPrivilege> objectPrivileges) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);
        checkUserOperationAllowed(tenantEntity, username);
        checkObjectPrivileges(objectPrivileges);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkUserExist(obAccessor, username);
        dbPrivilegeManager.revokeObjectPrivilege(obTenantId, Grantee.user(username), objectPrivileges);
    }

    @Override
    public void modifyObjectPrivilege(Long obTenantId, String username, List<ObjectPrivilege> objectPrivileges) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);
        checkUserOperationAllowed(tenantEntity, username);
        checkObjectPrivileges(objectPrivileges);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkUserExist(obAccessor, username);
        ObUser user = obAccessor.user().getUser(username);
        dbPrivilegeManager.modifyObjectPrivilege(obTenantId, Grantee.user(username), user.getObjectPrivileges(),
                objectPrivileges);
    }

    @Override
    public void modifyPassword(Long obTenantId, String username, ModifyDbUserPasswordParam param) {
        ObTenantEntity entity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkUserOperationAllowed(entity, username);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        ExceptionUtils.illegalArgs(isSuperUser(username) || isUserExist(obAccessor, username),
                ErrorCodes.OB_USER_NAME_NOT_FOUND, username);
        obAccessor.user().alterPassword(username, param.getNewPassword());
        ObConnectors.invalidate(managedCluster.clusterName(), entity.getName(), username);
        saveCredential(entity, username, param.getNewPassword());
    }

    private boolean isSuperUser(String username) {
        return Arrays.stream(TenantMode.values())
                .map(TenantMode::getSuperUser)
                .collect(Collectors.toList())
                .contains(username);
    }

    private void saveCredential(ObTenantEntity entity, String username, String password) {
        obCredentialOperator.saveObCredential(managedCluster.clusterName(), entity.getName(), username, password);
    }

    @Override
    public void lockUser(Long obTenantId, String username) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkUserOperationAllowed(tenantEntity, username);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        ExceptionUtils.illegalArgs(isUserExist(obAccessor, username), ErrorCodes.OB_USER_NAME_NOT_FOUND, username);
        obAccessor.user().lockUser(username);
    }

    @Override
    public void unlockUser(Long obTenantId, String username) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkUserOperationAllowed(tenantEntity, username);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        ExceptionUtils.illegalArgs(isUserExist(obAccessor, username), ErrorCodes.OB_USER_NAME_NOT_FOUND, username);
        obAccessor.user().unlockUser(username);
    }

    private void checkMysqlPrivilegeManagementSupported(ObTenantEntity tenantEntity) {
        ExceptionUtils.illegalArgs(tenantEntity.isMysqlMode(), ErrorCodes.OB_USER_ORACLE_MODE_NOT_SUPPORTED);
    }

    private void checkOraclePrivilegeManagementSupported(ObTenantEntity tenantEntity) {
        ExceptionUtils.illegalArgs(tenantEntity.isOracleMode(), ErrorCodes.OB_USER_MYSQL_MODE_NOT_SUPPORTED);
    }

    private void checkUsernamePattern(ObTenantEntity tenantEntity, String username) {
        String usernamePattern = tenantEntity.isMysqlMode() ? MYSQL_USERNAME_PATTERN : ORACLE_USERNAME_PATTERN;
        ErrorCodes errorCode = tenantEntity.isMysqlMode() ? ErrorCodes.OB_USER_NAME_INVALID
                : ErrorCodes.OB_USER_NAME_INVALID_FOR_ORACLE_MODE;
        ExceptionUtils.illegalArgs(username.matches(usernamePattern), errorCode, username);
    }

    private void checkUserOperationAllowed(ObTenantEntity tenantEntity, String username) {
        ExceptionUtils.illegalArgs(isOperationAllowed(tenantEntity, username), ErrorCodes.OB_USER_OPERATION_NOT_ALLOW,
                username);
    }

    private void checkUserExist(ObAccessor obAccessor, String username) {
        ExceptionUtils.illegalArgs(isUserExist(obAccessor, username), ErrorCodes.OB_USER_NAME_NOT_FOUND, username);
    }

    private void checkUserNotExist(ObAccessor obAccessor, String username) {
        ExceptionUtils.illegalArgs(isUserNotExist(obAccessor, username), ErrorCodes.OB_USER_NAME_EXISTS, username);
    }

    private List<DbPrivilege> mapDbPrivilege(List<DbPrivilegeParam> dbPrivileges) {
        return dbPrivileges.stream()
                .filter(param -> !DatabaseServiceImpl.NO_PRIVILEGE_DATABASE_LIST.contains(param.getDbName()))
                .map(DbPrivilegeParam::toDbPrivilege)
                .collect(Collectors.toList());
    }

    private DbUser mapToModel(ObUser obUser) {
        DbUser dbUser = new DbUser();
        dbUser.setUsername(obUser.getUserName());
        dbUser.setGlobalPrivileges(obUser.getGlobalPrivileges());
        dbUser.setDbPrivileges(obUser.getDbPrivileges());
        dbUser.setGrantedRoles(obUser.getGrantedRoles());
        dbUser.setObjectPrivileges(obUser.getObjectPrivileges());
        dbUser.setIsLocked(obUser.isLocked());
        OffsetDateTime createTime = DefaultOffsetDateTimeConverter.fromTimestamp(obUser.getGmtCreate());
        dbUser.setCreateTime(createTime);
        return dbUser;
    }

    private void attachUsers(ObAccessor obAccessor, ObTenantEntity tenantEntity, List<DbUser> dbUsers) {
        List<ObproxyAndConnectionString> connectionStringTemplates =
                tenantService.getConnectionStringTemplates(tenantEntity.getObTenantId());
        List<String> allDbs = obAccessor.object().listDatabases().stream()
                .map(ObDatabase::getName)
                .collect(Collectors.toList());
        for (DbUser user : dbUsers) {
            attachConnectionStrings(user, connectionStringTemplates);
            if (tenantEntity.isMysqlMode()) {
                attachDbs(user, allDbs);
            }
        }
    }

    private void attachConnectionStrings(DbUser dbUser,
            List<ObproxyAndConnectionString> templates) {
        List<ObproxyAndConnectionString> connectionStrings = templates.stream()
                .map(template -> template.formatConnectionString(dbUser.getUsername()))
                .collect(Collectors.toList());
        dbUser.setConnectionStrings(connectionStrings);
    }

    private void attachDbs(DbUser dbUser, List<String> allDbs) {
        List<String> dbs = allDbs.stream()
                .filter(db -> hasPrivilege(dbUser, db))
                .collect(Collectors.toList());
        dbUser.setAccessibleDatabases(dbs);
    }

    private boolean hasPrivilege(DbUser dbUser, String dbName) {
        if (DatabaseServiceImpl.NO_PRIVILEGE_DATABASE_LIST.contains(dbName)) {
            return false;
        } else if (DatabaseServiceImpl.RESTRICTED_DATABASE_LIST.contains(dbName)) {
            // Restricted databases only support SELECT privilege
            return hasPrivilege(dbUser, dbName, Collections.singletonList(MysqlGlobalPrivilege.SELECT));
        } else {
            List<MysqlGlobalPrivilege> privTypes = Arrays.stream(DbPrivType.values())
                    .map(DbPrivType::name)
                    .map(MysqlGlobalPrivilege::fromValue)
                    .collect(Collectors.toList());
            return hasPrivilege(dbUser, dbName, privTypes);
        }
    }

    // A user has privilege to database if granted to database privileges or global
    // privileges
    private boolean hasPrivilege(DbUser dbUser, String dbName, List<MysqlGlobalPrivilege> privTyps) {
        List<String> dbs = dbUser.getDbPrivileges().stream()
                .map(DbPrivilege::getDbName)
                .collect(Collectors.toList());
        if (dbs.contains(dbName)) {
            return true;
        }
        return dbUser.getGlobalPrivileges().stream().anyMatch(privTyps::contains);
    }

    private void validateDbPrivilege(List<DbPrivilegeParam> dbPrivilegeParams) {
        if (CollectionUtils.isEmpty(dbPrivilegeParams)) {
            return;
        }
        // Restricted databases only support SELECT privilege
        dbPrivilegeParams.stream()
                .filter(param -> DatabaseServiceImpl.RESTRICTED_DATABASE_LIST.contains(param.getDbName()))
                .filter(param -> CollectionUtils.isNotEmpty(param.getPrivileges()))
                .forEach(param -> {
                    boolean valid =
                            param.getPrivileges().size() == 1 && param.getPrivileges().contains(DbPrivType.SELECT);
                    ExceptionUtils.illegalArgs(valid, ErrorCodes.OB_USER_DB_PRIVILEGE_INVALID, param.getDbName());
                });
    }

    private boolean isUserNotExist(ObAccessor obAccessor, String username) {
        return !isUserExist(obAccessor, username);
    }

    private boolean isUserExist(ObAccessor obAccessor, String username) {
        return obAccessor.user().listUsernames().contains(username);
    }

    private boolean isOperationAllowed(ObTenantEntity tenantEntity, String username) {
        if (OcpConstants.TENANT_SYS.equals(tenantEntity.getName())) {
            return !EXCLUDE_LIST.contains(username);
        }
        return !(tenantEntity.getName().equals(systemInfo.getMetaTenantName())
                && username.equals(systemInfo.getMetaUsername()));
    }
}
