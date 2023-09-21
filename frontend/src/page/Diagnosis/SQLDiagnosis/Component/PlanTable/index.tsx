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
import { Button, Tooltip, Typography, Table } from '@oceanbase/design';
import React, { useState } from 'react';
import { findByValue } from '@oceanbase/util';
import { ExclamationCircleOutlined } from '@oceanbase/icons';
import { max } from 'lodash';
import { isEnglish } from '@/util';
import { formatTime } from '@/util/datetime';
import { PLAN_TYPE_LIST } from '@/constant/tenant';
import PlanDrawer from './PlanDrawer';

interface PlanTableProps {
  tenantId: string;
  startTime?: string;
  endTime?: string;
  rangeKey?: string;
  topPlans: API.PlanStatGroup[];
  topPlansLoading: boolean;
}

const PlanTable: React.FC<PlanTableProps> = ({
  tenantId,
  startTime,
  endTime,
  rangeKey,
  topPlans,
  topPlansLoading,
}) => {
  const [planGroupRecord, setPlanGroupRecord] = useState<API.PlanStatGroup | null>(null);
  const [visible, setVisible] = useState(false);

  /** 响应时间的横条图 */
  const renderLine = (record: API.PlanStatDetail, key: string) => {
    const maxValue = max(topPlans.map(plan => plan[key]));
    const rate = (record[key] || 0) / (maxValue as number);

    return (
      <div>
        <div
          style={{
            width: rate * 100,
            backgroundColor: '#5b8ff9',
            height: 10,
            display: 'inline-block',
          }}
        />

        <span style={{ marginLeft: 12 }}>{record[key]}</span>
      </div>
    );
  };

  const columns = [
    {
      title: 'Plan Hash',
      dataIndex: 'planHash',
      render: (node: number, record: API.PlanStatGroup) => {
        return (
          <div>
            <Tooltip placement="topLeft" title={node}>
              {/* Button Width 86 , marginRight 8  */}
              <Typography.Link
                style={{
                  width: record?.hitDiagnosis ? 'calc(100% - 94px)' : '100%',
                  minWidth: 100,
                  marginRight: 8,
                }}
                copyable={{ text: node }}
                ellipsis={true}
                onClick={() => {
                  setPlanGroupRecord(record);
                  setVisible(true);
                }}
              >
                {node}
              </Typography.Link>
            </Tooltip>
            {record?.hitDiagnosis && (
              <Button
                size="small"
                icon={<ExclamationCircleOutlined style={{ color: '#fa8c16' }} />}
              >
                <span style={{ fontSize: 12 }}>
                  {formatMessage({
                    id: 'ocp-express.SQLDiagnosis.Component.RecordTable.RecommendedAttention',
                    defaultMessage: '建议关注',
                  })}
                </span>
              </Button>
            )}
          </div>
        );
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.SQLDiagnosis.Component.RecordTable.ExecutionPlanType',
        defaultMessage: '执行计划类型',
      }),

      dataIndex: 'planType',
      render: (text: string) => findByValue(PLAN_TYPE_LIST, text)?.label,
    },

    {
      title: formatMessage({
        id: 'ocp-express.SQLDiagnosis.Component.RecordTable.PlanGenerationTime',
        defaultMessage: '计划生成时间',
      }),

      dataIndex: 'firstLoadTime',
      render: (text: string) => formatTime(text),
    },

    {
      title: formatMessage({
        id: 'ocp-express.SQLDiagnosis.Component.RecordTable.CpuTimeMs',
        defaultMessage: 'CPU 时间（ms）',
      }),

      dataIndex: 'avgCpuTime',
      render: (text: number, record: API.PlanStatDetail) => renderLine(record, 'avgCpuTime'),
    },
  ];

  return (
    <>
      <Table
        rowKey={record => record.planHash}
        dataSource={topPlans}
        columns={columns}
        loading={topPlansLoading}
        size={isEnglish() ? 'middle' : 'large'}
        pagination={{
          hideOnSinglePage: true,
        }}
      />

      <PlanDrawer
        tenantId={tenantId}
        PlanStatGroup={planGroupRecord}
        startTime={startTime}
        endTime={endTime}
        rangeKey={rangeKey}
        onClose={() => setVisible(false)}
        visible={visible}
      />
    </>
  );
};

export default PlanTable;
