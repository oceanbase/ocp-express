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
import { Form, Row, Col, InputNumber, Descriptions, Modal } from '@oceanbase/design';
import { isEqual } from 'lodash';
import { findBy } from '@oceanbase/util';
import { useRequest } from 'ahooks';
import * as ObTenantController from '@/service/ocp-express/ObTenantController';
import { MODAL_FORM_ITEM_LAYOUT } from '@/constant';
import { REPLICA_TYPE_LIST } from '@/constant/oceanbase';
import { getUnitSpecLimit } from '@/util/cluster';
import { taskSuccess } from '@/util/task';
import MySelect from '@/component/MySelect';

const { Option } = MySelect;

export interface AddReplicaModalProps {
  dispatch: any;
  visible: boolean;
  clusterId: number;
  tenantId: number;
  tenantData: API.TenantInfo;
  clusterZones: API.Zone[];
  unitSpecLimit: any;
  onSuccess: () => void;
}

const AddReplicaModal: React.FC<AddReplicaModalProps> = ({
  dispatch,
  visible,
  clusterId,
  tenantId,
  tenantData,
  clusterZones,
  unitSpecLimit,
  onSuccess,
  ...restProps
}) => {
  const [form] = Form.useForm();
  const { validateFields, getFieldValue } = form;
  const [resourcePool, setResourcePool] = useState(undefined);

  const tenantZoneNames = (tenantData.zones || []).map(item => item.name);
  // 已经设置了副本的 zone，不可重复设置
  const unsetZones = clusterZones.filter(item => tenantZoneNames.indexOf(item.name) === -1);

  useEffect(() => {
    if (visible && tenantData?.zones?.length > 0) {
      const currnetResourcePool: API.ResourcePool = tenantData?.zones[0]?.resourcePool;
      const currnetUnitConfig: API.UnitConfig = currnetResourcePool?.unitConfig;
      const unitConfigList: API.Zone[] = tenantData?.zones?.filter(item =>
        isEqual(item?.resourcePool?.unitConfig, currnetUnitConfig)
      );

      if (unitConfigList.length === tenantData.zones?.length) {
        setResourcePool({
          cpuCore: currnetUnitConfig?.maxCpuCoreCount,
          memorySize: currnetUnitConfig?.maxMemorySize,
          unitCount: currnetResourcePool?.unitCount,
        });
      }
    }
  }, [visible]);

  const { run: addReplica, loading: addReplicaLoading } = useRequest(
    ObTenantController.addReplica,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          const taskId = res.data?.id;
          taskSuccess({
            taskId,
            message: formatMessage({
              id: 'ocp-express.src.model.tenant.TheTaskForAddingA',
              defaultMessage: '新增副本的任务提交成功',
            }),
          });

          if (onSuccess) {
            onSuccess();
          }
          dispatch({
            type: 'tenant/getTenantData',
            payload: {
              tenantId: tenantData.obTenantId,
            },
          });

          dispatch({
            type: 'task/update',
            payload: {
              runningTaskListDataRefreshDep: taskId,
            },
          });
        }
      },
    }
  );

  const handleSubmit = () => {
    validateFields().then(values => {
      const { zoneName, replicaType, cpuCore, memorySize } = values;
      addReplica(
        {
          tenantId: tenantData.obTenantId,
        },

        [
          {
            zoneName,
            replicaType,
            resourcePool: {
              unitSpec: {
                cpuCore,
                memorySize,
              },
              unitCount: resourcePool?.unitCount,
            },
          },
        ]
      );
    });
  };

  const currentZoneName = getFieldValue('zoneName');

  let idleCpuCore, idleMemoryInBytes;
  if (unsetZones?.length > 0) {
    const zoneData = findBy(unsetZones, 'name', currentZoneName);
    if (zoneData && zoneData?.servers?.length > 0 && zoneData?.servers[0]?.stats) {
      const { idleCpuCoreTotal, idleMemoryInBytesTotal } = getUnitSpecLimit(
        zoneData?.servers[0]?.stats
      );

      idleCpuCore = idleCpuCoreTotal;
      idleMemoryInBytes = idleMemoryInBytesTotal;
    }
  }

  const { cpuLowerLimit, memoryLowerLimit } = unitSpecLimit;

  return (
    <Modal
      visible={visible}
      title={formatMessage({
        id: 'ocp-express.Detail.Component.AddReplicaModal.NewCopy',
        defaultMessage: '新增副本',
      })}
      destroyOnClose={true}
      confirmLoading={addReplicaLoading}
      onOk={handleSubmit}
      {...restProps}
    >
      <Descriptions column={1}>
        <Descriptions.Item
          label={formatMessage({
            id: 'ocp-express.Detail.Component.AddReplicaModal.Tenant',
            defaultMessage: '租户',
          })}
        >
          {tenantData.name}
        </Descriptions.Item>
      </Descriptions>
      <Form form={form} preserve={false} layout="vertical" hideRequiredMark={true}>
        <Form.Item
          {...MODAL_FORM_ITEM_LAYOUT}
          label={formatMessage({
            id: 'ocp-express.Detail.Component.AddReplicaModal.TargetZone',
            defaultMessage: '目标 Zone',
          })}
          name="zoneName"
          rules={[
            {
              required: true,
              message: formatMessage({
                id: 'ocp-express.Detail.Component.AddReplicaModal.SelectAZone',
                defaultMessage: '请选择 Zone',
              }),
            },
          ]}
        >
          <MySelect showSearch={true}>
            {unsetZones.map(item => (
              <Option key={item.name} value={item.name}>
                {item.name}
              </Option>
            ))}
          </MySelect>
        </Form.Item>
        <Form.Item
          {...MODAL_FORM_ITEM_LAYOUT}
          label={formatMessage({
            id: 'ocp-express.Detail.Component.AddReplicaModal.CopyType',
            defaultMessage: '副本类型',
          })}
          name="replicaType"
          initialValue="FULL"
          rules={[
            {
              required: true,
              message: formatMessage({
                id: 'ocp-express.Detail.Component.AddReplicaModal.PleaseSelectTheCopyType',
                defaultMessage: '请选择副本类型',
              }),
            },
          ]}
        >
          <MySelect>
            {REPLICA_TYPE_LIST.map(item => (
              <Option key={item.value} value={item.value}>
                {item.label}
              </Option>
            ))}
          </MySelect>
        </Form.Item>
        <Row gutter={8}>
          <Col span={9} style={{ paddingRight: 8 }}>
            <Form.Item
              label={formatMessage({
                id: 'ocp-express.Detail.Component.AddReplicaModal.UnitSpecification',
                defaultMessage: 'Unit 规格',
              })}
              name="cpuCore"
              initialValue={resourcePool?.cpuCore}
              extra={
                cpuLowerLimit &&
                idleCpuCore &&
                formatMessage(
                  {
                    id: 'ocp-express.Detail.Component.AddReplicaModal.CurrentConfigurableRangeValueCpulowerlimitIdlecpucore',
                    defaultMessage: '当前可配置范围值 {cpuLowerLimit}~{idleCpuCore}',
                  },
                  { cpuLowerLimit: cpuLowerLimit, idleCpuCore: idleCpuCore }
                )
              }
              rules={[
                {
                  required: true,
                  message: formatMessage({
                    id: 'ocp-express.Detail.Component.AddReplicaModal.EnterTheUnitSpecification',
                    defaultMessage: '请输入 unit 规格',
                  }),
                },
              ]}
            >
              <InputNumber
                addonAfter={formatMessage({
                  id: 'ocp-express.Detail.Component.AddReplicaModal.Nuclear',
                  defaultMessage: '核',
                })}
                step={0.5}
                min={cpuLowerLimit || 0.5}
                max={idleCpuCore}
              />
            </Form.Item>
          </Col>
          <Col span={9}>
            <Form.Item
              label=" "
              name="memorySize"
              initialValue={resourcePool?.memorySize}
              extra={
                memoryLowerLimit &&
                idleMemoryInBytes &&
                formatMessage(
                  {
                    id: 'ocp-express.Detail.Component.AddReplicaModal.CurrentConfigurableRangeValueMemorylowerlimitIdlememoryinbytes',
                    defaultMessage: '当前可配置范围值 {memoryLowerLimit}~{idleMemoryInBytes}',
                  },
                  { memoryLowerLimit: memoryLowerLimit, idleMemoryInBytes: idleMemoryInBytes }
                )
              }
              rules={[
                {
                  required: true,
                  message: formatMessage({
                    id: 'ocp-express.Detail.Component.AddReplicaModal.EnterTheUnitSpecification',
                    defaultMessage: '请输入 unit 规格',
                  }),
                },
              ]}
            >
              <InputNumber addonAfter="GB" min={memoryLowerLimit} max={idleMemoryInBytes} />
            </Form.Item>
          </Col>
        </Row>
      </Form>
      <Descriptions column={1}>
        <Descriptions.Item
          label={formatMessage({
            id: 'ocp-express.Detail.Component.AddReplicaModal.NumberOfUnits',
            defaultMessage: 'Unit 数量',
          })}
        >
          {resourcePool?.unitCount}
        </Descriptions.Item>
      </Descriptions>
    </Modal>
  );
};

export default AddReplicaModal;
