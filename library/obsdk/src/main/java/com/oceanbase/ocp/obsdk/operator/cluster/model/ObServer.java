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

import javax.persistence.Column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <pre>
 * obsdk server object
 * - idc/region info maintained in __all_zone
 * </pre>
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ObServer {

    @Column(name = "svr_ip")
    private String svrIp;

    @Column(name = "svr_port")
    private Integer svrPort;

    /**
     * sql_port in OCP
     */
    @Column(name = "inner_port")
    private Integer innerPort;

    @Column(name = "with_rootserver")
    private Boolean withRootserver;

    @Column(name = "status")
    private String status;

    @Column(name = "zone")
    private String zone;

    @Column(name = "build_version")
    private String buildVersion;

    @Column(name = "stop_time")
    private Long stopTime;

    @Column(name = "start_service_time")
    private Long startServiceTime;

    @Column(name = "block_migrate_in_time")
    private Long blockMigrateInTime;

    @Column(name = "id")
    private Long id;

    public String getAddress() {
        if (svrIp == null || svrPort == null) {
            return null;
        }
        return svrIp + ":" + svrPort;
    }

    public ObServerKey getKey() {
        return new ObServerKey(svrIp, svrPort);
    }
}
