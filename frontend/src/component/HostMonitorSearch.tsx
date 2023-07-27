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
import { useSelector } from 'umi';
import React, { useState } from 'react';
import { Space, Switch } from '@oceanbase/design';
import type { Moment } from 'moment';
import moment from 'moment';
import { useInterval } from 'ahooks';
import { DATE_TIME_FORMAT_DISPLAY } from '@/constant/datetime';
import { FREQUENCY } from '@/constant';
import RangeTimeDropdown from '@/component/RangeTimeDropdown';

export interface HostMonitorSearchValue {
  realtime: boolean;
  range: [Moment?, Moment?];
  menuKey: string;
}

export interface HostMonitorSearchProps {
  onChange?: (value: HostMonitorSearchValue) => void;
}

const HostMonitorSearch: React.FC<HostMonitorSearchProps> = ({ onChange }) => {
  const [realtime, setRealtime] = useState(false);
  const [range, setRange] = useState<[Moment?, Moment?]>([]);
  const [menuKey, setMenuKey] = useState<string>();
  const { systemInfo } = useSelector((state: DefaultRootState) => state.global);
  const collectInterval = systemInfo?.monitorInfo?.collectInterval || FREQUENCY;

  const handleChange = (values?: Partial<HostMonitorSearchValue>) => {
    if (onChange) {
      onChange({
        realtime,
        range,
        menuKey,
        ...values,
      });
    }
  };

  useInterval(
    () => {
      handleChange({
        range: [moment().subtract(10, 'minutes'), moment()],
        menuKey: 'custom',
      });
    },
    realtime ? collectInterval * 1000 : undefined
  );

  return (
    <Space size={16}>
      {!realtime ? (
        <>
          <RangeTimeDropdown
            menuKeys={['hour', 'day', 'week', 'custom']}
            onChange={(value, newMenuKey) => {
              setRange(value);
              setMenuKey(newMenuKey);
              handleChange({
                range: value || [],
                menuKey: newMenuKey,
              });
            }}
          />
        </>
      ) : (
        <span>
          {formatMessage({
            id: 'ocp-express.Host.Detail.Monitor.UpdateTime',
            defaultMessage: '更新时间:',
          })}

          {moment().format(DATE_TIME_FORMAT_DISPLAY)}
        </span>
      )}

      <Space>
        <span>
          {formatMessage({
            id: 'ocp-express.src.component.HostMonitorSearch.AutomaticRefresh',
            defaultMessage: '自动刷新:',
          })}
        </span>
        <Switch
          size="small"
          checked={realtime}
          style={{ marginTop: -4 }}
          onChange={value => {
            const newRange = value
              ? ([moment().subtract(10, 'minutes'), moment()] as [Moment, Moment])
              : range;
            const newMenuKey = value ? 'custom' : menuKey;
            setRealtime(value);
            setRange(newRange);
            setMenuKey(newMenuKey);
            handleChange({
              realtime: value,
              range: newRange,
              menuKey: newMenuKey,
            });
          }}
        />
      </Space>
    </Space>
  );
};

export default HostMonitorSearch;
