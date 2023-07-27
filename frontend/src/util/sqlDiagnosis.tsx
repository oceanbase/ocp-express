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

import { RFC3339_DATE_TIME_FORMAT } from '@/constant/datetime';
import { includesChinese } from '@/util';
import { formatMessage } from '@/util/intl';
import { max, toString } from 'lodash';
import type { Moment } from 'moment';
import moment from 'moment';
import { sortByMoment, sortByNumber } from '@oceanbase/util';
import XLSX from 'xlsx';

export const getRange = type => {
  const now = moment();
  let past: Moment | null = null;

  switch (type) {
    case '5m':
      past = moment().subtract(5, 'minute');
      break;
    case '15m':
      past = moment().subtract(15, 'minute');
      break;
    case '30m':
      past = moment().subtract(30, 'minute');
      break;
    case '1h':
      past = moment().subtract(1, 'hour');
      break;
    case '3h':
      past = moment().subtract(3, 'hour');
      break;
    default:
      break;
  }

  return [
    past ? past.format(RFC3339_DATE_TIME_FORMAT) : past,
    now.format(RFC3339_DATE_TIME_FORMAT),
  ];
};

// 模拟前端表格筛选排序逻辑，返回处理过后的列表
export const getTableSorterWithFilterList = (list, { sorter, filters }) => {
  let result = list;
  if (filters) {
    Object.keys(filters).forEach(key => {
      if (Array.isArray(filters[key])) {
        // filters（{name: ['a','b','c']}）
        // 如果 key 等于 name， showList 就是 ['a','b','c']， 也就是 filter 中选中需要展示的值
        const showList: [] = filters[key];
        // result 在上一次过滤完后的值，在进行新的过滤，过滤逻辑为对应 key 的值是否是筛选时中选择的值
        result = result.filter(item => showList.includes(item[key]));
      }
    });
  }

  if (sorter?.field) {
    const { field, order } = sorter;
    result = result.sort((a, b) => {
      const [p1, p2] = order === 'ascend' ? [a, b] : [b, a];

      // 可疑SQL 中的最后执行时间需要特殊处理
      if (field === 'lastExecutedTime') {
        return sortByMoment(p1, p2, field);
      }

      return sortByNumber(p1, p2, field);
    });
  }

  return result;
};

/**
 * 导出 list 为 xlsx
 * @param list  导出数据
 * @param header 表头 key 的列表
 * @param filename 文件名
 * @param sheetName 表名
 */
export const exportListToXlsx = (
  list: any[],
  header: string[],
  filename = 'sql.xlsx',
  sheetName = 'Sheet1'
) => {
  // 内容最大只能支持 32767 字符，对超过 32767 的字符进行截断处理
  const realList = list.map(item => {
    Object.entries(item).map(([key, value]) => {
      if (value?.length > 32767) {
        item[key] = String(value)?.slice(0, 32767);
      }
    });
    return item;
  });

  // 根据字符长度获取列宽
  const getWidth = (str?: string) => {
    // 如果包含中文，则长度 * 2
    return (str?.length || 0) * (includesChinese(str) ? 2 : 1);
  };
  const widthConfigList = header.map(key => {
    const headerMap = realList[0] || {};
    // 列标题宽度
    const headerWidth = getWidth(toString(headerMap[key]));
    // 列内容最大宽度
    const contentWidth = max(realList.slice(1).map(item => getWidth(item[key])));
    // 取其中宽度更大的
    const width = max([headerWidth, contentWidth]) || 0;
    // 限制宽度不能超出 30
    return {
      wch: width > 30 ? 30 : width,
    };
  });

  const ws = XLSX.utils.json_to_sheet(realList, {
    header,
    // 去掉默认表头，要求传入 list 的第一条记录为表头 key => title 的映射对象
    skipHeader: true,
  });
  ws['!cols'] = widthConfigList;

  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, sheetName);
  XLSX.writeFile(wb, filename);
};

export const getBindIndexContent = ({
  bindOutlineData,
  supportOutline,
}: {
  bindOutlineData?: API.Outline;
  supportOutline: boolean;
}) => {
  const unsupportOutlineText = formatMessage({
    id: 'ocp-express.SQLDiagnosis.util.ThisIndexMayNotTake',
    defaultMessage:
      '绑定索引后，此索引可能不会生效。OB 2.2.70 以下版本，SQL 须由关键字 select、update、replace、delete 或 insert 后跟空格开头，绑定的索引才能生效。',
  });

  const indexName = bindOutlineData?.bindIndex?.indexName;

  let content = supportOutline
    ? formatMessage({
        id: 'ocp-express.SQLDiagnosis.util.AfterAnIndexIsBound',
        defaultMessage: '绑定索引后，可能会使该 SQL 的执行计划发生变更，请谨慎操作',
      })
    : unsupportOutlineText;

  // // 存在绑定的情况
  if (bindOutlineData) {
    const typeMap = {
      INDEX: formatMessage(
        {
          id: 'ocp-express.SQLDiagnosis.util.TheSqlStatementHasBeen',
          defaultMessage: '该 SQL 已经绑定了索引 {indexName}，该操作会替换原绑定内容。',
        },
        { indexName: indexName }
      ),
      PLAN: formatMessage({
        id: 'ocp-express.SQLDiagnosis.util.TheSqlStatementHasBeen',
        defaultMessage: '该 SQL 已经绑定了执行计划，该操作会解绑执行计划且绑定此索引。',
      }),

      CONCURRENT_LIMIT: formatMessage({
        id: 'ocp-express.SQLDiagnosis.util.TheSqlStatementHasSet',
        defaultMessage: '该 SQL 已经设置了限流，该操作会取消限流且绑定此索引。',
      }),
    };

    content = `${typeMap[bindOutlineData?.type as API.OutlineType]}${
      supportOutline ? '' : unsupportOutlineText
    }`;
  }
  return content;
};

