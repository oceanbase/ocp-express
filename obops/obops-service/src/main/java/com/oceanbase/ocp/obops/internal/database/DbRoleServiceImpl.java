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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.common.time.DefaultOffsetDateTimeConverter;
import com.oceanbase.ocp.core.exception.IllegalArgumentException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.obsdk.ObAccessorFactory;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obops.database.DbRoleService;
import com.oceanbase.ocp.obops.database.model.DbRole;
import com.oceanbase.ocp.obops.database.model.Grantee;
import com.oceanbase.ocp.obops.database.param.CreateDbRoleParam;
import com.oceanbase.ocp.obsdk.accessor.ObAccessor;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObRole;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObjectPrivilege;
import com.oceanbase.ocp.obsdk.enums.GlobalPrivilege;
import com.oceanbase.ocp.obsdk.enums.ObjectPrivilegeType;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DbRoleServiceImpl implements DbRoleService {

    @Resource
    private DbPrivilegeManager dbPrivilegeManager;

    @Resource
    private ObAccessorFactory obAccessorFactory;

    @Resource
    private TenantDaoManager tenantDaoManager;

    @Override
    public List<DbRole> listRoles(Long obTenantId) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        List<ObRole> obRoles = obAccessor.user().listRoles();
        return obRoles.stream()
                .map(this::mapToModel)
                .collect(Collectors.toList());
    }

    @Override
    public DbRole getRole(Long obTenantId, String roleName) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        ObRole obRole = obAccessor.user().getRole(roleName);
        return mapToModel(obRole);
    }

    @Override
    public DbRole createRole(Long obTenantId, CreateDbRoleParam param) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);

        // Role names are force to be uppercase in creation.
        String roleName = StringUtils.upperCase(param.getRoleName());

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkRoleNotExist(obAccessor, roleName);

        obAccessor.user().createRole(roleName);
        if (CollectionUtils.isNotEmpty(param.getGlobalPrivileges())) {
            grantGlobalPrivilege(obTenantId, roleName, param.getGlobalPrivileges());
        }
        if (CollectionUtils.isNotEmpty(param.getRoles())) {
            grantRole(obTenantId, roleName, param.getRoles());
        }
        ObRole obRole = obAccessor.user().getRole(roleName);
        return mapToModel(obRole);
    }

    @Override
    public void deleteRole(Long obTenantId, String roleName) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkRoleExist(obAccessor, roleName);
        obAccessor.user().dropRole(roleName);
    }

    @Override
    public void grantGlobalPrivilege(Long obTenantId, String roleName, List<String> privilegeStrings) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);
        List<GlobalPrivilege> privileges =
                DbPrivilegeUtils.parseGlobalPrivileges(privilegeStrings, tenantEntity.getMode());

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkRoleExist(obAccessor, roleName);
        dbPrivilegeManager.grantGlobalPrivilege(obTenantId, Grantee.role(roleName), privileges);
    }

    @Override
    public void revokeGlobalPrivilege(Long obTenantId, String roleName, List<String> privilegeStrings) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);
        List<GlobalPrivilege> privileges =
                DbPrivilegeUtils.parseGlobalPrivileges(privilegeStrings, tenantEntity.getMode());

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkRoleExist(obAccessor, roleName);
        dbPrivilegeManager.revokeGlobalPrivilege(obTenantId, Grantee.role(roleName), privileges);
    }

    @Override
    public void modifyGlobalPrivilege(Long obTenantId, String roleName, List<String> privilegeStrings) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);
        List<GlobalPrivilege> privileges =
                DbPrivilegeUtils.parseGlobalPrivileges(privilegeStrings, tenantEntity.getMode());

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkRoleExist(obAccessor, roleName);
        ObRole role = obAccessor.user().getRole(roleName);
        dbPrivilegeManager.modifyGlobalPrivilege(obTenantId, Grantee.role(roleName), role.getGlobalPrivileges(),
                privileges);
    }

    @Override
    public void grantRole(Long obTenantId, String roleName, List<String> roles) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkRoleExist(obAccessor, roleName);
        dbPrivilegeManager.grantRole(obTenantId, Grantee.role(roleName), roles);
    }

    @Override
    public void revokeRole(Long obTenantId, String roleName, List<String> roles) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkRoleExist(obAccessor, roleName);
        dbPrivilegeManager.revokeRole(obTenantId, Grantee.role(roleName), roles);
    }

    @Override
    public void modifyRole(Long obTenantId, String roleName, List<String> roles) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkRoleExist(obAccessor, roleName);
        ObRole role = obAccessor.user().getRole(roleName);
        dbPrivilegeManager.modifyRole(obTenantId, Grantee.role(roleName), role.getGrantedRoles(), roles);
    }

    @Override
    public void grantObjectPrivilege(Long obTenantId, String roleName, List<ObjectPrivilege> objectPrivileges) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);
        checkObjectPrivileges(objectPrivileges);
        checkRoleObjectPrivilege(objectPrivileges);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkRoleExist(obAccessor, roleName);
        dbPrivilegeManager.grantObjectPrivilege(obTenantId, Grantee.role(roleName), objectPrivileges);
    }

    @Override
    public void revokeObjectPrivilege(Long obTenantId, String roleName, List<ObjectPrivilege> objectPrivileges) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);
        checkObjectPrivileges(objectPrivileges);
        checkRoleObjectPrivilege(objectPrivileges);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkRoleExist(obAccessor, roleName);
        dbPrivilegeManager.revokeObjectPrivilege(obTenantId, Grantee.role(roleName), objectPrivileges);
    }

    @Override
    public void modifyObjectPrivilege(Long obTenantId, String roleName, List<ObjectPrivilege> objectPrivileges) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkOraclePrivilegeManagementSupported(tenantEntity);
        checkObjectPrivileges(objectPrivileges);
        checkRoleObjectPrivilege(objectPrivileges);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        checkRoleExist(obAccessor, roleName);
        ObRole role = obAccessor.user().getRole(roleName);
        dbPrivilegeManager.modifyObjectPrivilege(obTenantId, Grantee.role(roleName), role.getObjectPrivileges(),
                objectPrivileges);
    }

    private void checkOraclePrivilegeManagementSupported(ObTenantEntity tenantEntity) {
        ExceptionUtils.illegalArgs(tenantEntity.isOracleMode(), ErrorCodes.OB_USER_MYSQL_MODE_NOT_SUPPORTED);
    }

    private DbRole mapToModel(ObRole obRole) {
        DbRole dbRole = new DbRole();
        dbRole.setName(obRole.getName());
        dbRole.setCreateTime(DefaultOffsetDateTimeConverter.fromTimestamp(obRole.getCreateTime()));
        dbRole.setUpdateTime(DefaultOffsetDateTimeConverter.fromTimestamp(obRole.getUpdateTime()));
        dbRole.setGlobalPrivileges(obRole.getGlobalPrivileges());
        dbRole.setGrantedRoles(obRole.getGrantedRoles());
        dbRole.setUserGrantees(obRole.getUserGrantees());
        dbRole.setRoleGrantees(obRole.getRoleGrantees());
        dbRole.setObjectPrivileges(obRole.getObjectPrivileges());
        return dbRole;
    }

    private void checkRoleExist(ObAccessor obAccessor, String roleName) {
        ExceptionUtils.illegalArgs(isRoleExist(obAccessor, roleName), ErrorCodes.OB_ROLE_NAME_NOT_FOUND, roleName);
    }

    private void checkRoleNotExist(ObAccessor obAccessor, String roleName) {
        ExceptionUtils.illegalArgs(!isRoleExist(obAccessor, roleName), ErrorCodes.OB_ROLE_NAME_EXISTS, roleName);
    }

    private boolean isRoleExist(ObAccessor obAccessor, String roleName) {
        List<ObRole> roleList = obAccessor.user().listRoles();
        return roleList.stream().anyMatch(role -> StringUtils.equals(role.getName(), roleName));
    }

    private void checkRoleObjectPrivilege(List<ObjectPrivilege> objectPrivileges) {
        List<ObjectPrivilegeType> unsupportedObjectPrivilegeTypes = objectPrivileges.stream()
                .map(ObjectPrivilege::getPrivileges)
                .flatMap(Collection::stream)
                .filter(objectPrivilegeType -> !objectPrivilegeType.roleSupported())
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(unsupportedObjectPrivilegeTypes)) {
            throw new IllegalArgumentException(ErrorCodes.OB_ROLE_OBJECT_PRIVILEGE_INVALID,
                    unsupportedObjectPrivilegeTypes.stream()
                            .map(ObjectPrivilegeType::getValue)
                            .collect(Collectors.joining(", ", "[", "]")));
        }
    }
}
