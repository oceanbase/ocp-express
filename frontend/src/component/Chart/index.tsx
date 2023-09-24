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
import { isObject } from 'lodash';
import { sortByMoment } from '@oceanbase/util';
import * as Charts from '@oceanbase/charts';
import * as OBCharts from '@oceanbase/charts';

export type ChartType =
  | 'Bar'
  | 'Column'
  | 'Area'
  | 'GroupBar'
  | 'RangeBar'
  | 'GroupedColumn'
  | 'Gauge'
  | 'Line'
  | 'Pie'
  | 'Radar'
  | 'Ring'
  | 'StackedBar'
  | 'StackColumn'
  | 'StackArea'
  | 'TinyArea'
  | 'TinyColumn'
  | 'TinyLine'
  | 'Progress'
  | 'RingProgress'
  | 'DualAxes';

export interface TooltipScrollProps {
  maxHeight: string;
}

export type TooltipScroll = boolean | TooltipScrollProps;

export interface ChartProps {
  type?: ChartType;
  autoFit?: boolean;
  data?: any[];
  percent?: number;
  title?: React.ReactNode;
  description?: React.ReactNode;
  height?: number;
  xField?: string;
  yField?: string;
  colorField?: string;
  seriesField?: string;
  animation?: boolean;
  meta?: Record<
    string,
    {
      alias?: string;
      formatter?: (value: any) => any;
    }
  >;
  xAxis?: any;
  yAxis?: any;
  legend?: any;
  tooltip?: any;
  interactions?: {
    type: string;
  }[];
  // 图表的 tooltip 是否可进入且可滚动，常用于 tooltip 数过多、需要滚动查看的场景
  tooltipScroll?: TooltipScroll;
  style?: React.CSSProperties;
  className?: string;
}

const Chart: React.FC<ChartProps> = ({
  type = 'Line',
  data,
  xField,
  xAxis,
  yAxis,
  tooltip,
  tooltipScroll,
  ...restProps
}) => {
  let newData = data;

  if (type === 'Line' && (xAxis?.type === 'time' || xAxis?.type === 'timeCat')) {
    newData = data?.sort((a, b) => sortByMoment(a, b, xField));
  } else if (type === 'DualAxes' && (xAxis?.type === 'time' || xAxis?.type === 'timeCat')) {
    newData = [
      (data && data[0] && data[0].sort((a, b) => sortByMoment(a, b, xField))) || [],
      (data && data[1] && data[1].sort((a, b) => sortByMoment(a, b, xField))) || [],
    ];
  }
  const config = {
    data: newData,
    padding: 'auto',
    autoFit: true,
    xField,
    xAxis: {
      title: false,
      ...(xAxis?.type === 'time'
        ? {
            nice: false,
          }
        : {}),
      ...xAxis,
    },
    yAxis: {
      title: false,
      ...yAxis,
    },
    tooltip: {
      ...(tooltipScroll
        ? {
            follow: true,
            shared: true,
            enterable: true,
            // 允许鼠标滑入 tooltip 会导致框选很难选中区间，因此加大鼠标和 tooltip 之间的间距，以缓解该问题
            offset: 40,
            domStyles: {
              'g2-tooltip': {
                maxHeight: '164px',
                overflow: 'auto',
                ...(isObject(tooltipScroll) ? (tooltipScroll as TooltipScrollProps) : {}),
              },
            },
          }
        : {}),
      ...tooltip,
    },
    ...restProps,
  };
  const ChartComp = OBCharts[type] || Charts[type];
  return <ChartComp {...config} />;
};

export default Chart;
