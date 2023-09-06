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
import { history } from 'umi';
import React from 'react';
import { Button, Result, Space } from '@oceanbase/design';
import type { ResultProps } from 'antd/es/result';
import { PageContainer } from '@oceanbase/ui';
import PageCard from '@/component/PageCard';
import styles from './index.less';

export interface SuccessProps extends ResultProps {
  iconUrl?: string;
  taskId?: number;
  children?: React.ReactNode;
  style?: React.CSSProperties;
  className?: string;
}

const Success: React.FC<SuccessProps> = ({
  iconUrl = '/assets/icon/success.svg',
  taskId,
  children,
  className,
  style,
  ...restProps
}) => {
  return (
    <PageContainer className={`${styles.container} ${className}`} style={style}>
      <PageCard
        style={{
          height: 'calc(100vh - 72px)',
        }}
      >
        <Result
          icon={<img src={iconUrl} alt="" className={styles.icon} />}
          extra={
            <Space>
              <Button
                onClick={() => {
                  history.goBack();
                }}
              >
                {formatMessage({
                  id: 'ocp-express.component.Result.Return',
                  defaultMessage: '返回',
                })}
              </Button>
              {taskId && (
                <Button
                  type="primary"
                  onClick={() => {
                    history.push(`/task/${taskId}`);
                  }}
                >
                  {formatMessage({
                    id: 'ocp-express.component.Result.ViewTaskDetails',
                    defaultMessage: '查看任务详情',
                  })}
                </Button>
              )}
            </Space>
          }
          {...restProps}
        >
          {children}
        </Result>
      </PageCard>
    </PageContainer>
  );
};

export default Success;
