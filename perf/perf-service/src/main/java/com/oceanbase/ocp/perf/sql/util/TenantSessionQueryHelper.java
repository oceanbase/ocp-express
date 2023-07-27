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
package com.oceanbase.ocp.perf.sql.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;

import com.oceanbase.ocp.core.util.PageableUtils;
import com.oceanbase.ocp.obsdk.operator.session.model.ObSession;
import com.oceanbase.ocp.obsdk.operator.session.model.SessionQueryCondition;
import com.oceanbase.ocp.perf.sql.model.TenantSession;
import com.oceanbase.ocp.perf.sql.param.QueryTenantSessionParam;

public class TenantSessionQueryHelper {

    public static SessionQueryCondition buildSessionQueryCondition(String tenantName, QueryTenantSessionParam param,
            Pageable pageable) {
        return SessionQueryCondition.builder()
                .tenantName(tenantName)
                .dbUser(param.getDbUser())
                .dbName(param.getDbName())
                .activeOnly(param.getActiveOnly())
                .clientIp(param.getClientIp())
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .sort(PageableUtils.buildSort(pageable.getSort()))
                .build();
    }

    public static TenantSession mapToTenantSession(ObSession session) {
        String userClient = session.getUserClientIp();
        String host = session.getHost();
        if (session.getProxySessid() == null && StringUtils.isNotEmpty(host)) {
            String ip = host.split(":")[0];
            if (ip.equals(userClient)) {
                userClient = host;
            }
        }
        return TenantSession.builder()
                .id(session.getId())
                .dbUser(session.getUser())
                .clientIp(userClient)
                .dbName(session.getDb())
                .command(session.getCommand())
                .time(session.getTime())
                .status(session.getState())
                .info(session.getInfo())
                .proxyIp(session.getProxyIp())
                .build();
    }

}
