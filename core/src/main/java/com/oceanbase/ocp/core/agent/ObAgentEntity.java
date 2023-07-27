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
package com.oceanbase.ocp.core.agent;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.oceanbase.ocp.core.constants.ObAgentOperation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ob_agent")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ObAgentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, nullable = false)
    private Long id;

    @Column(name = "create_time", insertable = false, updatable = false)
    private OffsetDateTime createTime;

    @Column(name = "update_time", insertable = false, updatable = false)
    private OffsetDateTime updateTime;

    @Column(name = "ip")
    private String ip;

    @Column(name = "mgr_port")
    private Integer mgrPort;

    @Column(name = "mon_port")
    private Integer monPort;

    @Column(name = "last_available_time")
    private OffsetDateTime lastAvailableTime;

    @Column(name = "ob_server_svr_port")
    private Integer obServerSvrPort;

    @Column(name = "operation")
    @Enumerated(value = EnumType.STRING)
    private ObAgentOperation operation;

}
