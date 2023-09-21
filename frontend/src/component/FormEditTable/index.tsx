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
import { PlusOutlined, DeleteOutlined } from '@oceanbase/icons';
import { Button, Popconfirm, Space, Table } from '@oceanbase/design';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import type { ButtonProps } from 'antd/es/button';
import type { GetFieldDecoratorOptions } from 'antd/es/form';
import type { WrappedFormUtils } from '@ant-design/compatible/lib/form/Form';
import type { TableProps, ColumnProps } from 'antd/es/table';
import classNames from 'classnames';
import { uniqueId } from 'lodash';
import EditRow, { EditContext } from './EditRow';
import EditCell from './EditCell';
import styles from './index.less';

export interface EditColumnProps<T> extends ColumnProps<T> {
  editable?: boolean;
  editing?: boolean;
  formItemProps: (text: any, record: T, index: number) => FormItemProps;
  fieldProps: (text: any, record: T, index: number) => GetFieldDecoratorOptions;
  fieldComponent: (text: any, record: T, index: number) => React.ReactNode;
}

export interface FormEditTableProps<T> extends TableProps<T> {
  /* 保存是否在加载中 */
  saveLoading?: boolean;
  /* 可编辑表格的模式: 表格模式 | 列表模式(无边框、间距更加紧凑，类似 List) */
  mode?: 'table' | 'list';
  /* 是否展示新增按钮 */
  allowAdd?: boolean;
  /* 是否编辑态和阅读态相互切换 */
  allowSwitch?: boolean;
  /* 是否整体禁用 */
  disabled?: boolean;
  /* 是否展示删除入口 */
  allowDelete?: boolean;
  /* 保存行的回调函数，第二个参数为保存成功的回调函数 */
  onSave?: (record: T, onSuccess: () => void) => void;
  /* 删除行的回调函数，第二个参数为删除成功的回调函数 */
  onDelete?: (record: T, onSuccess: () => void) => void;
  /* 删除行时是否展示气泡确认框 */
  showDeletePopconfirm: boolean;
  deletePopconfirmTitle?: (record: T) => React.ReactNode;
  /* 添加按钮文本 */
  addButtonText?: React.ReactNode;
  addButtonProps?: ButtonProps;
  columns: EditColumnProps<T>[];
  value: T[];
  onValueChange: (value: T[]) => void;
  className?: string;
}

interface FormEditTableState {
  editingKey: string;
}

class FormEditTable<T> extends React.Component<FormEditTableProps<T>, FormEditTableState> {
  constructor(props: FormEditTableProps<T>) {
    super(props);
    this.state = {
      editingKey: '',
    };
  }

  public isEditing = (record: T) => {
    const { editingKey } = this.state;
    return record.key === editingKey;
  };

  public handleValueChange = (value: T[]) => {
    const { onValueChange } = this.props;
    if (onValueChange) {
      // 通过下发的 onValueChange 属性函数收集 EditFormTable 的值
      onValueChange(value);
    }
  };

  public handleEdit = (record: T) => {
    const { value, onValueChange } = this.props;
    const newValue = (value || []).map((item) => {
      if (item.key === record.key) {
        return {
          ...item,
          ...record,
        };
      }
      return item;
    });
    if (onValueChange) {
      // 通过下发的 onValueChange 属性函数收集 EditFormTable 的值
      onValueChange(newValue);
    }
  };

  public handleAdd = () => {
    const { value, onValueChange } = this.props;
    const record: any = {
      key: uniqueId(),
    };

    if (onValueChange) {
      // 通过下发的 onValueChange 属性函数收集 EditFormTable 的值
      onValueChange([...(value || []), record]);
    }
  };

  handleCancel = () => {
    this.setState({ editingKey: '' });
  };

  handleSave = (form: WrappedFormUtils, key: string) => {
    const { value, onSave } = this.props;
    const { validateFields } = form;
    validateFields((err, values) => {
      if (!err) {
        // 浅拷贝 value 的值
        let newValue = [...(value || [])];
        let currentRecord;
        const index = newValue.findIndex((item) => key === item.key);
        if (index > -1) {
          const item = newValue[index];
          currentRecord = {
            ...item,
            ...values,
          };

          newValue.splice(index, 1, currentRecord);
        } else {
          newValue = [...newValue, values];
          currentRecord = values;
        }

        if (onSave) {
          onSave(currentRecord, () => {
            this.handleValueChange(newValue);
            this.setState({
              editingKey: '',
            });
          });
        }
      }
    });
  };

