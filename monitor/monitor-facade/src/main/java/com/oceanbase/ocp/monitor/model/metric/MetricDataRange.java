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

package com.oceanbase.ocp.monitor.model.metric;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricDataRange {

    private Long seriesId;

    private Long rangeFrom;

    private Long rangeTo;

    private int count;

    private List<MetricData> dataList;

    public MetricDataRange(Long seriesId, List<MetricData> dataList) {
        this.seriesId = seriesId;
        this.dataList = dataList;
        this.count = dataList.size();
        if (this.count == 0) {
            rangeFrom = null;
            rangeTo = null;
        } else {
            rangeFrom = dataList.get(0).getTimestamp();
            rangeTo = dataList.get(this.count - 1).getTimestamp();
        }
    }

}
