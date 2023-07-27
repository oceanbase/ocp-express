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

package com.oceanbase.ocp.perf.sql.model;

import java.time.OffsetDateTime;

import com.oceanbase.ocp.perf.sql.enums.OutlineType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Outline {

    private Long outlineId;

    private String tenantName;

    private Long obTenantId;

    private Long obDbId;

    private String dbName;

    private String outlineName;

    private OutlineType type;

    private String sqlId;

    private String outlineContent;

    private String planUid;

    private String visibleSignature;

    private String sqlText;

    private Long concurrentNum;

    private String limitTarget;

    private OutlineStatus status;

    private OffsetDateTime createTime;

    private OffsetDateTime deleteTime;

}
