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
import { history } from 'umi';
import React from 'react';
import { max } from 'lodash';
import moment from 'moment';
import { Progress } from '@oceanbase/charts';
import { toPercent } from '@oceanbase/charts/es/util/number';
import { Col, Empty, Row, Typography, token } from '@oceanbase/design';
import { useRequest } from 'ahooks';
import * as MonitorController from '@/service/ocp-express/MonitorController';
import MyCard from '@/component/MyCard';
import ContentWithQuestion from '@/component/ContentWithQuestion';
import { RFC3339_DATE_TIME_FORMAT } from '@/constant/datetime';
import { formatSize } from '@/util';
import { isNullValue } from '@oceanbase/util';

export interface TenantResourceTop3Props { }

const TenantResourceTop3: React.FC<TenantResourceTop3Props> = () => {

  const commonParams = {
    endTime: moment().format(RFC3339_DATE_TIME_FORMAT),
    labels: 'app:OB',
    // 按 ob_tenant_id 聚合是为了返回租户 ID，用于跳转租户监控
    groupBy: 'app,tenant_name,ob_tenant_id',
    limit: 3,
  };

  // 租户 CPU 消耗比 Top3
  const { data: cpuPercentData, loading: cpuPercentLoading } = useRequest(
    MonitorController.queryRealtimeTopMetrics,
    {
      defaultParams: [
        {
          ...commonParams,
          metrics: 'ob_cpu_percent',
        },
      ],
    }
  );

  const cpuPercentList = cpuPercentData?.data?.contents || [];

  // 租户内存消耗比 Top3
  const { data: memoryPercentData, loading: memoryPercentLoading } = useRequest(
    MonitorController.queryRealtimeTopMetrics,
    {
      defaultParams: [
        {
          ...commonParams,
          metrics: 'ob_memory_percent',
        },
      ],
    }
  );

  const memoryPercentList = memoryPercentData?.data?.contents || [];

  // 租户数据量 Top3
  const { data: diskUsedData, loading: diskUsedLoading } = useRequest(
    MonitorController.queryRealtimeTopMetrics,
    {
      defaultParams: [
        {
          ...commonParams,
          metrics: 'ob_tenant_disk_usage',
        },
      ],
    }
  );

  const diskUsedList = diskUsedData?.data?.contents || [];

  // 集群磁盘总大小，用于计算租户数据量占集群磁盘容量的百分比 Top3
  const { data: clusterDiskTotalData, loading: clusterDiskTotalDataLoading } = useRequest(
    MonitorController.queryRealtimeTopMetrics,
    {
      defaultParams: [
        {
          ...commonParams,
          metrics: 'ob_disk_total',
          // 按集群进行聚合
          groupBy: 'app',
          limit: 1,
        },
      ],
    }
  );

  // 取第一条数据即可，因为只有一个集群
  const clusterDiskTotal = (clusterDiskTotalData?.data?.contents || [])?.[0]?.data;

  const tenantMetricList = [
    {
      key: 'ob_cpu_percent',
      title: formatMessage({
        id: 'ocp-express.Component.TenantResourceTop3.CpuUsage',
        defaultMessage: 'CPU 消耗比',
      }),
      description: formatMessage({
        id: 'ocp-express.Component.TenantResourceTop3.PercentageOfCpuUsedToTenantCpu',
        defaultMessage: '已使用 CPU 占分配 CPU 的百分比',
      }),
      chartData: cpuPercentList.map(item => ({
        ...item,
        percentValue: item.data,
      })),
      loading: cpuPercentLoading,
    },

    {
      key: 'ob_memory_percent',
      title: formatMessage({
        id: 'ocp-express.Component.TenantResourceTop3.MemoryUsage',
        defaultMessage: '内存消耗比',
      }),
      description: formatMessage({
        id: 'ocp-express.Component.TenantResourceTop3.UsedMemoryAsAPercentageOfTenantMemory',
        defaultMessage: '已使用内存占分配内存的百分比',
      }),
      chartData: memoryPercentList.map(item => ({
        ...item,
        percentValue: item.data,
      })),
      loading: memoryPercentLoading,
    },

    {
      key: 'ob_tenant_disk_usage',
      title: formatMessage({
        id: 'ocp-express.Component.TenantResourceTop3.DataVolume',
        defaultMessage: '数据量',
      }),
      description: formatMessage({
        id: 'ocp-express.Component.TenantResourceTop3.TenantDataSizeAndPercentageOfClusterDisk',
        defaultMessage: '租户数据量大小，以及占集群磁盘容量的百分比',
      }),
      chartData: diskUsedList.map(item => ({
        ...item,
        usedValue: item.data,
        percentValue: (item.data / clusterDiskTotal) * 100,
      })),
      loading: diskUsedLoading || clusterDiskTotalDataLoading,
    },
  ];

  const tenantCount = max(tenantMetricList.map(item => item.chartData?.length)) || 3;

  return (
    <MyCard
      title={formatMessage({
        id: 'ocp-express.Component.TenantResourceTop3.TenantResourceUsageTop',
        defaultMessage: '租户资源使用 Top3',
      })}
      headStyle={{
        marginBottom: 16,
      }}
      style={{
        height: tenantCount < 3 ? 210 - (3 - tenantCount) * 32 : 210,
      }}
      bordered={false}
    >
      <Row gutter={48}>
        {tenantMetricList.map((item, index) => {
          return (
            <Col
              key={item.key}
              span={8}
              style={
                index !== tenantMetricList.length - 1
                  ? { borderRight: `1px solid ${token.colorBorderSecondary}` }
                  : {}
              }
            >
              <MyCard
                loading={item.loading}
                title={
                  <div
                    style={{
                      fontSize: 14,
                      fontWeight: 400,
                      fontFamily: 'PingFangSC',
                      color: token.colorText,
                    }}
                  >
                    <ContentWithQuestion
                      content={item.title}
                      tooltip={{
                        title: item.description,
                      }}
                    />
                  </div>
                }
                headStyle={{
                  marginBottom: 16,
                }}
                className="card-without-padding"
              >
                {item.chartData.length > 0 ? (
                  <Row gutter={[0, 8]}>
                    {item.chartData.map(dataItem => (
                      <Col key={dataItem.ob_tenant_id} span={24}>
                        <Progress
                          title={
                            <Typography.Text
                              data-aspm-click="c304255.d308758"
                              data-aspm-desc="租户资源使用 Top3-跳转租户详情"
                              data-aspm-param={``}
                              data-aspm-expo
                              ellipsis={{
                                tooltip: true,
                              }}
                              onClick={() => {
                                history.push(`/tenant/${dataItem.ob_tenant_id}/monitor`);
                              }}
                              className="ocp-link-hover"
                              style={{ width: 70, color: token.colorTextTertiary }}
                            >
                              {dataItem.tenant_name}
                            </Typography.Text>
                          }
                          // Progress 的 percent 为 0 ~ 1 的值，监控返回的 percent 是 0 ~ 100 的百分比值，需要进行换算
                          percent={dataItem.percentValue / 100}
                          formatter={() => {
                            if (item.key === 'ob_tenant_disk_usage') {
                              // 刚部署的 OCP Express，可能存在 percentValue 为空、usedValue 不为空的情况，为了避免百分比出现 NaN，需要做下空值判断
                              return `${formatSize(dataItem.usedValue)} / ${isNullValue(dataItem.percentValue)
                                ? '-'
                                : toPercent(dataItem.percentValue / 100, 1)
                                }%`;
                            }
                            return `${toPercent(dataItem.percentValue / 100, 1)}%`;
                          }}
                          percentStyle={{
                            width: item.key === 'ob_tenant_disk_usage' ? 120 : 60,
                          }}
                          warningPercent={0.7}
                          dangerPercent={0.8}
                          maxColumnWidth={6}
                        />
                      </Col>
                    ))}
                  </Row>
                ) : (
                  <Empty
                    imageStyle={{
                      height: 72,
                    }}
                    description=""
                  />
                )}
              </MyCard>
            </Col>
          );
        })}
      </Row>
    </MyCard>
  );
};

export default TenantResourceTop3;
