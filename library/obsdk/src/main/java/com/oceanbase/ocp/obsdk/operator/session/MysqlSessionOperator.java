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

package com.oceanbase.ocp.obsdk.operator.session;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.operator.SessionOperator;
import com.oceanbase.ocp.obsdk.operator.session.model.ObSession;
import com.oceanbase.ocp.obsdk.operator.session.model.SessionQueryCondition;
import com.oceanbase.ocp.obsdk.util.ObSdkUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MysqlSessionOperator implements SessionOperator {

    private static final String COUNT_PROCESSLIST =
            "SELECT COUNT(*) FROM oceanbase.__all_virtual_processlist WHERE 1=1 %s";

    private static final String QUERY_SINGLE_PROCESST =
            "SELECT /*+ READ_CONSISTENCY(WEAK) */ * FROM oceanbase.__all_virtual_processlist WHERE `tenant` = ? AND `id` = ?";

    private static final String QUERY_DEAD_LOCK_HISTORY =
            "SELECT /*+ READ_CONSISTENCY(WEAK) */ event_id, svr_ip, svr_port, report_time as report_time_us, cycle_idx as cycle_index, cycle_size, role, visitor, resource, extra_name1, extra_value1"
                    + " FROM oceanbase.__all_virtual_deadlock_event_history WHERE `tenant_id` = ?";


    private ObConnectTemplate connectTemplate;

    public MysqlSessionOperator(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public long countSession(SessionQueryCondition condition) {
        List<Object> param = new ArrayList<>();
        String condStr = buildConditionStr(condition, param);
        String sql = String.format(COUNT_PROCESSLIST, condStr);
        return connectTemplate.queryForObject(sql, param.toArray(), Long.class);
    }

    private String buildConditionStr(SessionQueryCondition condition, List<Object> param) {
        String sql = "";
        if (condition.getTenantName() != null) {
            sql += " AND `tenant`= ?";
            param.add(condition.getTenantName());
        }
        if (condition.getDbUser() != null) {
            sql += " AND `user`= ?";
            param.add(condition.getDbUser());
        }
        if (condition.getDbName() != null) {
            sql += " AND `db`= ?";
            param.add(condition.getDbName());
        }
        if (condition.getActiveOnly() != null && condition.getActiveOnly()) {
            sql += " AND `state`='ACTIVE'";
        }
        if (condition.getClientIp() != null) {
            sql += " AND `host` like '%" + condition.getClientIp() + "%'";
        }
        return sql;
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
                if (ObSdkUtils.versionBefore(connectTemplate.getObVersion(), "2.2.30")) {
                    obSession.setUserClientIp(hostIp);
                }
            }
        }
        return obSession;
    }
}
