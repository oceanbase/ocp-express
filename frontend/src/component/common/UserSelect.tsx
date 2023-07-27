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
import { useSelector, useDispatch } from 'umi';
import React, { useEffect } from 'react';
import { Select } from 'antd';
import type { SelectProps } from 'antd/es/select';

const { Option } = Select;

export interface IUserSelectProps extends SelectProps<number | string> {
  valueProp?: 'id' | 'username';
}

const UserSelect: React.FC<IUserSelectProps> = ({ valueProp = 'id', ...restProps }) => {
  const { userListData } = useSelector((state: DefaultRootState) => state.iam);
  const userList = userListData.contents || [];

  const dispatch = useDispatch();

  useEffect(() => {
    dispatch({
      type: 'iam/getUserListData',
      payload: {},
    });
  }, []);

  return (
    <Select
      showSearch={true}
      optionFilterProp="children"
      placeholder={formatMessage({
        id: 'ocp-express.component.common.UserSelect.Select',
        defaultMessage: '请选择',
      })}
      {...restProps}
    >
      {userList.map(item => (
        <Option key={item.id} value={item[valueProp]}>
          {item.username}
        </Option>
      ))}
    </Select>
  );
};

export default UserSelect;
