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

import { formatMessage } from '@/util/intl'; // copy from techui
import type { Moment } from 'moment';
import moment from 'moment';

const DAY_UNIT = 'day';
const WEEK_UNIT = 'week';
const MONTH_UNIT = 'month';
const QUARTER_UNIT = 'quarter';
const YEAR_UNIT = 'year';

export interface RangeOption {
  /**
   * @description 选项名称
   */
  name: string;
  /**
   * @description 时间范围
   */
  range: () => [Moment, Moment];
}

export const NEAR_1_MINUTES: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.NearlyMinute',
    defaultMessage: '近 1 分钟',
  }),
  range: () => [moment().subtract(1, 'minute'), moment()],
};

export const NEAR_5_MINUTES: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.NearlyMinutes',
    defaultMessage: '近 5 分钟',
  }),
  range: () => [moment().subtract(5, 'minute'), moment()],
};

export const NEAR_10_MINUTES: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.NearlyMinutes.1',
    defaultMessage: '近 10 分钟',
  }),
  range: () => [moment().subtract(10, 'minute'), moment()],
};

export const NEAR_20_MINUTES: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.NearlyMinutes.2',
    defaultMessage: '近 20 分钟',
  }),
  range: () => [moment().subtract(20, 'minute'), moment()],
};

export const NEAR_30_MINUTES: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.NearlyMinutes.3',
    defaultMessage: '近 30 分钟',
  }),
  range: () => [moment().subtract(30, 'minute'), moment()],
};

export const NEAR_1_HOURS: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.NearlyHour',
    defaultMessage: '近 1 小时',
  }),
  range: () => [moment().subtract(60, 'minute'), moment()],
};

export const NEAR_3_HOURS: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.NearlyHours',
    defaultMessage: '近 3 小时',
  }),
  range: () => [moment().subtract(3, 'hour'), moment()],
};

export const NEAR_6_HOURS: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.Nearly6Hours',
    defaultMessage: '近 6 小时',
  }),
  range: () => [moment().subtract(6, 'hour'), moment()],
};

export const TODAY: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.Today',
    defaultMessage: '今天',
  }),
  range: () => [moment().startOf(DAY_UNIT), moment().endOf(DAY_UNIT)],
};

export const YESTERDAY: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.Yesterday',
    defaultMessage: '昨天',
  }),
  range: () => [
    moment().startOf(DAY_UNIT).add(-1, DAY_UNIT),
    moment().endOf(DAY_UNIT).add(-1, DAY_UNIT),
  ],
};

export const TOMORROW: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.Tomorrow',
    defaultMessage: '明天',
  }),
  range: () => [
    moment().startOf(DAY_UNIT).add(1, DAY_UNIT),
    moment().endOf(DAY_UNIT).add(1, DAY_UNIT),
  ],
};

export const THIS_WEEK: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.ThisWeek',
    defaultMessage: '本周',
  }),
  range: () => [moment().startOf(WEEK_UNIT), moment().endOf(WEEK_UNIT)],
};

export const LAST_WEEK: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.LastWeek',
    defaultMessage: '上周',
  }),
  range: () => [
    moment().startOf(WEEK_UNIT).add(-1, WEEK_UNIT),
    moment().endOf(WEEK_UNIT).add(-1, WEEK_UNIT),
  ],
};

export const NEXT_WEEK: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.NextWeek',
    defaultMessage: '下周',
  }),
  range: () => [
    moment().startOf(WEEK_UNIT).add(1, WEEK_UNIT),
    moment().endOf(WEEK_UNIT).add(1, WEEK_UNIT),
  ],
};

export const THIS_MONTH: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.ThisMonth',
    defaultMessage: '本月',
  }),
  range: () => [moment().startOf(MONTH_UNIT), moment().endOf(MONTH_UNIT)],
};

export const LAST_MONTH: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.LastMonth',
    defaultMessage: '上月',
  }),
  range: () => [
    moment().startOf(MONTH_UNIT).add(-1, MONTH_UNIT),
    moment().endOf(MONTH_UNIT).add(-1, MONTH_UNIT),
  ],
};

export const NEXT_MONTH: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.NextMonth',
    defaultMessage: '下月',
  }),
  range: () => [
    moment().startOf(MONTH_UNIT).add(1, MONTH_UNIT),
    moment().endOf(MONTH_UNIT).add(1, MONTH_UNIT),
  ],
};

export const THIS_QUARTER: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.ThisQuarter',
    defaultMessage: '本季度',
  }),
  range: () => [moment().startOf(QUARTER_UNIT), moment().endOf(QUARTER_UNIT)],
};

export const LAST_QUARTER: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.LastQuarter',
    defaultMessage: '上季度',
  }),
  range: () => [
    moment().startOf(QUARTER_UNIT).add(-1, QUARTER_UNIT),
    moment().endOf(QUARTER_UNIT).add(-1, QUARTER_UNIT),
  ],
};

export const NEXT_QUARTER: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.NextQuarter',
    defaultMessage: '下季度',
  }),
  range: () => [
    moment().startOf(QUARTER_UNIT).add(1, QUARTER_UNIT),
    moment().endOf(QUARTER_UNIT).add(1, QUARTER_UNIT),
  ],
};

export const THIS_YEAR: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.ThisYear',
    defaultMessage: '今年',
  }),
  range: () => [moment().startOf(YEAR_UNIT), moment().endOf(YEAR_UNIT)],
};

export const LAST_YEAR: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.LastYear',
    defaultMessage: '去年',
  }),
  range: () => [
    moment().startOf(YEAR_UNIT).add(-1, YEAR_UNIT),
    moment().endOf(YEAR_UNIT).add(-1, YEAR_UNIT),
  ],
};

export const NEXT_YEAR: RangeOption = {
  name: formatMessage({
    id: 'ocp-express.component.OCPRangePicker.constant.NextYear',
    defaultMessage: '明年',
  }),
  range: () => [
    moment().startOf(YEAR_UNIT).add(1, YEAR_UNIT),
    moment().endOf(YEAR_UNIT).add(1, YEAR_UNIT),
  ],
};
