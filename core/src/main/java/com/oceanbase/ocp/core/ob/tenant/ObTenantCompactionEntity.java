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

package com.oceanbase.ocp.core.ob.tenant;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.oceanbase.ocp.core.ob.constant.TenantCompactionResult;

import lombok.Data;

@Data
@Entity
@Table(name = "ob_tenant_compaction")
public class ObTenantCompactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "create_time", columnDefinition = "DATETIME", insertable = false, updatable = false)
    private OffsetDateTime createTime;

    @Column(name = "update_time", columnDefinition = "DATETIME", insertable = false, updatable = false)
    private OffsetDateTime updateTime;

    @Column(name = "ob_tenant_id", nullable = false)
    private Long obTenantId;

    @Column(name = "status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TenantCompactionResult status;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "last_finish_time", nullable = false)
    private OffsetDateTime lastFinishTime;

    @Column(name = "frozen_scn", nullable = false)
    private Long frozenScn;
}
