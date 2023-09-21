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
import React, { useState, useEffect, useRef } from 'react';
import { Empty, Space, Spin, Tooltip, Modal } from '@oceanbase/design';
import { every } from 'lodash';
import moment from 'moment';
import type { Moment } from 'moment';
import { findByValue, isNullValue } from '@oceanbase/util';
import { FilterOutlined, FullscreenOutlined } from '@oceanbase/icons';
import { useInViewport } from 'ahooks';
import * as MonitorController from '@/service/ocp-express/MonitorController';
import { MAX_POINTS, MONITOR_SCOPE_LIST } from '@/constant/monitor';
import {
  DATE_TIME_FORMAT_DISPLAY,
  RFC3339_DATE_TIME_FORMAT,
  TIME_FORMAT_WITHOUT_SECOND,
} from '@/constant/datetime';
import useRequestOfMonitor from '@/hook/useRequestOfMonitor';
import { formatValueForChart } from '@/util';
import { getLabelsAndGroupBy, getTopChartData, getTopTargetList } from '@/util/monitor';
import type { ChartProps } from '@/component/Chart';
import Chart from '@/component/Chart';
import MyCard from '@/component/MyCard';
import ContentWithQuestion from '@/component/ContentWithQuestion';
import RangeTimeDropdown from '@/component/RangeTimeDropdown';
import type { OptionType, OptionValue } from '@/component/CheckboxPopover';
import CheckboxPopover from '@/component/CheckboxPopover';
import useStyles from './Item.style';

export interface MetricGroupWithChartConfig extends API.MetricGroup {
  chartConfig?: ChartProps;
  tooltipScroll?: boolean;
  scope?: Global.MonitorScope;
  othersMetricKeys?: string[];
}

export interface CommonProps {
  setMenuKey?: (v: 'hour' | 'day' | 'week') => void;
  menuKey?: 'hour' | 'day' | 'week';
  // 是否支持筛选
  showFilter?: boolean;
  // 筛选入口类型: 图标 | 图标+文字
  filterEntryType?: 'icon' | 'iconWithLabel';
  // 是否展示放大查看入口
  showEnlargeEntry?: boolean;
  // 是否展示弹窗内的时间范围选择器
  showModalRangePicker?: boolean;
  chartConfig?: ChartProps;
  scope?: Global.MonitorScope;
  app?: Global.MonitorApp;
  clusterName?: string;
  tenantName?: string;
  isRealtime?: boolean;
  startTime?: string;
  endTime?: string;
  zoneName?: string;
  serverIp?: string;
  mount_point?: string;
  mount_label?: string;
  maxPoints?: 120 | 180 | 360 | 720 | 1440;
  obproxyCluster?: string;
  obproxyClusterId?: number;
  limit?: number;
  className?: string;
  style?: React.CSSProperties;
  titleStyle?: React.CSSProperties;
}

export interface ItemProps extends CommonProps {
  metricGroup: MetricGroupWithChartConfig;
}

