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

import ContentWithQuestion from '@/component/ContentWithQuestion';
import ContentWithReload from '@/component/ContentWithReload';
import * as ObPlanController from '@/service/ocp-express/ObPlanController';
import { formatTime } from '@/util/datetime';
import { formatMessage } from '@/util/intl';
import { Col, Descriptions, Divider, Drawer, Row, Table, Space } from '@oceanbase/design';
import React, { useEffect, useState } from 'react';
import { DownOutlined, EyeOutlined, UpOutlined } from '@ant-design/icons';
import { useRequest } from 'ahooks';
import type { ColumnProps } from 'antd/es/table';
import useStyles from './index.style';

interface ServerDrawerProps {
  tenantId: number;
  record: API.PlanStatDetail | null;
  visible: boolean;
  onClose: () => void;
  startTime: string;
  endTime: string;
}

export const ServerDrawer: React.FC<ServerDrawerProps> = (props) => {
  const { styles } = useStyles();
  const { record, visible, tenantId, onClose, startTime, endTime } = props;
  const [propertyVis, setPropertyVis] = useState(false);
  const [expandedKeys, setExpandedKeys] = useState<string[]>([]);
  const [allExpandedKeyLength, setAllExpandedKeyLength] = useState(0);
  const [selected, setSelected] = useState<API.PlanOperation | null>(null);

  const {
    data: plansData,
    loading,
    run: doPlanExplain,
  } = useRequest(ObPlanController.planExplain, {
    manual: true,
    onSuccess: () => {
      // 默认展开执行步骤
      const keys: string[] = getKeys(plans);
      // 默认展开 22 项目
      setExpandedKeys(keys.slice(0, 22));
      setAllExpandedKeyLength(keys.length);
    },
  });

  const plans = plansData?.data?.rootOperations || [];

  const getPlanRowKey = (r: API.PlanOperation) => {
    return `${r?.objectName}${r?.cost}${r?.operator}${r?.property}`;
  };

  useEffect(() => {
    reload();
  }, [record]);

  const columns: ColumnProps<API.PlanOperation>[] = [
    {
      title: formatMessage({
        id: 'ocp-express.SQLDiagnosis.Component.Plan.Operator',
        defaultMessage: '算子',
      }),

      dataIndex: 'operator',
      width: 300,
    },

    {
      title: formatMessage({
        id: 'ocp-express.SQLDiagnosis.Component.Plan.Name',
        defaultMessage: '名称',
      }),

      width: 166,
      dataIndex: 'objectName',
      fixed: 'right',
      render: (node) => {
        // todo 下个版本再支持查看 ddl
        // return <a onClick={() => showDDL(r)}>{node}</a>;
        return node;
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.SQLDiagnosis.Component.Plan.EstimatedRows',
        defaultMessage: '预估行',
      }),

      width: 150,
      dataIndex: 'rows',
      fixed: 'right',
    },

    {
      title: (
        <ContentWithQuestion
          content={formatMessage({
            id: 'ocp-express.SQLDiagnosis.Component.Plan.Price',
            defaultMessage: '代价',
          })}
          tooltip={{
            title: formatMessage({
              id: 'ocp-express.SQLDiagnosis.Component.Plan.TheCostAndScaleOf',
              defaultMessage:
                'SQL 引擎预估的算子的成本及规模。代价的评估会考虑谓词、数据行数等诸多因素。引擎会选择总代价最低的路径执行 SQL。',
            }),
          }}
        />
      ),

      width: 130,
      dataIndex: 'rows',
      fixed: 'right',
    },

    {
      title: formatMessage({
        id: 'ocp-express.SQLDiagnosis.Component.Plan.OutputAndFilter',
        defaultMessage: '输出 & 过滤',
      }),

      width: 360,
      dataIndex: 'property',
      fixed: 'right',
      render: (node, r) => {
        return (
          <Space style={{ width: 380 }}>
            <span className="ellipsis" style={{ width: 360 }}>
              {node}
            </span>
            <a
              onClick={() => {
                setPropertyVis(true);
                setSelected(r);
              }}
            >
              <EyeOutlined />
            </a>
          </Space>
        );
      },
    },
  ];

  const onExpand = (expanded: boolean, r: API.PlanOperation) => {
    const key = getPlanRowKey(r);
    setExpandedKeys(expanded ? [...expandedKeys, key] : expandedKeys.filter((k) => k !== key));
  };

  const getKeys = (operations) => {
    const keys: string[] = [];
    const getTreekey = (records: API.PlanOperation[]) => {
      records.forEach((r) => {
        // 每个 item 的最后一个 child 并不会有 expand
        if (r.children) {
          keys.push(getPlanRowKey(r));
          getTreekey(r.children);
        }
      });
    };
    getTreekey(operations);

    return keys;
  };

  const expandAll = () => {
    const keys: string[] = getKeys(plans);

    setExpandedKeys(keys);
    setAllExpandedKeyLength(keys.length);
  };

  const reload = () => {
    if (record === null) return;

    doPlanExplain({
      planUid: record?.uid,
      tenantId,
      startTime,
      endTime,
    });
  };

  const allExpansion = expandedKeys?.length === allExpandedKeyLength && plans?.length > 0;

  return (
    <Drawer
      title={
        <ContentWithReload
          spin={loading}
          content={formatMessage({
            id: 'ocp-express.SQLDiagnosis.Component.ServerDrawer.ServerExecutionPlanDetails',
            defaultMessage: 'Server 执行计划详情',
          })}
          onClick={reload}
        />
      }
      onClose={onClose}
      visible={visible}
      width={'80%'}
      bodyStyle={{ overflow: 'auto' }}
      footer={null}
    >
      <div>
        <Descriptions
          title={formatMessage({
            id: 'ocp-express.SQLDiagnosis.Component.ServerDrawer.BasicInformation',
            defaultMessage: '基本信息',
          })}
          column={3}
        >
          <Descriptions.Item label="Server IP">{record?.server}</Descriptions.Item>
          <Descriptions.Item
            label={formatMessage({
              id: 'ocp-express.SQLDiagnosis.Component.ServerDrawer.PlanHash',
              defaultMessage: 'Plan Hash',
            })}
          >
            {record?.planHash}
          </Descriptions.Item>
          <Descriptions.Item
            label={formatMessage({
              id: 'ocp-express.SQLDiagnosis.Component.ServerDrawer.PlanId',
              defaultMessage: 'Plan ID',
            })}
          >
            {record?.planId}
          </Descriptions.Item>
          <Descriptions.Item
            label={formatMessage({
              id: 'ocp-express.SQLDiagnosis.Component.ServerDrawer.ScheduledGenerationTime',
              defaultMessage: '计划生成时间',
            })}
          >
            {formatTime(record?.firstLoadTime)}
          </Descriptions.Item>
          <Descriptions.Item
            label={formatMessage({
              id: 'ocp-express.SQLDiagnosis.Component.ServerDrawer.CpuTime',
              defaultMessage: 'CPU 时间',
            })}
          >
            {`${record?.cpuTime}ms`}
          </Descriptions.Item>
        </Descriptions>
        <Divider className={styles.planDivider} />
        <Row style={{ paddingTop: 10, paddingBottom: 10 }}>
          <Col span={4}>
            <span className={styles.planTitle}>
              {formatMessage({
                id: 'ocp-express.SQLDiagnosis.Component.Plan.Procedure',
                defaultMessage: '执行步骤',
              })}
            </span>
          </Col>
          {allExpandedKeyLength > 0 && (
            <Col span={4} offset={16}>
              <a
                onClick={() => (allExpansion ? setExpandedKeys([]) : expandAll())}
                style={{ float: 'right' }}
              >
                {allExpansion ? (
                  <UpOutlined style={{ marginRight: 4 }} />
                ) : (
                  <DownOutlined style={{ marginRight: 4 }} />
                )}

                {allExpansion
                  ? formatMessage({
                      id: 'ocp-express.SQLDiagnosis.Component.Plan.FoldAll',
                      defaultMessage: '全部收起',
                    })
                  : formatMessage({
                      id: 'ocp-express.SQLDiagnosis.Component.Plan.ExpandAll',
                      defaultMessage: '全部展开',
                    })}
              </a>
            </Col>
          )}
        </Row>
        <Row style={{ paddingTop: 10, paddingBottom: 10 }}>
          <Table
            rowKey={getPlanRowKey}
            className={styles.obPlanTable}
            loading={loading}
            columns={columns}
            dataSource={plans}
            pagination={false}
            scroll={{ x: 'auto', y: 500 }}
            expandable={{
              onExpand,
              expandedRowKeys: expandedKeys,
            }}
          />
        </Row>
        {/** 输出过滤面板 */}
        <Drawer
          title={formatMessage({
            id: 'ocp-express.SQLDiagnosis.Component.Plan.OutputAndFilter',
            defaultMessage: '输出 & 过滤',
          })}
          visible={propertyVis}
          width={1056}
          onClose={() => {
            setPropertyVis(false);
            setSelected(null);
          }}
        >
          <div
            style={{
              height: '100%',
              backgroundColor: '#F8FAFE',
              padding: '0 8px',
              borderRadius: '8px',
              wordWrap: 'break-word',
            }}
          >
            {selected?.property}
          </div>
        </Drawer>
      </div>
    </Drawer>
  );
};
