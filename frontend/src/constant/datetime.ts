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

import moment from 'moment';

const localeData = moment.localeData();

export const FOREVER_TIME = '2099-12-31T00:00:00.000Z';

/* 年 */
export const YEAR_FORMAT = 'YYYY';

export const YEAR_FORMAT_DISPLAY = localeData.longDateFormat('year');

/* 月 */
export const MONTH_FORMAT = 'YYYY-MM';

export const MONTH_FORMAT_DISPLAY = localeData.longDateFormat('month');

/* 日期 */
export const DATE_FORMAT = 'YYYY-MM-DD';

export const DATE_FORMAT_DISPLAY = localeData.longDateFormat('date');

export const DATE_FORMAT_WITHOUT_YEAR_DISPLAY = localeData.longDateFormat('dateWithoutYear');

/* 日期 + 时间 */

// RFC3339 的日期时间格式
export const RFC3339_DATE_TIME_FORMAT = 'YYYY-MM-DDTHH:mm:ssZ';

// 日期时间格式
export const DATE_TIME_FORMAT = 'YYYY-MM-DD HH:mm:ss';

// 没有秒数据的日期时间格式
export const DATE_TIME_FORMAT_WITHOUT_SECOND = 'YYYY-MM-DD HH:mm';

export const DATE_TIME_FORMAT_DISPLAY = localeData.longDateFormat('datetime');

export const DATE_TIME_FORMAT_WITH_SSS_DISPLAY = localeData.longDateFormat('datetimeWithSSS');

export const DATE_TIME_FORMAT_WITHOUT_SECOND_DISPLAY =
  localeData.longDateFormat('datetimeWithoutSecond');

export const DATE_TIME_FORMAT_WITHOUT_YEAR_AND_SECOND_DISPLAY = localeData.longDateFormat(
  'datetimeWithoutYearAndSecond'
);

/* 时间 */

// RFC3339 的时间格式
export const RFC3339_TIME_FORMAT = 'HH:mm:ssZ';

// 时间格式
export const TIME_FORMAT = 'HH:mm:ss';

// 带毫秒的时间格式
export const TIME_FORMAT_WITH_SSS = 'HH:mm:ss.SSS';

// 不带秒信息的时间格式
export const TIME_FORMAT_WITHOUT_SECOND = 'HH:mm';
