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

import React from 'react';
import { Card } from '@oceanbase/design';
import type { CommonProps, MetricGroupWithChartConfig } from './Item';
import Item from './Item';

export interface MetricTopChartProps extends CommonProps {
  metricGroupList: MetricGroupWithChartConfig[];
  cardStyle: React.CSSProperties;
}

const MetricTopChart: React.FC<MetricTopChartProps> = ({
  metricGroupList,
  cardStyle = {},
  ...restProps
}) => {
  return (
    <Card bordered={false}>
      {metricGroupList.map(item => (
        <Card.Grid key={item.key} hoverable={false} style={{ width: '50%', ...cardStyle }}>
          <Item {...restProps} metricGroup={item} />
        </Card.Grid>
      ))}
    </Card>
  );
};

export default MetricTopChart;
