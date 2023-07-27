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

package com.oceanbase.ocp.obops.tenant;

import java.util.List;

import com.oceanbase.ocp.obops.tenant.param.AddReplicaParam;
import com.oceanbase.ocp.obops.tenant.param.DeleteReplicaParam;
import com.oceanbase.ocp.obops.tenant.param.ModifyReplicaParam;
import com.oceanbase.ocp.task.model.TaskInstance;

public interface TenantReplicaService {

    /**
     * Batch add tenant replica
     *
     * @param tenantId tenant ID
     * @param paramList list of add replica param
     * @return {@link TaskInstance}
     */
    TaskInstance addReplica(Long tenantId, List<AddReplicaParam> paramList);

    /**
     * Batch delete tenant replica
     *
     * @param tenantId tenant ID
     * @param paramList list of delete replica param
     * @return {@link TaskInstance}
     */
    TaskInstance deleteReplica(Long tenantId, List<DeleteReplicaParam> paramList);

    /**
     * Batch modify tenant replica
     *
     * @param tenantId tenant ID
     * @param paramList list of modify replica param
     * @return {@link TaskInstance}
     */
    TaskInstance modifyReplica(Long tenantId, List<ModifyReplicaParam> paramList);
}
