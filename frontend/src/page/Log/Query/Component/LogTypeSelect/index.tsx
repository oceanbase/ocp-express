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

import MySelect from '@/component/MySelect';
import { LOG_TYPE_LIST } from '@/constant/log';
import { Space } from '@oceanbase/design';
import React, { useEffect, useState } from 'react';
import useStyles from './index.style';

interface LogTypeSelectProps {
  onChangeLogType?: (val: string) => void;
  onChange?: (value: string[]) => void;
  defaultLogType?: string;
}

const LogTypeSelect: React.FC<LogTypeSelectProps> = ({
  onChange,
  onChangeLogType,
  defaultLogType,
}) => {
  const { styles } = useStyles();
  const [type, setType] = useState<string>();
  const [typeValue, setTypeValue] = useState<string[]>([]);
  const [typeList, setTypeList] = useState<string[]>([]);

  useEffect(() => {
    if (defaultLogType) {
      setType(defaultLogType);
      if (onChangeLogType) {
        let typeText;
        switch (defaultLogType) {
          case 'CLUSTER':
            typeText = 'OB_LOG';
            break;
          case 'OCPAGENT':
            typeText = 'OCPAGENT_LOG';
            break;
          default:
            break;
        }
        onChangeLogType(typeText);
      }
      const typeOptions = LOG_TYPE_LIST.find(item => item.value === defaultLogType);
      if (typeOptions?.types) {
        setTypeList(typeOptions?.types);
        setTypeValue(typeOptions?.types);
        if (onChange) {
          onChange(typeOptions?.types);
        }
      }
    } else {
      setType(undefined);
      setTypeList([]);
      setTypeValue([]);
    }
  }, [defaultLogType]);

  const onLogTypeChange = (value: string, selectedOptions) => {
    if (onChangeLogType) {
      onChangeLogType(value);
    }
    setType(value);
    setTypeValue(selectedOptions?.types || []);
    setTypeList(selectedOptions?.types);
    if (onChange) {
      onChange(selectedOptions?.types || []);
    }
  };

  return (
    <Space size={0} className={styles.LogTypeSelect}>
      <MySelect
        allowClear={false}
        dropdownMatchSelectWidth={false}
        style={{ width: 140 }}
        value={type}
        options={LOG_TYPE_LIST}
        onChange={onLogTypeChange}
      />

      <MySelect
        mode="tags"
        value={typeValue}
        allowClear={true}
        maxTagCount="responsive"
        onChange={val => {
          setTypeValue(val);
          if (onChange) {
            onChange(val);
          }
        }}
        options={typeList?.map((item: string) => ({
          label: item,
          value: item,
        }))}
      />
    </Space>
  );
};

export default LogTypeSelect;
