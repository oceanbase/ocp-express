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
import { useSelector } from 'umi';
import { Empty, Space, Spin, Tooltip, Modal, token } from '@oceanbase/design';
import { Ranger } from '@oceanbase/ui';
import React, { useEffect, useState, useRef } from 'react';
import { every, uniq } from 'lodash';
import type { Moment } from 'moment';
import moment from 'moment';
import { findBy, isNullValue, jsonParse, sortByNumber } from '@oceanbase/util';
import Icon, { CloseOutlined, FullscreenOutlined } from '@oceanbase/icons';
import { useInViewport } from 'ahooks';
import * as MonitorController from '@/service/ocp-express/MonitorController';
import {
  DATE_TIME_FORMAT_DISPLAY,
  RFC3339_DATE_TIME_FORMAT,
  TIME_FORMAT_WITHOUT_SECOND,
} from '@/constant/datetime';
import useRequestOfMonitor from '@/hook/useRequestOfMonitor';
import { formatValueForChart } from '@/util';
import { getLabelsAndGroupBy } from '@/util/monitor';
import tracert from '@/util/tracert';
import MyCard from '@/component/MyCard';
import type { ChartProps } from '@/component/Chart';
import type { TooltipScroll } from '@/component/Chart';
import Chart from '@/component/Chart';
import ContentWithQuestion from '@/component/ContentWithQuestion';
import type { MonitorServer } from '@/component/MonitorSearch';
import { ReactComponent as DrilldownSvg } from '@/asset/drilldown.svg';
import DrilldownDrawer from './DrilldownDrawer';
import useStyles from './Item.style';
import { MAX_POINTS } from '@/constant/monitor';

export interface MetricGroupWithChartConfig extends API.MetricGroup {
  chartConfig?: ChartProps;
  drilldownable?: string;
  tooltipScroll?: TooltipScroll;
  scope?: Global.MonitorScope;
  metrics: any[];
}

export interface CommonProps {
  // 监控卡片的顺序索引，用于判断是否为第一个卡片
  index?: number;
  /* 一级下钻维度，为空时不展示下钻入口，只支持集群、租户和主机 */
  drilldownScope?: 'ob_cluster_id' | 'tenant_name' | 'svr_ip';
  metricClass?: API.MetricClass;
  // 是否展示时间范围选择器
  showRangePicker?: boolean;
  // 是否展示弹窗内的时间范围选择器
  showModalRangePicker?: boolean;
  // 是否展示放大查看入口
  showEnlargeEntry?: boolean;
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
  serverPort?: string;
  mount_point?: string;
  mount_label?: string;
  task_type?: string;
  process?: string;
  device?: string;
  cpu?: string;
  zoneNameList?: string[];
  serverList?: MonitorServer[];
  tenantList?: API.TenantInfo[];
  className?: string;
  style?: React.CSSProperties;
  titleStyle?: React.CSSProperties;
  maxPoints?: 120 | 180 | 360 | 720 | 1440;
}

export interface ItemProps extends CommonProps {
  metricGroup: MetricGroupWithChartConfig;
}

