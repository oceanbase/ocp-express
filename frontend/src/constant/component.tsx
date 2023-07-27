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
import { Tooltip, Input, InputNumber, Select } from '@oceanbase/design';
import { formatMessage } from '@/util/intl';
import { toNumber } from 'lodash';
import { isNullValue } from '@oceanbase/util';

const { Option } = Select;

/* 由于该验证信息会在 constant 中用到，因此需要使用自定义的国际化函数去处理 */
export function getNameValidateMessage() {
  return (
    <span>
      <Tooltip
        placement="topLeft"
        title={formatMessage({
          id: 'ocp-express.src.util.component.ItCanStartWithAnForName',
          defaultMessage:
            '以英文字母开头、英文或数字结尾，可包含英文、数字和下划线，且长度为 2 ~ 32',
        })}
        overlayClassName="tooltip-small"
      >
        <a>
          {formatMessage({
            id: 'ocp-express.src.util.component.NameFormat',
            defaultMessage: '名称格式',
          })}
        </a>
      </Tooltip>
      {formatMessage({ id: 'ocp-express.src.util.component.Incorrect', defaultMessage: '不正确' })}
    </span>
  );
}

/* 由于该验证信息会在 constant 中用到，因此需要使用自定义的国际化函数去处理 */
export function getChineseNameValidateMessage() {
  return (
    <span>
      <Tooltip
        placement="topLeft"
        title={formatMessage({
          id: 'ocp-express.src.constant.component.ItCanContainLettersDigits',
          defaultMessage: '可包含中文、英文、数字、下划线、中横线，且长度为 2 ~ 32',
        })}
        overlayClassName="tooltip-small"
      >
        <a>
          {formatMessage({
            id: 'ocp-express.src.constant.component.NameFormat',
            defaultMessage: '名称格式',
          })}
        </a>
      </Tooltip>
      {formatMessage({
        id: 'ocp-express.src.constant.component.Incorrect',
        defaultMessage: '不正确',
      })}
    </span>
  );
}

/* 由于该验证信息会在 constant 中用到，因此需要使用自定义的国际化函数去处理 */
export function getUsernameValidateMessage() {
  return (
    <span>
      <Tooltip
        placement="topLeft"
        title={formatMessage({
          id: 'ocp-express.src.util.component.ItCanStartWithAnForUsername',
          defaultMessage:
            '以英文字母开头、英文或数字结尾，可包含英文、数字、点号、中划线和下划线，且长度为 4 ~ 48',
        })}
        overlayClassName="tooltip-small"
      >
        <a>
          {formatMessage({
            id: 'ocp-express.src.util.component.NameFormat',
            defaultMessage: '名称格式',
          })}
        </a>
      </Tooltip>
      {formatMessage({ id: 'ocp-express.src.util.component.Incorrect', defaultMessage: '不正确' })}
    </span>
  );
}

export function getDatabaseNameValidateMessage() {
  return (
    <span>
      <Tooltip
        placement="topLeft"
        title={formatMessage({
          id: 'ocp-express.src.constant.component.ItMustBeToCharacters',
          defaultMessage: '以英文字母开头，可包含小写字母、数字和下划线，且长度为 2 ~ 128',
        })}
        overlayClassName="tooltip-small"
      >
        <a>
          {formatMessage({
            id: 'ocp-express.src.constant.component.NameFormat',
            defaultMessage: '名称格式',
          })}
        </a>
      </Tooltip>
      {formatMessage({
        id: 'ocp-express.src.constant.component.Incorrect',
        defaultMessage: '不正确',
      })}
    </span>
  );
}

export function getMySQLDbUserNameValidateMessage() {
  return (
    <span>
      <Tooltip
        placement="topLeft"
        title={formatMessage({
          id: 'ocp-express.src.constant.component.ItMustBeToCharacters.1',
          defaultMessage: '以英文字母开头，可包含小写字母、数字和下划线，且长度为 2 ~ 64',
        })}
        overlayClassName="tooltip-small"
      >
        <a>
          {formatMessage({
            id: 'ocp-express.src.constant.component.NameFormat',
            defaultMessage: '名称格式',
          })}
        </a>
      </Tooltip>
      {formatMessage({
        id: 'ocp-express.src.constant.component.Incorrect',
        defaultMessage: '不正确',
      })}
    </span>
  );
}

