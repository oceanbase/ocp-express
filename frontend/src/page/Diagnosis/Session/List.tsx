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
import {
  Button,
  Card,
  Checkbox,
  Space,
  Tooltip,
  Typography,
  Table,
  Modal,
  message,
  Highlight,
} from '@oceanbase/design';
import React, { useEffect, useImperativeHandle, useState } from 'react';
import { CheckOutlined, CopyOutlined } from '@oceanbase/icons';
import { useRequest } from 'ahooks';
import * as ObTenantSessionController from '@/service/ocp-express/ObTenantSessionController';
import { formatSql, getTableData } from '@/util';
import { getColumnSearchProps, getOperationComponent } from '@/util/component';

const { Text } = Typography;

export interface ListProps {
  tenantId: number;
  query?: {
    dbUser: string;
    dbName: string;
    clientIp: string;
  };

  mode?: 'page' | 'component';
  activeOnly?: boolean;
}

const List = React.forwardRef<
  {
    refreshListTenantSessions: () => Promise<any>;
  },
  ListProps
>(({ tenantId, query, mode = 'page', activeOnly }, ref) => {
  const [visible, setVisible] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<API.TenantSession | null>(null);
  const [selectedRowKeys, setSelectedRowKeys] = useState<number[]>([]);
  const { dbUser, dbName, clientIp } = query || {};
  const [innerActiveOnly, setInertActiveOnly] = useState(false);

  const { tableProps, run, refreshAsync, params } = getTableData({
    fn: ObTenantSessionController.listTenantSessions,
    params: {
      tenantId: tenantId,
      activeOnly: activeOnly || innerActiveOnly,
    },

    deps: [activeOnly, innerActiveOnly, tenantId],
  });

  useImperativeHandle(ref, () => ({
    refreshListTenantSessions: () => {
      return refreshAsync();
    },
  }));

  const { sorter = {} } = params[0] || ({} as any);

  // dbUser、dbName、clientIp 仅在组件挂载时会变化一次，因此下拉函数最多只运行一次
  // 用于会话统计页面，根据不同维度对象进行初始化请求
  // 由于 ahooks useRequest 并不支持 defaultFilters，因此这里通过手动 run 的方式来模拟
  useEffect(() => {
    if (dbUser || dbName || clientIp) {
      // 需要异步发起，以保证当前请求的值是最新的，否则会被默认的请求结果覆盖
      setTimeout(() => {
        // 这里的 run 方法并不是 listTenantSessions，而是 getTableData 的 service 方法，传参均为 antd Table 相关的参数
        run({
          pageSize: 10,
          current: 1,
          filters: {
            dbUser: dbUser ? [dbUser] : [],
            dbName: dbName ? [dbName] : [],
            clientIp: clientIp ? [clientIp] : [],
          },
        });
      }, 0);
    }
  }, [dbUser || dbName || clientIp]);

  // 批量关闭查询
  const { runAsync: closeTenantQuery } = useRequest(ObTenantSessionController.closeTenantQuery, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        message.success(
          formatMessage({
            id: 'ocp-express.Detail.Session.List.TheQueryIsClosed',
            defaultMessage: '查询关闭成功',
          })
        );

        setSelectedRowKeys([]);
        refreshAsync();
      }
    },
  });

  // 批量关闭会话
  const { runAsync: closeTenantSession } = useRequest(
    ObTenantSessionController.closeTenantSession,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage({
              id: 'ocp-express.Detail.Session.List.TheSessionIsClosed',
              defaultMessage: '会话关闭成功',
            })
          );

          setSelectedRowKeys([]);
          refreshAsync();
        }
      },
    }
  );

  const handleOperation = (key: 'closeSession' | 'closeQuery', record: API.TenantSession) => {
    if (key === 'closeSession') {
      Modal.confirm({
        title: formatMessage({
          id: 'ocp-express.Detail.Session.List.AreYouSureYouWant',
          defaultMessage: '确定要关闭会话吗？',
        }),
        onOk: () => {
          return closeTenantSession(
            {
              tenantId: tenantId,
            },

            {
              sessionIds: [record.id as number],
            }
          );
        },
      });
    } else if (key === 'closeQuery') {
      Modal.confirm({
        title: formatMessage({
          id: 'ocp-express.Detail.Session.List.AreYouSureYouWant.2',
          defaultMessage: '确定要关闭查询吗？',
        }),
        onOk: () => {
          return closeTenantQuery(
            {
              tenantId: tenantId,
            },

            {
              sessionIds: [record.id as number],
            }
          );
        },
      });
    }
  };

  const columns = [
    {
      title: formatMessage({
        id: 'ocp-express.Detail.Session.List.SessionId',
        defaultMessage: '会话 ID',
      }),

      dataIndex: 'id',
      render: (text: number) => (
        <Tooltip placement="topLeft" title={text}>
          <span>{text}</span>
        </Tooltip>
      ),
    },

    {
      title: 'SQL',
      dataIndex: 'info',
      render: (text: string, record: API.TenantSession) =>
        text ? (
          <Tooltip placement="topLeft" title={text}>
            <a
              onClick={() => {
                setVisible(true);
                setCurrentRecord(record);
              }}
            >
              {formatMessage({
                id: 'ocp-express.Detail.Session.List.ViewSqlStatements',
                defaultMessage: '查看 SQL',
              })}
            </a>
          </Tooltip>
        ) : (
          '-'
        ),
    },

    {
      title: formatMessage({ id: 'ocp-express.Detail.Session.List.User', defaultMessage: '用户' }),
      dataIndex: 'dbUser',
      ...(dbUser ? { defaultFilteredValue: [dbUser] } : {}),
      ...getColumnSearchProps({
        frontEndSearch: false,
      }),
    },

    {
      title: formatMessage({
        id: 'ocp-express.Detail.Session.List.Source',
        defaultMessage: '来源',
      }),
      dataIndex: 'clientIp',
      ...(clientIp ? { defaultFilteredValue: [clientIp] } : {}),
      ...getColumnSearchProps({
        frontEndSearch: false,
      }),
    },

    {
      title: formatMessage({
        id: 'ocp-express.Detail.Session.List.DatabaseName',
        defaultMessage: '数据库名',
      }),

      dataIndex: 'dbName',
      ...(dbName ? { defaultFilteredValue: [dbName] } : {}),
      ...getColumnSearchProps({
        frontEndSearch: false,
      }),
    },

    {
      title: formatMessage({
        id: 'ocp-express.Detail.Session.List.Command',
        defaultMessage: '命令',
      }),
      dataIndex: 'command',
    },

    {
      title: formatMessage({
        id: 'ocp-express.Detail.Session.List.ExecutionTimeS',
        defaultMessage: '执行时间（s）',
      }),

      dataIndex: 'time',
      sorter: true,
      sortOrder: sorter.field === 'time' && sorter.order,
    },

    {
      title: formatMessage({ id: 'ocp-express.Detail.Session.List.State', defaultMessage: '状态' }),
      dataIndex: 'status',
    },

    {
      title: 'OBProxy',
      dataIndex: 'proxyIp',
      render: (text: string) => <span>{text || '-'}</span>,
    },

    {
      title: formatMessage({
        id: 'ocp-express.Detail.Session.List.Actions',
        defaultMessage: '操作',
      }),
      width: 130,
      dataIndex: 'operation',
      render: (text: string, record: API.TenantSession) => {
        const operations = [
          {
            value: 'closeSession',
            label: formatMessage({
              id: 'ocp-express.Detail.Session.List.CloseSession',
              defaultMessage: '关闭会话',
            }),
          },

          {
            value: 'closeQuery',
            label: formatMessage({
              id: 'ocp-express.Detail.Session.List.DisableTheCurrentQuery',
              defaultMessage: '关闭当前查询',
            }),
          },
        ];

        return getOperationComponent({
          operations,
          handleOperation,
          record,
          displayCount: 1,
        });
      },
    },
  ];

  const currentSessionId = currentRecord && currentRecord.id;

  const toolOptionsRender = () => {
    return [
      <Button
        key="batch-close-query"
        data-aspm-click="c304247.d308743"
        data-aspm-desc="会话列表-批量关闭查询"
        data-aspm-param={``}
        data-aspm-expo
        onClick={() => {
          Modal.confirm({
            title: formatMessage({
              id: 'ocp-express.Detail.Session.List.AreYouSureYouWant.2',
              defaultMessage: '确定要关闭查询吗？',
            }),
            onOk: () => {
              return closeTenantQuery(
                {
                  tenantId,
                },

                {
                  sessionIds: selectedRowKeys,
                }
              );
            },
          });
        }}
      >
        {formatMessage({
          id: 'ocp-express.Detail.Session.List.CloseTheCurrentQuery',
          defaultMessage: '关闭当前查询',
        })}
      </Button>,
      <Button
        key="batch-close-session"
        data-aspm-click="c304247.d308742"
        data-aspm-desc="会话列表-批量关闭会话"
        data-aspm-param={``}
        data-aspm-expo
        onClick={() => {
          Modal.confirm({
            title: formatMessage({
              id: 'ocp-express.Detail.Session.List.AreYouSureYouWant',
              defaultMessage: '确定要关闭会话吗？',
            }),
            onOk: () => {
              return closeTenantSession(
                {
                  tenantId,
                },

                {
                  sessionIds: selectedRowKeys,
                }
              );
            },
          });
        }}
      >
        {formatMessage({
          id: 'ocp-express.Detail.Session.List.CloseSession',
          defaultMessage: '关闭会话',
        })}
      </Button>,
    ];
  };

  const toolSelectedContent = (_selectedRowKeys: any, selectedRows: any) => {
    return (
      <Table
        columns={columns.filter(item =>
          ['id', 'clientIp', 'dbName', 'command', 'time', 'proxyIp'].includes(item.dataIndex)
        )}
        dataSource={selectedRows}
        pagination={false}
      />
    );
  };

  return (
    <div>
      <Card
        bordered={false}
        className="card-without-padding"
        extra={
          mode === 'component' && (
            <Checkbox
              checked={innerActiveOnly}
              onChange={e => {
                setInertActiveOnly(e.target.checked);
              }}
            >
              {formatMessage({
                id: 'ocp-express.Detail.Session.List.ViewOnlyActiveSessions',
                defaultMessage: '仅查看活跃会话',
              })}
            </Checkbox>
          )
        }
      >
        <Table
          data-aspm="c304247"
          data-aspm-desc="会话列表"
          data-aspm-param={``}
          data-aspm-expo
          columns={columns}
          rowKey={(record: API.TenantSession) => record.id}
          toolOptionsRender={toolOptionsRender}
          toolSelectedContent={toolSelectedContent}
          rowSelection={{
            selectedRowKeys,
            onChange: (keys: React.Key[]) => {
              setSelectedRowKeys(keys);
            },
          }}
          {...tableProps}
        />
      </Card>

      <Modal
        width={760}
        title={formatMessage(
          {
            id: 'ocp-express.Detail.Session.List.SqlDetailsCurrentsessionid',
            defaultMessage: 'SQL 详情 - {currentSessionId}',
          },

          { currentSessionId }
        )}
        visible={visible}
        footer={
          <Space>
            <Text
              copyable={{
                text: formatSql(currentRecord && currentRecord.info),
                icon: [
                  <Button
                    data-aspm-click="c318541.d343265"
                    data-aspm-desc="SQL 详情弹窗-复制"
                    data-aspm-param={``}
                    data-aspm-expo
                    key="copy"
                    icon={<CopyOutlined />}
                  >
                    <span>
                      {formatMessage({
                        id: 'ocp-express.Detail.Session.List.Copy',
                        defaultMessage: '复制',
                      })}
                    </span>
                  </Button>,
                  // CheckOutlined 不能设置为 Button.icon，否则复制后 Tooltip 不会主动消失
                  <Button key="check">
                    <CheckOutlined />
                  </Button>,
                ],
              }}
            />
            <Button
              data-aspm-click="c318541.d343264"
              data-aspm-desc="SQL 详情弹窗-关闭"
              data-aspm-param={``}
              data-aspm-expo
              type="primary"
              onClick={() => {
                setVisible(false);
                setCurrentRecord(null);
              }}
            >
              {formatMessage({
                id: 'ocp-express.Detail.Session.List.Closed',
                defaultMessage: '关闭',
              })}
            </Button>
          </Space>
        }
        onCancel={() => {
          setVisible(false);
          setCurrentRecord(null);
        }}
        bodyStyle={{
          maxHeight: 300,
          overflow: 'auto',
        }}
      >
        <div data-aspm="c318541" data-aspm-desc="SQL 详情弹窗" data-aspm-param={``} data-aspm-expo>
          <Highlight language="sql" copyable={false}>
            {formatSql(currentRecord && currentRecord.info)}
          </Highlight>
        </div>
      </Modal>
    </div>
  );
});

export default List;
