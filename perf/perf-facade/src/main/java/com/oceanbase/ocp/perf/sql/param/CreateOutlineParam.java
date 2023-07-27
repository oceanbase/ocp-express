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

import javax.validation.constraints.NotNull;

import com.oceanbase.ocp.perf.sql.enums.OutlineType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOutlineParam {

    @NotNull(message = "{error.common.field.value.not.null}")
    public OutlineType type;

    @NotNull(message = "{error.common.field.value.not.null}")
    public String dbName;

    public String tableName;

    public String indexName;

    @NotNull(message = "{error.common.field.value.not.null}")
    public OffsetDateTime startTime;

    @NotNull(message = "{error.common.field.value.not.null}")
    public OffsetDateTime endTime;

    /**
     * Sql text for rate limit
     */
    public String sqlText;

    public Long concurrentNum;

}
