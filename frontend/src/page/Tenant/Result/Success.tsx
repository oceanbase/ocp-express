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
import React, { useEffect } from 'react';
import { Button, Card, Descriptions, Result } from '@oceanbase/design';
import { PageContainer } from '@ant-design/pro-components';
import { isNullValue } from '@oceanbase/util';
import { useRequest, useInterval } from 'ahooks';
import * as TaskController from '@/service/ocp-express/TaskController';
import { getTaskProgress } from '@/util/task';
import PageCard from '@/component/PageCard';
import styles from './Success.less';

export interface TaskSuccessProps {
  match?: {
    params?: {
      taskId?: number;
    };
  };

  style?: React.CSSProperties;
  className?: string;
  taskId?: number[] | number;
}

const TaskSuccess: React.FC<TaskSuccessProps> = ({ match, taskId }) => {
  const taskIdList = Array.isArray(taskId) ? taskId : [taskId];
  // 是否为多个任务，当前页面可以同时支持路由传参
  const isMultipleTask = match?.params?.taskId ? false : taskIdList.length > 1;

  let taskInstanceId;
  if (!isMultipleTask) {
    taskInstanceId = match?.params?.taskId || taskIdList?.[0];
  }

  const {
    data,
    refresh,
    run: getTaskInstance,
  } = useRequest(TaskController.getTaskInstance, {
    manual: true,
  });

  useEffect(() => {
    if (!isMultipleTask && !isNullValue(taskInstanceId)) {
      getTaskInstance({
        taskInstanceId,
      });
    }
  }, [isMultipleTask, taskInstanceId]);

  const taskData = data?.data || {};

  useInterval(
    () => {
      refresh();
    },
    // 任务处于运行态，则轮询任务进度
    taskData?.status === 'RUNNING' ? 1000 : null
  );

  return (
    <PageContainer className={styles.container}>
      <PageCard
        className={styles.newOBProxyCard}
        style={{
          height: 'calc(100vh - 72px)',
        }}
      >
        <Result
          className={styles.newOBProxyInfo}
          icon={<img src="/assets/common/guide.svg" alt="" />}
          title={formatMessage({
            id: 'ocp-express.Tenant.Result.Success.NewTenantTaskSubmittedSuccessfully',
            defaultMessage: '新建租户任务提交成功',
          })}
          subTitle={null}
          extra={
            <div>
              {!isMultipleTask && (
                <Button
                  data-aspm-click="c318536.d343257"
                  data-aspm-desc="新建租户任务提交成功-返回租户列表"
                  data-aspm-param={``}
                  data-aspm-expo
                  size="large"
                  style={{ marginRight: 8 }}
                  onClick={() => {
                    history.push('/tenant');
                  }}
                >
                  {formatMessage({
                    id: 'ocp-express.Tenant.Result.Success.ReturnToTenantList',
                    defaultMessage: '返回租户列表',
                  })}
                </Button>
              )}

              <Button
                data-aspm-click="c318536.d343263"
                data-aspm-desc="新建租户任务提交成功-查看任务详情"
                data-aspm-param={``}
                data-aspm-expo
                size="large"
                type="primary"
                onClick={() => {
                  history.push(`/task/${taskData.id}`);
                }}
              >
                {formatMessage({
                  id: 'ocp-express.Tenant.Result.Success.ViewTaskDetails',
                  defaultMessage: '查看任务详情',
                })}
              </Button>
            </div>
          }
        />

        <Card bordered={false} className={styles.detail}>
          <Descriptions
            title={formatMessage({
              id: 'ocp-express.OBProxy.Result.Success.TaskInformation',
              defaultMessage: '任务信息',
            })}
          >
            <Descriptions.Item
              label={formatMessage({
                id: 'ocp-express.OBProxy.Result.Success.TaskName',
                defaultMessage: '任务名称',
              })}
            >
              {taskData.name}
            </Descriptions.Item>
            <Descriptions.Item
              label={formatMessage({
                id: 'ocp-express.OBProxy.Result.Success.TaskId',
                defaultMessage: '任务 ID',
              })}
            >
              {taskInstanceId}
            </Descriptions.Item>
            <Descriptions.Item
              label={formatMessage({
                id: 'ocp-express.OBProxy.Result.Success.TaskProgress',
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
