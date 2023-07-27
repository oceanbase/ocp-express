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
import { Form, Checkbox, Row, Col, Modal, message } from '@oceanbase/design';
import * as ObUserController from '@/service/ocp-express/ObUserController';
import { useRequest } from 'ahooks';
import {
  ORACLE_TABLE_PRIVILEGE_LIST,
  ORACLE_VIEW_PRIVILEGE_LIST,
  ORACLE_STORED_PROCEDURE_PRIVILEGE_LIST,
} from '@/constant/tenant';
import { MAX_FORM_ITEM_LAYOUT } from '@/constant';

/**
 * 参数说明
 * username 用户名
 * roleName 角色名
 * username / roleName 二者只需传一个
 *  */
interface ModifyObjectPrivilegeModalProps {
  tenantId: number;
  username?: string;
  roleName?: string;
  dbObject: API.ObjectPrivilege;
  onSuccess: () => void;
}

const ModifyObjectPrivilegeModal: React.FC<ModifyObjectPrivilegeModalProps> = ({
  tenantId,
  username,
  roleName,
  dbObject,
  onSuccess,
  ...restProps
}) => {
  const [form] = Form.useForm();
  const { validateFields } = form;

  const { run: modifyObjectPrivilegeFromUser, loading: userLoading } = useRequest(
    ObUserController.modifyObjectPrivilegeFromUser,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          const objectName = dbObject?.object?.objectName;
          message.success(
            formatMessage(
              {
                id: 'ocp-express.Oracle.Component.ModifyObjectPrivilegeModal.TheObjectPermissionOfObjectname',
              },
              { objectName }
            )
          );
          if (onSuccess) {
            onSuccess();
          }
        }
      },
    }
  );

  const { run: modifyObjectPrivilegeFromRole, loading: roleLoading } = useRequest(
    ObUserController.modifyObjectPrivilegeFromRole,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          const objectName = dbObject?.object?.objectName;
          message.success(
            formatMessage(
              {
                id: 'ocp-express.Oracle.Component.ModifyObjectPrivilegeModal.TheObjectPermissionOfObjectname',
              },
              { objectName }
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
      const { privileges } = values;
      const objectPrivileges = [
        {
          object: dbObject.object,
          privileges,
        },
      ];

      if (username) {
        modifyObjectPrivilegeFromUser(
          {
            tenantId,
            username,
          },

          { objectPrivileges }
        );
      } else if (roleName) {
        modifyObjectPrivilegeFromRole(
          {
            tenantId,
            roleName,
          },

          { objectPrivileges }
        );
      }
    });
  };

  let privilegeOptions: string[] = ORACLE_TABLE_PRIVILEGE_LIST || [];
  if (dbObject?.object?.objectType === 'TABLE') {
    privilegeOptions = ORACLE_TABLE_PRIVILEGE_LIST;
    if (roleName) {
      privilegeOptions = ORACLE_TABLE_PRIVILEGE_LIST.filter(
        item => item !== 'INDEX' && item !== 'REFERENCES'
      );
    }
  } else if (dbObject?.object?.objectType === 'VIEW') {
    privilegeOptions = ORACLE_VIEW_PRIVILEGE_LIST;
  } else if (dbObject?.object?.objectType === 'STORED_PROCEDURE') {
    privilegeOptions = ORACLE_STORED_PROCEDURE_PRIVILEGE_LIST;
  }

  return (
    <Modal
      title={formatMessage({
        id: 'ocp-express.Oracle.Component.ModifyObjectPrivilegeModal.ModifyPermissions',
        defaultMessage: '修改权限',
      })}
      destroyOnClose={true}
      confirmLoading={userLoading || roleLoading}
      onOk={submitFn}
      okText={formatMessage({
        id: 'ocp-express.Oracle.Component.ModifyObjectPrivilegeModal.Submitted',
        defaultMessage: '提交',
      })}
      {...restProps}
    >
      <Form
        form={form}
        layout="vertical"
        hideRequiredMark
        preserve={false}
        {...MAX_FORM_ITEM_LAYOUT}
      >
        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Oracle.Component.ModifyObjectPrivilegeModal.Object',
            defaultMessage: '对象',
          })}
          name="object"
        >
          {dbObject?.object?.schemaName}.{dbObject?.object?.objectName}
        </Form.Item>
        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Oracle.Component.ModifyObjectPrivilegeModal.GrantPermissions',
            defaultMessage: '授予权限',
          })}
          name="privileges"
          initialValue={dbObject?.privileges}
          rules={[
            {
              required: true,
              message: formatMessage({
                id: 'ocp-express.Oracle.Component.ModifyObjectPrivilegeModal.SelectAtLeastOnePermission',
                defaultMessage: '至少选择一个权限',
              }),
            },
          ]}
        >
          <Checkbox.Group>
            <Row>
              {privilegeOptions.map(item => (
                <Col span={6}>
                  <Checkbox key={item} value={item}>
                    {item}
                  </Checkbox>
                </Col>
              ))}
            </Row>
          </Checkbox.Group>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default ModifyObjectPrivilegeModal;
