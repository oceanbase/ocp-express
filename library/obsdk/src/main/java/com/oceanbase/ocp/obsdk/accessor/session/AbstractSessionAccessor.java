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

package com.oceanbase.ocp.obsdk.accessor.session;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.oceanbase.ocp.obsdk.accessor.SessionAccessor;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.operator.session.model.ObSession;
import com.oceanbase.ocp.obsdk.operator.session.model.SessionClientStats;
import com.oceanbase.ocp.obsdk.operator.session.model.SessionDbStats;
import com.oceanbase.ocp.obsdk.operator.session.model.SessionQueryCondition;
import com.oceanbase.ocp.obsdk.operator.session.model.SessionStats;
import com.oceanbase.ocp.obsdk.operator.session.model.SessionUserStats;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractSessionAccessor implements SessionAccessor {

    private static final String COUNT_PROCESSLIST =
            "SELECT COUNT(*) FROM GV$OB_PROCESSLIST WHERE 1=1 %s";

    private static final String STATS_BY_DB_USER =
            "SELECT  user  as db_user, COUNT(*) AS total_count, SUM(case state when 'ACTIVE' then 1 else 0 end) AS active_count "
                    + "FROM GV$OB_PROCESSLIST WHERE 1=1 %s GROUP BY  user  "
                    + "ORDER BY  active_count  DESC";
    private static final String STATS_BY_DB_NAME =
            "SELECT  db  as db_name, COUNT(*) AS total_count, SUM(case state when 'ACTIVE' then 1 else 0 end) AS active_count "
                    + "FROM GV$OB_PROCESSLIST WHERE 1=1 %s GROUP BY  db  "
                    + "ORDER BY  active_count  DESC";
    private static final String STATS_BY_CLIENT =
            "SELECT  user_client_ip  as client_ip, COUNT(*) AS total_count, SUM(case state when 'ACTIVE' then 1 else 0 end) AS active_count "
                    + "FROM GV$OB_PROCESSLIST WHERE 1=1 %s GROUP BY  user_client_ip  "
                    + "ORDER BY  active_count  DESC";
    private static final String STATS_SESSION =
            "SELECT COUNT(*) AS total_count, SUM(case state when 'ACTIVE' then 1 else 0 end) AS active_count FROM GV$OB_PROCESSLIST WHERE 1=1 %s";
    private static final String GET_MAX_ACTIVE_TIME =
            "SELECT MAX( time ) as max_time FROM GV$OB_PROCESSLIST WHERE  state ='ACTIVE' %s";

    private static final String QUERY_SINGLE_PROCESST =
            "SELECT * FROM GV$OB_PROCESSLIST WHERE  tenant  = ? AND  id  = ?";

    protected ObConnectTemplate connectTemplate;

    protected AbstractSessionAccessor(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public long countSession(SessionQueryCondition condition) {
        List<Object> param = new ArrayList<>();
        String condStr = buildConditionStr(condition, param);
        String sql = String.format(COUNT_PROCESSLIST, condStr);
        return connectTemplate.queryForObject(sql, param.toArray(), Long.class);
    }

    @Override
    public SessionStats getSessionStats(String tenantName) {
        String condStr = "";
        List<Object> param = new ArrayList<>();
        if (tenantName != null) {
            condStr += " AND  tenant = ?";
            param.add(tenantName);
        }
        SessionStats stats = connectTemplate.queryForObject(String.format(STATS_SESSION, condStr), param.toArray(),
                new BeanPropertyRowMapper<>(SessionStats.class));
        List<SessionUserStats> userStatsList = connectTemplate.query(String.format(STATS_BY_DB_USER, condStr),
                param.toArray(), new BeanPropertyRowMapper<>(SessionUserStats.class));
        List<SessionDbStats> dbStatsList = connectTemplate.query(String.format(STATS_BY_DB_NAME, condStr),
                param.toArray(), new BeanPropertyRowMapper<>(SessionDbStats.class));
        Long maxActiveTime = connectTemplate.queryForObject(String.format(GET_MAX_ACTIVE_TIME, condStr),
                param.toArray(), (rs, rowNum) -> rs.getLong("max_time"));

        stats.setDbStats(dbStatsList);
        stats.setUserStats(userStatsList);
        stats.setMaxActiveTime(maxActiveTime);
        List<SessionClientStats> clientStatsList = connectTemplate.query(String.format(STATS_BY_CLIENT, condStr),
                param.toArray(), new BeanPropertyRowMapper<>(SessionClientStats.class));
        stats.setClientStats(clientStatsList);
        return stats;
    }

    @Override
    public ObSession getSession(String tenantName, Long sessionId) {
        log.debug("[obsdk] MysqlSessionOperator.getSession, tenantName:{}, sessionId:{}", tenantName, sessionId);
        ObSession obSession =
                connectTemplate.queryForObject(QUERY_SINGLE_PROCESST, new Object[] {tenantName, sessionId},
                        new BeanPropertyRowMapper<>(ObSession.class));
        if (obSession == null) {
            return null;
        }
        if (obSession.getHost() != null) {
            String hostIp = obSession.getHost().split(":")[0];
            if (obSession.getProxySessid() != null) {
                obSession.setProxyIp(hostIp);
            } else {
                obSession.setUserClientIp(hostIp);
            }
        }
        return obSession;
    }

    protected String buildConditionStr(SessionQueryCondition condition, List<Object> param) {
        String sql = "";
        if (condition.getTenantName() != null) {
            sql += " AND  tenant = ?";
            param.add(condition.getTenantName());
        }
        if (condition.getDbUser() != null) {
            sql += " AND  user like ?";
            String userCondition = "%" + condition.getDbUser() + "%";
            param.add(userCondition);
        }
        if (condition.getDbName() != null) {
            sql += " AND  db like ?";
            String dbNameCondition = "%" + condition.getDbName() + "%";
            param.add(dbNameCondition);
        }
        if (condition.getActiveOnly() != null && condition.getActiveOnly()) {
            sql += " AND  state ='ACTIVE'";
        }
        if (condition.getClientIp() != null) {
            sql += " AND user_client_ip like ? ";
            String serverCondition = "%" + condition.getClientIp() + "%";
            param.add(serverCondition);
        }
        return sql;
    }

}
