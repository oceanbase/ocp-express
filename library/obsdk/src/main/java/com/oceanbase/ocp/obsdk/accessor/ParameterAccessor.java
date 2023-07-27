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

package com.oceanbase.ocp.obsdk.accessor;

import java.util.List;

import com.oceanbase.ocp.obsdk.operator.cluster.model.ObParameter;
import com.oceanbase.ocp.obsdk.operator.parameter.model.SetObParameter;

/**
 * Tenant parameter management in user tenants.
 * <p>
 * For cluster parameter management, refer to
 * {@link com.oceanbase.ocp.obsdk.operator.ParameterOperator}.
 */
public interface ParameterAccessor {

    /**
     * List tenant parameters.
     *
     * @return list of {@link ObParameter}
     */
    List<ObParameter> listParameters();

    /**
     * Set a tenant parameter.
     */
    void setParameter(SetObParameter input);
}
