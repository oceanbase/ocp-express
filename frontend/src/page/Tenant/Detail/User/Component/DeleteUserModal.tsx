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
import { Input, Alert, Descriptions, Modal, message } from '@oceanbase/design';
import { useRequest } from 'ahooks';
import * as ObUserController from '@/service/ocp-express/ObUserController';

/**
 * 参数说明
 * username 用户名
 * roleName 角色名
 * username / roleName 二者只需传一个
 * 删除用户 传 username
 * 删除角色 传 roleName
 *
 *  */
interface DeleteUserModalProps {
  username?: string;
  roleName?: string;
  userStats?: API.SessionStats[];
  tenantData: API.TenantInfo;
  onSuccess?: () => void;
}

const DeleteUserModal: React.FC<DeleteUserModalProps> = ({
  username,
  roleName,
  userStats,
  tenantData,
  onSuccess,
  ...restProps
}) => {
  const [allowDelete, setAllowDelete] = useState(false);

  let APIName = ObUserController.deleteDbRole;
  const deleteDbRoleParams = {
    tenantId: tenantData.obTenantId,
    roleName,
  };

  const deleteDbUserParams = {
    tenantId: tenantData.obTenantId,
    username,
  };

  if (username) {
    APIName = ObUserController.deleteDbUser;
  } else if (roleName) {
    APIName = ObUserController.deleteDbRole;
  }
  const { run, loading } = useRequest(APIName, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        message.success(
          username
            ? formatMessage({ id: 'ocp-express.User.Component.DeleteUserModal.UserDeleted' })
            : formatMessage({ id: 'ocp-express.User.Component.DeleteUserModal.RoleDeleted' })
        );
        if (onSuccess) {
          onSuccess();
        }
      }
    },
  });

  const handleDelete = () => {
    run(roleName ? deleteDbRoleParams : deleteDbUserParams);
  };

  return (
    <Modal
      title={
        username
          ? formatMessage({
              id: 'ocp-express.User.Component.DeleteUserModal.DeleteAUser',
              defaultMessage: '删除用户',
            })
          : formatMessage({
              id: 'ocp-express.User.Component.DeleteUserModal.DeleteARole',
              defaultMessage: '删除角色',
            })
      }
      destroyOnClose={true}
      confirmLoading={loading}
      okText={formatMessage({
        id: 'ocp-express.User.Component.DeleteUserModal.Delete',
        defaultMessage: '删除',
      })}
      cancelText={formatMessage({
        id: 'ocp-express.User.Component.DeleteUserModal.Cancel',
        defaultMessage: '取消',
      })}
      onOk={handleDelete}
      {...restProps}
      okButtonProps={{
        ghost: true,
        danger: true,
        disabled: !allowDelete,
      }}
    >
      <Alert
        message={
          username
            ? formatMessage({
                id: 'ocp-express.User.Component.DeleteUserModal.DeletingAUserWillDelete',
                defaultMessage: '删除用户将删除该用户下所有对象和数据，请谨慎操作',
              })
            : formatMessage({
                id: 'ocp-express.User.Component.DeleteUserModal.DeletingARoleWillCause',
                defaultMessage:
                  '删除角色将导致引用了该角色的角色和用户失去该角色的所有权限，请谨慎操作',
              })
        }
        type="warning"
        showIcon={true}
        style={{
          marginBottom: 12,
        }}
      />

      <Descriptions column={1}>
        <Descriptions.Item
          label={formatMessage({
            id: 'ocp-express.Detail.Session.List.User',
            defaultMessage: '用户',
          })}
        >
          {username || roleName}
        </Descriptions.Item>
        <Descriptions.Item
          label={formatMessage({
            id: 'ocp-express.User.Component.DeleteUserModal.Tenant',
            defaultMessage: '所属租户',
          })}
        >
          {tenantData.name}
        </Descriptions.Item>
        {username && (
          <>
            <Descriptions.Item
              label={formatMessage({
                id: 'ocp-express.User.Component.DeleteUserModal.CurrentSessions',
                defaultMessage: '当前会话总数',
              })}
            >
              {(userStats && userStats[0] && userStats[0].totalCount) || 0}
            </Descriptions.Item>
            <Descriptions.Item
              label={formatMessage({
                id: 'ocp-express.User.Component.DeleteUserModal.ActiveSessions',
                defaultMessage: '活跃会话数',
              })}
            >
              {(userStats && userStats[0] && userStats[0].activeCount) || 0}
            </Descriptions.Item>
          </>
        )}
      </Descriptions>
      <div>
        {formatMessage({
          id: 'ocp-express.User.Component.DeleteUserModal.Enter',
          defaultMessage: '请输入',
        })}

        <span style={{ color: 'red' }}> delete </span>
        {formatMessage({
          id: 'ocp-express.User.Component.DeleteUserModal.ConfirmOperation',
          defaultMessage: '确认操作',
        })}
      </div>
      <Input
        style={{ width: 400, marginTop: 8 }}
        onChange={e => setAllowDelete(e.target.value === 'delete')}
      />
    </Modal>
  );
};

export default DeleteUserModal;
