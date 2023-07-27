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

package com.oceanbase.ocp.obsdk.operator.tenant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.oceanbase.ocp.obsdk.accessor.variable.model.SetVariableInput;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.enums.CompatibilityMode;
import com.oceanbase.ocp.obsdk.enums.RootServiceJobType;
import com.oceanbase.ocp.obsdk.enums.RootServiceJobTypeGroup;
import com.oceanbase.ocp.obsdk.enums.SysVariableName;
import com.oceanbase.ocp.obsdk.enums.VariableValueType;
import com.oceanbase.ocp.obsdk.exception.OceanBaseException;
import com.oceanbase.ocp.obsdk.operator.TenantOperator;
import com.oceanbase.ocp.obsdk.operator.tenant.model.CreateTenantInput;
import com.oceanbase.ocp.obsdk.operator.tenant.model.ObTenant;
import com.oceanbase.ocp.obsdk.operator.tenant.model.SysVariable;
import com.oceanbase.ocp.obsdk.operator.tenant.model.TenantJobProgress;
import com.oceanbase.ocp.obsdk.util.ObSdkUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MysqlTenantOperator implements TenantOperator {

    private static final String CREATE_TENANT_BASIC = "CREATE TENANT `%s` resource_pool_list=(%s)";

    private static final String SELECT_OB_TENANTS = "SELECT"
            + " TENANT_ID, TENANT_NAME, TENANT_TYPE, PRIMARY_ZONE, LOCALITY, COMPATIBILITY_MODE, STATUS, IN_RECYCLEBIN,"
            + " (CASE WHEN LOCKED = 'YES' THEN 1 ELSE 0 END) AS LOCKED,"
            + " TIMESTAMPDIFF(SECOND, CREATE_TIME, now()) AS exist_seconds"
            + " FROM oceanbase.DBA_OB_TENANTS"
            + " WHERE TENANT_TYPE IN ('SYS', 'USER')";

    private static final String DROP_TENANT = "DROP TENANT IF EXISTS `%s`";
    private static final String ALTER_TENANT = "ALTER TENANT `%s`";
    private static final String ALTER_TENANT_LOCK = "ALTER TENANT `%s` LOCK";
    private static final String ALTER_TENANT_UNLOCK = "ALTER TENANT `%s` UNLOCK";
    private static final String ALTER_TENANT_LOCALITY = "ALTER TENANT `%s` LOCALITY = ?";
    private static final String ALTER_TENANT_RESOURCE_POOL = "ALTER TENANT `%s` RESOURCE_POOL_LIST=(%s)";
    private static final String ALTER_TENANT_WHITELIST = "ALTER TENANT `%s` SET VARIABLES ob_tcp_invited_nodes = ?";
    private static final String SELECT_CDB_OB_SYS_VARIABLES =
            "SELECT TENANT_ID, NAME, VALUE FROM oceanbase.CDB_OB_SYS_VARIABLES WHERE TENANT_ID = ?";
    private static final String SELECT_LATEST_OB_TENANT_JOB = "SELECT"
            + " TENANT_ID, JOB_ID, JOB_TYPE, JOB_STATUS, PROGRESS FROM oceanbase.DBA_OB_TENANT_JOBS"
            + " WHERE TENANT_ID = ? AND JOB_TYPE = ? ORDER BY JOB_ID DESC LIMIT 1";

    private ObConnectTemplate connectTemplate;

    public MysqlTenantOperator(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public ObTenant createTenant(CreateTenantInput input) {
        Validate.notNull(input, "input can not be null");
        Validate.notEmpty(input.getName(), "input tenant name is empty");
        Validate.notEmpty(input.getResourcePoolList(), "input resource pool list is empty");
        StringBuilder sqlBuilder = new StringBuilder(String.format(CREATE_TENANT_BASIC, input.getName(),
                ObSdkUtils.toCommaSeparatedStringWithQuotationMark(input.getResourcePoolList())));
        List<Object> param = new ArrayList<>();
        if (StringUtils.isNotEmpty(input.getLocality())) {
            sqlBuilder.append(", LOCALITY = ?");
            param.add(input.getLocality());
        }
        if (StringUtils.isNotEmpty(input.getPrimaryZone())) {
            sqlBuilder.append(", PRIMARY_ZONE = ?");
            param.add(input.getPrimaryZone());
        }
        if (StringUtils.isNotEmpty(input.getCharset())) {
            sqlBuilder.append(", CHARSET = ?");
            param.add(input.getCharset());
        }
        if (StringUtils.isEmpty(input.getMode())
                || StringUtils.equalsIgnoreCase(input.getMode(), CompatibilityMode.MYSQL.toString())) {
            if (StringUtils.isNotEmpty(input.getCollation())) {
                sqlBuilder.append(", COLLATE = ?");
                param.add(input.getCollation());
            }
        }
        sqlBuilder.append(" SET ob_tcp_invited_nodes='%'");
        if (StringUtils.isNotEmpty(input.getMode())) {
            sqlBuilder.append(", ob_compatibility_mode = ?");
            param.add(input.getMode().toLowerCase());
        }



        for (SetVariableInput variable : input.getVariables()) {
            VariableValueType variableType = variable.getType();
            Object value;
            if (variableType == VariableValueType.INT || variableType == VariableValueType.NUMERIC) {
                value = Long.parseLong(variable.getValue());
            } else {
                value = variable.getValue();
            }
            sqlBuilder.append(", ").append(variable.getName()).append(" = ?");
            param.add(value);
        }
        connectTemplate.update(sqlBuilder.toString(), param.toArray());
        return getTenant(input.getName());
    }

    @Override
    public List<ObTenant> listTenant() {
        List<ObTenant> tenants = connectTemplate.query(SELECT_OB_TENANTS, new BeanPropertyRowMapper<>(ObTenant.class));
        return tenants.stream().peek(this::appendObTenant).collect(Collectors.toList());
    }

    @Override
    public ObTenant getTenant(Long obTenantId) {
        Validate.notNull(obTenantId, "input tenant id is null");
        String sql = SELECT_OB_TENANTS + " AND tenant_id = ?";
        ObTenant obTenant = connectTemplate.queryForObject(sql, new Object[] {obTenantId},
                new BeanPropertyRowMapper<>(ObTenant.class));
        appendObTenant(obTenant);
        return obTenant;
    }

    @Override
    public ObTenant getTenant(String tenantName) {
        Validate.notEmpty(tenantName, "input tenant name is empty");
        String sql = SELECT_OB_TENANTS + " AND tenant_name = ?";
        ObTenant obTenant = connectTemplate.queryForObject(sql, new Object[] {tenantName},
                new BeanPropertyRowMapper<>(ObTenant.class));
        appendObTenant(obTenant);
        return obTenant;
    }

    @Override
    public void deleteTenant(String tenantName) {
        Validate.notEmpty(tenantName, "input tenant name is empty");
        String sql = String.format(DROP_TENANT, tenantName) + " FORCE";
        connectTemplate.execute(sql);
    }

    @Override
    public void lockTenant(String tenantName) {
        Validate.notEmpty(tenantName, "input tenant name is empty");
        connectTemplate.execute(String.format(ALTER_TENANT_LOCK, tenantName));
    }

    @Override
    public void unlockTenant(String tenantName) {
        Validate.notEmpty(tenantName, "input tenant name is empty");
        connectTemplate.execute(String.format(ALTER_TENANT_UNLOCK, tenantName));
    }

    @Override
    public Optional<String> getWhitelist(Long obTenantId) {
        List<SysVariable> variableList = listSysVariable(obTenantId);
        return variableList.stream()
                .filter(t -> StringUtils.equals(t.getName(), SysVariableName.OB_TCP_INVITED_NODES.toString()))
                .findFirst()
                .map(SysVariable::getValue);
    }

    @Override
    public void modifyWhitelist(String tenantName, String whitelist) {
        Validate.notEmpty(tenantName, "input tenantName can not be empty");
        Validate.notEmpty(whitelist, "input whitelist can not be empty");
        connectTemplate.update(String.format(ALTER_TENANT_WHITELIST, tenantName), whitelist);
    }

    private List<SysVariable> listSysVariable(Long obTenantId) {
        Validate.notNull(obTenantId, "input tenant id is null");
        return connectTemplate.query(SELECT_CDB_OB_SYS_VARIABLES, new Object[] {obTenantId},
                new BeanPropertyRowMapper<>(SysVariable.class));
    }

    @Override
    public void modifyPrimaryZone(String tenantName, String primaryZone) {
        Validate.notEmpty(tenantName, "input tenant name is empty");
        Validate.notNull(primaryZone, "input primaryZone is null");
        String primaryZoneStr = primaryZone.trim();

        String sql = String.format(ALTER_TENANT, tenantName);
        List<Object> param = new ArrayList<>();
        if ("RANDOM".equalsIgnoreCase(primaryZoneStr) || "".equals(primaryZoneStr)) {

            sql += " PRIMARY_ZONE = RANDOM";
        } else {
            sql += " PRIMARY_ZONE = ?";
            param.add(primaryZoneStr);
        }

        connectTemplate.update(sql, param.toArray());
    }

    @Override
    public void modifyLocality(String tenantName, String locality) {
        Validate.notEmpty(tenantName, "input tenant name is empty");
        Validate.notNull(locality, "input locality is null");
        connectTemplate.update(String.format(ALTER_TENANT_LOCALITY, tenantName), locality);
    }

    @Override
    public void modifyResourcePoolList(String tenantName, List<String> poolList) {
        Validate.notEmpty(tenantName, "input tenant name is empty");
        Validate.notEmpty(poolList, "input pool list is empty");
        connectTemplate.execute(String.format(ALTER_TENANT_RESOURCE_POOL, tenantName,
                ObSdkUtils.toCommaSeparatedStringWithQuotationMark(poolList)));
    }

    @Override
    public TenantJobProgress getJobProgress(Long obTenantId, RootServiceJobType type) {
        Validate.notNull(obTenantId, "input tenant id is null");
        Validate.notNull(type, "input root service job type is null");
        Validate.isTrue(type.getGroup() == RootServiceJobTypeGroup.TENANT, "only tenant job type group supported");
        List<TenantJobProgress> rootServiceJobList = connectTemplate.query(SELECT_LATEST_OB_TENANT_JOB,
                new Object[] {obTenantId, type.toString()}, new BeanPropertyRowMapper<>(TenantJobProgress.class));
        return DataAccessUtils.singleResult(rootServiceJobList);
    }

    private void appendObTenant(ObTenant obTenant) {
        try {
            appendReadOnly(obTenant);
            appendCollationType(obTenant);
        } catch (OceanBaseException e) {
            log.info("exception when append ob tenant, message={}", e.getMessage());
        }
    }

    private void appendReadOnly(ObTenant obTenant) {
        List<SysVariable> variableList = listSysVariable(obTenant.getTenantId());
        Boolean readOnly = variableList.stream()
                .filter(t -> StringUtils.equals(t.getName(), SysVariableName.READ_ONLY.toString()))
                .findFirst()
                .map(SysVariable::getValue)
                .map(value -> StringUtils.equalsAnyIgnoreCase(value, "ON", "1"))
                .orElse(false);
        obTenant.setReadOnly(readOnly);
    }

    private void appendCollationType(ObTenant obTenant) {
        List<SysVariable> variableList = listSysVariable(obTenant.getTenantId());
        variableList.stream()
                .filter(t -> StringUtils.equals(t.getName(), SysVariableName.CHARACTER_SET_SERVER.toString()))
                .findFirst()
                .map(SysVariable::getValue)
                .map(Long::valueOf)
                .ifPresent(obTenant::setCollationType);
    }
}
