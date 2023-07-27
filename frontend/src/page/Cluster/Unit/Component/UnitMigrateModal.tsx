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

import MySelect from '@/component/MySelect';
import { MODAL_HORIZONTAL_FORM_ITEM_LAYOUT } from '@/constant';
import { UNIT_MIGRATE_TYPE_LIST } from '@/constant/oceanbase';
import * as ObUnitController from '@/service/custom/ObUnitController';
import { isEnglish } from '@/util';
import { formatMessage } from '@/util/intl';
import { Button, Divider, Form, Popover, Space, Modal, message } from '@oceanbase/design';
import React, { useEffect } from 'react';
import { find } from 'lodash';
import { byte2GB, findBy, findByValue, formatNumber, isNullValue } from '@oceanbase/util';
import { useRequest } from 'ahooks';
import type { ModalProps } from 'antd/es/modal';

const { Option } = MySelect;

export interface UnitMigrateModalProps extends ModalProps {
  clusterId: number;
  unitInfo?: API.ClusterUnitViewOfUnit;
  serverInfos?: API.ClusterUnitViewOfServer[];
  onSuccess?: () => void;
  className?: string;
}

const UnitMigrateModal: React.FC<UnitMigrateModalProps> = ({
  clusterId,
  unitInfo,
  serverInfos = [],
  onCancel,
  onSuccess,
  ...restProps
}) => {
  const [form] = Form.useForm();
  const { validateFields } = form;
  // 获取可迁移的目标端列表
  const {
    run: getUnitMigrateDestination,
    data: migrateDestinationData,
    loading: migrateDestinationDataLoading,
  } = useRequest(ObUnitController.getUnitMigrateDestination, {
    manual: true,
  });

  // 获取已使用磁盘
  const { data, run: getUnitStats } = useRequest(ObUnitController.getUnitStats, {
    manual: true,
  });

  const diskUsedByte = data?.data?.diskUsedByte || 0;

  // TODO: 由于 useRequest 的 ready 存在问题，先用 useEffect 来模拟，升级到 ahooks 3.x 可解决该问题
  useEffect(() => {
    if (!isNullValue(unitInfo?.obUnitId)) {
      getUnitMigrateDestination({
        id: clusterId,
        obUnitId: unitInfo?.obUnitId,
      });

      getUnitStats({
        id: clusterId,
        obUnitId: unitInfo?.obUnitId,
        serverIp: unitInfo?.serverIp,
      });
    }
  }, [unitInfo?.obUnitId]);

  const migrateDestinationList =
    migrateDestinationData?.data?.migrateDestinations || ([] as API.ObServerEntity[]);

  // 迁移 Unit
  const { run: migrateUnit, loading: migrateUnitLoading } = useRequest(
    ObUnitController.migrateUnit,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage({
              id: 'ocp-express.Resource.Component.UnitMigrateModal.UnitMigrationTaskInitiated',
              defaultMessage: 'Unit 迁移任务发起成功',
            })
          );

          if (onSuccess) {
            onSuccess();
          }
        }
      },
    }
  );

  function hanelSubmit() {
    validateFields().then(values => {
      const { destinationServerId } = values;
      const destinationServer = find(
        migrateDestinationList,
        item => item.id === destinationServerId
      );

      migrateUnit(
        {
          id: clusterId,
          obUnitId: unitInfo?.obUnitId,
        },

        {
          obUnitId: unitInfo?.obUnitId,
          destinationServerId,
          destinationAddress: destinationServer?.ip,
        }
      );
    });
  }

  return (
    <Modal
      title={formatMessage({
        id: 'ocp-express.Resource.Component.UnitMigrateModal.UnitMigration',
        defaultMessage: 'Unit 迁移',
      })}
      destroyOnClose={true}
      confirmLoading={migrateUnitLoading}
      className="form-with-small-margin"
      // 为了实现迁移操作的权限控制，需要自定义 footer
      footer={
        <Space>
          <Button
            data-aspm-click="c318542.d343266"
            data-aspm-desc="Unit 迁移-取消"
            data-aspm-param={``}
            data-aspm-expo
            onClick={e => {
              if (onCancel) {
                onCancel(e);
              }
            }}
          >
            {formatMessage({
              id: 'ocp-express.Resource.Component.UnitMigrateModal.Cancel',
              defaultMessage: '取消',
            })}
          </Button>

          <Button
            data-aspm-click="c318542.d343267"
            data-aspm-desc="Unit 迁移-提交"
            data-aspm-param={``}
            data-aspm-expo
            type="primary"
            onClick={hanelSubmit}
          >
            {formatMessage({
              id: 'ocp-express.Resource.Component.UnitMigrateModal.Determine',
              defaultMessage: '确定',
            })}
          </Button>
        </Space>
      }
      // 需要显式设置 onCancel
      onCancel={onCancel}
      {...restProps}
    >
      <Form
        form={form}
        preserve={false}
        requiredMark={false}
        layout="horizontal"
        {...(isEnglish()
          ? {
            labelCol: {
              span: 9,
            },
            wrapperCol: {
              span: 15,
            },
          }
          : MODAL_HORIZONTAL_FORM_ITEM_LAYOUT)}
      >
        <Form.Item label="Unit ID">{unitInfo?.obUnitId}</Form.Item>
        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Resource.Component.UnitMigrateModal.TenantName',
            defaultMessage: '租户名',
          })}
        >
          {unitInfo?.tenantName}
        </Form.Item>
        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Resource.Component.UnitMigrateModal.ResourceSpecifications',
            defaultMessage: '资源规格',
          })}
        >
          {unitInfo?.unitConfig}
        </Form.Item>
        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Resource.Component.UnitMigrateModal.SourceEndpoint',
            defaultMessage: '源端地址',
          })}
        >
          {unitInfo?.serverIp}
        </Form.Item>
        <Form.Item
          name="destinationServerId"
          label={formatMessage({
            id: 'ocp-express.Resource.Component.UnitMigrateModal.DestinationAddress',
            defaultMessage: '目标端地址',
          })}
          extra={formatMessage({
            id: 'ocp-express.Resource.Component.UnitMigrateModal.FilteredExistingTenantResourcesOr',
            defaultMessage: '已过滤已有租户资源或资源不足的目标端',
          })}
          rules={[
            {
              required: true,
              message: formatMessage({
                id: 'ocp-express.Resource.Component.UnitMigrateModal.SelectADestinationEndpoint',
                defaultMessage: '请选择目标端地址',
              }),
            },
          ]}
        >
          <MySelect
            loading={migrateDestinationDataLoading}
            showSearch={true}
            optionFilterProp="label"
          >
            {migrateDestinationList?.map(item => {
              const label = `${item.ip}:${item.port}`;
              // 通过 IP 去做筛选
              const serverInfo = findBy(serverInfos, 'serverIp', item.ip);
              /**
               * 以下计算结果都需要用 formatNumber 包一层，避免浮点数运算带来的小数问题
               * */
              // CPU
              const currentCpu = formatNumber(
                (serverInfo.totalCpuCount || 0) - (serverInfo.cpuCountAssigned || 0)
              );

              // const minCpu = formatNumber(currentCpu - (unitInfo?.maxCpuAssignedCount || 0));
              const maxCpu = formatNumber(currentCpu - (unitInfo?.minCpuAssignedCount || 0));
              // 内存
              const currentMemory = formatNumber(
                byte2GB(serverInfo.totalMemorySizeByte || 0) -
                byte2GB(serverInfo.memorySizeAssignedByte || 0)
              );

              // const minMemory = formatNumber(
              //   currentMemory - byte2GB(unitInfo?.maxMemoryAssignedByte || 0)
              // );

              const maxMemory = formatNumber(
                currentMemory - byte2GB(unitInfo?.minMemoryAssignedByte || 0)
              );

              // 磁盘
              const currentDisk = formatNumber(
                byte2GB(serverInfo.totalDiskSizeByte || 0) - byte2GB(serverInfo.diskUsedByte || 0)
              );

              // ob 3.x 以前，查 unit 的磁盘使用非常耗时，所以 unit 视图接口不会返回 unit 磁盘信息，需要单独请求获得
              const disk = formatNumber(currentDisk - byte2GB(diskUsedByte || 0));

              return (
                <Option key={item.id} value={item.id} label={label}>
                  <Popover
                    placement="topLeft"
                    content={
                      <div style={{ width: 300, fontSize: 12, lineHeight: '20px' }}>
                        {unitInfo && (
                          <div>
                            <div>{`Unit ID: ${unitInfo.obUnitId}`}</div>
                            <div>
                              {formatMessage(
                                {
                                  id: 'ocp-express.Resource.Component.UnitMigrateModal.TenantNameUnitinfotenantname',
                                },
                                { unitInfoTenantName: unitInfo.tenantName }
                              )}
                            </div>
                            <div>
                              {formatMessage(
                                {
                                  id: 'ocp-express.Resource.Component.UnitMigrateModal.StatusFindbyvalueunitmigrate',
                                  defaultMessage: '状态：{findByValueUNITMIGRATE}',
                                },

                                {
                                  findByValueUNITMIGRATE: findByValue(
                                    UNIT_MIGRATE_TYPE_LIST,
                                    unitInfo?.migrateType
                                  ).label,
                                }
                              )}
                            </div>
                            <div>
                              {formatMessage(
                                {
                                  id: 'ocp-express.Resource.Component.UnitMigrateModal.SpecificationUnitinfounitconfig',
                                },
                                { unitInfoUnitConfig: unitInfo.unitConfig }
                              )}
                            </div>
                            <div>
                              {formatMessage(
                                {
                                  id: 'ocp-express.Resource.Component.UnitMigrateModal.ResourcePoolUnitinforesourcepoolname',
                                },
                                { unitInfoResourcePoolName: unitInfo.resourcePoolName }
                              )}
                            </div>
                          </div>
                        )}
                        <Divider style={{ margin: '8px 0' }} />
                        <div>
                          <div>
                            {formatMessage({
                              id: 'ocp-express.Resource.Component.UnitMigrateModal.AfterMigration',
                              defaultMessage: '迁移后',
                            })}
                          </div>
                          <div>
                            <span>
                              {formatMessage(
                                {
                                  id: 'ocp-express.Resource.Component.UnitMigrateModal.CpuCoreCurrentcpu',
                                  defaultMessage: 'CPU（核）：{currentCpu} ',
                                },

                                { currentCpu }
                              )}
                            </span>
                            <span style={{ color: 'rgba(0,0,0,0.45)' }}>{maxCpu}</span>
                          </div>
                          <div>
                            <span>
                              {formatMessage(
                                {
                                  id: 'ocp-express.Resource.Component.UnitMigrateModal.MemoryGbCurrentmemory',
                                  defaultMessage: '内存（GB）：{currentMemory} ',
                                },

                                { currentMemory }
                              )}
                            </span>
                            <span style={{ color: 'rgba(0,0,0,0.45)' }}>{maxMemory}</span>
                          </div>
                          <div>
                            <span>
                              {formatMessage(
                                {
                                  id: 'ocp-express.Resource.Component.UnitMigrateModal.DiskGbCurrentdisk',
                                  defaultMessage: '磁盘（GB）：{currentDisk} ',
                                },

                                { currentDisk }
                              )}
                            </span>
                            <span style={{ color: 'rgba(0,0,0,0.45)' }}>{`-> ${disk}`}</span>
                          </div>
                        </div>
                      </div>
                    }
                  >
                    <div>{label}</div>
                  </Popover>
                </Option>
              );
            })}
          </MySelect>
        </Form.Item>
      </Form>
    </Modal>
  );
};
export default UnitMigrateModal;
