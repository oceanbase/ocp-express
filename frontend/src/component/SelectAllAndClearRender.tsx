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

export interface SelectAllAndClearRenderProps {
  menu: any[];
  onSelectAll: () => void;
  onClearAll: () => void;
}

const SelectAllAndClearRender: React.FC<SelectAllAndClearRenderProps> = ({
  menu,
  onSelectAll,
  onClearAll,
}) => {
  return (
    <div>
      {menu}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          padding: '5px 12px',
          borderTop: '1px solid #f0f0f0',
        }}
      >
        <a onClick={onSelectAll}>
          {formatMessage({
            id: 'ocp-express.src.component.SelectAllAndClearRender.SelectAll',
            defaultMessage: '全选',
          })}
        </a>
        <a onClick={onClearAll}>
          {formatMessage({
            id: 'ocp-express.src.component.SelectAllAndClearRender.Empty',
            defaultMessage: '清空',
          })}
        </a>
      </div>
    </div>
  );
};

export default SelectAllAndClearRender;
