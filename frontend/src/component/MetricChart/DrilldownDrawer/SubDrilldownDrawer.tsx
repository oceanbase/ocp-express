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
import React, { useState, useEffect } from 'react';
import { Alert, Card, Col, Empty, Menu, Row, theme } from '@oceanbase/design';
import { flatten } from 'lodash';
import moment from 'moment';
import { useRequest } from 'ahooks';
import * as MonitorController from '@/service/ocp-express/MonitorController';
import {
  RFC3339_DATE_TIME_FORMAT,
  DATE_TIME_FORMAT_DISPLAY,
  TIME_FORMAT_WITHOUT_SECOND,
} from '@/constant/datetime';
import { formatValueForChart, getPopupContainer } from '@/util';
import { getLabelsAndGroupBy } from '@/util/monitor';
import { formatTextWithSpace } from '@/util/format';
import type { MyDrawerProps } from '@/component/MyDrawer';
import MyDrawer from '@/component/MyDrawer';
import MyCard from '@/component/MyCard';
import type { ChartProps } from '@/component/Chart';
import Chart from '@/component/Chart';
import type { MonitorServer, MonitorSearchQueryData } from '@/component/MonitorSearch';
import MonitorSearch from '@/component/MonitorSearch';
import type { MetricGroupWithChartConfig } from '../Item';
import DrilldownChart from './DrilldownChart';

export interface SubDrilldownDrawerProps extends MyDrawerProps {
  metricGroup?: MetricGroupWithChartConfig;
  metric?: API.MetricMeta;
  /* 二级下钻维度，只支持租户和 OBServer */
  drilldownScope?: 'tenant_name' | 'svr_ip';
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
}

