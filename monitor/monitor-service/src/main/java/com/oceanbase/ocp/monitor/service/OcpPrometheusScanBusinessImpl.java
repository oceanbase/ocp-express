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

package com.oceanbase.ocp.monitor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.oceanbase.ocp.monitor.OcpPrometheusScanBusiness;
import com.oceanbase.ocp.monitor.constants.MonitorConstants;
import com.oceanbase.ocp.monitor.model.OcpPrometheusData;
import com.oceanbase.ocp.monitor.model.OcpPrometheusDataContainer;
import com.oceanbase.ocp.monitor.model.OcpPrometheusQueryParam;
import com.oceanbase.ocp.monitor.model.OcpPrometheusScanResp;
import com.oceanbase.ocp.monitor.model.metric.MetricData;
import com.oceanbase.ocp.monitor.model.metric.MetricDataRange;
import com.oceanbase.ocp.monitor.store.MetricDataStoreFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OcpPrometheusScanBusinessImpl implements InitializingBean, OcpPrometheusScanBusiness {

    private static final long SLOW_SCAN_MILLIS = 100L;

    private static OcpPrometheusScanBusinessImpl instance;

    @Autowired
    private MetricDataStoreFactory metricDataStoreFactory;

    @Autowired
    private SeriesIdKeyService seriesIdKeyService;

    @Override
    public void afterPropertiesSet() {
        instance = this;
    }

    public static OcpPrometheusScanBusinessImpl build() {
        return instance;
    }

    @Override
    public List<OcpPrometheusScanResp> scan(OcpPrometheusQueryParam param) {
        validateParam(param);

        Long startTime = param.getStartTime();
        Long endTime = param.getEndTime();

        long scanStartAt = System.currentTimeMillis();

        List<OcpPrometheusScanResp> scanRespList = new ArrayList<>();
        List<Long> seriesIds = seriesIdKeyService.scanSeriesIds(param);
        long step1At = System.currentTimeMillis();

        if (seriesIds.isEmpty()) {
            log.debug("no seriesIds found, param={}", param);
            return scanRespList;
        }

        Map<Long, MetricDataRange> seriesId2DataList;
        if (param.isOnlyCache()) {
            seriesId2DataList = metricDataStoreFactory.rangesFromCache(seriesIds, startTime, endTime, param.getStep());
        } else {
            seriesId2DataList = metricDataStoreFactory.ranges(seriesIds, startTime, endTime, param.getStep());
        }
        long step2At = System.currentTimeMillis();

        Map<Long, String> seriesId2Key = seriesIdKeyService.getSeriesId2KeyMap(seriesIds);
        long step3At = System.currentTimeMillis();
        long valuesCount = 0;
        for (Map.Entry<Long, MetricDataRange> entry : seriesId2DataList.entrySet()) {
            Long seriesId = entry.getKey();
            List<MetricData> dataList = entry.getValue().getDataList();
            if (CollectionUtils.isEmpty(dataList)) {
                continue;
            }
            OcpPrometheusDataContainer container = new OcpPrometheusDataContainer();
            for (MetricData data : dataList) {
                container.add(new OcpPrometheusData(data.getTimestamp(), data.getValue()));
                valuesCount++;
            }
            String measurement = seriesId2Key.get(seriesId);
            scanRespList.add(new OcpPrometheusScanResp(measurement, container));
        }

        long step4At = System.currentTimeMillis();
        long costMillis = step4At - scanStartAt;
        String logPrefix = costMillis >= SLOW_SCAN_MILLIS ? "ocp-prometheus-scan-slow" : "ocp-prometheus-scan";
        Object[] arguments = new Object[] {logPrefix, costMillis, seriesIds.size(), valuesCount, step1At - scanStartAt,
                step2At - step1At, step3At - step2At, step4At - step3At, param};
        log.debug("{}, costMillis={}, seriesIdsCount={}, valuesCount={}, "
                + "step1Cost={}, step2Cost={}, step3Cost={}, step4Cost={}, param={}", arguments);
        return scanRespList;
    }

    private void validateParam(OcpPrometheusQueryParam param) {
        Validate.notNull(param, "param cannot be null");
        Long startTime = param.getStartTime();
        Long endTime = param.getEndTime();
        Validate.notNull(startTime, String.format("startTime not set, param=%s", param));
        Validate.notNull(endTime, String.format("endTime not set, param=%s", param));
        Validate.isTrue(startTime <= endTime, String.format("startTime cannot after endTime, param=%s", param));
        Validate.isTrue(endTime < MonitorConstants.MAY_MILLIS_JUDGE_THRESHOLD,
                String.format("invalid endTime, may milliseconds used, param=%s", param));
    }

}
