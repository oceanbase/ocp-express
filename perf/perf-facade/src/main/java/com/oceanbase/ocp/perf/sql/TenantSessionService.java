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
package com.oceanbase.ocp.perf.sql;

import com.oceanbase.ocp.obsdk.operator.session.model.SessionStats;
import com.oceanbase.ocp.perf.sql.model.CloseSessionParam;
import com.oceanbase.ocp.perf.sql.model.TenantSession;
import com.oceanbase.ocp.perf.sql.param.QueryTenantSessionParam;

public interface TenantSessionService {

    SessionStats getSessionStats(Long tenantId);

    void killSession(Long tenantId, CloseSessionParam param);

    void killQuery(Long tenantId, CloseSessionParam param);

    TenantSession getTenantSession(QueryTenantSessionParam param);

}
