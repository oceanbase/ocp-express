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
import { Card, Spin } from '@oceanbase/design';
import useStyles from './index.style';

export interface TaskSuccessProps {
  children: React.ReactNode;
  loading?: boolean;
  style?: React.CSSProperties;
  className?: string;
}

const PageCard: React.FC<TaskSuccessProps> = ({
  children,
  loading = false,
  className,
  ...restProps
}) => {
  const { styles } = useStyles();
  return (
    <Card bordered={false} className={`${styles.card} ${className}`} {...restProps}>
      <Spin spinning={loading}>{children}</Spin>
    </Card>
  );
};

export default PageCard;
