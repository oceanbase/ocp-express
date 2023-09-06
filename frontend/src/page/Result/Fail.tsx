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
import { Button, Result } from '@oceanbase/design';
import Icon from '@oceanbase/icons';
import { PageContainer } from '@oceanbase/ui';
import PageCard from '@/component/PageCard';
import { ReactComponent as FailSvg } from '@/asset/fail.svg';

export interface TaskFailProps {
  style?: React.CSSProperties;
  className?: string;
}

const TaskFail: React.FC<TaskFailProps> = () => (
  <PageContainer>
    <PageCard
      style={{
        height: 'calc(100vh - 72px)',
      }}
    >
      <Result
        icon={<Icon component={FailSvg} />}
        title={formatMessage({
          id: 'ocp-express.page.Result.Fail.ClusterCreationFailed',
          defaultMessage: '创建集群失败',
        })}
        subTitle={formatMessage({
          id: 'ocp-express.page.Result.Fail.YouCanTryToRe',
          defaultMessage: '你可以尝试重新创建或返回集群概览',
        })}
        extra={
          <div>
            <Button
              type="primary"
              onClick={() => {
                history.push('/cluster/new');
              }}
              style={{ marginRight: 8 }}
            >
              {formatMessage({
                id: 'ocp-express.page.Result.Fail.ReCreateACluster',
                defaultMessage: '重新创建集群',
              })}
            </Button>
            <Button
              onClick={() => {
                history.push('/cluster');
              }}
            >
              {formatMessage({
                id: 'ocp-express.page.Result.Fail.ReturnToClusterOverview',
                defaultMessage: '返回集群概览',
              })}
            </Button>
          </div>
        }
      />
    </PageCard>
  </PageContainer>
);

export default TaskFail;
