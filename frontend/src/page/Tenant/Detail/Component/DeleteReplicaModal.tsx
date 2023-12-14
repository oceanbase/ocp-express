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
import { connect } from 'umi';
import React from 'react';
import { Alert, Descriptions, Modal } from '@oceanbase/design';
import type { ModalProps } from '@oceanbase/design/es/modal';
import { useRequest } from 'ahooks';
import * as ObTenantController from '@/service/ocp-express/ObTenantController';
import { taskSuccess } from '@/util/task';

export interface DeleteReplicaModalProps extends ModalProps {
  dispatch: any;
  tenantData: API.TenantInfo;
  tenantZones: API.TenantZone[];
  onSuccess: () => void;
}

const DeleteReplicaModal: React.FC<DeleteReplicaModalProps> = ({
  dispatch,
  tenantData,
  tenantZones = [],
  onSuccess,
  ...restProps
}) => {
  const { run: deleteReplica, loading } = useRequest(ObTenantController.deleteReplica, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        const taskId = res.data?.id;
        taskSuccess({
          taskId,
          message: formatMessage({
            id: 'ocp-express.src.model.tenant.TheTaskOfDeletingThe',
            defaultMessage: '删除副本的任务提交成功',
          }),
        });
        if (onSuccess) {
          onSuccess();
        }
        dispatch({
          type: 'tenant/getTenantData',
          payload: {
            id: tenantData.clusterId,
            tenantId: tenantData.id,
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

  return (
    <Modal
      title={formatMessage({
        id: 'ocp-express.Detail.Component.DeleteReplicaModal.AreYouSureYouWant',
        defaultMessage: '确定要删除副本吗？',
      })}
      destroyOnClose={true}
      confirmLoading={loading}
      onOk={() => {
        deleteReplica(
          {
            tenantId: tenantData.obTenantId,
          },
          tenantZones.map(item => ({
            zoneName: item.name as string,
          }))
        );
      }}
      okText={formatMessage({
        id: 'ocp-express.Detail.Component.DeleteReplicaModal.Delete',
        defaultMessage: '删除',
      })}
      okButtonProps={{
        danger: true,
        ghost: true,
      }}
      {...restProps}
    >
      <Alert
        type="warning"
        showIcon={true}
        message={formatMessage({
          id: 'ocp-express.Detail.Component.DeleteReplicaModal.DeletingTheCopyDataWill',
          defaultMessage: '删除副本数据将不可恢复，请谨慎操作',
        })}
        style={{ marginBottom: 24 }}
      />

      <Descriptions column={1}>
        <Descriptions.Item
          label={formatMessage({
            id: 'ocp-express.Detail.Component.DeleteReplicaModal.Tenant',
            defaultMessage: '所在租户',
          })}
        >
          {tenantData.name}
        </Descriptions.Item>
        <Descriptions.Item
          label={formatMessage({
            id: 'ocp-express.Detail.Component.BatchDeleteReplicaModal.Zone',
            defaultMessage: '所在 Zone',
          })}
        >
          {tenantZones
            .map(item => {
              // const replicaTypeLabel = findByValue(REPLICA_TYPE_LIST, item.replicaType).label;
              return item.name;
            })
            .join('、')}
        </Descriptions.Item>
        <Descriptions.Item
          label={formatMessage({
            id: 'ocp-express.Detail.Component.DeleteReplicaModal.ReplicaType',
            defaultMessage: '副本类型',
          })}
        >
          {formatMessage({
            id: 'ocp-express.Detail.Component.DeleteReplicaModal.AllPurposeCopy',
            defaultMessage: '全能型副本',
          })}
        </Descriptions.Item>
      </Descriptions>
    </Modal>
  );
};

export default connect(() => ({}))(DeleteReplicaModal);
