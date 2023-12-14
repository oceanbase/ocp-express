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
import React, { useState } from 'react';
import {
  Button,
  Checkbox,
  Col,
  Form,
  message,
  Radio,
  Row,
  Space,
  theme,
  Tooltip,
} from '@oceanbase/design';
import * as ObUserController from '@/service/ocp-express/ObUserController';
import { uniq, findIndex } from 'lodash';
import {
  ORACLE_OBJECT_TYPE_LIST,
  ORACLE_TABLE_PRIVILEGE_LIST,
  ORACLE_VIEW_PRIVILEGE_LIST,
  ORACLE_STORED_PROCEDURE_PRIVILEGE_LIST,
} from '@/constant/tenant';
import { FORM_ITEM_LAYOUT } from '@/constant';
import MyDrawer from '@/component/MyDrawer';
import DbObjectTreeSelect from './DbObjectTreeSelect';

/**
 * 参数说明
 * username 用户名
 * roleName 角色名
 * username / roleName 二者只需传一个
 *  */
interface AddObjectPrivilegeDrawerProps {
  tenantId?: number;
  username?: string;
  roleName?: string;
  addedDbObjects?: API.ObjectPrivilege[]; // 已经被添加的对象权限
  onSuccess: () => void;
  onCancel: () => void;
}

