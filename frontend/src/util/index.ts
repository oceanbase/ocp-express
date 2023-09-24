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

import { ALL, BOOLEAN_LIST, DEFAULT_LIST_DATA, SIZE_UNIT_LIST, TIME_UNIT_LIST } from '@/constant';
import type { MICROSECOND_TYPE } from '@/constant/must-ignore';
import { MICROSECOND } from '@/constant/must-ignore';
import { formatMessage } from '@/util/intl';
import { getLocale } from 'umi';
import {
  find,
  isArray,
  isNil,
  isNumber,
  isString,
  max,
  min,
  noop,
  omitBy,
  some,
  toNumber,
} from 'lodash';
import {
  byte2GB,
  byte2KB,
  byte2MB,
  byte2PB,
  byte2TB,
  formatNumber,
  isNullValue,
} from '@oceanbase/util';
import type { WrappedFormUtils } from '@ant-design/compatible/lib/form/Form';
import { useAntdTable } from 'ahooks';
import type {
  AntdTableOptions,
  Data,
  Params,
  AntdTableResult,
} from 'ahooks/lib/useAntdTable/types';
import type { FormInstance } from '@oceanbase/design/es/form';
import sqlFormatter from 'sql-formatter';
import validator from 'validator';

const sortOrderMap = {
  ascend: 'asc',
  descend: 'desc',
};

export const secondToTime = (seconds: number) => {
  if (!isNumber(seconds) || seconds === 0) {
    return '0';
  }

  const muniteUnit = 60;
  const hourUnit = muniteUnit * 60;
  const dayUnit = hourUnit * 24;

  const days = Math.floor(seconds / dayUnit);
  const hours = Math.floor((seconds % dayUnit) / hourUnit);
  const minutes = Math.floor((seconds % hourUnit) / muniteUnit);
  // 秒数最多保留两位小数
  const realSeconds = formatNumber(seconds % muniteUnit);
  const timeUnitList = [
    formatMessage({ id: 'ocp-express.src.util.Days', defaultMessage: '天' }),
    formatMessage({ id: 'ocp-express.src.util.Hours', defaultMessage: '小时' }),
    formatMessage({ id: 'ocp-express.src.util.Minutes', defaultMessage: '分钟' }),
    formatMessage({ id: 'ocp-express.src.util.Seconds', defaultMessage: '秒' }),
  ];

  const timeList = [days, hours, minutes, realSeconds];
  const result = timeList.reduce((accumulator, currentValue, index) => {
    if (currentValue) {
      return (accumulator += `${currentValue}${timeUnitList[index]}`);
    }
    return accumulator;
  }, '');

  return result;
};

export const isURL = (url: any) => {
  // validator.isURL 传入非 string 类型值会抛出错误，中断程序执行
  return isString(url) && validator.isURL(url);
};

export const getParameterValueRange = (
  valueRange: API.ClusterParameterValueRange | API.TenantParameterValueRange
) => {
  // const typeMap = {
  //   STRING: '字符串',
  //   STRING_LIST: '分号分隔的字符串列表',
  //   INT: '整数',
  //   DOUBLE|NUMERIC: '数字',
  //   CAPACITY: '容量大小',
  //   ENUM: '枚举值',
  //   MOMENT: '时间点',
  //   TIME: '时间段',
  // };
  const { allowedValues, minValue, maxValue } = valueRange || {};
  if (allowedValues) {
    return `[${allowedValues.split(',').join('/')}]`;
  }
  if (minValue || maxValue) {
    return `[${minValue || ' '}, ${maxValue || ' '}]`;
  }
  return '-';
};

export const formatterNumber = (value: string | number | undefined) => {
  if (value?.toString().includes('.')) {
    return value.toString().replace(/(\d)(?=(\d{3})+\.)/g, '$1,') || '';
  }
  //  $& 表示与正则表达式相匹配的内容
  return value?.toString()?.replace(/\B(?=(\d{3})+(?!\d))/g, ',') || '';
};

