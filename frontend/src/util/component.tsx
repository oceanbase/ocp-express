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

import TableFilterDropdown from '@/component/TableFilterDropdown';
import { DATE_TIME_FORMAT_DISPLAY } from '@/constant/datetime';
import { formatTimeWithMicroseconds } from '@/util/datetime';
import { formatMessage } from '@/util/intl';
import { history } from 'umi';
import type { ModalFuncProps } from '@oceanbase/design';
import {
  Button,
  Col,
  Divider,
  Dropdown,
  Input,
  Menu,
  Popconfirm,
  Row,
  Space,
  Tooltip,
  Tree,
  Modal,
  theme,
} from '@oceanbase/design';
import React from 'react';
import { noop } from 'lodash';
import moment from 'moment';
import { EllipsisOutlined, SearchOutlined } from '@oceanbase/icons';
import type { ButtonProps } from 'antd/es/button';
import type { PopconfirmProps } from 'antd/es/popconfirm';
import type { FilterDropdownProps } from 'antd/es/table/interface';
import type { TooltipProps } from 'antd/es/tooltip';

export interface getConfirmModalProps extends ModalFuncProps {
  operationType: 'confirm' | 'delete';
  subTitle?: React.ReactNode;
}

interface TreeNode {
  key?: string;
  title?: string;
  children?: TreeNode[];
}

export const getConfirmModal = ({
  // 操作类型，当 Input 输入内容为 {operationType} 时，才允许用户确定提交
  operationType = 'confirm',
  subTitle,
  ...restProps
}: getConfirmModalProps): void => {
  const modal = Modal.confirm({
    content: (
      <>
        <Row gutter={[8, 8]}>
          <Col span={24}>{subTitle}</Col>
          <Col span={24}>
            {formatMessage({
              id: 'ocp-express.src.util.component.Enter',
              defaultMessage: '请输入',
            })}
            {/* operationType 前后需要加空格，已突出展示，并在英文环境下自动使用空格分隔 */}
            <span style={{ color: 'rgb(255, 103, 105)' }}>{` ${operationType} `}</span>
            {formatMessage({
              id: 'ocp-express.src.util.component.ConfirmOperation',
              defaultMessage: '确认操作',
            })}
          </Col>
          <Col span={24}>
            <Input
              placeholder={operationType}
              onChange={e => {
                if (e.target.value === operationType) {
                  modal.update({
                    okButtonProps: {
                      disabled: false,
                      danger: operationType === 'delete',
                      ghost: operationType === 'delete',
                    },
                  });
                } else {
                  modal.update({
                    okButtonProps: {
                      disabled: true,
                    },
                  });
                }
              }}
            />
          </Col>
        </Row>
      </>
    ),

    ...restProps,
    okButtonProps: {
      disabled: true,
    },
  });
};

export const getColumnSearchProps = ({
  frontEndSearch,
  dataIndex,
  onConfirm,
}: {
  frontEndSearch: boolean;
  // 前端分页时，dataIndex 必传，该参数对后端分页无效
  dataIndex?: string;
  onConfirm?: (value?: React.Key) => void;
}) => ({
  filterDropdown: (props: FilterDropdownProps) => (
    <TableFilterDropdown {...props} onConfirm={onConfirm} />
  ),

  filterIcon: (filtered: boolean) => (
    <SearchOutlined style={{ color: filtered ? theme.token.colorPrimary : undefined }} />
  ),

  // 前端搜索，需要定义 onFilter 函数
  ...(frontEndSearch
    ? {
      onFilter: (value, record) =>
        record[dataIndex] &&
        record[dataIndex]
          .toString()
          .toLowerCase()
          .includes(value && value.toLowerCase()),
    }
    : {}),
});

export function breadcrumbItemRender(route, params, routes) {
  const last = routes.indexOf(route) === routes.length - 1;

  return last ? (
    <span>{route.breadcrumbName}</span>
  ) : (
    <a
      onClick={() => {
        history.push(
          route.query
            ? {
              pathname: route.path,
              query: route.query,
            }
            : route.path
        );
      }}
    >
      {route.breadcrumbName}
    </a>
  );
}

