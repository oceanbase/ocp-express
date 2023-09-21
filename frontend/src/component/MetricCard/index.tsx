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

import React, { useState } from 'react';
import { Empty, Spin } from '@oceanbase/design';
import { Modal } from '@oceanbase/design';
import { FullscreenOutlined } from '@oceanbase/icons';
import type { ChartProps } from '@/component/Chart';
import Chart from '@/component/Chart';
import MyCard from '@/component/MyCard';

export interface MetricCardProps {
  loading?: boolean;
  title: React.ReactNode;
  extra?: React.ReactNode;
  range?: React.ReactNode;
  typeButton?: React.ReactNode;
  chartConfig: ChartProps;
}

const MetricCard: React.FC<MetricCardProps> = ({
  loading = false,
  title,
  extra,
  range,
  typeButton,
  chartConfig = { data: [] },
  ...restProps
}) => {
  const [visible, setVisible] = useState(false);
  const { data = [] } = chartConfig || {};
  const realChartConfig = {
    ...chartConfig,
    ...(chartConfig.type === 'Line'
      ? {
        interactions: [
          {
            type: 'brush-x',
          },
        ],
      }
      : {}),
  };

  return (
    <MyCard
      loading={loading}
      title={title}
      extra={
        <span style={{ display: 'flex', alignItems: 'center' }}>
          {extra}
          <FullscreenOutlined onClick={() => setVisible(true)} style={{ cursor: 'pointer' }} />
        </span>
      }
      {...restProps}
    >
      {data?.length > 0 ? (
        <>
          {typeButton}
          <Chart height={186} {...realChartConfig} />
        </>
      ) : (
        <>
          {typeButton}
          <Empty style={{ height: 160 }} imageStyle={{ marginTop: 46 }} />
        </>
      )}
      <Modal
        width={960}
        title={title}
        visible={visible}
        destroyOnClose={true}
        footer={false}
        onCancel={() => setVisible(false)}
      >
        <div>
          {range}
          <Spin spinning={loading}>
            {data?.length > 0 ? (
              <Chart height={300} {...realChartConfig} />
            ) : (
              <Empty style={{ height: 240 }} imageStyle={{ marginTop: 60 }} />
            )}
          </Spin>
        </div>
      </Modal>
    </MyCard>
  );
};

export default MetricCard;