export const defaultAsyncFnOfGetTableData = () => {
  const promise = new Promise(resolve => {
    resolve({
      tableProps: {
        dataSource: [],
        loading: false,
        pagination: {
          total: 0,
          current: 1,
          pageSize: 10,
        },
      },

      run: noop,
      refresh: noop,
      cancel: noop,
      search: {
        type: 'simple',
        changeType: noop,
        submit: noop,
        reset: noop,
      },
    });
  });
  promise.then(data => {
    return data;
  });
  return promise;
};

interface OCPListData<T> {
  data?: { contents?: T[]; page?: { totalElements?: number } };
}

export function getTableData<P, Item>({
  fn,
  params = {},
  condition = [],
  deps = [],
  options = {},
}: {
  fn: (params: Omit<P, 'page' | 'size' | 'sort'>) => Promise<OCPListData<Item>>;
  params: P;
  condition?: unknown[];
  deps?: (number | string | boolean | undefined)[];
  options?: AntdTableOptions<Data, Params>;
}) {
  let result!: AntdTableResult<Data, Params>;
  const newOptions = {
    refreshDeps: deps,
    ...options,
  };

  if (some(condition, item => isNullValue(item))) {
    // eslint-disable-next-line
    result = useAntdTable(defaultAsyncFnOfGetTableData, newOptions);
  } else {
    // eslint-disable-next-line
    result = useAntdTable<Data, Params>(
      async ({ current, pageSize, sorter = {}, filters = {} }) => {
        const newFilters = {};
        Object.keys(filters).forEach(key => {
          newFilters[key] = filters[key] && filters[key].join(',');
        });

        // 对列表查询参数进行处理
        const newParams = omitBy(
          {
            page: current,
            size: pageSize,
            // 如果设置了 columnKey ，排序默认使用 columnKey，用于兼容后端的使用的排序字段和返回给前端的模型不相同的情况
            sort: sorter.order
              ? `${sorter.columnKey || sorter.field},${sortOrderMap[sorter.order]}`
              : null,
            ...newFilters,
            ...params,
          },

          value => isNullValue(value) || value === '' // 将空值剔除
        );
        const format = (res: OCPListData<Item>) => {
          const { data } = res || {};
          // 接口请求出错时，后端返回的 res.data 为 undefined。避免前端解析错误导致页面崩溃，这里需要做健壮性处理
          const { page: { totalElements = 0 } = {}, contents = [] } = data || DEFAULT_LIST_DATA;
          return {
            total: totalElements,
            list: contents,
          };
        };

        const response = await fn(newParams);
        return format(response);
      },
      newOptions
    );

    result.tableProps.pagination.showSizeChanger = true;
    result.tableProps.pagination.showTotal = total =>
      formatMessage(
        {
          id: 'ocp-express.src.util.TotalTotal',
          defaultMessage: '共 {total} 条',
        },

        { total }
      );
  }

  return result;
}

export function validateEmail(rule, value, callback) {
  if (value && !validator.isEmail(value)) {
    callback(
      formatMessage({
        id: 'ocp-express.src.util.InvalidEmailAddress',
        defaultMessage: '邮箱地址不合法',
      })
    );
  }
  callback();
}

export function validatePassword(passed) {
  return (rule, value, callback) => {
    if (value && !passed) {
      callback(
        formatMessage({
          id: 'ocp-express.src.util.ThePasswordDoesNotMeet',
          defaultMessage: '密码设置不符合要求',
        })
      );
    }
    callback();
  };
}

export function validateMobile(rule, value, callback) {
  if (value && !validator.isMobilePhone(value)) {
    callback(
      formatMessage({
        id: 'ocp-express.src.util.InvalidMobilePhoneNumber',
        defaultMessage: '手机号码不合法',
      })
    );
  }
  callback();
}

export function validateIpv4(rule, value, callback) {
  if (value && !validator.isIP(value, '4')) {
    callback(
      formatMessage({
        id: 'ocp-express.src.util.InvalidIpAddress',
        defaultMessage: 'IP 地址不合法',
      })
    );
  }
  callback();
}

