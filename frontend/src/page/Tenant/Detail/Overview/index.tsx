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
import { history } from 'umi';
import {
  Button,
  Col,
  Dropdown,
  Form,
  Menu,
  Row,
  Badge,
  Space,
  Tag,
  Radio,
  Tooltip,
  Typography,
  Descriptions,
  Modal,
  message,
} from '@oceanbase/design';
import React, { useEffect, useState } from 'react';
import { uniqueId, find } from 'lodash';
import { directTo, findByValue, jsonParse } from '@oceanbase/util';
import moment from 'moment';
import { PageContainer } from '@ant-design/pro-components';
import { EllipsisOutlined } from '@ant-design/icons';
import * as ObTenantController from '@/service/ocp-express/ObTenantController';
import * as ObClusterController from '@/service/ocp-express/ObClusterController';
import * as ObTenantCompactionController from '@/service/ocp-express/ObTenantCompactionController';
import { COMPACTION_STATUS_LISTV4 } from '@/constant/compaction';
import { TENANT_MODE_LIST } from '@/constant/tenant';
import { REPLICA_TYPE_LIST } from '@/constant/oceanbase';
import { getCompactionStatusV4 } from '@/util/cluster';
import { getTextLengthRule } from '@/util';
import { taskSuccess } from '@/util/task';
import { formatTime } from '@/util/datetime';
import tracert from '@/util/tracert';
import { useRequest, useInterval } from 'ahooks';
import MyCard from '@/component/MyCard';
import MyInput from '@/component/MyInput';
import ContentWithQuestion from '@/component/ContentWithQuestion';
import ContentWithReload from '@/component/ContentWithReload';
import BatchOperationBar from '@/component/BatchOperationBar';
import FormEditZoneReplicaTable from '@/component/FormEditZoneReplicaTable';
import RenderConnectionString from '@/component/RenderConnectionString';
import BatchModifyUnitModal from '../Component/BatchModifyUnitModal';
import AddReplicaModal from '../Component/AddReplicaModal';
import DeleteTenantModal from '../Component/DeleteTenantModal';
import DeleteReplicaModal from '../Component/DeleteReplicaModal';
import ModifyPasswordModal from '../Component/ModifyPasswordModal';
import ModifyWhitelistModal from '../Component/ModifyWhitelistModal';
import ModifyPrimaryZoneDrawer from '../Component/ModifyPrimaryZoneDrawer';
import ModifyTenantPasswordModal from '../Component/ModifyTenantPasswordModal';
import OBProxyAndConnectionStringModal from '../Component/OBProxyAndConnectionStringModal';
const { Text } = Typography;

interface NewProps {
  match: {
    params: {
      tenantId: number;
    };
  };
}

