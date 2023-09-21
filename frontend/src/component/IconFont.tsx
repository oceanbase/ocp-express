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
import { createFromIconfontCN } from '@oceanbase/icons';

interface IconFontProps {
  type: string;
  className?: string;
  style?: React.CSSProperties;
}

const CustomIcon = createFromIconfontCN({
  // OCP Express 需要引入本地资源，保证离线环境下自定义图标的可访问性
  scriptUrl: '/js/iconfont.js',
});

export type IconFontType =
  | 'backup'
  | 'backup-colored'
  | 'cluster'
  | 'cluster-colored'
  | 'data-source'
  | 'data-source-colored'
  | 'docs'
  | 'diagnosis'
  | 'diagnosis-colored'
  | 'host'
  | 'host-colored'
  | 'log'
  | 'log-colored'
  | 'migration'
  | 'migration-colored'
  | 'monitor'
  | 'monitor-colored'
  | 'notification'
  | 'obproxy'
  | 'obproxy-colored'
  | 'package'
  | 'package-colored'
  | 'overview'
  | 'overview-colored'
  | 'property'
  | 'property-colored'
  | 'tenant'
  | 'tenant-colored'
  | 'sync'
  | 'sync-colored'
  | 'system'
  | 'system-colored'
  | 'user';

const IconFont = (props: IconFontProps) => {
  const { type, className, ...restProps } = props;
  return <CustomIcon type={type} className={className} {...restProps} />;
};

export default IconFont;
