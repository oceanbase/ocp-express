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

package com.oceanbase.ocp.perf.sql.param;

import java.time.OffsetDateTime;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class BatchConcurrentLimitRequest {

    /**
     *
     */
    @NotNull(message = "{error.common.field.value.not.null}")
    public OffsetDateTime startTime;

    @NotNull(message = "{error.common.field.value.not.null}")
    public OffsetDateTime endTime;

    public List<Sql> sqlList;

    public Long concurrentNum;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Sql {

        public String dbName;

        public String sqlId;
    }

}