// 根据集群参数的值获取对应展示的简单组件
export function getSimpleComponentByClusterParameterValue(
  value: API.ClusterParameterValue,
  parameterType: string,
  onClick = noop,
  // mode 仅在自定义范围生效
  mode: 'link' | 'text' = 'link'
) {
  // 值列表对应的字符串
  const valuesString = value.values && value.values.join(';');
  return value && value.singleValueInCluster ? (
    <span>
      {parameterType === 'OB_TENANT_PARAMETER'
        ? formatMessage(
          {
            id: 'ocp-express.src.util.component.ValuesstringAllTenants',
            defaultMessage: '{valuesString}（全部租户）',
          },
          { valuesString: valuesString }
        )
        : formatMessage(
          {
            id: 'ocp-express.src.util.component.ValuesstringCluster',
            defaultMessage: '{valuesString}（集群）',
          },

          { valuesString }
        )}
    </span>
  ) : mode === 'link' ? (
    <a onClick={onClick}>
      {formatMessage(
        {
          id: 'ocp-express.src.util.component.ValuesstringCustom',
          defaultMessage: '{valuesString}（自定义）',
        },

        { valuesString }
      )}
    </a>
  ) : (
    <span>
      {formatMessage(
        {
          id: 'ocp-express.src.util.component.ValuesstringCustom',
          defaultMessage: '{valuesString} (自定义)',
        },

        { valuesString }
      )}
    </span>
  );
}
// 集群参数根据parameterType & 参数值 展示的详细组件
export function getDetailComponentByParameterValue(
  value: API.ClusterParameter | API.ClusterParameterChange | null,
  clusterData: API.Cluster,
  historyType?: string
) {
  const { parameterType } = value || {};
  let serverValues, tenantValues;

  if (historyType === 'newValue') {
    serverValues = value?.newValue?.serverValues;
    tenantValues = value?.newValue?.tenantValues;
  } else if (historyType === 'previousValue') {
    serverValues = value?.previousValue?.serverValues;
    tenantValues = value?.previousValue?.tenantValues;
  } else {
    serverValues = value?.currentValue?.serverValues;
    tenantValues = value?.currentValue?.tenantValues;
  }

  if (parameterType === 'OB_TENANT_PARAMETER') {
    return (
      <>
        {tenantValues
          ?.sort((a, b) => {
            const nameA = a?.tenantName?.toUpperCase();
            const nameB = b?.tenantName?.toUpperCase();
            if (nameA < nameB) {
              return -1;
            }
            if (nameA > nameB) {
              return 1;
            }
            return 0;
          })
          ?.map(tenantValue => (
            <div>{`${tenantValue?.tenantName}: ${tenantValue?.value}`}</div>
          ))}
      </>
    );
  }

  // 修改参数值，修改前状态
  /**
   * 添加 OBServer 等操作后，参数生效范围不包含新添加的server  集群信息里已经有了新添加的 server;
   * 此处采用以参数生效范围为准
   * */
  if (parameterType === 'OB_CLUSTER_PARAMETER') {
    let treeData: TreeNode[] = [];
    treeData = [
      {
        title: 'cluster',
        key: '1',
        children: clusterData?.zones?.map((zone, index) => ({
          title: zone.name,
          key: `1-${index}`,
          children: zone?.servers
            ?.map((server, key) => {
              const serverValue = serverValues?.find(item => item?.svrIp === server?.ip);
              if (serverValue) {
                return {
                  title: serverValue?.value
                    ? `${serverValue?.svrIp}:${server?.port}：${serverValue?.value}`
                    : `${serverValue?.svrIp}`,
                  key: `1-${index}-${key}`,
                };
              }
              return undefined;
            })
            // 需要排除掉空值，否则 Tree 组件死循环导致页面崩溃
            ?.filter(item => !!item),
        })),
      },
    ];

    return (
      <Tree
        defaultExpandAll={true}
        treeData={treeData}
        titleRender={node => {
          return <div>{node.title}</div>;
        }}
      />
    );
  }
}

export type Operation = {
  value: string;
  label: string;
  // 按钮样式
  buttonProps?: ButtonProps;
  tooltip?: Omit<TooltipProps, 'overlay'>;
  popconfirm?: PopconfirmProps;
  // 是否增加一个 divider
  divider?: boolean;
};

