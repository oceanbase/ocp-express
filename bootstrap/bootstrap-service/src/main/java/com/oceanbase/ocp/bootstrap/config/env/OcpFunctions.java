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

package com.oceanbase.ocp.bootstrap.config.env;

import javax.sql.DataSource;

import com.oceanbase.ocp.bootstrap.Bootstrap;
import com.oceanbase.ocp.bootstrap.db.DataSourceName;
import com.oceanbase.ocp.bootstrap.db.property.ConfigPropertyManager;

public class OcpFunctions {

    public static Version oldOcpVersion() {
        DataSource dataSource = Bootstrap.getInstance().getDataSourceProvider().getDataSource(DataSourceName.SPRING);
        if (dataSource == null) {
            return null;
        }
        return new ConfigPropertyManager(dataSource).ocpVersion();
    }

}
