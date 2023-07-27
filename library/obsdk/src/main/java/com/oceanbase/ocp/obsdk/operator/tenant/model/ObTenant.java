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

import lombok.Data;

@Data
public class ObTenant {

    private Long tenantId;

    private String tenantName;


    private TenantType tenantType;

    @Deprecated
    private String zoneList;

    private String primaryZone;

    private Boolean locked;

    private Long collationType;

    private Boolean readOnly;

    private String locality;

    private String compatibilityMode;

    private String status;

    private long existSeconds;

    public String getTenantMode() {
        return compatibilityMode;
    }

    public Boolean isLocked() {
        return locked;
    }

    public Boolean isReadonly() {
        return readOnly;
    }
}
