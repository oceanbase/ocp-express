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
import { useRequest } from 'ahooks';
import { Form } from '@oceanbase/design';
import type { SelectProps } from 'antd/es/select';
import MyDrawer from '@/component/MyDrawer';
import MyInput from '@/component/MyInput';
import MySelect from '@/component/MySelect';
import SelectAllAndClearRender from '@/component/SelectAllAndClearRender';
import SelectDropdownRender from '@/component/SelectDropdownRender';
import { ORACLE_DATABASE_USER_NAME_RULE, MODAL_FORM_ITEM_LAYOUT } from '@/constant';
import { ORACLE_SYS_PRIVS } from '@/constant/tenant';
import * as ObUserController from '@/service/ocp-express/ObUserController';

const { Option } = MySelect;

interface RoleSelectProps extends SelectProps<string> {
  tenantId?: number;
  onAddSuccess: (roles) => void;
  onCancel: () => void;
}

const RoleSelect: React.FC<RoleSelectProps> = ({
  tenantId,
  onAddSuccess,
  onCancel,
  ...restProps
}) => {
  const [visible, setVisible] = useState(false);

  const [form] = Form.useForm();
  const { validateFields, setFieldsValue } = form;

  const { data, refresh } = useRequest(ObUserController.listDbRoles, {
    defaultParams: [
      {
        tenantId,
      },
    ],

    refreshDeps: [tenantId],
  });

  const dbRoleList = data?.data?.contents || [];

  const { run, loading } = useRequest(ObUserController.createDbRole, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        setVisible(false);
        refresh();
        if (onAddSuccess) {
          onAddSuccess([res?.data]);
        }
      }
    },
  });

  const handleSubmit = () => {
    validateFields().then(values => {
      const { roleName, globalPrivileges, roles } = values;
      run(
        {
          tenantId,
        },

        {
          roleName,
          globalPrivileges,
          roles,
        }
      );
    });
  };

  return (
    <span>
      <MySelect
        width={520}
        showSearch={true}
        mode="multiple"
        maxTagCount={5}
        allowClear={true}
        // ID 作为值，但要根据 name(作为 children) 进行过滤
        optionFilterProp="children"
        dropdownRender={menu => (
          <SelectDropdownRender
            menu={menu}
            text={formatMessage({
              id: 'ocp-express.Oracle.Component.RoleSelect.CreateARole',
              defaultMessage: '新建角色',
            })}
            onClick={() => {
              setVisible(true);
            }}
          />
        )}
        {...restProps}
      >
        {dbRoleList.map(item => (
          <Option key={item.name} value={item.name}>
            {item.name}
          </Option>
        ))}
      </MySelect>
      <MyDrawer
        width={520}
        title={formatMessage({
          id: 'ocp-express.Oracle.Component.RoleSelect.CreateARole',
          defaultMessage: '新建角色',
        })}
        visible={visible}
        confirmLoading={loading}
        destroyOnClose={true}
        onCancel={() => {
          setVisible(false);
        }}
        okText={formatMessage({
          id: 'ocp-express.Oracle.Component.RoleSelect.Submitted',
          defaultMessage: '提交',
        })}
        onOk={() => handleSubmit()}
      >
        <Form
          form={form}
          layout="vertical"
          requiredMark="optional"
          preserve={false}
          {...MODAL_FORM_ITEM_LAYOUT}
        >
          <Form.Item
            label={formatMessage({
              id: 'ocp-express.Oracle.Component.RoleSelect.Role',
              defaultMessage: '角色名',
            })}
            name="roleName"
            rules={[
              {
                required: true,
                message: formatMessage({
                  id: 'ocp-express.Oracle.Component.RoleSelect.EnterARoleName',
                  defaultMessage: '请输入角色名',
                }),
              },

              ORACLE_DATABASE_USER_NAME_RULE,
            ]}
          >
            <MyInput
              placeholder={formatMessage({
                id: 'ocp-express.Oracle.Component.RoleSelect.EnterARoleName',
                defaultMessage: '请输入角色名',
              })}
            />
          </Form.Item>
          <Form.Item
            label={formatMessage({
              id: 'ocp-express.Oracle.Component.RoleSelect.HaveSystemPermissions',
              defaultMessage: '拥有系统权限',
            })}
            tooltip={{
              placement: 'right',
              title: formatMessage({
                id: 'ocp-express.Oracle.Component.RoleSelect.ApplicableToAllDatabases',
                defaultMessage: '适用于所有的数据库',
              }),
            }}
            name="globalPrivileges"
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
          <Form.Item
            label={formatMessage({
              id: 'ocp-express.Oracle.Component.RoleSelect.HaveARole',
              defaultMessage: '拥有角色',
            })}
            name="roles"
            tooltip={{
              placement: 'right',
              title: formatMessage({
                id: 'ocp-express.Oracle.Component.RoleSelect.HaveARole',
                defaultMessage: '拥有角色',
              }),
            }}
          >
            <MySelect mode="multiple" allowClear={true} showSearch={true}>
              {dbRoleList.map(item => (
                <Option key={item.name} value={item.name}>
                  {item.name}
                </Option>
              ))}
            </MySelect>
          </Form.Item>
        </Form>
      </MyDrawer>
    </span>
  );
};

export default RoleSelect;
