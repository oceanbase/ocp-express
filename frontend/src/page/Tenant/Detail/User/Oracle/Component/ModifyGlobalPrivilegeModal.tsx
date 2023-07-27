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
import SelectAllAndClearRender from '@/component/SelectAllAndClearRender';
import { ORACLE_SYS_PRIVS } from '@/constant/tenant';

const { Option } = MySelect;

/**
 * 参数说明
 * username 用户名
 * roleName 角色名
 * username / roleName 二者只需传一个
 *  */
interface ModifyGlobalPrivilegeModalProps {
  tenantId?: number;
  dispatch: any;
  username?: string;
  roleName?: string;
  globalPrivileges: string[];
  onSuccess: () => void;
}

const ModifyGlobalPrivilegeModal: React.FC<ModifyGlobalPrivilegeModalProps> = ({
  tenantId,
  username,
  roleName,
  globalPrivileges,
  onSuccess,
  ...restProps
}) => {
  const [form] = Form.useForm();
  const { validateFields, setFieldsValue } = form;

  const { run: modifyGlobalPrivilege, loading: userLoading } = useRequest(
    ObUserController.modifyGlobalPrivilege,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage(
              {
                id: 'ocp-express.Oracle.Component.ModifyGlobalPrivilegeModal.TheSystemPermissionForUsername',
                defaultMessage: '{username} 的系统权限修改成功',
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

  const { run: modifyGlobalPrivilegeFromRole, loading: roleLoading } = useRequest(
    ObUserController.modifyGlobalPrivilegeFromRole,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage(
              {
                id: 'ocp-express.Oracle.Component.ModifyGlobalPrivilegeModal.TheSystemPermissionOfRolename',
                defaultMessage: '{roleName} 的系统权限修改成功',
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

  const submitFn = () => {
    validateFields().then(values => {
      if (username) {
        modifyGlobalPrivilege(
          {
            tenantId,
            username,
          },

          { globalPrivileges: values?.globalPrivileges }
        );
      } else {
        modifyGlobalPrivilegeFromRole(
          {
            tenantId,
            roleName,
          },

          { globalPrivileges: values.globalPrivileges }
        );
      }
    });
  };

  return (
    <Modal
      title={formatMessage({
        id: 'ocp-express.Oracle.Component.ModifyGlobalPrivilegeModal.ModifySystemPermissions',
        defaultMessage: '修改系统权限',
      })}
      destroyOnClose={true}
      confirmLoading={userLoading || roleLoading}
      onOk={submitFn}
      {...restProps}
    >
      <Form form={form} layout="vertical" requiredMark="optional" preserve={false}>
        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Oracle.Component.ModifyGlobalPrivilegeModal.SystemPermissions',
            defaultMessage: '系统权限',
          })}
          tooltip={{
            placement: 'right',
            title: formatMessage({
              id: 'ocp-express.Oracle.Component.ModifyGlobalPrivilegeModal.ApplicableToAllDatabases',
              defaultMessage: '适用于所有的数据库',
            }),
          }}
          name="globalPrivileges"
          initialValue={globalPrivileges}
        >
          <MySelect
            mode="multiple"
            maxTagCount={5}
            allowClear={true}
            showSearch={true}
            dropdownRender={menu => (
              <SelectAllAndClearRender
                menu={menu}
                onSelectAll={() => {
                  setFieldsValue({
                    globalPrivileges: ORACLE_SYS_PRIVS,
                  });
                }}
                onClearAll={() => {
                  setFieldsValue({
                    globalPrivileges: [],
                  });
                }}
              />
            )}
          >
            {ORACLE_SYS_PRIVS.map((item: string) => (
              <Option key={item} value={item}>
                {item.replace(/_/g, ' ')}
              </Option>
            ))}
          </MySelect>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default ModifyGlobalPrivilegeModal;
