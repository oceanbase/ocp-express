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

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oceanbase.ocp.core.response.IterableResponse;
import com.oceanbase.ocp.core.response.NoDataResponse;
import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.core.response.SuccessResponse;
import com.oceanbase.ocp.obops.database.DatabaseService;
import com.oceanbase.ocp.obops.database.model.Database;
import com.oceanbase.ocp.obops.database.param.CreateDatabaseParam;
import com.oceanbase.ocp.obops.database.param.ModifyDatabaseParam;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@Validated
@RequestMapping("/api/v1")
public class ObDatabaseController {

    @Resource
    private DatabaseService databaseService;

    @GetMapping(value = "/ob/tenants/{tenantId:[\\d]+}/databases", produces = {"application/json"})
    public IterableResponse<Database> listDatabases(@PathVariable("tenantId") Long tenantId) {
        return ResponseBuilder.iterable(databaseService.listDatabases(tenantId));
    }

    @PostMapping(value = "/ob/tenants/{tenantId:[\\d]+}/databases", produces = {"application/json"},
            consumes = {"application/json"})
    public SuccessResponse<Database> createDatabase(@PathVariable("tenantId") Long tenantId,
            @Valid @RequestBody CreateDatabaseParam param) {
        return ResponseBuilder.single(databaseService.createDatabase(tenantId, param));
    }

    @PutMapping(value = "/ob/tenants/{tenantId:[\\d]+}/databases/{dbName}", produces = {"application/json"},
            consumes = {"application/json"})
    public SuccessResponse<Database> modifyDatabase(@PathVariable("tenantId") Long tenantId,
            @PathVariable("dbName") String dbName, @Valid @RequestBody ModifyDatabaseParam param) {
        return ResponseBuilder.single(databaseService.modifyDatabase(tenantId, dbName, param));
    }

    @DeleteMapping(value = "/ob/tenants/{tenantId:[\\d]+}/databases/{dbName}", produces = {"application/json"})
    public NoDataResponse deleteDatabase(@PathVariable("tenantId") Long tenantId,
            @PathVariable("dbName") String dbName) {
        databaseService.deleteDatabase(tenantId, dbName);
        return ResponseBuilder.noData();
    }
}
