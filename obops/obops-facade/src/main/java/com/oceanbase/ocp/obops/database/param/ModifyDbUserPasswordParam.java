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

package com.oceanbase.ocp.obops.database.param;

import javax.validation.constraints.NotEmpty;

import com.oceanbase.ocp.common.util.json.MaskField;
import com.oceanbase.ocp.security.annotation.SensitiveAttribute;
import com.oceanbase.ocp.security.annotation.SensitiveType;

import lombok.Data;

@Data
@SensitiveType
public class ModifyDbUserPasswordParam {

    @NotEmpty(message = "{error.ob.user.password.empty}")
    @MaskField
    @SensitiveAttribute
    private String newPassword;
}
