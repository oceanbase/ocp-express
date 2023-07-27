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
import React, { useEffect, useRef } from 'react';
import { Button, Space } from 'antd';
import type { FilterDropdownProps } from 'antd/es/table/interface';
import { TreeSearch, useToken } from '@oceanbase/design';
import type { TreeSearchRef, Node } from '@oceanbase/design/src/TreeSearch';

export interface TableTreeFilterDropdownProps extends FilterDropdownProps {
  /* 确定筛选后的回调函数 */
  onConfirm?: (value?: React.Key[]) => void;
  treeData: Node[];
  // TODO: 待从 ob-ui 中导出TreeSearch 的属性类型定义并直接引用
  height?: number;
  width?: number;
  defaultExpandAll?: boolean;
}

const TableTreeFilterDropdown: React.FC<TableTreeFilterDropdownProps> = ({
  setSelectedKeys,
  selectedKeys,
  confirm,
  clearFilters,
  visible,
  onConfirm,
  ...restProps
}) => {
  const ref = useRef<TreeSearchRef>(null);

  const { token } = useToken();

  const confirmFilter = () => {
    confirm();
    if (onConfirm) {
      onConfirm(selectedKeys);
    }
  };

  useEffect(() => {
    if (!visible) {
      confirmFilter();
    }
  }, [visible]);

  return (
    <div style={{ paddingTop: '12px' }}>
      <TreeSearch
        ref={ref}
        onChange={nodes => {
          setSelectedKeys(nodes.map(node => node.value));
        }}
        height={300}
        {...restProps}
      />

      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          padding: '5px 12px',
          borderTop: '1px solid #f0f0f0',
        }}
      >
        <Button
          type="link"
          size="small"
          onClick={() => {
            if (ref.current?.invertSelect) {
              ref.current?.invertSelect();
            }
            confirmFilter();
          }}
        >
          {formatMessage({
            id: 'ocp-express.Detail.Component.Event.InvertSelection',
            defaultMessage: '反选',
          })}
        </Button>
        <Space>
          <Button
            size="small"
            onClick={() => {
              if (ref.current?.reset) {
                ref.current?.reset();
              }
              if (clearFilters) {
                clearFilters();
              }
              if (onConfirm) {
                onConfirm(selectedKeys);
              }
            }}
            disabled={selectedKeys?.length === 0}
            style={
              selectedKeys?.length > 0
                ? {
                  color: token.colorPrimary,
                }
                : {}
            }
          >
            {formatMessage({
              id: 'ocp-express.Detail.Component.Event.Reset',
              defaultMessage: '重置',
            })}
          </Button>
          <Button
            type="primary"
            size="small"
            onClick={() => {
              confirmFilter();
            }}
          >
            {formatMessage({
              id: 'ocp-express.Detail.Component.Event.Determine',
              defaultMessage: '确定',
            })}
          </Button>
        </Space>
      </div>
    </div>
  );
};

export default TableTreeFilterDropdown;
