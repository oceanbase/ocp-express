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

import type { ChartProps } from '@/component/Chart';
import Chart from '@/component/Chart';
import ContentWithQuestion from '@/component/ContentWithQuestion';
import FilterDropdown from '@/component/FilterDropdown';
import MyCard from '@/component/MyCard';
import {
  DATE_FORMAT_DISPLAY,
  DATE_FORMAT_WITHOUT_YEAR_DISPLAY,
  DATE_TIME_FORMAT,
  RFC3339_DATE_TIME_FORMAT,
} from '@/constant/datetime';
import * as ObResourceController from '@/service/ocp-express/ObResourceController';
import { formatSizeForChart } from '@/util';
import { Modal } from '@oceanbase/design';
import { Empty, Space, Spin } from '@oceanbase/design';
import React, { useEffect, useState } from 'react';
import { every, find, flatten, uniq } from 'lodash';
import type { Moment } from 'moment';
import moment from 'moment';
import { byte2GB, isNullValue } from '@oceanbase/util';
import { FullscreenOutlined } from '@oceanbase/icons';
import { useRequest } from 'ahooks';
import useStyles from './Item.style';
import { MAX_POINTS } from '@/constant/monitor';

export interface MetricGroupWithChartConfig extends API.MetricGroup {
  chartConfig?: ChartProps;
  othersMetricKeys?: string[];
}

export interface CommonProps {
  chartConfig?: ChartProps;
  showFilter?: boolean;
  showFullScreen?: boolean;
  clusterId?: number;
  tenantId?: number;
  app?: Global.MonitorApp;
  clusterName?: string;
  tenantName?: string;
  databaseName?: string;
  tableGroupName?: string;
  tableName?: string;
  partitionName?: string;
  startTime?: string;
  endTime?: string;
  zoneName?: string;
  serverIp?: string;
  scope?: string;
  maxPoints?: 120 | 180 | 360 | 720 | 1440;
}

export interface ItemProps extends CommonProps {
  metricGroup: MetricGroupWithChartConfig;
}