export const getBindPlanContent = ({
  bindOutlineData,
  supportOutline,
}: {
  bindOutlineData?: API.Outline;
  supportOutline: boolean;
}) => {
  const unSupportOutlineText = formatMessage({
    id: 'ocp-express.SQLDiagnosis.util.AfterYouBindAnExecution',
    defaultMessage:
      '绑定执行计划后，此执行计划可能不会生效。OB 2.2.70 以下版本，SQL 须由关键字 select、update、replace、delete 或 insert 后跟空格开头，绑定的执行计划才能生效。',
  });

  const indexName = bindOutlineData?.bindIndex?.indexName;

  let content = supportOutline
    ? formatMessage({
        id: 'ocp-express.SQLDiagnosis.util.AfterYouBindAnExecution.1',
        defaultMessage: '绑定执行计划后，可能会使该 SQL 的执行计划发生变更，请谨慎操作',
      })
    : unSupportOutlineText;

  // 存在绑定的情况
  if (bindOutlineData) {
    const typeMap = {
      INDEX: formatMessage(
        {
          id: 'ocp-express.SQLDiagnosis.util.TheSqlStatementHasBeen',
          defaultMessage: '该 SQL 已经绑定了索引 {indexName}，该操作会替换原绑定内容。',
        },
        { indexName: indexName }
      ),
      PLAN: formatMessage({
        id: 'ocp-express.SQLDiagnosis.util.TheSqlStatementHasBeen.1',
        defaultMessage: '该 SQL 已经绑定了执行计划，该操作会解绑原执行计划且绑定此执行计划。',
      }),

      CONCURRENT_LIMIT: formatMessage({
        id: 'ocp-express.SQLDiagnosis.util.TheSqlStatementHasBeen.2',
        defaultMessage: '该 SQL 已经设置了限流，该操作会取消限流且绑定此执行计划。',
      }),
    };
    content = `${typeMap[bindOutlineData?.type]}${supportOutline ? '' : unSupportOutlineText}`;
  }
  return content;
};

// 为该 SQL 设置限流

export const getBindLimitContent = ({
  bindOutlineData,
  supportOutline,
}: {
  bindOutlineData?: API.Outline;
  supportOutline: boolean;
}) => {
  const unSupportOutlineText = formatMessage({
    id: 'ocp-express.SQLDiagnosis.util.ThisExecutionPlanMayNot',
    defaultMessage:
      '设置限流后，此执行计划可能不会生效。OB 2.2.70 以下版本，SQL 须由关键字 select、update、replace、delete 或 insert 后跟空格开头，未设置关键字的限流才能生效。',
  });

  const indexName = bindOutlineData?.bindIndex?.indexName;
  let content = supportOutline ? null : unSupportOutlineText;

  // 存在绑定的情况
  if (bindOutlineData) {
    const typeMap = {
      INDEX: formatMessage(
        {
          id: 'ocp-express.SQLDiagnosis.util.TheSqlStatementIsBound',
          defaultMessage:
            '该 SQL 已经绑定了索引 {indexName}，该操作会解绑执行计划且为该 SQL 设置限流。',
        },
        { indexName: indexName }
      ),
      PLAN: formatMessage({
        id: 'ocp-express.SQLDiagnosis.util.TheSqlStatementHasBeen.3',
        defaultMessage: '该 SQL 已经绑定了执行计划，该操作会解绑执行计划且为该 SQL 设置限流。',
      }),

      CONCURRENT_LIMIT: '',
    };

    content = `${typeMap[bindOutlineData.type as API.OutlineType]}${
      supportOutline ? '' : unSupportOutlineText
    }`;
  }
  return content;
};

// 对正则中的特殊字符进行转义
export const transRegcharacter = (v: string = '') => {
  let result = '';

  v.split('').forEach(str => {
    if (['$', '(', ')', '*', '+', '.', '[', ']', '?', '\\', '^', '{', '}', '|'].includes(str)) {
      result += `\\${str}`;
    } else {
      result += str;
    }
  });

  return result;
};

