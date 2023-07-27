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
import MetricTopChartItem from '@/component/MetricTopChart/Item';
import type { CommonProps, MetricGroupWithChartConfig } from './Item';
import Item from './Item';

export interface MetricChartProps extends CommonProps {
  metricGroupList: MetricGroupWithChartConfig[];
  cardStyle: React.CSSProperties;
}

const MetricChart: React.FC<MetricChartProps> = ({ metricGroupList, ...restProps }) => {
  return (
    <Card bordered={false}>
      {metricGroupList.map((item, index) => {
        let padding = '0px 12px';
        if (index === 0 || index === 1) {
          padding = '12px 12px 0px 12px';
        }
        if (
          metricGroupList.length % 2 === 0 &&
          (index === metricGroupList.length - 1 || index === metricGroupList.length - 2)
        ) {
          padding = '0px 12px 12px 12px';
        }
        if (metricGroupList.length % 2 === 1 && index === metricGroupList.length - 1) {
          padding = '0px 12px 12px 12px';
        }
        return (
          <Card.Grid
            key={item.key}
            hoverable={false}
            style={{
              width: '50%',
              boxShadow: 'none',
              padding,
              ...(restProps.cardStyle || {}),
            }}
          >
            {item.withLabel ? (
              <MetricTopChartItem index={index} {...restProps} metricGroup={item} />
            ) : (
              <Item index={index} {...restProps} metricGroup={item} />
            )}
          </Card.Grid>
        );
      })}
    </Card>
  );
};

export default MetricChart;
