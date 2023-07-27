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

package com.oceanbase.ocp.obsdk.accessor;

import java.util.List;

import com.oceanbase.ocp.obsdk.operator.session.model.ObSession;
import com.oceanbase.ocp.obsdk.operator.session.model.SessionQueryCondition;
import com.oceanbase.ocp.obsdk.operator.session.model.SessionStats;

/**
 * Session management in user tenants.
 *
 */
public interface SessionAccessor {

    /**
     * Kill specified session, connect ob directly
     *
     * @param sessionId session id
     */
    void killSession(Long sessionId);

    /**
     * Kill the current query of specified session, connect ob directly
     *
     * @param sessionId session id
     */
    void killQuery(Long sessionId);

    /**
     * query session by condition
     *
     * @param condition query condition
     * @return list of {@link ObSession}
     */
    List<ObSession> listSession(SessionQueryCondition condition);

    /**
     * get count by condition
     *
     * @param condition query condition
     * @return total count
     */
    long countSession(SessionQueryCondition condition);

    /**
     * get the statistics of ob session
     *
     * @param tenantName name of tenant
     * @return {@link SessionStats}
     */
    SessionStats getSessionStats(String tenantName);

    /**
     * query session by condition
     *
     * @param tenantName tenantName
     * @param sessionId sessionId
     * @return {@link ObSession}
     */
    ObSession getSession(String tenantName, Long sessionId);

}
