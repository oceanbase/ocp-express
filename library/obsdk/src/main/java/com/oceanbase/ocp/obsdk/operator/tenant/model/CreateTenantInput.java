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

package com.oceanbase.ocp.obsdk.operator.tenant.model;

import java.util.ArrayList;
import java.util.List;

import com.oceanbase.ocp.obsdk.accessor.variable.model.SetVariableInput;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateTenantInput {

    private String name;

    private List<String> resourcePoolList;

    private String locality;

    private String primaryZone;

    private String mode;

    private String charset;

    private String collation;

    @Builder.Default
    private List<SetVariableInput> variables = new ArrayList<>();
}
