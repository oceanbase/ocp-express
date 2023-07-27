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
import { getLocale } from 'umi';
import { isEmpty, isNumber, omit, toNumber } from 'lodash';
import { formatNumber, isNullValue } from '@oceanbase/util';

/**
 * 将微秒转换为秒，并最多保留两位小数
 */
export function us2s(value: number): string | number {
  return Math.round((toNumber(value) / 1000 / 1000) * 100) / 100;
}

/**
 * 对对象数组的key进行去重，只支持 value 为简单值
 * @param array 需要去重的对象数组
 * @param key 根据那个key去重
 */
export const deDuplicateOfObjArr = (array: any[], key: string) => {
  if (array instanceof Array) {
    return array?.reduce((res: any[], item: any) => {
      if (!res.includes(item?.[key])) {
        res.push(item?.[key]);
      }
      return res;
    }, []);
  }
  return [];
};

// setDataSource(transFormArray(data, 'ip', ["zone"]));
/**
 * @param arr 源数组
 * @param key 根据指定的Key，进行数据划分，
 * @param needTransformKeys 需要进行转换的 key 值
 */
export const transFormArray = (arr: any[], key: string, needTransformKeys: string[]) => {
  if (!Array.isArray(arr)) {
    return [];
  }

  const snapshotIds = deDuplicateOfObjArr(arr, 'snapshotId').sort((a, b) => a - b);

  const startId = snapshotIds?.[0];
  const endId = snapshotIds?.[1];

  const aggregationArray =
    deDuplicateOfObjArr(arr, key)?.map(item => arr.filter(i => i[key] === item)) || [];

  // aggregationArray 每个元素都是一个非空数组
  return aggregationArray?.map((itemArr: any[]) => {
    if (Array.isArray(itemArr)) {
      const preObj = itemArr?.find(item => item?.snapshotId === startId) || {};
      const nowObj = itemArr?.find(item => item?.snapshotId === endId) || {};

      return needTransformKeys?.reduce((newObj: any, newkey: any) => {
        const oldKey = `${newkey}_start`;
        const nowKey = `${newkey}_end`;
        // 只有开始和结束的一些字段
        // eslint-disable-next-line
        newObj[oldKey] = preObj[newkey];
        // eslint-disable-next-line
        newObj[nowKey] = nowObj[newkey];

        return {
          ...newObj,
          ...omit(!isEmpty(preObj) ? preObj : nowObj, needTransformKeys),
        };
      }, {});
    }
    return null;
  });
};

// transFormAndCompactArray(data, 'snapshotId', ["zone", "snapshotId", "obTenantId", "ip"])
/**
 * 用于节点数据分区信息和租户数据容量信息进行数据源转换改造
 * 和上面不同，这个是根据快照点id进行 开始和结束的划分，以租户维度聚合，保留相同租户-不同zone的数据
 * @param arr 数据源
 * @param key 相同的key
 * @param sameKey 相同的key
 * @param dontNeedTransform 不必进行转换的 key
 */
// data ip
// 拆分成两个数组，需要两个关键字，在A当中找到B, 忽略掉不需要的，组合需要的
export const transformAndCompactArray = (arr: any[], key: string, needTransformKeys: string[]) => {
  const aggregationArray = deDuplicateOfObjArr(arr, key).map(item =>
    arr.filter(i => i[key] === item)
  );

  const start = aggregationArray?.[0] || [];
  const end = aggregationArray?.[1] || [];

  const newarr = start?.map(startItem => {
    const endItem =
      end?.find(item => item?.ip === startItem?.ip && item?.obTenantId === startItem?.obTenantId) ||
      {};
    const preObj = startItem;
    const nowObj = endItem;

    return needTransformKeys?.reduce((newObj: any, newkey: any) => {
      const oldKey = `${newkey}_start`;
      const nowKey = `${newkey}_end`;
      // eslint-disable-next-line
      newObj[oldKey] = preObj[newkey];
      // eslint-disable-next-line
      newObj[nowKey] = nowObj[newkey];
      return {
        ...newObj,
        ...omit(!isEmpty(preObj) ? preObj : nowObj, needTransformKeys),
      };
    }, {});
  });
  return newarr;
};

