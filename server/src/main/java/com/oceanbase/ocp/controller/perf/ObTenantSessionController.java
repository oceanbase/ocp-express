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

package com.oceanbase.ocp.controller.perf;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oceanbase.ocp.core.response.NoDataResponse;
import com.oceanbase.ocp.core.response.PaginatedResponse;
import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.core.response.SuccessResponse;
import com.oceanbase.ocp.obsdk.operator.session.model.SessionStats;
import com.oceanbase.ocp.perf.sql.ListTenantSessionService;
import com.oceanbase.ocp.perf.sql.TenantSessionService;
import com.oceanbase.ocp.perf.sql.model.CloseSessionParam;
import com.oceanbase.ocp.perf.sql.model.TenantSession;
import com.oceanbase.ocp.perf.sql.param.QueryTenantSessionParam;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1")
public class ObTenantSessionController {

    @Resource
    private TenantSessionService tenantSessionService;
    @Resource
    private ListTenantSessionService listTenantSessionService;

    @GetMapping(value = "/ob/tenants/{tenantId:[\\d]+}/sessions", produces = {"application/json"})
    public PaginatedResponse<TenantSession> listTenantSessions(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "2147483647") Integer size,
            @PathVariable("tenantId") Long tenantId,
            @Valid @RequestParam(value = "dbUser", required = false) String dbUser,
            @Valid @RequestParam(value = "dbName", required = false) String dbName,
            @Valid @RequestParam(value = "clientIp", required = false) String clientIp,
            @Valid @RequestParam(value = "activeOnly", required = false, defaultValue = "true") Boolean activeOnly) {
        QueryTenantSessionParam param = QueryTenantSessionParam.builder().tenantId(tenantId).dbUser(dbUser)
                .dbName(dbName).clientIp(clientIp).activeOnly(activeOnly).build();
        Pageable pageable = PageRequest.of(page - 1, size, Direction.ASC, "id");
        return ResponseBuilder.paginated(listTenantSessionService.listTenantSession(param, pageable));
    }

    @GetMapping(value = "/ob/tenants/{tenantId:[\\d]+}/sessions/stats", produces = {"application/json"})
    public SuccessResponse<SessionStats> getSessionStats(@PathVariable("tenantId") Long tenantId) {
        return ResponseBuilder.single(tenantSessionService.getSessionStats(tenantId));
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/sessions/close", produces = {"application/json"})
    public NoDataResponse closeTenantSession(@PathVariable("tenantId") Long tenantId,
            @NotNull @Valid @RequestBody CloseSessionParam param) {
        tenantSessionService.killSession(tenantId, param);
        return ResponseBuilder.noData();
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/sessions/closeQuery", produces = {"application/json"})
    public NoDataResponse closeTenantQuery(@PathVariable("tenantId") Long tenantId,
            @NotNull @Valid @RequestBody CloseSessionParam param) {
        tenantSessionService.killQuery(tenantId, param);
        return ResponseBuilder.noData();
    }

    @GetMapping(value = "/ob/tenants/{tenantId:[\\d]+}/sessions/{sessionId:[\\d]+}", produces = {"application/json"})
    public SuccessResponse<TenantSession> getTenantSession(@PathVariable("tenantId") Long tenantId,
            @PathVariable("sessionId") Long sessionId) {
        QueryTenantSessionParam param =
                QueryTenantSessionParam.builder().tenantId(tenantId).sessionId(sessionId).build();
        return ResponseBuilder.single(tenantSessionService.getTenantSession(param));
    }

}
