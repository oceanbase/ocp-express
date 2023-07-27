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
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;
import com.oceanbase.ocp.obsdk.operator.session.model.ObSession;
import com.oceanbase.ocp.obsdk.operator.session.model.SessionQueryCondition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MysqlSessionAccessor extends AbstractSessionAccessor {

    private static final String KILL_SESSION = "KILL %d";
    private static final String KILL_QUERY = "KILL QUERY %d";
    private static final String QUERY_PROCESSLIST =
            "SELECT * FROM GV$OB_PROCESSLIST WHERE 1=1 %s %s %s";

    public MysqlSessionAccessor(ObConnectTemplate connectTemplate) {
        super(connectTemplate);
    }

    @Override
    public void killSession(Long sessionId) {
        Validate.notNull(sessionId, "input sessionId is null");
        connectTemplate.execute(String.format(KILL_SESSION, sessionId));
    }

    @Override
    public void killQuery(Long sessionId) {
        Validate.notNull(sessionId, "input sessionId is null");
        connectTemplate.execute(String.format(KILL_QUERY, sessionId));
    }

    @Override
    public List<ObSession> listSession(SessionQueryCondition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("Input condition is not supposed to be null.");
        }
        List<Object> param = new ArrayList<>();
        String condStr = buildConditionStr(condition, param);
        String orderStr = "";
        if (condition.getSort() != null && condition.getSort().isSorted()) {
            String orderByClause = condition.getSort().getOrders().stream()
                    .map(order -> String.format(" %s  %s", order.getProperty(), order.getDirection().name()))
                    .collect(Collectors.joining(", "));
            orderStr = " ORDER BY " + orderByClause;
        }
        String limitStr = "";
        if (condition.getPage() != null && condition.getSize() != null) {
            limitStr += String.format(" LIMIT %d,%d", condition.getPage() * condition.getSize(), condition.getSize());
        }
        String sql = String.format(QUERY_PROCESSLIST, condStr, orderStr, limitStr);
        log.debug("[obsdk] MysqlSessionOperator.listSession, sql:{}", sql);
        List<ObSession> obSessionList =
                connectTemplate.query(sql, param.toArray(), new BeanPropertyRowMapper<>(ObSession.class));
        return obSessionList.stream().peek(obSession -> {
            if (obSession.getHost() != null) {
                // the previous stop of IP reaching observer
                String hostIp = obSession.getHost().split(":")[0];
                if (obSession.getProxySessid() != null) {
                    // a non-null proxy_sessid suggests a connection from proxy
                    obSession.setProxyIp(hostIp);
                } else {
                    obSession.setUserClientIp(hostIp);
                }
            }
        }).collect(Collectors.toList());
    }
}