const Detail: React.FC<NewProps> = ({
  match: {
    params: { tenantId },
  },
}) => {
  const [form] = Form.useForm();
  const { setFieldsValue, validateFields } = form;

  const [showAddReplicaModal, setShowAddReplicaModal] = useState(false);
  const [showDeleteReplicaModal, setShowDeleteReplicaModal] = useState(false);
  const [showBatchModifyUnitModal, setShowBatchModifyUnitModal] = useState(false);

  const [showDeleteTenantModal, setShowDeleteTenantModal] = useState(false);
  const [showWhitelistModal, setShowWhitelistModal] = useState(false);
  const [showModifyPrimaryZoneDrawer, setShowModifyPrimaryZoneDrawer] = useState(false);
  const [modifyPasswordVisible, setModifyPasswordVisible] = useState(false);
  const [connectionStringModalVisible, setConnectionStringModalVisible] = useState(false);
  const [ediLockStatusModal, setEdiLockStatusModal] = useState(false);
  const [ediRemarksModal, setEdiRemarksModal] = useState(false);

  const [showTenantPasswordModal, setShowTenantPasswordModal] = useState(false);

  const [currentTenantZone, setCurrentTenantZone] = useState(null);
  const [selectedRows, setSelectedRows] = useState([]);
  const [selectedRowKeys, setSelectedRowKeys] = useState([]);

  const { data: clusterInfo } = useRequest(ObClusterController.getClusterInfo, {
    manual: false,
    defaultParams: [{}],
  });

  const clusterData = clusterInfo?.data || {};
  const clusterZones = clusterData?.zones || [];

  const {
    data: tenantInfo,
    run: getTenantData,
    loading,
    refresh,
  } = useRequest(ObTenantController.getTenant, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        const currentTenantData = res?.data || {};
        setFieldsValue({
          zones: (currentTenantData?.zones || []).map(item => ({
            key: uniqueId(),
            name: item.name,
            replicaType: item.replicaType,
            resourcePool: item.resourcePool,
          })),
        });
      }
    },
  });

  const tenantData = tenantInfo?.data || {};

  const ocpExpressEmptySuperUserPasswordTime = jsonParse(
    localStorage.getItem(`__OCP_EXPRESS_TENANT__${tenantId}_EMPTY_SUPER_USER_PASSWORD_TIME__`),
    []
  ) as any[];

  const timeDifference = new Date().getTime() - (ocpExpressEmptySuperUserPasswordTime || 0);
  const { data: preCheckResult, run: tenantPreCheck } = useRequest(
    ObTenantController.tenantPreCheck,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          if (
            (!ocpExpressEmptySuperUserPasswordTime || timeDifference > 86400000) &&
            res?.data?.emptySuperUserPassword
          ) {
            setShowTenantPasswordModal(true);
          } else {
            localStorage.removeItem(
              `__OCP_EXPRESS_TENANT__${tenantId}_EMPTY_SUPER_USER_PASSWORD_TIME__`
            );
          }
        }
      },
    }
  );

  const emptySuperUserPassword = preCheckResult?.data?.emptySuperUserPassword || false;

  // 获取 unit 规格的限制规则
  const { data: clusterUnitSpecLimitData } = useRequest(
    ObClusterController.getClusterUnitSpecLimit,
    {
      manual: false,
      defaultParams: [{}],
    }
  );

  const clusterUnitSpecLimit = clusterUnitSpecLimitData?.data || {};

  useEffect(() => {
    if (tenantId) {
      getTenantData({ tenantId });
    }
  }, [tenantId]);

  useEffect(() => {
    // 每间隔大于24小时 || 首次检查
    if (tenantData?.name && (!ocpExpressEmptySuperUserPasswordTime || timeDifference > 86400000)) {
      tenantPreCheck({ tenantId });
    }
  }, [tenantData]);

  // 获取副本模式，即 3F、2F1L、2F1L1R 的简写形式
  const replicaMode = REPLICA_TYPE_LIST.map(item => ({
    count: (tenantData?.zones || []).filter(tenantZone => tenantZone.replicaType === item.value)
      .length,
    shortLabel: item.shortLabel,
  }))
    .filter(item => item.count > 0)
    .map(item => `${item.count}${item.shortLabel}`)
    .join('');

  const allZoneHasReplica = clusterZones.length === (tenantData?.zones || []).length;

  // 修改副本
  const { runAsync: modifyReplica, loading: modifyUnitLoading } = useRequest(
    ObTenantController.modifyReplica,
    {
      manual: true,
      defaultParams: [{}],
      onSuccess: res => {
        const taskId = res.data && res.data.id;
        taskSuccess({
          taskId,
          message: formatMessage({
            id: 'ocp-express.Detail.Overview.TheTaskOfModifyingTheReplicaWasSubmitted',
            defaultMessage: '修改副本的任务提交成功',
          }),
        });
        setFieldsValue({
          // 由于表格表单的最新值和展示态的数据格式不一样，因此修改成功后还需要重新设置副本的值
          zones: (tenantData.zones || []).map(item => ({
            key: uniqueId(),
            name: item.name,
            replicaType: item.replicaType,
            resourcePool: item.resourcePool,
          })),
        });
      },
    }
  );

  const { run: lockTenant, loading: lockTenantLOading } = useRequest(
    ObTenantController.lockTenant,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage({
              id: 'ocp-express.src.model.tenant.TenantLockedSuccessfully',
              defaultMessage: '租户锁定成功',
            })
          );
          setEdiLockStatusModal(false);
          refresh();
        }
      },
    }
  );

  const { run: unlockTenant, loading: unlockTenantLOading } = useRequest(
    ObTenantController.unlockTenant,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage({
              id: 'ocp-express.src.model.tenant.TenantUnlockedSuccessfully',
              defaultMessage: '租户解锁成功',
            })
          );
          setEdiLockStatusModal(false);
          refresh();
        }
      },
    }
  );

  const { run: modifyTenantDescription, loading: modifyTenantDescriptionLoading } = useRequest(
    ObTenantController.modifyTenantDescription,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage({
              id: 'ocp-express.src.model.tenant.RemarksModified',
              defaultMessage: '备注修改成功',
            })
          );
          setEdiRemarksModal(false);
          refresh();
        }
      },
    }
  );

  // 获取租户合并详情
  const { data, refresh: getTenantCompaction } = useRequest(
    ObTenantCompactionController.getTenantCompaction,
    {
      defaultParams: [
        {
          tenantId,
        },
      ],
    }
  );

  const tenantCompaction: API.TenantCompaction = data?.data || {};
  const compactionStatus = tenantCompaction ? getCompactionStatusV4([tenantCompaction]) : null;
  const statusItem = findByValue(COMPACTION_STATUS_LISTV4, compactionStatus);
  // 最近一次合并的起止时间
  const startTime = tenantCompaction?.startTime;
  // 最近 1 次合并时间如果晚于开始时间，则说明处于新的合并中，结束时间为空
  const endTime = moment(tenantCompaction?.lastFinishTime).isAfter(
    moment(tenantCompaction?.startTime)
  )
    ? tenantCompaction?.lastFinishTime
    : undefined;

  // 合并中或等待合并调度中，进行轮询
  const polling = compactionStatus === 'COMPACTING' || compactionStatus === 'WAIT_MERGE';
  useInterval(
    () => {
      getTenantCompaction();
    },
    polling ? 3000 : null
  );

  // 发起合并
  const { run: triggerTenantCompaction } = useRequest(
    ObTenantCompactionController.triggerTenantCompaction,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage({
              id: 'ocp-express.Detail.Overview.MergeInitiatedSuccessfully',
              defaultMessage: '合并发起成功',
            })
          );

          getTenantCompaction();
        }
      },
    }
  );

  // 清除租户合并异常
  const { run: clearCompactionError, loading: clearLoading } = useRequest(
    ObTenantCompactionController.clearCompactionError,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage({
              id: 'ocp-express.Detail.Overview.TheMergeExceptionIsClearedTheMergeWill',
              defaultMessage: '合并异常清除成功，将继续进行合并',
            })
          );

          getTenantCompaction();
        }
      },
    }
  );

  const handleMenuClick = (key: 'clone' | 'modifyPassword') => {
    if (key === 'clone') {
      directTo(`/tenant/new?tenantId=${tenantData.obTenantId}`);
    } else if (key === 'modifyPassword') {
      setModifyPasswordVisible(true);
    }
  };

  const menu = (
    <Menu onClick={({ key }) => handleMenuClick(key)}>
      <Menu.Item key="modifyPassword">
        <span
          data-aspm-click="c304245.d308738"
          data-aspm-desc="租户详情-修改密码"
          data-aspm-param={``}
          data-aspm-expo
        >
          {formatMessage({
            id: 'ocp-express.Tenant.Detail.ChangePassword',
            defaultMessage: '修改密码',
          })}
        </span>
      </Menu.Item>

      {/* 需要有所属集群的新建租户权限 */}
      <Menu.Item key="clone">
        <span
          data-aspm-click="c304245.d308741"
          data-aspm-desc="租户详情-复制租户"
          data-aspm-param={``}
          data-aspm-expo
        >
          {formatMessage({
            id: 'ocp-express.Tenant.Detail.ReplicationTenant',
            defaultMessage: '复制租户',
          })}
        </span>
      </Menu.Item>
    </Menu>
  );

  const handleModifyReplica = (value: API.TenantZone, onSuccess: () => void) => {
    const { name, replicaType, resourcePool } = value || {};
    const currentModifyTenantZone = find(tenantData.zones, item => item.name === name);

    if (
      !resourcePool ||
      (resourcePool.unitConfig?.maxCpuCoreCount ===
        currentModifyTenantZone?.resourcePool?.unitConfig?.maxCpuCoreCount &&
        resourcePool.unitConfig?.maxMemorySize ===
        currentModifyTenantZone?.resourcePool?.unitConfig?.maxMemorySize)
    ) {
      return message.info(
        formatMessage({
          id: 'ocp-express.Detail.Overview.ZoneInformationHasNotChanged',
          defaultMessage: 'Zone 的信息没有变更',
        })
      );
    }
    modifyReplica(
      {
        tenantId: tenantData.obTenantId,
      },
      [
        {
          zoneName: name,
          replicaType,
          resourcePool: resourcePool?.unitSpec || resourcePool?.unitCount ? resourcePool : null,
        },
      ]
    ).then(res => {
      if (res.successful) {
        if (onSuccess) {
          refresh();
          onSuccess();
        }
      }
    });
  };

  const handleDeleteReplica = (record: API.TenantZone) => {
    setShowDeleteReplicaModal(true);
    setCurrentTenantZone(record);
  };

  const rowSelection = {
    selectedRowKeys,
    onChange: (newSelectedRowKeys: string[], newSelectedRows: API.TenantZone[]) => {
      setSelectedRowKeys(newSelectedRowKeys);
      setSelectedRows(newSelectedRows);
    },
  };

  return (
    <PageContainer
      ghost={true}
      header={{
        title: (
          <ContentWithReload
            content={formatMessage({
              id: 'ocp-express.Detail.Overview.Overview',
              defaultMessage: '总览',
            })}
            spin={loading}
            onClick={refresh}
          />
        ),

        extra: (
          <Space>
            <Tooltip
              placement="topRight"
              title={
                tenantData.name === 'sys' &&
                formatMessage({
                  id: 'ocp-express.Detail.Overview.TheSysTenantCannotBe',
                  defaultMessage: 'sys 租户无法删除',
                })
              }
            >
              <Button
                data-aspm-click="c304245.d308734"
                data-aspm-desc="租户详情-删除租户"
                data-aspm-param={``}
                data-aspm-expo
                disabled={tenantData.name === 'sys'}
                onClick={() => {
                  setShowDeleteTenantModal(true);
                }}
              >
                {formatMessage({
                  id: 'ocp-express.Tenant.Detail.DeleteATenant',
                  defaultMessage: '删除租户',
                })}
              </Button>
            </Tooltip>

            <Tooltip
              placement="topRight"
              title={
                allZoneHasReplica &&
                formatMessage({
                  id: 'ocp-express.Tenant.Detail.TheCurrentTenantHasSet',
                  defaultMessage: '当前租户在所属集群的全部 Zone 上均已设置副本',
                })
              }
            >
              <Button
                data-aspm-click="c304245.d308737"
                data-aspm-desc="租户详情-新增副本"
                data-aspm-param={``}
                data-aspm-expo
                disabled={allZoneHasReplica}
                type="primary"
                onClick={() => {
                  setShowAddReplicaModal(true);
                }}
              >
                {formatMessage({
                  id: 'ocp-express.Tenant.Detail.NewCopy',
                  defaultMessage: '新增副本',
                })}
              </Button>
            </Tooltip>
            <Dropdown overlay={menu}>
              <Button>
                <EllipsisOutlined />
              </Button>
            </Dropdown>
          </Space>
        ),
      }}
    >
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <MyCard
            title={
              <span style={{ marginLeft: 2 }}>
                {formatMessage({
                  id: 'ocp-express.Detail.Overview.BasicInformation',
                  defaultMessage: '基本信息',
                })}
              </span>
            }
          >
            <div
              data-aspm="c304177"
              data-aspm-desc="租户信息"
              data-aspm-expo
              // 扩展参数
              data-aspm-param={tracert.stringify({
                // 租户模式
                tenantMode: tenantData.mode,
                // 租户 OB 版本
                tenantObVersion: tenantData.obVersion,
                // 租户字符集
                tenantCharset: tenantData.charset,
                // 租户 collation
                tenantCollation: tenantData.collation,
                // 租户 Zone 优先级
                tenantPrimaryZone: tenantData.primaryZone,
                // 租户副本分布
                tenantLocality: tenantData.locality,
                // 租户副本模式，即 3F、2F1L、2F1L1R 的简写形式
                tenantReplicaMode: replicaMode,
              })}
            >
              <Descriptions column={4}>
                <Descriptions.Item
                  label={formatMessage({
                    id: 'ocp-express.Tenant.Detail.TenantMode',
                    defaultMessage: '租户模式',
                  })}
                >
                  {findByValue(TENANT_MODE_LIST, tenantData.mode).label}
                </Descriptions.Item>
                <Descriptions.Item
                  label={formatMessage({
                    id: 'ocp-express.Detail.Overview.ObVersion',
                    defaultMessage: 'OB 版本号',
                  })}
                >
                  {tenantData.obVersion}
                </Descriptions.Item>
                <Descriptions.Item
                  span={2}
                  label={formatMessage({
                    id: 'ocp-express.component.TenantList.ConnectionString',
                    defaultMessage: '连接字符串',
                  })}
                >
                  <RenderConnectionString
                    connectionStrings={tenantData.obproxyAndConnectionStrings || []}
                    maxWidth={500}
                    callBack={() => {
                      setConnectionStringModalVisible(true);
                    }}
                  />
                </Descriptions.Item>
                <Descriptions.Item
                  label={formatMessage({
                    id: 'ocp-express.Tenant.Detail.CharacterSet',
                    defaultMessage: '字符集',
                  })}
                >
                  {tenantData.charset}
                </Descriptions.Item>

                <Descriptions.Item
                  label={formatMessage({
                    id: 'ocp-express.Detail.Overview.LockedState',
                    defaultMessage: '锁定状态',
                  })}
                >
                  <Text
                    ellipsis={true}
                    editable={{
                      editing: false,
                      onStart: () => {
                        setEdiLockStatusModal(true);
                      },
                    }}
                  >
                    {tenantData?.locked
                      ? formatMessage({
                        id: 'ocp-express.Detail.Overview.Locked',
                        defaultMessage: '已锁定',
                      })
                      : formatMessage({
                        id: 'ocp-express.Detail.Overview.Unlocked',
                        defaultMessage: '未锁定',
                      })}
                  </Text>
                </Descriptions.Item>
                <Descriptions.Item
                  label={formatMessage({
                    id: 'ocp-express.Tenant.Detail.CreationTime',
                    defaultMessage: '创建时间',
                  })}
                >
                  {formatTime(tenantData.createTime)}
                </Descriptions.Item>
                <Descriptions.Item
                  className="descriptions-item-with-ellipsis"
                  label={formatMessage({
                    id: 'ocp-express.Detail.Overview.Note',
                    defaultMessage: '备注',
                  })}
                >
                  <Tooltip placement="bottomLeft" title={tenantData?.description}>
                    <Text
                      style={{ width: '95%', minWidth: 180 }}
                      ellipsis={true}
                      editable={{
                        editing: false,
                        onStart: () => {
                          setEdiRemarksModal(true);
                        },
                      }}
                    >
                      {tenantData?.description || '-'}
                    </Text>
                  </Tooltip>
                </Descriptions.Item>
              </Descriptions>
            </div>
          </MyCard>
        </Col>
        <Col span={24}>
          <MyCard
            title={formatMessage({
              id: 'ocp-express.Tenant.Detail.CopyDetails',
              defaultMessage: '副本详情',
            })}
            extra={
              <Button
                data-aspm-click="c304245.d308733"
                data-aspm-desc="租户详情-修改 Unit"
                data-aspm-param={``}
                data-aspm-expo
                onClick={() => {
                  setShowBatchModifyUnitModal(true);
                }}
              >
                {formatMessage({
                  id: 'ocp-express.Detail.Overview.ModifyUnit',
                  defaultMessage: '修改 Unit',
                })}
              </Button>
            }
          >
            <Form form={form} preserve={false} layout="vertical" hideRequiredMark={true}>
              <Form.Item name="zones" initialValue={[]}>
                <FormEditZoneReplicaTable
                  onSave={handleModifyReplica}
                  onDelete={handleDeleteReplica}
                  saveLoading={modifyUnitLoading}
                  showDeletePopconfirm={false}
                  tenantData={tenantData}
                  clusterData={clusterData}
                  tenantId={tenantId}
                  rowSelection={rowSelection}
                  rowKey={record => record.name}
                  unitSpecLimit={clusterUnitSpecLimit}
                />
              </Form.Item>
            </Form>
            {selectedRowKeys && selectedRowKeys.length > 0 && (
              <BatchOperationBar
                size="small"
                selectedCount={selectedRowKeys && selectedRowKeys.length}
                onCancel={() => {
                  setSelectedRowKeys([]);
                }}
                actions={[
                  <Button
                    key="batch-delete"
                    data-aspm-click="c304245.d308736"
                    data-aspm-desc="租户详情-批量删除副本"
                    data-aspm-param={``}
                    data-aspm-expo
                    danger={true}
                    ghost={true}
                    onClick={() => {
                      setShowDeleteReplicaModal(true);
                    }}
                  >
                    {formatMessage({
                      id: 'ocp-express.Tenant.Detail.BatchDelete',
                      defaultMessage: '批量删除',
                    })}
                  </Button>,
                ]}
                style={{ marginBottom: 16 }}
              />
            )}
          </MyCard>
        </Col>
        <Col span={24}>
          <MyCard
            bordered={false}
            title={
              <ContentWithQuestion
                content={formatMessage({
                  id: 'ocp-express.Tenant.Detail.ZonePriority',
                  defaultMessage: 'Zone 优先级',
                })}
                tooltip={{
                  title: formatMessage({
                    id: 'ocp-express.Tenant.Detail.PriorityOfTheDistributionOf',
                    defaultMessage: '租户中主副本分布的优先级',
                  }),
                }}
              />
            }
            extra={
              <Button
                data-aspm-click="c304245.d308739"
                data-aspm-desc="租户详情-修改 Zone 优先级"
                data-aspm-param={``}
                data-aspm-expo
                onClick={() => {
                  setShowModifyPrimaryZoneDrawer(true);
                }}
              >
                {formatMessage({
                  id: 'ocp-express.Tenant.Detail.Modify',
                  defaultMessage: '修改',
                })}
              </Button>
            }
          >
            {tenantData.primaryZone &&
              tenantData.primaryZone.split(';').map(item => <Tag>{item}</Tag>)}
          </MyCard>
        </Col>
        <Col span={24}>
          <MyCard
            bordered={false}
            title={
              <ContentWithQuestion
                content={formatMessage({
                  id: 'ocp-express.Tenant.Detail.Whitelist',
                  defaultMessage: '白名单',
                })}
                tooltip={{
                  title: formatMessage({
                    id: 'ocp-express.Tenant.Detail.ListOfAddressesThatCan',
                    defaultMessage: '能够连接到租户的地址列表',
                  }),
                }}
              />
            }
            extra={
              <Button
                data-aspm-click="c304245.d308740"
                data-aspm-desc="租户详情-修改白名单"
                data-aspm-param={``}
                data-aspm-expo
                onClick={() => {
                  setShowWhitelistModal(true);
                }}
              >
                {formatMessage({
                  id: 'ocp-express.Tenant.Detail.Modify',
                  defaultMessage: '修改',
                })}
              </Button>
            }
          >
            <div className="multiple-tag-wrapper">
              {tenantData.whitelist &&
                tenantData.whitelist.split(',').map(item => <Tag key={item}>{item}</Tag>)}
            </div>
          </MyCard>
        </Col>
        <Col span={24}>
          <MyCard
            bordered={false}
            title={formatMessage({
              id: 'ocp-express.Detail.Overview.MergeManagement',
              defaultMessage: '合并管理',
            })}
          >
            <Descriptions>
              <Descriptions.Item
                label={formatMessage({
                  id: 'ocp-express.Compaction.Detail.State',
                  defaultMessage: '状态',
                })}
              >
                {/**
                 * 状态对应的操作
                 * 租户合并中：  查看租户
                 * ERROR 租户合并异常： 查询租户 清除异常
                 * IDLE 租户空闲：发起合并
                 * */}
                <Badge
                  status={statusItem.badgeStatus}
                  text={statusItem.label}
                  style={{ marginRight: 8 }}
                />

                {statusItem.value === 'ERROR' && (
                  <Tooltip
                    color="#fff"
                    overlayInnerStyle={{ color: 'rgba(0, 0, 0, 0.85)' }}
                    title={formatMessage({
                      id: 'ocp-express.Detail.Overview.TheFollowingTenantMergeHasAnExceptionAfter',
                      defaultMessage:
                        '以下租户合并存在异常，异常解决后，再点击「清除异常」，操作后系统会继续合并',
                    })}
                  >
                    <Button
                      data-aspm-click="c304245.d308735"
                      data-aspm-desc="租户详情-清除合并异常"
                      data-aspm-param={``}
                      data-aspm-expo
                      type="link"
                      loading={clearLoading}
                      onClick={() => {
                        clearCompactionError({
                          tenantId,
                        });
                      }}
                    >
                      {formatMessage({
                        id: 'ocp-express.Compaction.Detail.ClearException',
                        defaultMessage: '清除异常',
                      })}
                    </Button>
                  </Tooltip>
                )}

                {/* 合并状态为空(说明从未发起过合并)，或者空闲状态，才能发起合并 */}
                {statusItem.value === 'IDLE' && (
                  <a
                    data-aspm-click="c304245.d308731"
                    data-aspm-desc="租户详情-发起合并"
                    data-aspm-param={``}
                    data-aspm-expo
                    onClick={() => {
                      Modal.confirm({
                        title: formatMessage({
                          id: 'ocp-express.Compaction.Detail.AreYouSureYouWant',
                          defaultMessage: '确定要发起合并吗？',
                        }),

                        onOk: () => {
                          return triggerTenantCompaction({
                            tenantId,
                          });
                        },
                      });
                    }}
                  >
                    {formatMessage({
                      id: 'ocp-express.Compaction.Detail.InitiateAMerge',
                      defaultMessage: '发起合并',
                    })}
                  </a>
                )}
              </Descriptions.Item>
              <Descriptions.Item
                label={formatMessage({
                  id: 'ocp-express.Compaction.Detail.StartTimeOfTheLastMerge',
                  defaultMessage: '最近 1 次合并开始时间',
                })}
              >
                {formatTime(startTime)}
              </Descriptions.Item>
              <Descriptions.Item
                label={formatMessage({
                  id: 'ocp-express.Compaction.Detail.LastMergeEndTime',
                  defaultMessage: '最近 1 次合并结束时间',
                })}
              >
                {formatTime(endTime)}
              </Descriptions.Item>
            </Descriptions>
          </MyCard>
        </Col>
      </Row>
      <AddReplicaModal
        visible={showAddReplicaModal}
        tenantId={tenantId}
        tenantData={tenantData}
        clusterZones={clusterZones}
        unitSpecLimit={clusterUnitSpecLimit}
        onCancel={() => {
          setShowAddReplicaModal(false);
        }}
        onSuccess={() => {
          setShowAddReplicaModal(false);
          refresh();
        }}
      />

      <DeleteReplicaModal
        visible={showDeleteReplicaModal}
        tenantData={tenantData}
        tenantZones={currentTenantZone ? [currentTenantZone] : selectedRows}
        onCancel={() => {
          setShowDeleteReplicaModal(false);
          setCurrentTenantZone(null);
        }}
        onSuccess={() => {
          setShowDeleteReplicaModal(false);
          setCurrentTenantZone(null);
          refresh();
        }}
      />

      <BatchModifyUnitModal
        visible={showBatchModifyUnitModal}
        tenantData={tenantData}
        clusterZones={clusterZones}
        tenantZones={tenantData?.zones}
        unitSpecLimit={clusterUnitSpecLimit}
        onCancel={() => {
          setShowBatchModifyUnitModal(false);
        }}
        onSuccess={() => {
          setShowBatchModifyUnitModal(false);
          setSelectedRows([]);
          setSelectedRowKeys([]);
          refresh();
        }}
      />

      <DeleteTenantModal
        tenantData={tenantData}
        visible={showDeleteTenantModal}
        onCancel={() => {
          setShowDeleteTenantModal(false);
        }}
        onSuccess={() => {
          setShowDeleteTenantModal(false);
          history.push('/tenant');
        }}
      />

      <ModifyWhitelistModal
        visible={showWhitelistModal}
        tenantData={tenantData}
        onCancel={() => {
          setShowWhitelistModal(false);
        }}
        onSuccess={() => {
          setShowWhitelistModal(false);
          refresh();
        }}
      />

      <ModifyPrimaryZoneDrawer
        visible={showModifyPrimaryZoneDrawer}
        tenantData={tenantData}
        onClose={() => {
          setShowModifyPrimaryZoneDrawer(false);
        }}
        onSuccess={() => {
          setShowModifyPrimaryZoneDrawer(false);
          refresh();
        }}
      />

      <ModifyPasswordModal
        visible={modifyPasswordVisible}
        tenantData={tenantData}
        onCancel={() => {
          setModifyPasswordVisible(false);
        }}
        onSuccess={() => {
          setModifyPasswordVisible(false);
          refresh();
        }}
      />

      <OBProxyAndConnectionStringModal
        width={900}
        visible={connectionStringModalVisible}
        obproxyAndConnectionStrings={tenantData?.obproxyAndConnectionStrings || []}
        onCancel={() => {
          setConnectionStringModalVisible(false);
        }}
        onSuccess={() => {
          setConnectionStringModalVisible(false);
          refresh();
        }}
      />

      <Modal
        title={formatMessage(
          {
            id: 'ocp-express.Detail.Overview.ModifyTheLockedStateOfTenantTenantdataname',
            defaultMessage: '修改租户 {tenantDataName} 锁定状态',
          },
          { tenantDataName: tenantData.name }
        )}
        visible={ediLockStatusModal}
        destroyOnClose={true}
        confirmLoading={lockTenantLOading || unlockTenantLOading}
        onOk={() => {
          validateFields().then(values => {
            const { locked } = values;
            if (locked) {
              lockTenant({
                tenantId: tenantData?.obTenantId,
              });
            } else {
              unlockTenant({
                tenantId: tenantData?.obTenantId,
              });
            }
          });
        }}
        onCancel={() => {
          setEdiLockStatusModal(false);
        }}
      >
        <Form form={form} layout="vertical" hideRequiredMark={true}>
          <Form.Item
            label={formatMessage({
              id: 'ocp-express.Detail.Overview.LockedState',
              defaultMessage: '锁定状态',
            })}
            name="locked"
            initialValue={tenantData?.locked}
          >
            <Radio.Group>
              <Radio value={true}>
                {formatMessage({ id: 'ocp-express.Detail.Overview.Lock', defaultMessage: '锁定' })}
              </Radio>
              <Radio value={false}>
                {formatMessage({
                  id: 'ocp-express.Detail.Overview.Unlock',
                  defaultMessage: '解锁',
                })}
              </Radio>
            </Radio.Group>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={formatMessage({
          id: 'ocp-express.Detail.Overview.ModifyRemarks',
          defaultMessage: '修改备注',
        })}
        visible={ediRemarksModal}
        destroyOnClose={true}
        confirmLoading={modifyTenantDescriptionLoading}
        onOk={() => {
          validateFields().then(values => {
            modifyTenantDescription(
              {
                tenantId,
              },

              { description: values?.description }
            );
          });
        }}
        onCancel={() => {
          setEdiRemarksModal(false);
        }}
      >
        <Form form={form} layout="vertical" hideRequiredMark={true}>
          <Form.Item
            label={formatMessage({
              id: 'ocp-express.Detail.Overview.Note',
              defaultMessage: '备注',
            })}
            name="description"
            initialValue={tenantData?.description}
            rules={[getTextLengthRule(0, 1024)]}
          >
            <MyInput.TextArea
              placeholder={formatMessage({
                id: 'ocp-express.Detail.Overview.Enter',
                defaultMessage: '请输入',
              })}
            />
          </Form.Item>
        </Form>
      </Modal>
      <ModifyTenantPasswordModal
        visible={showTenantPasswordModal}
        onCancel={() => {
          if (!ocpExpressEmptySuperUserPasswordTime && emptySuperUserPassword) {
            localStorage.setItem(
              `__OCP_EXPRESS_TENANT__${tenantId}_EMPTY_SUPER_USER_PASSWORD_TIME__`,
              JSON.stringify(new Date().getTime())
            );
          }
          setShowTenantPasswordModal(false);
        }}
        onSuccess={() => {
          localStorage.removeItem(
            `__OCP_EXPRESS_TENANT__${tenantId}_EMPTY_SUPER_USER_PASSWORD_TIME__`
          );
          setShowTenantPasswordModal(false);
        }}
      />
    </PageContainer>
  );
};
export default Detail;
