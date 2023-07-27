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

package com.oceanbase.ocp.core.i18n;

/**
 * Enum for list all errors and exceptions while running OCP instances. This may
 * include: validation, runtime exceptions, and unexpected server errors. Each
 * error code is comprised of one integer value and one message key (of String
 * type). The integer code is used for displaying with clients or end users, and
 * can associated with docs. While the message key is used for internal, to
 * handle internationalization.
 */
public interface ErrorCode {

    ErrorCode SUCCESS = new ErrorCode() {

        @Override
        public int getCode() {
            return 0;
        }

        @Override
        public String getKey() {
            return "error.common.success";
        }
    };

    /**
     * Error code for docs
     *
     * @return errorCode
     */
    int getCode();

    /**
     * message key for internationalization, refer to src/main/resources/i18n
     *
     * @return errorCodeMessageKey
     */
    String getKey();
}
