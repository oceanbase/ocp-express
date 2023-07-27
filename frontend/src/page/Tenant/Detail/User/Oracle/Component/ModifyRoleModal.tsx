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
import { Form, Modal, message } from '@oceanbase/design';
import * as ObUserController from '@/service/ocp-express/ObUserController';
import { useRequest } from 'ahooks';
import MySelect from '@/component/MySelect';

const { Option } = MySelect;

/**
 * 参数说明
 * username 用户名
 * roleName 角色名
 * username / roleName 二者只需传一个
 *  */
interface ModifyRoleModalProps {
  tenantId?: number;
  username?: string;
  roleName?: string;
  grantedRoles?: string[];
  onSuccess: () => void;
}

const ModifyRoleModal: React.FC<ModifyRoleModalProps> = ({
  tenantId,
  username,
  roleName,
  grantedRoles,
  onSuccess,
  ...restProps
}) => {
  const [form] = Form.useForm();
  const { validateFields } = form;

  const { data } = useRequest(ObUserController.listDbRoles, {
    defaultParams: [
      {
        tenantId,
      },
    ],

    refreshDeps: [tenantId],
  });

  const dbRoleList = data?.data?.contents || [];

  const { run: modifyRoleFromRole, loading: roleLoading } = useRequest(
    ObUserController.modifyRoleFromRole,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage(
              {
                id: 'ocp-express.Oracle.Component.ModifyRoleModal.RolenameRoleModified',
                defaultMessage: '{roleName} 角色修改成功',
              },
              { roleName }
            )
          );
          if (onSuccess) {
            onSuccess();
          }
        }
      },
    }
  );

  const { run: modifyRoleFromUser, loading: userLoading } = useRequest(
    ObUserController.modifyRoleFromUser,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage(
              {
                id: 'ocp-express.Oracle.Component.ModifyRoleModal.TheUsernameRoleHasBeen',
                defaultMessage: '{username} 角色修改成功',
              },
              { username }
            )
          );
          if (onSuccess) {
            onSuccess();
          }
        }
      },
    }
  );

  const submitFn = () => {
    validateFields().then(values => {
      const { roles } = values;

      if (roleName) {
        modifyRoleFromRole(
          {
            tenantId,
            roleName,
          },

          { roles }
        );
      } else {
        modifyRoleFromUser(
          {
            tenantId,
            username,
          },

          { roles }
        );
      }
    });
  };

  return (
    <Modal
      title={formatMessage({
        id: 'ocp-express.Oracle.Component.ModifyRoleModal.ModifyARole',
        defaultMessage: '修改角色',
      })}
      destroyOnClose={true}
      confirmLoading={userLoading || roleLoading}
      onOk={submitFn}
      {...restProps}
    >
      <Form form={form} layout="vertical" requiredMark="optional" preserve={false}>
        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Oracle.Component.ModifyRoleModal.HaveARole',
            defaultMessage: '拥有角色',
          })}
          name="roles"
          tooltip={{
            placement: 'right',
            title: formatMessage({
              id: 'ocp-express.Oracle.Component.ModifyRoleModal.HaveARole',
              defaultMessage: '拥有角色',
            }),
          }}
          initialValue={grantedRoles}
        >
          <MySelect mode="multiple" maxTagCount={5} allowClear={true} showSearch={true}>
            {dbRoleList
              // 只能拥有其他角色，不能拥有自身，否则内核会出现死循环错误
              .filter(role => role.name !== roleName)
              .map((item: API.DbRole) => (
                <Option key={item.name} value={item.name}>
                  {item.name}
                </Option>
              ))}
          </MySelect>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default ModifyRoleModal;
