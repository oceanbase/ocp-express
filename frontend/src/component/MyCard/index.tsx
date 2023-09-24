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
import type { CardProps } from '@oceanbase/design/es/card';
import useStyles from './index.style';

export interface MyCardProps extends CardProps {
  children: React.ReactNode;
  title?: React.ReactNode;
  extra?: React.ReactNode;
  loading?: boolean;
  className?: string;
  headStyle?: React.CSSProperties;
}

const MyCard = ({
  children,
  title,
  extra,
  loading = false,
  className,
  headStyle,
  bodyStyle,
  ...restProps
}: MyCardProps) => {
  const { styles } = useStyles();
  return (
    <Card
      className={`${className} ${styles.card}`}
      bordered={false}
      bodyStyle={{ padding: '20px 24px', ...bodyStyle }}
      {...restProps}
    >
      {(title || extra) && (
        <div className={styles.header} style={headStyle}>
          {title && <span className={styles.title}>{title}</span>}
          {extra && <span className={styles.extra}>{extra}</span>}
        </div>
      )}

      <div style={{ width: '100%' }}>
        <Spin spinning={loading}>{children}</Spin>
      </div>
    </Card>
  );
};

export default MyCard;
