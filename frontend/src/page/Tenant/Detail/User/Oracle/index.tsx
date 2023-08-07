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
import { history, useSelector } from 'umi';
import React, { useState, useEffect } from 'react';
import { Button, Space } from '@oceanbase/design';
import { PageContainer } from '@ant-design/pro-components';
import { useRequest } from 'ahooks';
import * as ObUserController from '@/service/ocp-express/ObUserController';
import Empty from '@/component/Empty';
import MyInput from '@/component/MyInput';
import MyCard from '@/component/MyCard';
import ContentWithInfo from '@/component/ContentWithInfo';
import UserList from './UserList';
import RoleList from './RoleList';
import AddOracleUserOrRoleDrawer from './Component/AddOracleUserOrRoleDrawer';
import useStyles from './index.style';

export interface IndexProps {
  pathname?: string;
  tenantId: number;
}

const Index: React.FC<IndexProps> = ({ pathname, tenantId }) => {
  const { styles } = useStyles();
  const { tenantData } = useSelector((state: DefaultRootState) => state.tenant);

  const [keyword, setKeyword] = useState('');
  const pathnameList: string[] = pathname?.split('/') || [];
  const [tab, setTab] = useState(pathnameList[pathnameList?.length - 1]);

  useEffect(() => {
    if (tab === 'user') {
      getDbUserList({
        tenantId,
      });
    } else if (tab === 'role') {
      getDbRoleList({
        tenantId,
      });
    }
  }, [tab]);

  // 新增用户 / 角色
  const [addUserVisible, setAddUserVisible] = useState(false);
  //  新增的是用户还是角色
  const [addIsUser, setAddIsUser] = useState('user');

  // 获取用户列表
  const {
    run: getDbUserList,
    data: dbUserListData,
    refresh: refreshDbUser,
    loading: dbUserLoading,
  } = useRequest(ObUserController.listDbUsers, {
    manual: true,
  });

  const dbUserList = dbUserListData?.data?.contents || [];
  // 获取角色列表
  const {
    run: getDbRoleList,
    data: dbRoleListData,
    refresh: refreshDbRole,
    loading: dbRoleLoading,
  } = useRequest(ObUserController.listDbRoles, {
    manual: true,
  });

  const dbRoleList = dbRoleListData?.data?.contents || [];

  return tenantData.status === 'CREATING' ? (
    <Empty
      title={formatMessage({ id: 'ocp-express.User.Oracle.NoData', defaultMessage: '暂无数据' })}
      description={formatMessage({
        id: 'ocp-express.User.Oracle.TheTenantIsBeingCreated',
        defaultMessage: '租户正在创建中，请等待租户创建完成',
      })}
    >
      <Button
        type="primary"
        onClick={() => {
          history.push(`/tenant/${tenantId}`);
        }}
      >
        {formatMessage({
          id: 'ocp-express.User.Oracle.AccessTheOverviewPage',
          defaultMessage: '访问总览页',
        })}
      </Button>
    </Empty>
  ) : (
    <PageContainer
      className={styles.container}
      ghost={true}
      header={{
        title: (
          <Space>
            {formatMessage({
              id: 'ocp-express.User.Oracle.UserManagement',
              defaultMessage: '用户管理',
            })}
            <ContentWithInfo
              content={
                <span
                  style={{
                    marginLeft: 8,
                  }}
                >
                  {formatMessage({
                    id: 'ocp-express.User.Oracle.SystemPermissionsRolesAndAccessibleObjectsCanBe',
                    defaultMessage: '系统权限、拥有角色和可访问对象可进入用户和角色详情页进行修改',
                  })}
                </span>
              }
              size={0}
            />
          </Space>
        ),

        extra: (
          <Space>
            <Button
              data-aspm-click="c304260.d308767"
              data-aspm-desc="Oracle 角色列表-新建角色"
              data-aspm-param={``}
              data-aspm-expo
              onClick={() => {
                setAddUserVisible(true);
                setAddIsUser('role');
              }}
            >
              {formatMessage({
                id: 'ocp-express.User.Oracle.CreateARole',
                defaultMessage: '新建角色',
              })}
            </Button>
            <Button
              data-aspm-click="c304262.d308773"
              data-aspm-desc="Oracle 用户列表-新建用户"
              data-aspm-param={``}
              data-aspm-expo
              type="primary"
              onClick={() => {
                setAddUserVisible(true);
                setAddIsUser('user');
              }}
            >
              {formatMessage({
                id: 'ocp-express.User.Oracle.CreateUser',
                defaultMessage: '新建用户',
              })}
            </Button>
          </Space>
        ),
      }}
    >
      <MyCard
        bordered={false}
        className={`card-without-padding ${styles.card}`}
        activeTabKey={tab}
        onTabChange={(key) => {
          setTab(key);
          history.push({
            pathname: `/tenant/${tenantId}/user${key === 'role' ? '/role' : ''}`,
          });
        }}
        tabList={[
          {
            key: 'user',
            tab: formatMessage({
              id: 'ocp-express.User.Oracle.UserList',
              defaultMessage: '用户列表',
            }),
          },

          {
            key: 'role',
            tab: formatMessage({
              id: 'ocp-express.User.Oracle.RoleList',
              defaultMessage: '角色列表',
            }),
          },
        ]}
        tabBarExtraContent={
          <MyInput.Search
            allowClear={true}
            onSearch={(value: string) => setKeyword(value)}
            onChange={(e) => setKeyword(e.target.value)}
            placeholder={formatMessage({
              id: 'ocp-express.User.Oracle.EnterAUsernameAndRole',
              defaultMessage: '请输入用户名、角色名',
            })}
            className="search-input"
          />
        }
      >
        {tab === 'user' && (
          <UserList
            dbUserLoading={dbUserLoading}
            refreshDbUser={refreshDbUser}
            dbUserList={dbUserList?.filter(
              (item) =>
                !keyword ||
                (item.username && item.username.includes(keyword)) ||
                (item.grantedRoles &&
                  item.grantedRoles.filter((o) => o.includes(keyword)).length > 0),
            )}
            tenantId={tenantId}
          />
        )}

        {tab === 'role' && (
          <RoleList
            tenantId={tenantId}
            dbRoleLoading={dbRoleLoading}
            refreshDbRole={refreshDbRole}
            dbRoleList={dbRoleList?.filter(
              (item) =>
                !keyword ||
                (item.name && item.name.includes(keyword)) ||
                (item.grantedRoles &&
                  item.grantedRoles.filter((o) => o.includes(keyword)).length > 0),
            )}
          />
        )}
      </MyCard>
      <AddOracleUserOrRoleDrawer
        visible={addUserVisible}
        tenantId={tenantId}
        addIsUser={addIsUser}
        onCancel={() => {
          setAddUserVisible(false);
        }}
        onSuccess={() => {
          setAddUserVisible(false);
          if (tab === 'user') {
            refreshDbUser();
          } else if (tab === 'role') {
            refreshDbRole();
          }
        }}
      />
    </PageContainer>
  );
};

export default Index;