export function validateURL(rule, value, callback) {
  if (value && !validator.isURL(value)) {
    callback(
      formatMessage({ id: 'ocp-express.src.util.TheUrlIsInvalid', defaultMessage: 'URL 不合法' })
    );
  }
  callback();
}

export function validateDomain(rule, value, callback) {
  if (value && !validator.isFQDN(value)) {
    callback(
      formatMessage({
        id: 'ocp-express.src.util.TheDomainNameIsInvalid',
        defaultMessage: '域名不合法',
      })
    );
  }
  callback();
}

// 判断是否为 ipv4 地址，支持 * 通配符
export function isIpv4WithWildCard(str: string) {
  const ipv4Regx =
    /^((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|\*)\.){3}(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|\*)$/;
  return ipv4Regx.test(str);
}

// 判断是否为 ipv4 租户白名单地址，需要支持 % 和 _ 通配符
export function isWhitelistWithWildCard(str: string) {
  const ipv4Regx =
    /^((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|%|_)\.){3}(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])|%|_)$/;
  return ipv4Regx.test(str);
}

export function getBooleanLabel(value: boolean) {
  return (find(BOOLEAN_LIST, item => item.value === value) || {}).label;
}

export function hasDefaultLabel(value: any) {
  return isNullValue(value) ? '-' : value;
}

/*
 `全部` 选项与其他选项是冲突的
 1. 选了 `全部` 选项，则去除其他选项
 2. 选了其他选项，则去除 `全部` 选项
 */
export function handleMultipleSelectChangeWithAll(
  form: WrappedFormUtils | FormInstance,
  fieldName: string,
  value: string[],
  allValue = ALL
) {
  setTimeout(() => {
    const { setFieldsValue } = form;
    const selectedValue = value && value[value.length - 1];
    if (selectedValue === allValue) {
      setFieldsValue({
        [fieldName]: [allValue],
      });
    } else {
      setFieldsValue({
        [fieldName]: value.filter(item => item !== allValue),
      });
    }
  }, 0);
}

export function getFirstItem(arr, target = '') {
  if (target) {
    return arr && isArray(arr) && arr.length > 0 && arr[0] && arr[0][target];
  }
  return arr && isArray(arr) && arr.length > 0 && arr[0];
}

export function formatSql(sql: string | null | undefined) {
  // 字符长度超过 20000 的 SQL 语句不会进行格式化展示，避免格式化时浏览器卡死
  // 应该是格式化逻辑会占据浏览器的主线程，影响交互响应，导致页面卡顿
  // 20000 字符的限制是经过实际测试得到的，超过 20000 字符，卡顿效果比较明显，会影响用户使用
  return sql && sql.length <= 20000
    ? sqlFormatter.format(sql || '', {
      // 使用 PL/SQL 格式对 sql 语句进行格式化
      language: 'pl/sql',
    })
    : sql || '';
}

// 为了避免解析错误格式的 json 字符串时页面奔溃，需要使用错误处理
export function allToNull(value) {
  return value === 'all' ? null : value;
}

export function getMetricTitle(metricItem) {
  const { metric, metricCn, unit } = metricItem || {};
  const locale = getLocale();
  return unit === 'ms'
    ? `${locale === 'zh-CN' ? metricCn : metric}(${unit})`
    : `${locale === 'zh-CN' ? metricCn : metric}`;
}

export function getTextLengthRule(minLength: number = 0, maxLength: number = 0) {
  return {
    min: minLength,
    max: maxLength,
    message:
      minLength && maxLength
        ? formatMessage(
          {
            id: 'ocp-express.src.util.TheLengthIsMinlengthMaxlength',
            defaultMessage: '长度为 {minLength} ~ {maxLength}',
          },

          { minLength, maxLength }
        )
        : formatMessage(
          {
            id: 'ocp-express.src.util.TheLengthCannotExceedMaxlength',
            defaultMessage: '长度不能超过 {maxLength}',
          },

          { maxLength }
        ),
  };
}

export function formatArrayResult<T>(data?: { data?: { contents?: T[] } }) {
  const ret = data?.data?.contents;
  if (!ret) return [];
  return ret;
}

