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

import React from 'react';
import { Divider } from '@oceanbase/design';
import { PlusOutlined } from '@ant-design/icons';
import ContentWithIcon from '@/component/ContentWithIcon';

const SelectDropdownRender: React.FC<{
  menu: any;
  text: React.ReactNode;
  onClick?: () => void;
}> = ({ menu, text, ...restProps }) => {
  return (
    <div>
      {menu}
      <div>
        <Divider style={{ margin: '4px 0' }} />
        <div
          style={{
            padding: '4px 8px 8px 8px',
            cursor: 'pointer',
            // 由于 Select 在 empty 状态会将下拉菜单的文字都置灰，会影响 SelectDropdownRender，这里做样式覆盖
            color: 'rgba(0, 0, 0, 0.85)',
          }}
          {...restProps}
        >
          <ContentWithIcon
            prefixIcon={{
              component: PlusOutlined,
            }}
            content={text}
          />
        </div>
      </div>
    </div>
  );
};

export default SelectDropdownRender;
