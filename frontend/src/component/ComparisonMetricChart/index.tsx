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
import ContentWithQuestion from '@/component/ContentWithQuestion';
import type { CommonProps, MetricGroupWithChartConfig } from './Item';
import Item from './Item';

export interface ComparisonMetricChartProps extends CommonProps {
  metricGroupList: MetricGroupWithChartConfig[];
  cardStyle?: React.CSSProperties;
  titleStyle?: React.CSSProperties;
}

const ComparisonMetricChart: React.FC<ComparisonMetricChartProps> = ({
  metricGroupList,
  titleStyle,
  ...restProps
}) => {
  return (
    <div className="card-without-padding">
      {metricGroupList.map((item, index) => {
        const title = (
          <ContentWithQuestion
            style={titleStyle}
            content={item?.name}
            tooltip={
              item?.description && {
                placement: 'right',
                // 指标组只包含一个指标，则用指标组信息代替指标信息
                title: (
                  <div>
                    {/* 用第一个指标的单位替代指标组的单位作展示 */}
                    {`${item?.description}`}
                  </div>
                ),
              }
            }
          />
        );

        return (
          <Card
            key={item.key}
            title={title}
            hoverable={false}
            style={{ width: '100%', ...(restProps.cardStyle || {}) }}
          >
            <Item index={index} {...restProps} metricGroup={item} />
          </Card>
        );
      })}
    </div>
  );
};

export default ComparisonMetricChart;
