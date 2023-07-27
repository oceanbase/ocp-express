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

package com.oceanbase.ocp.executor.model.response.error;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import lombok.Data;

@Data
public class ApiError {

    /**
     * Max error message length.
     */
    private static final int TRUNCATE_LENGTH = 5000;

    private static final String TRUNCATE_NOTE =
            "Error message content exceeds " + TRUNCATE_LENGTH + " characters and has been truncated!";

    /**
     * Error code.
     */
    private int code;

    /**
     * Error message.
     */
    private String message;

    /**
     * Sub errors.
     */
    private List<Object> subErrors;

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        ToStringStyle defaultStyle = ToStringBuilder.getDefaultStyle();
        defaultStyle.append(buffer, "code", code);
        defaultStyle.append(buffer, "message", truncate(message), true);
        defaultStyle.append(buffer, "subErrors", subErrors, true);
        return buffer.toString();
    }

    public String getMessage() {
        return truncate(message);
    }

    private String truncate(String content) {
        if (StringUtils.length(content) <= TRUNCATE_LENGTH) {
            return content;
        }
        return content.substring(0, ApiError.TRUNCATE_LENGTH) + "\n" + TRUNCATE_NOTE;
    }
}
