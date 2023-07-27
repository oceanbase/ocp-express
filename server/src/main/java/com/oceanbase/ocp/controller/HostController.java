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
package com.oceanbase.ocp.controller;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oceanbase.ocp.core.exception.UnexpectedException;
import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.core.response.SuccessResponse;
import com.oceanbase.ocp.obops.host.HostService;
import com.oceanbase.ocp.obops.host.ObAgentService;
import com.oceanbase.ocp.obops.host.QueryLogService;
import com.oceanbase.ocp.obops.host.model.HostInfo;
import com.oceanbase.ocp.obops.host.model.ObAgentDetail;
import com.oceanbase.ocp.obops.host.model.QueryLogResult;
import com.oceanbase.ocp.obops.host.param.DownloadLogRequest;
import com.oceanbase.ocp.obops.host.param.QueryLogRequest;
import com.oceanbase.ocp.task.model.TaskInstance;

import jakarta.ws.rs.ProcessingException;
import lombok.extern.slf4j.Slf4j;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class HostController {

    @Autowired
    private ObAgentService agentService;
    @Autowired
    private HostService hostService;
    @Autowired
    private QueryLogService queryLogService;


    @GetMapping(value = "/obagent", produces = {"application/json"})
    public SuccessResponse<ObAgentDetail> getAgentDetail(@RequestParam("ip") String ip,
            @RequestParam("obSvrPort") Integer obSvrPort) {
        return ResponseBuilder.single(agentService.findDetailByIpAndObSvrPort(ip, obSvrPort).orElse(null));
    }

    @GetMapping(value = "/hosts", produces = {"application/json"})
    public SuccessResponse<HostInfo> getHostInfo(@RequestParam("ip") String ip,
            @RequestParam("obSvrPort") Integer obSvrPort) {
        return ResponseBuilder.single(hostService.getHostInfo(ip, obSvrPort));
    }

    @PostMapping("/obagent/restart")
    public SuccessResponse<TaskInstance> restartHostAgent(@RequestParam("ip") String ip,
            @RequestParam("obSvrPort") Integer obSvrPort) {
        return ResponseBuilder.single(agentService.restartObAgent(ip, obSvrPort));
    }

    @PostMapping("/hosts/logs/query")
    public SuccessResponse<QueryLogResult> queryLog(@RequestBody QueryLogRequest queryLogRequest)
            throws Exception {
        SuccessResponse<QueryLogResult> queryLogResult;
        try {
            queryLogResult = ResponseBuilder.single(queryLogService.queryLog(queryLogRequest));
        } catch (ProcessingException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("call queryLog api got SocketTimeoutException", e);
                throw new UnexpectedException(e, ErrorCodes.COMPUTE_HOST_AGENT_QUERY_LOG_TIMEOUT);
            }
            throw e;
        } catch (Throwable e) {
            log.error("call queryLog api got unknown exception", e);
            throw e;
        }
        return queryLogResult;
    }

    @PostMapping("/hosts/logs/download")
    public ResponseEntity<Resource> downloadLog(@RequestBody DownloadLogRequest downloadLogReq)
            throws IOException {
        Resource resource = queryLogService.downloadLog(downloadLogReq);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
