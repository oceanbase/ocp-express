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

package com.oceanbase.ocp.obsdk.operator.compaction;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.operator.CompactionOperator;
import com.oceanbase.ocp.obsdk.operator.compaction.model.ObTenantCompaction;
import com.oceanbase.ocp.obsdk.util.ObSdkUtils;

public abstract class AbstractCompactionOperator implements CompactionOperator {

    protected static final String MIN_VERSION_FOR_TENANT_COMPACTION = "4.0.0.0";

    private static final String SELECT_TENANT_MIN_COMPACTION_INFO = "SELECT tenant_id, "
            + " MIN(unix_timestamp(`start_time`)*1000000) AS min_start_time, "
            + " MAX(unix_timestamp(`finish_time`)*1000000) AS max_finish_time, "
            + " SUM(occupy_size) AS occupy_size, "
            + " SUM(total_row_count) AS total_row_count, "
            + " COUNT(1) AS tablet_count "
            + " FROM GV$OB_TABLET_COMPACTION_HISTORY "
            + " WHERE type = 'MINI_MERGE' AND unix_timestamp(`finish_time`)*1000000 > ? AND unix_timestamp(`finish_time`)*1000000 <= ? GROUP BY tenant_id;";

    private static final String SELECT_ALL_TENANT_TENANT_COMPACTIONS =
            "SELECT tenant_id, global_broadcast_scn AS broadcast_scn, is_error AS error, status,"
                    + " frozen_scn, last_scn, is_suspended AS suspend, info, start_time, last_finish_time FROM CDB_OB_MAJOR_COMPACTION";

    private static final String SELECT_TENANT_COMPACTION_INFO =
            SELECT_ALL_TENANT_TENANT_COMPACTIONS + " WHERE tenant_id = ?";

    private static final String TRIGGER_ALL_TENANTS_COMPACTIONS = "ALTER SYSTEM MAJOR FREEZE TENANT = %s";

    private static final String CLEAR_ALL_TENANTS_COMPACTION_ERROR_FLAG = "ALTER SYSTEM CLEAR MERGE ERROR TENANT = %s";

    private final ObConnectTemplate connectTemplate;

    public AbstractCompactionOperator(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public ObTenantCompaction getObTenantCompaction(Long obTenantId) {
        String version = connectTemplate.getObVersion();
        ObSdkUtils.versionShouldAfter(version, MIN_VERSION_FOR_TENANT_COMPACTION);
        List<ObTenantCompaction> compactions = connectTemplate.query(SELECT_TENANT_COMPACTION_INFO,
                new Object[] {obTenantId}, new BeanPropertyRowMapper<>(ObTenantCompaction.class));
        return CollectionUtils.isEmpty(compactions) ? null : compactions.get(0);
    }

    @Override
    public void triggerTenantsCompaction(List<String> tenants) {
        String version = connectTemplate.getObVersion();
        ObSdkUtils.versionShouldAfter(version, MIN_VERSION_FOR_TENANT_COMPACTION);
        String sql = String.format(TRIGGER_ALL_TENANTS_COMPACTIONS, String.join(",", tenants));
        connectTemplate.execute(sql);
    }

    @Override
    public void clearTenantsCompactionErrorFlag(List<String> tenants) {
        String version = connectTemplate.getObVersion();
        ObSdkUtils.versionShouldAfter(version, MIN_VERSION_FOR_TENANT_COMPACTION);
        String sql = String.format(CLEAR_ALL_TENANTS_COMPACTION_ERROR_FLAG, String.join(",", tenants));
        connectTemplate.execute(sql);
    }
}
