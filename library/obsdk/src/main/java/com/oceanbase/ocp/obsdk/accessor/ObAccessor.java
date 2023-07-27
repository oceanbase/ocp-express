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

import lombok.Builder;

/**
 * ObAccessor is designed for operations in user tenants.
 */
@Builder
public class ObAccessor {

    private InfoAccessor infoAccessor;

    private VariableAccessor variableAccessor;

    private ParameterAccessor parameterAccessor;

    private UserAccessor userAccessor;

    private DatabaseAccessor databaseAccessor;

    private ObjectAccessor objectAccessor;

    private SessionAccessor sessionAccessor;

    private SqlTuningAccessor sqlTuningAccessor;


    public InfoAccessor info() {
        return infoAccessor;
    }

    public VariableAccessor variable() {
        return variableAccessor;
    }

    public ParameterAccessor parameter() {
        return parameterAccessor;
    }

    public UserAccessor user() {
        return userAccessor;
    }

    public DatabaseAccessor database() {
        return databaseAccessor;
    }

    public ObjectAccessor object() {
        return objectAccessor;
    }

    public SessionAccessor session() {
        return sessionAccessor;
    }

    public SqlTuningAccessor sqlTuning() {
        return sqlTuningAccessor;
    }
}