// xey 的形式 是科学计数法 表示x乘以10的y次幂
// 四舍五入（含小数点）
export const round = (number, precision: number = 2) => {
  if (isNullValue(number)) {
    // 返回 0，方便后续其他数值处理
    return 0;
  }
  return Math.round(`${+number}e${precision}`) / 10 ** precision;
};

// 转成百分比
export const percent = (fenzi: number, fenmu: number, fixedN: number = 2) => {
  if (isNullValue(fenzi) || isNullValue(fenmu)) {
    return '-';
  }
  if (typeof fenzi !== 'number' || typeof fenmu !== 'number') {
    return '-';
  }
  if (fenmu === 0) {
    return '0.00';
  }
  // 1.00 和 0.100 和 1.010 怎么处理？

  // const num = (fenzi / fenmu * 100).toFixed(3).toString().split('.')
  // const int = num[0];
  // const float = num[1];
  // if (int === '0') {
  //   const index = float.split('').reverse().findIndex(item => item !== '0');
  //   //   const  floatArr = .map(item=>{
  //   //   if(item==='0'){
  //   //     break
  //   //   }
  //   // })
  // }

  return round((fenzi / fenmu) * 100, 2).toFixed(fixedN);
};

/**
 *
 * @param data [
 *        {key: 0, age: "18"},
 *        {key: 0, age: "18"},
 *        {key: 0, age: "18"},
 *        {key: 1, age: "20"},
 *        {key: 1, age: "20"}]
 * @param key string
 * @returns [
 *        {key: 0, age: "18",rowSpan: 3},
 *        {key: 0, age: "18",rowSpan: 0},
 *        {key: 0, age: "18",rowSpan: 0},
 *        {key: 1, age: "20",rowSpan: 2},
 *        {key: 1, age: "20",rowSpan: 0}]
 */
// 创建聚合行
export const createAggregateArr = (data: any, key: string, rowSpan: string = 'rowSpan') => {
  // @returns [ 0 , 1 ]
  const uniqueKeyList = data.reduce((result: any, item: any) => {
    // 首先将name字段作为新数组result取出
    if (!result.includes(item[key])) {
      result.push(item[key]);
    }
    return result;
  }, []);

  return uniqueKeyList.reduce((result: any, val: number) => {
    // 将name相同的数据作为新数组取出，并在其内部添加新字段**rowSpan**
    const children = data?.filter((item: any) => item?.[key] === val) || [];
    return result.concat(
      children.map((item: any, index: number) => ({
        ...item,
        // 将数据添加 rowSpan 字段，表明要跨的行数
        ...(rowSpan && {
          [rowSpan]: index === 0 ? children.length : 0,
        }),
      }))
    );
  }, []);
};

// 千分位分割
export const formatterNumber = (value: string | number | undefined) => {
  if (isNullValue(value)) {
    return '-';
  }

  if (value?.toString().includes('.')) {
    return value.toString().replace(/(\d)(?=(\d{3})+\.)/g, '$1,') || '';
  }
  //  $& 表示与正则表达式相匹配的内容
  return value?.toString()?.replace(/\B(?=(\d{3})+(?!\d))/g, ',') || '';
};

export function isEnglish() {
  return getLocale() === 'en-US';
}

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
    formatMessage({ id: 'ocp-performance-report.src.utils.Days', defaultMessage: '天' }),
    formatMessage({ id: 'ocp-performance-report.src.utils.Hours', defaultMessage: '小时' }),
    formatMessage({ id: 'ocp-performance-report.src.utils.Minutes', defaultMessage: '分钟' }),
    formatMessage({ id: 'ocp-performance-report.src.utils.Seconds', defaultMessage: '秒' }),
  ];

  const timeList = [days, hours, minutes, realSeconds];
  const result = timeList.reduce((accumulator, currentValue, index) => {
    /*  eslint-disable */
    if (currentValue) {
      return (accumulator +=
        timeList.length - 1 !== index
          ? `${currentValue}${timeUnitList[index]} `
          : `${currentValue}${timeUnitList[index]}`);
    }

    return accumulator;
  }, '');

  return result;
};

/**
 * 获取报告名
 * @param reportData 报告数据
 * @param obTenantId 租户 ID
 * @returns 租户名或者租户 ID
 */
export const getTenantName = (reportData: API.WorkloadReport, obTenantId: number) => {
  return (
    reportData?.tenantInfoList?.find(tenant => tenant?.obTenantId === toNumber(obTenantId))?.name ||
    obTenantId
  );
};
