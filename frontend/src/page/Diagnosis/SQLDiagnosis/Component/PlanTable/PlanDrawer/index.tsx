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
  Col,
  Descriptions,
  Divider,
  Drawer,
  Row,
  Space,
  Tooltip,
  Typography,
  Table,
} from '@oceanbase/design';
import React, { useEffect, useMemo, useState } from 'react';
import { max } from 'lodash';
import { findByValue, formatNumber, sortByMoment } from '@oceanbase/util';
import {
  DownOutlined,
  ExclamationCircleOutlined,
  EyeOutlined,
  UpOutlined,
} from '@oceanbase/icons';
import type { ColumnProps } from 'antd/es/table';
import { PLAN_TYPE_LIST } from '@/constant/tenant';
import { formatTime } from '@/util/datetime';
import ContentWithQuestion from '@/component/ContentWithQuestion';
import { ServerDrawer } from './ServerDrawer';
import styles from './index.less';

const { Text } = Typography;

interface TopSQLPlanProps {
  tenantId: number;
  PlanStatGroup: API.PlanStatGroup | null;
  visible: boolean;
  onClose: () => void;
  startTime: string;
  endTime: string;
  rangeKey: string;
}

const PlanDrawer: React.FC<TopSQLPlanProps> = props => {
  const { PlanStatGroup, visible, tenantId, onClose, startTime, endTime, rangeKey } = props;
  const [propertyVis, setPropertyVis] = useState(false);
  const [expandedKeys, setExpandedKeys] = useState<string[]>([]);
  const [allExpandedKeyLength, setAllExpandedKeyLength] = useState(0);
  const [serverVisible, setServerVisible] = useState(false);
  const [serverRecord, setServerRecord] = useState<API.PlanStatDetail | null>(null);
  const [selected, setSelected] = useState<API.PlanOperation | null>(null);

  const rootOperations = PlanStatGroup?.planExplain?.rootOperations || [];
  const plans = PlanStatGroup?.plans;
  /** 响应时间的横条图 */
  const renderLine = (myRecord: API.PlanStatDetail, key: string) => {
    const maxValue = max(plans?.map(plan => plan[key]));
    const rate = (myRecord[key] || 0) / (maxValue as number);

    return (
      <div>
        <div
          style={{
            width: rate * 130,
            backgroundColor: '#5b8ff9',
            height: 10,
            display: 'inline-block',
          }}
        />

        <span style={{ marginLeft: 12 }}>{myRecord[key]}</span>
      </div>
    );
  };

  const serverColumns = [
    {
      title: 'Server IP',
      dataIndex: 'server',
      render: (node: string, myRecord: API.PlanStatDetail) => (
        <Space>
          <Text className="typography-copyable-hover" copyable={{ text: node }}>
            <a
              onClick={() => {
                setServerRecord(myRecord);
                setServerVisible(true);
              }}
            >
              {node}
            </a>
          </Text>
          {PlanStatGroup?.hitDiagnosis && (
            <Button size="small" icon={<ExclamationCircleOutlined style={{ color: '#fa8c16' }} />}>
              <span style={{ fontSize: 12 }}>
                {formatMessage({
                  id: 'ocp-express.SQLDiagnosis.Component.Plan.RecommendedAttention',
                  defaultMessage: '建议关注',
                })}
              </span>
            </Button>
          )}
        </Space>
      ),
    },

    {
      title: 'Plan ID',
      dataIndex: 'planId',
      render: (node: number) => <Text copyable>{node}</Text>,
    },

    {
      title: formatMessage({
        id: 'ocp-express.SQLDiagnosis.Component.Plan.PlanGenerationTime',
        defaultMessage: '计划生成时间',
      }),
      dataIndex: 'firstLoadTime',
      render: (text: string) => formatTime(text),
      sorter: (a: string, b: string) => sortByMoment(a, b, 'firstLoadTime'),
    },

    {
      title: formatMessage({
        id: 'ocp-express.SQLDiagnosis.Component.Plan.CpuTimeMs',
        defaultMessage: 'CPU 时间（ms）',
      }),
      dataIndex: 'cpuTime',
      render: (text: number, myRecord: API.PlanStatDetail) => renderLine(myRecord, 'cpuTime'),
      sorter: (a: number, b: number) => sortByMoment(a, b, 'cpuTime'),
    },
  ];

  const getPlanRowKey = (r: API.PlanOperation) => {
    return `${r?.objectName}${r?.cost}${r?.operator}${r?.property}`;
  };

  // 根据 operations 的 child 层级计算出展示算子内容所需要的宽度，最低为 300
  const operatorWidth = useMemo(() => {
    const childrenList = [...rootOperations];
    // i 是用来表示 child 最深的层级是多少
    let i = 0;
    while (childrenList.length) {
      // 每一层的算子数量
      let size = childrenList.length;
      // 循环将这层的算子移除出数组，并用来判断是否具有 children
      while (size-- > 0) {
        const c = childrenList.shift();
        if (c?.children) {
          // 将 children 加入数组中
          childrenList.push(...c.children);
        }
      }
      // 每往下一层 i + 1
      i++;
    }
    // 16 为 padding，每一个子项缩进 15，缩进按钮 16 + 8，默认给内容 150 的展示空间
    const maxWidth = 16 + (15 * i - 1) + 16 + 8 + 150;

    return maxWidth > 300 ? maxWidth : 300;
  }, [rootOperations]);

  const columns: ColumnProps<API.PlanOperation>[] = [
    {
      title: formatMessage({
        id: 'ocp-express.SQLDiagnosis.Component.Plan.Operator',
        defaultMessage: '算子',
      }),

      dataIndex: 'operator',
      width: operatorWidth,
      ellipsis: true,
      render: text => {
        return (
          <Tooltip placement="topLeft" title={text}>
            {text}
          </Tooltip>
        );
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.SQLDiagnosis.Component.Plan.Name',
        defaultMessage: '名称',
      }),

      width: 166,
      dataIndex: 'objectName',
      fixed: 'right',
      render: node => {
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
    setExpandedKeys(expanded ? [...expandedKeys, key] : expandedKeys.filter(k => k !== key));
  };

  const getKeys = operations => {
    const keys: string[] = [];
    const getTreekey = (records: API.PlanOperation[]) => {
      records.forEach(r => {
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
    const keys = getKeys(rootOperations);
    setExpandedKeys(keys);
    setAllExpandedKeyLength(keys.length);
  };

  const allExpansion = expandedKeys?.length === allExpandedKeyLength && rootOperations?.length > 0;

  // 初始化展开算子
  useEffect(() => {
    if (visible) {
      const keys = getKeys(rootOperations);
      setExpandedKeys(keys?.slice(0, 22));
      setAllExpandedKeyLength(keys.length);
    }
  }, [visible]);

  return (
    <Drawer
      title={formatMessage({
        id: 'ocp-express.SQLDiagnosis.Component.Plan.PlanHashExecutionPlanDetails',
        defaultMessage: 'Plan Hash 执行计划详情',
      })}
      onClose={onClose}
      visible={visible}
      width={'80%'}
      bodyStyle={{ overflow: 'auto' }}
      footer={null}
    >
      <Descriptions
        title={formatMessage({
          id: 'ocp-express.SQLDiagnosis.Component.Plan.BasicInformation',
          defaultMessage: '基本信息',
        })}
        column={3}
      >
        <Descriptions.Item
          label={formatMessage({
            id: 'ocp-express.SQLDiagnosis.Component.Plan.PlanHash',
            defaultMessage: 'Plan Hash',
          })}
        >
          {PlanStatGroup?.planHash}
        </Descriptions.Item>
        <Descriptions.Item
          label={formatMessage({
            id: 'ocp-express.SQLDiagnosis.Component.Plan.ExecutionPlanType',
            defaultMessage: '执行计划类型',
          })}
        >
          {findByValue(PLAN_TYPE_LIST, PlanStatGroup?.planType)?.label}
        </Descriptions.Item>
        <Descriptions.Item
          label={formatMessage({
            id: 'ocp-express.SQLDiagnosis.Component.Plan.ScheduledGenerationTime',
            defaultMessage: '计划生成时间',
          })}
        >
          {formatTime(PlanStatGroup?.firstLoadTime)}
        </Descriptions.Item>
        <Descriptions.Item
          label={formatMessage({
            id: 'ocp-express.SQLDiagnosis.Component.Plan.CpuTime',
            defaultMessage: 'CPU 时间',
          })}
        >
          {`${PlanStatGroup?.avgCpuTime}ms`}
        </Descriptions.Item>
        {PlanStatGroup?.hitPercentage && (
          <Descriptions.Item
            label={
              <ContentWithQuestion
                content={formatMessage({
                  id: 'ocp-express.SQLDiagnosis.Component.Plan.HitRatio',
                  defaultMessage: '命中率',
                })}
                tooltip={{
                  title: formatMessage({
                    id: 'ocp-express.SQLDiagnosis.Component.Plan.TheNumberOfTimesThat',
                    defaultMessage: 'SQL 使用该执行计划的次数 / SQL 执行的次数',
                  }),
                }}
              />
            }
          >
            {`${formatNumber(PlanStatGroup?.hitPercentage)}%`}
          </Descriptions.Item>
        )}
      </Descriptions>
      <div>
        <Divider className={styles.planDivider} />
        <Row style={{ paddingTop: 12, paddingBottom: 12 }}>
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
        <Row style={{ paddingTop: 12, paddingBottom: 12 }}>
          <Table
            rowKey={getPlanRowKey}
            className={styles.obPlanTable}
            columns={columns}
            dataSource={rootOperations}
            pagination={false}
            scroll={{ x: 'auto', y: 500 }}
            expandable={{
              onExpand,
              expandedRowKeys: expandedKeys,
            }}
          />
        </Row>
        <Divider className={styles.planDivider} />

        <Row style={{ paddingTop: 12, paddingBottom: 12 }}>
          <div className={styles.planTitle}>
            {formatMessage({
              id: 'ocp-express.SQLDiagnosis.Component.Plan.ServerExecutionPlan',
              defaultMessage: 'Server 执行计划',
            })}
          </div>
          <Table
            style={{ width: '100%', paddingTop: 20 }}
            columns={serverColumns}
            dataSource={plans}
          />
        </Row>

        <ServerDrawer
          tenantId={tenantId}
          record={serverRecord}
          startTime={startTime}
          endTime={endTime}
          rangeKey={rangeKey}
          onClose={() => setServerVisible(false)}
          visible={serverVisible}
        />

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

export default PlanDrawer;
