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

import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oceanbase.ocp.core.response.IterableResponse;
import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.core.response.SuccessResponse;
import com.oceanbase.ocp.obsdk.operator.sql.param.QuerySlowSqlRankParam;
import com.oceanbase.ocp.perf.sql.SqlAuditRawStatService;
import com.oceanbase.ocp.perf.sql.SqlTextService;
import com.oceanbase.ocp.perf.sql.model.SlowSqlRankInfo;
import com.oceanbase.ocp.perf.sql.model.SqlAuditStatSummary;
import com.oceanbase.ocp.perf.sql.model.SqlText;
import com.oceanbase.ocp.perf.sql.param.QuerySqlTextParam;
import com.oceanbase.ocp.perf.sql.param.QueryTopSqlParam;

@RestController
@Validated
@RequestMapping("/api/v1/ob")
public class ObSqlStatController {

    @Autowired
    private SqlAuditRawStatService sqlAuditRawStatService;

    @Autowired
    private SqlTextService sqlTextService;

    @GetMapping(value = "/tenants/{tenantId}/topSql", produces = {"application/json"})
    public IterableResponse<SqlAuditStatSummary> topSql(
            @PathVariable("tenantId") Long tenantId,
            @RequestParam(value = "startTime") OffsetDateTime startTime,
            @RequestParam(value = "endTime") OffsetDateTime endTime,
            @RequestParam(value = "server", required = false) String server,
            @RequestParam(value = "inner", required = false) Boolean inner,
            @RequestParam(value = "sqlText", required = false) String sqlText,
            @RequestParam(value = "limit", required = false) Long limit,
            @RequestParam(value = "filterExpression", required = false) String filterExpression) {
        QueryTopSqlParam.Builder builder = QueryTopSqlParam.builder()
                .tenantId(tenantId)
                .startTime(startTime).endTime(endTime)
                .server(server).sqlText(sqlText)
                .limit(limit)
                .filterExpression(filterExpression);
        if (inner != null) {
            builder.inner(inner);
        }
        return ResponseBuilder.iterable(sqlAuditRawStatService.topSql(builder.build()));
    }

    @GetMapping(value = "/tenants/{tenantId}/slowSql", produces = {"application/json"})
    public IterableResponse<SqlAuditStatSummary> slowSql(
            @PathVariable("tenantId") Long tenantId,
            @RequestParam(value = "startTime") OffsetDateTime startTime,
            @RequestParam(value = "endTime") OffsetDateTime endTime,
            @RequestParam(value = "server", required = false) String server,
            @RequestParam(value = "inner", required = false) Boolean inner,
            @RequestParam(value = "sqlText", required = false) String sqlText,
            @RequestParam(value = "limit", required = false) Long limit,
            @RequestParam(value = "sqlTextLength", required = false) Integer sqlTextLength,
            @RequestParam(value = "filterExpression", required = false) String filterExpression) {
        QueryTopSqlParam.Builder builder = QueryTopSqlParam.builder()
                .tenantId(tenantId)
                .startTime(startTime)
                .endTime(endTime)
                .sqlText(sqlText)
                .limit(limit)
                .server(server)
                .filterExpression(filterExpression)
                .sqlTextLength(sqlTextLength);
        if (inner != null) {
            builder.inner(inner);
        }
        return ResponseBuilder.iterable(sqlAuditRawStatService.slowSql(builder.build()));
    }

    @GetMapping(value = "/slowSql/top", produces = {"application/json"})
    public IterableResponse<SlowSqlRankInfo> slowSqlRank(
            @RequestParam(value = "startTime") OffsetDateTime startTime,
            @RequestParam(value = "endTime") OffsetDateTime endTime,
            @RequestParam(value = "top") Integer top) {
        QuerySlowSqlRankParam param = QuerySlowSqlRankParam.builder().startTime(startTime).endTime(endTime)
                .top(top).build();
        return ResponseBuilder.iterable(sqlAuditRawStatService.getSlowSqlRank(param));
    }

    @GetMapping(value = "/tenants/{tenantId}/dbName/{dbName}/sqls/{sqlId}/text", produces = {"application/json"})
    public SuccessResponse<SqlText> sqlText(
            @PathVariable("tenantId") Long tenantId,
            @PathVariable("dbName") String dbName,
            @PathVariable("sqlId") String sqlId,
            @RequestParam(value = "startTime") OffsetDateTime startTime,
            @RequestParam(value = "endTime") OffsetDateTime endTime) {
        return ResponseBuilder.single(sqlTextService.getAny(QuerySqlTextParam.builder()
                .startTime(startTime)
                .endTime(endTime)
                .dbName(dbName)
                .tenantId(tenantId)
                .sqlId(sqlId)
                .build()));
    }

}
