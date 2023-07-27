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

package com.oceanbase.ocp.obops.tenant.param;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.oceanbase.ocp.common.util.json.MaskField;
import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.obops.parameter.param.TenantParameterParam;
import com.oceanbase.ocp.obops.tenant.model.ReplicaType;
import com.oceanbase.ocp.security.annotation.SensitiveAttribute;
import com.oceanbase.ocp.security.annotation.SensitiveType;

import lombok.Data;

@Data
@SensitiveType
public class CreateTenantParam {

    @NotEmpty(message = "{error.ob.tenant.name.empty}")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z_0-9]{1,63}$", message = "{error.ob.tenant.name.invalid}")
    private String name;

    private TenantMode mode = TenantMode.MYSQL;

    @Size(max = 128, message = "{error.ob.tenant.primary.zone.invalid}")
    private String primaryZone;

    private String charset;

    private String collation;

    @Size(max = 1024, message = "{error.ob.tenant.description.invalid}")
    private String description;

    @Size(max = 65535, message = "{error.ob.tenant.whitelist.invalid}")
    private String whitelist;

    private String timeZone;

    @NotEmpty(message = "{error.ob.tenant.password.empty}")
    @SensitiveAttribute
    @MaskField
    private String rootPassword;

    @NotEmpty(message = "{error.ob.tenant.zone.empty}")
    @Valid
    private List<ZoneParam> zones;

    @Data
    public static class ZoneParam {

        @NotNull(message = "{error.ob.tenant.zone.name.empty}")
        private String name;

        @NotNull(message = "{error.ob.tenant.replica.type.empty}")
        private ReplicaType replicaType;

        @NotNull(message = "{error.ob.tenant.resource.pool.empty}")
        @Valid
        private PoolParam resourcePool;
    }

    @Data
    public static class PoolParam {

        // TODO error code
        @NotNull(message = "{error.ob.tenant.unit.spec.name.empty}")
        @Valid
        private UnitSpecParam unitSpec;

        @NotNull(message = "{error.ob.tenant.unit.count.empty}")
        private Long unitCount;
    }

    @Valid
    private List<TenantParameterParam> parameters = new ArrayList<>();
}
