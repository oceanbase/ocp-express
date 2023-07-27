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

package com.oceanbase.ocp.obops.database.model;

import java.time.OffsetDateTime;
import java.util.List;

import com.oceanbase.ocp.obsdk.accessor.user.model.ObjectPrivilege;
import com.oceanbase.ocp.obsdk.enums.GlobalPrivilege;

import lombok.Data;

@Data
public class DbRole {

    private String name;

    private OffsetDateTime createTime;

    private OffsetDateTime updateTime;

    private List<GlobalPrivilege> globalPrivileges;

    private List<ObjectPrivilege> objectPrivileges;

    /**
     * What roles are granted to this role.
     */
    private List<String> grantedRoles;

    /**
     * What users are this role granted to.
     */
    private List<String> userGrantees;

    /**
     * What roles are this role granted to.
     */
    private List<String> roleGrantees;
}
