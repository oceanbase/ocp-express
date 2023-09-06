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
import { Checkbox, Radio } from '@oceanbase/design';
import { BACKUP_SCHEDULE_MODE_LIST, WEEK_OPTIONS, MONTH_OPTIONS } from '@/constant/backup';
import styles from './index.less';

export type ScheduleMode = 'WEEK' | 'MONTH';

export interface Value {
  scheduleMode?: ScheduleMode;
  dayList?: number[];
}

export interface ScheduleModeAndDayListProps {
  value?: Value;
  onChange?: (value: Value) => void;
  // 调度周期
  SCHEDULE_MODE_LIST?: any[];
  // 默认调度周期
  initialMode?: string;
}

export interface ScheduleModeAndDayListInterface extends React.FC<ScheduleModeAndDayListProps> {
  validate: (rule: any, value: any, callback: any) => void;
}

const ScheduleModeAndDayList: ScheduleModeAndDayListInterface = ({
  value = {},
  onChange = () => { },
  SCHEDULE_MODE_LIST,
  initialMode,
}) => {
  const { scheduleMode = initialMode || 'WEEK', dayList = [] } = value;
  return (
    <div className={styles.container}>
      <Radio.Group
        value={scheduleMode}
        onChange={e => {
          onChange({
            ...value,
            scheduleMode: e.target.value,
            dayList: [],
          });
        }}
      >
        {(SCHEDULE_MODE_LIST ? SCHEDULE_MODE_LIST : BACKUP_SCHEDULE_MODE_LIST).map(item => (
          <Radio.Button key={item.value} value={item.value}>
            {item.label}
          </Radio.Button>
        ))}
      </Radio.Group>
      {/* 调度周期为日，样式不展示 */}
      <ul className={scheduleMode !== 'DAY' && styles.scheduleDayWrapper}>
        {
          //  调度周期为月、周时，展示选项
          scheduleMode !== 'DAY'
            ? (scheduleMode === 'WEEK' ? WEEK_OPTIONS : MONTH_OPTIONS).map(item => (
              <li
                key={item.value}
                // 调度周期为月，选中日期数为 10 天时，disable 掉未选中的日期
                className={`${dayList.includes(item.value) ? styles.selected : ''} ${scheduleMode === 'MONTH' &&
                  dayList.length === 10 &&
                  !dayList.includes(item.value)
                  ? styles.disabled
                  : ''
                  }`}
                onClick={() => {
                  onChange({
                    ...value,
                    dayList: dayList.includes(item.value)
                      ? dayList.filter(day => day !== item.value)
                      : [...dayList, item.value],
                  });
                }}
              >
                {item.label}
              </li>
            ))
            : null
        }

        {/* 全选只支持 `周` 调度周期 */}
        {scheduleMode === 'WEEK' && (
          <Checkbox
            checked={dayList.length === 7}
            indeterminate={dayList.length > 0 && dayList.length < 7}
            onChange={e => {
              onChange({
                ...value,
                dayList: e.target.checked ? WEEK_OPTIONS.map(item => item.value) : [],
              });
            }}
            style={{ marginLeft: 14, marginTop: 8 }}
          >
            {formatMessage({
              id: 'ocp-express.Component.ScheduleModeAndDayList.SelectAll',
              defaultMessage: '全选',
            })}
          </Checkbox>
        )}
      </ul>
    </div>
  );
};

ScheduleModeAndDayList.validate = (rule, value, callback) => {
  if (value && !value.scheduleMode) {
    callback(
      formatMessage({
        id: 'ocp-express.Component.ScheduleModeAndDayList.SelectASchedulingMode',
        defaultMessage: '请选择调度模式',
      })
    );
  }
  // 如果调度周期等于日时，跳出校验调度周期规则
  if (value && value.scheduleMode !== 'DAY' && (!value.dayList || value.dayList.length === 0)) {
    callback(
      formatMessage({
        id: 'ocp-express.Component.ScheduleModeAndDayList.SelectASchedulingPeriod',
        defaultMessage: '请选择调度周期',
      })
    );
  }

  callback();
};

export default ScheduleModeAndDayList;
