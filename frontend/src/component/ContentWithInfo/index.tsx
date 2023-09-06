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
import type { SpaceProps } from '@oceanbase/design';
import { Space } from '@oceanbase/design';
import { InfoCircleFilled } from '@oceanbase/icons';
import styles from './index.less';

export interface ContentWithInfoProps extends SpaceProps {
  content: React.ReactNode;
  className?: string;
  style?: React.CSSProperties;
}

const ContentWithInfo: React.FC<ContentWithInfoProps> = ({ content, className, ...restProps }) => (
  <Space className={`${styles.container} ${className}`} {...restProps}>
    <InfoCircleFilled className={styles.icon} />
    <span className={styles.content}>{content}</span>
  </Space>
);

export default ContentWithInfo;
