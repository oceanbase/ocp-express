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
import React from 'react';
import { Input, Select } from '@oceanbase/design';
import type { InputProps } from 'antd/es/input';
import type { SelectProps } from 'antd/es/select';

export interface ValueType {
  key?: string;
  value?: string;
}

export interface SelectAndInputGroupProps {
  value?: ValueType;
  onChange?: (value?: ValueType) => void;
  selectProps?: SelectProps;
  inputProps?: InputProps;
}

const SelectAndInputGroup: React.FC<SelectAndInputGroupProps> = ({
  value,
  onChange,
  selectProps,
  inputProps,
}) => {
  return (
    <Input.Group compact={true}>
      <Select
        placeholder={formatMessage({
          id: 'ocp-express.src.component.MySelect.PleaseSelect',
          defaultMessage: '请选择',
        })}
        {...selectProps}
        style={{
          width: 130,
          ...selectProps?.style,
        }}
        value={value?.key}
        onChange={selectValue => {
          if (onChange) {
            onChange({
              ...value,
              key: selectValue,
              // 切换筛选维度，需要重置筛选值
              value: undefined,
            });
          }
        }}
      />
      <Input
        allowClear={true}
        placeholder={formatMessage({
          id: 'ocp-express.src.component.MyInput.PleaseEnter',
          defaultMessage: '请输入',
        })}
        {...inputProps}
        style={{
          width: 'calc(100% - 130px)',
          ...inputProps?.style,
        }}
        value={value?.value}
        onChange={e => {
          if (onChange) {
            onChange({
              ...value,
              value: e.target.value,
            });
          }
        }}
      />
    </Input.Group>
  );
};

export default SelectAndInputGroup;
