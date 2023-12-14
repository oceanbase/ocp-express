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
import React, { useEffect, useState } from 'react';
import { Form, message } from '@oceanbase/design';
import Password from '@/component/Password';
import MyDrawer from '@/component/MyDrawer';
import MyInput from '@/component/MyInput';
import MySelect from '@/component/MySelect';
import { useRequest } from 'ahooks';
import * as IamController from '@/service/ocp-express/IamController';
import SelectAllAndClearRender from '@/component/SelectAllAndClearRender';
import DatabasePrivilegeTransfer from '@/component/DatabasePrivilegeTransfer';
import { DATABASE_USER_NAME_RULE } from '@/constant';
import { validatePassword } from '@/util';
import encrypt from '@/util/encrypt';
import { DATABASE_GLOBAL_PRIVILEGE_LIST } from '@/constant/tenant';
import { differenceBy, isEqual, uniq } from 'lodash';

const { Option } = MySelect;

interface AddOrEditUserDrawerProps {
  tenantId?: string;
  clusterId?: string;
  dbUser?: API.DbUser;
  dispatch: any;
  tenantData: API.TenantInfo;
  databaseListLoading: boolean;
  createDbUserLoading: boolean;
  modifyGlobalPrivilegeLoading: boolean;
  modifyDbPrivilegeLoading: boolean;
  databaseList: API.DbPrivilege[];
  charsetList: API.Charset[];
  onSuccess: () => void;
  onCancel: () => void;
}

