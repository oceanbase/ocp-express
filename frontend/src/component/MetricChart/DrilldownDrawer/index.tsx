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
import React, { useState, useEffect } from 'react';
import { Alert, Card, Col, Empty, Menu, Row, Space, theme } from '@oceanbase/design';
import { LinkOutlined } from '@oceanbase/icons';
import { directTo, findByValue } from '@oceanbase/util';
import { find, flatten } from 'lodash';
import moment from 'moment';
import { stringify } from 'query-string';
import { useRequest } from 'ahooks';
import * as MonitorController from '@/service/ocp-express/MonitorController';
import {
  RFC3339_DATE_TIME_FORMAT,
  DATE_TIME_FORMAT_DISPLAY,
  TIME_FORMAT_WITHOUT_SECOND,
  DATE_TIME_FORMAT,
} from '@/constant/datetime';
import { MONITOR_SCOPE_LIST } from '@/constant/monitor';
import { formatValueForChart, getPopupContainer } from '@/util';
import { getLabelsAndGroupBy } from '@/util/monitor';
import { formatTextWithSpace } from '@/util/format';
import type { MyDrawerProps } from '@/component/MyDrawer';
import MyDrawer from '@/component/MyDrawer';
import MyCard from '@/component/MyCard';
import type { ChartProps } from '@/component/Chart';
import Chart from '@/component/Chart';
import type { MonitorServer } from '@/component/MonitorSearch';
import type { MonitorSearchQueryData } from '@/component/MonitorSearch';
import MonitorSearch from '@/component/MonitorSearch';
import HostMonitorSearch from '@/component/HostMonitorSearch';
import type { MetricGroupWithChartConfig } from '../Item';
import DrilldownChart from './DrilldownChart';

