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
import { history, useDispatch } from 'umi';
import {
  Badge,
  Card,
  Input,
  Space,
  Table,
  Tooltip,
  Popconfirm,
  Button,
  Modal,
} from '@oceanbase/design';
import React, { useState } from 'react';
import { directTo, findByValue } from '@oceanbase/util';
import { DATE_FORMAT_DISPLAY } from '@/constant/datetime';
import { TENANT_MODE_LIST, TENANT_STATUS_LIST } from '@/constant/tenant';
import { getBooleanLabel, getTableData } from '@/util';
import { formatTime } from '@/util/datetime';
import { useRequest } from 'ahooks';
import * as ObTenantController from '@/service/ocp-express/ObTenantController';
import tracert from '@/util/tracert';
import RenderConnectionString from '@/component/RenderConnectionString';

import styles from './index.less';

export interface TenantListProps {
  statusList: API.TenantStatus[];
}

const TenantList: React.FC<TenantListProps> = ({ statusList: initialStatusList }) => {
  const dispatch = useDispatch();
  const [keyword, setKeyword] = useState('');

  const [modeList, setModeList] = useState<string[]>([]);

  const [statusList, setStatusList] = useState<string[]>(initialStatusList);

  const { tableProps, refresh } = getTableData({
    fn: ObTenantController.listTenants,
    params: {
      name: keyword,
      mode: modeList,
      status: statusList,
    },
    deps: [keyword, modeList.join(','), statusList.join(',')],
  });

  const onSuccess = () => {
    refresh();
  };

  // 删除租户
  const { runAsync: deleteTenant } = useRequest(ObTenantController.deleteTenant, {
    manual: true,
  });

  const handleLock = (record: API.Tenant) => {
    // 锁定
    Modal.confirm({
      title: formatMessage(
        {
          id: 'ocp-express.component.TenantList.AreYouSureYouWant',
          defaultMessage: '确定要锁定租户 {recordName} 吗？',
        },

        { recordName: record.name }
      ),

      content: formatMessage({
        id: 'ocp-express.component.TenantList.LockingATenantWillCause',
        defaultMessage: '锁定租户会导致用户无法访问该租户，请谨慎操作',
      }),

      okText: formatMessage({
        id: 'ocp-express.component.TenantList.Locking',
        defaultMessage: '锁定',
      }),
      okButtonProps: {
        danger: true,
        ghost: true,
      },

      onOk: () => {
        dispatch({
          type: 'tenant/lockTenant',
          payload: {
            tenantId: record.obTenantId,
          },

          onSuccess,
        });
      },
    });
  };

  const handleUnlock = (record: API.Tenant) => {
    // 解锁
    Modal.confirm({
      title: formatMessage(
        {
          id: 'ocp-express.component.TenantList.AreYouSureYouWant.1',
          defaultMessage: '确定要解锁租户 {recordName} 吗？',
        },

        { recordName: record.name }
      ),

      content: formatMessage({
        id: 'ocp-express.component.TenantList.UnlockingTheTenantRestoresUser',
        defaultMessage: '解锁租户将恢复用户对租户的访问',
      }),

      okText: formatMessage({
        id: 'ocp-express.component.TenantList.Unlock',
        defaultMessage: '解锁',
      }),
      okButtonProps: {
        danger: true,
        ghost: true,
      },

      onOk: () => {
        dispatch({
          type: 'tenant/unlockTenant',
          payload: {
            tenantId: record.obTenantId,
          },

          onSuccess,
        });
      },
    });
  };

  const columns = [
    {
      title: formatMessage({
        id: 'ocp-express.component.TenantList.NameOfTheTenant',
        defaultMessage: '租户名',
      }),

      dataIndex: 'name',
      render: (text: string, record: API.Tenant) => {
        if (record.status === 'CREATING') {
          return record.name;
        }
        const pathname = `/tenant/${record.obTenantId}`;
        return (
          <a
            data-aspm-click="c304184.d308808"
            data-aspm-desc="租户列表-跳转租户详情"
            data-aspm-param={``}
            data-aspm-expo
            onClick={() => {
              history.push(pathname);
            }}
          >
            {record.name}
          </a>
        );
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.component.TenantList.TenantMode',
        defaultMessage: '租户模式',
      }),

      dataIndex: 'mode',
      filters: TENANT_MODE_LIST.map(item => ({
        text: item.label,
        value: item.value,
      })),
      filteredValue: modeList,
      render: (text: API.TenantMode) => <span>{findByValue(TENANT_MODE_LIST, text).label}</span>,
    },

    {
      title: formatMessage({
        id: 'ocp-express.component.TenantList.ConnectionString',
        defaultMessage: '连接字符串',
      }),

      dataIndex: 'obproxyAndConnectionStrings',
      render: (obproxyAndConnectionStrings: API.ObproxyAndConnectionString[]) => {
        return (
          <RenderConnectionString
            data-aspm-click="c304184.d308808"
            data-aspm-desc="租户列表-跳转租户详情"
            data-aspm-param={``}
            data-aspm-expo
            maxWidth={360}
            connectionStrings={obproxyAndConnectionStrings}
          />
        );
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.component.TenantList.ReadOnly',
        defaultMessage: '只读',
      }),
      dataIndex: 'readonly',
      render: (text: boolean) => <span>{getBooleanLabel(text)}</span>,
    },

    {
      title: formatMessage({
        id: 'ocp-express.component.TenantList.Locked',
        defaultMessage: '锁定',
      }),
      dataIndex: 'locked',
      render: (text: boolean) => <span>{getBooleanLabel(text)}</span>,
    },

    {
      title: formatMessage({
        id: 'ocp-express.component.TenantList.State',
        defaultMessage: '状态',
      }),
      dataIndex: 'status',
      filters: TENANT_STATUS_LIST.map(item => ({
        text: item.label,
        value: item.value,
      })),
      filteredValue: statusList,
      render: (text: API.TenantStatus) => {
        const statusItem = findByValue(TENANT_STATUS_LIST, text);
        return <Badge status={statusItem.badgeStatus} text={statusItem.label} />;
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.component.TenantList.CreationTime',
        defaultMessage: '创建时间',
      }),

      dataIndex: 'createTime',
      sorter: true,
      render: (text: string) => <span>{formatTime(text, DATE_FORMAT_DISPLAY)}</span>,
    },

    {
      title: formatMessage({
        id: 'ocp-express.component.TenantList.Operation',
        defaultMessage: '操作',
      }),
      dataIndex: 'operation',
      render: (text: string, record: API.Tenant) => {
        if (record.status === 'CREATING') {
          return (
            <a
              data-aspm-click="c304184.d308812"
              data-aspm-desc="租户列表-查看创建任务"
              data-aspm-param={``}
              data-aspm-expo
              onClick={() => history.push(`/task`)}
            >
              {formatMessage({
                id: 'ocp-express.component.TenantList.ViewTasks',
                defaultMessage: '查看任务',
              })}
            </a>
          );
        } else if (record.status === 'FAILED') {
          return (
            <Space size="middle">
              <a
                data-aspm-click="c304184.d308811"
                data-aspm-desc="租户列表-查看失败任务"
                data-aspm-param={``}
                data-aspm-expo
                onClick={() => history.push(`/task`)}
              >
                {formatMessage({
                  id: 'ocp-express.component.TenantList.ViewTasks',
                  defaultMessage: '查看任务',
                })}
              </a>
              <Popconfirm
                placement="topRight"
                title={formatMessage({
                  id: 'ocp-express.component.TenantList.AreYouSureYouWantToDeleteThis',
                  defaultMessage: '确定要删除此租户吗？',
                })}
                onConfirm={() =>
                  deleteTenant({
                    tenantId: record.obTenantId,
                  }).then(() => refresh())
                }
              >
                <a
                  data-aspm-click="c304184.d308806"
                  data-aspm-desc="租户列表-删除失败租户"
                  data-aspm-param={``}
                  data-aspm-expo
                >
                  {formatMessage({
                    id: 'ocp-express.component.TenantList.Delete',
                    defaultMessage: '删除',
                  })}
                </a>
              </Popconfirm>
            </Space>
          );
        } else {
          return (
            <Space size="middle">
              {record.locked ? (
                <Tooltip
                  title={
                    record.name === 'sys' &&
                    formatMessage({
                      id: 'ocp-express.component.TenantList.TheSysTenantDoesNot',
                      defaultMessage: 'sys 租户不支持解锁',
                    })
                  }
                  placement="topRight"
                >
                  <a
                    data-aspm-click="c304184.d308810"
                    data-aspm-desc="租户列表-解锁租户"
                    data-aspm-param={``}
                    data-aspm-expo
                    disabled={record.name === 'sys'}
                    onClick={() => handleUnlock(record)}
                  >
                    {formatMessage({
                      id: 'ocp-express.component.TenantList.Unlock',
                      defaultMessage: '解锁',
                    })}
                  </a>
                </Tooltip>
              ) : (
                <Tooltip
                  title={
                    record.name === 'sys' &&
                    formatMessage({
                      id: 'ocp-express.component.TenantList.SysTenantsCannotBeLocked',
                      defaultMessage: 'sys 租户不支持锁定',
                    })
                  }
                  placement="topRight"
                >
                  <Button
                    data-aspm-click="c304184.d308807"
                    data-aspm-desc="租户列表-锁定租户"
                    data-aspm-param={``}
                    data-aspm-expo
                    type="link"
                    disabled={record.name === 'sys'}
                    onClick={() => handleLock(record)}
                  >
                    {formatMessage({
                      id: 'ocp-express.component.TenantList.Locking',
                      defaultMessage: '锁定',
                    })}
                  </Button>
                </Tooltip>
              )}

              <a
                data-aspm-click="c304184.d308813"
                data-aspm-desc="租户列表-复制租户"
                data-aspm-param={``}
                data-aspm-expo
                onClick={() => {
                  directTo(`/tenant/new?tenantId=${record.obTenantId}`);
                }}
              >
                {formatMessage({
                  id: 'ocp-express.component.TenantList.Copy',
                  defaultMessage: '复制',
                })}
              </a>
            </Space>
          );
        }
        // } else {
        //   return '-';
        // }
      },
    },
  ];

  return (
    <Card
      bordered={false}
      title={formatMessage({
        id: 'ocp-express.component.TenantList.TenantList',
        defaultMessage: '租户列表',
      })}
      className={`card-without-padding ${styles.container}`}
      extra={
        <span className={styles.extra}>
          <Input.Search
            data-aspm-click="c304184.d308809"
            data-aspm-desc="租户列表-搜索租户"
            data-aspm-param={``}
            data-aspm-expo
            onSearch={(value: string) => {
              setKeyword(value);
            }}
            className="search-input-small"
            placeholder={formatMessage({
              id: 'ocp-express.component.TenantList.EnterTheTenantName',
              defaultMessage: '输入租户名',
            })}
            allowClear={true}
          />
        </span>
      }
    >
      <div
        data-aspm="c304184"
        data-aspm-desc="租户列表"
        data-aspm-expo
        // 扩展参数
        data-aspm-param={tracert.stringify({
          // 租户数
          tenantCount: tableProps.pagination?.total,
        })}
      >
        <Table
          {...tableProps}
          onChange={(pagination, filters, sorter) => {
            // 如果筛选状态变化，则接管其 onChange 的逻辑
            if (filters.mode?.join(',') !== modeList.join(',')) {
              setModeList(filters.mode || []);
            } else if (filters.status?.join(',') !== statusList.join(',')) {
              setStatusList(filters.status || []);
            } else if (tableProps.onChange) {
              tableProps.onChange(pagination, filters, sorter);
            }
          }}
          columns={columns}
          rowKey={(record: API.Tenant) => record.id}
        />
      </div>
    </Card>
  );
};

export default TenantList;
