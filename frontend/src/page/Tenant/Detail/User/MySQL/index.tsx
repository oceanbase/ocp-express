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
import { useSelector } from 'umi';
import React, { useState } from 'react';
import {
  Table,
  Col,
  Row,
  Card,
  Tooltip,
  Button,
  Space,
  Switch,
  Modal,
  message,
} from '@oceanbase/design';
import { PageContainer } from '@ant-design/pro-components';
import { useRequest } from 'ahooks';
import * as TenantSessionService from '@/service/ocp-express/ObTenantSessionController';
import * as ObUserController from '@/service/ocp-express/ObUserController';
import { PAGINATION_OPTION_10 } from '@/constant';
import ContentWithReload from '@/component/ContentWithReload';
import MyInput from '@/component/MyInput';
import AddUserDrawer from '../../Component/AddUserDrawer';
import ModifyDbUserPassword from '../../Component/ModifyDbUserPassword';
import DeleteUserModal from '../Component/DeleteUserModal';
import OBProxyAndConnectionStringModal from '../../Component/OBProxyAndConnectionStringModal';
import RenderConnectionString from '@/component/RenderConnectionString';

export interface IndexProps {
  tenantId: number;
}

const Index: React.FC<IndexProps> = ({ tenantId }) => {
  const { tenantData } = useSelector((state: DefaultRootState) => state.tenant);

  const [keyword, setKeyword] = useState('');
  const [connectionStringModalVisible, setConnectionStringModalVisible] = useState(false);
  const [dbUser, setDbUser] = useState<API.DbUser | null>(null);
  const [userStats, setUserStats] = useState<API.SessionUserStats[]>([]);

  // 修改参数值的抽屉是否可见
  const [valueVisible, setValueVisible] = useState(false);
  // 修改密码
  const [modifyPasswordVisible, setModifyPasswordVisible] = useState(false);
  // 删除Modal
  const [deleteModalVisible, setDeleteModalVisible] = useState(false);

  const {
    data: dbUserListData,
    refresh,
    loading,
  } = useRequest(ObUserController.listDbUsers, {
    defaultParams: [
      {
        tenantId,
      },
    ],
  });

  const dbUserList = dbUserListData?.data?.contents || [];
  const dataSource = dbUserList?.filter(
    item => !keyword || (item.username && item.username.includes(keyword))
  );

  const { data, run } = useRequest(TenantSessionService.getSessionStats, {
    defaultParams: [
      {
        tenantId,
      },
    ],

    onSuccess: res => {
      if (res.successful) {
        setUserStats(data?.data?.userStats || []);
      }
    },
  });

  const modifyPassword = (record: API.DbUser) => {
    run({ tenantId });
    setModifyPasswordVisible(true);
    setDbUser(record);
  };
  const modifyPrivileges = (record: API.DbUser) => {
    setValueVisible(true);
    setDbUser(record);
  };
  const { runAsync: unlockDbUser } = useRequest(ObUserController.unlockDbUser, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        message.success(
          formatMessage({
            id: 'ocp-express.User.MySQL.TheUserIsUnlocked',
            defaultMessage: '用户解锁成功',
          })
        );

        refresh();
      }
    },
  });

  const { runAsync: lockDbUser } = useRequest(ObUserController.lockDbUser, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        message.success(
          formatMessage({
            id: 'ocp-express.User.MySQL.TheUserIsLocked',
            defaultMessage: '用户锁定成功',
          })
        );

        refresh();
      }
    },
  });

  const changeLockedStatus = (record: API.DbUser) => {
    if (record.isLocked) {
      Modal.confirm({
        title: formatMessage({
          id: 'ocp-express.User.MySQL.UnlockedUsersWillAllowThem',
          defaultMessage: '解锁用户将允许其登录',
        }),

        content: (
          <div style={{ color: '#5C6B8A' }}>
            <div>
              {formatMessage(
                {
                  id: 'ocp-express.User.MySQL.TenantTenantdataname',
                  defaultMessage: '租户：{tenantDataName}',
                },

                { tenantDataName: tenantData.name }
              )}
            </div>
            <div>
              {formatMessage(
                {
                  id: 'ocp-express.User.MySQL.UsernameRecordusername',
                  defaultMessage: '用户名：{recordUsername}',
                },

                { recordUsername: record.username }
              )}
            </div>
          </div>
        ),

        onOk: () => {
          return unlockDbUser({ tenantId, username: record.username });
        },
      });
    } else {
      Modal.confirm({
        title: formatMessage({
          id: 'ocp-express.User.MySQL.LockedUsersAreNotAllowed',
          defaultMessage: '被锁定的用户将不允许登录，请谨慎操作',
        }),
        content: (
          <div style={{ color: '#5C6B8A' }}>
            <div>
              {formatMessage(
                {
                  id: 'ocp-express.User.MySQL.TenantTenantdataname',
                  defaultMessage: '租户： {tenantDataName}',
                },

                { tenantDataName: tenantData.name }
              )}
            </div>
            <div>
              {formatMessage(
                {
                  id: 'ocp-express.User.MySQL.UsernameRecordusername',
                  defaultMessage: '用户名： {recordUsername}',
                },

                { recordUsername: record.username }
              )}
            </div>
          </div>
        ),

        onOk: () => {
          return lockDbUser({ tenantId, username: record.username });
        },
      });
    }
  };

  const columns = [
    {
      title: formatMessage({ id: 'ocp-express.User.MySQL.UserName', defaultMessage: '用户名' }),
      dataIndex: 'username',
    },

    {
      title: formatMessage({
        id: 'ocp-express.User.MySQL.AccessibleDatabases',
        defaultMessage: '可访问数据库',
      }),

      dataIndex: 'accessibleDatabases',
      render: (text: string[], record: API.DbUser) => {
        if (record.username === 'root' || record.username === 'proxyro') {
          return '*';
        }
        const textContent = text?.join('、');
        return text?.length > 0 ? (
          <Tooltip placement="topLeft" title={textContent}>
            {textContent}
          </Tooltip>
        ) : (
          '-'
        );
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.User.MySQL.LogonConnectionString',
        defaultMessage: '登录连接串',
      }),

      dataIndex: 'connectionStrings',
      render: (connectionStrings: API.ObproxyAndConnectionString[], record: API.DbUser) => {
        return (
          <RenderConnectionString
            callBack={() => {
              setDbUser(record);
              setConnectionStringModalVisible(true);
            }}
            connectionStrings={connectionStrings}
          />
        );
      },
    },

    {
      title: formatMessage({ id: 'ocp-express.User.MySQL.Locking', defaultMessage: '锁定' }),
      dataIndex: 'isLocked',
      render: (text: boolean, record: API.DbUser) =>
        record.username === 'root' || record.username === 'proxyro' ? (
          <Switch size="small" checked={text} disabled={true} />
        ) : (
          <Switch
            data-aspm-click="c304264.d308789"
            data-aspm-desc="MySQL 用户列表-切换锁定状态"
            data-aspm-param={``}
            data-aspm-expo
            onClick={() => changeLockedStatus(record)}
            checked={text}
            size="small"
          />
        ),
    },
    {
      title: formatMessage({
        id: 'ocp-express.User.MySQL.Operation',
        defaultMessage: '操作',
      }),
      dataIndex: 'operation',
      render: (text: string, record: API.DbUser) => {
        return (
          <Space size="middle">
            <a
              data-aspm-click="c304264.d308787"
              data-aspm-desc="MySQL 用户列表-修改密码"
              data-aspm-param={``}
              data-aspm-expo
              onClick={() => {
                modifyPassword(record);
              }}
            >
              {formatMessage({
                id: 'ocp-express.User.MySQL.ChangePassword',
                defaultMessage: '修改密码',
              })}
            </a>
            {record.username !== 'root' && (
              <>
                <a
                  data-aspm-click="c304264.d308784"
                  data-aspm-desc="MySQL 用户列表-修改权限"
                  data-aspm-param={``}
                  data-aspm-expo
                  onClick={() => {
                    modifyPrivileges(record);
                  }}
                >
                  {formatMessage({
                    id: 'ocp-express.User.MySQL.ModifyPermissions',
                    defaultMessage: '修改权限',
                  })}
                </a>
                <a
                  data-aspm-click="c304264.d308786"
                  data-aspm-desc="MySQL 用户列表-删除用户"
                  data-aspm-param={``}
                  data-aspm-expo
                  onClick={() => {
                    setDbUser(record);
                    setDeleteModalVisible(true);
                  }}
                >
                  {formatMessage({
                    id: 'ocp-express.User.MySQL.Delete',
                    defaultMessage: '删除',
                  })}
                </a>
              </>
            )}
          </Space>
        );
      },
    },
  ];

  return (
    <PageContainer
      ghost={true}
      header={{
        title: (
          <ContentWithReload
            content={formatMessage({
              id: 'ocp-express.User.MySQL.UserManagement',
              defaultMessage: '用户管理',
            })}
            spin={loading}
            onClick={() => {
              refresh();
            }}
          />
        ),
        extra: (
          <Button
            data-aspm-click="c304264.d308782"
            data-aspm-desc="MySQL 用户列表-新建用户"
            data-aspm-param={``}
            data-aspm-expo
            type="primary"
            onClick={() => {
              setValueVisible(true);
            }}
          >
            {formatMessage({
              id: 'ocp-express.User.MySQL.CreateUser',
              defaultMessage: '新建用户',
            })}
          </Button>
        ),
      }}
    >
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <div
            data-aspm="c304265"
            data-aspm-desc="MySQL 用户列表"
            data-aspm-param={``}
            data-aspm-expo
          >
            <Card
              title={formatMessage({
                id: 'ocp-express.User.MySQL.UserList',
                defaultMessage: '用户列表',
              })}
              bordered={false}
              className="card-without-padding"
              extra={
                <MyInput.Search
                  data-aspm-click="c304264.d308781"
                  data-aspm-desc="MySQL 用户列表-搜索用户"
                  data-aspm-param={``}
                  data-aspm-expo
                  allowClear={true}
                  onSearch={(value: string) => setKeyword(value)}
                  placeholder={formatMessage({
                    id: 'ocp-express.User.MySQL.SearchUserName',
                    defaultMessage: '搜索用户名',
                  })}
                  className="search-input"
                />
              }
            >
              <Table
                columns={columns}
                rowKey="id"
                loading={loading}
                dataSource={dataSource}
                pagination={PAGINATION_OPTION_10}
              />

              <AddUserDrawer
                visible={valueVisible}
                tenantId={tenantId}
                dbUser={dbUser}
                onCancel={() => {
                  setDbUser(null);
                  setValueVisible(false);
                }}
                onSuccess={() => {
                  setDbUser(null);
                  setValueVisible(false);
                  refresh();
                }}
              />

              <ModifyDbUserPassword
                visible={modifyPasswordVisible}
                dbUser={dbUser}
                userStats={userStats.filter(item => item.dbUser === dbUser?.username)}
                tenantData={tenantData}
                onCancel={() => {
                  setModifyPasswordVisible(false);
                  setDbUser(null);
                }}
                onSuccess={() => {
                  setModifyPasswordVisible(false);
                  setDbUser(null);
                }}
              />

              <DeleteUserModal
                visible={deleteModalVisible}
                username={dbUser?.username}
                tenantData={tenantData}
                userStats={userStats.filter(item => item.dbUser === dbUser?.username)}
                onCancel={() => {
                  setDeleteModalVisible(false);
                  setDbUser(null);
                }}
                onSuccess={() => {
                  setDeleteModalVisible(false);
                  setDbUser(null);
                  refresh();
                }}
              />

              <OBProxyAndConnectionStringModal
                width={900}
                userName={dbUser?.username}
                visible={connectionStringModalVisible}
                obproxyAndConnectionStrings={dbUser?.connectionStrings || []}
                onCancel={() => {
                  setConnectionStringModalVisible(false);
                }}
                onSuccess={() => {
                  setConnectionStringModalVisible(false);
                }}
              />
            </Card>
          </div>
        </Col>
      </Row>
    </PageContainer>
  );
};

export default Index;
