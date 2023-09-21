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
import { connect } from 'umi';
import { InputNumber } from '@oceanbase/design';
import type { SelectValue } from 'antd/es/select';
import UnitSpecSelect from '@/component/UnitSpecSelect';
import styles from './index.less';

export type ValueType =
  | {
      unitSpecName?: string | undefined;
      unitCount?: number | undefined;
    }
  | undefined;

export interface ResourcePoolSelectProps {
  value?: ValueType;
  onChange?: (value: ValueType) => void;
  obVersion?: string;
}

interface ResourcePoolSelectState {
  value: ValueType;
}

class ResourcePoolSelect extends React.Component<ResourcePoolSelectProps, ResourcePoolSelectState> {
  static getDerivedStateFromProps(props: ResourcePoolSelectProps) {
    const newState: any = {
      prevProps: props,
    };

    const { value } = props;
    if ('value' in props) {
      // 将下发的 value 赋值给 FormEditTable
      newState.value = value;
    }
    return newState;
  }

  static validate = (rule, value, callback) => {
    if (value) {
      if (!value.unitSpecName) {
        callback(
          formatMessage({
            id: 'ocp-express.component.ResourcePoolSelect.PleaseSelectTheUnitSpecification',
            defaultMessage: '请选择 Unit 规格',
          }),
        );
      }
      if (!value.unitCount) {
        callback(
          formatMessage({
            id: 'ocp-express.component.ResourcePoolSelect.PleaseEnterTheUnitQuantity',
            defaultMessage: '请输入 Unit 数量',
          }),
        );
      }
      callback();
    }
    callback();
  };

  constructor(props: ResourcePoolSelectProps) {
    super(props);
    this.state = {
      value: undefined,
    };
  }

  public handleChange = (value: ValueType) => {
    const { onChange } = this.props;
    if (onChange) {
      // 通过下发的 onChange 属性函数收集 ResourcePoolSelect 的值
      onChange(value);
    }
  };

  public handleSelectChange = (val: SelectValue) => {
    const { value } = this.state;
    this.handleChange({
      ...value,
      unitSpecName: val as string | undefined,
    });
  };

  public handleInputNumberChange = (val: number | undefined) => {
    const { value } = this.state;

    this.handleChange({
      ...value,
      unitCount: val,
    });
  };

  public render() {
    const { value } = this.state;
    const { obVersion } = this.props;

    return (
      <span className={styles.container}>
        <UnitSpecSelect
          value={value && value.unitSpecName}
          obVersion={obVersion}
          onChange={this.handleSelectChange}
        />

        <InputNumber
          min={1}
          value={value && value.unitCount}
          onChange={this.handleInputNumberChange}
          placeholder={formatMessage({
            id: 'ocp-express.component.ResourcePoolSelect.PleaseEnterTheUnitQuantity',
            defaultMessage: '请输入 Unit 数量',
          })}
        />
      </span>
    );
  }
}

export default connect(() => ({}))(ResourcePoolSelect);
