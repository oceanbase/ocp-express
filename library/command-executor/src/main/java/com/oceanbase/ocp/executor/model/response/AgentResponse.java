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

package com.oceanbase.ocp.executor.model.response;

import java.time.OffsetDateTime;

import com.oceanbase.ocp.executor.model.response.error.ApiError;

import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgentResponse<T> {

    private Boolean successful;

    /**
     * Request timestamp.
     */
    private OffsetDateTime timestamp;

    /**
     * Response duration.
     */
    private Long duration;

    /**
     * Response status.
     */
    private Response.Status status;

    /**
     * Http request trace id.
     */
    private String traceId;

    /**
     * Server ip address.
     */
    private String server;

    /**
     * Response object.
     */
    private T data;

    /**
     * Api error info.
     */
    private ApiError error;
}
