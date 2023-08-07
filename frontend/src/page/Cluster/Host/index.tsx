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
import {
  Col,
  Progress,
  Row,
  Table,
  Typography,
  Badge,
  Space,
  Tag,
  Button,
  Descriptions,
  Modal,
  useToken,
} from '@oceanbase/design';
import React from 'react';
import { groupBy, sum, uniq } from 'lodash';
import moment from 'moment';
import { PageContainer } from '@ant-design/pro-components';
import type { Route } from 'antd/es/breadcrumb/Breadcrumb';
import { findByValue, formatNumber, isNullValue, sortByNumber } from '@oceanbase/util';
import { LoadingOutlined } from '@ant-design/icons';
import type { ColumnProps } from 'antd/es/table';
import { OCP_AGENT_PROCESS_STATUS_LIST } from '@/constant/compute';
import { useRequest } from 'ahooks';
import * as MonitorController from '@/service/ocp-express/MonitorController';
import * as ObClusterController from '@/service/ocp-express/ObClusterController';
import * as HostController from '@/service/ocp-express/HostController';
import { RFC3339_DATE_TIME_FORMAT } from '@/constant/datetime';
import { formatSize, isEnglish } from '@/util';
import { breadcrumbItemRender } from '@/util/component';
import ContentWithQuestion from '@/component/ContentWithQuestion';
import MyCard from '@/component/MyCard';
import useStyles from './index.style';

const { Text } = Typography;

export interface BasicProps {
  match: {
    params: {
      ip: string;
      serverPort: number;
    };
  };
}

interface DataItem {
  device?: string;
  host_disk_total?: number;
  host_disk_used?: number;
  host_disk_free?: number;
  host_disk_used_percent?: number;
}

