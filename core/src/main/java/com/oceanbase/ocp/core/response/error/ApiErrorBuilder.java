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

package com.oceanbase.ocp.core.response.error;

import java.util.Collections;
import java.util.List;

import com.oceanbase.ocp.obsdk.exception.OceanBaseException;

public class ApiErrorBuilder {

    public static List<ApiSubError> buildOceanBaseSubErrors(OceanBaseException oceanBaseException) {
        return Collections.singletonList(buildOceanBaseError(oceanBaseException));
    }

    public static ApiOceanBaseError buildOceanBaseError(OceanBaseException ex) {
        return new ApiOceanBaseError(ex.getSQLState(), ex.getErrorCode(), ex.getMessage());
    }
}
