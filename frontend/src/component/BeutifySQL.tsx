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

import React, { useState, useRef, useEffect, useImperativeHandle } from 'react';
import { Highlight } from '@oceanbase/design';
import { useUpdate } from 'ahooks';
import { reduce } from 'lodash';
import { formatSql } from '@/util';
import { getSqlOperatorList, transRegcharacter } from '@/util/sqlDiagnosis';

type sqlOperator = '=' | '<>' | '>' | '<' | '>=' | '<=' | 'BETWEEN' | 'LIKE';
type sqlOpreaterItem = {
  field: string;
  operator: sqlOperator;
  // maxValue 和 minValue 只有 BETWEEN 情况下才会使用
  minValue: string;
  maxValue: string;
  value: string;
};

type sqlContentErrorType = 'NO_INPUT' | 'ILLEGAL_SPACE' | 'MISS_QUOTATION';

export type sqlContentErrorItem = {
  type: sqlContentErrorType;
} & sqlOpreaterItem;

const OCP_LIMIT_KEY = '__OCP_SQL_DIAGNOSIS_LIMIT_INPUT';

const getMarkerSqlText = (sqlText: string) => {
  const result = getSqlOperatorList(sqlText);
  let realSqlText = sqlText;

  result.forEach(({ field, operator, value, maxValue, minValue }, index) => {
    const realField = transRegcharacter(field);
    const realOperator = transRegcharacter(operator);
    const realMinValue = transRegcharacter(minValue);
    const realMaxValue = transRegcharacter(maxValue);
    const realValue = transRegcharacter(value);

    const regStr =
      operator === 'BETWEEN'
        ? `(${realField}\\s?${realOperator}\\s?)${realMinValue}(\\n\s+and\\s)${realMaxValue}`
        : `(${realField}\\s?${realOperator}\\s?)${realValue}`;

    const reg = new RegExp(regStr, 'i');
    if (operator === 'BETWEEN') {
      realSqlText = realSqlText.replace(
        reg,
        `$1minValue${OCP_LIMIT_KEY}${index}$2maxValue${OCP_LIMIT_KEY}${index}`
      );
    } else {
      realSqlText = realSqlText.replace(reg, `$1value${OCP_LIMIT_KEY}${index}`);
    }
  });

  return realSqlText;
};

const getSqlContentErrorList: (
  sqlOperatorList: sqlOpreaterItem[]
) => sqlContentErrorItem[] = sqlOperatorList => {
  const errorList = [];

  const getContentErrorType: (s: string) => null | sqlContentErrorType = s => {
    // 去除两边的空格
    const content = s?.trim() || '';

    if (/^\'.*\'$/.test(content) || /^\".*\"$/.test(content)) {
      // 包裹了单引号或双引号的内容
      return null;
    } else if (content === '?') {
      return null;
    } else if (content === '') {
      return 'NO_INPUT';
    } else if (content.includes(' ')) {
      return 'ILLEGAL_SPACE';
    } else if (!/^\d+$/.test(content)) {
      // 未包裹双引号且存在除数字以外的字符
      return 'MISS_QUOTATION';
    }

    return null;
  };

  sqlOperatorList.map(({ field, operator, value, minValue, maxValue }) => {
    let errorType = null;
    if (operator === 'BETWEEN') {
      errorType = getContentErrorType(minValue) || getContentErrorType(maxValue);
    } else {
      errorType = getContentErrorType(value);
    }

    if (errorType) {
      errorList.push({
        field,
        value,
        operator,
        minValue,
        maxValue,
        type: errorType,
      });
    }
  });

  return errorList;
};

const errorColor = 'rgba(232, 104, 74, 0.15)';
const successColor = 'rgba(90, 216, 216, 0.15)';

interface BeutifySQLProps {
  sqlText: string;
  onChange?: (list: sqlOpreaterItem[]) => void;
  edit?: boolean;
}

export interface BeutifySQLRef {
  validateFields: (
    cb?: (sqlContentErrorList: sqlContentErrorItem[], sqlOperatorList: sqlOpreaterItem[]) => void
  ) => void;
  setSqlOperatorList: (list: sqlOpreaterItem[]) => void;
}

