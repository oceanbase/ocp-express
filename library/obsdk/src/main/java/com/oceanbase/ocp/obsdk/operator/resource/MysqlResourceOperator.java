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

package com.oceanbase.ocp.obsdk.operator.resource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.operator.ResourceOperator;
import com.oceanbase.ocp.obsdk.operator.resource.model.CreateResourcePoolInput;
import com.oceanbase.ocp.obsdk.operator.resource.model.CreateUnitConfigInput;
import com.oceanbase.ocp.obsdk.operator.resource.model.ListUnitInput;
import com.oceanbase.ocp.obsdk.operator.resource.model.ObReplicaType;
import com.oceanbase.ocp.obsdk.operator.resource.model.ObResourcePool;
import com.oceanbase.ocp.obsdk.operator.resource.model.ObUnit;
import com.oceanbase.ocp.obsdk.operator.resource.model.ObUnitConfig;
import com.oceanbase.ocp.obsdk.util.ObSdkUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MysqlResourceOperator implements ResourceOperator {

    private static final String CREATE_RESOURCE_UNIT =
            "CREATE RESOURCE UNIT `%s` MAX_CPU = ?, MIN_CPU = ?, MAX_MEMORY = ?, MIN_MEMORY = ?, MAX_IOPS = ?, "
                    + "MIN_IOPS = ?, MAX_DISK_SIZE = ?, MAX_SESSION_NUM = ?";

    private static final String CREATE_RESOURCE_UNIT_OB4 =
            "CREATE RESOURCE UNIT `%s` MAX_CPU = ?, MIN_CPU = ?, MEMORY_SIZE = ?";

    private static final String SELECT_OB_UNIT_CONFIGS = "SELECT"
            + " UNIT_CONFIG_ID, NAME, MAX_CPU, MIN_CPU, MEMORY_SIZE AS MAX_MEMORY, MEMORY_SIZE AS MIN_MEMORY, LOG_DISK_SIZE, MAX_IOPS, MIN_IOPS, IOPS_WEIGHT"
            + " FROM oceanbase.DBA_OB_UNIT_CONFIGS";

    private static final String SELECT_ALL_UNIT_CONFIG =
            "SELECT unit_config_id, name, max_cpu, min_cpu, max_memory, min_memory FROM oceanbase.__all_unit_config";

    private static final String DROP_RESOURCE_UNIT = "DROP RESOURCE UNIT IF EXISTS `%s`";

    private static final String CREATE_RESOURCE_POOL =
            "CREATE RESOURCE POOL `%s` UNIT = ?, UNIT_NUM = ?, ZONE_LIST=(%s)";

    private static final String SELECT_OB_RESOURCE_POOLS = "SELECT /*+ QUERY_TIMEOUT(60000000) */"
            + " time_to_usec(t1.MODIFY_TIME) AS UPDATE_TIME,"
            + " t1.RESOURCE_POOL_ID, t1.NAME, t1.UNIT_COUNT, t1.UNIT_CONFIG_ID, t1.ZONE_LIST, t1.TENANT_ID, t1.REPLICA_TYPE,"
            + " t2.NAME AS UNIT_CONFIG_NAME, t2.MAX_CPU, t2.MIN_CPU, t2.MEMORY_SIZE AS MAX_MEMORY, t2.MEMORY_SIZE AS MIN_MEMORY, t2.MAX_IOPS, t2.MIN_IOPS"
            + " FROM oceanbase.DBA_OB_RESOURCE_POOLS AS t1 JOIN oceanbase.DBA_OB_UNIT_CONFIGS AS t2"
            + " ON t1.UNIT_CONFIG_ID = t2.UNIT_CONFIG_ID";

    private static final String SELECT_ALL_RESOURCE_POOL =
            "SELECT  /*+ READ_CONSISTENCY(WEAK) QUERY_TIMEOUT(60000000) */  time_to_usec(t1.gmt_modified) AS update_time, t1.resource_pool_id, t1.`name`, t1.unit_count, t1.unit_config_id, t1.zone_list, t1.tenant_id,"
                    + "(CASE t1.replica_type WHEN 0 THEN 'FULL' WHEN 5 THEN 'LOGONLY' WHEN 16 THEN 'READONLY' ELSE NULL END) AS replica_type,"
                    + " t2.`name` AS unit_config_name, t2.max_cpu, t2.min_cpu, t2.max_memory, t2.min_memory"
                    + " FROM oceanbase.__all_resource_pool AS t1 "
                    + "JOIN oceanbase.__all_unit_config AS t2 ON t1.unit_config_id = t2.unit_config_id";

    private static final String ALTER_RESOURCE_POOL_UNIT = "ALTER RESOURCE POOL `%s` UNIT = ?";

    private static final String ALTER_RESOURCE_TENANT_UNIT_NUM = "ALTER RESOURCE TENANT `%s` UNIT_NUM = ?";

    private static final String ALTER_RESOURCE_POOL_SPLIT = "ALTER RESOURCE POOL `%s` SPLIT INTO (%s) ON (%s)";

    private static final String DROP_RESOURCE_POOL = "DROP RESOURCE POOL IF EXISTS `%s`";

    private static final String SELECT_ALL_UNIT =
            "SELECT  /*+ READ_CONSISTENCY(WEAK) QUERY_TIMEOUT(5000000) */ t1.unit_id, t1.resource_pool_id, t1.zone, t1.svr_ip, t1.svr_port, t1.`status`,"
                    + " (CASE t2.replica_type WHEN 0 THEN 'FULL' WHEN 5 THEN 'LOGONLY' WHEN 16 THEN 'READONLY' ELSE NULL END) AS replica_type,"
                    + " t2.tenant_id, t3.tenant_name, t2.`name` AS resource_pool_name, t2.gmt_modified AS resource_pool_update_time,"
                    + " t1.migrate_from_svr_ip, t1.migrate_from_svr_port, t1.manual_migrate"
                    + " FROM oceanbase.__all_unit AS t1"
                    + " JOIN oceanbase.__all_resource_pool AS t2 ON t1.resource_pool_id = t2.resource_pool_id"
                    + " JOIN oceanbase.__all_tenant t3 ON t2.tenant_id = t3.tenant_id WHERE 1=1";

    private static final String SELECT_OB_UNITS = "SELECT /*+ QUERY_TIMEOUT(5000000) */"
            + " t1.UNIT_ID, t1.RESOURCE_POOL_ID, t1.ZONE, t1.SVR_IP, t1.SVR_PORT, t1.STATUS, t2.REPLICA_TYPE,"
            + " t2.TENANT_ID, t3.TENANT_NAME, t2.NAME AS RESOURCE_POOL_NAME, t2.MODIFY_TIME AS RESOURCE_POOL_UPDATE_TIME,"
            + " t1.MIGRATE_FROM_SVR_IP, t1.MIGRATE_FROM_SVR_PORT, t1.MANUAL_MIGRATE"
            + " FROM oceanbase.DBA_OB_UNITS AS t1"
            + " JOIN oceanbase.DBA_OB_RESOURCE_POOLS AS t2 ON t1.RESOURCE_POOL_ID = t2.RESOURCE_POOL_ID"
            + " JOIN oceanbase.DBA_OB_TENANTS t3 ON t2.TENANT_ID = t3.TENANT_ID"
            + " WHERE 1=1";

    private static final String SELECT_ALL_UNUSED_UNIT =
            "SELECT  /*+ READ_CONSISTENCY(WEAK) QUERY_TIMEOUT(5000000) */ t1.unit_id, t1.resource_pool_id, t1.zone, t1.svr_ip, t1.svr_port, t1.`status`,"
                    + " (CASE t2.replica_type WHEN 0 THEN 'FULL' WHEN 5 THEN 'LOGONLY' WHEN 16 THEN 'READONLY' ELSE NULL END) AS replica_type,"
                    + " null AS tenant_id, null AS tenant_name, t2.`name` AS resource_pool_name, t2.gmt_modified AS resource_pool_update_time,"
                    + " t1.migrate_from_svr_ip, t1.migrate_from_svr_port, t1.manual_migrate"
                    + " FROM oceanbase.__all_unit AS t1"
                    + " JOIN oceanbase.__all_resource_pool AS t2 ON t1.resource_pool_id = t2.resource_pool_id"
                    + " WHERE t2.tenant_id = -1";

    private static final String SELECT_UNUSED_OB_UNITS = "SELECT /*+ QUERY_TIMEOUT(5000000) */"
            + " t1.UNIT_ID, t1.RESOURCE_POOL_ID, t1.ZONE, t1.SVR_IP, t1.SVR_PORT, t1.STATUS, t2.REPLICA_TYPE,"
            + " NULL AS TENANT_ID, NULL AS TENANT_NAME, t2.NAME AS RESOURCE_POOL_NAME, t2.MODIFY_TIME AS RESOURCE_POOL_UPDATE_TIME,"
            + " t1.MIGRATE_FROM_SVR_IP, t1.MIGRATE_FROM_SVR_PORT, t1.MANUAL_MIGRATE"
            + " FROM oceanbase.DBA_OB_UNITS AS t1"
            + " JOIN oceanbase.DBA_OB_RESOURCE_POOLS AS t2 ON t1.RESOURCE_POOL_ID = t2.RESOURCE_POOL_ID"
            + " WHERE t2.TENANT_ID IS NULL";

    private final ObConnectTemplate connectTemplate;

    public MysqlResourceOperator(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public ObUnitConfig createUnitConfig(CreateUnitConfigInput input) {
        Validate.notNull(input, "input can not be null");
        Validate.notEmpty(input.getName(), "input name is empty");
        Validate.notNull(input.getMaxCpu(), "input max cpu is null");
        Validate.notNull(input.getMinCpu(), "input min cpu is null");
        Validate.notNull(input.getMaxMemoryByte(), "input max memory is null");
        Validate.notNull(input.getMinMemoryByte(), "input min memory is null");

        if (ObSdkUtils.versionAfter(connectTemplate.getObVersion(), "4.0.0.0")) {
            String sql = String.format(CREATE_RESOURCE_UNIT_OB4, input.getName());
            connectTemplate.update(sql, input.getMaxCpu(), input.getMinCpu(), input.getMaxMemoryByte());
        } else {
            String sql = String.format(CREATE_RESOURCE_UNIT, input.getName());
            connectTemplate.update(sql, input.getMaxCpu(), input.getMinCpu(), input.getMaxMemoryByte(),
                    input.getMinMemoryByte(),
                    input.getMaxIops(), input.getMinIops(), input.getMaxDiskSizeByte(), input.getMaxSessionNum());
        }
        return getUnitConfig(input.getName());
    }

    private ObUnitConfig getUnitConfig(String name) {
        String sql;
        if (ObSdkUtils.versionAfter(connectTemplate.getObVersion(), "4.0.0.0")) {
            sql = SELECT_OB_UNIT_CONFIGS + " WHERE `NAME` = ?";
        } else {
            sql = SELECT_ALL_UNIT_CONFIG + " WHERE `name` = ?";
        }
        return connectTemplate.queryForObject(sql, new Object[] {name},
                new BeanPropertyRowMapper<>(ObUnitConfig.class));
    }

    @Override
    public void deleteUnitConfig(String unitConfigName) {
        Validate.notEmpty(unitConfigName, "input unit config name is empty");
        connectTemplate.execute(String.format(DROP_RESOURCE_UNIT, unitConfigName));
    }

    @Override
    public ObResourcePool createResourcePool(CreateResourcePoolInput input) {
        Validate.notNull(input, "input can not be null");
        Validate.notEmpty(input.getName(), "input name is empty");
        Validate.notEmpty(input.getUnitConfigName(), "input unit config name is empty");
        Validate.notNull(input.getUnitCount(), "input unit count is null");
        Validate.notEmpty(input.getZoneList(), "input zone list is empty");

        String sql = String.format(CREATE_RESOURCE_POOL, input.getName(),
                ObSdkUtils.toCommaSeparatedStringWithQuotationMark(input.getZoneList()));
        connectTemplate.update(sql, input.getUnitConfigName(), input.getUnitCount());
        return getResourcePool(input.getName());
    }

    private ObResourcePool getResourcePool(String name) {
        String sql;
        if (ObSdkUtils.versionAfter(connectTemplate.getObVersion(), "4.0.0.0")) {
            sql = SELECT_OB_RESOURCE_POOLS + " WHERE t1.name = ?";
        } else {
            sql = SELECT_ALL_RESOURCE_POOL + " WHERE t1.name = ?";
        }
        return connectTemplate.queryForObject(sql, new Object[] {name}, (rs, rowNum) -> resourcePoolMapRow(rs));
    }

    @Override
    public List<ObResourcePool> listResourcePool(@Nullable Long tenantId) {
        String sql;
        if (ObSdkUtils.versionAfter(connectTemplate.getObVersion(), "4.0.0.0")) {
            sql = SELECT_OB_RESOURCE_POOLS;
        } else {
            sql = SELECT_ALL_RESOURCE_POOL;
        }
        List<Object> param = new ArrayList<>();
        if (tenantId != null) {
            sql += " AND t1.tenant_id = ?";
            param.add(tenantId);
        }
        return connectTemplate.query(sql, param.toArray(), (rs, rowNum) -> resourcePoolMapRow(rs));
    }

    @Override
    public void modifyResourcePoolUnitConfig(String poolName, String unitConfigName) {
        Validate.notEmpty(poolName, "input pool name is empty");
        Validate.notEmpty(unitConfigName, "input unit config name is empty");
        connectTemplate.update(String.format(ALTER_RESOURCE_POOL_UNIT, poolName), unitConfigName);
    }

    @Override
    public void splitResourcePool(String poolName, List<String> poolList, List<String> zoneList) {
        Validate.notEmpty(poolName, "input pool name is empty");
        Validate.notEmpty(poolList, "input pool list is empty");
        Validate.notEmpty(zoneList, "input zone list is empty");
        connectTemplate.execute(String.format(ALTER_RESOURCE_POOL_SPLIT, poolName,
                ObSdkUtils.toCommaSeparatedStringWithQuotationMark(poolList),
                ObSdkUtils.toCommaSeparatedStringWithQuotationMark(zoneList)));
    }

    @Override
    public void deleteResourcePool(String poolName) {
        Validate.notEmpty(poolName, "input pool name is empty");
        log.info("start to delete resource pool, pool_name = {}", poolName);
        connectTemplate.execute(String.format(DROP_RESOURCE_POOL, poolName));
    }

    @Override
    public void modifyTenantUnitCount(String tenantName, Long unitCount) {
        ObSdkUtils.versionShouldAfter(connectTemplate.getObVersion(), "4.0.0.0");
        connectTemplate.update(String.format(ALTER_RESOURCE_TENANT_UNIT_NUM, tenantName), unitCount);
    }

    @Override
    public List<ObUnit> listUnit(ListUnitInput input) {
        Validate.notNull(input, "input can not be null");
        String sql;
        if (ObSdkUtils.versionAfter(connectTemplate.getObVersion(), "4.0.0.0")) {
            sql = SELECT_OB_UNITS;
        } else {
            sql = SELECT_ALL_UNIT;
        }
        List<Object> param = new ArrayList<>();
        if (input.getResourcePoolId() != null) {
            sql += " AND t1.resource_pool_id = ?";
            param.add(input.getResourcePoolId());
        }
        if (input.getResourcePoolName() != null) {
            sql += " AND t2.name = ?";
            param.add(input.getResourcePoolName());
        }
        if (input.getTenantId() != null) {
            sql += " AND t2.tenant_id = ?";
            param.add(input.getTenantId());
        }
        if (input.getTenantName() != null) {
            sql += " AND t3.tenant_name = ?";
            param.add(input.getTenantName());
        }
        if (input.getZone() != null) {
            sql += " AND t1.zone = ?";
            param.add(input.getZone());
        }
        return connectTemplate.query(sql, param.toArray(), new BeanPropertyRowMapper<>(ObUnit.class));
    }

    @Override
    public List<ObUnit> listUnusedUnit() {
        String sql;
        if (ObSdkUtils.versionAfter(connectTemplate.getObVersion(), "4.0.0.0")) {
            sql = SELECT_UNUSED_OB_UNITS;
        } else {
            sql = SELECT_ALL_UNUSED_UNIT;
        }
        return connectTemplate.query(sql, new BeanPropertyRowMapper<>(ObUnit.class));
    }

    private ObResourcePool resourcePoolMapRow(ResultSet rs) throws SQLException {
        ObResourcePool obResourcePool = new ObResourcePool();
        obResourcePool.setResourcePoolId(rs.getLong("resource_pool_id"));
        obResourcePool.setName(rs.getString("name"));
        obResourcePool.setUnitCount(rs.getLong("unit_count"));
        obResourcePool.setUnitConfigId(rs.getLong("unit_config_id"));
        obResourcePool.setZoneListStr(rs.getString("zone_list"));
        obResourcePool.setTenantId(rs.getLong("tenant_id"));
        obResourcePool.setReplicaType(ObReplicaType.fromValue(rs.getString("replica_type")));
        obResourcePool.setUpdateTime(rs.getLong("update_time"));
        ObUnitConfig obUnitConfig = new ObUnitConfig();
        obUnitConfig.setUnitConfigId(rs.getLong("unit_config_id"));
        obUnitConfig.setName(rs.getString("unit_config_name"));
        obUnitConfig.setMaxCpu(rs.getDouble("max_cpu"));
        obUnitConfig.setMinCpu(rs.getDouble("min_cpu"));
        obUnitConfig.setMaxMemory(rs.getLong("max_memory"));
        obUnitConfig.setMinMemory(rs.getLong("min_memory"));
        obResourcePool.setObUnitConfig(obUnitConfig);
        return obResourcePool;
    }
}
