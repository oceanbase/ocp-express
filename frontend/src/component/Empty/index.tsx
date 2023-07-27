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
import { Empty } from '@oceanbase/design';
import type { EmptyProps as AntdEmptyProps } from 'antd/es/empty';
import { PageContainer } from '@ant-design/pro-components';
import PageCard from '@/component/PageCard';
import styles from './index.less';

export interface EmptyProps extends AntdEmptyProps {
  image?: React.ReactNode;
  title?: React.ReactNode;
  description?: React.ReactNode;
  children?: React.ReactNode;
  // 展示模式: 页面模式 | 组件模式
  mode?: 'page' | 'pageCard' | 'component';
  size?: 'default' | 'small';
}

export default ({
  image = '/assets/common/empty.svg',
  title,
  description,
  children,
  // 默认为页面模式
  mode = 'page',
  size = 'default',
  className,
  style,
  ...restProps
}: EmptyProps) => {
  const empty = (
    <Empty
      className={`${styles.empty} ${size === 'small' ? styles.small : ''}`}
      image={image}
      description={
        <div>
          {title && <h2 className={styles.title}>{title}</h2>}
          <span className={styles.description}>{description}</span>
        </div>
      }
      {...restProps}
    >
      {children}
    </Empty>
  );
  const pageCard = (
    <PageCard
      className={`${mode === 'page' ? styles.page : styles.component} ${className}`}
      style={style}
    >
      {empty}
    </PageCard>
  );
  if (mode === 'page') {
    return <PageContainer>{pageCard}</PageContainer>;
  }
  if (mode === 'pageCard') {
    return pageCard;
  }
  if (mode === 'component') {
    return empty;
  }
  return <PageContainer>{pageCard}</PageContainer>;
};
