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

import com.oceanbase.ocp.obsdk.enums.RootServiceJobStatus;
import com.oceanbase.ocp.obsdk.enums.RootServiceJobType;

import lombok.Data;

@Data
public class TenantJobProgress {

    private Long tenantId;

    private Long jobId;

    private String jobType;

    private String jobStatus;

    private Long progress;


    public RootServiceJobStatus getStatus() {
        return RootServiceJobStatus.fromValue(jobStatus);
    }

    public RootServiceJobType getType() {
        return RootServiceJobType.fromValue(jobType);
    }

    public Boolean finished() {
        return RootServiceJobStatus.SUCCESS.toString().equals(jobStatus)
                || RootServiceJobStatus.FAILED.toString().equals(jobStatus);
    }
}
