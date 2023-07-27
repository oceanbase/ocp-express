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
package com.oceanbase.ocp.monitor.model.exporter;

import java.time.OffsetDateTime;

import com.oceanbase.ocp.monitor.constants.ExporterStatus;
import com.oceanbase.ocp.monitor.util.ExporterAddressUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ExporterAddress {

    private long id;

    private String exporterUrl;

    private ExporterStatus status;

    private OffsetDateTime createTime;

    private OffsetDateTime updateTime;

    private String path;

    public String getPath() {
        if (path == null && exporterUrl != null) {
            path = "/metrics" + ExporterAddressUtils.getPath(exporterUrl);
        }
        return path;
    }

}