export function getOracleDbUserNameValidateMessage(count: number) {
  return (
    <span>
      <Tooltip
        placement="topLeft"
        title={formatMessage(
          {
            id: 'ocp-express.src.constant.component.ItMustStartWithA',
            defaultMessage:
              '以英文字母开头，可包含大写字母、小写字母、数字和下划线，且长度为 2 ~ {count}',
          },
          { count }
        )}
        overlayClassName="tooltip-small"
      >
        <a>
          {formatMessage({
            id: 'ocp-express.src.constant.component.NameFormat',
            defaultMessage: '名称格式',
          })}
        </a>
      </Tooltip>
      {formatMessage({
        id: 'ocp-express.src.constant.component.Incorrect',
        defaultMessage: '不正确',
      })}
    </span>
  );
}

/**
 * 根据参数类型获取参数组件
 */
export const getComponentByValueRange = (
  valueRange: API.ParameterInfo | API.ClusterParameterValueRange | API.TenantParameterValueRange
) => {
  const { type, allowedValues, minValue, maxValue } = valueRange || {};

  // 字符串 或者 时间段类型
  if (type === 'TIME' || type === 'STRING') {
    return (
      <Input
        placeholder={formatMessage({
          id: 'ocp-express.src.constant.component.EnterTextContent',
          defaultMessage: '请输入文本内容',
        })}
      />
    );
  }
  if (type === 'BOOL') {
    return (
      <Select
        showSearch={true}
        placeholder={formatMessage({
          id: 'ocp-express.src.constant.component.Select',
          defaultMessage: '请选择',
        })}
      >
        <Option value="true"> True </Option>
        <Option value="false"> False </Option>
      </Select>
    );
  }
  // 枚举值类型
  if (type === 'ENUM' && allowedValues) {
    return (
      <Select
        showSearch={true}
        placeholder={formatMessage({
          id: 'ocp-express.src.constant.component.Select',
          defaultMessage: '请选择',
        })}
      >
        {allowedValues.split(',').map((item: string) => (
          <Option key={item} value={item}>
            {item}
          </Option>
        ))}
      </Select>
    );
  }
  // 数字类型 InputNumer 可以控制最大和最小值，不必校验
  if (type === 'INT' || type === 'NUMERIC' || type === 'DOUBLE') {
    // 1、修复防止 maxValue 和 minValue 是空串 ""。 toNumber("")-> 0 导致最大最小值设置为 0 的问题
    // 2、部分时间类型是 Time 的取值范围是 [0,] 而不是正常[1s,1d]这样的，已经跟后端沟通，在解析上存在问题，但可以正常修改
    const min = minValue ? toNumber(minValue) : undefined;
    const max = maxValue ? toNumber(maxValue) : undefined;

    // 超出 JS 能处理的最大、最小整数范围，则使用 Input，按照字符串进行处理
    if (
      min > Number.MAX_SAFE_INTEGER ||
      min < Number.MIN_SAFE_INTEGER ||
      max > Number.MAX_SAFE_INTEGER ||
      max < Number.MIN_SAFE_INTEGER
    ) {
      return (
        <Input
          placeholder={formatMessage({
            id: 'ocp-express.src.constant.component.Enter',
            defaultMessage: '请输入',
          })}
        />
      );
    }

    return (
      <InputNumber
        {...(isNullValue(min) ? {} : { min })}
        // maxValue 为空时，会导致输入的值自动被清空，无法正确输入，因此根据 maxValue 是否为空判断是否设置 max 属性
        {...(isNullValue(max) ? {} : { max })}
        // 如果是 INT 整数类型，需要控制数字精度，保证输入为整数
        {...(type === 'INT' ? { precision: 0 } : {})}
        style={{ width: '100%' }}
      />
    );
  }
  // 字符串类型 或者 undefined 类型（OBProxy参数范围接口取不到值的话，使用字符串类型兜底）
  return (
    <Input
      placeholder={formatMessage({
        id: 'ocp-express.src.constant.component.Enter',
        defaultMessage: '请输入',
      })}
    />
  );
};

export function getSpaceValidateMessage() {
  return (
    <span>
      {formatMessage({
        id: 'ocp-express.src.constant.component.TheInputContentContainsSpaces',
        defaultMessage: '输入内容中含有空格',
      })}
    </span>
  );
}