export function getOperationComponent<T>({
  operations = [],
  handleOperation,
  record,
  mode = 'link',
  // link 模式，默认露出一个操作项
  // button 模式，默认露出三个操作项
  displayCount = mode === 'link' ? 1 : 3,
}: {
  operations?: Operation[];
  handleOperation: (key: string, record: T, operations: Operation[]) => void;
  record: T;
  // link: 链接模式，常用于表格的操作列
  // buttom: 按钮模式，常用于页头的 extra 操作区域
  mode?: 'link' | 'button';
  displayCount?: number;
}) {
  const operations1 =
    mode === 'link'
      ? operations.slice(0, displayCount)
      : // button 模式的操作优先级与 link 模式正好相反，且只针对 operations1 生效
      // 由于 reverse 会改变原数组，因此这里采用解构浅拷贝一份数据进行操作
      [...operations.slice(0, displayCount)].reverse();
  const operations2 = operations.slice(displayCount, operations.length);

  return (
    // link 和 button 模式的间距做差异化处理
    <Space size={mode === 'link' ? 16 : 8}>
      {operations1.map(({ value, label, buttonProps, tooltip, popconfirm }, index) => {
        return (
          <Tooltip key={value} title={tooltip?.title} {...tooltip}>
            {mode === 'link' ? (
              popconfirm ? (
                <Popconfirm placement="bottomLeft" {...popconfirm}>
                  <a>{label}</a>
                </Popconfirm>
              ) : (
                <a onClick={() => handleOperation(value, record, operations)}>{label}</a>
              )
            ) : (
              <Button
                // 最后一个按钮设置为 primary
                type={index === operations1.length - 1 ? 'primary' : 'default'}
                onClick={() => handleOperation(value, record, operations)}
                {...(buttonProps || {})}
              >
                {label}
              </Button>
            )}
          </Tooltip>
        );
      })}

      {/* 操作项不为空，且具有其中一个操作项的权限 */}
      {operations2.length > 0 && (
        <Dropdown
          overlay={
            <Menu
              onClick={({ key }) => {
                handleOperation(key, record, operations);
              }}
            >
              {operations2.map(({ value, label, divider, tooltip }) => {
                return (
                  <>
                    <Menu.Item key={value}>
                      <Tooltip title={tooltip?.title} {...tooltip}>
                        <div>{label}</div>
                      </Tooltip>
                    </Menu.Item>
                    {divider && <Menu.Divider />}
                  </>
                );
              })}
            </Menu>
          }
        >
          {mode === 'link' ? (
            <a>
              <EllipsisOutlined />
            </a>
          ) : (
            <Button>
              <EllipsisOutlined />
            </Button>
          )}
        </Dropdown>
      )}
    </Space>
  );
}

export function getBatchOperationComponent<T>({
  mode = 'link',
  operations,
  handleOperation,
  selectedRows,
  displayCount = 1,
}: {
  mode?: 'link' | 'button';
  operations: Operation[];
  handleOperation: (key: string, selectedRows: T) => void;
  selectedRows: T;
  displayCount?: number;
}) {
  const operations1 = operations.slice(0, displayCount);
  const operations2 = operations.slice(displayCount, operations.length);

  return (
    <Space>
      {operations1.map(item => (
        <Button
          onClick={() => handleOperation(item.value, selectedRows)}
          {...(item?.buttonProps || {})}
        >
          {item.label}
        </Button>
      ))}

      {/* 操作项不为空，且具有其中一个操作项的权限 */}
      {operations2.length > 0 && (
        <Dropdown
          overlay={
            <Menu onClick={({ key }) => handleOperation(key, selectedRows)}>
              {operations2.map(({ value, label, divider }) => {
                return (
                  <>
                    <Menu.Item key={value}>
                      <span>{label}</span>
                    </Menu.Item>
                    {divider && <Menu.Divider />}
                  </>
                );
              })}
            </Menu>
          }
        >
          {mode === 'link' ? (
            <a>
              <EllipsisOutlined />
            </a>
          ) : (
            <Button>
              <EllipsisOutlined />
            </Button>
          )}
        </Dropdown>
      )}
    </Space>
  );
}

