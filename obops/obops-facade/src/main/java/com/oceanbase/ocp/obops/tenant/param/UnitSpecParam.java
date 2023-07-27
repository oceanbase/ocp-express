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

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class UnitSpecParam {

    @NotNull(message = "{error.ob.tenant.unit.spec.max.cpu.empty}")
    @DecimalMin(value = "0.5", message = "{error.ob.tenant.unit.spec.cpu.invalid}")
    @DecimalMax(value = "999", message = "{error.ob.tenant.unit.spec.cpu.invalid}")
    private Double cpuCore;

    @NotNull(message = "{error.ob.tenant.unit.spec.max.memory.empty}")
    @Min(value = 1, message = "{error.ob.tenant.unit.spec.memory.invalid}")
    @Max(value = 10000, message = "{error.ob.tenant.unit.spec.memory.invalid}")
    private Long memorySize;

    // TODO IOPS & LOG_DISK_SIZE config

    public Long getMemoryBytes() {
        return memorySize * 1024 * 1024 * 1024;
    }
}
