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

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.common.time.DefaultOffsetDateTimeConverter;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.ob.tenant.TenantDaoManager;
import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.core.obsdk.ObAccessorFactory;
import com.oceanbase.ocp.core.property.SystemInfo;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.obops.cluster.ClusterCharsetService;
import com.oceanbase.ocp.obops.database.DatabaseService;
import com.oceanbase.ocp.obops.database.model.Database;
import com.oceanbase.ocp.obops.database.param.CreateDatabaseParam;
import com.oceanbase.ocp.obops.database.param.ModifyDatabaseParam;
import com.oceanbase.ocp.obops.tenant.TenantService;
import com.oceanbase.ocp.obops.tenant.model.ObproxyAndConnectionString;
import com.oceanbase.ocp.obsdk.accessor.ObAccessor;
import com.oceanbase.ocp.obsdk.accessor.database.model.AlterDatabaseInput;
import com.oceanbase.ocp.obsdk.accessor.database.model.CreateDatabaseInput;
import com.oceanbase.ocp.obsdk.accessor.database.model.ObDatabase;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObCollation;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DatabaseServiceImpl implements DatabaseService {

    // Databases with no privileges, granting privilege not allowed
    static final List<String> NO_PRIVILEGE_DATABASE_LIST = Collections.singletonList("information_schema");

    // Databases restricted to SELECT privilege
    static final List<String> RESTRICTED_DATABASE_LIST =
            Arrays.asList("oceanbase", "mysql", "SYS", "LBACSYS", "ORAAUDITOR");

    @Resource
    private ObAccessorFactory obAccessorFactory;

    @Resource
    private TenantDaoManager tenantDaoManager;

    @Resource
    private TenantService tenantService;

    @Resource
    private ClusterCharsetService clusterCharsetService;

    @Resource
    private SystemInfo systemInfo;

    @Override
    public List<Database> listDatabases(Long obTenantId) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkDatabaseManagementSupported(tenantEntity);

        Map<Long, ObCollation> collationMap = clusterCharsetService.getCollationMap();
        List<ObproxyAndConnectionString> connectionUrlTemplateList =
                tenantService.getConnectionUrlTemplates(obTenantId);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        return obAccessor.database()
                .listDatabases()
                .stream()
                .map(db -> mapToModel(db, collationMap))
                .peek(db -> attachConnectionUrlList(db, connectionUrlTemplateList))
                .collect(Collectors.toList());
    }

    @Override
    public Database createDatabase(Long obTenantId, CreateDatabaseParam param) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkDatabaseManagementSupported(tenantEntity);

        List<ObCollation> collations = clusterCharsetService.listCollations();
        validateCollation(collations, param.getCollation());

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        ExceptionUtils.illegalArgs(isDbNotExist(obAccessor, param.getDbName()), ErrorCodes.OB_DATABASE_NAME_EXISTS,
                param.getDbName());

        CreateDatabaseInput input = CreateDatabaseInput.builder()
                .name(param.getDbName())
                .collation(param.getCollation())
                .readonly(param.getReadonly())
                .build();
        ObDatabase obDatabase = obAccessor.database().createDatabase(input);
        Map<Long, ObCollation> collationMap = collations.stream().collect(Collectors.toMap(ObCollation::getId, t -> t));
        return mapToModel(obDatabase, collationMap);
    }

    @Override
    public Database modifyDatabase(Long obTenantId, String dbName, ModifyDatabaseParam param) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkDatabaseManagementSupported(tenantEntity);
        checkDatabaseOperationAllowed(tenantEntity.getName(), dbName);

        param.validate();

        List<ObCollation> collations = clusterCharsetService.listCollations();
        validateCollation(collations, param.getCollation());

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        ExceptionUtils.illegalArgs(isDbExist(obAccessor, dbName), ErrorCodes.OB_DATABASE_NAME_NOT_FOUND, dbName);

        AlterDatabaseInput input = AlterDatabaseInput.builder()
                .name(dbName)
                .collation(param.getCollation())
                .readonly(param.getReadonly())
                .build();
        ObDatabase obDatabase = obAccessor.database().alterDatabase(input);
        Map<Long, ObCollation> collationMap = collations.stream().collect(Collectors.toMap(ObCollation::getId, t -> t));
        return mapToModel(obDatabase, collationMap);
    }

    @Override
    public void deleteDatabase(Long obTenantId, String dbName) {
        ObTenantEntity tenantEntity = tenantDaoManager.nullSafeGetObTenant(obTenantId);
        checkDatabaseManagementSupported(tenantEntity);
        checkDatabaseOperationAllowed(tenantEntity.getName(), dbName);

        ObAccessor obAccessor = obAccessorFactory.createObAccessor(obTenantId);
        ExceptionUtils.illegalArgs(isDbExist(obAccessor, dbName), ErrorCodes.OB_DATABASE_NAME_NOT_FOUND, dbName);
        obAccessor.database().dropDatabase(dbName);
    }

    private void checkDatabaseManagementSupported(ObTenantEntity tenantEntity) {
        ExceptionUtils.illegalArgs(tenantEntity.getMode() != TenantMode.ORACLE,
                ErrorCodes.OB_DATABASE_ORACLE_MODE_NOT_SUPPORTED);
    }

    private void checkDatabaseOperationAllowed(String tenantName, String dbName) {
        ExceptionUtils.illegalArgs(!RESTRICTED_DATABASE_LIST.contains(dbName),
                ErrorCodes.OB_DATABASE_OPERATION_NOT_ALLOW, dbName);
        // meta tenant's meta database can be modified.
        ExceptionUtils.illegalArgs(
                !(tenantName.equals(systemInfo.getMetaTenantName()) && dbName.equals(systemInfo.getMetaDatabaseName())),
                ErrorCodes.OB_DATABASE_OPERATION_NOT_ALLOW, dbName);
    }

    private void validateCollation(List<ObCollation> collations, String collation) {
        if (StringUtils.isEmpty(collation)) {
            return;
        }
        boolean valid = collations.stream().anyMatch(t -> StringUtils.equals(t.getCollation(), collation));
        ExceptionUtils.illegalArgs(valid, ErrorCodes.OB_DATABASE_COLLATION_NOT_VALID);
    }

    private void attachConnectionUrlList(Database database,
            List<ObproxyAndConnectionString> templates) {
        List<ObproxyAndConnectionString> connectionUrls = templates.stream()
                .map(template -> template.formatConnectionString(database.getDbName()))
                .collect(Collectors.toList());
        database.setConnectionUrls(connectionUrls);
    }

    private Database mapToModel(ObDatabase obDatabase, Map<Long, ObCollation> collationMap) {
        Database database = new Database();
        database.setDbName(obDatabase.getName());
        database.setReadonly(obDatabase.isReadonly());
        Timestamp timestamp = obDatabase.getGmtCreate();
        OffsetDateTime offsetDateTime = DefaultOffsetDateTimeConverter.fromTimestamp(timestamp);
        database.setCreateTime(offsetDateTime);
        ObCollation collation = collationMap.get(obDatabase.getCollationType());
        if (collation != null) {
            database.setCharset(collation.getCharset());
            database.setCollation(collation.getCollation());
        }
        return database;
    }

    private boolean isDbNotExist(ObAccessor obAccessor, String dbName) {
        return !isDbExist(obAccessor, dbName);
    }

    private boolean isDbExist(ObAccessor obAccessor, String dbName) {
        List<ObDatabase> dbList = obAccessor.database().listDatabases();
        return dbList.stream().anyMatch(db -> StringUtils.equals(db.getName(), dbName));
    }
}