// 获取可恢复时间区间
export function getRecoverableRangeTime(
  recoverableList: API.ObRecoverableSectionItem[],
  format = DATE_TIME_FORMAT_DISPLAY
) {
  return (
    <div>
      {recoverableList.length > 0 ? (
        recoverableList
          .sort(
            (a, b) =>
              // 根据起始时间进行逆序展示，最新的时间区间展示在前面
              moment(b.realRecoverableTimeInterval?.startTime).valueOf() -
              moment(a.realRecoverableTimeInterval?.startTime).valueOf()
          )
          .map(item => {
            return (
              <Space key={item.realRecoverableTimeInterval?.startTime}>
                <span>
                  {/* 前端展示备份和恢复时间需要精确到微秒 (接口也支持) */}
                  {`${formatTimeWithMicroseconds(
                    item.realRecoverableTimeInterval?.startTime,
                    format
                  )} ~${formatTimeWithMicroseconds(
                    item.realRecoverableTimeInterval?.endTime,
                    format
                  )}`}
                </span>
              </Space>
            );
          })
      ) : (
        <div>-</div>
      )}
    </div>
  );
}

// 获取最近的一个可恢复时间区间
export function getLatestRecoverableRangeTime(
  recoverableList: API.ObRecoverableSectionItem[],
  format = DATE_TIME_FORMAT_DISPLAY
) {
  return (
    <span>
      {recoverableList
        // 展示最后一项
        .slice(recoverableList.length - 1, recoverableList.length)
        .map(item => {
          return (
            <Space key={item.realRecoverableTimeInterval?.startTime}>
              <span>
                {`${formatTimeWithMicroseconds(
                  item.realRecoverableTimeInterval?.startTime,
                  format
                )} ~ ${formatTimeWithMicroseconds(
                  item.realRecoverableTimeInterval?.endTime,
                  format
                )}`}
              </span>
            </Space>
          );
        })}
    </span>
  );
}

// 获取备份时间区间: 日志备份时间区间 + 数据备份时间点
export function getBackupRangeTime(
  logList: API.ObRecoverableSectionItem[],
  dataList: API.ObRecoverableSectionItem[],
  format = DATE_TIME_FORMAT_DISPLAY
) {
  return (
    <div>
      <div>
        {formatMessage({
          id: 'ocp-express.src.util.component.LogBackupTimeInterval',
          defaultMessage: '日志备份时间区间：',
        })}

        {logList.length > 0 ? (
          logList.map(item => (
            <div key={item.logRecoverableTimeInterval?.startTime}>
              {`${formatTimeWithMicroseconds(
                item.logRecoverableTimeInterval?.startTime,
                format
              )} ~${formatTimeWithMicroseconds(item.logRecoverableTimeInterval?.endTime, format)}`}
            </div>
          ))
        ) : (
          <div>-</div>
        )}
      </div>
      <Divider style={{ margin: '12px 0px' }} />
      <div>
        {formatMessage({
          id: 'ocp-express.src.util.component.DataBackupTime',
          defaultMessage: '数据备份时间点：',
        })}

        {dataList.length > 0 ? (
          dataList.map(item => (
            <div key={item.dataBackupRecoverableInfo?.recoverableTime}>
              {`${formatTimeWithMicroseconds(
                item.dataBackupRecoverableInfo?.recoverableTime,
                format
              )}`}
            </div>
          ))
        ) : (
          <div>-</div>
        )}
      </div>
    </div>
  );
}

// 获取最近的一个备份时间区间: 日志备份时间区间 或 数据备份时间点
export function getLatestBackupRangeTime(
  logList: API.ObRecoverableSectionItem[],
  dataList: API.ObRecoverableSectionItem[],
  format = DATE_TIME_FORMAT_DISPLAY
) {
  // 优先展示日志备份时间区间
  return (
    <span>
      {logList.length > 0
        ? logList
          // 展示最后一项
          .slice(logList.length - 1, logList.length)
          .map(item => (
            <span key={item.logRecoverableTimeInterval?.startTime}>
              {`${formatTimeWithMicroseconds(
                item.logRecoverableTimeInterval?.startTime,
                format
              )} ~${formatTimeWithMicroseconds(
                item.logRecoverableTimeInterval?.endTime,
                format
              )}`}
            </span>
          ))
        : dataList
          // 展示最后一项
          .slice(dataList.length - 1, dataList.length)
          .map(item => (
            <span key={item.dataBackupRecoverableInfo?.recoverableTime}>
              {`${formatTimeWithMicroseconds(
                item.dataBackupRecoverableInfo?.recoverableTime,
                format
              )}`}
            </span>
          ))}
    </span>
  );
}