const AddUserDrawer: React.FC<AddOrEditUserDrawerProps> = ({
  clusterId,
  tenantId,
  onSuccess,
  onCancel,
  dispatch,
  dbUser,
  databaseListLoading,
  createDbUserLoading,
  modifyGlobalPrivilegeLoading,
  modifyDbPrivilegeLoading,
  databaseList,
  tenantData,
  charsetList,
  ...restProps
}) => {
  const [form] = Form.useForm();
  const { validateFields, getFieldsValue, setFieldsValue } = form;
  // TODO 这里借助一下深拷贝，来解决在组件内操作时dbUser被篡改的问题；待后期排查优化
  const dbUserInfo = JSON.parse(JSON.stringify(dbUser));
  const [passed, setPassed] = useState(true);

  const getlistDatabases = (dbName: string = '') => {
    dispatch({
      type: 'database/listDatabases',
      payload: {
        tenantId,
        dbName,
      },
    });
  };

  useEffect(() => {
    getlistDatabases();
  }, []);

  const validateConfirmPassword = (rule, value, callback) => {
    const { password } = getFieldsValue();
    if (value && value !== password) {
      callback(
        formatMessage({
          id: 'ocp-express.Detail.Component.AddUserDrawer.TheNewPasswordEnteredTwice',
          defaultMessage: '两次输入的新密码不一致，请重新输入',
        })
      );
    } else {
      callback();
    }
  };

  const { runAsync: getLoginKey } = useRequest(IamController.getLoginKey, {
    manual: true,
  });

  const modifyPrivilege = (globalPrivileges?: string[], dbPrivileges?: API.DbPrivilege) => {
    const promiseList = [];
    if (!isEqual(uniq(globalPrivileges), uniq(dbUserInfo.globalPrivileges))) {
      promiseList.push(
        dispatch({
          type: 'database/modifyGlobalPrivilege',
          payload: {
            tenantId: tenantData.obTenantId,
            username: dbUser?.username,
            globalPrivileges,
          },

          onSuccess: () => {
            const dbUsername = dbUser?.username;
            message.success(
              formatMessage(
                {
                  id: 'ocp-express.Detail.Component.AddUserDrawer.TheSystemPermissionOfDbusername',
                },
                { dbUsername }
              )
            );
          },
        })
      );
    }
    if (dbPrivileges) {
      promiseList.push(
        dispatch({
          type: 'database/modifyDbPrivilege',
          payload: {
            tenantId: tenantData.obTenantId,
            username: dbUser?.username,
            dbPrivileges,
          },

          onSuccess: () => {
            const dbUsername = dbUser?.username;
            message.success(
              formatMessage(
                {
                  id: 'ocp-express.Detail.Component.AddUserDrawer.ThePermissionOfDbusernameHas',
                },
                { dbUsername }
              )
            );
          },
        })
      );
    }

    if (promiseList.length) {
      Promise.all(promiseList).then(() => {
        if (onSuccess) {
          onSuccess();
        }
      });
    } else {
      message.info(
        formatMessage({
          id: 'ocp-express.Detail.Component.AddUserDrawer.NoPermissionsHaveBeenModified',
          defaultMessage: '未修改任何权限',
        })
      );
    }
  };

  const handleSubmit = () => {
    validateFields().then(values => {
      const { username, password, globalPrivileges, dbPrivileges } = values;
      if (dbUserInfo) {
        if (!dbPrivileges) {
          modifyPrivilege(globalPrivileges);
        } else {
          const oldDbNameList = uniq(
            dbUserInfo.dbPrivileges?.map((item: API.DbPrivilege) => item.dbName)
          );

          const newDbNameList = uniq(dbPrivileges?.map((item: API.DbPrivilege) => item.dbName));
          // 比较修改前后的权限，如果只是简单修改权限，直接保存
          if (isEqual(oldDbNameList, newDbNameList)) {
            modifyPrivilege(globalPrivileges, dbPrivileges);
          } else {
            // 整体删除了某个数据库的权限，需要传递给后端 {dbName, privileges: []}，接口拿到这种参数才会整体删除数据库的权限
            dbUserInfo.dbPrivileges?.forEach((item: API.DbPrivilege) => {
              if (!newDbNameList.includes(item.dbName)) {
                dbPrivileges.push({
                  dbName: item?.dbName,
                  privileges: [],
                });
              }
            });
            modifyPrivilege(globalPrivileges, dbPrivileges);
          }
        }
      } else {
        getLoginKey().then(response => {
          const publicKey = response?.data?.publicKey || '';
          dispatch({
            type: 'database/createDbUser',
            payload: {
              tenantId: tenantData.obTenantId,
              username,
              password: encrypt(password, publicKey),
              globalPrivileges,
              dbPrivileges,
            },

            onSuccess: () => {
              if (onSuccess) {
                onSuccess();
              }
            },
          });
        });
      }
    });
  };

  return (
    <MyDrawer
      width={960}
      title={
        dbUser
          ? formatMessage({
              id: 'ocp-express.Detail.Component.AddUserDrawer.ModifyDatabaseUserPermissions',
              defaultMessage: '修改数据库用户的权限',
            })
          : formatMessage({
              id: 'ocp-express.Detail.Component.AddUserDrawer.CreateADatabaseUser',
              defaultMessage: '新建数据库用户',
            })
      }
      destroyOnClose={true}
      okText={formatMessage({
        id: 'ocp-express.Detail.Component.AddUserDrawer.Submitted',
        defaultMessage: '提交',
      })}
      confirmLoading={
        createDbUserLoading || modifyGlobalPrivilegeLoading || modifyDbPrivilegeLoading
      }
      onOk={handleSubmit}
      onCancel={onCancel}
      {...restProps}
    >
      <Form form={form} layout="vertical" requiredMark="optional" preserve={false}>
        <Form.Item
          wrapperCol={{ span: 9 }}
          label={formatMessage({
            id: 'ocp-express.Detail.Component.AddUserDrawer.UserName',
            defaultMessage: '用户名',
          })}
          name="username"
          initialValue={dbUser?.username}
          rules={[
            {
              required: true,
              message: formatMessage({
                id: 'ocp-express.Detail.Component.AddUserDrawer.EnterAUsername',
                defaultMessage: '请输入用户名',
              }),
            },
            // 修改权限时，不对用户名做格式校验
            dbUser ? {} : DATABASE_USER_NAME_RULE,
          ]}
        >
          <MyInput
            disabled={dbUser}
            placeholder={formatMessage({
              id: 'ocp-express.Detail.Component.AddUserDrawer.EnterAUsername',
              defaultMessage: '请输入用户名',
            })}
          />
        </Form.Item>
        {!dbUser && (
          <>
            <Form.Item
              wrapperCol={{ span: 9 }}
              label={formatMessage({
                id: 'ocp-express.Detail.Component.AddUserDrawer.Password',
                defaultMessage: '密码',
              })}
              name="password"
              rules={[
                {
                  required: true,
                  message: formatMessage({
                    id: 'ocp-express.Detail.Component.AddUserDrawer.EnterAPassword',
                    defaultMessage: '请输入密码',
                  }),
                },
                {
                  validator: validatePassword(passed),
                },
              ]}
            >
              <Password onValidate={setPassed} />
            </Form.Item>
            <Form.Item
              wrapperCol={{ span: 9 }}
              label={formatMessage({
                id: 'ocp-express.Detail.Component.AddUserDrawer.ConfirmPassword',
                defaultMessage: '确认密码',
              })}
              name="confirmPassword"
              dependencies={['password']}
              rules={[
                {
                  required: true,
                  message: formatMessage({
                    id: 'ocp-express.Detail.Component.AddUserDrawer.EnterThePasswordAgain',
                    defaultMessage: '请再次输入密码',
                  }),
                },

                {
                  validator: validateConfirmPassword,
                },
              ]}
            >
              <MyInput.Password
                placeholder={formatMessage({
                  id: 'ocp-express.Detail.Component.AddUserDrawer.EnterThePasswordAgain',
                  defaultMessage: '请再次输入密码',
                })}
              />
            </Form.Item>
          </>
        )}

        <Form.Item
          wrapperCol={{ span: 9 }}
          label={formatMessage({
            id: 'ocp-express.Detail.Component.AddUserDrawer.GlobalPermissions',
            defaultMessage: '全局权限',
          })}
          tooltip={{
            placement: 'right',
            title: formatMessage({
              id: 'ocp-express.Detail.Component.AddUserDrawer.ApplicableToAllDatabases',
              defaultMessage: '适用于所有的数据库',
            }),
          }}
          name="globalPrivileges"
          initialValue={(dbUser && dbUser.globalPrivileges) || undefined}
        >
          <MySelect
            mode="multiple"
            maxTagCount={8}
            allowClear={true}
            showSearch={true}
            dropdownRender={menu => (
              <SelectAllAndClearRender
                menu={menu}
                onSelectAll={() => {
                  setFieldsValue({
                    globalPrivileges: uniq(DATABASE_GLOBAL_PRIVILEGE_LIST?.map(item => item.value)),
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
            {DATABASE_GLOBAL_PRIVILEGE_LIST.map(item => (
              <Option key={item.value} value={item.value}>
                {item.value}
              </Option>
            ))}
          </MySelect>
        </Form.Item>
        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Detail.Component.AddUserDrawer.DatabasePermissions',
            defaultMessage: '数据库权限',
          })}
          tooltip={{
            placement: 'right',
            title: formatMessage({
              id: 'ocp-express.Detail.Component.AddUserDrawer.AppliesToAllTargetsIn',
              defaultMessage: '适用于一个给定数据库中的所有目标',
            }),
          }}
          name="dbPrivileges"
        >
          <DatabasePrivilegeTransfer
            dbUserList={(dbUser && dbUser.dbPrivileges
              ? differenceBy(databaseList, dbUser.dbPrivileges, 'dbName')
              : databaseList
            )?.filter(item => item.dbName !== 'information_schema')}
            loading={databaseListLoading}
            dbPrivilegedList={dbUser && dbUser.dbPrivileges}
          />
        </Form.Item>
      </Form>
    </MyDrawer>
  );
};

function mapStateToProps({ loading, tenant, database }) {
  return {
    tenantData: tenant.tenantData,
    databaseListLoading: loading.effects['database/listDatabases'],
    createDbUserLoading: loading.effects['database/createDbUser'],
    modifyDbPrivilegeLoading: loading.effects['database/modifyDbPrivilege'],
    modifyGlobalPrivilegeLoading: loading.effects['database/modifyGlobalPrivilege'],
    databaseList: database.databaseList,
  };
}

export default connect(mapStateToProps)(AddUserDrawer);
