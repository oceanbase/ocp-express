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

package com.oceanbase.ocp.obsdk.accessor.user.model;

import java.sql.Timestamp;
import java.util.List;

import com.oceanbase.ocp.obsdk.enums.GlobalPrivilege;

import lombok.Data;

@Data
public class ObUser {

    private Timestamp gmtCreate;

    private String userName;

    private Long isLocked;

    private List<GlobalPrivilege> globalPrivileges;

    private List<DbPrivilege> dbPrivileges;

    private List<String> grantedRoles;

    private List<ObjectPrivilege> objectPrivileges;

    private String password;

    public Boolean isLocked() {
        if (isLocked == null) {
            return null;
        }
        return isLocked == 0 ? Boolean.FALSE : Boolean.TRUE;
    }

}
