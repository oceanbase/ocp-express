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
import React, { useState, useEffect } from 'react';
import { Dropdown, Menu, Space, DatePicker } from '@oceanbase/design';
import { ClockCircleOutlined, DownOutlined } from '@ant-design/icons';
import type { DropDownProps } from 'antd/es/dropdown';
import type { Moment } from 'moment';
import moment from 'moment';
import { findByValue } from '@oceanbase/util';
import ContentWithIcon from '@/component/ContentWithIcon';
import { DATE_TIME_FORMAT_DISPLAY } from '@/constant/datetime';

type MenuKey =
  | '5-minute'
  | '10-minute'
  | '20-minute'
  | '30-minute'
  | '1-hour'
  | '3-hour'
  | 'hour'
  | 'day'
  | '3-day'
  | 'week'
  | 'month'
  | 'half-year'
  | 'year'
  | 'custom';

const { RangePicker } = DatePicker;
export interface RangeTimeDropdownProps extends Omit<DropDownProps, 'overlay'> {
  defaultMenuKey?: MenuKey;
  defaultValue?: [Moment?, Moment?];
  menuKeys: MenuKey[];
  menuLabels?: string[];
  onChange?: (value: [Moment?, Moment?], menuKey: MenuKey) => void;
  style?: React.CSSProperties;
  className?: string;
}

const RangeTimeDropdown: React.FC<RangeTimeDropdownProps> = ({
  defaultMenuKey,
  defaultValue,
  menuKeys = [],
  // 自定义 menu 的菜单项名称，顺序需要与 menuKeys 保持一致
  menuLabels = [],
  onChange,
  style = {},
  className,
  ...restProps
}) => {
  const ALL_MENU_LIST = [
    {
      value: '5-minute',
      label: formatMessage({
        id: 'ocp-express.component.RangeTimeDropdown.LastMinutes',
        defaultMessage: '最近 5 分钟',
      }),
      range: [moment().subtract(5, 'minutes'), moment()],
    },

    {
      value: '10-minute',
      label: formatMessage({
        id: 'ocp-express.component.RangeTimeDropdown.LastMinutes.1',
        defaultMessage: '最近 10 分钟',
      }),
      range: [moment().subtract(10, 'minutes'), moment()],
    },

    {
      value: '20-minute',
      label: formatMessage({
        id: 'ocp-express.component.RangeTimeDropdown.LastMinutes.2',
        defaultMessage: '最近 20 分钟',
      }),
      range: [moment().subtract(20, 'minutes'), moment()],
    },

    {
      value: '30-minute',
      label: formatMessage({
        id: 'ocp-express.component.RangeTimeDropdown.LastMinutes.3',
        defaultMessage: '最近 30 分钟',
      }),
      range: [moment().subtract(30, 'minutes'), moment()],
    },

    {
      value: '1-hour',
      label: formatMessage({
        id: 'ocp-express.component.RangeTimeDropdown.LastHour',
        defaultMessage: '最近 1 小时',
      }),
      range: [moment().subtract(1, 'hours'), moment()],
    },

    {
      value: '3-hour',
      label: formatMessage({
        id: 'ocp-express.component.RangeTimeDropdown.LastHours',
        defaultMessage: '最近 3 小时',
      }),
      range: [moment().subtract(3, 'hours'), moment()],
    },

    {
      value: 'hour',
      label: formatMessage({
        id: 'ocp-express.src.component.RangeTimeDropdown.LastHour',
        defaultMessage: '最近一小时',
      }),

      range: [moment().subtract(1, 'hours'), moment()],
    },

    {
      value: 'day',
      label: formatMessage({
        id: 'ocp-express.src.component.RangeTimeDropdown.LastDay',
        defaultMessage: '最近一天',
      }),

      range: [moment().subtract(1, 'days'), moment()],
    },

    {
      value: '3-day',
      label: formatMessage({
        id: 'ocp-express.component.RangeTimeDropdown.LastThreeDays',
        defaultMessage: '最近三天',
      }),

      range: [moment().subtract(3, 'days'), moment()],
    },

    {
      value: 'week',
      label: formatMessage({
        id: 'ocp-express.src.component.RangeTimeDropdown.LastWeek',
        defaultMessage: '最近一周',
      }),

      range: [moment().subtract(1, 'weeks'), moment()],
    },

    {
      value: 'month',
      label: formatMessage({
        id: 'ocp-express.component.RangeTimeDropdown.LastMonth',
        defaultMessage: '最近一个月',
      }),

      range: [moment().subtract(1, 'months'), moment()],
    },

    {
      value: 'half-year',
      label: formatMessage({
        id: 'ocp-express.component.RangeTimeDropdown.NearlySixMonths',
        defaultMessage: '近六个月',
      }),

      range: [moment().subtract(6, 'months'), moment()],
    },

    {
      value: 'year',
      label: formatMessage({
        id: 'ocp-express.component.RangeTimeDropdown.LastYear',
        defaultMessage: '最近一年',
      }),

      range: [moment().subtract(1, 'years'), moment()],
    },

    {
      value: 'custom',
      label: formatMessage({
        id: 'ocp-express.component.RangeTimeDropdown.CustomTime',
        defaultMessage: '自定义时间',
      }),
    },
  ];

  const menuList = menuKeys.map((item, index) => {
    const menuItem = findByValue(ALL_MENU_LIST, item);
    return {
      ...menuItem,
      // 支持属性配置 menu label
      label: menuLabels[index] || menuItem.label,
    };
  });

  // 默认选中菜单项: 默认选项 > 默认值不为空，则为自定义时间 > 默认值为空，则取第一个选项
  const realDefaultMenuKey = defaultMenuKey || (defaultValue ? 'custom' : menuKeys && menuKeys[0]);
  // 当前选中菜单项
  const [menuKey, setMenuKey] = useState(realDefaultMenuKey);
  // 默认时间范围
  const realDefaultValue = findByValue(menuList, realDefaultMenuKey).range || defaultValue;
  // 当前时间范围
  const [value, setValue] = useState(realDefaultValue);

  // 组件挂载时执行一次 onChange，以便外部能获取组件的值
  useEffect(() => {
    if (onChange) {
      onChange(value, menuKey);
    }
  }, []);

  const menu = (
    <Menu
      onClick={({ key }) => {
        const menuItem = findByValue(menuList, key);
        setMenuKey(key as MenuKey);
        setValue(menuItem.range);
        // 自定义时间选中后不触发值的变化
        if (onChange && menuItem.range) {
          onChange(menuItem.range, menuItem.value as MenuKey);
        }
      }}
    >
      {menuList.map(item => (
        <Menu.Item key={item.value}>{item.label}</Menu.Item>
      ))}
    </Menu>
  );

  return (
    <Space>
      <Dropdown placement="bottomLeft" {...restProps} overlay={menu}>
        <ContentWithIcon
          className={`range-time-dropdown pointable ${className}`}
          prefixIcon={{
            component: ClockCircleOutlined,
          }}
          affixIcon={{
            component: DownOutlined,
          }}
          content={findByValue(menuList, menuKey).label}
          style={style}
        />
      </Dropdown>
      {menuKey === 'custom' && (
        <RangePicker
          value={value}
          bordered={false}
          format={DATE_TIME_FORMAT_DISPLAY}
          allowClear={false}
          showTime={true}
          onChange={newRange => {
            if (onChange && newRange) {
              onChange(newRange, menuKey);
            }
          }}
        />
      )}
    </Space>
  );
};

export default RangeTimeDropdown;
