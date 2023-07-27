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

package com.oceanbase.ocp.bootstrap.db.property;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import com.oceanbase.ocp.bootstrap.Params;
import com.oceanbase.ocp.bootstrap.Params.PropertyPair;
import com.oceanbase.ocp.bootstrap.config.env.Version;
import com.oceanbase.ocp.bootstrap.core.def.Row;
import com.oceanbase.ocp.bootstrap.util.SQLUtils;

public class ConfigPropertyManager {

    private final DataSource metaDataSource;

    public ConfigPropertyManager(DataSource metaDataSource) {
        this.metaDataSource = metaDataSource;
    }

    public String getProperty(String name) {
        try {
            List<Row> rows = SQLUtils.queryRows(metaDataSource, "select * from config_properties where `key`=?", name);
            return extractValue(rows);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    static String extractValue(List<Row> rows) {
        if (rows.size() < 1) {
            return null;
        }
        Row row = rows.get(0);
        Object v = row.get("value");
        if (v != null) {
            return (String) v;
        }
        return (String) row.get("default_value");
    }

    public void updateProperty(String name, String value) {
        try {
            SQLUtils.execute(metaDataSource, "update config_properties set value=? where `key`=?", value, name);
            System.setProperty(name, value);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void update(List<Params.PropertyPair> properties) {
        if (properties == null) {
            return;
        }
        for (PropertyPair property : properties) {
            updateProperty(property.getName(), property.getValue());
        }
    }

    public Version ocpVersion() {
        Version fullVersion = ocpFullVersion();
        if (fullVersion == null) {
            return new Version(getProperty("ocp.version"));
        }
        return fullVersion;
    }

    public Version ocpFullVersion() {
        String bootstrapVerString = getProperty("ocp.version.full");
        if (bootstrapVerString == null) {
            return null;
        }
        return new Version(bootstrapVerString);
    }

    public void updateOcpVersion(Version version) {
        updateProperty("ocp.version", version.getSimpleVersionString());
    }

    public void updateOcpFullVersion(Version version) {
        updateProperty("ocp.version.full", version.getVersionString());
    }
}
