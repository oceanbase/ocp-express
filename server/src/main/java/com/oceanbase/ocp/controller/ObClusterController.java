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

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oceanbase.ocp.core.ob.cluster.BasicCluster;
import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.core.response.IterableResponse;
import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.core.response.SuccessResponse;
import com.oceanbase.ocp.obops.cluster.ClusterCharsetService;
import com.oceanbase.ocp.obops.cluster.ClusterInitService;
import com.oceanbase.ocp.obops.cluster.ClusterService;
import com.oceanbase.ocp.obops.cluster.ClusterUnitService;
import com.oceanbase.ocp.obops.cluster.model.Charset;
import com.oceanbase.ocp.obops.cluster.model.ClusterInfo;
import com.oceanbase.ocp.obops.cluster.model.ClusterUnitSpecLimit;
import com.oceanbase.ocp.obops.cluster.model.Server;
import com.oceanbase.ocp.obops.cluster.param.ClusterInitParam;

import lombok.extern.slf4j.Slf4j;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class ObClusterController {

    @Autowired
    private ClusterInitService clusterInitService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private ClusterCharsetService clusterCharsetService;

    @Autowired
    private ClusterUnitService clusterUnitService;

    @PostMapping(value = "/ob/cluster/init", produces = {"application/json"}, consumes = {"application/json"})
    public SuccessResponse<BasicCluster> initObCluster(@Valid @RequestBody ClusterInitParam param) {
        return ResponseBuilder.single(clusterInitService.initCluster(param));
    }

    @GetMapping(value = "/ob/cluster", produces = {"application/json"})
    public SuccessResponse<ClusterInfo> getClusterInfo() {
        return ResponseBuilder.single(clusterService.getClusterInfo());
    }

    @GetMapping(value = "/ob/cluster/server", produces = {"application/json"})
    public SuccessResponse<Server> getServerInfo(@RequestParam("ip") String ip,
            @RequestParam("obSvrPort") Integer obSvrPort) {
        return ResponseBuilder.single(clusterService.getServerInfo(ip, obSvrPort));
    }

    @GetMapping(value = "/ob/cluster/charsets", produces = {"application/json"})
    public IterableResponse<Charset> listCharsets(
            @Valid @RequestParam(value = "tenantMode", required = false) TenantMode tenantMode) {
        return ResponseBuilder.iterable(clusterCharsetService.listCharsets(tenantMode));
    }

    @GetMapping(value = "/ob/cluster/unitSpecLimit", produces = {"application/json"})
    public SuccessResponse<ClusterUnitSpecLimit> getClusterUnitSpecLimit() {
        return ResponseBuilder.single(clusterUnitService.getClusterUnitSpecLimit());
    }

}
