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

package com.oceanbase.ocp.obsdk.operator.cluster.model;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObRestoreTask {

    private Long jobId;

    private Long externalJobId;

    private Long backupClusterId;

    private String backupClusterName;

    private Long backupTenantId;

    private String backupTenantName;

    private Long restoreTenantId;

    private String restoreTenantName;

    private String restorePoolList;

    private String status;

    private Timestamp startTime;

    private Timestamp completionTime;

    private Timestamp restoreStartTimestamp;

    private Timestamp restoreFinishTimestamp;

    private Timestamp restoreCurrentTimestamp;

    private String backupDest;

    private String restoreOption;

    private String errorMsg;
}