export function formatTextResult(data?: { data?: { fulltext?: string } }) {
  const ret = data?.data?.fulltext;
  if (!ret) return '';
  return ret;
}

export function formatNormalResult<T>(data?: { data?: T }) {
  return data?.data;
}

export function isEnglish() {
  return getLocale() === 'en-US';
}

export function isZhCN() {
  return getLocale() === 'zh-CN';
}

export function showTotal(total: number) {
  return formatMessage(
    {
      id: 'ocp-express.src.util.TotalTotal',
      defaultMessage: '共 {total} 条',
    },

    { total }
  );
}

// size 自适应: 需要传入 byte 值
export function formatSize(value: number | undefined, withUnit = true): string | number {
  if (isNullValue(value)) {
    return '-';
  }
  const realValue = value as number;
  const pb = byte2PB(realValue);
  const tb = byte2TB(realValue);
  const gb = byte2GB(realValue);
  const mb = byte2MB(realValue);
  const kb = byte2KB(realValue);

  if (pb >= 1) {
    return withUnit ? `${pb} PB` : pb;
  }
  if (tb >= 1) {
    return withUnit ? `${tb} TB` : tb;
  }
  if (gb >= 1) {
    return withUnit ? `${gb} GB` : gb;
  }
  if (mb >= 1) {
    return withUnit ? `${mb} MB` : mb;
  }
  if (kb >= 1) {
    return withUnit ? `${kb} KB` : kb;
  }
  return withUnit ? `${realValue} B` : realValue;
}

type SizeUnit = 'byte' | 'KB' | 'MB' | 'GB' | 'TB' | 'PB' | undefined;

/**
 * 将存储单位统一转换为 byte
 */
export function formatSizeUnit(value: number, unit: SizeUnit) {
  if (unit === 'byte') {
    return value;
  }
  if (unit === 'KB') {
    return value * 1024;
  }
  if (unit === 'MB') {
    return value * 1024 * 1024;
  }
  if (unit === 'GB') {
    return value * 1024 * 1024 * 1024;
  }

  if (unit === 'TB') {
    return value * 1024 * 1024 * 1024 * 1024;
  }
  if (unit === 'PB') {
    return value * 1024 * 1024 * 1024 * 1024 * 1024;
  }

  return value;
}

/**
 * 为了同时保证最小值和最大值的展示效果，需要综合判断
 * 如果最大与最小值的倍数差 < 10^3，则保证最小值的整数化展示
 * 如果最大与最小值的倍数差 >= 10^3，则保证最大值的整数化展示
 * */
export function formatSizeForChart(
  data: Global.ChartData,
  value: number,
  unit: SizeUnit = 'byte',
  valueField = 'value'
) {
  // 取最小值的格式化逻辑，作为整体的 format
  const minValue =
    min(
      // 去掉 0 值，避免干扰
      data.filter(item => (item[valueField] || 0) > 0).map(item => item[valueField])
    ) || 0;
  const maxValue =
    max(
      // 去掉 0 值，避免干扰
      data.filter(item => (item[valueField] || 0) > 0).map(item => item[valueField])
    ) || 0;

  const realValue = formatSizeUnit(value, unit);
  const realMinValue = formatSizeUnit(minValue, unit);
  const realMaxValue = formatSizeUnit(maxValue, unit);

  const minPB = byte2PB(realMinValue);
  const minTB = byte2TB(realMinValue);
  const minGB = byte2GB(realMinValue);
  const minMB = byte2MB(realMinValue);
  const minKB = byte2KB(realMinValue);

  const maxTB = byte2TB(realMaxValue);
  const maxGB = byte2GB(realMaxValue);
  const maxMB = byte2MB(realMaxValue);
  const maxKB = byte2KB(realMaxValue);

  if (minPB >= 1) {
    return `${byte2PB(realValue)}P`;
  }
  if (minTB >= 1) {
    if (maxTB >= 1000) {
      return `${byte2PB(realValue)}P`;
    }
    return `${byte2TB(realValue)}T`;
  }
  if (minGB >= 1) {
    if (maxGB >= 1000) {
      return `${byte2TB(realValue)}T`;
    }
    return `${byte2GB(realValue)}G`;
  }
  if (minMB >= 1) {
    if (maxMB >= 1000) {
      return `${byte2GB(realValue)}G`;
    }
    return `${byte2MB(realValue)}M`;
  }
  if (minKB >= 1) {
    if (maxKB >= 1000) {
      return `${byte2MB(realValue)}M`;
    }
    return `${byte2KB(realValue)}K`;
  }
  if (realMinValue >= 1) {
    if (realMaxValue >= 1000) {
      return `${byte2KB(realValue)}K`;
    }
    return `${realValue}B`;
  }
  // 对 0 值做单独处理，以避免 0B 被用户误以为是 OB
  // 涉及到 size 的监控指标都是以 B (Byte 字节) 为单位
  return realValue === 0 ? 0 : `${realValue}B`;
}

