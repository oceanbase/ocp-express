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
import {
  Button,
  Empty,
  Form,
  InputNumber,
  Radio,
  Space,
  Tooltip,
  Dropdown,
  Menu,
  Table,
  Modal,
  message,
} from '@oceanbase/design';
import { EllipsisOutlined } from '@ant-design/icons';
import React, { useEffect, useRef, useState } from 'react';
import { groupBy, max, omit, omitBy, toNumber } from 'lodash';
import type { Moment } from 'moment';
import moment from 'moment';
import { stringify } from 'query-string';
import { isNullValue, jsonParse, useScrollToPosition } from '@oceanbase/util';
import { useRequest, useSetState } from 'ahooks';
import * as ObSqlStatController from '@/service/ocp-express/ObSqlStatController';
import * as ObPlanController from '@/service/ocp-express/ObPlanController';
import * as ObOutlineController from '@/service/ocp-express/ObOutlineController';
import { DATE_TIME_FORMAT, RFC3339_DATE_TIME_FORMAT } from '@/constant/datetime';
import { NEAR_30_MINUTES } from '@/component/OCPRangePicker/constant';
import useCompare from '@/hook/useCompare';
import { formatterNumber, includesChinese } from '@/util';
import ContentWithQuestion from '@/component/ContentWithQuestion';
import type { FormInstance } from 'antd/es/form';
import type { ColumnProps } from 'antd/es/table';
import PlanTable from '../PlanTable';
import { SqlText } from '../SqlText';
import styles from './index.less';

export interface SQLTableProps {
  location: {
    query: SQLDiagnosis.QueryValues;
  };

  form: FormInstance<any>;
  setQueryValues: any;
  queryValues: SQLDiagnosis.QueryValues;
  sqlType: SQLDiagnosis.SqlType;
  actives: SQLDiagnosis.SqlAuditStatDetailAttribute[];
  fields: SQLDiagnosis.SqlAuditStatDetailAttribute[];
  setActives: () => void;
}