export type sqlOpreaterItem = {
  field: string;
  operator: '=' | '<>' | '>' | '<' | '>=' | '<=' | 'BETWEEN' | 'LIKE';
  // maxValue 和 minValue 只有 BETWEEN 情况下才会使用
  minValue: string;
  maxValue: string;
  value: string;
};

export const getSqlOperatorList = (sqlText: string = '') => {
  /**
   * @description 匹配 WHERE 子句中除 in 以外的运算符（=,<>,>,<,>=,<=,BETWEEN,LIKE）相对应的语句，正则不区分大小写，用于字符串的 match 方法匹配
   * @example `SET score = 9.0 WHERE Author = 'Tom' AND ISBN > 'N0004' AND ISBN < 'N0007' AND sal between 1500 and 3000;` => ['score = 9.0', "Author = 'Tom'", "ISBN > 'N0004'", "ISBN < 'N0007'", 'sal between 1500 and 3000']
   */
  const sqlRegGlobal =
    /([^\s\()]+)\s?((=)\s?((\d|\".*\"|\'.*\'|\?)+)|(<>)\s?((\d|\".*\"|\'.*\'|\?)+)|(>=)\s?((\d|\".*\"|\'.*\'|\?)+)|(<=)\s?((\d|\".*\"|\'.*\'|\?)+)|(>)\s?((\d|\".*\"|\'.*\'|\?)+)|(<)\s?((\d|\".*\"|\'.*\'|\?)+)|(LIKE)\s?((\d|\".*\"|\'.*\'|\?)+)|(BETWEEN)\s?((\d|\".*\"|\'.*\'|\?)+)\sand\s?((\d|\".*\"|\'.*\'|\?)+))/gi;

  /**
   * @description 分组匹配到单条 WHERE 子句中的 field、operator、value，正则不区分大小写，用于字符串的 match 方法匹配
   * @example 'score = 9.0' => ['score = 9.0', 'score', '=', '9.0']
   * @example 'sal between 1500 and 3000' => ['sal between 1500 and 3000', 'sal', 'betwwen', '150 and 3000', '150', '3000']
   */
  const sqlRegAtom =
    /([^\s\(]+)\s?(=|<>|>=|<=|>|<|BETWEEN|LIKE)\s?(([^;\s\)]+)\sand\s([^;\s\)]+)|[^;\s\)]+)/i;

  const conditions = sqlText.match(sqlRegGlobal);
  const result: sqlOpreaterItem[] = [];
  conditions?.forEach(condition => {
    const syntaxList = condition.match(sqlRegAtom) || [];
    const field = syntaxList[1];
    const operatorStatement = transRegcharacter(syntaxList[0]);

    // 匹配到的子语句之前需要要 where 或者 join ，他们中间不能存在 select 或者 update
    const conditionRegStr = `(WHERE|JOIN)((?!SELECT|UPDATE)(.|\\n))*${operatorStatement}`;
    const conditionReg = new RegExp(conditionRegStr, 'i');

    // 如果不符合条件，这条语句不需要进行保存
    if (!conditionReg.test(sqlText)) {
      return false;
    }

    const operator = syntaxList[2]?.toUpperCase();
    if (operator === 'BETWEEN') {
      const minValue = syntaxList[4];
      const maxValue = syntaxList[5];
      result.push({
        field,
        operator,
        minValue,
        maxValue,
      });
    } else {
      result.push({
        field,
        operator,
        value: syntaxList[3],
      });
    }
  });

  return result;
};

export const getSqlExpressionContent = (item: sqlOpreaterItem) => {
  return item.operator === 'BETWEEN'
    ? `${item.field} ${item.minValue} ${item.operator} ${item.maxValue}`
    : `${item.field} ${item.operator} ${item.value}`;
};

export const getInsertValueSqlText: (
  sqlText: string,
  sqlOperatorList: sqlOpreaterItem[]
) => string = (sqlText, sqlOperatorList) => {
  let realSqlText = sqlText;
  sqlOperatorList.forEach(({ field, operator, minValue, maxValue, value }) => {
    const realFiled = transRegcharacter(field);
    const realOperator = transRegcharacter(operator);
    const regStr =
      operator === 'BETWEEN'
        ? `(${realFiled}\\s?${realOperator}\\s?)\\?(\\n\s+and\\s)\\?`
        : `(${realFiled}\\s?${realOperator}\\s?)\\?`;

    const reg = new RegExp(regStr, 'i');
    if (operator === 'BETWEEN') {
      realSqlText = realSqlText.replace(reg, `$1${minValue?.trim()}$2${maxValue?.trim()}`);
    } else {
      realSqlText = realSqlText.replace(reg, `$1${value?.trim()}`);
    }
  });
  return realSqlText;
};

export const getFiledValueList: (
  sqlOperatorList: sqlOpreaterItem[]
) => sqlOpreaterItem[] = sqlOperatorList => {
  return sqlOperatorList.filter(item =>
    item.operator === 'BETWEEN'
      ? item.minValue !== '?' || item.maxValue !== '?'
      : item.value !== '?'
  );
};
