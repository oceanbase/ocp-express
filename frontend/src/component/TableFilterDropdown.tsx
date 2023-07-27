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
import React, { useEffect, useState } from 'react';
import { Button, Space } from '@oceanbase/design';
import type { FilterDropdownProps } from 'antd/es/table/interface';
import MyInput from '@/component/MyInput';

export interface TableFilterDropdownProps extends FilterDropdownProps {
  /* 确定筛选后的回调函数 */
  onConfirm?: (value: React.Key) => void;
}

const TableFilterDropdown: React.FC<TableFilterDropdownProps> = ({
  setSelectedKeys,
  selectedKeys,
  confirm,
  clearFilters,
  visible,
  onConfirm,
}) => {
  const [value, setValue] = useState('');

  const confirmFilter = () => {
    confirm();
    if (onConfirm) {
      onConfirm(selectedKeys && selectedKeys[0]);
    }
  };

  useEffect(() => {
    if (!visible) {
      confirmFilter();
    }
  }, [visible]);

  return (
    <div
      style={{ padding: 8 }}
      onBlur={() => {
        confirmFilter();
      }}
    >
      <MyInput
        spm="表格自定义搜索-搜索框"
        onChange={e => {
          setValue(e.target.value);
          setSelectedKeys(e.target.value ? [e.target.value] : []);
        }}
        value={value}
        onPressEnter={() => {
          confirmFilter();
        }}
        style={{ width: 188, marginBottom: 8, display: 'block' }}
        data-aspm-expo
        data-aspm-param={``}
      />
      <Space>
        <Button
          spm="表格自定义搜索-搜索"
          type="primary"
          onClick={() => {
            confirmFilter();
          }}
          size="small"
          style={{ width: 90 }}
          data-aspm-expo
          data-aspm-param={``}
        >
          {formatMessage({ id: 'ocp-express.src.util.component.Search', defaultMessage: '搜索' })}
        </Button>
        <Button
          spm="表格自定义搜索-重置"
          onClick={() => {
            if (clearFilters) {
              clearFilters();
              setValue('');
            }
            confirmFilter();
          }}
          size="small"
          style={{ width: 90 }}
          data-aspm-expo
          data-aspm-param={``}
        >
          {formatMessage({ id: 'ocp-express.src.util.component.Reset', defaultMessage: '重置' })}
        </Button>
      </Space>
    </div>
  );
};

export default TableFilterDropdown;