/* 按对象 (租户、OBServer) 进行二级下钻的抽屉组件 */
const SubDrilldownDrawer: React.FC<SubDrilldownDrawerProps> = ({
  visible,
  drilldownScope,
  metricGroup,
  metric,
  app = 'OB',
  clusterName,
  tenantName,
  isRealtime: defaultIsRealtime,
  startTime: defaultStartTime,
  endTime: defaultEndTime,
  zoneName: defaultZoneName,
  serverIp: defaultServerIp,
  zoneNameList,
  serverList,
  tenantList,
  ...restProps
}) => {
  const { token } = theme.useToken();
  // 默认的 MonitorSearch 查询参数
  const defaultSearchValues = {
    isRealtime: defaultIsRealtime,
    startTime: defaultStartTime,
    endTime: defaultEndTime,
    zoneName: defaultZoneName,
    serverIp: defaultServerIp,
  };

  // 最新的 MonitorSearch 查询参数
  const [searchValues, setSearchValues] = useState<MonitorSearchQueryData>(defaultSearchValues);
  const { isRealtime, startTime, endTime, zoneName, serverIp } = searchValues;

  // 只需要根据 zoneName 进行筛选即可，因为 serverIp 和 zone 是联动的，不需要根据 serverIp 来反筛选 zoneNameList
  const realZoneNameList = zoneNameList?.filter(item => !zoneName || item === zoneName);
  const realServerList = serverList?.filter(
    item => (!zoneName || item.zoneName === zoneName) && (!serverIp || item.ip === serverIp)
  );

  const realTenantList = tenantList?.filter(
    item =>
      (!zoneName || item.zones?.map(zone => zone.name).includes(zoneName)) &&
      (!serverIp ||
        flatten(item.zones?.map(zone => zone.units?.map(unit => unit.serverIp) || [])).includes(
          serverIp
        ))
  );

  // 默认选中第一个租户或 OBServer
  const defaultSelectedTenantName = realTenantList?.[0]?.name;
  const defaultSelectedServerIp = realServerList?.[0]?.ip;
  const [selectedTenantName, setSelectedTenantName] = useState(defaultSelectedTenantName);
  const [selectedServerIp, setSelectedServerIp] = useState(defaultSelectedServerIp);
  const isTenant = drilldownScope === 'tenant_name';
  // 下钻维度的展示文本
  const dilldownScopeLabel = isTenant
    ? formatMessage({
        id: 'ocp-express.MetricChart.DrilldownDrawer.SubDrilldownDrawer.Tenant',
        defaultMessage: '租户',
      })
    : 'OBServer';
  // 聚合维度的展示文本
  const scopeLabel = isTenant
    ? 'OBServer'
    : formatMessage({
        id: 'ocp-express.MetricChart.DrilldownDrawer.SubDrilldownDrawer.Tenant',
        defaultMessage: '租户',
      });
  const selectedTarget = `${isTenant ? selectedTenantName : selectedServerIp}`;

  // 是否存在多个细分指标
  const isMultipleMetric = (metricGroup?.metrics?.length || 0) > 1;
  // 指标组 + 细分指标组成的指标名称
  const metricLabel = `${metricGroup?.name}${isMultipleMetric ? `_${metric?.name}` : ''}`;

  const realTenantName = isTenant ? selectedTenantName : tenantName;
  const realServerIp = drilldownScope === 'svr_ip' ? selectedServerIp : serverIp;
  const { labels, groupBy } = getLabelsAndGroupBy({
    app,
    clusterName,
    tenantName: realTenantName,
    zoneName,
    serverIp: realServerIp,
  });

  const { data, loading, refresh } = useRequest(
    () =>
      MonitorController.queryMetric({
        startTime: moment(startTime).format(RFC3339_DATE_TIME_FORMAT),
        endTime: moment(endTime).format(RFC3339_DATE_TIME_FORMAT),
        metrics: metric?.key,
        labels,
        groupBy,
      }),

    {
      // ready 仅首次生效
      ready: visible,
      refreshDeps: [startTime, endTime, metric?.key, labels, groupBy],
    }
  );

  useEffect(() => {
    if (visible) {
      // 打开弹窗时重新请求数据
      refresh();
    } else {
      // 关闭抽屉时重置选中菜单
      setSelectedTenantName(defaultSelectedTenantName);
      setSelectedServerIp(defaultSelectedServerIp);
    }
  }, [visible]);

  useEffect(() => {
    // 选中的 Zone 和 OBServer 改变后重置选中菜单
    setSelectedTenantName(defaultSelectedTenantName);
    setSelectedServerIp(defaultSelectedServerIp);
  }, [zoneName, serverIp]);

  const chartData = (data?.data?.contents || []).map(item => ({
    ...item,
    timestamp: item.timestamp * 1000,
    value: item[metric?.key || ''],
  }));

  const chartConfig: ChartProps = {
    data: chartData,
    type: 'Line',
    xField: 'timestamp',
    yField: 'value',
    // 关闭图表动画，避免受到外层监控实时刷新的影响
    animation: false,
    meta: {
      timestamp: {
        formatter: (value: moment.MomentInput) => {
          return moment(value).format(DATE_TIME_FORMAT_DISPLAY);
        },
      },

      value: {
        // 指标组只包含一个指标，则使用指标组名代替指标名
        alias: isMultipleMetric ? metric?.name : metricGroup?.name,
        formatter: (value: number) => {
          return formatValueForChart(chartData, value, metric?.unit);
        },
      },
    },

    xAxis: {
      type: 'time',
      tickCount: isRealtime ? 1 : 4,
      label: {
        formatter: (value: moment.MomentInput) => {
          return moment(value, DATE_TIME_FORMAT_DISPLAY).format(TIME_FORMAT_WITHOUT_SECOND);
        },
      },
    },

    yAxis: {
      nice: true,
      tickCount: 3,
    },

    interactions: [
      {
        type: 'brush-x',
      },
    ],
  };

  return (
    <MyDrawer
      width={1124}
      title={formatTextWithSpace(
        formatMessage(
          {
            id: 'ocp-express.MetricChart.DrilldownDrawer.SubDrilldownDrawer.DilldownscopelabelDrillDownAnalysisOf',
            defaultMessage: '{dilldownScopeLabel}的{metricLabel}下钻分析',
          },
          { dilldownScopeLabel: dilldownScopeLabel, metricLabel: metricLabel }
        )
      )}
      visible={visible}
      destroyOnClose={true}
      footer={false}
      bodyStyle={{
        backgroundColor: token.colorBgLayout,
        height: '100%',
      }}
      {...restProps}
    >
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <MonitorSearch
            location={{
              query: defaultSearchValues,
            }}
            onSearch={values => {
              setSearchValues(values);
            }}
            // 这里 zoneNameList 和 serverList 用于查询的选项，不需要经过筛选，而是使用原始数据
            zoneNameList={zoneNameList}
            serverList={serverList}
          />
        </Col>
        <Col span={24}>
          <Card bodyStyle={{ padding: 0 }}>
            <Row gutter={0}>
              <Col span={6}>
                <div
                  style={{
                    fontSize: 16,
                    fontFamily: 'PingFangSC-Medium',
                    padding: '20px 24px',
                    borderRight: `1px solid ${token.colorBorder}`,
                  }}
                >
                  {dilldownScopeLabel}
                </div>
                <Menu
                  mode="inline"
                  selectedKeys={isTenant ? [selectedTenantName || ''] : [selectedServerIp || '']}
                  style={{
                    // 菜单高度需要撑满整个卡片，以保证左侧菜单导航与右侧图表等高
                    height: 'calc(100% - 65px)',
                    color: token.colorTextSecondary,
                  }}
                >
                  {isTenant
                    ? realTenantList?.map(item => (
                        <Menu.Item
                          key={item.name}
                          onClick={() => {
                            setSelectedTenantName(item.name);
                          }}
                          style={{ padding: '0 24px' }}
                        >
                          {item.name}
                        </Menu.Item>
                      ))
                    : realServerList?.map(item => (
                        <Menu.Item
                          key={item.ip}
                          onClick={() => {
                            setSelectedServerIp(item.ip);
                          }}
                          style={{ padding: '0 24px' }}
                        >
                          {item.ip}
                        </Menu.Item>
                      ))}
                </Menu>
              </Col>
              <Col span={18}>
                <div>
                  <Alert
                    type="info"
                    showIcon={true}
                    closable={true}
                    message={formatMessage(
                      {
                        id: 'ocp-express.MetricChart.DrilldownDrawer.SubDrilldownDrawer.ByDefaultDilldownscopelabelOfThe',
                        defaultMessage:
                          '默认展示此{dilldownScopeLabel} Top10 的{scopeLabel}，并支持自定义筛选其他{scopeLabel}',
                      },
                      {
                        dilldownScopeLabel: dilldownScopeLabel,
                        scopeLabel: scopeLabel,
                        scopeLabel: scopeLabel,
                      }
                    )}
                    style={{ color: token.colorTextSecondary, margin: '24px 24px 1px 24px' }}
                  />
                  <Card bordered={false}>
                    <Card.Grid
                      hoverable={false}
                      style={{
                        width: '100%',
                        paddingBottom: 12,
                        // 去掉 boxShadow 效果，避免遮挡上面的 Alert
                        boxShadow: 'none',
                      }}
                    >
                      <MyCard
                        loading={loading && !isRealtime}
                        title={formatTextWithSpace(
                          formatMessage(
                            {
                              id: 'ocp-express.MetricChart.DrilldownDrawer.SubDrilldownDrawer.SelectedtargetOfMetriclabel',
                              defaultMessage: '{selectedTarget} 的{metricLabel}',
                            },
                            { selectedTarget: selectedTarget, metricLabel: metricLabel }
                          )
                        )}
                        className="card-without-padding"
                        style={{
                          boxShadow: 'none',
                        }}
                      >
                        {chartData.length > 0 ? (
                          <Chart height={186} {...chartConfig} />
                        ) : (
                          <Empty style={{ height: 160 }} imageStyle={{ marginTop: 46 }} />
                        )}
                      </MyCard>
                    </Card.Grid>
                    {isTenant ? (
                      <Card.Grid hoverable={false} style={{ width: '100%' }}>
                        <DrilldownChart
                          drilldownable={false}
                          getPopupContainer={getPopupContainer}
                          title={formatMessage(
                            {
                              id: 'ocp-express.MetricChart.DrilldownDrawer.SubDrilldownDrawer.SelectedtargetOnDifferentHostsMetriclabel',
                              defaultMessage: '{selectedTarget} 在不同 OBServer 上的{metricLabel}',
                            },
                            { selectedTarget: selectedTarget, metricLabel: metricLabel }
                          )}
                          metric={metric}
                          scope="svr_ip"
                          clusterName={clusterName}
                          {...searchValues}
                          // 当下钻维度为租户时，需要使用实际的 realTenantName 覆盖从查询表单中获取的 serverIp 值
                          tenantName={realTenantName}
                          zoneNameList={realZoneNameList}
                          serverList={realServerList}
                          tenantList={realTenantList}
                          options={realServerList?.map(item => ({
                            value: item.ip,
                            label: item.ip,
                            group: item.zoneName,
                            hostId: item.hostId,
                          }))}
                        />
                      </Card.Grid>
                    ) : (
                      <Card.Grid hoverable={false} style={{ width: '100%' }}>
                        <DrilldownChart
                          drilldownable={false}
                          getPopupContainer={getPopupContainer}
                          title={formatMessage(
                            {
                              id: 'ocp-express.MetricChart.DrilldownDrawer.SubDrilldownDrawer.SelectedtargetOnDifferentTenantsMetriclabel',
                              defaultMessage: '{selectedTarget} 在不同租户上的{metricLabel}',
                            },
                            { selectedTarget: selectedTarget, metricLabel: metricLabel }
                          )}
                          metric={metric}
                          scope="tenant_name"
                          app={app}
                          clusterName={clusterName}
                          tenantName={realTenantName}
                          {...searchValues}
                          // 当下钻维度为 OBServer 时，需要使用实际的 realServerIp 覆盖从查询表单中获取的 serverIp 值
                          serverIp={realServerIp}
                          zoneNameList={realZoneNameList}
                          serverList={realServerList}
                          tenantList={realTenantList}
                          options={realTenantList?.map(item => ({
                            value: item.name,
                            label: item.name,
                            clusterId: item.clusterId,
                            tenantId: item.id,
                          }))}
                        />
                      </Card.Grid>
                    )}
                  </Card>
                </div>
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>
    </MyDrawer>
  );
};

export default SubDrilldownDrawer;
