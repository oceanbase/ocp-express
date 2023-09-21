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
import React, { useState } from 'react';
import { Empty, Space, Tooltip } from '@oceanbase/design';
import { find, isString } from 'lodash';
import moment from 'moment';
import { directTo, findByValue } from '@oceanbase/util';
import Icon, { FilterOutlined, LinkOutlined } from '@oceanbase/icons';
import { useRequest } from 'ahooks';
import * as MonitorController from '@/service/ocp-express/MonitorController';
import {
  RFC3339_DATE_TIME_FORMAT,
  DATE_TIME_FORMAT_DISPLAY,
  TIME_FORMAT_WITHOUT_SECOND,
} from '@/constant/datetime';
import { MONITOR_SCOPE_LIST } from '@/constant/monitor';
import { formatValueForChart } from '@/util';
import { getLabelsAndGroupBy, getTopChartData, getTopTargetList } from '@/util/monitor';
import { formatTextWithSpace } from '@/util/format';
import type { MyCardProps } from '@/component/MyCard';
import MyCard from '@/component/MyCard';
import type { ChartProps } from '@/component/Chart';
import Chart from '@/component/Chart';
import FilterDropdown from '@/component/FilterDropdown';
import type { OptionType, OptionValue } from '@/component/CheckboxPopover';
import CheckboxPopover from '@/component/CheckboxPopover';
import type { MonitorServer } from '@/component/MonitorSearch';
import { ReactComponent as DrilldownSvg } from '@/asset/drilldown.svg';
import type { MetricGroupWithChartConfig } from '../Item';
import SubDrilldownDrawer from './SubDrilldownDrawer';

export interface DrilldownChartProps extends Omit<MyCardProps, 'children'> {
  /* 图表是否可下钻 */
  drilldownable?: boolean;
  metricClass?: API.MetricClass;
  metricGroup: MetricGroupWithChartConfig;
  metric?: API.MetricMeta;
  /* 下钻图表的数据聚合维度，支持租户、主机和设备 */
  scope?: 'tenant_name' | 'svr_ip' | 'device';
  app?: Global.MonitorApp;
  clusterName?: string;
  tenantName?: string;
  isRealtime?: boolean;
  startTime?: string;
  endTime?: string;
  zoneName?: string;
  serverIp?: string;
  zoneNameList?: string[];
  serverList?: MonitorServer[];
  tenantList?: API.TenantInfo[];
  options?: OptionType &
  {
    clusterId: number;
    tenantId: number;
    hostId: number;
  }[];

  // 当前组件包含 FilterDropdown、Tooltip 和 CheckboxPopover 等弹窗层组件，并会在抽屉中使用到 (滚动时容器不是 body)，需要支持定制 getPopupContainer
  getPopupContainer?: (triggerNode: HTMLElement) => HTMLElement;
  title?: string;
}

