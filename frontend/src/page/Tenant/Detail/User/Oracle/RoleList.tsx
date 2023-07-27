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
import { Table, Tooltip } from '@oceanbase/design';
import { uniq } from 'lodash';
import { PAGINATION_OPTION_10 } from '@/constant';
import { ORACLE_BUILT_IN_ROLE_LIST } from '@/constant/tenant';
import DeleteUserModal from '../Component/DeleteUserModal';

export interface RoleListProps {
  tenantId: number;
  dbRoleLoading?: boolean;
  dbRoleList: [];
  refreshDbRole?: () => void;
  tenantData: API.TenantInfo;
}

const RoleList: React.FC<RoleListProps> = ({
  tenantId,
  dbRoleLoading,
  dbRoleList,
  refreshDbRole,
  tenantData,
}) => {
  const [current, setCurrent] = useState<API.DbRole | null>(null);
  // 删除 Modal
  const [deleteModalVisible, setDeleteModalVisible] = useState(false);

  const columns = [
    {
      title: formatMessage({
        id: 'ocp-express.User.Oracle.RolesList.Role',
        defaultMessage: '角色名',
      }),
      dataIndex: 'name',
      render: (text: string) => (
        <Tooltip placement="topLeft" title={text}>
          <a
            data-aspm-click="c304260.d308766"
            data-aspm-desc="Oracle 角色列表-跳转角色详情"
            data-aspm-param={``}
            data-aspm-expo
            onClick={() => history.push(`/tenant/${tenantId}/user/role/${text}`)}
          >
            {text}
          </a>
        </Tooltip>
      ),
    },

    {
      title: formatMessage({
        id: 'ocp-express.User.Oracle.RolesList.HaveSystemPermissions',
        defaultMessage: '拥有系统权限',
      }),
      dataIndex: 'globalPrivileges',
      render: (text: string[]) => {
        const textContent = text?.map(item => item.replace(/_/g, ' ')).join('、');
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
        id: 'ocp-express.User.Oracle.RolesList.HaveARole',
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
        id: 'ocp-express.User.Oracle.RolesList.ReferencedByTheFollowingRoles',
        defaultMessage: '被以下角色和用户引用',
      }),
      dataIndex: 'roleGrantees',
      render: (text, record: API.DbRole) => {
        const roleGrantees = record.roleGrantees?.join('、');
        const userGrantees = record.userGrantees?.join('、');
        return (
          <>
            <div
              className="ellipsis"
              style={{
                display: 'block',
              }}
            >
              {formatMessage({
                id: 'ocp-express.User.Oracle.RolesList.Role.1',
                defaultMessage: '角色：',
              })}

              <Tooltip placement="topLeft" title={roleGrantees}>
                <span>{roleGrantees || '-'}</span>
              </Tooltip>
            </div>
            <div
              className="ellipsis"
              style={{
                display: 'block',
              }}
            >
              {formatMessage({
                id: 'ocp-express.User.Oracle.RolesList.User',
                defaultMessage: '用户：',
              })}

              <Tooltip placement="topLeft" title={userGrantees}>
                <span>{userGrantees || '-'}</span>
              </Tooltip>
            </div>
          </>
        );
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.User.Oracle.RolesList.Operation',
        defaultMessage: '操作',
      }),
      dataIndex: 'operation',
      render: (text: string, record: API.DbRole) => {
        // 内置角色不支持删除操作
        if (ORACLE_BUILT_IN_ROLE_LIST.includes(record?.name)) {
          return '';
        }
        return (
          <a
            data-aspm-click="c304260.d308770"
            data-aspm-desc="Oracle 角色列表-删除角色"
            data-aspm-param={``}
            data-aspm-expo
            onClick={() => {
              setDeleteModalVisible(true);
              setCurrent(record);
            }}
          >
            {formatMessage({
              id: 'ocp-express.User.Oracle.RolesList.Delete',
              defaultMessage: '删除',
            })}
          </a>
        );
      },
    },
  ];

  return (
    <>
      <Table
        data-aspm="c304260"
        data-aspm-desc="Oracle 角色列表"
        data-aspm-param={``}
        data-aspm-expo
        columns={columns}
        rowKey="id"
        loading={dbRoleLoading}
        dataSource={dbRoleList}
        pagination={PAGINATION_OPTION_10}
      />

      <DeleteUserModal
        visible={deleteModalVisible}
        roleName={current?.name}
        tenantData={tenantData}
        onCancel={() => {
          setDeleteModalVisible(false);
          setCurrent(null);
        }}
        onSuccess={() => {
          setDeleteModalVisible(false);
          setCurrent(null);
          refreshDbRole();
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

export default connect(mapStateToProps)(RoleList);
