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

package com.oceanbase.ocp.obsdk.operator.cluster;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.enums.PartitionRole;
import com.oceanbase.ocp.obsdk.operator.ClusterOperator;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObCharset;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObCollation;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServer;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObZone;
import com.oceanbase.ocp.obsdk.operator.cluster.model.RootService;
import com.oceanbase.ocp.obsdk.operator.cluster.model.RootServiceEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MysqlClusterOperator implements ClusterOperator {

    private static final String SHOW_CHARSET = "SHOW CHARACTER SET";
    private static final String SHOW_COLLATION_4 =
            "SELECT `collation_name` AS collation, `character_set_name` AS charset, `id`, `is_default` FROM information_schema.collations";

    private static final String SELECT_VERSION_CE = "select version() REGEXP 'OceanBase[\\s_]CE'";

    private static final String SELECT_ALL_ROOT_SERVICE =
            "SELECT `svr_ip`, `svr_port`, `zone`, `role` FROM `CDB_OB_LS_LOCATIONS` WHERE `tenant_id` = 1 AND `ls_id` = 1";

    static final String SELECT_FREEZE_VERSION_FROM_ALL_CORE_TABLE =
            "select column_value from oceanbase.__all_core_table where table_name ="
                    + "'__all_global_stat' and column_name = 'frozen_version'";
    private static final String SELECT_ZONES_FROM_OB_ZONES_4 =
            "SELECT `zone`, `status`, `region`, `idc` FROM `DBA_OB_ZONES`";

    private static final String SELECT_SERVERS_FROM_ALL_SERVER_FROM_VIEW =
            "SELECT zone, svr_ip, svr_port, sql_port as inner_port, with_rootserver,                "
                    + " UPPER(`status`) as `status`, build_version,"
                    + " UNIX_TIMESTAMP(NVL(`stop_time`, 0))*1000000 as stop_time,"
                    + " UNIX_TIMESTAMP(NVL(`start_service_time`, 0))*1000000 as start_service_time,"
                    + " UNIX_TIMESTAMP(NVL(`block_migrate_in_time`, 0))*1000000 as block_migrate_in_time, id FROM DBA_OB_SERVERS ORDER BY zone ASC, id ASC";
    private static final String ALL_ROOT_SERVICE_EVENT_HISTORY_4 =
            "SELECT /*+QUERY_TIMEOUT(60000000) */ timestamp as gmt_create, module, event, name1, value1, name2, value2,"
                    + " name3, value3, name4, value4, name5, value5, name6, value6, extra_info, rs_svr_ip, rs_svr_port"
                    + " FROM DBA_OB_ROOTSERVICE_EVENT_HISTORY";

    private ObConnectTemplate connectTemplate;

    public MysqlClusterOperator(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public String getObVersion() {
        return connectTemplate.getCurrentObVersion();
    }


    @Override
    public List<ObZone> listZones() {
        return connectTemplate.query(SELECT_ZONES_FROM_OB_ZONES_4, new BeanPropertyRowMapper<>(ObZone.class));
    }

    @Override
    public List<ObServer> listServers() {
        return connectTemplate.query(SELECT_SERVERS_FROM_ALL_SERVER_FROM_VIEW,
                new BeanPropertyRowMapper<>(ObServer.class));
    }

    @Override
    public List<RootService> listRootService() {
        return connectTemplate.query(SELECT_ALL_ROOT_SERVICE, buildRootServiceRowMapper());
    }

    @Override
    public List<ObCharset> showCharset() {
        return connectTemplate.query(SHOW_CHARSET, new BeanPropertyRowMapper<>(ObCharset.class));
    }

    @Override
    public List<ObCollation> showCollation() {
        return connectTemplate.query(SHOW_COLLATION_4, new BeanPropertyRowMapper<>(ObCollation.class));
    }

    @Override
    public boolean isCommunityEdition() {
        return connectTemplate.queryForObject(SELECT_VERSION_CE, Boolean.class);
    }

    @Override
    public List<RootServiceEvent> listUnitEventDesc(List<Long> obUnitIds, Timestamp startTime, Timestamp endTime) {
        if (CollectionUtils.isEmpty(obUnitIds)) {
            return null;
        }
        String sql = ALL_ROOT_SERVICE_EVENT_HISTORY_4;
        sql += " WHERE module = 'unit' and value1 IN (:obUnitIds)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("obUnitIds", obUnitIds);
        sql = fixEventDuration(sql, params, startTime, endTime);
        sql += " ORDER BY ${CREATE_TIME_COLUMN} DESC limit 100";
        return connectTemplate.namedQuery(replaceCreateTimeColumn(sql), params,
                new BeanPropertyRowMapper<>(RootServiceEvent.class));
    }

    private String fixEventDuration(String sql, MapSqlParameterSource params, Timestamp startTime, Timestamp endTime) {
        if (Objects.nonNull(startTime)) {
            sql += " AND ${CREATE_TIME_COLUMN} > :startTime";
            params.addValue("startTime", startTime);
        }
        if (Objects.nonNull(endTime)) {
            sql += (" AND ${CREATE_TIME_COLUMN} <= :endTime");
            params.addValue("endTime", endTime);
        }
        return sql;
    }

    private String replaceCreateTimeColumn(String sql) {
        return sql.replace("${CREATE_TIME_COLUMN}", "timestamp");
    }

    private RowMapper<RootService> buildRootServiceRowMapper() {
        return (resultSet, rowNum) -> {
            RootService rs = new RootService();
            rs.setSvrIp(resultSet.getString("svr_ip"));
            rs.setSvrPort(resultSet.getInt("svr_port"));
            rs.setZone(resultSet.getString("zone"));
            String role = resultSet.getString("role");
            rs.setRole(PartitionRole.fromValue(role));
            return rs;
        };
    }

}