const Item: React.FC<ItemProps> = ({
  metricGroup,
  chartConfig,
  showFilter = false,
  showFullScreen = true,
  clusterId,
  tenantId,
  // 默认获取 OB 的监控数据
  app = 'OB',
  clusterName,
  // OB 租户
  tenantName,
  databaseName,
  tableGroupName,
  tableName,
  partitionName,
  startTime,
  endTime,
  zoneName,
  serverIp,
  // 数据的最终聚合维度
  scope,
  // 图表最大展示点数
  maxPoints = MAX_POINTS,
}) => {
  const { styles } = useStyles();
  const [visible, setVisible] = useState(false);
  const [filterKeys, setFilterKeys] = useState<(string | number)[]>([]);
  const [modalFilterKeys, setModalFilterKeys] = useState<(string | number)[]>([]);
  const {
    key: metricGroupKey,
    name,
    description,
    metrics = [],
    othersMetricKeys = [],
    chartConfig: metricGroupChartConfig,
  } = metricGroup;
  const realChartConfig = (metricGroupChartConfig || chartConfig || {}) as any;
  // 当前指标组是否只包含单个指标
  const isSingleMetric = metrics && metrics.length === 1;
  // 如果指标组只有一个指标，则将其作为指标组的单位作展示
  const singleMetricUnit = isSingleMetric && metrics && metrics[0] && metrics[0].unit;

  const [range, setRange] = useState<Moment[]>([]);
  useEffect(() => {
    setRange([moment(startTime), moment(endTime)]);
  }, [startTime, endTime]);

  const labelsObj = {
    app,
    obregion: clusterName,
    obzone: zoneName,
    svr_ip: serverIp,
    // 通常 tenant_name 不会与 obzone、svr_ip 共存，因此将 tenant_name 放在 obzone、svr_ip 前后均不影响请求结果
    tenant_name: tenantName,
    // 数据库、表组、表和分区通常和租户相关联
    database_name: databaseName,
    tablegroup_name: tableGroupName,
    table_name: tableName,
    partition_name: partitionName,
  };
  const labels = Object.keys(labelsObj)
    .filter(key => !isNullValue(labelsObj[key]))
    .map(key => `${key}:${labelsObj[key]}`)
    .join(',');
  let groupBy: string | string[] = [];
  const labelKeyList = Object.keys(labelsObj);
  labelKeyList.forEach((key, index) => {
    // 当聚合维度值不为空，或者 label key 在 scope 顺序之前
    if (!isNullValue(labelsObj[key]) || (scope && index <= labelKeyList.indexOf(scope))) {
      (groupBy as string[]).push(key);
    }
  });
  // 最终的聚合维度
  const lastScope = groupBy && groupBy[groupBy.length - 1];
  groupBy = groupBy.join(',');
  // 监控图对应的指标名数组
  const metricKeys = metrics.map(item => item.key);
  // 用于接口请求的指标字符串
  const metricsString = [...metricKeys, ...othersMetricKeys].join(',');

  // 查询参数变化时，重置筛选条件
  useEffect(() => {
    setFilterKeys([]);
    setModalFilterKeys([]);
  }, [
    moment(startTime).format(DATE_TIME_FORMAT),
    moment(endTime).format(DATE_TIME_FORMAT),
    metricsString,
    labels,
    groupBy,
  ]);

  function getOptions(type: 'card' | 'modal') {
    // 弹窗里的时间
    const modalStartTime = range && range[0];
    const modalEndTime = range && range[1];
    // 使用的时间
    const realStartTime = type === 'card' ? startTime && moment(startTime) : modalStartTime;
    const realEndTime = type === 'card' ? endTime && moment(endTime) : modalEndTime;
    // 用于实际请求的时间
    const realRequestStartTime =
      realStartTime && moment(realStartTime).format(RFC3339_DATE_TIME_FORMAT);
    const realRequestEndTime = realEndTime && moment(realEndTime).format(RFC3339_DATE_TIME_FORMAT);
    return {
      params: {
        id: clusterId,
        tenantId,
        startTime: realRequestStartTime,
        endTime: realRequestEndTime,
        metrics: metricsString,
        labels,
        groupBy,
        maxPoints,
      },
      deps: [
        clusterId,
        tenantId,
        realRequestStartTime,
        realRequestEndTime,
        metricsString,
        labels,
        groupBy,
        type === 'modal' ? visible : '',
      ],

      condition: [
        realRequestStartTime,
        realRequestEndTime,
        labels,
        groupBy,
        // 按 app 聚合，说明是无效请求，要避免发起
        groupBy !== 'app' || '',
        // 弹窗不可见时不向后端发请求，避免请求数过多
        type === 'modal' && !visible ? '' : true,
      ],
    };
  }

  function getChartData(res: API.IterableResponse_SeriesMetricValues_ | undefined) {
    const metricData = flatten(
      ((res && res.data && res.data.contents) || []).map(item => {
        const unit = find(metrics, metricItem => metricItem.key === item.metric?.name)?.unit;
        const newData = (item.data || []).map(dataItem => {
          const { value, ...restDataItem } = dataItem;
          return {
            ...restDataItem,
            // 后端返回的时间戳为 10 位数的秒数，需要转成毫秒数
            timestamp: (dataItem.timestamp || 0) * 1000,
            // 指标名称
            metric: item.metric && item.metric.name,
            ...(!isNullValue(tenantId) && unit === '%'
              ? {
                  percentValue: value,
                }
              : {
                  value,
                }),
            // 分组字段
            // 集群资源趋势，按监控对象进行分组
            // 租户资源趋势，按指标进行分组
            seriesField: isNullValue(tenantId)
              ? item.metric?.labels?.[lastScope]
              : item.metric?.name,
          };
        });
        return newData;
      })
    );
    // 不参与绘图的监控数据，但会用于展示额外信息
    const otherData = metricData.filter(item => othersMetricKeys.includes(item.metric));
    // 参与绘图的监控数据
    const chartData = metricData
      .filter(item => metricKeys.includes(item.metric))
      .map(item => {
        const filterOtherData = otherData.filter(
          // 根据 timestamp 筛选出对应时间点的数据
          otherDataItem => otherDataItem.timestamp === item.timestamp
        );
        const mergeObject = {};
        filterOtherData.forEach(otherDataItem => {
          // 根据 seriesField 获取对应指标的值
          if (otherDataItem.seriesField === item.seriesField) {
            mergeObject[otherDataItem.metric] = otherDataItem.value;
          }
        });
        return {
          ...item,
          ...mergeObject,
        };
      });
    return chartData;
  }

  const options = getOptions('card');
  const modalOptions = getOptions('modal');

  const resourceTrends = isNullValue(tenantId)
    ? // 集群资源趋势
      ObResourceController.clusterResourceTrends
    : // 租户资源趋势
      ObResourceController.tenantResourceTrends;

  // 获取监控数据
  const { data, loading } = useRequest(() => resourceTrends(options.params), {
    ready: every(options.condition, item => !isNullValue(item)),
    refreshDeps: options.deps,
  });
  // 获取 Modal 中的监控数据
  const { data: modalData, loading: modalLoading } = useRequest(
    () => resourceTrends(modalOptions.params),
    {
      ready: every(modalOptions.condition, item => !isNullValue(item)),
      refreshDeps: modalOptions.deps,
    }
  );

  const chartData = getChartData(data);
  const modalChartData = getChartData(modalData);

  const getChartConfig = (chartDataForConfig: Global.ChartData) => ({
    type: 'Line',
    xField: 'timestamp',
    yField: 'value',
    seriesField: 'seriesField',
    meta: {
      timestamp: {
        formatter: (value: number) => {
          // 由于是后端是每天采集一个数据点，因此只需要展示日期
          return moment(value).format(DATE_FORMAT_DISPLAY);
        },
      },
      value: {
        formatter: (value: number) => {
          return isNullValue(tenantId)
            ? // 集群资源趋势
              singleMetricUnit === '%'
              ? `${value}%`
              : singleMetricUnit === 'G'
              ? `${byte2GB(value)}G`
              : value
            : // 租户资源趋势，需要对特定指标的值进行格式化
            metricGroupKey === 'disk' || metricGroupKey === 'memory'
            ? formatSizeForChart(chartDataForConfig, value)
            : value;
        },
      },
      // 租户资源趋势由于是双轴图，percentValue 是对应的百分比维度字段，这里对其进行格式化
      percentValue: {
        formatter: (value: number) => {
          return `${value}%`;
        },
      },
      metric: {
        formatter: (metric: string) =>
          isNullValue(tenantId)
            ? // 集群资源趋势，按监控对象进行分组
              metric
            : // 租户资源趋势，按指标进行分组
            isSingleMetric
            ? // 指标组只包含一个指标，使用指标组名代替指标名即可
              name
            : // 指标组包含多个指标，通过 metricGroup 查询指标对应的展示名称
              find(metricGroup.metrics, item => item.key === metric)?.name,
      },
    },
    xAxis: {
      type: 'time',
      tickCount: 3,
      label: {
        // 空间不够时，避免自动旋转，此时图表会自动对坐标轴标签做裁剪
        autoRotate: false,
        formatter: (value: number) => {
          // X 坐标轴日期展示去掉年份
          return moment(value, DATE_FORMAT_DISPLAY).format(DATE_FORMAT_WITHOUT_YEAR_DISPLAY);
        },
      },
    },
    yAxis: {
      nice: true,
      tickCount: 3,
      ...realChartConfig.yAxis,
    },
    legend: {
      // legend 过多、过长时进行翻页处理
      flipPage: true,
      ...realChartConfig.legend,
    },
    ...realChartConfig,
  });

  const title = (
    <ContentWithQuestion
      className={styles.title}
      content={`${name}${singleMetricUnit ? ` (${singleMetricUnit})` : ''}`}
      tooltip={{
        placement: 'right',
        // 指标组只包含一个指标，则用指标组信息代替指标信息
        title: isSingleMetric ? (
          <div>{`${description}`}</div>
        ) : (
          <div>
            <div>{description}</div>
            <div>
              {metrics.map(metric => (
                <div key={metric.key}>
                  {`${metric.name}: ${metric.description || ''}${
                    metric.unit ? ` (${metric.unit})` : ''
                  }`}
                </div>
              ))}
            </div>
          </div>
        ),
      }}
    />
  );

  return (
    <MyCard
      title={title}
      className={styles.container}
      extra={
        <Space>
          {showFilter && (
            <FilterDropdown
              value={filterKeys}
              onChange={value => {
                setFilterKeys(value);
              }}
              filters={uniq(chartData.map(item => item.seriesField)).map(item => ({
                value: item,
                label: item,
              }))}
            />
          )}

          {showFullScreen && (
            <FullscreenOutlined className={styles.fullscreen} onClick={() => setVisible(true)} />
          )}
        </Space>
      }
    >
      <Spin spinning={loading}>
        {chartData.length > 0 ? (
          <Chart
            height={186}
            data={
              isNullValue(tenantId)
                ? // 集群资源视图: 折线图数据
                  chartData.filter(
                    item => filterKeys.length === 0 || filterKeys.includes(item.seriesField)
                  )
                : // 租户资源视图: 双轴图数据
                  [
                    chartData.filter(item => !isNullValue(item.value)),
                    chartData.filter(item => !isNullValue(item.percentValue)),
                  ]
            }
            {...getChartConfig(chartData)}
          />
        ) : (
          <Empty style={{ height: 160 }} imageStyle={{ marginTop: 46 }} />
        )}
      </Spin>
      <Modal
        width={960}
        title={title}
        visible={visible}
        destroyOnClose={true}
        footer={false}
        onCancel={() => setVisible(false)}
      >
        <Spin spinning={modalLoading}>
          {showFilter && (
            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <FilterDropdown
                value={modalFilterKeys}
                onChange={value => {
                  setModalFilterKeys(value);
                }}
                filters={uniq(modalChartData.map(item => item.seriesField)).map(item => ({
                  value: item,
                  label: item,
                }))}
              />
            </div>
          )}

          {modalChartData.length > 0 ? (
            <Chart
              height={300}
              data={
                isNullValue(tenantId)
                  ? // 集群资源视图: 折线图数据
                    modalChartData.filter(
                      item =>
                        modalFilterKeys.length === 0 || modalFilterKeys.includes(item.seriesField)
                    )
                  : // 租户资源视图: 双轴图数据
                    [
                      modalChartData.filter(item => !isNullValue(item.value)),
                      modalChartData.filter(item => !isNullValue(item.percentValue)),
                    ]
              }
              {...getChartConfig(modalChartData)}
            />
          ) : (
            <Empty style={{ height: 240 }} imageStyle={{ marginTop: 60 }} />
          )}
        </Spin>
      </Modal>
    </MyCard>
  );
};

export default Item;