const Item: React.FC<ItemProps> = ({
  index: itemIndex,
  drilldownScope,
  metricClass,
  metricGroup,
  showRangePicker = false,
  showModalRangePicker = true,
  showEnlargeEntry = true,
  chartConfig,
  // 数据的最终聚合维度
  scope,
  // 默认获取 OB 的监控数据
  app = 'OB',
  clusterName,
  // 租户
  tenantName,
  isRealtime,
  startTime,
  endTime,
  zoneName,
  serverIp,
  serverPort,
  mount_point,
  mount_label,
  task_type,
  process,
  device,
  cpu,
  zoneNameList,
  serverList,
  tenantList,
  className,
  titleStyle,
  // 图表最大展示点数
  maxPoints = MAX_POINTS,
  ...restProps
}) => {
  const { styles } = useStyles();
  const { userData } = useSelector((state: DefaultRootState) => state.profile);
  // const { tenantData } = useSelector((state: DefaultRootState) => state.tenant);

  const {
    location: { pathname },
  } = useSelector((state: DefaultRootState) => state.router);

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

  // 是否放大查看
  const [visible, setVisible] = useState(false);
  // 是否展示下钻分析的抽屉
  const [drilldownVisible, setDrilldownVisible] = useState(false);
  // 是否展示下钻分析的入口提示
  const [drilldownTooltipVisible, setDrilldownTooltipVisible] = useState(false);

  // 访问且关闭过下钻入口提示的用户 ID 列表，在列表中的用户进入支持下钻的页面时，不展示监控下钻入口
  const drilldownTooltipUserList = jsonParse(
    localStorage.getItem('__OCP_DRILLDOWN_TOOLTIP_USER_LIST__') || '[]',
    []
  ) as number[];

  useEffect(() => {
    if (!isNullValue(userData.id)) {
      // 首次查看监控下钻的用户才展示第一个监控卡片的入口提示
      if (
        drilldownScope &&
        drilldownable &&
        !drilldownTooltipUserList.includes(userData.id) &&
        // 主机监控下钻，需要在第三个监控卡片进行入口提示，因为前两个监控卡片不支持下钻
        (drilldownScope === 'svr_ip' ? itemIndex === 2 : itemIndex === 0)
      ) {
        setDrilldownTooltipVisible(true);
      }
    }
  }, [userData.id]);

  const handleCloseDrilldownTooltip = () => {
    setDrilldownTooltipVisible(false);
    // 点击关闭后，将访问记录写到 localStorage，下次进入就不会展示了
    drilldownTooltipUserList.push(userData.id);
    localStorage.setItem(
      '__OCP_DRILLDOWN_TOOLTIP_USER_LIST__',
      JSON.stringify(drilldownTooltipUserList)
    );
  };

  const {
    name,
    description,
    metrics = [],
    chartConfig: metricGroupChartConfig,
    // 暂不开放监控下钻
    drilldownable = false,
    tooltipScroll,
  } = metricGroup;
  // 先用 metricGroup 的 chartConfig, 在使用 common 的 chartConfig
  const {
    meta = {},
    xAxis = {},
    ...restRealChartConfig
  } = metricGroupChartConfig || chartConfig || ({} as any);
  // 当前指标组是否只包含单个指标
  const isSingleMetric = metrics && metrics.length === 1;
  const unitList = uniq(metrics.map(item => item.unit)) || [];
  // 是否为双轴图 (包含两种单位)
  const isDualAxes = unitList.length === 2;

  let config = {
    type: 'Line',
    xField: 'timestamp',
    yField: 'value',
    seriesField: 'metric',
    // 关闭动画，避免实时图表更新不符合预期，以及提升大数据量下的渲染性能
    animation: false,
    meta: {
      timestamp: {
        formatter: (value: moment.MomentInput) => {
          return moment(value).format(DATE_TIME_FORMAT_DISPLAY);
        },
      },
      value: {
        formatter: (value: number) => {
          return formatValueForChart(chartData, value, metrics?.[0]?.unit as string);
        },
      },
      ...(isSingleMetric
        ? {
            metric: {
              // range 数据类型，且指标组只包含一个指标，使用指标组名代替指标名
              alias: name,
            },
          }
        : {}),
      ...meta,
    },
    xAxis: {
      type: 'time',
      label: {
        formatter: (value: moment.MomentInput) => {
          return moment(value, DATE_TIME_FORMAT_DISPLAY).format(TIME_FORMAT_WITHOUT_SECOND);
        },
      },
      ...xAxis,
    },
    ...restRealChartConfig,
  };

  // 双轴图配置
  if (isDualAxes) {
    config = {
      ...config,
      type: 'DualAxes',
      yField: ['value1', 'value2'],
      meta: {
        ...config.meta,
        value1: {
          formatter: (value: number) => {
            return formatValueForChart(chartData, value, unitList[0] as string, 'value1');
          },
        },
        value2: {
          formatter: (value: number) => {
            return formatValueForChart(chartData, value, unitList[1] as string, 'value2');
          },
        },
      },
      // 双轴图需要手动设置 yAxis 的最小值
      yAxis: {
        ...config.yAxis,
        value1: {
          min: 0,
          ...config.yAxis?.value1,
        },
        value2: {
          min: 0,
          ...config.yAxis?.value2,
        },
      },
      geometryOptions: [
        {
          geometry: 'line',
          // 双轴图支持多指标，需要设置 seriesField
          seriesField: 'metric',
        },
        {
          geometry: 'line',
          seriesField: 'metric',
        },
      ],
    };
  }

  const [range, setRange] = useState<Moment[]>([]);
  const [modalRange, setModalRange] = useState<Moment[]>([]);

  useEffect(() => {
    if (startTime && endTime) {
      setRange([moment(startTime), moment(endTime)]);
      setModalRange([moment(startTime), moment(endTime)]);
    }
  }, [startTime, endTime]);

  const { labels, groupBy } = getLabelsAndGroupBy({
    scope,
    app,
    clusterName,
    tenantName,
    zoneName,
    serverIp,
    serverPort,
    mount_point,
    mount_label,
    task_type,
    process,
    device,
    cpu,
  });

  const metricsString = metrics.map(item => item.key).join(',');

  function getOptions(type: 'card' | 'modal') {
    const propsRange = [moment(startTime), moment(endTime)];
    const realModalRange = isRealtime ? propsRange : modalRange;
    const realRange = isRealtime ? propsRange : range;

    // 弹窗里的时间
    const modalStartTime = realModalRange && realModalRange[0];
    const modalEndTime = realModalRange && realModalRange[1];
    // 使用的时间
    const realStartTime = type === 'card' ? realRange && realRange[0] : modalStartTime;
    const realEndTime = type === 'card' ? realRange && realRange[1] : modalEndTime;
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
        maxPoints,
      },
      deps: [
        realRequestStartTime,
        realRequestEndTime,
        metricsString,
        labels,
        groupBy,
        inViewPort,
        type === 'modal' ? visible : '',
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
          // 构造双轴图数据
          if (isDualAxes) {
            if (metricItem.unit === unitList[0]) {
              dataItem.value1 = item[key];
            } else if (metricItem.unit === unitList[1]) {
              dataItem.value2 = item[key];
            }
          } else {
            dataItem.value = item[key];
          }
          dataItem.timestamp = item.timestamp * 1000;
          chartData.push(dataItem);
        });
    });
    // 需要根据 value 从大到小排序，这样图例和 tooltip 才会从大到小展示，方便用户查看
    chartData = chartData.sort((a, b) => sortByNumber(b, a, 'value'));
    return chartData;
  }

  const options = getOptions('card');
  const modalOptions = getOptions('modal');

  // 获取监控数据
  const {
    data,
    run: queryMetric,
    loading,
  } = useRequestOfMonitor(MonitorController.queryMetric, {
    manual: true,
    isRealtime,
  });
  // 获取 Modal 中的监控数据
  const {
    data: modalData,
    run: queryModalMetric,
    loading: modalLoading,
  } = useRequestOfMonitor(MonitorController.queryMetric, {
    manual: true,
    isRealtime,
  });

  const chartData = getChartData(data);
  const modalChartData = getChartData(modalData);

  useEffect(() => {
    // 需要手动实现条件请求，因为 useRequest ready 配置仅在首次请求生效
    if (every(options.condition, item => !isNullValue(item))) {
      queryMetric(options.params);
    }
  }, options.deps);

  useEffect(() => {
    // 需要手动实现条件请求，因为 useRequest ready 配置仅在首次请求生效
    if (every(modalOptions.condition, item => !isNullValue(item))) {
      queryModalMetric(modalOptions.params);
    }
  }, modalOptions.deps);

  const title = (
    <ContentWithQuestion
      className={styles.title}
      style={titleStyle}
      content={name}
      tooltip={
        description && {
          placement: 'right',
          // 指标组只包含一个指标，则用指标组信息代替指标信息
          title: isSingleMetric ? (
            <div>
              {/* 用第一个指标的单位替代指标组的单位作展示 */}
              {`${description}`}
            </div>
          ) : (
            <div>
              <div>{description}</div>
              <ul>
                {metrics.map(metric => (
                  <li key={metric.key}>{`${metric.name}: ${metric.description}`}</li>
                ))}
              </ul>
            </div>
          ),
        }
      }
    />
  );

  const selects = [
    {
      name: formatMessage({
        id: 'ocp-express.component.MetricChart.Item.LastHour',
        defaultMessage: '最近一小时',
      }),
      range: () => [moment().subtract(1, 'hours'), moment()],
    },

    {
      name: formatMessage({
        id: 'ocp-express.component.MetricChart.Item.LastDay',
        defaultMessage: '最近一天',
      }),
      range: () => [moment().subtract(1, 'days'), moment()],
    },

    {
      name: formatMessage({
        id: 'ocp-express.component.MetricChart.Item.LastWeek',
        defaultMessage: '最近一周',
      }),
      range: () => [moment().subtract(1, 'weeks'), moment()],
    },
  ];

  return (
    // MyCard 不支持 ref 属性，需要在外面包一层 div
    <div ref={ref}>
      <MyCard
        // 实时模式下不展示 loading 态
        loading={loading && !isRealtime}
        title={title}
        className={`${styles.container} ${className}`}
        extra={
          <Space size={12}>
            {showRangePicker && !isRealtime && (
              <Ranger
                mode="mini"
                quickType="dropdown"
                allowClear={false}
                defaultValue={[moment(startTime), moment(endTime)]}
                format={DATE_TIME_FORMAT_DISPLAY}
                selects={selects}
                onChange={(value: Moment[]) => {
                  setRange(value);
                  // 同时更新弹窗中的时间范围，这样放大查看时间范围是同步的
                  setModalRange(value);
                }}
              />
            )}

            {
              // sql_rt: 租户监控 -> 性能与 SQL -> 响应时间，可关联跳转到 SQL 诊断 -> SQL 请求分析
              // sql_rt 指标卡片仅出现在租户监控，可从 tenantData 取值
              // metricGroup.key === 'sql_rt' && (
              //   <Tooltip
              //     title={formatMessage({
              //       id: 'ocp-express.component.MetricChart.Item.SqlRequestAnalysis',
              //       defaultMessage: 'SQL 请求分析',
              //     })}
              //   >
              //     <Icon
              //       component={SqlDigestSvg}
              //       onClick={() => {
              //         directTo(
              //           `/cluster/${tenantData.clusterId}/tenant/${
              //             tenantData.id
              //           }/sqlDiagnosis/digest?${stringify({
              //             startTime,
              //             endTime,
              //           })}`
              //         );
              //       }}
              //       className="pointable"
              //       style={{ color: 'rgba(0, 0, 0, 0.45)' }}
              //       data-aspm-click="ca29866.da14039"
              //       data-aspm-desc="监控卡片-SQL 请求分析跳转 icon"
              //       data-aspm-expo
              //     />
              //   </Tooltip>
              // )
            }
            {
              // 支持下钻、且下钻维度不为空才展示下钻分析的入口
              drilldownable && drilldownScope && (
                <Tooltip
                  overlayStyle={{ zIndex: 999 }}
                  visible={drilldownTooltipVisible}
                  placement="bottomRight"
                  color={token.colorPrimary}
                  arrowPointAtCenter={true}
                  title={
                    <div>
                      <div
                        style={{
                          display: 'flex',
                          justifyContent: 'space-between',
                          fontFamily: 'PingFangSC-Semibold',
                          marginBottom: 8,
                        }}
                      >
                        {formatMessage({
                          id: 'ocp-express.component.MetricChart.Item.ClickExperienceDrillDownAnalysis',
                          defaultMessage: '点击体验下钻分析！',
                        })}

                        <CloseOutlined
                          onClick={handleCloseDrilldownTooltip}
                          className="pointable"
                        />
                      </div>
                      <div>
                        {formatMessage({
                          id: 'ocp-express.component.MetricChart.Item.GetToKnowNow',
                          defaultMessage: '马上了解',
                        })}{' '}
                        <a
                          target="_blank"
                          style={{ color: token.colorTextLightSolid, textDecoration: 'underline' }}
                        >
                          {formatMessage({
                            id: 'ocp-express.component.MetricChart.Item.WhatIsDrillDownAnalysis',
                            defaultMessage: '什么是下钻分析',
                          })}
                        </a>
                      </div>
                    </div>
                  }
                >
                  <Tooltip
                    title={formatMessage({
                      id: 'ocp-express.component.MetricChart.Item.DrillDownAnalysis',
                      defaultMessage: '下钻分析',
                    })}
                  >
                    <Icon
                      component={DrilldownSvg}
                      onClick={() => {
                        setDrilldownVisible(true);
                        if (drilldownVisible) {
                          // 如果点击了下钻分析，则关闭提示
                          handleCloseDrilldownTooltip();
                        }
                      }}
                      className="pointable"
                      style={{ color: 'rgba(0, 0, 0, 0.45)' }}
                      data-aspm-click="ca48181.da30494"
                      data-aspm-desc="监控卡片-下钻"
                      data-aspm-expo
                      // 扩展参数
                      data-aspm-param={tracert.stringify({
                        // 监控下钻所处的页面路径
                        drilldownPathname: pathname,
                      })}
                    />
                  </Tooltip>
                </Tooltip>
              )
            }

            {showEnlargeEntry && (
              <FullscreenOutlined
                onClick={() => {
                  setVisible(true);
                }}
                className={styles.fullscreen}
              />
            )}
          </Space>
        }
        {...restProps}
      >
        {chartData.length > 0 ? (
          <Chart
            height={200}
            data={
              isDualAxes
                ? [
                    chartData.filter(item => !isNullValue(item.value1)),
                    chartData.filter(item => !isNullValue(item.value2)),
                  ]
                : chartData
            }
            tooltipScroll={
              tooltipScroll
                ? {
                    maxHeight: '180px',
                  }
                : false
            }
            {...config}
          />
        ) : (
          <Empty style={{ height: 180 }} imageStyle={{ marginTop: 52 }} />
        )}

        <Modal
          width={960}
          title={title}
          visible={visible}
          destroyOnClose={true}
          footer={false}
          onCancel={() => setVisible(false)}
        >
          {/* 实时模式下不展示 loading 态 */}
          <Spin spinning={modalLoading && !isRealtime}>
            <div
              style={{
                display: 'flex',
                justifyContent: 'flex-end',
                alignItems: 'center',
                marginBottom: 16,
              }}
            >
              {showModalRangePicker && !isRealtime && (
                <Ranger
                  mode="mini"
                  quickType="dropdown"
                  allowClear={false}
                  defaultValue={[moment(startTime), moment(endTime)]}
                  format={DATE_TIME_FORMAT_DISPLAY}
                  selects={selects}
                  onChange={(value: Moment[]) => {
                    setModalRange(value);
                  }}
                />
              )}
            </div>
            {modalChartData.length > 0 ? (
              <Chart
                height={400}
                data={
                  isDualAxes
                    ? [
                        modalChartData.filter(item => !isNullValue(item.value1)),
                        modalChartData.filter(item => !isNullValue(item.value2)),
                      ]
                    : modalChartData
                }
                tooltipScroll={
                  tooltipScroll
                    ? {
                        maxHeight: '350px',
                      }
                    : false
                }
                {...config}
              />
            ) : (
              <Empty style={{ height: 340 }} imageStyle={{ marginTop: 100 }} />
            )}
          </Spin>
        </Modal>
        <DrilldownDrawer
          visible={drilldownVisible}
          onCancel={() => {
            setDrilldownVisible(false);
          }}
          drilldownScope={drilldownScope}
          metricClass={metricClass}
          metricGroup={metricGroup}
          app={app}
          clusterName={clusterName}
          tenantName={tenantName}
          isRealtime={isRealtime}
          startTime={startTime}
          endTime={endTime}
          zoneName={zoneName}
          serverIp={serverIp}
          zoneNameList={zoneNameList}
          serverList={serverList}
          tenantList={tenantList}
        />
      </MyCard>
    </div>
  );
};

export default Item;