const DrilldownChart: React.FC<DrilldownChartProps> = ({
  drilldownable = true,
  metricClass,
  metricGroup,
  metric,
  scope,
  app = 'OB',
  clusterName,
  tenantName,
  isRealtime,
  startTime,
  endTime,
  zoneName,
  serverIp,
  zoneNameList,
  serverList,
  tenantList,
  options,
  getPopupContainer,
  title,
  ...restProps
}) => {
  const [visible, setVisible] = useState(false);
  // 默认选中的对象列表，用于 CheckboxPopover 组件内部的重置功能
  const [defaultSelectedList, setDefaultSelectedList] = useState<OptionValue[]>([]);
  // 选中的对象列表
  const [selectedList, setSelectedList] = useState<OptionValue[]>([]);
  const scopeItem = findByValue(MONITOR_SCOPE_LIST, scope);
  // 当 scope 为 svr_ip 时: scopeLabel 为 OBServer，scope.label 为主机，不同场景需要使用不同的 label
  const scopeLabel = scope === 'svr_ip' ? 'OBServer' : scopeItem.label;
  const { labels, groupBy } = getLabelsAndGroupBy({
    scope,
    app,
    clusterName,
    tenantName,
    zoneName,
    serverIp,
  });

  const metricKey = metric?.key || '';
  const { data, loading } = useRequest(
    () =>
      MonitorController.queryMetricTop({
        startTime: moment(startTime).format(RFC3339_DATE_TIME_FORMAT),
        endTime: moment(endTime).format(RFC3339_DATE_TIME_FORMAT),
        metrics: metricKey,
        labels,
        groupBy,
      }),

    {
      refreshDeps: [startTime, endTime, metricKey, labels, groupBy],
      onSuccess: res => {
        if (res.successful) {
          const newDefaultSelectedList = getTopTargetList({
            dataList: res.data?.contents || [],
            groupBy,
            metricKeys: [metricKey],
            // 如果聚合维度是 device，则不限制 top，展示全部对象
            limit: scope === 'device' ? undefined : 10,
          })
            // TODO: 由于监控接口返回的数据不符合预期，前端先临时根据 OBServer 和租户列表对监控对象做筛选
            .filter(item =>
              // 如果聚合维度是 device，则不进行筛选
              scope === 'device'
                ? true
                : scope === 'svr_ip'
                  ? serverList?.map(server => server.ip).includes(item)
                  : tenantList?.map(tenant => tenant.name).includes(item)
            );
          setDefaultSelectedList(newDefaultSelectedList);
          setSelectedList(newDefaultSelectedList);
        }
      },
    }
  );

  // 根据监控对象筛选监控数据
  const chartData = getTopChartData({
    dataList: data?.data?.contents || [],
    groupBy,
    metricKeys: [metricKey],
    clusterName,
  }).filter(item => selectedList.includes(item.target));
  const chartConfig: ChartProps = {
    data: chartData,
    type: 'Line',
    xField: 'timestamp',
    yField: 'value',
    seriesField: 'target',
    // 关闭图表动画，避免受到外层监控实时刷新的影响
    animation: false,
    meta: {
      timestamp: {
        formatter: (value: number) => {
          return moment(value).format(DATE_TIME_FORMAT_DISPLAY);
        },
      },

      value: {
        formatter: (value: number) => {
          return formatValueForChart(chartData, value, metric?.unit);
        },
      },
    },

    xAxis: {
      type: 'time',
      tickCount: isRealtime ? 1 : 4,
      label: {
        formatter: (value: number) => {
          return moment(value, DATE_TIME_FORMAT_DISPLAY).format(TIME_FORMAT_WITHOUT_SECOND);
        },
      },
    },

    yAxis: {
      nice: true,
      tickCount: 3,
    },

    legend: {
      position: 'top-left',
      // legend 过多、过长时进行翻页处理
      flipPage: true,
    },

    interactions: [
      {
        type: 'brush-x',
      },
    ],
  };

  return (
    <MyCard
      loading={loading && !isRealtime}
      hoverable={false}
      className="card-without-padding"
      style={{
        boxShadow: 'none',
      }}
      // title 可能为 ReactNode，为了避免格式化报错，增加 isString 判断
      title={isString(title) ? formatTextWithSpace(title) : title}
      extra={
        <Space size={16}>
          {/* 目前只支持跳转到租户和主机监控 */}
          {(scope === 'tenant_name' || scope === 'svr_ip') && (
            <FilterDropdown
              mode="single"
              placement="bottomRight"
              getPopupContainer={getPopupContainer}
              filters={
                selectedList.map(item => ({
                  value: item,
                  label: item,
                })) || []
              }
              onClick={value => {
                const option = find(options, item => item.value === value);
                if (scope === 'tenant_name') {
                  directTo(`/cluster/${option?.clusterId}/tenant/${option?.tenantId}/monitor`);
                } else if (scope === 'svr_ip') {
                  directTo(`/host/${option?.hostId}?tab=monitor`);
                }
              }}
              inputProps={{
                placeholder: formatMessage(
                  {
                    id: 'ocp-express.MetricChart.DrilldownDrawer.DrilldownChart.SearchForScopeitemlabel',
                    defaultMessage: '搜索{scopeItemLabel}',
                  },
                  { scopeItemLabel: scopeItem.label }
                ),
              }}
            >
              <a>
                <Space size={4}>
                  <LinkOutlined />
                  {formatMessage(
                    {
                      id: 'ocp-express.MetricChart.DrilldownDrawer.DrilldownChart.GoToScopeitemlabelForPerformance',
                      defaultMessage: '前往{scopeItemLabel}性能监控',
                    },
                    { scopeItemLabel: scopeItem.label }
                  )}
                </Space>
              </a>
            </FilterDropdown>
          )}

          {drilldownable && (
            <Tooltip
              title={formatMessage(
                {
                  id: 'ocp-express.MetricChart.DrilldownDrawer.DrilldownChart.DrillDownAnalysisOfScopeitemlabel',
                  defaultMessage: '对{scopeItemLabel}进行下钻分析',
                },
                { scopeItemLabel: scopeLabel }
              )}
              getPopupContainer={getPopupContainer}
            >
              <Icon
                component={DrilldownSvg}
                onClick={() => {
                  setVisible(true);
                }}
                style={{ color: 'rgba(0, 0, 0, 0.45)' }}
                className="pointable"
              />
            </Tooltip>
          )}

          {options?.length > 0 && (
            <CheckboxPopover
              placement="bottomRight"
              trigger={['click']}
              arrowPointAtCenter={true}
              getPopupContainer={getPopupContainer}
              title={formatMessage(
                {
                  id: 'ocp-express.MetricChart.DrilldownDrawer.DrilldownChart.SelectScopeitemlabel',
                  defaultMessage: '选择{scopeItemLabel}',
                },
                { scopeItemLabel: scopeLabel }
              )}
              options={options}
              defaultValue={defaultSelectedList}
              value={selectedList}
              onChange={value => {
                setSelectedList(value);
              }}
              maxSelectCount={10}
              maxSelectCountLabel={formatMessage(
                {
                  id: 'ocp-express.MetricChart.DrilldownDrawer.DrilldownChart.YouCanSelectUpTo',
                  defaultMessage: '最多可选择 10 个{scopeItemLabel}',
                },
                { scopeItemLabel: scopeLabel }
              )}
            >
              <Tooltip
                title={formatMessage(
                  {
                    id: 'ocp-express.MetricChart.DrilldownDrawer.DrilldownChart.SelectScopeitemlabel',
                    defaultMessage: '选择{scopeItemLabel}',
                  },
                  { scopeItemLabel: scopeLabel }
                )}
                getPopupContainer={getPopupContainer}
              >
                <FilterOutlined style={{ color: 'rgba(0, 0, 0, 0.45)' }} className="pointable" />
              </Tooltip>
            </CheckboxPopover>
          )}
        </Space>
      }
      {...restProps}
    >
      {chartData.length > 0 ? (
        <Chart
          // 由于下钻图表包含 legend，下钻图表的高度需要增加 36px，以保证图表内容区高度一致，这样纵轴的标尺与参考图表的标尺也会一致，方便用户比对
          height={186 + 36}
          tooltipScroll={
            // 如果聚合维度是 device，则 Top 对象数可能较多，允许鼠标进入并滚动查看 tooltip
            scope === 'device'
              ? {
                maxHeight: '200px',
              }
              : false
          }
          {...chartConfig}
        />
      ) : (
        <Empty style={{ height: 160 + 18 }} imageStyle={{ marginTop: 64 }} />
      )}

      <SubDrilldownDrawer
        visible={visible}
        onCancel={() => {
          setVisible(false);
        }}
        // 下钻维度和当前图表的聚合维度一致
        drilldownScope={scope}
        metricGroup={metricGroup}
        metric={metric}
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
  );
};

export default DrilldownChart;