const Item: React.FC<ItemProps> = ({
  metricGroup,
  menuKey = 'hour',
  showFilter = false,
  filterEntryType = 'icon',
  showEnlargeEntry = true,
  showModalRangePicker = true,
  chartConfig,
  // 默认获取 OB 的监控数据
  app = 'OB',
  clusterName,
  // 租户
  tenantName,
  zoneName,
  serverIp,
  mount_point,
  mount_label,
  // OBProxy
  obproxyCluster,
  obproxyClusterId,
  isRealtime,
  startTime,
  endTime,
  // limit 可为空，为空时获取全部对象的监控数据
  limit,
  // 数据的最终聚合维度
  scope: outerScope,
  // 图表最大展示点数
  maxPoints = MAX_POINTS,
  className,
  titleStyle,
  ...restProps
}) => {
  const { styles } = useStyles();
  const ref = useRef();
  // 图表是否在可视范围内
  const [isInViewPort] = useInViewport(ref);
  const [inViewPort, setInViewPort] = useState(false);

  // 如果进入过可视范围内，则缓存起来，避免重新进入可视范围时重复发起请求
  useEffect(() => {
    if (isInViewPort) {
      setInViewPort(isInViewPort);
    }
  }, [isInViewPort]);

  const [visible, setVisible] = useState(false);
  const [optionList, setOptionList] = useState<OptionType[]>([]);
  // 默认选中的对象列表，用于 CheckboxPopover 组件内部的重置功能
  const [defaultSelectedList, setDefaultSelectedList] = useState<OptionValue[]>([]);
  // 选中的对象列表
  const [selectedList, setSelectedList] = useState<OptionValue[]>([]);

  const {
    name,
    description,
    metrics = [],
    chartConfig: metricGroupChartConfig,
    tooltipScroll,
    scope: metricGroupScope,
    othersMetricKeys = [],
  } = metricGroup;
  const realChartConfig = (metricGroupChartConfig || chartConfig || {}) as any;
  // 当前指标组是否只包含单个指标
  const isSingleMetric = metrics && metrics.length === 1;

  const [modalRange, setModalRange] = useState<Moment[]>([]);

  // 不展示弹窗中的时间范围选择器时，需要手动设置弹窗图表的 range ，这一逻辑和 RangeTimeDropdown 组件相关
  useEffect(() => {
    if (visible && !showModalRangePicker) {
      setModalRange(startTime && endTime ? [moment(startTime), moment(endTime)] : []);
    }
  }, [visible, showModalRangePicker]);

  const scope = metricGroupScope || outerScope;
  const scopeLabel = findByValue(MONITOR_SCOPE_LIST, scope).label;

  const { labels, groupBy } = getLabelsAndGroupBy({
    scope,
    app,
    clusterName,
    obproxyClusterName: obproxyCluster,
    obproxyClusterId,
    tenantName,
    zoneName,
    serverIp,
    mount_point,
    mount_label,
  });

  // 监控图对应的指标名数组
  const metricKeys = metrics.map((item) => item.key);
  // 用于接口请求的指标字符串
  const metricsString = [...metricKeys, ...othersMetricKeys].join(',');

  function getOptions(type: 'card' | 'modal') {
    const propsRange = [moment(startTime), moment(endTime)];
    const realModalRange = isRealtime ? propsRange : modalRange;

    // 弹窗里的时间
    const modalStartTime = realModalRange && realModalRange[0];
    const modalEndTime = realModalRange && realModalRange[1];
    // 使用的时间
    const realStartTime = type === 'card' ? propsRange && propsRange[0] : modalStartTime;
    const realEndTime = type === 'card' ? propsRange && propsRange[1] : modalEndTime;
    // 用于实际请求的时间
    const realRequestStartTime =
      realStartTime && moment(realStartTime).format(RFC3339_DATE_TIME_FORMAT);
    const realRequestEndTime = realEndTime && moment(realEndTime).format(RFC3339_DATE_TIME_FORMAT);

    return {
      params: {
        startTime: realRequestStartTime,
        endTime: realRequestEndTime,
        metrics: metricsString,
        labels,
        groupBy,
        limit,
        maxPoints,
      },
      deps: [
        realRequestStartTime,
        realRequestEndTime,
        metricsString,
        labels,
        groupBy,
        limit,
        inViewPort,
        // top 监控由于弹窗展示时会触发 RangeTimeDropdown 的 onChange 事件，所以不需要将 visible 设置为 deps
        // type === 'modal' ? visible : '',
      ],
      condition: [
        realRequestStartTime,
        realRequestEndTime,
        labels,
        groupBy,
        // 不在可视范围内时不发起请求
        inViewPort || '',
        // 弹窗不可见时不向后端发请求，避免请求数过多
        type === 'modal' && !visible ? '' : true,
      ],
    };
  }

  const options = getOptions('card');
  const modalOptions = getOptions('modal');

  // 获取监控数据
  const {
    data,
    loading,
    run: queryMetricTop,
  } = useRequestOfMonitor(MonitorController.queryMetricTop, {
    manual: true,
    isRealtime,
    onSuccess: (res) => {
      if (res.successful && showFilter) {
        setOptionList(
          getTopTargetList({
            dataList: res.data?.contents || [],
            groupBy,
            metricKeys,
          }).map((item) => ({
            value: item,
            label: item,
            span: 24,
          })),
        );
        const newDefaultSelectedList = getTopTargetList({
          dataList: res.data?.contents || [],
          groupBy,
          metricKeys,
          limit: 10,
        });
        setDefaultSelectedList(newDefaultSelectedList);
        setSelectedList(newDefaultSelectedList);
      }
    },
  });

  useEffect(() => {
    // 需要手动实现条件请求，因为 useRequest ready 配置仅在首次请求生效
    if (every(options.condition, (item) => !isNullValue(item))) {
      queryMetricTop(options.params);
    }
  }, options.deps);

  // 获取 Modal 中的监控数据
  const {
    data: modalData,
    loading: modalLoading,
    run: queryModalMetricTop,
  } = useRequestOfMonitor(MonitorController.queryMetricTop, {
    manual: true,
    isRealtime,
  });

  useEffect(() => {
    // 需要手动实现条件请求，因为 useRequest ready 配置仅在首次请求生效
    if (every(options.condition, (item) => !isNullValue(item))) {
      queryMetricTop(options.params);
    }
  }, options.deps);

  useEffect(() => {
    if (every(modalOptions.condition, (item) => !isNullValue(item))) {
      queryModalMetricTop(modalOptions.params);
    }
  }, modalOptions.deps);

  // Top 监控也可能同时有多个细分指标，此时 target 应该展示指标
  const targetWithMetric = metrics.length > 1;

  const chartData = getTopChartData({
    dataList: data?.data?.contents || [],
    groupBy,
    metricKeys,
    targetWithMetric,
    clusterName,
    othersMetricKeys,
  }).filter((item) => (showFilter ? selectedList.includes(item.target) : true));

  const modalChartData = getTopChartData({
    dataList: modalData?.data?.contents || [],
    groupBy,
    metricKeys,
    targetWithMetric,
    clusterName,
    othersMetricKeys,
  }).filter((item) => (showFilter ? selectedList.includes(item.target) : true));

  // 先用 metricGroup 的 chartConfig, 在使用 common 的 chartConfig
  const { meta = {}, xAxis = {}, ...restRealChartConfig } = realChartConfig;
  const config = {
    type: 'Line',
    xField: 'timestamp',
    yField: 'value',
    seriesField: 'target',
    // 关闭动画，避免实时图表更新不符合预期，以及提升大数据量下的渲染性能
    animation: false,
    meta: {
      timestamp: {
        formatter: (value) => {
          return moment(value).format(DATE_TIME_FORMAT_DISPLAY);
        },
      },
      // 固定小数位数为 2 位，方便对比
      value: {
        formatter: (value: number) => {
          return formatValueForChart(chartData, value, metrics[0].unit);
        },
      },
      ...meta,
    },
    xAxis: {
      type: 'time',
      label: {
        formatter: (value) => {
          return moment(value, DATE_TIME_FORMAT_DISPLAY).format(TIME_FORMAT_WITHOUT_SECOND);
        },
      },
      ...xAxis,
    },
    ...restRealChartConfig,
  };

  const title = (
    <ContentWithQuestion
      className={styles.title}
      style={titleStyle}
      content={name}
      tooltip={
        description && {
          placement: 'right',
          // 如果指标组只包含一个指标，则用指标组信息代替指标信息
          title: isSingleMetric ? (
            <div>{description}</div>
          ) : (
            <div>
              <div>{description}</div>
              <ul>
                {metrics.map((metric) => (
                  <li key={metric.key}>{`${metric.name}: ${metric.description}`}</li>
                ))}
              </ul>
            </div>
          ),
        }
      }
    />
  );

  return (
    <div ref={ref}>
      <MyCard
        // 实时模式下不展示 loading 态
        loading={loading && !isRealtime}
        title={title}
        className={`${styles.container} ${className}`}
        extra={
          <Space>
            {/* 对象筛选 */}
            {showFilter && (
              <CheckboxPopover
                placement="bottomRight"
                trigger={['click']}
                arrowPointAtCenter={true}
                title={formatMessage(
                  {
                    id: 'ocp-express.MetricChart.DrilldownDrawer.DrilldownChart.SelectScopeitemlabel',
                    defaultMessage: '选择{scopeItemLabel}',
                  },
                  { scopeItemLabel: scopeLabel },
                )}
                options={optionList}
                defaultValue={defaultSelectedList}
                value={selectedList}
                onChange={(value) => {
                  setSelectedList(value);
                }}
                maxSelectCount={10}
                maxSelectCountLabel={formatMessage(
                  {
                    id: 'ocp-express.MetricChart.DrilldownDrawer.DrilldownChart.YouCanSelectUpTo',
                    defaultMessage: '最多可选择 10 个{scopeItemLabel}',
                  },
                  { scopeItemLabel: scopeLabel },
                )}
                overlayStyle={{
                  minWidth: 320,
                  maxWidth: 320,
                }}
              >
                {filterEntryType === 'icon' ? (
                  <Tooltip
                    title={formatMessage(
                      {
                        id: 'ocp-express.MetricChart.DrilldownDrawer.DrilldownChart.SelectScopeitemlabel',
                        defaultMessage: '选择{scopeItemLabel}',
                      },
                      { scopeItemLabel: scopeLabel },
                    )}
                  >
                    <FilterOutlined
                      style={{ color: 'rgba(0, 0, 0, 0.45)' }}
                      className="pointable"
                    />
                  </Tooltip>
                ) : (
                  <Space size={6} style={{ color: 'rgba(0, 0, 0, 0.45)' }} className="pointable">
                    <FilterOutlined />
                    <span>
                      {formatMessage(
                        {
                          id: 'ocp-express.MetricChart.DrilldownDrawer.DrilldownChart.SelectScopeitemlabel',
                          defaultMessage: '选择{scopeItemLabel}',
                        },
                        { scopeItemLabel: scopeLabel },
                      )}
                    </span>
                  </Space>
                )}
              </CheckboxPopover>
            )}

            {/* 放大查看 */}
            {showEnlargeEntry && (
              <FullscreenOutlined
                className={styles.fullscreen}
                onClick={() => {
                  setVisible(true);
                }}
              />
            )}
          </Space>
        }
        {...restProps}
      >
        {chartData.length > 0 ? (
          <Chart
            height={186}
            data={chartData}
            tooltipScroll={
              tooltipScroll
                ? {
                    maxHeight: '164px',
                  }
                : false
            }
            {...config}
          />
        ) : (
          <Empty style={{ height: 160 }} imageStyle={{ marginTop: 46 }} />
        )}

        <Modal
          width={960}
          title={title}
          visible={visible}
          destroyOnClose={true}
          footer={false}
          onCancel={() => setVisible(false)}
        >
          <Spin spinning={modalLoading && !isRealtime}>
            {showModalRangePicker && !isRealtime && (
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'flex-end',
                  alignItems: 'center',
                  marginBottom: 16,
                }}
              >
                <RangeTimeDropdown
                  menuKeys={['hour', 'day', 'week']}
                  // 默认继承外部传入的 menuKey
                  defaultMenuKey={menuKey}
                  // 优先级: defaultMenuKey > defaultValue
                  defaultValue={[moment(startTime), moment(endTime)]}
                  onChange={(value) => {
                    setModalRange(value);
                  }}
                />
              </div>
            )}

            {modalChartData.length > 0 ? (
              <Chart
                height={300}
                data={modalChartData}
                tooltipScroll={{
                  maxHeight: '284px',
                }}
                {...config}
              />
            ) : (
              <Empty style={{ height: 240 }} imageStyle={{ marginTop: 76 }} />
            )}
          </Spin>
        </Modal>
      </MyCard>
    </div>
  );
};

export default Item;