export interface DrilldownDrawerProps extends MyDrawerProps {
  /* 一级下钻维度，为空时不展示下钻入口，只支持集群、租户和主机 */
  drilldownScope?: 'ob_cluster_id' | 'tenant_name' | 'svr_ip' | 'device';
  metricClass?: API.MetricClass;
  metricGroup: MetricGroupWithChartConfig;
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

/* 按指标进行一级下钻的抽屉组件 */
const DrilldownDrawer: React.FC<DrilldownDrawerProps> = ({
  visible,
  drilldownScope,
  metricClass,
  metricGroup,
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

  const { tenantData } = useSelector((state: DefaultRootState) => state.tenant);

  const realTenantList = tenantList?.filter(
    item =>
      (!zoneName || item.zones?.map(zone => zone.name).includes(zoneName)) &&
      (!serverIp ||
        flatten(item.zones?.map(zone => zone.units?.map(unit => unit.serverIp) || [])).includes(
          serverIp
        ))
  );

  const drilldownScopeLabel = findByValue(MONITOR_SCOPE_LIST, drilldownScope).label;
  const drawerTitleMap = {
    ob_cluster_id: formatMessage(
      {
        id: 'ocp-express.MetricChart.DrilldownDrawer.ClusternameDrillDownAnalysisOfClusterMetricgroupname',
        defaultMessage: '集群 {clusterName} 的{metricGroupName}下钻分析',
      },
      { clusterName: clusterName, metricGroupName: metricGroup.name }
    ),
    tenant_name: formatMessage(
      {
        id: 'ocp-express.MetricChart.DrilldownDrawer.DrillDownAnalysisOfTenant',
        defaultMessage: '租户 {tenantName} 的{metricGroupName}下钻分析',
      },

      { tenantName: tenantName, metricGroupName: metricGroup.name }
    ),

    svr_ip: formatMessage(
      {
        id: 'ocp-express.MetricChart.DrilldownDrawer.DrillDownAnalysisOfServerip',
        defaultMessage: '主机 {serverIp} 的{metricGroupName}下钻分析',
      },

      { serverIp: serverIp, metricGroupName: metricGroup.name }
    ),
  };

  const alertMessageMap = {
    ob_cluster_id: formatMessage({
      id: 'ocp-express.MetricChart.DrilldownDrawer.ByDefaultTheTopTenants',
      defaultMessage: '默认展示此集群 Top10 的租户和 OBServer，并支持自定义筛选其他租户和 OBServer',
    }),

    tenant_name: formatMessage({
      id: 'ocp-express.MetricChart.DrilldownDrawer.ByDefaultTheTopHosts',
      defaultMessage: '默认展示此租户 Top10 的 OBServer，并支持自定义筛选其他 OBServer',
    }),
  };

  // 是否存在多个细分指标
  const isMultipleMetric = (metricGroup.metrics?.length || 0) > 1;
  // 默认选中第一个细分指标
  const defaultMetricKey = metricGroup.metrics?.[0]?.key;
  const [metricKey, setMetricKey] = useState(defaultMetricKey);
  const metricItem = find(metricGroup.metrics, item => item.key === metricKey);
  // 指标组 + 细分指标组成的指标名称
  const metricLabel = `${metricGroup.name}${isMultipleMetric ? `_${metricItem?.name}` : ''}`;
  const { labels, groupBy } = getLabelsAndGroupBy({
    app,
    clusterName,
    tenantName,
    zoneName,
    serverIp,
  });

  const { data, loading, refresh } = useRequest(
    () =>
      MonitorController.queryMetric({
        startTime: moment(startTime).format(RFC3339_DATE_TIME_FORMAT),
        endTime: moment(endTime).format(RFC3339_DATE_TIME_FORMAT),
        metrics: metricKey,
        labels,
        groupBy,
      }),

    {
      // ready 仅首次生效
      ready: visible,
      refreshDeps: [startTime, endTime, metricKey, labels, groupBy],
    }
  );

  // 打开弹窗时重新请求数据
  useEffect(() => {
    if (visible) {
      refresh();
    } else {
      // 关闭抽屉时重置选中菜单
      setMetricKey(defaultMetricKey);
    }
  }, [visible]);

  const chartData = (data?.data?.contents || []).map(item => ({
    ...item,
    timestamp: item.timestamp * 1000,
    value: item[metricKey || ''],
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
        alias: isMultipleMetric ? metricItem?.name : metricGroup.name,
        formatter: (value: number) => {
          return formatValueForChart(chartData, value, metricItem?.unit);
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

  const sqlDirlldownList = [
    {
      groupKey: 'sql_count', // QPS
      name: 'all',
      title: formatMessage({
        id: 'ocp-express.MetricChart.DrilldownDrawer.QueryTheMostFrequentlyRequested',
        defaultMessage: '查找请求次数最多的 SQL',
      }),

      sorter: {
        order: 'descend',
        field: 'executions', // 执行次数
        // 高亮
        highlight: true,
      },
    },

    {
      groupKey: 'sql_rt', // 响应时间
      name: 'all',
      title: formatMessage({
        id: 'ocp-express.MetricChart.DrilldownDrawer.FindTheSqlStatementWith',
        defaultMessage: '查找平均响应时间最长的 SQL',
      }),

      sorter: {
        order: 'descend',
        field: 'avgElapsedTime', // 平均响应时间
        highlight: true,
      },
    },

    {
      groupKey: 'sql_plan', // SQL 执行计划类别
      name: 'remote',
      title: formatMessage({
        id: 'ocp-express.MetricChart.DrilldownDrawer.FindTheSqlStatementsWith',
        defaultMessage: '查找远程计划最多的 SQL',
      }),

      sorter: {
        order: 'descend',
        field: '@executions * @remotePlanPercentage', // 执行计划 * 远程计划
        highlight: true,
      },

      // 自定义列
      customColumnName: formatMessage({
        id: 'ocp-express.MetricChart.DrilldownDrawer.ExecutionTimesRemotePlan',
        defaultMessage: '执行次数 * 远程计划占比',
      }),

      customColumns: ['@executions * @remotePlanPercentage'],
    },

    {
      groupKey: 'wait_event', // 等待事件
      name: 'all',
      title: formatMessage({
        id: 'ocp-express.MetricChart.DrilldownDrawer.FindTheSqlStatementThat',
        defaultMessage: '查找等待事件次数最多的 SQL',
      }),

      sorter: {
        order: 'descend',
        field: 'avgWaitCount', // 等待次数
        highlight: true,
      },
    },

    {
      groupKey: 'wait_event_rt', // 等待事件耗时
      name: 'all',
      title: formatMessage({
        id: 'ocp-express.MetricChart.DrilldownDrawer.FindTheSqlStatementThat.1',
        defaultMessage: '查找等待事件耗时最长的 SQL',
      }),

      sorter: {
        order: 'descend',
        field: 'sumWaitTime', // 总等待时间
        highlight: true,
      },
    },
  ];

  const sqlDirlldownItem = find(sqlDirlldownList, item => item.groupKey === metricGroup.key);
  const { userData } = useSelector((state: DefaultRootState) => state.profile);

  return (
    <MyDrawer
      width={1124}
      title={drilldownScope && formatTextWithSpace(drawerTitleMap[drilldownScope])}
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
        {/* OB 监控展示 MonitorSearch */}
        {app === 'OB' && (
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
        )}

        <Col span={24}>
          <Card
            // Host 监控展示 HostMonitorSearch
            extra={
              app === 'HOST' && (
                <HostMonitorSearch
                  onChange={values => {
                    setSearchValues({
                      ...searchValues,
                      isRealtime: values.realtime,
                      startTime:
                        values.range &&
                        values.range[0] &&
                        moment(values.range[0]).format(DATE_TIME_FORMAT),
                      endTime:
                        values.range &&
                        values.range[1] &&
                        moment(values.range[1]).format(DATE_TIME_FORMAT),
                    });
                  }}
                />
              )
            }
            bodyStyle={{ padding: 0 }}
          >
            <Row gutter={0}>
              {isMultipleMetric && (
                <Col span={6}>
                  <div
                    style={{
                      fontSize: 16,
                      fontFamily: 'PingFangSC-Medium',
                      padding: '20px 24px',
                      borderRight: `1px solid ${token.colorBorder}`,
                    }}
                  >
                    {formatMessage(
                      {
                        id: 'ocp-express.MetricChart.DrilldownDrawer.MetricgroupnameSubdivisionMetrics',
                        defaultMessage: '{metricGroupName}细分指标',
                      },

                      { metricGroupName: metricGroup.name }
                    )}
                  </div>
                  <Menu
                    mode="inline"
                    selectedKeys={metricKey ? [metricKey] : []}
                    onClick={({ key }) => {
                      setMetricKey(key);
                    }}
                    style={{
                      // 菜单高度需要撑满整个卡片，以保证左侧菜单导航与右侧图表等高
                      height: 'calc(100% - 65px)',
                      color: token.colorTextSecondary,
                    }}
                  >
                    {metricGroup.metrics?.map(item => (
                      <Menu.Item key={item.key} style={{ padding: '0 24px' }}>
                        {item.name}
                      </Menu.Item>
                    ))}
                  </Menu>
                </Col>
              )}

              <Col span={isMultipleMetric ? 18 : 24}>
                <div
                  style={{
                    // 为了保证上方的 border 能够正常展示，需要设置与下方 Card 的间距
                    marginBottom: 2,
                  }}
                >
                  {alertMessageMap[drilldownScope] && (
                    <Alert
                      type="info"
                      showIcon={true}
                      closable={true}
                      message={alertMessageMap[drilldownScope]}
                      style={{ color: token.colorTextSecondary, margin: '24px 24px 0px 24px' }}
                    />
                  )}
                </div>
                <div>
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
                              id: 'ocp-express.MetricChart.DrilldownDrawer.DrilldownscopelabelOfMetriclabel',
                              defaultMessage: '{drilldownScopeLabel}的{metricLabel}',
                            },

                            { drilldownScopeLabel: drilldownScopeLabel, metricLabel: metricLabel }
                          )
                        )}
                        className="card-without-padding"
                        style={{
                          boxShadow: 'none',
                        }}
                        extra={
                          sqlDirlldownItem?.name === metricItem?.name &&
                          drilldownScope === 'tenant_name' ? (
                            <a
                              onClick={() => {
                                if (sqlDirlldownItem?.customColumnName) {
                                  // 存在自定义列的情况下，需要将自定义列内容缓存到本地
                                  localStorage.setItem(
                                    `__OCP_USER_${userData.id}_SQL_TOPSQL_CUSTOM_COLUMNS__`,
                                    JSON.stringify(sqlDirlldownItem.customColumns)
                                  );

                                  localStorage.setItem(
                                    `__OCP_USER_${userData.id}_SQL_TOPSQL_CUSTOM_COLUMN_NAME__`,
                                    sqlDirlldownItem.customColumnName
                                  );
                                }
                                directTo(
                                  `/cluster/${tenantData?.clusterId}/tenant/${
                                    tenantData?.id
                                  }/sqlDiagnosis/topSql?${stringify({
                                    startTime,
                                    endTime,
                                    rangeKey: 'customize',
                                    sorter: JSON.stringify(sqlDirlldownItem?.sorter),
                                    customColumnName: sqlDirlldownItem?.customColumnName,
                                    ...(sqlDirlldownItem?.customColumns
                                      ? {
                                          customColumns: JSON.stringify(
                                            sqlDirlldownItem?.customColumns
                                          ),
                                        }
                                      : {}),
                                  })}`
                                );
                              }}
                            >
                              <Space size={4}>
                                <LinkOutlined />
                                {sqlDirlldownItem?.title}
                              </Space>
                            </a>
                          ) : null
                        }
                      >
                        {chartData.length > 0 ? (
                          <Chart height={186} {...chartConfig} />
                        ) : (
                          <Empty style={{ height: 160 }} imageStyle={{ marginTop: 46 }} />
                        )}
                      </MyCard>
                    </Card.Grid>

                    {drilldownScope === 'ob_cluster_id' && metricClass?.key !== 'host_metrics' && (
                      // 集群下的主机性能指标暂不支持对租户聚合，这里先注释掉
                      // || drilldownScope === 'svr_ip'
                      <Card.Grid hoverable={false} style={{ width: '100%' }}>
                        <DrilldownChart
                          // 二级下钻链路: 集群监控 + 非主机指标 -> 租户 -> OBServer
                          drilldownable={drilldownScope === 'ob_cluster_id' ? true : false}
                          getPopupContainer={getPopupContainer}
                          title={
                            drilldownScope === 'ob_cluster_id'
                              ? formatMessage(
                                  {
                                    id: 'ocp-express.MetricChart.DrilldownDrawer.TenantMetriclabel',
                                    defaultMessage: '租户的{metricLabel}',
                                  },

                                  { metricLabel: metricLabel }
                                )
                              : formatMessage(
                                  {
                                    id: 'ocp-express.MetricChart.DrilldownDrawer.MetriclabelOfDifferentTenantsOn',
                                    defaultMessage: '在该 OBServer 上不同租户的{metricLabel}',
                                  },

                                  { metricLabel: metricLabel }
                                )
                          }
                          metricClass={metricClass}
                          metricGroup={metricGroup}
                          metric={metricItem}
                          scope="tenant_name"
                          app="OB"
                          clusterName={clusterName}
                          tenantName={tenantName}
                          {...searchValues}
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

                    {(drilldownScope === 'ob_cluster_id' || drilldownScope === 'tenant_name') && (
                      <Card.Grid hoverable={false} style={{ width: '100%' }}>
                        <DrilldownChart
                          // 二级下钻链路: 集群监控 + 非主机指标 -> OBServer -> 租户
                          drilldownable={
                            drilldownScope === 'ob_cluster_id' &&
                            metricClass?.key !== 'host_metrics'
                              ? true
                              : false
                          }
                          getPopupContainer={getPopupContainer}
                          title={
                            drilldownScope === 'ob_cluster_id'
                              ? formatMessage(
                                  {
                                    id: 'ocp-express.MetricChart.DrilldownDrawer.Metriclabel',
                                    defaultMessage: 'OBServer 的{metricLabel}',
                                  },

                                  { metricLabel: metricLabel }
                                )
                              : formatMessage(
                                  {
                                    id: 'ocp-express.MetricChart.DrilldownDrawer.TenantInDifferentHostsOn',
                                    defaultMessage: '租户在不同 OBServer 上的{metricLabel}',
                                  },

                                  { metricLabel: metricLabel }
                                )
                          }
                          metricClass={metricClass}
                          metricGroup={metricGroup}
                          metric={metricItem}
                          scope="svr_ip"
                          app="OB"
                          clusterName={clusterName}
                          tenantName={tenantName}
                          {...searchValues}
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
                    )}

                    {drilldownScope === 'svr_ip' && (
                      <Card.Grid hoverable={false} style={{ width: '100%' }}>
                        <DrilldownChart
                          drilldownable={false}
                          getPopupContainer={getPopupContainer}
                          title={formatMessage(
                            {
                              id: 'ocp-express.MetricChart.DrilldownDrawer.MetriclabelOfDifferentDevicesOnTheHost',
                              defaultMessage: '在该主机上不同设备的{metricLabel}',
                            },
                            { metricLabel: metricLabel }
                          )}
                          metricClass={metricClass}
                          metricGroup={metricGroup}
                          metric={metricItem}
                          scope="device"
                          app="HOST"
                          {...searchValues}
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

export default DrilldownDrawer;