const AddObjectPrivilegeDrawer: React.FC<AddObjectPrivilegeDrawerProps> = ({
  tenantId,
  username,
  roleName,
  addedDbObjects,
  onSuccess,
  onCancel,
  ...restProps
}) => {
  const { token } = theme.useToken();
  const [objectType, setObjectType] = useState('TABLE');

  const [form] = Form.useForm();
  const { validateFields, setFieldsValue, getFieldValue } = form;

  const validateObjectPrivileges = (rule, value: string[], callback) => {
    const checkedDbObjects: string[] = [];
    value?.forEach(object => {
      if (username && object?.split('.')[0] === username) {
        callback(
          formatMessage({
            id: 'ocp-express.Oracle.Component.AddObjectPrivilegeDrawer.ObjectsOwnedByTheCurrent',
            defaultMessage: '当前用户所拥有的对象无需授权，已自动去除',
          })
        );
      } else if (
        // 判断是否是否存在且正确
        findIndex(
          dbObjectList?.filter(item => item?.objectType === objectType),
          item => item?.fullName === object
        ) !== -1
      ) {
        //  已赋权
        if (findIndex(addedDbObjects, item => item?.object?.fullName === object) !== -1) {
          checkedDbObjects.push(object);
        }
      } else {
        checkedDbObjects.push(object);
      }
    });
    if (checkedDbObjects.length > 0) {
      callback(
        <Space>
          <span>
            {formatMessage(
              {
                id: 'ocp-express.Oracle.Component.AddObjectPrivilegeDrawer.CheckeddbobjectslengthObjectsHaveExceptions',
                defaultMessage: '{checkedDbObjectsLength}个对象存在异常',
              },
              { checkedDbObjectsLength: checkedDbObjects.length }
            )}
          </span>
          <div style={{ color: token.colorTextSecondary }}>
            <span
              style={{
                background: token.colorErrorBg,
                border: '1px solid rgba(255,163,158,1)',
                width: 12,
                height: 12,
                display: 'inline-block',
                marginRight: 8,
              }}
            />

            {formatMessage({
              id: 'ocp-express.Oracle.Component.AddObjectPrivilegeDrawer.TheObjectDoesNotExist',
              defaultMessage: '对象不存在，请检查输入是否有误',
            })}

            <span
              style={{
                background: '#FFFBE6',
                border: '1px solid rgba(255,229,143,1)',
                width: 12,
                height: 12,
                display: 'inline-block',
                margin: '0px 8px 0px 18px',
              }}
            />

            {formatMessage({
              id: 'ocp-express.Oracle.Component.AddObjectPrivilegeDrawer.TheObjectHasBeenGranted',
              defaultMessage: '对象已赋权，无需重复操作',
            })}
          </div>
        </Space>
      );
    }
    callback();
  };

  const { data } = useRequest(ObUserController.listDbObjects, {
    defaultParams: [
      {
        tenantId,
      },
    ],

    refreshDeps: [tenantId],
  });

  // 过滤掉已经赋权的对象
  let dbObjectList = data?.data?.contents || [];
  if (addedDbObjects?.length > 0) {
    const addedDbObjectsFullNameList = uniq(addedDbObjects?.map(item => item?.object?.fullName));
    if (username) {
      dbObjectList = dbObjectList.filter(
        item =>
          item?.schemaName !== username && !addedDbObjectsFullNameList?.includes(item?.fullName)
      );
    } else {
      dbObjectList = dbObjectList.filter(
        item => !addedDbObjectsFullNameList?.includes(item?.fullName)
      );
    }
  }

  const schemaNameList = uniq(
    dbObjectList?.map(item => item.schemaName).filter(item => item !== username)
  );

  const treeData = schemaNameList.map(item => ({
    key: item,
    value: item,
    title: item,
    children: dbObjectList
      .filter(dbObject => dbObject.schemaName === item)
      .map(dbObject => ({
        objectType: dbObject.objectType,
        key: `${dbObject.schemaName}.${dbObject.objectName}`,
        value: `${dbObject.schemaName}.${dbObject.objectName}`,
        title: dbObject.objectName,
      })),
  }));

  const { run: grantObjectPrivilegeToUser, loading: userAddObjectLoading } = useRequest(
    ObUserController.grantObjectPrivilegeToUser,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage(
              {
                id: 'ocp-express.Oracle.Component.AddObjectPrivilegeDrawer.UsernameObjectPermissionAdded',
                defaultMessage: '{username} 对象权限添加成功',
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

  const { run: grantObjectPrivilegeToRole, loading: roleAddObjectLoading } = useRequest(
    ObUserController.grantObjectPrivilegeToRole,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage(
              {
                id: 'ocp-express.Oracle.Component.AddObjectPrivilegeDrawer.RolenameObjectPermissionAdded',
                defaultMessage: '{roleName} 对象权限添加成功',
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

  const handleSubmit = () => {
    validateFields().then(values => {
      const { objects, privileges } = values;
      // 整理参数 用户/角色 添加对象 || 用户/角色 修改对象权限
      const param = {
        tenantId,
      };

      const objectPrivileges = objects.map(item => ({
        object: {
          objectType,
          fullName: item,
          objectName: item.split('.') && item.split('.')[1],
          schemaName: item.split('.') && item.split('.')[0],
        },

        privileges,
      }));

      // 用户
      if (username) {
        grantObjectPrivilegeToUser({ ...param, username }, { objectPrivileges });
      }
      // 角色
      if (roleName) {
        grantObjectPrivilegeToRole({ ...param, roleName }, { objectPrivileges });
      }
    });
  };

  let privilegeOptions: string[] = [];
  if (objectType === 'TABLE') {
    privilegeOptions = ORACLE_TABLE_PRIVILEGE_LIST;
    if (roleName) {
      privilegeOptions = ORACLE_TABLE_PRIVILEGE_LIST.filter(
        item => item !== 'INDEX' && item !== 'REFERENCES'
      );
    }
  } else if (objectType === 'VIEW') {
    privilegeOptions = ORACLE_VIEW_PRIVILEGE_LIST;
  } else if (objectType === 'STORED_PROCEDURE') {
    privilegeOptions = ORACLE_STORED_PROCEDURE_PRIVILEGE_LIST;
  }

  return (
    <MyDrawer
      width={728}
      title={formatMessage({
        id: 'ocp-express.Oracle.Component.AddObjectPrivilegeDrawer.AddObjects',
        defaultMessage: '添加对象',
      })}
      destroyOnClose={true}
      {...restProps}
      onCancel={onCancel}
      footer={
        <Space>
          <Button onClick={onCancel}>
            {formatMessage({
              id: 'ocp-express.Oracle.Component.AddObjectPrivilegeDrawer.Cancel',
              defaultMessage: '取消',
            })}
          </Button>
          <Button
            type="primary"
            onClick={handleSubmit}
            loading={userAddObjectLoading || roleAddObjectLoading}
          >
            {formatMessage({
              id: 'ocp-express.Oracle.Component.AddObjectPrivilegeDrawer.Determine',
              defaultMessage: '确定',
            })}
          </Button>
        </Space>
      }
    >
      <Form
        form={form}
        preserve={false}
        layout="vertical"
        hideRequiredMark={true}
        {...FORM_ITEM_LAYOUT}
      >
        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Oracle.Component.AddObjectPrivilegeDrawer.ObjectType',
            defaultMessage: '对象类型',
          })}
          name="objectType"
          initialValue="TABLE"
        >
          <Radio.Group
            options={ORACLE_OBJECT_TYPE_LIST}
            onChange={e => {
              setObjectType(e.target.value);
              setFieldsValue({
                objects: [],
                privileges: [],
              });
            }}
            optionType="button"
          />
        </Form.Item>
        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Oracle.Component.AddObjectPrivilegeDrawer.Object',
            defaultMessage: '对象',
          })}
          name="objects"
          rules={[
            {
              required: true,
              message: formatMessage({
                id: 'ocp-express.Oracle.Component.AddObjectPrivilegeDrawer.PleaseSelectAnObject',
                defaultMessage: '请选择对象',
              }),
            },

            {
              validator: validateObjectPrivileges,
            },
          ]}
          extra={
            <div>
              {formatMessage({
                id: 'ocp-express.Oracle.Component.AddObjectPrivilegeDrawer.YouCanPasteMultipleObjects',
                defaultMessage: '支持批量粘贴多个对象到输入框，',
              })}

              <Tooltip
                placement="top"
                overlayStyle={{ width: 400, maxWidth: 'none' }}
                title={formatMessage({
                  id: 'ocp-express.Oracle.Component.AddObjectPrivilegeDrawer.YouCanPasteMultipleObjects.1',
                  defaultMessage:
                    '请按照“用户名.对象名”（如 schemaname.tablename）的格式可以粘贴多个对象，用英文逗号隔开',
                })}
              >
                <a>
                  {formatMessage({
                    id: 'ocp-express.Oracle.Component.AddObjectPrivilegeDrawer.FormatRequirements',
                    defaultMessage: '格式要求',
                  })}
                </a>
              </Tooltip>
            </div>
          }
        >
          <DbObjectTreeSelect
            style={{ width: 640 }}
            value={getFieldValue('objects')}
            treeData={treeData}
            objectType={objectType}
            dbObjectList={data?.data?.contents || []}
            addedDbObjects={addedDbObjects}
          />
        </Form.Item>
        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Oracle.Component.AddObjectPrivilegeDrawer.GrantPermissions',
            defaultMessage: '授予权限',
          })}
          name="privileges"
          rules={[
            {
              required: true,
              message: formatMessage({
                id: 'ocp-express.Oracle.Component.AddObjectPrivilegeDrawer.SelectAtLeastOnePermission',
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
export default AddObjectPrivilegeDrawer;