const Index: React.FC<BasicProps> = ({ match }) => {
  const { styles } = useStyles();
  const { token } = useToken();

  const ip = match?.params?.ip;
  const obSvrPort = match?.params?.serverPort;

  const metricList = [
    'host_disk_total',
    'host_disk_used',
    'host_disk_free',
    'host_disk_used_percent',
  ];

  const { data: hostInfoRes, loading: hostInfoLoading } = useRequest(HostController.getHostInfo, {
    manual: false,
    defaultParams: [
      {
        ip,
        obSvrPort,
      },
    ],
  });

  const hostInfoData: API.HostInfo = hostInfoRes?.data || {};

  // 获取 server 的指标数据
  const { data: serverInfoData } = useRequest(ObClusterController.getServerInfo, {
    defaultParams: [
      {
        ip,
        obSvrPort,
      },
    ],
  });

  const serverInfo = serverInfoData?.data || {};

  const {
    data: agentDetailData,
    loading: getAgentDetailLoading,
    refresh,
  } = useRequest(HostController.getAgentDetail, {
    manual: false,
    defaultParams: [
      {
        ip,
        obSvrPort,
      },
    ],
  });

  const agentDetail: API.RemoteHostInfo = agentDetailData?.data || {};

  // 重启 Agent
  const { run: restartHostAgent } = useRequest(HostController.restartHostAgent, {
    manual: true,
    onSuccess: (res) => {
      if (res.successful) {
        refresh();
      }
    },
  });

  // 获取磁盘的指标数据
  const { data, loading } = useRequest(MonitorController.queryMetricTop, {
    defaultParams: [
      {
        metrics: metricList.join(','),
        labels: `svr_ip:${ip}`,
        // 虽然是获取磁盘指标，应该按磁盘聚合，但还需要获取挂载目录的信息，因此按挂载目录进行聚合，在后面的数据处理中对磁盘数据进行手动计算
        groupBy: 'app,svr_ip,device,mount_point',
        startTime: moment().subtract(1, 'minutes').format(RFC3339_DATE_TIME_FORMAT),
        endTime: moment().format(RFC3339_DATE_TIME_FORMAT),
      },
    ],
  });

  const metricDataList: DataItem &
    {
      mount_point?: string;
    }[] = (data?.data?.contents || []).map((item) => {
    // 取最后一个数据点，作为磁盘当前的数据展示
    const last = item?.data?.[item?.data?.length - 1];
    return {
      ...item,
      ...last,
    };
  });

  const dataSource = Object.entries(groupBy(metricDataList, 'device')).map(([key, value]) => {
    const dataItem: DataItem & {
      mount_point_list?: string[];
    } = {
      device: key,
      // 同一个磁盘可能有多个挂载目录
      mount_point_list: uniq(value?.map((item) => item.mount_point as string) || []),
    };

    metricList
      .filter((metric) => metric !== 'host_disk_used_percent')
      .forEach((metric) => {
        dataItem[metric] =
          // 对求和结果做数据处理
          formatNumber(
            sum(value?.filter((item) => !isNullValue(item[metric])).map((item) => item[metric])),
            2,
          ) || 0;
      });
    // 磁盘使用率，不直接使用接口返回的 (因为有多个挂载目录，接口返回的是按挂载目录计算的使用率)，而是手动计算
    dataItem.host_disk_used_percent = dataItem.host_disk_total
      ? // 对除法结果做数据处理
        formatNumber((dataItem.host_disk_used || 0) / dataItem.host_disk_total, 2)
      : 0;
    return dataItem;
  });

  const columns: ColumnProps<
    DataItem & {
      mount_point_list?: string[];
    }
  >[] = [
    {
      title: formatMessage({ id: 'ocp-express.Host.Detail.Basic.Path', defaultMessage: '路径' }),
      dataIndex: 'device',
    },

    {
      title: formatMessage({
        id: 'ocp-express.Host.Detail.Basic.PartitionDiskGb',
        defaultMessage: '分区磁盘（GB）',
      }),

      dataIndex: 'host_disk_total',
    },

    {
      title: formatMessage({
        id: 'ocp-express.Host.Detail.Basic.DiskUsedGb',
        defaultMessage: '磁盘已使用（GB）',
      }),

      sorter: (a, b) => sortByNumber(a, b, 'host_disk_used'),
      dataIndex: 'host_disk_used',
    },

    {
      title: formatMessage({
        id: 'ocp-express.Host.Detail.Basic.AvailableSizeGb',
        defaultMessage: '可用大小（GB）',
      }),

      sorter: (a, b) => sortByNumber(a, b, 'host_disk_free'),
      dataIndex: 'host_disk_free',
    },

    {
      title: formatMessage({ id: 'ocp-express.Host.Detail.Basic.Usage', defaultMessage: '使用率' }),
      dataIndex: 'host_disk_used_percent',
      sorter: (a, b) => sortByNumber(a, b, 'host_disk_used_percent'),
      defaultSortOrder: 'descend',
      width: '18%',
      render: (text = 0) => (
        <Progress
          // 对乘法结果做数据处理
          percent={formatNumber(text * 100, 2)}
          // percent 大于 97 显示红色线段
          strokeColor={text * 100 >= 97 ? token.colorError : undefined}
          status="normal"
        />
      ),
    },

    {
      title: formatMessage({
        id: 'ocp-express.Host.Detail.Basic.MountDirectory',
        defaultMessage: '挂载目录',
      }),

      dataIndex: 'mount_point_list',
      render: (text?: string[]) => text?.map((item) => <div>{item}</div>),
    },
  ];

  const OBAgentColumns = [
    {
      title: formatMessage({
        id: 'ocp-express.Host.Detail.OCPAgent.ProcessId',
        defaultMessage: '进程 ID',
      }),

      dataIndex: 'pid',
      ellipsis: true,
    },
    {
      title: formatMessage({ id: 'ocp-express.Host.Detail.OCPAgent.Name', defaultMessage: '名称' }),
      dataIndex: 'name',
    },
    {
      title: formatMessage({
        id: 'ocp-express.Host.Detail.OCPAgent.PortNumber',
        defaultMessage: '端口号',
      }),

      dataIndex: 'port',
      render: (text, record: API.ObAgentProcess) => {
        const portMap = {
          ob_mgragent: 'mgrPort',
          ob_monagent: 'monPort',
        };
        return <span>{agentDetail[portMap[record.name]] || '-'}</span>;
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.Host.Detail.OCPAgent.State',
        defaultMessage: '状态',
      }),
      dataIndex: 'state',
      render: (text: API.HostAgentProcessStatus) => {
        const {
          label = formatMessage({
            id: 'ocp-express.Host.Detail.OCPAgent.NotRunning',
            defaultMessage: '未运行',
          }),

          badgeStatus = 'default',
        } = findByValue(OCP_AGENT_PROCESS_STATUS_LIST, text) || {};
        return <Badge status={badgeStatus} text={label} />;
      },
    },
  ];

  const routes: Route[] = [
    {
      path: '/overview',
      breadcrumbName: formatMessage({
        id: 'ocp-express.Cluster.Host.ClusterOverview',
        defaultMessage: '集群总览',
      }),
    },

    {
      breadcrumbName: ip,
    },
  ];

  return (
    <PageContainer
      className={styles.container}
      loading={loading}
      ghost={true}
      header={{
        title: ip,
        breadcrumb: { routes, itemRender: breadcrumbItemRender },
        onBack: () => history.push('/overview'),
      }}
    >
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <MyCard bordered={false}>
            <div data-aspm="c304178" data-aspm-desc="主机信息" data-aspm-param={``} data-aspm-expo>
              <Descriptions
                title={formatMessage({
                  id: 'ocp-express.Host.Detail.Basic.Details',
                  defaultMessage: '系统信息',
                })}
                column={4}
              >
                <Descriptions.Item
                  label={formatMessage({
                    id: 'ocp-express.Host.Detail.Basic.HostName',
                    defaultMessage: '主机名',
                  })}
                  className="descriptions-item-with-ellipsis"
                >
                  <Text ellipsis={{ tooltip: true }}>{hostInfoData.hostname}</Text>
                </Descriptions.Item>
                <Descriptions.Item
                  label={formatMessage({
                    id: 'ocp-express.Host.Detail.Basic.OperatingSystem',
                    defaultMessage: '操作系统',
                  })}
                  className="descriptions-item-with-ellipsis"
                >
                  <Text ellipsis={{ tooltip: true }}>{hostInfoData.os}</Text>
                </Descriptions.Item>
                <Descriptions.Item
                  label={formatMessage({
                    id: 'ocp-express.Host.Detail.Basic.ClockOffset',
                    defaultMessage: '时钟偏移',
                  })}
                >
                  {hostInfoLoading ? (
                    <LoadingOutlined />
                  ) : (
                    <ContentWithQuestion
                      content={
                        isNullValue(hostInfoData.clockDiffMillis)
                          ? '-'
                          : `${hostInfoData.clockDiffMillis}ms`
                      }
                      tooltip={{
                        placement: 'top',
                        title: formatMessage({
                          id: 'ocp-express.Host.Detail.Basic.ClockOffsetBetweenOcpServer',
                          defaultMessage: 'OCP Server 与主机的时钟偏移量',
                        }),
                      }}
                    />
                  )}
                </Descriptions.Item>
                <Descriptions.Item
                  label={formatMessage({
                    id: 'ocp-express.Cluster.Host.TotalMemory',
                    defaultMessage: '总内存',
                  })}
                >
                  {hostInfoLoading ? <LoadingOutlined /> : formatSize(hostInfoData.totalMemory)}
                </Descriptions.Item>
                <Descriptions.Item
                  label={formatMessage({
                    id: 'ocp-express.Cluster.Host.CpuArchitecture',
                    defaultMessage: 'CPU 架构',
                  })}
                >
                  {hostInfoLoading ? <LoadingOutlined /> : hostInfoData.architecture}
                </Descriptions.Item>
                <Descriptions.Item
                  label={formatMessage({
                    id: 'ocp-express.Cluster.Host.CpuCores',
                    defaultMessage: 'CPU 核数',
                  })}
                >
                  {hostInfoLoading ? <LoadingOutlined /> : hostInfoData.cpuCount}
                </Descriptions.Item>
                <Descriptions.Item
                  label={formatMessage({
                    id: 'ocp-express.Cluster.Host.DataDirectory',
                    defaultMessage: '数据目录',
                  })}
                >
                  {serverInfo?.dataPath || '-'}
                </Descriptions.Item>
                <Descriptions.Item
                  label={formatMessage({
                    id: 'ocp-express.Cluster.Host.LogDirectory',
                    defaultMessage: '日志目录',
                  })}
                >
                  {serverInfo?.logPath || '-'}
                </Descriptions.Item>
              </Descriptions>
            </div>
          </MyCard>
        </Col>
        <Col span={24}>
          <MyCard
            headStyle={{ padding: '16px 24px 0 24px' }}
            className="card-without-padding"
            bordered={false}
            title={
              <span>
                {formatMessage({
                  id: 'ocp-express.Host.Detail.Basic.DiskUsage',
                  defaultMessage: '磁盘使用',
                })}
              </span>
            }
          >
            <Table
              loading={loading}
              columns={columns}
              dataSource={dataSource}
              rowKey={(record) => record.device}
              pagination={false}
            />
          </MyCard>
        </Col>
        <Col span={24}>
          <div
            data-aspm="c304246"
            data-aspm-desc="Agent 进程列表"
            data-aspm-param={``}
            data-aspm-expo
          >
            <MyCard
              headStyle={{ padding: '16px 24px 0 24px' }}
              className="card-without-padding"
              bordered={false}
              title={
                <Space>
                  <span>
                    {formatMessage({
                      id: 'ocp-express.Cluster.Host.ObAgentProcess',
                      defaultMessage: 'OB Agent 进程',
                    })}
                  </span>
                  <Tag color="geekblue">
                    {agentDetail?.version}
                    {isEnglish()
                      ? null
                      : formatMessage({
                          id: 'ocp-express.Cluster.Host.Version',
                          defaultMessage: '版本',
                        })}
                  </Tag>
                </Space>
              }
              extra={
                <Button
                  data-aspm-click="c304246.d308732"
                  data-aspm-desc="Agent 进程列表-重启 Agent"
                  data-aspm-param={``}
                  data-aspm-expo
                  onClick={() => {
                    Modal.confirm({
                      title: formatMessage(
                        {
                          id: 'ocp-express.Cluster.Host.AreYouSureYouWantToRestartThe',
                          defaultMessage: '确定要重启主机 {ip} OBAgent 吗？',
                        },
                        { ip: ip },
                      ),

                      content: formatMessage({
                        id: 'ocp-express.Cluster.Host.RestartTheHostOcpAgentDuringWhichO',
                        defaultMessage: '重启主机 OBAgent，期间运维操作和监控数据采集都无法进行',
                      }),
                      okText: formatMessage({
                        id: 'ocp-express.Cluster.Host.Confirm',
                        defaultMessage: '确认',
                      }),
                      cancelText: formatMessage({
                        id: 'ocp-express.Cluster.Host.Cancel',
                        defaultMessage: '取消',
                      }),
                      onOk: () => {
                        restartHostAgent({
                          ip,
                          obSvrPort,
                        });
                      },
                    });
                  }}
                >
                  {formatMessage({
                    id: 'ocp-express.Cluster.Host.RestartTheAgent',
                    defaultMessage: '重启 Agent',
                  })}
                </Button>
              }
            >
              <Table
                loading={getAgentDetailLoading}
                rowKey="id"
                columns={OBAgentColumns}
                dataSource={agentDetail.obAgentProcesses || []}
              />
            </MyCard>
          </div>
        </Col>
      </Row>
    </PageContainer>
  );
};

export default Index;
