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
import { SyncOutlined } from '@ant-design/icons';
import ContentWithIcon from '@/component/ContentWithIcon';

export interface ContentWithReloadProps {
  content?: React.ReactNode;
  spin?: boolean;
  onClick?: (e: React.UIEvent) => void;
  style?: React.CSSProperties;
  className?: string;
}

const ContentWithReload: React.FC<ContentWithReloadProps> = ({
  content,
  spin = false,
  onClick,
  ...restProps
}) => {
  return (
    <ContentWithIcon
      content={content}
      affixIcon={{
        component: SyncOutlined,
        spin,
        pointable: true,
        style: {
          fontSize: 14,
          marginTop: -4,
          marginRight: 4,
        },
        onClick,
        tooltip: {
          title: formatMessage({
            id: 'ocp-express.src.component.ContentWithReload.Refresh',
            defaultMessage: '刷新',
          }),
        },
      }}
      {...restProps}
    />
  );
};

export default ContentWithReload;
