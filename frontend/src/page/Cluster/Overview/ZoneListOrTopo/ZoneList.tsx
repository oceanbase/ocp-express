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
import React, { useState, useImperativeHandle } from 'react';
import { Badge } from '@oceanbase/design';
import { Table } from '@oceanbase/design';
import { byte2GB, findByValue } from '@oceanbase/util';
import { isEnglish } from '@/util';
import MyProgress from '@/component/MyProgress';
import { ZONE_STATUS_LIST, OB_SERVER_STATUS_LIST } from '@/constant/oceanbase';
import useStyles from './index.style';

export interface ZoneListRef {
  expandAll: () => void;
  setStatusList: (statusList: API.ObServerStatus[]) => void;
}

export interface ZoneListProps {
  clusterData: API.ClusterInfo;
}

const ZoneList = React.forwardRef<ZoneListRef, ZoneListProps>(({ clusterData }, ref) => {
  const { styles } = useStyles();
  const dataSource = clusterData?.zones || [];
  const expandable =
    clusterData?.zones?.filter(item => (item?.servers?.length || 0) > 0).length > 0;

  const [expandedRowKeys, setExpandedRowKeys] = useState<React.Key[]>(
    // 默认展开全部 Zone
    dataSource.map(item => item.name) || []
  );
  const [statusList, setStatusList] = useState<string[]>([]);

  // 向组件外部暴露 refresh 属性函数，可通过 ref 引用
  useImperativeHandle(ref, () => ({
    expandAll: () => {
      setExpandedRowKeys(dataSource.map(item => item.name as string));
    },
    setStatusList: (newStatusList: API.ObServerStatus[]) => {
      setStatusList(newStatusList);
    },
  }));

  const columns = [
    {
      title: formatMessage({
        id: 'ocp-express.Component.ZoneListOrTopo.ZoneList.ZoneName',
        defaultMessage: 'Zone 名',
      }),
      dataIndex: 'name',
    },

    {
      title: formatMessage({
        id: 'ocp-express.Component.ZoneList.Region',
        defaultMessage: '所属 Region',
      }),
      dataIndex: 'regionName',
    },

    {
      title: formatMessage({
        id: 'ocp-express.Component.ZoneList.DataCenter',
        defaultMessage: '所在机房',
      }),

      dataIndex: 'idcName',
      render: (text: string) => <span>{text || '-'}</span>,
    },

    {
      title: formatMessage({
        id: 'ocp-express.Component.ZoneList.NumberOfMachines',
        defaultMessage: 'OBServer 数量',
      }),
      key: 'serverCount',
      dataIndex: 'servers',
      render: (text?: API.Server[]) => <span>{text?.length || 0}</span>,
    },

    {
      title: 'Root Server',
      key: 'rootServer',
      dataIndex: 'rootServer',
      render: (text?: API.RootServer, record: API.Zone) => {
        const roleLabel =
          text && text.role === 'LEADER'
            ? formatMessage({ id: 'ocp-express.Component.ZoneList.Main', defaultMessage: '（主）' })
            : '';

        return <span>{text ? `${text?.ip}:${text?.svrPort}${roleLabel}` : '-'}</span>;
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.Component.ZoneList.State',
        defaultMessage: '状态',
      }),
      dataIndex: 'status',
      filters: ZONE_STATUS_LIST.map(item => ({
        value: item.value,
        text: item.label,
      })),
      onFilter: (value: API.ObZoneStatus, record: API.Zone) => record.status === value,
      render: (text: API.ObZoneStatus) => {
        const statusItem = findByValue(ZONE_STATUS_LIST, text);
        return <Badge status={statusItem.badgeStatus} text={statusItem.label} />;
      },
    },
  ];

  function getExpandedRowRender(record: API.Zone) {
    const { servers } = record;
    const expandColumns = [
      {
        title: 'OBServer IP',
        dataIndex: 'ip',
        render: (text: string, expandedRecord: API.Server) => {
          return (
            <a
              data-aspm-click="c304256.d308759"
              data-aspm-desc="集群拓扑列表-跳转 OBServer 详情"
              data-aspm-param={``}
              data-aspm-expo
              onClick={() => {
                history.push(`/overview/server/${text}/${expandedRecord.port}`);
              }}
            >
              {text}
            </a>
          );
        },
      },

      {
        title: formatMessage({
          id: 'ocp-express.Component.ZoneListOrTopo.ZoneList.SqlPort',
          defaultMessage: 'SQL 端口',
        }),
        dataIndex: 'sqlPort',
      },

      {
        title: formatMessage({
          id: 'ocp-express.Component.ZoneListOrTopo.ZoneList.RpcPort',
          defaultMessage: 'RPC 端口',
        }),
        dataIndex: 'port',
      },

      {
        title: formatMessage({
          id: 'ocp-express.Component.ZoneListOrTopo.ZoneList.HardwareArchitecture',
          defaultMessage: '硬件架构',
        }),
        dataIndex: 'architecture',
      },

      {
        title: formatMessage({
          id: 'ocp-express.Detail.Component.WaterLevel.ResourceWaterLevel',
          defaultMessage: '资源水位',
        }),

        dataIndex: 'stats',
        render: (text: API.ServerResourceStats) => {
          const {
            cpuCoreAssigned = 0,
            cpuCoreTotal = 0,
            cpuCoreAssignedPercent = 0,
            memoryInBytesAssigned = 0,
            memoryInBytesTotal = 0,
            memoryAssignedPercent = 0,
            diskUsed = 0,
            diskTotal = 0,
            diskUsedPercent = 0,
          } = text || {};
          const prefixWidth = isEnglish() ? 50 : 30;
          return (
            <span className={styles.stats}>
              <MyProgress
                showInfo={false}
                prefix="CPU"
                prefixWidth={prefixWidth}
                affix={formatMessage(
                  {
                    id: 'ocp-express.Component.OBServerList.CpucoreassignedCpucoretotalCore',
                    defaultMessage: '{cpuCoreAssigned}/{cpuCoreTotal} 核',
                  },

                  { cpuCoreAssigned, cpuCoreTotal }
                )}
                affixWidth={110}
                percent={cpuCoreAssignedPercent}
              />

              <MyProgress
                showInfo={false}
                prefix={formatMessage({
                  id: 'ocp-express.Component.OBServerList.Memory',
                  defaultMessage: '内存',
                })}
                prefixWidth={prefixWidth}
                affix={`${byte2GB(memoryInBytesAssigned).toFixed(1)}/${byte2GB(
                  memoryInBytesTotal
                ).toFixed(1)} GB`}
                affixWidth={110}
                percent={memoryAssignedPercent}
              />

              <MyProgress
                showInfo={false}
                prefix={formatMessage({
                  id: 'ocp-express.Component.OBServerList.Disk',
                  defaultMessage: '磁盘',
                })}
                prefixWidth={prefixWidth}
                // 磁盘使用率一般不高，已使用量和总量可能差距较大，因此这里展示时不统一单位
                affix={`${diskUsed}/${diskTotal}`}
                affixWidth={110}
                percent={diskUsedPercent}
              />
            </span>
          );
        },
      },

      {
        title: formatMessage({
          id: 'ocp-express.Component.OBServerList.State',
          defaultMessage: '状态',
        }),
        dataIndex: 'status',
        filters: OB_SERVER_STATUS_LIST.map(item => ({
          value: item.value,
          text: item.label,
        })),
        filteredValue: statusList,
        // 这里不用设置 onFilter，dataSource 已经根据 statusList 做了筛选
        render: (text: API.ObServerStatus) => {
          const statusItem = findByValue(OB_SERVER_STATUS_LIST, text);
          return <Badge status={statusItem.badgeStatus} text={statusItem.label} />;
        },
      },
    ];

    return (
      <Table
        columns={expandColumns}
        dataSource={(servers || []).filter(
          item => statusList.length === 0 || statusList.includes(item.status as API.ObServerStatus)
        )}
        rowKey={item => item.id}
        pagination={false}
        onChange={(pagination, filters) => {
          setStatusList(filters.status || []);
        }}
      />
    );
  }

  return (
    <Table
      id="ocp-zone-table"
      columns={columns}
      dataSource={dataSource}
      rowKey={(record: API.Zone) => record.name as string}
      rowClassName={(record: API.Zone) =>
        expandable && !record.servers?.length ? 'table-row-hide-expand-icon' : ''
      }
      expandedRowKeys={expandedRowKeys}
      onExpandedRowsChange={newExpandedRowKeys => {
        setExpandedRowKeys(newExpandedRowKeys);
      }}
      expandable={{
        expandRowByClick: true,
        expandedRowRender: expandable ? getExpandedRowRender : undefined,
      }}
      pagination={false}
      className={styles.table}
    />
  );
});

export default ZoneList;
