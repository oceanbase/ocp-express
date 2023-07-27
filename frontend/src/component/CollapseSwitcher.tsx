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
import { DownOutlined, UpOutlined } from '@ant-design/icons';
import { noop } from 'lodash';

interface CollapseSwictherProps {
  textList?: React.ReactElement[];
  collapsed: boolean;
  onCollapse: (collapsed: boolean) => void;
}

class CollapseSwicther extends React.PureComponent<CollapseSwictherProps> {
  public state = {};

  public render() {
    const {
      textList = [
        formatMessage({
          id: 'ocp-express.component-legacy.common.CollapseSwitcher.Expand',
          defaultMessage: '展开',
        }),
        formatMessage({
          id: 'ocp-express.component-legacy.common.CollapseSwitcher.PutAway',
          defaultMessage: '收起',
        }),
      ],
      collapsed = false, // 默认展开
      onCollapse = noop,
    } = this.props;
    return (
      <a
        onClick={() => {
          onCollapse(!collapsed);
        }}
      >
        <span style={{ marginRight: 4 }}>{collapsed ? textList[0] : textList[1]}</span>
        {collapsed ? <DownOutlined /> : <UpOutlined />}
      </a>
    );
  }
}

export default CollapseSwicther;
