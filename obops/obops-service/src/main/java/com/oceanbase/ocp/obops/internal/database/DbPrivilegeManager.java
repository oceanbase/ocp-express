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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import com.oceanbase.ocp.common.lang.Pair;
import com.oceanbase.ocp.core.obsdk.ObAccessorFactory;
import com.oceanbase.ocp.obops.database.model.Grantee;
import com.oceanbase.ocp.obops.internal.database.operation.DbPrivilegeOperation;
import com.oceanbase.ocp.obops.internal.database.operation.GlobalPrivilegeOperation;
import com.oceanbase.ocp.obops.internal.database.operation.ObjectPrivilegeOperation;
import com.oceanbase.ocp.obops.internal.database.operation.PrivilegeOperation;
import com.oceanbase.ocp.obops.internal.database.operation.PrivilegeOperation.PrivilegeOperationType;
import com.oceanbase.ocp.obops.internal.database.operation.RolePrivilegeOperation;
import com.oceanbase.ocp.obsdk.accessor.UserAccessor;
import com.oceanbase.ocp.obsdk.accessor.user.model.DbObject;
import com.oceanbase.ocp.obsdk.accessor.user.model.DbPrivilege;
import com.oceanbase.ocp.obsdk.accessor.user.model.ObjectPrivilege;
import com.oceanbase.ocp.obsdk.enums.DbPrivType;
import com.oceanbase.ocp.obsdk.enums.GlobalPrivilege;
import com.oceanbase.ocp.obsdk.enums.ObjectPrivilegeType;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DbPrivilegeManager {

    @Autowired
    private ObAccessorFactory obAccessorFactory;

    public void grantGlobalPrivilege(Long tenantId, Grantee grantee, List<GlobalPrivilege> globalPrivileges) {
        PrivilegeOperation operation = GlobalPrivilegeOperation.builder()
                .tenantId(tenantId)
                .grantee(grantee)
                .operationType(PrivilegeOperationType.GRANT)
                .globalPrivileges(globalPrivileges)
                .build();
        operatePrivilege(operation);
        log.info("Grant privilege done, tenantId={}, grantee={}, privileges={}", tenantId, grantee, globalPrivileges);
    }

    public void revokeGlobalPrivilege(Long tenantId, Grantee grantee, List<GlobalPrivilege> globalPrivileges) {
        PrivilegeOperation operation = GlobalPrivilegeOperation.builder()
                .tenantId(tenantId)
                .grantee(grantee)
                .operationType(PrivilegeOperationType.REVOKE)
                .globalPrivileges(globalPrivileges)
                .build();
        operatePrivilege(operation);
        log.info("revoke privilege done, tenantId={}, grantee={}, privileges={}", tenantId, grantee, globalPrivileges);
    }

    public void modifyGlobalPrivilege(Long tenantId, Grantee grantee, List<GlobalPrivilege> currentPrivileges,
            List<GlobalPrivilege> targetPrivileges) {
        List<PrivilegeOperation> operations = operationPrimitives(currentPrivileges, targetPrivileges).stream()
                .map(primitive -> GlobalPrivilegeOperation.builder()
                        .tenantId(tenantId)
                        .grantee(grantee)
                        .operationType(primitive.getLeft())
                        .globalPrivileges(primitive.getRight())
                        .build())
                .collect(Collectors.toList());
        operatePrivilege(operations);
        log.info("Modify privilege done, tenantId={}, grantee={}, current privileges={}, target privileges={}",
                tenantId, grantee, currentPrivileges, targetPrivileges);
    }

    public void grantRole(Long tenantId, Grantee grantee, List<String> roles) {
        PrivilegeOperation operation = RolePrivilegeOperation.builder()
                .tenantId(tenantId)
                .grantee(grantee)
                .operationType(PrivilegeOperationType.GRANT)
                .roles(roles)
                .build();
        operatePrivilege(operation);
        log.info("grant privilege done, tenantId={}, grantee={}, privileges={}", tenantId, grantee, roles);
    }

    public void revokeRole(Long tenantId, Grantee grantee, List<String> roles) {
        PrivilegeOperation operation = RolePrivilegeOperation.builder()
                .tenantId(tenantId)
                .grantee(grantee)
                .operationType(PrivilegeOperationType.REVOKE)
                .roles(roles)
                .build();
        operatePrivilege(operation);
        log.info("revoke privilege done, tenantId={}, grantee={}, privileges={}", tenantId, grantee, roles);
    }

    public void modifyRole(Long tenantId, Grantee grantee, List<String> currentRoles, List<String> targetRoles) {
        List<PrivilegeOperation> operations = operationPrimitives(currentRoles, targetRoles).stream()
                .map(primitive -> RolePrivilegeOperation.builder()
                        .tenantId(tenantId)
                        .grantee(grantee)
                        .operationType(primitive.getLeft())
                        .roles(primitive.getRight())
                        .build())
                .collect(Collectors.toList());
        operatePrivilege(operations);
        log.info("Modify privilege done, tenantId={}, grantee={}, current privileges={}, target privileges={}",
                tenantId, grantee, currentRoles, targetRoles);
    }

    public void grantDbPrivilege(Long tenantId, Grantee grantee, List<DbPrivilege> dbPrivileges) {
        List<PrivilegeOperation> operations = dbPrivileges.stream()
                .map(dbPrivilege -> DbPrivilegeOperation.builder()
                        .tenantId(tenantId)
                        .grantee(grantee)
                        .operationType(PrivilegeOperationType.GRANT)
                        .dbName(dbPrivilege.getDbName())
                        .dbPrivileges(dbPrivilege.getPrivileges())
                        .build())
                .collect(Collectors.toList());
        operatePrivilege(operations);
        log.info("grant privilege done, tenantId={}, grantee={}, privileges={}", tenantId, grantee, dbPrivileges);
    }

    public void modifyDbPrivilege(Long tenantId, Grantee grantee, List<DbPrivilege> currentDbPrivileges,
            List<DbPrivilege> targetDbPrivileges) {
        Map<String, List<DbPrivType>> currentPrivilegesMap = currentDbPrivileges.stream()
                .collect(Collectors.toMap(DbPrivilege::getDbName, DbPrivilege::getPrivileges));
        List<PrivilegeOperation> operations = new ArrayList<>();
        for (DbPrivilege dbPrivilege : targetDbPrivileges) {
            String dbName = dbPrivilege.getDbName();
            List<DbPrivType> currentPrivileges = currentPrivilegesMap.getOrDefault(dbName, Collections.emptyList());
            List<DbPrivType> targetPrivileges = dbPrivilege.getPrivileges();

            List<PrivilegeOperation> ops = operationPrimitives(currentPrivileges, targetPrivileges).stream()
                    .map(primitive -> DbPrivilegeOperation.builder()
                            .tenantId(tenantId)
                            .grantee(grantee)
                            .operationType(primitive.getLeft())
                            .dbName(dbName)
                            .dbPrivileges(primitive.getRight())
                            .build())
                    .collect(Collectors.toList());
            operations.addAll(ops);
        }
        operatePrivilege(operations);
        log.info("Modify privilege done, tenantId={}, grantee={}, current privileges={}, target privileges={}",
                tenantId, grantee, currentDbPrivileges, targetDbPrivileges);
    }

    public void grantObjectPrivilege(Long tenantId, Grantee grantee, List<ObjectPrivilege> objectPrivileges) {
        List<PrivilegeOperation> operations = objectPrivileges.stream()
                .map(objectPrivilege -> ObjectPrivilegeOperation.builder()
                        .tenantId(tenantId)
                        .grantee(grantee)
                        .operationType(PrivilegeOperationType.GRANT)
                        .dbObject(objectPrivilege.getObject())
                        .objectPrivileges(objectPrivilege.getPrivileges())
                        .build())
                .collect(Collectors.toList());
        operatePrivilege(operations);
        log.info("Grant privilege done, tenantId={}, grantee={}, privileges={}", tenantId, grantee, objectPrivileges);
    }

    public void revokeObjectPrivilege(Long tenantId, Grantee grantee, List<ObjectPrivilege> objectPrivileges) {
        List<PrivilegeOperation> operations = objectPrivileges.stream()
                .map(objectPrivilege -> ObjectPrivilegeOperation.builder()
                        .tenantId(tenantId)
                        .grantee(grantee)
                        .operationType(PrivilegeOperationType.REVOKE)
                        .dbObject(objectPrivilege.getObject())
                        .objectPrivileges(objectPrivilege.getPrivileges())
                        .build())
                .collect(Collectors.toList());
        operatePrivilege(operations);
        log.info("Revoke privilege done, tenantId={}, grantee={}, privileges={}", tenantId, grantee, objectPrivileges);
    }

    public void modifyObjectPrivilege(Long tenantId, Grantee grantee, List<ObjectPrivilege> currentObjectPrivileges,
            List<ObjectPrivilege> targetObjectPrivileges) {
        Map<DbObject, List<ObjectPrivilegeType>> currentPrivilegesMap = currentObjectPrivileges.stream()
                .collect(Collectors.toMap(ObjectPrivilege::getObject, ObjectPrivilege::getPrivileges));
        List<PrivilegeOperation> operations = new ArrayList<>();
        for (ObjectPrivilege objectPrivilege : targetObjectPrivileges) {
            DbObject dbObject = objectPrivilege.getObject();
            List<ObjectPrivilegeType> currentPrivileges =
                    currentPrivilegesMap.getOrDefault(dbObject, Collections.emptyList());
            List<ObjectPrivilegeType> targetPrivileges = objectPrivilege.getPrivileges();

            List<PrivilegeOperation> ops = operationPrimitives(currentPrivileges, targetPrivileges).stream()
                    .map(primitive -> ObjectPrivilegeOperation
                            .builder()
                            .tenantId(tenantId)
                            .grantee(grantee)
                            .operationType(primitive.getLeft())
                            .dbObject(dbObject)
                            .objectPrivileges(primitive.getRight())
                            .build())
                    .collect(Collectors.toList());
            operations.addAll(ops);
        }
        operatePrivilege(operations);
        log.info("Modify privilege done, tenantId={}, grantee={}, current privileges={}, target privileges={}",
                tenantId, grantee, currentObjectPrivileges, targetObjectPrivileges);
    }

    private void operatePrivilege(PrivilegeOperation operation) {
        operateSinglePrivilege(operation);
    }

    private void operatePrivilege(List<PrivilegeOperation> operations) {
        for (PrivilegeOperation operation : operations) {
            operateSinglePrivilege(operation);
        }
    }

    private void operateSinglePrivilege(PrivilegeOperation operation) {
        if (!operation.hasPrivileges()) {
            log.info("Privilege operation skipped: {}, empty privilege list, tenantId={}",
                    operation.getDescription(), operation.getTenantId());
            return;
        }

        Long tenantId = operation.getTenantId();
        UserAccessor userAccessor = obAccessorFactory.createObAccessor(tenantId).user();
        operation.operate(userAccessor);
    }

    // Calculate granting and revoking operations according to current and target
    // privileges.
    private <T> List<Pair<PrivilegeOperationType, List<T>>> operationPrimitives(List<T> currentPrivileges,
            List<T> targetPrivileges) {
        if (CollectionUtils.isEmpty(currentPrivileges)) {
            return Collections.singletonList(Pair.of(PrivilegeOperationType.GRANT, targetPrivileges));
        }
        if (CollectionUtils.isEmpty(targetPrivileges)) {
            return Collections.singletonList(Pair.of(PrivilegeOperationType.REVOKE, currentPrivileges));
        }
        Set<T> currentPrivilegeSet = new HashSet<>(currentPrivileges);
        Set<T> targetPrivilegeSet = new HashSet<>(targetPrivileges);
        ArrayList<T> privilegesToGrant =
                new ArrayList<>(Sets.difference(targetPrivilegeSet, currentPrivilegeSet));
        ArrayList<T> privilegesToRevoke =
                new ArrayList<>(Sets.difference(currentPrivilegeSet, targetPrivilegeSet));
        return Arrays.asList(Pair.of(PrivilegeOperationType.REVOKE, privilegesToRevoke),
                Pair.of(PrivilegeOperationType.GRANT, privilegesToGrant));
    }
}
