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
import React, { useState } from 'react';
import { Card, Divider, Dropdown, Input, Typography } from '@oceanbase/design';
import type { DropDownProps } from 'antd/es/dropdown';
import type { InputProps } from 'antd/es/input';
import { CheckOutlined, FilterOutlined, SearchOutlined } from '@ant-design/icons';
import styles from './index.less';
import { findByValue } from '@oceanbase/util';

const { Text } = Typography;

export type FilterValue = string | number | undefined;

export interface Filter {
  value: FilterValue;
  label?: string;
}

export interface FilterDropdownProps extends Omit<DropDownProps, 'overlay'> {
  mode?: 'multiple' | 'single';
  filters: Filter[];
  value?: FilterValue[];
  onChange?: (value: FilterValue[]) => void;
  onClick?: (value: FilterValue) => void;
  /* 是否展示搜索框 */
  showSearch?: boolean;
  /* 是否展示选中项 */
  showSelection?: boolean;
  inputProps?: InputProps;
  children?: React.ReactElement;
  style?: React.CSSProperties;
  cardBodyStyle?: React.CSSProperties;
  className?: string;
}

const FilterDropdown: React.FC<FilterDropdownProps> = ({
  mode = 'multiple',
  filters = [],
  value = [],
  onChange,
  onClick,
  showSearch = true,
  showSelection = false,
  inputProps,
  children,
  style,
  className,
  cardBodyStyle,
  ...restProps
}) => {
  const [keyword, setKeyword] = useState('');
  return (
    <Dropdown
      placement="bottomRight"
      {...restProps}
      overlay={
        <Card
          bordered={false}
          bodyStyle={cardBodyStyle ? { ...cardBodyStyle, padding: 0 } : { padding: 0 }}
          className={`${styles.overlay} ${styles[`${mode}Overlay`]}`}
        >
          {showSearch && (
            <div
              className={styles.searchWrapper}
              onClick={e => {
                // 阻止事件冒泡，避免点击后隐藏下拉菜单
                e.stopPropagation();
              }}
            >
              <Input
                allowClear={true}
                value={keyword}
                onChange={e => {
                  setKeyword(e.target.value);
                }}
                prefix={<SearchOutlined className={styles.searchIcon} />}
                {...inputProps}
              />
            </div>
          )}
          <ul style={{ maxHeight: 320, overflow: 'auto', width: '100%' }}>
            {filters
              .filter(item => !keyword || (item.label && item.label.includes(keyword)))
              .map(item => (
                <li
                  key={item.value}
                  onClick={e => {
                    if (mode === 'multiple') {
                      // 阻止事件冒泡，避免点击后隐藏下拉菜单
                      e.stopPropagation();
                    }
                    const newValue = value.includes(item.value)
                      ? value.filter(valueItem => valueItem !== item.value)
                      : [...value, item.value];
                    if (onChange) {
                      onChange(newValue);
                    }
                    if (onClick) {
                      onClick(item.value);
                    }
                  }}
                  className={value.includes(item.value) ? styles.selected : ''}
                >
                  <Text ellipsis={true}>{item.label}</Text>
                  <CheckOutlined className={styles.checkIcon} />
                </li>
              ))}
          </ul>
          {mode === 'multiple' && (
            <>
              <Divider style={{ margin: 0 }} />
              <div
                style={{
                  padding: '8px 12px 4px 12px',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                }}
              >
                <a
                  disabled={!(value && value.length > 0)}
                  onClick={() => {
                    if (onChange) {
                      onChange([]);
                    }
                  }}
                >
                  {formatMessage({
                    id: 'ocp-express.component.FilterDropdown.Reset',
                    defaultMessage: '重置',
                  })}
                </a>
              </div>
            </>
          )}
        </Card>
      }
    >
      <span>
        {showSelection ? (
          value.length > 0 ? (
            <span
              style={{
                cursor: 'pointer',
                padding: '4px 12px',
                backgroundColor: 'rgba(0,0,0,.04)',
              }}
            >
              {value.map(item => findByValue(filters, item).label).join(',')}
            </span>
          ) : (
            children
          )
        ) : children ? (
          React.cloneElement(children, {
            // 筛选项不为空时用主色调标记筛选 icon
            className: `pointable ${
              value && value.length > 0 ? styles.filterIconFiltered : ''
            } ${className}`,
            style,
          })
        ) : (
          <FilterOutlined
            // 筛选项不为空时用主色调标记筛选 icon
            className={`pointable ${
              value && value.length > 0 ? styles.filterIconFiltered : ''
            } ${className}`}
            style={style}
          />
        )}
      </span>
    </Dropdown>
  );
};

export default FilterDropdown;
