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

package com.oceanbase.ocp.obsdk.operator.resource.model;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import lombok.Data;

@Data
public class ObResourcePool {

    private Long resourcePoolId;

    private String name;

    private Long unitCount;

    private Long unitConfigId;

    private String zoneListStr;

    private Long tenantId;

    private ObReplicaType replicaType;

    private ObUnitConfig obUnitConfig;

    private List<String> zoneList;

    private Long updateTime;

    public List<String> getZoneList() {
        if (CollectionUtils.isNotEmpty(zoneList)) {
            return zoneList;
        }

        zoneList = Arrays.asList(zoneListStr.split(";"));
        return zoneList;
    }
}
