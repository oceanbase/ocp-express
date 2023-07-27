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

package com.oceanbase.ocp.obsdk.operator;

import java.util.List;

import com.oceanbase.ocp.obsdk.operator.resource.model.CreateResourcePoolInput;
import com.oceanbase.ocp.obsdk.operator.resource.model.CreateUnitConfigInput;
import com.oceanbase.ocp.obsdk.operator.resource.model.ListUnitInput;
import com.oceanbase.ocp.obsdk.operator.resource.model.ObResourcePool;
import com.oceanbase.ocp.obsdk.operator.resource.model.ObUnit;
import com.oceanbase.ocp.obsdk.operator.resource.model.ObUnitConfig;

public interface ResourceOperator {

    ObUnitConfig createUnitConfig(CreateUnitConfigInput input);

    void deleteUnitConfig(String unitConfigName);

    ObResourcePool createResourcePool(CreateResourcePoolInput input);

    List<ObResourcePool> listResourcePool(Long tenantId);

    void modifyResourcePoolUnitConfig(String poolName, String unitConfigName);

    void splitResourcePool(String poolName, List<String> poolList, List<String> zoneList);

    void deleteResourcePool(String poolName);

    void modifyTenantUnitCount(String tenantName, Long unitCount);


    default List<ObUnit> listAllUnits() {
        return listUnit(ListUnitInput.builder().build());
    }

    List<ObUnit> listUnit(ListUnitInput input);

    List<ObUnit> listUnusedUnit();

}
