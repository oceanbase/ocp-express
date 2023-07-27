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

package com.oceanbase.ocp.perf.sql;

import java.time.OffsetDateTime;
import java.util.List;

import com.oceanbase.ocp.perf.sql.model.BatchConcurrentLimitResult;
import com.oceanbase.ocp.perf.sql.model.BatchDropOutlineResult;
import com.oceanbase.ocp.perf.sql.model.Outline;
import com.oceanbase.ocp.perf.sql.param.BatchConcurrentLimitRequest;
import com.oceanbase.ocp.perf.sql.param.BatchDropOutlineRequest;

public interface OutlineService {

    List<Outline> getOutline(Long tenantId, String dbName, String sqlId, OffsetDateTime startTime,
            OffsetDateTime endTime, Boolean attachPerfData);

    BatchConcurrentLimitResult batchConcurrentLimit(Long tenantId, BatchConcurrentLimitRequest param);

    BatchDropOutlineResult batchDropOutline(Long tenantId, BatchDropOutlineRequest request);

}