type TimeUnit = MICROSECOND_TYPE | 'ms' | 's' | 'min' | undefined;

/**
 * 将时间单位统一转换为 μs
 * 后端定义 μs 为微秒的单位
 */
function formatTimeUnit(value: number, unit: TimeUnit = MICROSECOND): number {
  if (unit === MICROSECOND) {
    return value;
  }
  if (unit === 'ms') {
    return value * 1000;
  }
  if (unit === 's') {
    return value * 1000 * 1000;
  }
  if (unit === 'min') {
    return value * 60 * 1000 * 1000;
  }
  return value;
}

/**
 * 将微秒转换为分钟，并最多保留两位小数
 */
function us2Min(value: number): string | number {
  return Math.round((toNumber(value) / 60 / 1000 / 1000) * 100) / 100;
}

/**
 * 将微秒转换为秒，并最多保留两位小数
 */
export function us2s(value: number): string | number {
  return Math.round((toNumber(value) / 1000 / 1000) * 100) / 100;
}

/**
 * 将微秒转换为毫秒，并最多保留两位小数
 */
export function us2ms(value: number): string | number {
  return Math.round((toNumber(value) / 1000) * 100) / 100;
}

/**
 * 为了同时保证最小值和最大值的展示效果，需要综合判断
 * 如果最大与最小值的倍数差 < 10^3 或 60 (取决于不同时间单位的转换逻辑)，则保证最小值的整数化展示
 * 如果最大与最小值的倍数差 >= 10^3 或 60 (取决于不同时间单位的转换逻辑)，则保证最大值的整数化展示
 * */
export function formatTimeForChart(
  data: Global.ChartData,
  value: number,
  unit: TimeUnit = MICROSECOND,
  valueField = 'value'
) {
  // 取最小值的格式化逻辑，作为整体的 format
  const minValue =
    min(
      // 去掉 0 值，避免干扰
      data.filter(item => (item[valueField] || 0) > 0).map(item => item[valueField])
    ) || 0;
  const maxValue =
    max(
      // 去掉 0 值，避免干扰
      data.filter(item => (item[valueField] || 0) > 0).map(item => item[valueField])
    ) || 0;

  // 将单位统一转换为 μs
  const realValue = formatTimeUnit(value, unit);
  const realMinValue = formatTimeUnit(minValue, unit);
  const realMaxValue = formatTimeUnit(maxValue, unit);

  const minMin = us2Min(realMinValue);
  const minS = us2s(realMinValue);
  const minMs = us2ms(realMinValue);

  const maxS = us2s(realMaxValue);
  const maxMs = us2ms(realMaxValue);

  if (minMin >= 1) {
    return `${us2Min(realValue)}min`;
  }
  if (minS >= 1) {
    if (maxS >= 60) {
      return `${us2Min(realValue)}min`;
    }
    return `${us2s(realValue)}s`;
  }
  if (minMs >= 1) {
    if (maxMs >= 1000) {
      return `${us2s(realValue)}s`;
    }
    return `${us2ms(realValue)}ms`;
  }
  if (realMinValue >= 1) {
    if (realMaxValue >= 1000) {
      return `${us2ms(realValue)}ms`;
    }
    return `${realValue}${MICROSECOND}`;
  }
  return `${realValue}${MICROSECOND}`;
}

