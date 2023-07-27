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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oceanbase.ocp.core.property.PropertyService;
import com.oceanbase.ocp.core.property.SystemInfo;
import com.oceanbase.ocp.core.property.model.PropertyMeta;
import com.oceanbase.ocp.core.response.NoDataResponse;
import com.oceanbase.ocp.core.response.PaginatedResponse;
import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.core.response.SuccessResponse;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;
    @Autowired
    private SystemInfo systemInfo;

    @GetMapping("/config/properties")
    public PaginatedResponse<PropertyMeta> findNonFatalProperties(
            @RequestParam(name = "keyLike", required = false) String keyLike,
            @PageableDefault(size = Integer.MAX_VALUE, sort = {"key"},
                    direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseBuilder.paginated(propertyService.findAllVisibleProperties(keyLike, pageable));
    }

    @PutMapping("/config/properties/{id:[\\d]+}")
    public SuccessResponse<PropertyMeta> updateProperty(@PathVariable long id, @RequestParam String newValue) {
        return ResponseBuilder.single(propertyService.updateProperty(id, newValue));
    }

    @PostMapping("/config/refresh")
    public NoDataResponse refreshProperties() {
        propertyService.refresh();
        return ResponseBuilder.noData();
    }

    @GetMapping("/config/systemInfo")
    public SuccessResponse<SystemInfo> getSystemInfo() {
        return ResponseBuilder.single(systemInfo);
    }

}
