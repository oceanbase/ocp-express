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
import { DatePicker, Select, Space } from '@oceanbase/design';
import type { Moment } from 'moment';
import moment from 'moment';
import { ClockCircleOutlined } from '@ant-design/icons';
import { useControllableValue } from 'ahooks';
import { DATE_TIME_FORMAT_DISPLAY } from '@/constant/datetime';
import type { RangeOption } from './constant';
import {
  NEAR_10_MINUTES,
  NEAR_1_HOURS,
  NEAR_3_HOURS,
  NEAR_6_HOURS,
  NEAR_20_MINUTES,
  NEAR_30_MINUTES,
  NEAR_5_MINUTES,
} from './constant';
import useStyles from './index.style';

export type RangeDateKey = 'customize' | string;

export type RangeDateValue = {
  key: RangeDateKey;
  range: [Moment?, Moment?];
};

interface IProps {
  selects?: RangeOption[];
  defaultValue?: RangeDateValue;
  // eslint-disable-next-line
  onChange?: (value: RangeDateValue) => void;
  // eslint-disable-next-line
  value?: RangeDateValue;
  mode?: 'normal' | 'mini';
  rangePickerStyle?: React.CSSProperties;
}

export const OCPRangePicker = (props: IProps) => {
  const { styles } = useStyles();
  const {
    selects = [
      NEAR_5_MINUTES,
      NEAR_10_MINUTES,
      NEAR_20_MINUTES,
      NEAR_30_MINUTES,
      NEAR_1_HOURS,
      NEAR_3_HOURS,
      NEAR_6_HOURS,
    ],

    defaultValue = { key: NEAR_1_HOURS.name, range: NEAR_1_HOURS.range() },
    value,
    mode = 'normal',
    rangePickerStyle = {},
  } = props;
  const [innerValue, setInnerValue] = useControllableValue<RangeDateValue>(props, {
    defaultValue: value || defaultValue,
  });
  const showBorder = mode === 'normal' || (mode === 'mini' && innerValue?.key === 'customize');
  const showRange = !(mode === 'mini' && innerValue?.key !== 'customize');

  const handleSelect = (key: RangeDateKey) => {
    if (key === 'customize') {
      setInnerValue({ ...(innerValue as RangeDateValue), key });
    } else {
      setInnerValue({
        key,
        range: selects.find((e) => e.name === key)?.range() as [Moment, Moment],
      });
    }
  };

  const handleRangeChange = (range: [Moment, Moment]) => {
    if (!range) {
      setInnerValue({
        key: NEAR_30_MINUTES.name,
        range: NEAR_30_MINUTES.range(),
      });
    } else {
      setInnerValue({
        key: 'customize',
        range,
      });
    }
  };

  const disabledDate = (current: Moment) => {
    return current && current > moment().endOf('day');
  };

  return (
    <Space className={styles.ocpRangePicker}>
      <Space size={0}>
        {!showRange && <ClockCircleOutlined />}
        <Select
          bordered={showBorder}
          style={{ minWidth: 120 }}
          onSelect={handleSelect}
          value={innerValue?.key}
        >
          {selects.map((e) => (
            <Select.Option key={e.name} value={e.name}>
              {e.name}
            </Select.Option>
          ))}

          <Select.Option value="customize">
            {formatMessage({
              id: 'ocp-express.component.OCPRangePicker.CustomTime',
              defaultMessage: '自定义时间',
            })}
          </Select.Option>
        </Select>
      </Space>
      {showRange && (
        <DatePicker.RangePicker
          disabledDate={disabledDate}
          format={DATE_TIME_FORMAT_DISPLAY}
          showTime={true}
          allowClear={false}
          value={innerValue?.range}
          onChange={handleRangeChange}
          style={rangePickerStyle}
        />
      )}
    </Space>
  );
};
