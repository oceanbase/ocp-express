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
package com.oceanbase.ocp.security.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.core.response.SuccessResponse;
import com.oceanbase.ocp.core.security.model.LoginKey;
import com.oceanbase.ocp.security.iam.LoginKeyService;

@RestController
@RequestMapping("/api/v1")
public class IamController {

    @Autowired
    private LoginKeyService loginKeyService;

    @GetMapping(value = "/loginKey", produces = MediaType.APPLICATION_JSON_VALUE)
    public SuccessResponse<LoginKey> getLoginKey() {
        return ResponseBuilder.single(loginKeyService.getLoginPublicKey());
    }

}
