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
import { Input } from '@oceanbase/design';
import type { InputProps } from 'antd/es/input';

interface MyInputProps extends React.FC<InputProps> {
  Search: typeof Input.Search;
  Password: typeof Input.Password;
  TextArea: typeof Input.TextArea;
}

const MyInput: MyInputProps = props => {
  return (
    <Input
      placeholder={formatMessage({
        id: 'ocp-express.src.component.MyInput.PleaseEnter',
        defaultMessage: '请输入',
      })}
      {...props}
    />
  );
};

MyInput.Search = Input.Search;
MyInput.Password = Input.Password;
MyInput.TextArea = Input.TextArea;

export default MyInput;
