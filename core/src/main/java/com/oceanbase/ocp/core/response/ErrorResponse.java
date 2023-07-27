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
package com.oceanbase.ocp.core.response;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;

import com.oceanbase.ocp.common.util.HostUtils;
import com.oceanbase.ocp.common.util.trace.TraceUtils;
import com.oceanbase.ocp.core.response.error.ApiError;

import lombok.Getter;
import lombok.Setter;

/**
 * A wrapper class for REST api error results, including error information, and
 * trace info.
 */
@Getter
@Setter
public class ErrorResponse extends BaseResponse {

    private final ApiError error;

    private static final String SERVER = HostUtils.getLocalIp();

    public ErrorResponse(HttpStatus status, ApiError error) {
        setSuccessful(false);
        setStatus(status.value());
        setTimestamp(OffsetDateTime.now());
        setTraceId(TraceUtils.getTraceId());
        setDuration(TraceUtils.getDuration());
        setServer(SERVER);
        this.error = error;
    }

    private ErrorResponse(HttpStatus status, ApiError error, String traceId, long duration) {
        this(status, error);
        setTraceId(traceId);
        setDuration(duration);
    }

    public static ErrorResponse error(HttpStatus status, ApiError error) {
        return new ErrorResponse(status, error);
    }

    public static ErrorResponse error(HttpStatus status, ApiError error, String traceId, long duration) {
        return new ErrorResponse(status, error, traceId, duration);
    }

}
