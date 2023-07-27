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

import type { SyntheticEvent } from 'react';
import React from 'react';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import type { GetFieldDecoratorOptions, FormItemProps } from 'antd/es/form';
import type { WrappedFormUtils } from '@ant-design/compatible/lib/form/Form';
import { EditContext } from './EditRow';

const FormItem = Form.Item;

interface CellValueType<T> extends SyntheticEvent<T> {
  currentTarget: EventTarget & T;
  target: EventTarget & { value: any };
}

export interface EditCellProps<T> {
  /* 是否为可编辑表单 */
  editable?: boolean;
  /* 是否正在编辑 */
  editing?: boolean;
  /* 列名称 */
  title: string;
  /* 列字段 */
  dataIndex: string;
  /* 是否禁用 */
  disabled: boolean;
  /* 每行对应的单条记录 */
  record: T;
  /* 单条记录对应的索引 */
  index: number;
  /* 对应表单项的属性配置 */
  formItemProps: (text: any, record: T, index: number) => FormItemProps;
  /* 对应字段的表单配置 */
  fieldProps: (text: any, record: T, index: number) => GetFieldDecoratorOptions;
  /* 对应字段的表单组件 */
  fieldComponent: (
    text: any,
    record: T,
    index: number,
    form: WrappedFormUtils
  ) => React.ReactElement;
  /* 处理编辑表单的逻辑 */
  handleEdit: (record: T) => void;
}

class EditCell<T> extends React.Component<EditCellProps<T>> {
  // eslint-disable-next-line
  getValue(record: T, fields: string[]) {
    let value = record;
    fields.forEach(item => {
      value = value && value[item];
    });
    return value;
  }

  renderCell = form => {
    const {
      editing,
      dataIndex,
      record,
      index,
      formItemProps = () => {},
      fieldProps = () => {},
      fieldComponent = () => <div />,
      handleEdit,
      children,
      disabled: tableDisabled,
    } = this.props;
    const { getFieldDecorator, validateFields } = form;
    let text;
    // 支持嵌套的数据格式
    if (dataIndex.includes('.')) {
      const fields = dataIndex.split('.');
      text = this.getValue(record, fields);
    } else {
      text = record[dataIndex];
    }
    const element = fieldComponent(text, record, index, form);
    const { onChange, disabled } = element.props;
    const FormComponent = React.cloneElement(element, {
      // 表单组件本身的 disabled 优先级要高于整个表格的 disabled
      disabled: disabled || tableDisabled,
      onChange: (value: CellValueType<T>) => {
        if (onChange) {
          onChange(value);
        }
        // 上层 EditFormTable 通过 handleEdit 去收集每次编辑后的最新值
        if (handleEdit) {
          // 由于需要拿到 onChange 后的值，需要异步执行
          setTimeout(() => {
            // TODO: 需要在上层提交表单时对 FormItem 进行校验，而不是只在 onChange 的时候校验
            validateFields((err: any, values: T) => {
              handleEdit({
                ...record,
                ...values,
              } as any);
            });
          }, 0);
        }
      },
    });
    return editing ? (
      <FormItem {...formItemProps(text, record, index)}>
        {getFieldDecorator(dataIndex, {
          initialValue: text,
          ...fieldProps(text, record, index),
        })(FormComponent)}
      </FormItem>
    ) : (
      children
    );
  };

  render() {
    const {
      editable,
      editing,
      disabled,
      dataIndex,
      title,
      record,
      index,
      fieldProps,
      fieldComponent,
      handleEdit,
      children,
      ...restProps
    } = this.props;
    return (
      <td {...restProps}>
        {editable ? (
          <EditContext.Consumer>{this.renderCell}</EditContext.Consumer>
        ) : (
          <span>{children}</span>
        )}
      </td>
    );
  }
}

export default EditCell;