  handleEditing = (key: number) => {
    this.setState({
      editingKey: key,
    });
  };

  handleDelete = (record: T) => {
    const { value } = this.props;
    const newValue = value.filter((item) => item.key !== record.key);
    this.handleValueChange(newValue);
  };

  public render() {
    const {
      saveLoading,
      value,
      /* 默认表格模式 */
      mode = 'table',
      /* 默认不展示新增按钮 */
      allowAdd = false,
      /* 默认为编辑态和阅读态之间不可切换 */
      allowSwitch = false,
      /* 未设置 allowDelete 时，行数大于 1 才展示删除按钮 */
      allowDelete = value?.length > 1,
      disabled,
      onDelete,
      showDeletePopconfirm = true,
      deletePopconfirmTitle,
      addButtonText,
      addButtonProps,
      columns,
      className,
      // 编辑权限默认为 true
      editAccessible = true,
      // 删除权限默认为 true
      deleteAccessible = true,
      ...restProps
    } = this.props;
    const { editingKey } = this.state;
    let newColumns = columns
      .map((column) => {
        const { title, dataIndex, editable, formItemProps, fieldProps, fieldComponent } = column;

        if (!editable) {
          return column;
        }
        return {
          ...column,
          editable: true,
          onCell: (record: any, index: number) => ({
            title,
            disabled,
            dataIndex,
            record,
            index,
            /*
             * 若允许切换，则 editing 根据 editingKey 判断；
             * 若不允许切换，则 editing 总是为 true；
             */
            editing: allowSwitch ? this.isEditing(record) : true,
            editable,
            formItemProps,
            fieldProps,
            fieldComponent,
            // 若允许切换，则收集值的时机则是手动点击确定时，而不是每次更新都收集
            handleEdit: allowSwitch ? () => {} : this.handleEdit,
          }),
        };
      })
      .map((column) => {
        // 兼容默认的 table
        if (column?.dataIndex?.includes('.')) {
          column.dataIndex = column?.dataIndex?.split('.');
        }
        return column;
      });

    if (allowSwitch && (editAccessible || deleteAccessible)) {
      newColumns = [
        ...newColumns,
        {
          title: formatMessage({
            id: 'ocp-express.component.FormEditTable.Operation',
            defaultMessage: '操作',
          }),
          dataIndex: 'operation',
          render: (text, record) => {
            const editing = this.isEditing(record);
            return editing ? (
              <Space
                size="middle"
                className={classNames({
                  disabled,
                })}
              >
                <EditContext.Consumer>
                  {(form: WrappedFormUtils) => (
                    <Button
                      type="link"
                      loading={saveLoading}
                      onClick={() => this.handleSave(form, record.key)}
                    >
                      {formatMessage({
                        id: 'ocp-express.component.FormEditTable.Determine',
                        defaultMessage: '确定',
                      })}
                    </Button>
                  )}
                </EditContext.Consumer>
                <Popconfirm
                  title={formatMessage({
                    id: 'ocp-express.component.FormEditTable.AreYouSureToCancel',
                    defaultMessage: '确定取消吗？',
                  })}
                  onConfirm={() => {
                    setTimeout(() => {
                      this.handleCancel(record.key);
                    }, 0);
                  }}
                >
                  <Button type="link">
                    {formatMessage({
                      id: 'ocp-express.component.FormEditTable.Cancel',
                      defaultMessage: '取消',
                    })}
                  </Button>
                </Popconfirm>
              </Space>
            ) : (
              <Space
                size="middle"
                className={classNames({
                  disabled,
                })}
              >
                <a disabled={!!editingKey} onClick={() => this.handleEditing(record.key)}>
                  {formatMessage({
                    id: 'ocp-express.component.FormEditTable.Editing',
                    defaultMessage: '编辑',
                  })}
                </a>

                {showDeletePopconfirm ? (
                  <Popconfirm
                    placement="left"
                    title={
                      deletePopconfirmTitle
                        ? deletePopconfirmTitle(record)
                        : formatMessage({
                            id: 'ocp-express.component.FormEditTable.AreYouSureYouWant',
                            defaultMessage: '确定要删除该项配置吗？',
                          })
                    }
                    okText={formatMessage({
                      id: 'ocp-express.component.FormEditTable.Delete',
                      defaultMessage: '删除',
                    })}
                    okButtonProps={{
                      danger: true,
                      ghost: true,
                    }}
                    onConfirm={() => {
                      if (onDelete) {
                        onDelete(record, () => {
                          this.handleDelete(record);
                        });
                      }
                    }}
                  >
                    <a disabled={!!editingKey}>
                      {formatMessage({
                        id: 'ocp-express.component.FormEditTable.Delete',
                        defaultMessage: '删除',
                      })}
                    </a>
                  </Popconfirm>
                ) : (
                  <a
                    disabled={!!editingKey}
                    onClick={() => {
                      if (onDelete) {
                        onDelete(record, () => {
                          this.handleDelete(record);
                        });
                      }
                    }}
                  >
                    {formatMessage({
                      id: 'ocp-express.component.FormEditTable.Delete',
                      defaultMessage: '删除',
                    })}
                  </a>
                )}
              </Space>
            );
          },
        },
      ];
    } else if (allowDelete && deleteAccessible) {
      // 在用户没有删除权限时，也不展示删除入口；权限所见即所得
      newColumns = [
        ...newColumns,
        {
          dataIndex: 'operation',
          editable: false,
          width: 20,
          render: (text: any, record: T) => (
            // 为了保证 delete icon 与其他表单项垂直对齐，需要被 Form.Item 包裹
            <Form.Item>
              <span
                className={classNames({
                  disabled,
                })}
              >
                {showDeletePopconfirm ? (
                  <Popconfirm
                    placement="left"
                    title={
                      deletePopconfirmTitle
                        ? deletePopconfirmTitle(record)
                        : formatMessage({
                            id: 'ocp-express.component.FormEditTable.AreYouSureYouWant',
                            defaultMessage: '确定要删除该项配置吗？',
                          })
                    }
                    okText={formatMessage({
                      id: 'ocp-express.component.FormEditTable.Delete',
                      defaultMessage: '删除',
                    })}
                    okButtonProps={{
                      danger: true,
                      ghost: true,
                    }}
                    onConfirm={() => {
                      if (onDelete) {
                        // 如果存在 onDelete，则表明需要自定义删除逻辑，handleDelete 需要由上层决定是否调用
                        onDelete(record, () => {
                          this.handleDelete(record);
                        });
                      } else {
                        this.handleDelete(record);
                      }
                    }}
                  >
                    <DeleteOutlined
                      type="delete"
                      // 保证 icon 与表单垂直居中对齐
                      style={{ display: 'inline-block', marginTop: 8 }}
                    />
                  </Popconfirm>
                ) : (
                  <DeleteOutlined
                    onClick={() => {
                      if (onDelete) {
                        // 如果存在 onDelete，则表明需要自定义删除逻辑，handleDelete 需要由上层决定是否调用
                        onDelete(record, () => {
                          this.handleDelete(record);
                        });
                      } else {
                        this.handleDelete(record);
                      }
                    }}
                    // 保证 icon 与表单垂直居中对齐
                    style={{ display: 'inline-block', marginTop: 8 }}
                  />
                )}
              </span>
            </Form.Item>
          ),
        },
      ];
    }
    const components = {
      body: {
        row: EditRow,
        cell: EditCell,
      },
    };

    const newProps = {
      ...restProps,
      columns: newColumns,
      // 对于可编辑表格，数据源等价于自身的 value
      dataSource: value,
      components,
    };

    return (
      <div
        className={classNames(`${styles.container} ${className}`, {
          [styles.table]: mode === 'table',
          [styles.editing]: !!editingKey,
          [styles.list]: mode === 'list',
          [styles.disabledBg]: disabled,
        })}
      >
        {/* 因为 cursor: not-allowed 失效问题, 所以需要单独用 span 包裹 Table */}
        <Table pagination={false} rowKey={(record) => record.key} {...newProps} />
        {allowAdd && (
          <Button
            // button 上使用自带的 disabled 属性实现
            disabled={disabled}
            block={true}
            icon={<PlusOutlined />}
            className={styles.addButton}
            onClick={this.handleAdd}
            {...addButtonProps}
            style={{
              width: allowDelete ? 'calc(100% - 24px)' : '100%',
              ...addButtonProps?.style,
            }}
          >
            {addButtonText ||
              formatMessage({
                id: 'ocp-express.component.FormEditTable.Add',
                defaultMessage: '新增',
              })}
          </Button>
        )}
      </div>
    );
  }
}

export default FormEditTable;
