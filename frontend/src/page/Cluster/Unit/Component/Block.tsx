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
import { Badge, Divider, Progress, Space, Tooltip, Typography } from '@oceanbase/design';
import { QuestionCircleOutlined } from '@oceanbase/icons';
import { isNullValue, byte2GB, directTo, findByValue } from '@oceanbase/util';
import { TENANT_STATUS_LIST } from '@/constant/tenant';
import MouseTooltip from '@/component/MouseTooltip';
import styles from './Block.less';

export type BlockType =
  | 'region'
  | 'zone'
  | 'server'
  | 'tenant'
  | 'unit'
  | 'replica'
  | 'memory'
  | 'cpu'
  | 'disk';

export interface BlockProps {
  type: BlockType;
  target?: string | number;
  zoneInfo?: (API.ClusterUnitViewOfZone | API.ClusterReplicaViewOfZone) & {
    obRegionName: string;
  };

  serverInfo?: API.ClusterUnitViewOfServer & API.ClusterReplicaViewOfZone;
  tenantInfo?: API.ClusterUnitViewOfTenant;
  clusterId?: number;
  className?: string;
}

const { Text } = Typography;

const Block: React.FC<BlockProps> = ({
  type,
  target,
  zoneInfo,
  serverInfo,
  tenantInfo,
  clusterId,
  className,
  ...restProps
}) => {
  // zoneInfo
  const memorySizeAssigned = byte2GB(zoneInfo?.memorySizeAssignedByte || 0);
  const diskSizeUsed = byte2GB(zoneInfo?.diskSizeUsedByte || 0);

  const typeList = [
    {
      value: 'region',
      label: formatMessage({
        id: 'ocp-express.Resource.Component.Block.Area',
        defaultMessage: '区域',
      }),
      badgeColor: 'pink',
      overlay: <div>{target}</div>,
    },

    {
      value: 'zone',
      label: 'Zone',
      badgeColor: 'purple',
      overlay: zoneInfo && (
        <div>
          <div>
            {formatMessage(
              {
                id: 'ocp-express.Resource.Component.Block.NameZoneinfoobzonename',
              },

              { zoneInfoObZoneName: zoneInfo.obZoneName }
            )}
          </div>
          <div>
            {formatMessage(
              {
                id: 'ocp-express.Resource.Component.Block.RegionZoneinfoobregionname',
              },

              { zoneInfoObRegionName: zoneInfo.obRegionName }
            )}
          </div>
          <div>
            {formatMessage(
              {
                id: 'ocp-express.Resource.Component.Block.TotalMemoryAllocationGbMemorysizeassigned',
              },

              { memorySizeAssigned }
            )}
          </div>
          <div>
            {formatMessage(
              {
                id: 'ocp-express.Resource.Component.Block.TotalCpuAllocationGbZoneinfocpucountassigned',
                defaultMessage: 'CPU 总分配：{zoneInfoCpuCountAssigned}',
              },

              { zoneInfoCpuCountAssigned: zoneInfo.cpuCountAssigned }
            )}
          </div>
          <div>
            {formatMessage(
              {
                id: 'ocp-express.Resource.Component.Block.MaximumDiskUsageGbDisksizeused',
              },

              { diskSizeUsed }
            )}
          </div>
          <div>
            {!isNullValue(zoneInfo.unitCount) &&
              formatMessage(
                {
                  id: 'ocp-express.Resource.Component.Block.UnitZoneinfounitcount',
                  defaultMessage: 'Unit 总数：{zoneInfoUnitCount}',
                },

                { zoneInfoUnitCount: zoneInfo.unitCount }
              )}
          </div>
        </div>
      ),
    },

    {
      value: 'server',
      label: 'OBServer',

      badgeColor: 'cyan',
      overlay: serverInfo && (
        <div>
          <div>{`Server IP: ${serverInfo.serverIp}`}</div>
          <div>{`Server Port: ${serverInfo.serverPort}`}</div>
        </div>
      ),
    },

    {
      value: 'tenant',
      label: formatMessage({
        id: 'ocp-express.Resource.Component.Block.Tenant',
        defaultMessage: '租户',
      }),

      badgeColor: '',
      overlay: tenantInfo && (
        <div>
          <div>
            {formatMessage(
              {
                id: 'ocp-express.Resource.Component.Block.TenantNameTenantinfotenantname',
                defaultMessage: '租户名：{tenantInfoTenantName}',
              },

              { tenantInfoTenantName: tenantInfo.tenantName }
            )}
          </div>
          <div>
            {formatMessage(
              { id: 'ocp-express.Resource.Component.Block.TenantIdTenantinfoobtenantid' },

              { tenantInfoObTenantId: tenantInfo.obTenantId }
            )}
          </div>
          <div>
            {formatMessage(
              {
                id: 'ocp-express.Resource.Component.Block.TenantStatusFindbyvaluetenantstatus',
                defaultMessage: '租户状态：{findByValueTENANTSTATUS}',
              },
              { findByValueTENANTSTATUS: findByValue(TENANT_STATUS_LIST, tenantInfo.status).label }
            )}
          </div>
          <div>
            {formatMessage(
              {
                id: 'ocp-express.Resource.Component.Block.ZonePriorityTenantinfoprimaryzone',
              },

              { tenantInfoPrimaryZone: tenantInfo.primaryZone }
            )}
          </div>
          <div>
            {formatMessage(
              {
                id: 'ocp-express.Resource.Component.Block.ReplicaDistributionTenantinfolocality',
              },

              { tenantInfoLocality: tenantInfo.locality }
            )}
          </div>
        </div>
      ),
    },

    {
      value: 'unit',
      label: formatMessage({
        id: 'ocp-express.Resource.Component.Block.Unit',
        defaultMessage: 'Unit 数目',
      }),

      badgeColor: 'gold',
      overlay: serverInfo && (
        <div>
          <div>
            {formatMessage(
              {
                id: 'ocp-express.Resource.Component.Block.UnitServerinfounitcount',
              },

              { serverInfoUnitCount: serverInfo.unitCount }
            )}
          </div>
          <div>
            {formatMessage(
              {
                id: 'ocp-express.Resource.Component.Block.UnitNotAssociatedWithTenant',
              },

              { serverInfoUnusedUnitCount: serverInfo.unusedUnitCount }
            )}
          </div>
        </div>
      ),
    },

    {
      value: 'replica',
      label: formatMessage({
        id: 'ocp-express.Resource.Component.Block.NumberOfReplicas',
        defaultMessage: '副本数目',
      }),

      badgeColor: 'gold',
      overlay: serverInfo && (
        <div>
          <div style={{ color: 'rgba(0,0,0,0.45)' }}>
            {formatMessage({
              id: 'ocp-express.Resource.Component.Block.ClickTheHostCardTo',
              defaultMessage: '鼠标点击主机卡片可停止或启动 OBServer',
            })}
          </div>
          <Divider style={{ margin: '8px -12px', width: 'calc(100% + 24px)' }} />
          <div>{`Server IP: ${serverInfo.serverIp}`}</div>
          <div>
            {formatMessage(
              {
                id: 'ocp-express.Resource.Component.Block.PrimaryReplicasServerinfoleaderreplicacount',
                defaultMessage: '主副本数：{serverInfoLeaderReplicaCount}',
              },

              { serverInfoLeaderReplicaCount: serverInfo.leaderReplicaCount }
            )}
          </div>
          <div>
            {formatMessage(
              {
                id: 'ocp-express.Resource.Component.Block.TotalNumberOfReplicasServerinfototalreplicacount',
                defaultMessage:
                  '副本总数：{serverInfoTotalReplicaCount}（其中全功能型 {serverInfoFullReplicaCount}、只读型 {serverInfoReadonlyReplicaCount}、日志型 {serverInfoLogonlyReplicaCount}）',
              },

              {
                serverInfoTotalReplicaCount: serverInfo.totalReplicaCount,
                serverInfoFullReplicaCount: serverInfo.fullReplicaCount,
                serverInfoReadonlyReplicaCount: serverInfo.readonlyReplicaCount,
                serverInfoLogonlyReplicaCount: serverInfo.logonlyReplicaCount,
              }
            )}
          </div>
          <div>
            {formatMessage(
              {
                id: 'ocp-express.Resource.Component.Block.MaximumNumberOfStandaloneReplicas',
                defaultMessage: '单机副本数上限：{serverInfoMaxReplicaLimitCount}',
              },

              { serverInfoMaxReplicaLimitCount: serverInfo.maxReplicaLimitCount }
            )}
          </div>
        </div>
      ),
    },

    {
      value: 'memory',
      label: formatMessage({
        id: 'ocp-express.Resource.Component.Block.AllocatedMemory',
        defaultMessage: '已分配内存',
      }),

      getDetail: () =>
        `${byte2GB(serverInfo?.memorySizeAssignedByte || 0)}/${byte2GB(
          serverInfo?.totalMemorySizeByte || 0
        )}G`,
      percentField: 'memoryAssignedPercent',
      badgeColor: '',
    },

    {
      value: 'cpu',
      label: formatMessage({
        id: 'ocp-express.Resource.Component.Block.AllocatedCpu',
        defaultMessage: '已分配 CPU',
      }),

      getDetail: () =>
        serverInfo &&
        formatMessage(
          {
            id: 'ocp-express.Resource.Component.Block.ServerinfocpucountassignedServerinfototalcpucountCores',
          },

          {
            serverInfoCpuCountAssigned: serverInfo.cpuCountAssigned,
            serverInfoTotalCpuCount: serverInfo.totalCpuCount,
          }
        ),

      percentField: 'cpuAssignedPercent',
      badgeColor: '',
    },

    {
      value: 'disk',
      label: formatMessage({
        id: 'ocp-express.Resource.Component.Block.UsedDisks',
        defaultMessage: '已使用磁盘',
      }),

      getDetail: () =>
        `${byte2GB(serverInfo?.diskUsedByte || 0)}/${byte2GB(serverInfo?.totalDiskSizeByte || 0)}G`,
      percentField: 'diskUsedPercent',
      badgeColor: '',
    },
  ];

  const typeItem = findByValue(typeList, type);

  return (
    <MouseTooltip
      // 目标不为空且设置了 overlay 的才展示 tooltip
      visible={!isNullValue(target) && !!typeItem.overlay}
      overlay={typeItem.overlay}
      style={{
        fontSize: 12,
        lineHeight: '20px',
        padding: '8px 12px',
      }}
    >
      <div
        onClick={() => {
          // 是具体某个租户的 Block、且有该租户的读权限、租户的状态还得不为 创建中 和 删除中，才允许点击跳转
          if (
            type === 'tenant' &&
            tenantInfo &&
            ['CREATING', 'DELETING'].includes(tenantInfo.status) === false
          ) {
            directTo(`/tenant/${tenantInfo?.obTenantId}`);
          }
        }}
        // 当 target 是 Unit 数目时，可能是数字 0，需要用 isNullValue 来判空
        // 对于无权限访问的租户，hover 时需要重置鼠标样式和字体颜色
        className={`${styles.container} ${!isNullValue(target) || serverInfo ? styles[`${type}WithTarget`] : styles[type]
          } ${type === 'tenant' && tenantInfo ? styles.tenantWithTargetForNoAuth : ''} ${className}`}
        {...restProps}
      >
        {serverInfo && ['memory', 'cpu', 'disk'].includes(type) ? (
          <div className={styles.metricWrapper}>
            <div className={styles.metricHeader}>
              <div>{typeItem && typeItem.getDetail && typeItem.getDetail()}</div>
              <div className={styles.metricPercent}>
                <Text ellipsis={true}>
                  {serverInfo && typeItem.percentField && `${serverInfo[typeItem.percentField]}%`}
                </Text>
              </div>
            </div>
            <Progress
              size="small"
              percent={
                (serverInfo && typeItem.percentField && serverInfo[typeItem.percentField]) || 0
              }
              showInfo={false}
              style={{ width: '100%' }}
            />
          </div>
        ) : /**
         * 当 target 是 Unit 数目时，可能是数字 0，需要用 isNullValue 来判空
         */
          !isNullValue(target) ? (
            typeItem.badgeColor ? (
              <Badge color={typeItem.badgeColor} text={target} />
            ) : (
              <Text ellipsis={true}>{target}</Text>
            )
          ) : (
            <Space>
              <img src={`/assets/unit/${type}.svg`} alt="" />
              <Text ellipsis={true} style={{ display: 'inline-flex', alignItems: 'center' }}>
                {typeItem.label}
                {typeItem.tooltip && (
                  <Tooltip title={typeItem.tooltip} placement="right">
                    <QuestionCircleOutlined
                      style={{ color: 'rgba(0, 0, 0, 0.45)', marginLeft: '4px', fontSize: '14px' }}
                    />
                  </Tooltip>
                )}
              </Text>
            </Space>
          )}
      </div>
    </MouseTooltip>
  );
};

export default Block;
