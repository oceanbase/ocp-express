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
import { useRequest } from 'ahooks';
import React from 'react';
import { Button, Checkbox, Col, Form, message, Row, Space, Tag, theme } from '@oceanbase/design';
import * as ObUserController from '@/service/ocp-express/ObUserController';
import {
  ORACLE_OBJECT_TYPE_LIST,
  ORACLE_TABLE_PRIVILEGE_LIST,
  ORACLE_VIEW_PRIVILEGE_LIST,
  ORACLE_STORED_PROCEDURE_PRIVILEGE_LIST,
} from '@/constant/tenant';
import MyDrawer from '@/component/MyDrawer';

/**
 * 参数说明
 * username 用户名
 * roleName 角色名
 * username / roleName 二者只需传一个
 *  */
interface ModifyObjectPrivilegeDrawerProps {
  tenantId?: number;
  username?: string;
  roleName?: string;
  dbObjects?: API.ObjectPrivilege[];
  onSuccess: () => void;
  onCancel: () => void;
}

const ModifyObjectPrivilegeDrawer: React.FC<ModifyObjectPrivilegeDrawerProps> = ({
  tenantId,
  username,
  roleName,
  dbObjects,
  onSuccess,
  onCancel,
  ...restProps
}) => {
  const { token } = theme.useToken();
  const objectType = dbObjects?.[0]?.object?.objectType;

  const [form] = Form.useForm();
  const { validateFields } = form;

  const { run: modifyObjectPrivilegeFromUser, loading: userModifyObjectLoading } = useRequest(
    ObUserController.modifyObjectPrivilegeFromUser,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage({
              id: 'ocp-express.Oracle.Component.ModifyObjectPrivilegeDrawer.PermissionModificationSucceeded',
              defaultMessage: '批量修改权限成功',
            })
          );
          if (onSuccess) {
            onSuccess();
          }
        }
      },
    }
  );

  const { run: modifyObjectPrivilegeFromRole, loading: roleModifyObjectLoading } = useRequest(
    ObUserController.modifyObjectPrivilegeFromRole,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage({
              id: 'ocp-express.Oracle.Component.ModifyObjectPrivilegeDrawer.PermissionModificationSucceeded',
              defaultMessage: '批量修改权限成功',
            })
          );
          if (onSuccess) {
            onSuccess();
          }
        }
      },
    }
  );

  const handleSubmit = () => {
    validateFields().then(values => {
      const { privileges } = values;
      // 整理参数 用户/角色 添加对象 || 用户/角色 修改对象权限
      let param = {
        tenantId,
      };

      const objectPrivileges = dbObjects?.map(item => ({
        object: item.object,
        privileges,
      }));

      // 用户
      if (username) {
        param = { ...param, username };
        modifyObjectPrivilegeFromUser(param, { objectPrivileges });
      }
      // 角色
      if (roleName) {
        param = { ...param, roleName };
        modifyObjectPrivilegeFromRole(param, { objectPrivileges });
      }
    });
  };

  let privilegeOptions: string[] = [];
  if (objectType === 'TABLE') {
    privilegeOptions = ORACLE_TABLE_PRIVILEGE_LIST;
  } else if (objectType === 'VIEW') {
    privilegeOptions = ORACLE_VIEW_PRIVILEGE_LIST;
  } else if (objectType === 'STORED_PROCEDURE') {
    privilegeOptions = ORACLE_STORED_PROCEDURE_PRIVILEGE_LIST;
  }

  const titleTextype = ORACLE_OBJECT_TYPE_LIST.filter(item => item.value === objectType)[0]?.label;
  const titleText = formatMessage(
    {
      id: 'ocp-express.Oracle.Component.ModifyObjectPrivilegeDrawer.ModifyTitletextypePermissions',
      defaultMessage: '批量修改{titleTextype}权限',
    },
    { titleTextype }
  );
  return (
    <MyDrawer
      width={680}
      title={titleText}
      destroyOnClose={true}
      {...restProps}
      onCancel={onCancel}
      footer={
        <Space>
          <Button onClick={onCancel}>
            {formatMessage({
              id: 'ocp-express.Oracle.Component.ModifyObjectPrivilegeDrawer.Cancel',
              defaultMessage: '取消',
            })}
          </Button>
          <Button
            type="primary"
            onClick={handleSubmit}
            loading={userModifyObjectLoading || roleModifyObjectLoading}
          >
            {formatMessage({
              id: 'ocp-express.Oracle.Component.ModifyObjectPrivilegeDrawer.Determine',
              defaultMessage: '确定',
            })}
          </Button>
        </Space>
      }
    >
      <Form form={form} preserve={false} layout="vertical" hideRequiredMark={true}>
        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Oracle.Component.ModifyObjectPrivilegeDrawer.Object',
            defaultMessage: '对象',
          })}
        >
          <Row
            gutter={[8, 8]}
            style={{
              padding: 12,
              maxHeight: 520,
              background: token.colorBgLayout,
              overflow: 'auto',
            }}
          >
            {dbObjects?.map(item => (
              <Col key={item?.object?.fullName} span={6}>
                <Tag>{item?.object?.fullName}</Tag>
              </Col>
            ))}
          </Row>
        </Form.Item>
        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Oracle.Component.ModifyObjectPrivilegeDrawer.GrantPermissions',
            defaultMessage: '授予权限',
          })}
          name="privileges"
          rules={[
            {
              required: true,
              message: formatMessage({
                id: 'ocp-express.Oracle.Component.ModifyObjectPrivilegeDrawer.SelectAtLeastOnePermission',
                defaultMessage: '至少选择一个权限',
              }),
            },
          ]}
        >
          <Checkbox.Group style={{ width: '100%' }}>
            <Row gutter={[8, 8]}>
              {privilegeOptions.map(item => (
                <Col key={item} span={6}>
                  <Checkbox key={item} value={item} style={{ marginLeft: 0 }}>
                    {item}
                  </Checkbox>
                </Col>
              ))}
            </Row>
          </Checkbox.Group>
        </Form.Item>
      </Form>
    </MyDrawer>
  );
};
export default ModifyObjectPrivilegeDrawer;
