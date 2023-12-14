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
import { Dropdown, Empty, Menu, Modal } from '@oceanbase/design';
import { isNullValue, byte2GB } from '@oceanbase/util';
import MouseTooltip from '@/component/MouseTooltip';
// import { useRequest } from 'ahooks';
// import * as ObUnitController from '@/service/custom/ObUnitController';
// import { UNIT_MIGRATE_TYPE_LIST } from '@/constant/oceanbase';
import useStyles from './UnitBlock.style';

export interface UnitBlockProps extends React.HTMLProps<HTMLDivElement> {
  clusterId: number;
  unitInfo?: API.ClusterUnitViewOfUnit;
  onSuccess?: () => void;
  className?: string;
  style?: React.CSSProperties;
}

const UnitBlock: React.FC<UnitBlockProps> = ({
  clusterId,
  unitInfo,
  onSuccess,
  className,
  style,
  ...restProps
}) => {
  const { styles } = useStyles();
  // const migrateTypeItem = findByValue(UNIT_MIGRATE_TYPE_LIST, unitInfo?.migrateType);
  // const migrateTarget = migrateTypeItem.migrateLabel
  //   ? `${migrateTypeItem.migrateLabel}: ${unitInfo?.migrateSvrIp} `
  //   : '';
  // const migrateType = unitInfo?.manualMigrate
  //   ? formatMessage({ id: 'ocp-express.Resource.Component.UnitBlock.ManuallyInitiated' })
  //   : formatMessage({
  //       id: 'ocp-express.Resource.Component.UnitBlock.ClusterAutomaticLoadBalancing',
  //     });
  // const migrateText = unitInfo?.migrateType !== 'NOT_IN_MIGRATE' ? `${migrateType} ` : '';
  // // unitInfo
  const maxMemoryAssigned = byte2GB(unitInfo?.maxMemoryAssignedByte || 0);

  // 回滚 Unit 迁移
  // const { runAsync: rollbackMigrateUnit } = useRequest(ObUnitController.rollbackMigrateUnit, {
  //   manual: true,
  //   onSuccess: res => {
  //     if (res.successful) {
  //       message.success(
  //         formatMessage({
  //           id: 'ocp-express.Resource.Component.UnitBlock.TheMigrationTaskOfThe',
  //           defaultMessage: '回滚 Unit 迁移的任务发起成功',
  //         })
  //       );

  //       if (onSuccess) {
  //         onSuccess();
  //       }
  //     }
  //   },
  // });

  return (
    <MouseTooltip
      visible={!!unitInfo}
      style={{
        fontSize: 12,
        lineHeight: '20px',
        padding: '8px 12px',
      }}
      overlay={
        <div>
          {/* <div
            style={{
              color: migrateTypeItem.color,
            }}
          >
            <div>
              {formatMessage(
                {
                  id: 'ocp-express.Resource.Component.UnitBlock.StatusMigratetypeitemlabelMigratetext',
                  defaultMessage: '状态：{migrateTypeItemLabel}{migrateText}',
                },

                { migrateTypeItemLabel: migrateTypeItem.label, migrateText }
              )}
            </div>
            <div>{migrateTarget}</div>
            {migrateTypeItem.extra && <div>{migrateTypeItem.extra}</div>}
          </div> */}
          {/* <Divider style={{ margin: '8px -12px', width: 'calc(100% + 24px)' }} /> */}
          {unitInfo && (
            <div className={styles.content}>
              <div>{`Unit ID: ${unitInfo?.obUnitId}`}</div>
              <div>
                {formatMessage(
                  {
                    id: 'ocp-express.Resource.Component.UnitBlock.TenantNameUnitinfotenantname',
                  },
                  { unitInfoTenantName: unitInfo.tenantName }
                )}
              </div>
              <div>
                {formatMessage(
                  {
                    id: 'ocp-express.Resource.Component.UnitBlock.ResourcePoolUnitinforesourcepoolname',
                  },
                  { unitInfoResourcePoolName: unitInfo.resourcePoolName }
                )}
              </div>
              {/* Popover 里就展示 Unit 规格的原本名称 */}
              <div>
                {formatMessage(
                  {
                    id: 'ocp-express.Resource.Component.UnitBlock.SpecificationUnitinfounitconfig',
                  },
                  { unitInfoUnitConfig: unitInfo.unitConfig }
                )}
              </div>
              <div>
                {formatMessage(
                  {
                    id: 'ocp-express.Resource.Component.UnitBlock.CpuCoresUnitinfomincpuassignedcountToUnitinfomaxcpuassignedcount',
                  },
                  {
                    unitInfoMaxCpuAssignedCount: unitInfo.maxCpuAssignedCount,
                  }
                )}
              </div>
              <div>
                {formatMessage(
                  {
                    id: 'ocp-express.Resource.Component.UnitBlock.MemoryGbMinmemoryassignedToMaxmemoryassigned',
                  },
                  {
                    maxMemoryAssigned,
                  }
                )}
              </div>
              <div>
                {formatMessage({
                  id: 'ocp-express.Resource.Component.UnitBlock.UsedDisksGb',
                  defaultMessage: '已使用磁盘（GB）：',
                })}
                {isNullValue(unitInfo.diskUsedByte)
                  ? '-'
                  : unitInfo.diskUsedByte && byte2GB(unitInfo.diskUsedByte)}
              </div>
            </div>
          )}
        </div>
      }
    >
      <Dropdown
        trigger={['contextMenu']}
        {...(unitInfo?.migrateType === 'MIGRATE_IN' || unitInfo?.migrateType === 'MIGRATE_OUT'
          ? {}
          : {
              // 只有迁入中和迁出中 Unit 才支持右键取消迁移
              visible: false,
            })}
        overlay={
          <Menu>
            <Menu.Item
              key="cancel"
              onClick={() => {
                Modal.confirm({
                  title: formatMessage({
                    id: 'ocp-express.Resource.Component.UnitBlock.AreYouSureYouWant',
                    defaultMessage: '确定回滚正在迁移的 Unit 吗？',
                  }),

                  content: unitInfo?.manualMigrate
                    ? null
                    : formatMessage({
                        id: 'ocp-express.Resource.Component.UnitBlock.AfterTheUnitMigrationIs',
                        defaultMessage:
                          '集群自动负载均衡执行的 Unit 迁移，在被手动回滚后，集群依然可能会再次自动执行同样的 Unit 迁移进行负载均衡',
                      }),

                  onOk: () => {
                    return rollbackMigrateUnit({
                      id: clusterId,
                      obUnitId: unitInfo?.obUnitId,
                    });
                  },
                });
              }}
            >
              <span>
                {formatMessage({
                  id: 'ocp-express.Resource.Component.UnitBlock.CancelMigration',
                  defaultMessage: '取消迁移',
                })}
              </span>
            </Menu.Item>
          </Menu>
        }
      >
        <div
          className={`${styles.container} : ''
            } ${className}`}
          style={{
            // 设置左边框的样式
            // backgroundColor: migrateTypeItem.backgroundColor,
            // 仅正常状态的 Unit 可点击
            // cursor:
            //   unitInfo?.migrateType &&
            //     ['NOT_IN_MIGRATE', 'MIGRATE_IN', 'MIGRATE_OUT'].includes(unitInfo?.migrateType)
            //     ? 'pointer'
            //     : 'default',
            ...style,
          }}
          {...restProps}
        >
          {unitInfo ? (
            // 展示 Unit 规格的简写，模式为 3C12G，由后端取 maxCpuCount 和 maxMemorySizeGb 拼接而成
            <div>{unitInfo?.unitConfig}</div>
          ) : (
            <Empty description={false} image={Empty.PRESENTED_IMAGE_SIMPLE} />
          )}
        </div>
      </Dropdown>
    </MouseTooltip>
  );
};

export default UnitBlock;
