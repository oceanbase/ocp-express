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
import { useDispatch } from 'umi';
import React, { useEffect, useState } from 'react';
import { Form, InputNumber, Row, Col, Descriptions, Modal } from '@oceanbase/design';
import { isEqual, minBy, find } from 'lodash';
import type { ModalProps } from '@oceanbase/design/es/modal';
import { useRequest } from 'ahooks';
import { taskSuccess } from '@/util/task';
import { getUnitSpecLimit } from '@/util/cluster';
import * as ObTenantController from '@/service/ocp-express/ObTenantController';
import { getMinServerCount } from '@/util/tenant';

export interface BatcModifyUnitModalProps extends ModalProps {
  tenantData?: API.TenantInfo;
  tenantZones?: API.TenantZone[];
  clusterZones: API.Zone[];
  unitSpecLimit: any;
  visible?: boolean;
  onSuccess: () => void;
  onCancel: () => void;
}

const BatchModifyUnitModal: React.FC<BatcModifyUnitModalProps> = ({
  tenantData,
  tenantZones,
  clusterZones,
  unitSpecLimit,
  onSuccess,
  onCancel,
  visible,
  ...restProps
}) => {
  const dispatch = useDispatch();
  const [form] = Form.useForm();
  const { validateFields } = form;

  const [resourcePool, setResourcePool] = useState(undefined);

  const minServerCount = getMinServerCount(clusterZones);

  useEffect(() => {
    setUnitSpecs();
  }, [visible]);

  const setUnitSpecs = () => {
    if (visible && tenantData?.zones?.length > 0) {
      const currnetResourcePool: API.ResourcePool = tenantData?.zones[0]?.resourcePool;
      const currnetUnitConfig: API.UnitConfig = currnetResourcePool?.unitConfig;
      const unitConfigList: API.Zone[] = tenantData?.zones?.filter(item =>
        isEqual(item?.resourcePool?.unitConfig, currnetUnitConfig)
      );

      if (unitConfigList.length === tenantData.zones?.length) {
        setResourcePool({
          unitCount: currnetResourcePool?.unitCount,
          cpuCore: currnetUnitConfig?.maxCpuCoreCount,
          memorySize: currnetUnitConfig?.maxMemorySize,
        });
      }
    }
  };

  const { run, loading } = useRequest(ObTenantController.modifyReplica, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        const taskId = res.data?.id;
        taskSuccess({
          taskId,
          message: formatMessage({
            id: 'ocp-express.Detail.Component.BatchModifyUnitModal.TheTaskOfModifyingUnitInBatchesHas',
            defaultMessage: '批量修改 Unit 的任务提交成功',
          }),
        });

        if (onSuccess) {
          onSuccess();
        }
        dispatch({
          type: 'tenant/getTenantData',
          payload: {
            tenantId: tenantData?.obTenantId,
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
  });

  let idleCpuCore, idleMemoryInBytes;
  if (clusterZones?.length > 0) {
    // 修改unit  计算当前 zone 内剩余资源可调整的最大值，取最小可用资源的 zone，提示规格调整的最大最小值配置
    const minIdleCpuZone = minBy(
      clusterZones?.map(
        zone =>
          zone?.servers?.length > 0 &&
          zone?.servers[0]?.stats &&
          getUnitSpecLimit(zone?.servers[0]?.stats)
      ),

      'idleCpuCoreTotal'
    );

    const minIdleMemoryZone = minBy(
      clusterZones?.map(
        zone =>
          zone?.servers?.length > 0 &&
          zone?.servers[0]?.stats &&
          getUnitSpecLimit(zone?.servers[0]?.stats)
      ),

      'idleMemoryInBytesTotal'
    );
    // 修改unit  当前已经分配资源最小zoen
    const minCpuCoreAssignedZone = minBy(
      clusterZones?.map(zone => zone?.servers?.length > 0 && zone?.servers[0]?.stats),

      'cpuCoreAssigned'
    )?.zone;

    const minMemoryInBytesAssignedZone = minBy(
      clusterZones?.map(zone => zone?.servers?.length > 0 && zone?.servers[0]?.stats),

      'memoryInBytesAssigned'
    )?.zone;

    const minCpuZone = find(tenantZones, item => item.name === minCpuCoreAssignedZone);

    const minMemoryZone = find(tenantZones, item => item.name === minMemoryInBytesAssignedZone);

    // 修改 unit 时，CUP可配置范围上限，当前 unit 已分配CUP + 剩余空闲CUP
    if (minIdleCpuZone && minCpuZone) {
      idleCpuCore =
        minIdleCpuZone?.idleCpuCoreTotal + minCpuZone?.resourcePool?.unitConfig?.maxCpuCoreCount;
    }

    // 修改 unit 时，可配置范围上限，当前 unit 已分配内存 + 剩余空闲内存
    if (minIdleMemoryZone && minMemoryZone) {
      idleMemoryInBytes =
        minIdleMemoryZone?.idleMemoryInBytesTotal +
        minMemoryZone?.resourcePool?.unitConfig?.maxMemorySize;
    }
  }
  const { cpuLowerLimit, memoryLowerLimit } = unitSpecLimit;

  return (
    <Modal
      title={formatMessage({
        id: 'ocp-express.Detail.Component.BatchModifyUnitModal.ModifyUnit',
        defaultMessage: '修改 Unit',
      })}
      visible={visible}
      destroyOnClose={true}
      confirmLoading={loading}
      onOk={() => {
        validateFields().then(values => {
          const { cpuCore, memorySize, unitCount } = values;
          run(
            {
              tenantId: tenantData?.obTenantId,
            },

            tenantZones?.map(item => {
              const { name, replicaType } = item;
              return {
                zoneName: name,
                replicaType,
                resourcePool: {
                  unitSpec: {
                    cpuCore,
                    memorySize,
                  },
                  unitCount,
                },
              };
            })
          );
        });
      }}
      onCancel={() => {
        onCancel();
      }}
      {...restProps}
    >
      <Descriptions column={2}>
        <Descriptions.Item
          label={formatMessage({
            id: 'ocp-express.Detail.Component.BatchModifyUnitModal.OperationObject',
            defaultMessage: '操作对象',
          })}
        >
          {tenantData?.name}
          {formatMessage({
            id: 'ocp-express.Detail.Component.BatchModifyUnitModal.AllZonesUnder',
            defaultMessage: '下的所有 Zone',
          })}
        </Descriptions.Item>
      </Descriptions>
      <Form form={form} preserve={false} layout="vertical" hideRequiredMark={true}>
        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Detail.Component.BatchModifyUnitModal.NumberOfUnits',
            defaultMessage: 'Unit 数量',
          })}
          name="unitCount"
          initialValue={resourcePool?.unitCount}
          extra={formatMessage(
            {
              id: 'ocp-express.Detail.Component.BatchModifyUnitModal.TheMaximumNumberOfCurrentKesheIsMinservercount',
              defaultMessage:
                '当前可设最大个数为 {minServerCount} (Zone 中最少 OBServer 数决定 Unit 可设最大个数)',
            },
            { minServerCount: minServerCount }
          )}
        >
          <InputNumber
            min={1}
            placeholder={formatMessage({
              id: 'ocp-express.Detail.Component.BatchModifyUnitModal.NullIndicatesThatTheNumberOfUnitsIs',
              defaultMessage: '为空表示不修改 Unit 数量',
            })}
            style={{ width: 168 }}
          />
        </Form.Item>
        <Row gutter={8}>
          <Col span={9} style={{ paddingRight: 8 }}>
            <Form.Item
              label={formatMessage({
                id: 'ocp-express.Detail.Component.BatchModifyUnitModal.UnitSpecification',
                defaultMessage: 'Unit 规格',
              })}
              name="cpuCore"
              initialValue={resourcePool?.cpuCore}
              extra={
                cpuLowerLimit &&
                idleCpuCore &&
                formatMessage(
                  {
                    id: 'ocp-express.Detail.Component.BatchModifyUnitModal.CurrentConfigurableRangeValueCpulowerlimitIdlecpucore',
                    defaultMessage: '当前可配置范围值 {cpuLowerLimit}~{idleCpuCore}',
                  },
                  { cpuLowerLimit: cpuLowerLimit, idleCpuCore: idleCpuCore }
                )
              }
              rules={[
                {
                  required: true,
                  message: formatMessage({
                    id: 'ocp-express.Detail.Component.BatchModifyUnitModal.EnterTheUnitSpecification',
                    defaultMessage: '请输入 unit 规格',
                  }),
                },
              ]}
            >
              <InputNumber
                addonAfter={formatMessage({
                  id: 'ocp-express.Detail.Component.BatchModifyUnitModal.Nuclear',
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
                    id: 'ocp-express.Detail.Component.BatchModifyUnitModal.CurrentConfigurableRangeValueMemorylowerlimitIdlememoryinbytes',
                    defaultMessage: '当前可配置范围值 {memoryLowerLimit}~{idleMemoryInBytes}',
                  },
                  { memoryLowerLimit: memoryLowerLimit, idleMemoryInBytes: idleMemoryInBytes }
                )
              }
              rules={[
                {
                  required: true,
                  message: formatMessage({
                    id: 'ocp-express.Detail.Component.BatchModifyUnitModal.EnterTheUnitSpecification',
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
    </Modal>
  );
};

export default BatchModifyUnitModal;
