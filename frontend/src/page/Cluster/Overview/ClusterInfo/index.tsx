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
import React from 'react';
import moment from 'moment';
import {
  Col,
  Descriptions,
  Row,
  Tooltip,
  Typography,
  Badge,
  Popover,
  theme,
} from '@oceanbase/design';
import { EllipsisOutlined } from '@oceanbase/icons';
import { findByValue, isNullValue } from '@oceanbase/util';
import { Liquid } from '@oceanbase/charts';
import { toPercent } from '@oceanbase/charts/es/util/number';
import { useRequest } from 'ahooks';
import * as MonitorController from '@/service/ocp-express/MonitorController';
import { RFC3339_DATE_TIME_FORMAT } from '@/constant/datetime';
import { formatSize, isEnglish } from '@/util';
import tracert from '@/util/tracert';
import { getObServerCountByObCluster, getIdcCountByObCluster } from '@/util/oceanbase';
import ObClusterDeployMode from '@/component/ObClusterDeployMode';
import MyCard from '@/component/MyCard';
import { OB_CLUSTER_STATUS_LIST } from '@/constant/oceanbase';
import MouseTooltip from '@/component/MouseTooltip';
import useStyles from './index.style';

const { Text } = Typography;

export interface DetailProps {
  clusterData: API.ClusterInfo;
}