const BeutifySQL: React.FC<BeutifySQLProps> = React.forwardRef<BeutifySQLRef, BeutifySQLProps>(
  (
    { sqlText, onChange, edit = true },

    ref
  ) => {
    const [highlightSqltext, setHighlightSqltext] = useState('');
    const [sqlOperatorList, setSqlOperatorList] = useState<sqlOpreaterItem[]>([]);
    const update = useUpdate();

    const refCode = useRef();

    // 向组件外部暴露 validateFields 属性函数，可通过 ref 引用
    useImperativeHandle(ref, () => ({
      validateFields: cb => {
        const mySqlContentErrorList = getSqlContentErrorList(sqlOperatorList);
        if (mySqlContentErrorList.length > 0) {
          mySqlContentErrorList.forEach(({ field, operator }) => {
            const e = document.getElementById(field + operator);
            if (e?.parentNode?.style) {
              e.parentNode.style.backgroundColor = errorColor;
            }
          });
        }
        if (cb) {
          cb(mySqlContentErrorList, sqlOperatorList);
        }
      },
      setSqlOperatorList: list => {
        setSqlOperatorList(list);
      },
    }));

    useEffect(() => {
      if (sqlText) {
        const realOperaterList = getSqlOperatorList(sqlText);
        setSqlOperatorList(realOperaterList);

        const realSqlText = getMarkerSqlText(sqlText);
        setHighlightSqltext(realSqlText);
      }
    }, [sqlText]);

    useEffect(() => {
      if (refCode.current) {
        const codeEle = refCode.current?.querySelector('code');
        if (codeEle) {
          // 说明已经替换过 Input 组件
          if (codeEle.querySelectorAll('input').length > 0) {
            return;
          }

          let realHTML = '';
          codeEle?.innerHTML.split('\n').map(rowHtml => {
            realHTML += `<div style="margin-bottom: 4px">${rowHtml}</div>`;
          });

          // ['valueOCP_LIMIT_KEY0', 'valueOCP_LIMIT_KEY1', 'valueOCP_LIMIT_KEY2', 'valueOCP_LIMIT_KEY3', 'valueOCP_LIMIT_KEY4', 'valueOCP_LIMIT_KEY5', 'valueOCP_LIMIT_KEY6', 'valueOCP_LIMIT_KEY7']
          const inputReg = new RegExp(`\\S+${OCP_LIMIT_KEY}[^\\s;<]+`, 'gi');
          const syntaxList = realHTML.match(inputReg);
          syntaxList?.forEach(str => {
            const [valueField, valueIndex] = str.split(OCP_LIMIT_KEY);
            const value = sqlOperatorList[valueIndex][valueField];
            const field = sqlOperatorList[valueIndex].field;
            const operator = sqlOperatorList[valueIndex].operator;
            realHTML = realHTML.replace(
              str,
              `<input
              ${edit ? '' : 'disabled'}
              id="${field + operator}"
              data-field="${valueField}"
              data-index="${valueIndex}"
              class="ant-input"
              value="${value || '?'}"
              style="
                min-width: 4.0em;
                width: 4.0em;
                text-align: center;
                padding: 0;
                line-height: 1.5;
                height: 1.5em;
                display: inline-block;
              "/>`
            );
          });

          codeEle.innerHTML = realHTML;

          const onInputUpdate = event => {
            const l = reduce(
              event.target.value || '',
              (m, c) => m + (c.charCodeAt(0) < 128 ? 0.55 : 1),
              0
            );
            event.target.style.width = l + 0.5 + 'em';
            if (event?.target?.parentNode?.style?.backgroundColor === errorColor) {
              event.target.parentNode.style.backgroundColor = successColor;
            }

            const { field = 'value', index } = event.target.dataset;
            sqlOperatorList[index][field] = event.target.value;

            if (onChange) {
              onChange(sqlOperatorList);
            }

            update();
          };

          codeEle.querySelectorAll('input').forEach(el => {
            if (el.parentNode) {
              el.parentNode.style.backgroundColor = successColor;
            }

            el.addEventListener('input', onInputUpdate);
          });
        }
      }
    }, [refCode.current]);

    return (
      <div ref={refCode}>
        <Highlight style={{ height: 'calc(100% - 80px)' }} language="sql">
          {formatSql(highlightSqltext)}
        </Highlight>
      </div>
    );
  }
);

export default BeutifySQL;
