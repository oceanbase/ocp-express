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
import React, { useState } from 'react';
import { Alert, Descriptions, Modal, message, token } from '@oceanbase/design';
import type { ModalProps } from '@oceanbase/design/es/modal';
import MyInput from '@/component/MyInput';
import { useRequest } from 'ahooks';
import * as ObTenantController from '@/service/ocp-express/ObTenantController';

export interface DeleteTenantModalProps extends ModalProps {
  tenantData: API.TenantInfo;
  onSuccess: () => void;
}

const DeleteTenantModal: React.FC<DeleteTenantModalProps> = ({
  tenantData,
  onSuccess,
  ...restProps
}) => {
  const [confirmCode, setConfirmCode] = useState('');

  const { run, loading } = useRequest(ObTenantController.deleteTenant, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        message.success(
          formatMessage({
            id: 'ocp-express.Detail.Component.DeleteTenantModal.TenantDeletedSuccessfully',
            defaultMessage: '租户删除成功',
          })
        );
        if (onSuccess) {
          onSuccess();
        }
      }
    },
  });

  return (
    <Modal
      title={formatMessage({
        id: 'ocp-express.Detail.Component.DeleteTenantModal.DeleteATenant',
        defaultMessage: '删除租户',
      })}
      destroyOnClose={true}
      confirmLoading={loading}
      onOk={() => run({ tenantId: tenantData.obTenantId })}
      okText={formatMessage({
        id: 'ocp-express.Detail.Component.DeleteTenantModal.Delete',
        defaultMessage: '删除',
      })}
      okButtonProps={{
        danger: true,
        ghost: true,
        disabled: confirmCode !== 'delete',
      }}
      {...restProps}
    >
      <Alert
        type="warning"
        showIcon={true}
        message={formatMessage({
          id: 'ocp-express.Detail.Component.DeleteTenantModal.DataCannotBeRecoveredAfter',
          defaultMessage: '租户删除后数据将不可恢复，请谨慎操作',
        })}
        style={{ marginBottom: 24 }}
      />

      <Descriptions column={1}>
        <Descriptions.Item
          label={formatMessage({
            id: 'ocp-express.Detail.Component.DeleteTenantModal.Tenant',
            defaultMessage: '租户',
          })}
        >
          {tenantData.name}
        </Descriptions.Item>
      </Descriptions>
      <div>
        {formatMessage({
          id: 'ocp-express.Detail.Component.DeleteTenantModal.PleaseEnter',
          defaultMessage: '请输入',
        })}

        <span style={{ color: token.colorError }}> delete </span>
        {formatMessage({
          id: 'ocp-express.Detail.Component.DeleteTenantModal.ConfirmOperation',
          defaultMessage: '确认操作',
        })}
      </div>
      <MyInput
        style={{ width: 400, marginTop: 8 }}
        onChange={e => setConfirmCode(e.target.value)}
      />
    </Modal>
  );
};

export default DeleteTenantModal;
