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

package com.oceanbase.ocp.obops.host.param;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.oceanbase.ocp.obops.host.model.LogLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryLogRequest {

    private String ip;

    private Integer port;

    private String logType;

    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

    private List<String> keyword = new ArrayList<>();

    private String keywordType;

    private List<String> excludeKeyword = new ArrayList<>();

    private String excludeKeywordType;

    private List<LogLevel> logLevel = new ArrayList<>();

    private String position = "";

    private Integer limit;
}
