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
import { Button, Card, Descriptions, Result } from '@oceanbase/design';
import { PageContainer } from '@ant-design/pro-components';
import { findBy } from '@oceanbase/util';
import { useRequest, useInterval } from 'ahooks';
import * as TaskController from '@/service/ocp-express/TaskController';
import { getTaskProgress } from '@/util/task';
import PageCard from '@/component/PageCard';
import styles from './Success.less';

export interface TaskSuccessProps {
  match: {
    params: {
      taskId: number;
    };
  };

  style?: React.CSSProperties;
  className?: string;
}

const TaskSuccess: React.FC<TaskSuccessProps> = ({
  match: {
    params: { taskId },
  },
}) => {
  const { data, refresh } = useRequest(TaskController.getTaskInstance, {
    defaultParams: [
      {
        taskInstanceId: taskId,
      },
    ],
  });
  const taskData = (data && data.data) || {};
  const currentSubtask = findBy(taskData.subtasks || [], 'status', 'RUNNING');

  useInterval(
    () => {
      refresh();
    },
    // 任务处于运行态，则轮询任务进度
    taskData.status === 'RUNNING' ? 1000 : null
  );

  return (
    <PageContainer className={styles.container}>
      <PageCard
        style={{
          height: 'calc(100vh - 72px)',
        }}
      >
        <Result
          icon={<img src="/assets/icon/success.svg" alt="" />}
          title={formatMessage({
            id: 'ocp-express.page.Result.Success.NewClusterTaskSubmittedSuccessfully',
            defaultMessage: '创建集群的任务提交成功',
          })}
          subTitle={formatMessage({
            id: 'ocp-express.page.Result.Success.YouCanSelectRelatedActions',
            defaultMessage: '你可以在当前页面选择相关操作或预览任务信息',
          })}
          extra={
            <div>
              <Button
                type="primary"
                style={{ marginRight: 8 }}
                onClick={() => {
                  history.push(`/task/${taskData.id}`);
                }}
              >
                {formatMessage({
                  id: 'ocp-express.page.Result.Success.ViewTaskDetails',
                  defaultMessage: '查看任务详情',
                })}
              </Button>
              <Button
                onClick={() => {
                  history.push('/cluster');
                }}
              >
                {formatMessage({
                  id: 'ocp-express.page.Result.Success.ReturnToClusterOverview',
                  defaultMessage: '返回集群概览',
                })}
              </Button>
            </div>
          }
        />

        <Card bordered={false} className={styles.detail}>
          <Descriptions
            title={formatMessage({
              id: 'ocp-express.page.Result.Success.TaskInformation',
              defaultMessage: '任务信息',
            })}
          >
            <Descriptions.Item
              label={formatMessage({
                id: 'ocp-express.page.Result.Success.TaskName',
                defaultMessage: '任务名称',
              })}
            >
              {taskData.name}
            </Descriptions.Item>
            <Descriptions.Item
              label={formatMessage({
                id: 'ocp-express.page.Result.Success.TaskId',
                defaultMessage: '任务 ID',
              })}
            >
              {taskId}
            </Descriptions.Item>
            <Descriptions.Item
              label={formatMessage({
                id: 'ocp-express.page.Result.Success.CurrentStep',
                defaultMessage: '当前步骤',
              })}
            >
              {currentSubtask.name || '-'}
            </Descriptions.Item>
            <Descriptions.Item
              label={formatMessage({
                id: 'ocp-express.page.Result.Success.TaskProgress',
                defaultMessage: '任务进度',
              })}
            >
              {getTaskProgress(taskData)}
            </Descriptions.Item>
          </Descriptions>
        </Card>
      </PageCard>
    </PageContainer>
  );
};

export default TaskSuccess;
