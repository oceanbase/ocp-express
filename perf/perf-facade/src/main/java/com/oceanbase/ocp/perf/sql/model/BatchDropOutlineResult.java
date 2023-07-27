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

import java.util.Map;

import com.oceanbase.ocp.perf.sql.param.BatchDropOutlineRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BatchDropOutlineResult {

    public boolean result;

    public Map<BatchDropOutlineRequest.Outline, String> failedSql;


    public static BatchDropOutlineResult of(Map<BatchDropOutlineRequest.Outline, String> map) {
        if (map.isEmpty()) {
            return BatchDropOutlineResult.builder()
                    .result(true)
                    .build();
        } else {
            return BatchDropOutlineResult.builder()
                    .result(false)
                    .failedSql(map)
                    .build();
        }
    }
}
