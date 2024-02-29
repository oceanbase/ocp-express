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
import { history, connect, useSelector } from 'umi';
import React, { useState } from 'react';
import {
  Table,
  Col,
  Row,
  Card,
  Descriptions,
  Tooltip,
  Button,
  Alert,
  Input,
  Space,
  Modal,
  message,
} from '@oceanbase/design';
import { PageContainer } from '@oceanbase/ui';
import { sortByMoment } from '@oceanbase/util';
import { PAGINATION_OPTION_10 } from '@/constant';
import { FORBID_OPERATION_DBLIST } from '@/constant/tenant';
import { formatTime } from '@/util/datetime';
import * as ObDatabaseController from '@/service/ocp-express/ObDatabaseController';
import { useRequest } from 'ahooks';
import ContentWithReload from '@/component/ContentWithReload';
import MyInput from '@/component/MyInput';
import Empty from '@/component/Empty';
import AddDatabaseDrawer from '../Component/AddDatabaseDrawer';
import OBProxyAndConnectionStringModal from '../Component/OBProxyAndConnectionStringModal';
import RenderConnectionString from '@/component/RenderConnectionString';

export interface DatabaseProps {
  match: {
    params: {
      clusterId: number;
      tenantId: number;
    };
  };

  tenantData: API.TenantInfo;
  dispatch: any;
  loading: boolean;
}

