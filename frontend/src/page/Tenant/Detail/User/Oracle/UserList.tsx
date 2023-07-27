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
import { history, connect } from 'umi';
import React, { useState } from 'react';
import { Table, Tooltip, Space, Switch, Modal, message } from '@oceanbase/design';
import { uniq } from 'lodash';
import { sortByMoment } from '@oceanbase/util';
import { useRequest } from 'ahooks';
import * as ObTenantSessionController from '@/service/ocp-express/ObTenantSessionController';
import * as ObUserController from '@/service/ocp-express/ObUserController';
import { PAGINATION_OPTION_10 } from '@/constant';
import { formatTime } from '@/util/datetime';
import ModifyDbUserPassword from '../../Component/ModifyDbUserPassword';
import DeleteUserModal from '../Component/DeleteUserModal';
import OBProxyAndConnectionStringModal from '../../Component/OBProxyAndConnectionStringModal';
import RenderConnectionString from '@/component/RenderConnectionString';

const { confirm } = Modal;

export interface UserProps {
  tenantId: number;
  dbUserLoading: boolean;
  dbUserList: [];
  refreshDbUser: () => void;
  tenantData: API.TenantInfo;
}

const UserList: React.FC<UserProps> = ({
  tenantId,
  dbUserLoading,
  dbUserList,
  refreshDbUser,
  tenantData,
}) => {
  const [current, setCurrent] = useState<API.DbUser | null>(null);
  const [userStats, setuserStats] = useState<any[]>([]);
  // 修改密码
  const [modifyPasswordVisible, setModifyPasswordVisible] = useState(false);
  // 删除Modal
  const [deleteModalVisible, setDeleteModalVisible] = useState(false);
  const [connectionStringModalVisible, setConnectionStringModalVisible] = useState(false);

  const { data, run } = useRequest(ObTenantSessionController.getSessionStats, {
    defaultParams: [
      {
        tenantId,
      },
    ],

    onSuccess: res => {
      if (res.successful) {
        setuserStats(data?.data?.userStats || []);
      }
    },
  });

  const modifyPassword = (record: API.DbUser) => {
    run({ tenantId });
    setModifyPasswordVisible(true);
    setCurrent(record);
  };

  const { runAsync: unlockDbUser } = useRequest(ObUserController.unlockDbUser, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        message.success(
          formatMessage({
            id: 'ocp-express.User.Oracle.UserList.TheUserIsUnlocked',
            defaultMessage: '用户解锁成功',
          })
        );
        refreshDbUser();
      }
    },
  });

  const { runAsync: lockDbUser } = useRequest(ObUserController.lockDbUser, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        message.success(
          formatMessage({
            id: 'ocp-express.User.Oracle.UserList.TheUserIsLocked',
            defaultMessage: '用户锁定成功',
          })
        );
        refreshDbUser();
      }
    },
  });

  const changeLockedStatus = (record: API.DbUser) => {
    let iconType: any = null;
    if (!record.isLocked) {
      iconType = {
        title: formatMessage({
          id: 'ocp-express.User.Oracle.UserList.LockedUsersAreNotAllowed',
          defaultMessage: '被锁定的用户将不允许登录，请谨慎操作',
        }),
        apiType: lockDbUser,
      };
    } else {
      iconType = {
        title: formatMessage({
          id: 'ocp-express.User.Oracle.UserList.UnlockedUsersWillAllowThem',
          defaultMessage: '解锁用户将允许其登录',
        }),
        apiType: unlockDbUser,
      };
    }
    confirm({
      title: iconType.title,
      icon: iconType.icon,
      okText: formatMessage({
        id: 'ocp-express.User.Oracle.UserList.Determine',
        defaultMessage: '确定',
      }),
      cancelText: formatMessage({
        id: 'ocp-express.User.Oracle.UserList.Cancel',
        defaultMessage: '取消',
      }),
      content: (
        <div style={{ color: '#5C6B8A' }}>
          <div>
            {formatMessage(
              {
                id: 'ocp-express.User.Oracle.UserList.TenantTenantdataname',
                defaultMessage: '租户：{tenantDataName}',
              },
              { tenantDataName: tenantData.name }
            )}
          </div>
          <div>
            {formatMessage(
              {
                id: 'ocp-express.User.Oracle.UserList.UsernameRecordusername',
                defaultMessage: '用户名：{recordUsername}',
              },
              { recordUsername: record.username }
            )}
          </div>
        </div>
      ),
      onOk() {
        return iconType.apiType({ tenantId, username: record.username });
      },
    });
  };

  const columns = [
    {
      title: formatMessage({
        id: 'ocp-express.User.Oracle.UserList.UserName',
        defaultMessage: '用户名',
      }),
      dataIndex: 'username',
      render: (text: string, record: API.DbUser) => (
        <Tooltip placement="topLeft" title={text}>
          <a onClick={() => history.push(`/tenant/${tenantId}/user/${record?.username}`)}>{text}</a>
        </Tooltip>
      ),
    },

    {
      title: formatMessage({
        id: 'ocp-express.User.Oracle.UserList.HaveSystemPermissions',
        defaultMessage: '拥有系统权限',
      }),
      dataIndex: 'globalPrivileges',
      render: (text: string[]) => {
        const textContent = text?.map(item => item?.replace(/_/g, ' ')).join('、');
        return textContent ? (
          <Tooltip placement="topLeft" title={textContent}>
            <span
              style={{
                maxWidth: 180,
              }}
              className="ellipsis"
            >
              {textContent}
            </span>
          </Tooltip>
        ) : (
          '-'
        );
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.User.Oracle.UserList.HaveARole',
        defaultMessage: '拥有角色',
      }),
      dataIndex: 'grantedRoles',
      render: (text: string[]) => {
        const textContent = text?.join('、');
        return textContent ? (
          <Tooltip placement="topLeft" title={textContent}>
            <span
              style={{
                maxWidth: 180,
              }}
              className="ellipsis"
            >
              {textContent}
            </span>
          </Tooltip>
        ) : (
          '-'
        );
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.User.Oracle.RolesList.AccessibleObjects',
        defaultMessage: '可访问对象',
      }),
      dataIndex: 'objectPrivileges',
      render: (text: API.ObjectPrivilege[]) => {
        const objectPrivileges = uniq(text?.map(item => item?.object?.fullName));
        const textContent = objectPrivileges?.join('、');
        return textContent ? (
          <Tooltip placement="topLeft" title={textContent}>
            <span
              style={{
                maxWidth: 180,
              }}
              className="ellipsis"
            >
              {textContent}
            </span>
          </Tooltip>
        ) : (
          '-'
        );
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.User.Oracle.UserList.CreationTime',
        defaultMessage: '新建时间',
      }),
      dataIndex: 'createTime',
      defaultSortOrder: 'descend',
      sorter: (a: API.DbUser, b: API.DbUser) => sortByMoment(a, b, 'createTime'),
      render: (text: string) => formatTime(text),
    },

    {
      title: formatMessage({
        id: 'ocp-express.User.Oracle.UserList.LogonConnectionString',
        defaultMessage: '登录连接串',
      }),
      dataIndex: 'connectionStrings',
      render: (connectionStrings: API.ObproxyAndConnectionString[], record: API.DbUser) => {
        return (
          <RenderConnectionString
            callBack={() => {
              setCurrent(record);
              setConnectionStringModalVisible(true);
            }}
            connectionStrings={connectionStrings}
          />
        );
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.User.Oracle.UserList.Locking',
        defaultMessage: '锁定',
      }),
      dataIndex: 'isLocked',
      render: (text: boolean, record: API.DbUser) =>
        record.username === 'SYS' ? (
          <Tooltip
            placement="topRight"
            title={formatMessage({
              id: 'ocp-express.User.Oracle.UserList.YouCannotLockUnlockOr',
              defaultMessage: 'SYS 用户（相当于 MySQL 租户的 root 用户）不支持锁定',
            })}
          >
            <Switch size="small" checked={text} disabled={true} />
          </Tooltip>
        ) : (
          <Switch
            data-aspm-click="c304262.d308778"
            data-aspm-desc="Oracle 用户列表-切换锁定状态"
            data-aspm-param={``}
            data-aspm-expo
            checked={text}
            size="small"
            onChange={() => {
              changeLockedStatus(record);
            }}
          />
        ),
    },
    {
      title: formatMessage({
        id: 'ocp-express.User.Oracle.UserList.Operation',
        defaultMessage: '操作',
      }),
      dataIndex: 'operation',
      render: (text: string, record: API.DbUser) => {
        return (
          <Space size="middle">
            <a
              data-aspm-click="c304262.d308776"
              data-aspm-desc="Oracle 用户列表-修改密码"
              data-aspm-param={``}
              data-aspm-expo
              onClick={() => {
                modifyPassword(record);
              }}
            >
              {formatMessage({
                id: 'ocp-express.User.Oracle.UserList.ChangePassword',
                defaultMessage: '修改密码',
              })}
            </a>
            {record.username !== 'SYS' && (
              <a
                data-aspm-click="c304262.d308775"
                data-aspm-desc="Oracle 用户列表-删除用户"
                data-aspm-param={``}
                data-aspm-expo
                onClick={() => {
                  setCurrent(record);
                  setDeleteModalVisible(true);
                }}
              >
                {formatMessage({
                  id: 'ocp-express.User.Oracle.UserList.Delete',
                  defaultMessage: '删除',
                })}
              </a>
            )}
          </Space>
        );
      },
    },
  ];

  return (
    <>
      <Table
        data-aspm="c304262"
        data-aspm-desc="Oracle 用户列表"
        data-aspm-param={``}
        data-aspm-expo
        columns={columns}
        rowKey="id"
        loading={dbUserLoading}
        dataSource={dbUserList}
        pagination={PAGINATION_OPTION_10}
      />

      <ModifyDbUserPassword
        visible={modifyPasswordVisible}
        dbUser={current}
        userStats={userStats.filter(item => item.dbUser === current?.username)}
        tenantData={tenantData}
        onCancel={() => {
          setModifyPasswordVisible(false);
          setCurrent(null);
        }}
        onSuccess={() => {
          setModifyPasswordVisible(false);
          refreshDbUser();
          setCurrent(null);
        }}
      />

      <DeleteUserModal
        visible={deleteModalVisible}
        username={current?.username}
        tenantData={tenantData}
        userStats={userStats.filter(item => item.dbUser === current?.username)}
        onCancel={() => {
          setDeleteModalVisible(false);
          setCurrent(null);
        }}
        onSuccess={() => {
          setDeleteModalVisible(false);
          setCurrent(null);
          refreshDbUser();
        }}
      />

      <OBProxyAndConnectionStringModal
        userName={current?.username}
        width={900}
        visible={connectionStringModalVisible}
        obproxyAndConnectionStrings={current?.connectionStrings || []}
        onCancel={() => {
          setConnectionStringModalVisible(false);
        }}
        onSuccess={() => {
          setConnectionStringModalVisible(false);
        }}
      />
    </>
  );
};

function mapStateToProps({ tenant }) {
  return {
    tenantData: tenant.tenantData,
  };
}

export default connect(mapStateToProps)(UserList);