const Detail: React.FC<DetailProps> = ({ clusterData }) => {
  const { styles } = useStyles();
  const { token } = theme.useToken();

  const statusItem = findByValue(OB_CLUSTER_STATUS_LIST, clusterData.status);
  // 将 badge 状态映射为 color
  const colorMap = {
    processing: token.colorPrimary,
    success: token.colorSuccess,
    warning: token.colorWarning,
    error: token.colorError,
  };

  // 部署模式
  const deployModeString = (clusterData.zones || [])
    .map(item => ({
      regionName: item.regionName,
      serverCount: (item.servers || []).length,
    }))
    .map(item => item.serverCount)
    .join('-');

  const commonParams = {
    endTime: moment().format(RFC3339_DATE_TIME_FORMAT),
    labels: 'app:OB',
    groupBy: 'app',
    limit: 1,
  };

  // CPU 分配率
  const { data: cpuAssignedPercentData, loading: cpuAssignedPercentLoading } = useRequest(
    MonitorController.queryRealtimeTopMetrics,
    {
      defaultParams: [
        {
          ...commonParams,
          metrics: 'ob_cpu_assigned_percentage',
        },
      ],
    }
  );

  const cpuAssignedPercent = (cpuAssignedPercentData?.data?.contents || [])[0]?.data;

  // CPU 已分配
  const { data: cpuAssignedData } = useRequest(MonitorController.queryRealtimeTopMetrics, {
    defaultParams: [
      {
        ...commonParams,
        metrics: 'ob_cpu_assigned',
      },
    ],
  });
  const cpuAssigned = (cpuAssignedData?.data?.contents || [])[0]?.data;

  // CPU 总量
  const { data: cpuTotalData } = useRequest(MonitorController.queryRealtimeTopMetrics, {
    defaultParams: [
      {
        ...commonParams,
        metrics: 'ob_cpu_total',
      },
    ],
  });
  const cpuTotal = (cpuTotalData?.data?.contents || [])[0]?.data;

  // 内存分配率
  const { data: memoryAssignedPercentData, loading: memoryAssignedPercentLoading } = useRequest(
    MonitorController.queryRealtimeTopMetrics,
    {
      defaultParams: [
        {
          ...commonParams,
          metrics: 'ob_memory_assigned_percentage',
        },
      ],
    }
  );

  const memoryAssignedPercent = (memoryAssignedPercentData?.data?.contents || [])[0]?.data;

  // 内存已分配
  const { data: memoryAssignedData } = useRequest(MonitorController.queryRealtimeTopMetrics, {
    defaultParams: [
      {
        ...commonParams,
        metrics: 'ob_memory_assigned',
      },
    ],
  });
  const memoryAssigned = (memoryAssignedData?.data?.contents || [])[0]?.data;

  // 内存总量
  const { data: memoryTotalData } = useRequest(MonitorController.queryRealtimeTopMetrics, {
    defaultParams: [
      {
        ...commonParams,
        metrics: 'ob_memory_total',
      },
    ],
  });
  const memoryTotal = (memoryTotalData?.data?.contents || [])[0]?.data;

  // 磁盘使用率
  const { data: diskUsedPercentData, loading: diskUsedPercentLoading } = useRequest(
    MonitorController.queryRealtimeTopMetrics,
    {
      defaultParams: [
        {
          ...commonParams,
          metrics: 'ob_disk_used_percentage',
        },
      ],
    }
  );

  const diskUsedPercent = (diskUsedPercentData?.data?.contents || [])[0]?.data;

  // 内存已使用
  const { data: diskUsedData } = useRequest(MonitorController.queryRealtimeTopMetrics, {
    defaultParams: [
      {
        ...commonParams,
        metrics: 'ob_disk_used',
      },
    ],
  });
  const diskUsed = (diskUsedData?.data?.contents || [])[0]?.data;

  // 磁盘总量
  const { data: diskTotalData } = useRequest(MonitorController.queryRealtimeTopMetrics, {
    defaultParams: [
      {
        ...commonParams,
        metrics: 'ob_disk_total',
      },
    ],
  });
  const diskTotal = (diskTotalData?.data?.contents || [])[0]?.data;

  const metricList = [
    {
      key: 'cpu',
      title: 'CPU',
      description: formatMessage({
        id: 'ocp-express.Component.ClusterInfo.Allocation',
        defaultMessage: '分配',
      }),
      percentValue: cpuAssignedPercent,
      totalValue: cpuTotal,
      leftValue: cpuTotal - cpuAssigned,
    },
    {
      key: 'memory',
      title: formatMessage({
        id: 'ocp-express.Component.ClusterInfo.Memory',
        defaultMessage: '内存',
      }),
      description: formatMessage({
        id: 'ocp-express.Component.ClusterInfo.Allocation',
        defaultMessage: '分配',
      }),
      percentValue: memoryAssignedPercent,
      totalValue: memoryTotal,
      leftValue: memoryTotal - memoryAssigned,
    },
    {
      key: 'disk',
      title: formatMessage({
        id: 'ocp-express.Component.ClusterInfo.Disk',
        defaultMessage: '磁盘',
      }),
      description: formatMessage({
        id: 'ocp-express.Component.ClusterInfo.Use',
        defaultMessage: '使用',
      }),
      percentValue: diskUsedPercent,
      totalValue: diskTotal,
      leftValue: diskTotal - diskUsed,
    },
  ];

  // 使用空字符串兜底，避免文案拼接时出现 undefined
  const clusterName = clusterData.clusterName || '';

  return (
    <div
      data-aspm="c304183"
      data-aspm-desc="集群信息"
      data-aspm-expo
      // 扩展参数
      data-aspm-param={tracert.stringify({
        // 集群 OB 版本
        clusterObVersion: clusterData.obVersion,
        // 集群部署模式
        clusterDeployMode: deployModeString,
        // 集群机房数
        clusterIdcCount: getIdcCountByObCluster(clusterData),
        // 集群 OBServer 数
        clusterObserverCount: getObServerCountByObCluster(clusterData),
        // 集群租户数
        clusterTenantCount: clusterData.tenants?.length || 0,
      })}
    >
      <MyCard
        loading={
          cpuAssignedPercentLoading || memoryAssignedPercentLoading || diskUsedPercentLoading
        }
        title={
          <div>
            <span
              style={{
                marginRight: 16,
              }}
            >
              {formatMessage(
                {
                  id: 'ocp-express.Component.ClusterInfo.ClusterClustername',
                  defaultMessage: '集群 {clusterName}',
                },
                { clusterName: clusterName }
              )}
            </span>
            <Badge
              status={statusItem.badgeStatus}
              text={
                <span
                  style={{
                    color: colorMap[statusItem.badgeStatus as string],
                  }}
                >
                  {statusItem.label}
                </span>
              }
            />
          </div>
        }
        extra={
          <Popover
            placement="bottomRight"
            arrowPointAtCenter={true}
            overlayStyle={{
              maxWidth: 200,
            }}
            content={
              <Descriptions colon={false} column={1}>
                <Descriptions.Item
                  label={formatMessage({
                    id: 'ocp-express.Component.ClusterInfo.ClusterName',
                    defaultMessage: '集群名称',
                  })}
                  className="descriptions-item-with-ellipsis"
                >
                  <Text
                    ellipsis={{
                      tooltip: clusterData.clusterName,
                    }}
                  >
                    {clusterData.clusterName}
                  </Text>
                </Descriptions.Item>
                <Descriptions.Item
                  label={formatMessage({
                    id: 'ocp-express.Detail.Overview.DeploymentMode',
                    defaultMessage: '部署模式',
                  })}
                  style={{
                    paddingBottom: 0,
                  }}
                >
                  {clusterData.zones?.length > 0 ? (
                    <Tooltip title={<ObClusterDeployMode clusterData={clusterData} />}>
                      <ObClusterDeployMode clusterData={clusterData} mode="text" />
                    </Tooltip>
                  ) : (
                    '-'
                  )}
                </Descriptions.Item>
              </Descriptions>
            }
            bodyStyle={{
              padding: '16px 24px',
            }}
          >
            <EllipsisOutlined
              data-aspm-click="c304183.d308772"
              data-aspm-desc="集群信息-查看更多"
              data-aspm-expo
              data-aspm-param={``}
              className="pointable"
              style={{
                fontSize: 20,
              }}
            />
          </Popover>
        }
      >
        <MouseTooltip
          style={{
            maxWidth: isEnglish() ? 600 : 500,
            padding: 16,
          }}
          overlay={
            <span>
              <Row gutter={[48, 12]}>
                {metricList.map(item => (
                  <Col key={item.key} span={8}>
                    <div
                      style={{ paddingLeft: 4, fontSize: 16, fontWeight: 500, marginBottom: 12 }}
                    >
                      {item.title}
                    </div>
                    <Descriptions
                      size="small"
                      colon={false}
                      column={1}
                      className={styles.borderRight}
                    >
                      <Descriptions.Item
                        label={formatMessage({
                          id: 'ocp-express.Component.ClusterInfo.Total',
                          defaultMessage: '总量',
                        })}
                      >
                        {isNullValue(item.totalValue)
                          ? '-'
                          : item.key === 'cpu'
                          ? `${item.totalValue} C`
                          : // 内存和磁盘需要进行单位换算
                            formatSize(item.totalValue)}
                      </Descriptions.Item>
                      <Descriptions.Item label={item.description}>
                        {/* 最多保留 1 位有效小数，需要用 toPercent 处理下 */}
                        {isNullValue(item.percentValue)
                          ? '-'
                          : `${toPercent(item.percentValue / 100, 1)}%`}
                      </Descriptions.Item>
                      <Descriptions.Item
                        label={formatMessage({
                          id: 'ocp-express.Component.ClusterInfo.Remaining',
                          defaultMessage: '剩余',
                        })}
                      >
                        {isNullValue(item.leftValue)
                          ? '-'
                          : item.key === 'cpu'
                          ? `${item.leftValue} C`
                          : // 内存和磁盘需要进行单位换算
                            formatSize(item.leftValue)}
                      </Descriptions.Item>
                    </Descriptions>
                  </Col>
                ))}
              </Row>
            </span>
          }
        >
          <Row gutter={48}>
            {metricList.map((item, index) => (
              <Col
                key={item.key}
                span={8}
                style={
                  index !== 2 ? { borderRight: `1px solid ${token.colorBorderSecondary}` } : {}
                }
              >
                <Liquid
                  height={54}
                  layout="horizontal"
                  title={item.title}
                  shape="rect"
                  // Progress 的 percent 为 0 ~ 1 的值，监控返回的 percent 是 0 ~ 100 的百分比值，需要进行换算
                  percent={item.percentValue / 100}
                  // 最多保留 1 位有效小数
                  decimal={1}
                  warningPercent={0.7}
                  dangerPercent={0.8}
                />
              </Col>
            ))}
          </Row>
        </MouseTooltip>
      </MyCard>
    </div>
  );
};

export default Detail;
