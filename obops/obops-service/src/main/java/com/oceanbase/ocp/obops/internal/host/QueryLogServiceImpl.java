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

package com.oceanbase.ocp.obops.internal.host;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.google.common.base.Functions;

import com.oceanbase.ocp.core.property.PropertyManager;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.executor.executor.AgentExecutor;
import com.oceanbase.ocp.executor.model.compute.AgentLogEntry;
import com.oceanbase.ocp.executor.model.compute.AgentQueryLogResult;
import com.oceanbase.ocp.executor.model.compute.request.AgentDownloadLogRequest;
import com.oceanbase.ocp.executor.model.compute.request.AgentQueryLogRequest;
import com.oceanbase.ocp.executor.model.monitor.HttpConfig;
import com.oceanbase.ocp.obops.host.ObAgentService;
import com.oceanbase.ocp.obops.host.QueryLogService;
import com.oceanbase.ocp.obops.host.model.LogEntry;
import com.oceanbase.ocp.obops.host.model.QueryLogResult;
import com.oceanbase.ocp.obops.host.param.DownloadLogRequest;
import com.oceanbase.ocp.obops.host.param.QueryLogRequest;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class QueryLogServiceImpl implements QueryLogService {

    @Autowired
    private PropertyManager propertyManager;

    @Autowired
    private ObAgentService obAgentService;

    private static final Integer POSITION_SPLIT_COUNT = 2;

    public QueryLogResult queryLog(QueryLogRequest queryLogRequest) throws IOException {
        log.info("QueryLog req={}", queryLogRequest);
        AgentExecutor agentExecutor =
                obAgentService.getAgentExecutor(queryLogRequest.getIp(), queryLogRequest.getPort());

        String[] positionStrings = queryLogRequest.getPosition().split(":");
        String lastQueryFileId = "0";
        Long lastQueryFileOffset = 0L;
        if (positionStrings.length == POSITION_SPLIT_COUNT) {
            lastQueryFileId = positionStrings[0];
            lastQueryFileOffset = Long.parseLong(positionStrings[1]);
        }

        List<String> logLevels = queryLogRequest.getLogLevel().stream()
                .map(Functions.toStringFunction())
                .collect(Collectors.toList());

        AgentQueryLogRequest queryLogReq = AgentQueryLogRequest.builder()
                .startTime(queryLogRequest.getStartTime())
                .endTime(queryLogRequest.getEndTime())
                .logType(queryLogRequest.getLogType())
                .keyword(queryLogRequest.getKeyword())
                .keywordType(queryLogRequest.getKeywordType())
                .excludeKeyword(queryLogRequest.getExcludeKeyword())
                .excludeKeywordType(queryLogRequest.getExcludeKeywordType())
                .logLevel(logLevels)
                .limit(queryLogRequest.getLimit())
                .lastQueryFileId(lastQueryFileId)
                .lastQueryFileOffset(lastQueryFileOffset)
                .build();

        AgentQueryLogResult agentQueryLogResult = agentExecutor.queryLog(queryLogReq);

        List<LogEntry> logEntries = new ArrayList<>();
        for (AgentLogEntry agentLogEntry : agentQueryLogResult.getLogEntries()) {
            LogEntry logEntry = LogEntry.builder()
                    .logAt(agentLogEntry.getLogAt())
                    .logLine(agentLogEntry.getLogLine())
                    .logType(queryLogRequest.getLogType())
                    .logLevel(agentLogEntry.getLogLevel())
                    .build();
            logEntries.add(logEntry);
        }

        return QueryLogResult.builder()
                .logEntries(logEntries)
                .ip(queryLogRequest.getIp())
                .port(queryLogRequest.getPort())
                .position(String.format("%s:%d", agentQueryLogResult.getFileId(), agentQueryLogResult.getFileOffset()))
                .build();
    }

    public Resource downloadLog(DownloadLogRequest downloadLogReq) throws IOException {
        log.info("Invoke download log, req={}", downloadLogReq);
        ExceptionUtils.illegalArgs(isValidTimeSpan(downloadLogReq.getStartTime(), downloadLogReq.getEndTime(),
                downloadLogReq.getLogType()));
        List<String> logLevels = downloadLogReq.getLogLevel().stream()
                .map(Functions.toStringFunction())
                .collect(Collectors.toList());

        AgentDownloadLogRequest agentDownloadLogReq = AgentDownloadLogRequest.builder()
                .logType(downloadLogReq.getLogType())
                .startTime(downloadLogReq.getStartTime())
                .endTime(downloadLogReq.getEndTime())
                .keyword(downloadLogReq.getKeyword())
                .keywordType(downloadLogReq.getKeywordType())
                .excludeKeyword(downloadLogReq.getExcludeKeyword())
                .excludeKeywordType(downloadLogReq.getExcludeKeywordType())
                .logLevel(logLevels)
                .build();

        HttpConfig config = HttpConfig.builder()
                .connectTimeout(propertyManager.getOcpLogDownloadHttpConnectTimeout())
                .readTimeout(propertyManager.getOcpLogDownloadHttpReadTimeout())
                .build();

        AgentExecutor agentExecutor = obAgentService.getAgentExecutor(downloadLogReq.getIp(), downloadLogReq.getPort());
        Response response = agentExecutor.downloadLog(agentDownloadLogReq, config);
        InputStream inputStream = response.readEntity(InputStream.class);
        return new InputStreamResource(inputStream);
    }

    private boolean isValidTimeSpan(OffsetDateTime startTime, OffsetDateTime endTime, List<String> logTypes) {
        if (logTypes.contains("observer") || logTypes.contains("election") || logTypes.contains("rootservice")) {
            return ChronoUnit.HOURS.between(startTime, endTime) <= 1L;
        }
        return ChronoUnit.HOURS.between(startTime, endTime) <= 24L;
    }
}


