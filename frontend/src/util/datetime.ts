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

import type { Moment } from 'moment';
import moment from 'moment';
import { range, toNumber } from 'lodash';
import { formatTime as formatTimeFromOBUtil, isNullValue } from '@oceanbase/util';
import { DATE_TIME_FORMAT_DISPLAY } from '@/constant/datetime';

export function formatTime(
  value: number | string | undefined,
  format: string = DATE_TIME_FORMAT_DISPLAY
) {
  return isNullValue(value) ? '-' : formatTimeFromOBUtil(value, format);
}

/**
 * 日期时间格式化: 带微秒值
 * 其中 value 是符合 RFC3339 规格格式的日期时间字符串
 * 示例: 2021-04-06T21:42:11.966709+08:00、2021-04-08T14:28:03.80652+08:00、2021-04-09T00:00:00+08:00
 * TODO: 加单测
 * */
export function formatTimeWithMicroseconds(
  value: string | undefined,
  format: string = DATE_TIME_FORMAT_DISPLAY
): string {
  // 微秒值的范围: 1 ~ 999999，长度为 1 ~ 6 位；支持 Z 零时区
  const regex = /^(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2}:\d{2}).(\d{1,6})(Z|(([+-])(\d{2}:\d{2})))$/;
  const match = regex.exec(value || '');
  // 微秒值为 0 时，后端不会返回微秒信息，此时匹配值为 null，需要使用空字符串兜底，否则 padEnd 后的字符串展示结果为 undefined
  const microseconds = (match && match[3]) || '';
  // padEnd 向后补 0
  return `${formatTimeFromOBUtil(value, format)}.${microseconds?.padEnd(6, '0')}`;
}

/**
 * 根据日期时间字符串，获取对应的微秒值
 * 其中 value 是符合 RFC3339 规格格式的日期时间字符串
 * */
export function getMicroseconds(value?: string) {
  // 微秒值的范围: 1 ~ 999999，长度为 1 ~ 6 位；支持 Z 零时区
  const regex = /^(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2}:\d{2}).(\d{1,6})(Z|(([+-])(\d{2}:\d{2})))$/;
  const match = regex.exec(value || '');
  // 微秒值为 0 时，后端不会返回微秒信息，此时匹配值为 null，需要使用空字符串兜底，否则 padEnd 后的字符串展示结果为 undefined
  const microseconds = (match && match[3]) || '';
  // padEnd 向后补 0
  return toNumber(microseconds.padEnd(6, '0'));
}

/**
 * 返回自 Unix 纪元以来的微秒数
 * 其中 value 是符合 RFC3339 规格格式的日期时间字符串
 * */
export function valueOfMicroseconds(value?: string) {
  return moment(value).unix() * 1000 * 1000 + getMicroseconds(value);
}

/**
 * 计算两个时间的微秒差值
 * 其中 value1 和 value2 是符合 RFC3339 规格格式的日期时间字符串
 * */
export function diffWithMicroseconds(value1?: string, value2?: string) {
  return valueOfMicroseconds(value1) - valueOfMicroseconds(value2);
}

export function disabledDate(current: Moment) {
  return current.isBefore(moment().format('YYYY-MM-DD'));
}

export function disabledTime(date) {
  const hours = (date && date.hours()) || 0;
  const minutes = (date && date.minutes()) || 0;
  const current = moment();
  const currentHours = current.hours();
  const currentMinutes = current.minutes();
  const currentSeconds = current.seconds();
  if (date && date.format('YYYY-MM-DD') === current.format('YYYY-MM-DD')) {
    return {
      disabledHours: () => range(0, currentHours),
      disabledMinutes: () => (hours === currentHours ? range(0, currentMinutes) : []),
      disabledSeconds: () =>
        hours === currentHours && minutes === currentMinutes ? range(0, currentSeconds) : [],
    };
  }
  return {
    disabledHours: () => [],
    disabledMinutes: () => [],
    disabledSeconds: () => [],
  };
}