const Database: React.FC<DatabaseProps> = ({
  match: {
    params: { clusterId, tenantId },
  },
  tenantData,
}) => {
  const { systemInfo } = useSelector((state: DefaultRootState) => state.global);

  const [connectionStringModalVisible, setConnectionStringModalVisible] = useState(false);
  const [keyword, setKeyword] = useState('');
  const [currentDatabase, setCurrentDatabase] = useState<API.Database | null>(null);
  const [IsAllowDel, setIsAllowDel] = useState(false);
  // 修改参数值的抽屉是否可见
  const [valueVisible, setValueVisible] = useState(false);
  // 删除Modal
  const [deleteDatabaseModalVisible, setDeleteDatabaseModalVisible] = useState(false);

  const { data, loading, refresh } = useRequest(ObDatabaseController.listDatabases, {
    defaultParams: [
      {
        tenantId,
      },
    ],

    refreshDeps: [keyword],
  });
  const dataSource = data?.data?.contents || [];

  const { runAsync, loading: deleteDatabaseLoading } = useRequest(
    ObDatabaseController.deleteDatabase,
    {
      manual: true,
      defaultParams: [{}],
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage({
              id: 'ocp-express.Detail.Database.TheDatabaseWasDeleted',
              defaultMessage: '数据库删除成功',
            })
          );
          refresh();
          setIsAllowDel(false);
          setCurrentDatabase(null);
          setDeleteDatabaseModalVisible(false);
        }
      },
    }
  );

  const editDatabase = (record: API.Database) => {
    setValueVisible(true);
    setCurrentDatabase(record);
  };

  const confirmOperation = (e: any) => {
    setIsAllowDel(e.target.value === 'delete');
  };

  const deleteDatabase = () => {
    runAsync({
      tenantId,
      dbName: currentDatabase && currentDatabase.dbName,
    });
  };

  const columns = [
    {
      title: formatMessage({
        id: 'ocp-express.Detail.Database.DatabaseName',
        defaultMessage: '数据库名',
      }),

      dataIndex: 'dbName',
    },

    {
      title: formatMessage({
        id: 'ocp-express.Detail.Database.CharacterSet',
        defaultMessage: '字符集',
      }),
      dataIndex: 'charset',
    },

    {
      title: 'Collation',
      dataIndex: 'collation',
      render: (text: string) => (
        <Tooltip placement="topLeft" title={text}>
          {text}
        </Tooltip>
      ),
    },

    {
      title: formatMessage({
        id: 'ocp-express.Detail.Database.JdbcConnectionString',
        defaultMessage: 'JDBC 连接串',
      }),

      dataIndex: 'connectionUrls',
      render: (connectionUrls: API.ObproxyAndConnectionString[], record: API.Database) => {
        return (
          <RenderConnectionString
            callBack={() => {
              setCurrentDatabase(record);
              setConnectionStringModalVisible(true);
            }}
            connectionStrings={connectionUrls}
          />
        );
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.Detail.Database.Created',
        defaultMessage: '创建时间',
      }),
      dataIndex: 'createTime',
      sorter: (a: API.DbUser, b: API.DbUser) => sortByMoment(a, b, 'createTime'),
      render: (text: string) => formatTime(text),
    },

    {
      title: formatMessage({ id: 'ocp-express.Detail.Database.ReadOnly', defaultMessage: '只读' }),
      dataIndex: 'readonly',
      render: (text: boolean) =>
        text
          ? formatMessage({ id: 'ocp-express.Detail.Database.Is', defaultMessage: '是' })
          : formatMessage({ id: 'ocp-express.Detail.Database.No', defaultMessage: '否' }),
    },
    {
      title: formatMessage({
        id: 'ocp-express.Detail.Database.Operation',
        defaultMessage: '操作',
      }),
      dataIndex: 'operation',
      render: (text: string, record: API.Database) => {
        if ([...FORBID_OPERATION_DBLIST, 'information_schema'].includes(record.dbName) || record?.dbName === systemInfo?.metaDatabaseName) {
          return '';
        }
        return (
          <Space size="middle">
            <a
              data-aspm-click="c304244.d308729"
              data-aspm-desc="数据库列表-编辑数据库"
              data-aspm-param={``}
              data-aspm-expo
              onClick={() => {
                editDatabase(record);
              }}
            >
              {formatMessage({
                id: 'ocp-express.Detail.Database.Modify',
                defaultMessage: '编辑',
              })}
            </a>
            <a
              data-aspm-click="c304244.d308728"
              data-aspm-desc="数据库列表-删除数据库"
              data-aspm-param={``}
              data-aspm-expo
              onClick={() => {
                setDeleteDatabaseModalVisible(true);
                setCurrentDatabase(record);
              }}
            >
              {formatMessage({
                id: 'ocp-express.Detail.Database.Delete',
                defaultMessage: '删除',
              })}
            </a>
          </Space>
        );
      },
    },
  ];

  return tenantData.mode === 'ORACLE' ? (
    <Empty
      title={formatMessage({
        id: 'ocp-express.Detail.Database.NoData',
        defaultMessage: '暂无数据',
      })}
      image="/assets/icon/warning.svg"
      description={formatMessage({
        id: 'ocp-express.Detail.Database.OracleTenantsDoNotSupport',
        defaultMessage: 'Oracle 类型的租户暂不支持数据库管理功能',
      })}
    >
      <Button
        type="primary"
        onClick={() => {
          history.push(`/cluster/${clusterId}/tenant/${tenantId}`);
        }}
      >
        {formatMessage({
          id: 'ocp-express.Detail.Database.AccessTheOverviewPage',
          defaultMessage: '访问总览页',
        })}
      </Button>
    </Empty>
  ) : (
    <PageContainer
      ghost={true}
      header={{
        title: (
          <ContentWithReload
            content={formatMessage({
              id: 'ocp-express.Detail.Database.DatabaseManagement',
              defaultMessage: '数据库管理',
            })}
            spin={loading}
            onClick={() => {
              refresh();
            }}
          />
        ),

        extra: (
          <Button
            data-aspm-click="c304244.d308725"
            data-aspm-desc="数据库列表-新建数据库"
            data-aspm-param={``}
            data-aspm-expo
            type="primary"
            onClick={() => {
              setValueVisible(true);
            }}
          >
            {formatMessage({
              id: 'ocp-express.Detail.Database.CreateADatabase',
              defaultMessage: '新建数据库',
            })}
          </Button>
        ),
      }}
    >
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <div data-aspm="c304244" data-aspm-desc="数据库列表" data-aspm-param={``} data-aspm-expo>
            <Card
              title={formatMessage({
                id: 'ocp-express.Detail.Database.DatabaseList',
                defaultMessage: '数据库列表',
              })}
              bordered={false}
              className="card-without-padding"
              extra={
                <MyInput.Search
                  data-aspm-click="c304244.d308730"
                  data-aspm-desc="数据库列表-搜索数据库"
                  data-aspm-param={``}
                  data-aspm-expo
                  allowClear={true}
                  onSearch={(value: string) => setKeyword(value)}
                  placeholder={formatMessage({
                    id: 'ocp-express.Detail.Database.SearchDatabaseName',
                    defaultMessage: '搜索数据库名',
                  })}
                  className="search-input"
                />
              }
            >
              <Table
                loading={loading}
                dataSource={dataSource.filter(
                  item => !keyword || (item.dbName && item.dbName.includes(keyword))
                )}
                columns={columns}
                rowKey={(record: API.Database) => record.id}
                pagination={PAGINATION_OPTION_10}
              />

              <AddDatabaseDrawer
                visible={valueVisible}
                tenantData={tenantData}
                clusterId={clusterId}
                tenantId={tenantId}
                database={currentDatabase}
                onCancel={() => {
                  setCurrentDatabase(null);
                  setValueVisible(false);
                }}
                onSuccess={() => {
                  setCurrentDatabase(null);
                  setValueVisible(false);
                  refresh();
                }}
              />

              <Modal
                width={540}
                title={formatMessage({
                  id: 'ocp-express.Detail.Database.DeleteADatabase',
                  defaultMessage: '删除数据库',
                })}
                destroyOnClose={true}
                visible={deleteDatabaseModalVisible}
                footer={
                  <>
                    <Button
                      data-aspm-click="c318540.d343253"
                      data-aspm-desc="删除数据库-取消"
                      data-aspm-param={``}
                      data-aspm-expo
                      onClick={() => {
                        setCurrentDatabase(null);
                        setDeleteDatabaseModalVisible(false);
                      }}
                    >
                      {formatMessage({
                        id: 'ocp-express.Detail.Database.Cancel',
                        defaultMessage: '取消',
                      })}
                    </Button>
                    <Button
                      data-aspm-click="c318540.d343261"
                      data-aspm-desc="删除数据库-提交"
                      data-aspm-param={``}
                      data-aspm-expo
                      type="primary"
                      danger={true}
                      ghost={true}
                      disabled={!IsAllowDel}
                      loading={deleteDatabaseLoading}
                      onClick={deleteDatabase}
                    >
                      {formatMessage({
                        id: 'ocp-express.Detail.Database.Delete',
                        defaultMessage: '删除',
                      })}
                    </Button>
                  </>
                }
                onCancel={() => {
                  setDeleteDatabaseModalVisible(false);
                }}
              >
                <Alert
                  message={formatMessage({
                    id: 'ocp-express.Detail.Database.DataCannotBeRecoveredAfter',
                    defaultMessage: '数据库删除后数据将不可恢复，请谨慎操作',
                  })}
                  type="warning"
                  showIcon
                  style={{
                    marginBottom: 12,
                  }}
                />

                <Descriptions column={1}>
                  <Descriptions.Item
                    label={formatMessage({
                      id: 'ocp-express.Detail.Session.Statistics.Database',
                      defaultMessage: '数据库',
                    })}
                  >
                    {currentDatabase && currentDatabase.dbName}
                  </Descriptions.Item>
                  <Descriptions.Item
                    label={formatMessage({
                      id: 'ocp-express.User.Component.DeleteUserModal.Tenant',
                      defaultMessage: '所属租户',
                    })}
                  >
                    {tenantData.name}
                  </Descriptions.Item>
                </Descriptions>
                <div>
                  {formatMessage({
                    id: 'ocp-express.Detail.Database.Enter',
                    defaultMessage: '请输入',
                  })}
                  <span style={{ color: 'red' }}> delete </span>
                  {formatMessage({
                    id: 'ocp-express.Detail.Database.ConfirmOperation',
                    defaultMessage: '确认操作',
                  })}
                </div>
                <Input style={{ width: 400, marginTop: 8 }} onChange={confirmOperation} />
              </Modal>

              <OBProxyAndConnectionStringModal
                width={900}
                visible={connectionStringModalVisible}
                obproxyAndConnectionStrings={currentDatabase?.connectionUrls || []}
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

function mapStateToProps({ tenant, loading, database }) {
  return {
    tenantData: tenant.tenantData,
    loading: loading.effects['database/listDatabases'],
    databaseList: database.databaseList || [],
  };
}

export default connect(mapStateToProps)(Database);
