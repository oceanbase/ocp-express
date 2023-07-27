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

import { formatMessage } from '@/util/intl';
import { useMemo } from 'react';
import { groupBy } from 'lodash';
import moment from 'moment';
import { useRequest } from 'ahooks';
import type { Options } from 'ahooks/lib/useRequest/src/types';
import { findBy, sortByNumber } from '@oceanbase/util';
import * as MonitorController from '@/service/ocp-express/MonitorController';
import { RFC3339_DATE_TIME_FORMAT } from '@/constant/datetime';
import type { ComparisonMetricChartItemProps } from '@/component/ComparisonMetricChart/Item';
import { getLabelsAndGroupBy } from '@/util/monitor';

export const useMetricChartData = (
  {
    startTime,
    endTime,
    scope,
    app,
    clusterName,
    tenantName,
    zoneName,
    serverIp,
    mount_point,
    mount_label,
    task_type,
    process,
    device,
    cpu,
    metricGroup,
  }: ComparisonMetricChartItemProps,
  requestOptions: Options<any, any> = {}
) => {
  const { metrics = [] } = metricGroup;

  const { labels, groupBy: chartGroupBy } = getLabelsAndGroupBy({
    scope,
    app,
    clusterName,
    tenantName,
    zoneName,
    serverIp,
    mount_point,
    mount_label,
    task_type,
    process,
    device,
    cpu,
  });

  const getParams = () => {
    const metricsString = metrics.map(item => item.key).join(',');
    return {
      startTime: moment(startTime).format(RFC3339_DATE_TIME_FORMAT),
      endTime: moment(endTime).format(RFC3339_DATE_TIME_FORMAT),
      interval: 60,
      metrics: metricsString,
      labels,
      groupBy: chartGroupBy,
    };
  };

  // 获取 Metric 监控数据
  const {
    data,
    runAsync: queryMetric,
    loading,
  } = useRequest(
    () => {
      const params = getParams();
      return MonitorController.queryMetric(params);
    },
    {
      ready: !!startTime && !!endTime,
      refreshDeps: [startTime, endTime],
      ...requestOptions,
    }
  );

  function getChartData(res: { data: { contents: any } }) {
    const metricData = (res && res.data && res.data.contents) || [];
    let chartData = [];
    metricData.forEach((item: { [x: string]: any; timestamp?: any }) => {
      Object.keys(item)
        .filter(key => key !== 'timestamp')
        .forEach(key => {
          const dataItem = {};
          const metricItem = findBy(metrics, 'key', key);
          // 使用指标的短名称 name，比如 { key: 'sql_all_count', name: 'all' }，取其中的 name
          dataItem.metric = metricItem.name;
          dataItem.key = metricItem.key;
          dataItem.value = item[key];

          dataItem.timestamp = item.timestamp * 1000;
          chartData.push(dataItem);
        });
    });
    // 需要根据 value 从大到小排序，这样图例和 tooltip 才会从大到小展示，方便用户查看
    chartData = chartData.sort((a, b) => sortByNumber(b, a, 'value'));
    return chartData;
  }

  const chartData = useMemo(() => getChartData(data), [data]);

  return {
    queryMetric,
    loading,
    chartData,
  };
};

// 获取 Metric 对比监控数据
export const useComparisonMetrichartDataList = ({
  baselineStartTime,
  baselineEndTime,
  startTime,
  endTime,
  ...reseProps
}: ComparisonMetricChartItemProps) => {
  const ready = !!baselineStartTime && !!baselineEndTime && !!startTime && !!endTime;

  const { chartData: baselineChartData, loading: baseLoading } = useMetricChartData(
    {
      startTime: baselineStartTime,
      endTime: baselineEndTime,
      ...reseProps,
    },
    { ready }
  );

  const { chartData, loading } = useMetricChartData(
    {
      startTime,
      endTime,
      ...reseProps,
    },
    { ready }
  );

  const { metrics = [] } = reseProps.metricGroup || {};

  const baselineChartObj = groupBy(baselineChartData, 'key');
  const chartObj = groupBy(chartData, 'key');

  const timestampDiff = moment(startTime).diff(baselineStartTime);
  const realLoading = baseLoading && loading;

  return {
    chartDataList: metrics
      .map(({ key, unit }) => {
        const baseList = baselineChartObj[key] || [];
        const list = chartObj[key] || [];

        if (baseList.length > 0 || list.length > 0) {
          return [
            ...baseList.map(item => {
              return {
                ...item,
                metric: formatMessage({
                  id: 'ocp-express.src.hook.sqlDiagnosis.BasePeriod',
                  defaultMessage: '基准时段',
                }),
                unit,
              };
            }),
            ...list.map((item, index) => {
              return {
                metric: formatMessage({
                  id: 'ocp-express.src.hook.sqlDiagnosis.ComparisonPeriod',
                  defaultMessage: '对比时段',
                }),
                value: item.value,
                key,
                // 先使用基准时段的时间,如果基准时段数据不存在再采用当前时间减去两者相差的时间
                timestamp: baseList[index]?.timestamp || item.timestamp - timestampDiff,
                ComparisonTimestamp: item.timestamp,
                unit,
              };
            }),
          ];
        }

        return null;
      })
      .filter(v => !!v),
    loading: realLoading,
  };
};