export function formatValueForChart(
  data: Global.ChartData,
  value: number,
  unit: string,
  valueField = 'value'
) {
  if (TIME_UNIT_LIST.includes(unit)) {
    return formatTimeForChart(data, value, unit, valueField);
  }

  if (SIZE_UNIT_LIST.includes(unit)) {
    return formatSizeForChart(data, value, unit, valueField);
  }

  // 单位为 times/s 时不展示
  if (unit && unit !== 'times/s') {
    // 固定小数位数为 2 位，方便对比
    return `${toNumber((value || 0).toFixed(2))}${unit}`;
  }

  return toNumber((value || 0).toFixed(2));
}

/* 获取弹出层的挂载点，常用于滚动容器不是 body 的场景 */
export function getPopupContainer(triggerNode: HTMLElement) {
  if (triggerNode && triggerNode.parentElement) {
    return triggerNode.parentElement;
  }
  return document.body;
}

/**
 * 判断字符串是否包含中文，包括汉字和中文符号
 * @param{string} str 字符串
 * @return{boolean}
 */
export function includesChinese(str?: string) {
  const regex = /[\u4e00-\u9fa5|\ufe30-\uffa0]/g;
  return !!regex.exec(str || '');
}

/**
 * 字符串分割，仅对第一个分隔符进行分隔
 * @param{string} str 字符串
 * @param{string} seperator 分隔符
 * @return{array}
 */
export function splitFirst(str: string | undefined | null, seperator: string) {
  if (isNil(str)) {
    return str;
  }
  const firstIndex = str.indexOf(seperator);
  return firstIndex === -1 ? [str] : [str.slice(0, firstIndex), str?.slice(firstIndex + 1)];
}

/**
 * 字符串分割，仅对最后个分隔符进行分隔
 * @param{string} str 字符串
 * @param{string} seperator 分隔符
 * @return{array}
 */
export function splitLast(str: string | undefined | null, seperator: string) {
  if (isNil(str)) {
    return str;
  }
  const lastIndex = str.lastIndexOf(seperator);
  return lastIndex === -1 ? [str] : [str.slice(0, lastIndex), str?.slice(lastIndex + 1)];
}

/**
 * 将秒转换为分钟
 */
export function second2Min(value: number = 0, decimal: number = 2): number {
  return formatNumber(toNumber(value) / 60, decimal);
}

/**
 * 将秒转换为小时
 */
export function second2Hour(value: number = 0, decimal: number = 2): number {
  return formatNumber(toNumber(value) / 60 / 60, decimal);
}

/**
 * 将秒转换为天
 */
export function second2Day(value: number = 0, decimal: number = 2): number {
  return formatNumber(toNumber(value) / 60 / 60 / 24, decimal);
}

// duration 自适应: 需要传入秒值
export function formatDuration(
  value?: number,
  // 默认最多保留 2 位小数
  decimal = 2
): {
  value?: number;
  unit?: 's' | 'min' | 'hour' | 'day';
  unitLabel?: '秒' | '分钟' | '小时' | '天';
} {
  if (isNullValue(value)) {
    return {};
  }
  const minute = second2Min(value, decimal);
  const hour = second2Hour(value, decimal);
  const day = second2Day(value, decimal);

  if (day >= 1) {
    return {
      value: day,
      unit: 'day',
      unitLabel: formatMessage({ id: 'ocp-express.src.util.Days', defaultMessage: '天' }),
    };
  }
  if (hour >= 1) {
    return {
      value: hour,
      unit: 'hour',
      unitLabel: formatMessage({ id: 'ocp-express.src.util.Hours', defaultMessage: '小时' }),
    };
  }
  if (minute >= 1) {
    return {
      value: minute,
      unit: 'min',
      unitLabel: formatMessage({ id: 'ocp-express.src.util.Minutes', defaultMessage: '分钟' }),
    };
  }
  return {
    value,
    unit: 's',
    unitLabel: formatMessage({ id: 'ocp-express.src.util.Seconds', defaultMessage: '秒' }),
  };
}
