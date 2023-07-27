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

package com.oceanbase.ocp.obsdk.accessor.sql.tuning;

import org.apache.commons.lang3.StringUtils;

import com.oceanbase.ocp.common.lang.Pair;
import com.oceanbase.ocp.obsdk.accessor.SqlTuningAccessor;
import com.oceanbase.ocp.obsdk.connector.ObConnectTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSqlTuningAccessor implements SqlTuningAccessor {

    protected ObConnectTemplate connectTemplate;

    protected AbstractSqlTuningAccessor(ObConnectTemplate connectTemplate) {
        this.connectTemplate = connectTemplate;
    }

    @Override
    public void createOutline(String databaseName, String outlineName, String sqlId, String hint) {
        try {
            doCreateOutline(databaseName, outlineName, sqlId, hint);
        } catch (Exception e) {
            if (StringUtils.isNotEmpty(e.getMessage())) {
                if (e.getMessage().contains("already exists")) {
                    throw new IllegalArgumentException(String.format("Outline %s already exists", sqlId));
                } else {
                    log.error(
                            "Error in creating outline:{} in database:{}, use sql id: {} and hint:{}, error message:{}",
                            databaseName, outlineName, sqlId, hint, e.getMessage());
                    throw e;
                }
            }
        }
    }

    @Override
    public void dropOutline(String databaseName, String outlineName) {
        try {
            doDropOutline(databaseName, outlineName);
        } catch (Exception e) {
            if (StringUtils.isNotEmpty(e.getMessage())) {
                if (e.getMessage().contains("doesn't exist")) {
                    throw new IllegalArgumentException(
                            String.format("Outline %s does not exist", Pair.of(databaseName, outlineName)));
                } else {
                    log.error("Error in dropping outline:{} in database:{}, error message:{}", databaseName,
                            outlineName, e.getMessage());
                    throw e;
                }
            }
        }
    }

    protected abstract void doCreateOutline(String databaseName, String outlineName, String sqlId, String hint);

    protected abstract void doDropOutline(String databaseName, String outlineName);
}
