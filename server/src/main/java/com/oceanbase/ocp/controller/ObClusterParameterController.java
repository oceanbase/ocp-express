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

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oceanbase.ocp.core.response.IterableResponse;
import com.oceanbase.ocp.core.response.NoDataResponse;
import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.obops.parameter.ClusterParameterService;
import com.oceanbase.ocp.obops.parameter.model.ClusterParameter;
import com.oceanbase.ocp.obops.parameter.param.UpdateClusterParameterParam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class ObClusterParameterController {

    @Autowired
    private ClusterParameterService clusterParameterService;

    @GetMapping(value = "/ob/cluster/parameters")
    public IterableResponse<ClusterParameter> listClusterParameters() {
        return ResponseBuilder.iterable(clusterParameterService.listParameters());
    }

    @PatchMapping(value = "/ob/cluster/parameters")
    public NoDataResponse updateClusterParameter(@Valid @RequestBody List<UpdateClusterParameterParam> params) {
        clusterParameterService.updateParameters(params);
        return ResponseBuilder.noData();
    }
}