const SQLTable: React.FC<SQLTableProps> = ({
  location,
  setQueryValues,
  queryValues,
  sqlType,
  actives,
  fields,
  setActives = () => { },
}) => {
  const tenantId = location?.query?.tenantId;
  const [showTable, setShowTable] = useState(false);
  const [selectedRows, setSelectedRows] = useState<API.SqlAuditStatSummary[]>([]);
  const [visible, setVisible] = useState(false);
  const [sqlRecord, setSqlrecord] = useState<API.SqlAuditStatSummary>();
  const [isBatch, setIsBatch] = useState(false);
  const onceSetActives = useRef<(() => void) | null>(setActives);

  const { systemInfo } = useSelector((state: DefaultRootState) => state.global);
  // 链路查询是否可用: 数据中台开启 + 链路采集开启
  const traceEnabled =
    systemInfo.analyzeInfo?.analyzeEnabled && systemInfo.analyzeInfo?.traceEnabled;

  // 用于查询的 SQL ID
  const searchSqlId = jsonParse(queryValues.filterExpressionList, [])?.find(
    item => item.searchAttr === 'sqlId'
  )?.searchVal;

  const {
    data: sqlData = {},
    runAsync: updateSqlList,
    loading,
  } = useRequest(
    (params: {
      range: [Moment, Moment];
      inner: boolean;
      serverId: string;
      sqlText: string;
      filterExpression: string;
    }) => {
      const [start, end] = params.range;

      const myParams = {
        tenantId,
        startTime: start.format(RFC3339_DATE_TIME_FORMAT),
        endTime: end.format(RFC3339_DATE_TIME_FORMAT),
        inner: params.inner || false,
        sqlText: params.sqlText,
        serverId: params.serverId && params.serverId !== 'all' ? params.serverId : undefined,
        filterExpression: params.filterExpression || undefined,
      };

      if (sqlType === 'topSql') {
        return ObSqlStatController.topSql(myParams);
      } else if (sqlType === 'slowSql') {
        return ObSqlStatController.slowSql(myParams);
      }
    },
    {
      manual: true,
      onSuccess: () => {
        // 只要请求返回，就展示 Table，不需要判断 successful
        // 就算请求失败，也应该渲染 Table
        setShowTable(true);

        if (typeof onceSetActives.current === 'function') {
          // 设置排序高亮 但是只需要设置第一次
          onceSetActives.current();
          onceSetActives.current = null;
        }
      },
    }
  );

  const sqlList = sqlData?.data?.contents || [];

  const refresh = () => {
    const { startTime, endTime } = queryValues;
    if (startTime && endTime) {
      updateSqlList({ ...queryValues, range: [moment(startTime), moment(endTime)] });
    }
  };

  // 获取已绑定的 outlineList
  const { data: sqlOutlineData, refresh: refreshSqlOutline } = useRequest(
    ObOutlineController.getSqlOutline,
    {
      defaultParams: [
        {
          tenantId,
        },
      ],

      ready: !!tenantId,
    }
  );

  const sqlOutlineList = sqlOutlineData?.data?.contents || [];

  // 重新请求 sql 列表、outline 列表，以及重置多选按钮
  const refreshAll = () => {
    refresh();
    refreshSqlOutline();
    setSelectedRows([]);
    setSqlrecord(undefined);
  };

  const getFailContent = (failedSql = {}) => {
    return (
      <Space direction="vertical">
        {Object.keys(failedSql).map((key, index) => {
          return (
            <div
              key={key}
              style={{
                wordBreak: 'break-all',
              }}
            >
              {`${index + 1}. ${key}: ${failedSql[key]}`}
            </div>
          );
        })}
      </Space>
    );
  };

  // 批量创建限流
  const { runAsync: batchCreateOutline } = useRequest(ObOutlineController.batchCreateOutline, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        if (res.data?.result) {
          refreshAll();
          setVisible(false);
          message.success(
            formatMessage({
              id: 'ocp-express.Component.SQLTable.SqlThrottlingIsSet',
              defaultMessage: 'SQL 限流设置成功',
            })
          );
        } else {
          Modal.error({
            // 错误信息较长，需要加大 Modal 宽度
            width: 600,
            title: formatMessage({
              id: 'ocp-express.Component.SQLTable.AnErrorOccurredWhileSetting',
              defaultMessage: 'SQL 限流设置失败',
            }),

            content: getFailContent(res.data?.failedSql),
          });
        }
      }
    },
  });

  // 批量取消限流
  const { runAsync: batchDropSqlOutline } = useRequest(ObOutlineController.batchDropSqlOutline, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        if (res.data?.result) {
          refreshAll();
          setVisible(false);
          message.success(
            formatMessage({
              id: 'ocp-express.Component.SQLTable.SqlThrottlingCanceled',
              defaultMessage: 'SQL 限流取消成功',
            })
          );
        } else {
          Modal.error({
            // 错误信息较长，需要加大 Modal 宽度
            width: 600,
            title: formatMessage({
              id: 'ocp-express.Component.SQLTable.FailedToCancelTheSql',
              defaultMessage: 'SQL 限流取消失败',
            }),

            content: getFailContent(res.data?.failedSql),
          });
        }
      }
    },
  });

  // 对内部的值进行比较 , 前端进行排序，筛选， 所以 'filters', 'sorter', 'page', 'size', 'fields' 'filterExpressionList' 的改变不应该重新发起请求
  const deps = useCompare(
    omit(queryValues, 'filters', 'sorter', 'page', 'size', 'fields', 'filterExpressionList')
  );

  useEffect(() => {
    if (!!tenantId) {
      refresh();
    }
  }, [deps]);

  const renderSpecialColumn = (
    field: API.SqlAuditStatSummaryAttribute,
    node: any,
    record: API.SqlAuditStatSummary
  ) => {
    switch (field.name) {
      // 截断的 SQL 文本: 因为 SQL 文本可能很长，且列表接口不分页，为了避免列表接口数据太大导致响应慢，因此列表中的 SQL 文本会在后端被截断
      case 'sqlTextShort':
        const sqlOutline = sqlOutlineList.find(
          item => item.sqlId === record.sqlId && item.type === 'CONCURRENT_LIMIT'
        );

        return (
          <SqlText
            canJump={false}
            startTime={queryValues.startTime}
            endTime={queryValues.endTime}
            node={node}
            record={record}
            sqlId={record.sqlId as string}
            dbName={record.dbName as string}
            tenantId={Number(tenantId)}
            copyable={true}
            isLimit={!!sqlOutline}
          />
        );

      // 平均响应时间和总响应时间，使用长度条 + 数字展示
      case 'avgElapsedTime':
      case 'sumElapsedTime':
        const maxValue = max(sqlList.map(topSql => topSql[field.name]));
        const rate = (record[field.name] || 0) / (maxValue as number);
        return (
          <div>
            <div
              style={{
                width: rate * 120,
                backgroundColor: '#5b8ff9',
                height: 6,
                display: 'inline-block',
              }}
            />

            <span className={styles.opcGridTarget}>{record[field.name]}</span>
          </div>
        );

      default:
        if (field.dataType === 'BOOLEAN') {
          return node
            ? formatMessage({
              id: 'ocp-express.SQLDiagnosis.Component.SQLTable.Is',
              defaultMessage: '是',
            })
            : formatMessage({
              id: 'ocp-express.SQLDiagnosis.Component.SQLTable.No',
              defaultMessage: '否',
            });
        }
        if (field.dataType === 'FLOAT' || field.dataType === 'INTEGER') {
          return formatterNumber(node as number);
        }
        return isNullValue(node) ? (
          '-'
        ) : (
          <Tooltip title={node} placement="topLeft">
            {node}
          </Tooltip>
        );
    }
  };

  let columns = fields.map(field => {
    const queryFilter = jsonParse(location?.query?.filters || '', {});
    const querySorter = jsonParse(location?.query?.sorter || '', {});

    const filterWithSortParams = {
      // 从 queryValues 中回填 filter 的值
      ...(queryFilter?.[field.name as string]
        ? {
          defaultFilteredValue: queryFilter?.[field.name as string],
        }
        : {}),
      ...(querySorter?.field === field.name && querySorter?.order
        ? {
          // 排序和 query 强绑定
          sortOrder: querySorter?.order,
        }
        : {}),
    };

    const defaultSorterItem = {};
    if (!querySorter?.order) {
      // 可疑 SQL 的默认排序列为 `平均响应时间`，其他 SQL 的默认排序列为 `总响应时间`
      if (sqlType === 'suspiciousSql' && field.name === 'avgElapsedTime') {
        defaultSorterItem.defaultSortOrder = 'descend';
      } else if (sqlType !== 'suspiciousSql' && field.name === 'sumElapsedTime') {
        defaultSorterItem.defaultSortOrder = 'descend';
      }
    }

    // 32 为左右 padding 之和
    const fieldNum = 32 + (field?.title?.length || 0) * (includesChinese(field?.title) ? 20 : 10);
    let realWidth: number = fieldNum > 100 ? fieldNum : 100;

    if (field.tooltip) {
      realWidth += 20;
    }

    // 带单位
    if (field.unit) {
      realWidth += 30;
    }

    // 带操作
    if (field.operation) {
      realWidth += 10;
    }

    const column: ColumnProps<API.SqlAuditStatSummary> = {
      title: (
        <ContentWithQuestion
          content={
            field.unit
              ? formatMessage(
                {
                  id: 'ocp-express.Component.SQLTable.FieldtitleFieldunit',
                  defaultMessage: '{fieldTitle}（{fieldUnit}）',
                },

                { fieldTitle: field.title, fieldUnit: field.unit }
              )
              : field.title
          }
          tooltip={{ arrowPointAtCenter: true, title: field.tooltip }}
        />
      ),

      dataIndex: field.name as string,
      // sqlTextShort 本身已经通过 Typography.Link 做了 ellipsis，就不需要设置列 ellipsis 了
      ellipsis: field.name !== 'sqlTextShort',
      fixed: ['diagTypes', 'highRiskTypes', 'sqlTextShort'].includes(field.name),
      width: realWidth,
      // 关闭排序的额 tooltip，避免与列描述的 tooltip 相互重叠，影响体验
      showSorterTooltip: false,
      className: actives.find(attr => attr.name === field.name) ? styles.active : '',
      className: styles.active,
      ...(field.operation === 'SORT'
        ? {
          sorter: (a, b) => a[field.name as string] - b[field.name as string],
        }
        : {}),
      ...defaultSorterItem,
      filters:
        field.operation === 'FILTER'
          ? Object.keys(groupBy(sqlList, sql => sql[field.name as string])).map(key => ({
            // 无值时使用 - 来当做默认值
            text:
              key === 'undefined'
                ? formatMessage({
                  id: 'ocp-express.SQLDiagnosis.Component.SQLTable.No.1',
                  defaultMessage: '无',
                })
                : key,
            value: key === 'undefined' ? '-' : key,
          }))
          : undefined,
      ...filterWithSortParams,
      // - 默认值时，使用 undefined 去判断
      onFilter: (value, record) =>
        value === '-'
          ? record[field.name as string] === undefined
          : record[field.name as string] === value,
      render: (text, record) => renderSpecialColumn(field, text, record),
    };

    if (field.name === 'sqlTextShort') {
      column.width = 200;
    }
    if (field.name === 'sqlId') {
      column.width = 160;
    }

    if (['avgElapsedTime', 'sumElapsedTime'].includes(field.name)) {
      column.width = 240;
    }
    return column;
  });

  columns = [
    ...columns,
    {
      title: formatMessage({
        id: 'ocp-express.Component.SQLTable.Actions',
        defaultMessage: '操作',
      }),
      dataIndex: 'operation',
      fixed: 'right',
      width: 80,
      render: (text, record: API.SqlAuditStatSummary) => {
        const noDatabase = record.dbName === 'NO_DATABASE';
        return (
          <Dropdown
            overlay={
              <Menu>
                <Menu.Item
                  key="limit"
                  disabled={noDatabase}
                  onClick={({ domEvent }) => {
                    // 阻止事件冒泡，避免触发行展开/收起
                    domEvent?.stopPropagation();
                    setVisible(true);
                    setIsBatch(false);
                    setSqlrecord(record);
                  }}
                >
                  <Tooltip
                    title={
                      noDatabase &&
                      formatMessage({
                        id: 'ocp-express.Component.SQLTable.ThrottlingIsNotSupportedForSqlInstancesWith',
                        defaultMessage: '数据库为空的 SQL 不支持设置限流',
                      })
                    }
                  >
                    <span
                      data-aspm-click="c304257.d308762"
                      data-aspm-desc="SQL 列表-设置限流"
                      data-aspm-param={``}
                      data-aspm-expo
                    >
                      {formatMessage({
                        id: 'ocp-express.Component.SQLTable.SetThrottling',
                        defaultMessage: '设置限流',
                      })}
                    </span>
                  </Tooltip>
                </Menu.Item>
              </Menu>
            }
          >
            <a>
              <EllipsisOutlined />
            </a>
          </Dropdown>
        );
      },
    },
  ];

  const handlePageChange = (current: number, pageSize?: number) => {
    setQueryValues({ ...queryValues, page: current, size: pageSize || 20 });
  };

  const handleChange = (pagination, filters, sorter) => {
    const params = {
      // filters 排除 null 值
      filters: JSON.stringify(omitBy(filters, v => isNullValue(v))),
      sorter: JSON.stringify({ field: sorter.field, order: sorter.order }),
    };

    setQueryValues({
      ...queryValues,
      page: pagination?.current,
      size: pagination?.pageSize || 20,
      ...params,
    });
  };

  useScrollToPosition(document.body, {
    ready: columns.length > 0 && showTable,
  });

  const getRowKey = (record: API.SqlAuditStatSummary) =>
    record.sqlId + '__EXPRESS__' + record.dbName;

  const rowSelection = {
    selectedRowKeys: selectedRows.map(getRowKey),
    onChange: (keys, rows) => {
      setSelectedRows(rows);
    },
    getCheckboxProps: (record: API.SqlAuditStatSummary) => ({
      // 数据库为空的 SQL 不支持设置限流
      disabled: record.dbName === 'NO_DATABASE',
    }),
  };

  const [planState, setPlanState] = useSetState<Record<string, any>>({});
  const [planLoadingState, setPlanLoadingState] = useSetState<Record<string, boolean>>({});

  const expandedRowRender = (record: API.SqlAuditStatSummary) => {
    const id = getRowKey(record);
    return (
      <div style={{ margin: '12px 0 12px 48px', backgroundColor: '#fff' }}>
        <PlanTable
          tenantId={tenantId}
          startTime={queryValues.startTime}
          endTime={queryValues.endTime}
          rangeKey={queryValues.rangeKey}
          topPlans={planState[id] || []}
          topPlansLoading={planLoadingState[id] || false}
        />
      </div>
    );
  };
  const [outlineForm] = Form.useForm();

  // 检索当前设置限流的 sql，原先是否存在过限流
  const sqlOutline = sqlOutlineList.find(
    item => item.sqlId === sqlRecord?.sqlId && item.type === 'CONCURRENT_LIMIT'
  );

  const toolOptionsRender = () => {
    return [
      <Button
        key="batch-limit"
        data-aspm-click="c304257.d308760"
        data-aspm-desc="SQL 列表-批量设置限流"
        data-aspm-param={``}
        data-aspm-expo
        onClick={() => {
          setVisible(true);
          setIsBatch(true);
        }}
      >
        {formatMessage({
          id: 'ocp-express.Component.SQLTable.SetThrottling.2',
          defaultMessage: '批量设置限流',
        })}
      </Button>,
    ];
  };

  return (
    <>
      <Table
        loading={loading}
        columns={columns}
        dataSource={sqlList}
        pagination={{
          defaultPageSize: toNumber(queryValues.size) || 20,
          defaultCurrent: toNumber(queryValues.page) || 1,
          showSizeChanger: true,
          onChange: handlePageChange,
          showTotal: total =>
            formatMessage(
              {
                id: 'ocp-express.Component.SQLTable.TotalTotal',
                defaultMessage: '共 {total} 条',
              },
              { total: total }
            ),
        }}
        toolOptionsRender={toolOptionsRender}
        rowSelection={rowSelection}
        rowKey={getRowKey}
        expandable={{
          expandedRowRender,
        }}
        onExpand={(expanded, record) => {
          const id = getRowKey(record);

          // 打开下拉框，并且未请求过当前执行计划时，再发起请求
          if (expanded && isNullValue(planState[id])) {
            const { sqlId, dbName } = record;

            const { startTime, endTime } = location?.query;

            setPlanLoadingState({
              [id]: true,
            });

            ObPlanController.topPlanGroup({
              tenantId,
              sqlId,
              startTime,
              endTime,
              dbName,
            }).then(res => {
              setPlanLoadingState({
                [id]: false,
              });

              if (res.successful) {
                const topPlans = res?.data?.contents || [];
                setPlanState({
                  [id]: topPlans,
                });
              }
            });
          }
        }}
        onChange={handleChange}
        scroll={{ x: 1000 }}
        className={styles.table}
        locale={
          // 引导到链路查询的条件: 链路查询可用 + 有链路查询的权限 + 根据 SQL ID 查询到的 SlowSQL 数据为空
          traceEnabled && sqlType === 'slowSql' && searchSqlId
            ? {
              emptyText: (
                <Empty
                  image={Empty.PRESENTED_IMAGE_SIMPLE}
                  description={
                    <div>
                      {formatMessage({
                        id: 'ocp-express.Component.SQLTable.TheCorrespondingSlowsqlIsNotFoundGo',
                        defaultMessage: '没有找到对应的 SlowSQL，去',
                      })}

                      <a
                        href={`/log/trace?${stringify({
                          tenantId,
                          sqlId: searchSqlId,
                          startTime: moment(queryValues.startTime).format(DATE_TIME_FORMAT),
                          endTime: moment(queryValues.endTime).format(DATE_TIME_FORMAT),
                        })}`}
                        target="_blank"
                      >
                        {formatMessage({
                          id: 'ocp-express.Component.SQLTable.LinkQuery',
                          defaultMessage: '链路查询',
                        })}
                      </a>
                    </div>
                  }
                />
              ),
            }
            : {}
        }
      />

      <Modal
        title={
          isBatch
            ? formatMessage({
              id: 'ocp-express.Component.SQLTable.SetThrottling.2',
              defaultMessage: '批量设置限流',
            })
            : formatMessage({
              id: 'ocp-express.Component.SQLTable.SetThrottling',
              defaultMessage: '设置限流',
            })
        }
        visible={visible}
        destroyOnClose={true}
        onCancel={() => {
          setVisible(false);
        }}
        onOk={() => {
          outlineForm.validateFields().then(values => {
            const { concurrentNum, launchLimit } = values;
            Modal.confirm({
              title: isBatch
                ? formatMessage({
                  id: 'ocp-express.Component.SQLTable.AreYouSureYouWant',
                  defaultMessage: '确定要批量修改限流吗？',
                })
                : formatMessage({
                  id: 'ocp-express.Component.SQLTable.AreYouSureYouWant.1',
                  defaultMessage: '确定要修改限流吗？',
                }),

              okButtonProps: { danger: true, type: 'default' },
              onOk: () => {
                const { startTime, endTime } = location?.query;
                // TODO: 待去除 NEAR_30_MINUTES 逻辑
                let range = NEAR_30_MINUTES.range();
                if (startTime && endTime) {
                  range = [moment(startTime), moment(endTime)];
                }
                if (launchLimit === 'open') {
                  const sqlListParam = isBatch
                    ? selectedRows.map(item => ({
                      dbName: item.dbName,
                      sqlId: item?.sqlId,
                    }))
                    : [
                      {
                        dbName: sqlRecord?.dbName,
                        sqlId: sqlRecord?.sqlId,
                      },
                    ];

                  return batchCreateOutline(
                    {
                      tenantId,
                    },

                    {
                      startTime: range[0].format(RFC3339_DATE_TIME_FORMAT),
                      endTime: range[1].format(RFC3339_DATE_TIME_FORMAT),
                      concurrentNum,
                      sqlList: sqlListParam,
                    }
                  );
                }

                // 取消限流依赖 sqlOutlineList 中的字段，使用 sqlId 去检索到对应的 sqlOutlineList
                const dropOutlineList = sqlOutlineList
                  .filter(item => {
                    const sqlRecordList = isBatch ? selectedRows : [sqlRecord];
                    return sqlRecordList.some(sql => sql?.sqlId === item.sqlId);
                  })
                  ?.map(item => ({
                    dbName: item?.dbName,
                    sqlId: item?.sqlId,
                    // 取消限流所需要的 outlineName 只存在 sqlOutlineList 中
                    outlineName: item?.outlineName,
                  }));

                return batchDropSqlOutline(
                  {
                    tenantId,
                  },

                  { outlineList: dropOutlineList }
                );
              },
            });
          });
        }}
      >
        <Form form={outlineForm} layout="vertical" requiredMark={false} preserve={false}>
          {/* 非批量设置限流情况下，且这条 sql 原先存在已经限流 */}
          <Form.Item
            name="launchLimit"
            label={formatMessage({
              id: 'ocp-express.Component.SQLTable.InitiateThrottling',
              defaultMessage: '发起限流',
            })}
            initialValue={!isBatch && sqlOutline ? 'open' : 'closed'}
          >
            <Radio.Group
              options={[
                {
                  label: formatMessage({
                    id: 'ocp-express.Component.SQLTable.Enable',
                    defaultMessage: '开启',
                  }),

                  value: 'open',
                },

                {
                  label: formatMessage({
                    id: 'ocp-express.Component.SQLTable.Close',
                    defaultMessage: '关闭',
                  }),

                  value: 'closed',
                },
              ]}
              optionType="button"
            />
          </Form.Item>
          <Form.Item
            noStyle={true}
            shouldUpdate={(prevValues, currentValues) =>
              prevValues.launchLimit !== currentValues.launchLimit
            }
          >
            {({ getFieldValue }) => {
              const launchLimit = getFieldValue('launchLimit');
              return (
                launchLimit === 'open' && (
                  <Form.Item
                    name="concurrentNum"
                    label={
                      <ContentWithQuestion
                        content={formatMessage({
                          id: 'ocp-express.Component.SQLTable.MaximumConcurrency',
                          defaultMessage: '最大并发数',
                        })}
                        tooltip={{
                          title: formatMessage({
                            id: 'ocp-express.Component.SQLTable.MaximumConcurrencyOfASingle',
                            defaultMessage: '单台 OBServer 执行限流 SQL 的最大并发数',
                          }),
                        }}
                      />
                    }
                    initialValue={sqlOutline?.concurrentNum || 0}
                    rules={[
                      {
                        required: true,
                        message: formatMessage({
                          id: 'ocp-express.Component.SQLTable.SetTheMaximumConcurrency',
                          defaultMessage: '请设置最大并发数',
                        }),
                      },
                    ]}
                  >
                    <InputNumber style={{ width: '40%' }} min={0} />
                  </Form.Item>
                )
              );
            }}
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default SQLTable;
