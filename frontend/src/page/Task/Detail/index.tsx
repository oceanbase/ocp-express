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

import React, { useState, useEffect, useRef } from 'react';
import { formatMessage } from '@/util/intl';
import { history } from 'umi';
import {
  Button,
  Descriptions,
  Divider,
  Radio,
  Space,
  Tooltip,
  Typography,
  Modal,
  message,
} from '@oceanbase/design';
import type { Route } from '@oceanbase/design/es/breadcrumb/Breadcrumb';
import { find, isFunction, toNumber } from 'lodash';
import { PageContainer } from '@oceanbase/ui';
import { token } from '@oceanbase/design';
import { isNullValue, findByValue } from '@oceanbase/util';
import Icon from '@oceanbase/icons';
import { useRequest, useInterval, useLockFn } from 'ahooks';
import useDocumentTitle from '@/hook/useDocumentTitle';
import { breadcrumbItemRender } from '@/util/component';
import * as TaskController from '@/service/ocp-express/TaskController';
import { TASK_STATUS_LIST } from '@/constant/task';
import { isEnglish } from '@/util';
import { download } from '@/util/export';
import { formatTime } from '@/util/datetime';
import { getTaskDuration, getTaskProgress } from '@/util/task';
import { getOperationComponent } from '@/util/component';
import tracert from '@/util/tracert';
import ContentWithReload from '@/component/ContentWithReload';
import ContentWithQuestion from '@/component/ContentWithQuestion';
import Log from './Log';
import type { TaskGraphRef } from './Log/TaskGraph';
import Flow from './Flow';
import useStyles from './index.style';

const { Text } = Typography;

const LogSvg = () => (
  <svg width="16px" height="16px" viewBox="0 0 16 16" fill="currentColor">
    <path d="M2.5,3.140625 C2.5,3.00255381 2.61192881,2.890625 2.75,2.890625 L6,2.890625 L6,2.015625 C6,1.8797619 6.10983737,1.76830711 6.24510006,1.76567268 L13.25,1.765625 C13.3858631,1.765625 13.4973179,1.87546237 13.5,2.01072506 L13.5,4.765625 C13.5,4.9014881 13.3901626,5.01294289 13.2548999,5.01557732 L13.25,5.015625 L6.25,5.015625 C6.1141369,5.015625 6.00268211,4.90578763 6,4.77052494 L6,3.890625 L3.5,3.890625 L3.5,7.5 L6,7.5 L6,6.625 C6,6.4891369 6.10983737,6.37768211 6.24510006,6.37504768 L13.25,6.375 C13.3858631,6.375 13.4973179,6.48483737 13.5,6.62010006 L13.5,9.375 C13.5,9.5108631 13.3901626,9.62231789 13.2548999,9.62495232 L6.25,9.625 C6.1141369,9.625 6.00268211,9.51516263 6,9.37989994 L6,8.515625 L3.5,8.515625 L3.5,12.125 L6,12.125 L6,11.25 C6,11.1141369 6.10983737,11.0026821 6.24510006,11.0000477 L13.25,11 C13.3858631,11 13.4973179,11.1098374 13.5,11.2451001 L13.5,14 C13.5,14.1358631 13.3901626,14.2473179 13.2548999,14.2499523 L6.25,14.25 C6.1141369,14.25 6.00268211,14.1401626 6,14.0048999 L6,13.125 L2.75,13.125 C2.61192881,13.125 2.5,13.0130712 2.5,12.875 L2.5,3.140625 Z M12.375,12.125 L7.125,12.125 L7.125,13.125 L12.375,13.125 L12.375,12.125 Z M12.375,7.5 L7.125,7.5 L7.125,8.5 L12.375,8.5 L12.375,7.5 Z M12.375,2.890625 L7.125,2.890625 L7.125,3.890625 L12.375,3.890625 L12.375,2.890625 Z" />
  </svg>
);

const FlowSvg = () => (
  <svg width="16px" height="16px" viewBox="0 0 16 16" fill="currentColor">
    <path d="M8,1.75 C8.96649831,1.75 9.75,2.53350169 9.75,3.5 C9.75,4.29245595 9.22327007,4.96188753 8.50081842,5.17728654 L8.5,6.81226563 L12.4595616,7.96451862 L12.469138,7.96737411 C12.7841631,8.06357715 13,8.3544789 13,8.68464636 L13,8.68464636 L13.0008184,10.8070885 C13.7232701,11.0224875 14.25,11.6919191 14.25,12.484375 C14.25,13.4508733 13.4664983,14.234375 12.5,14.234375 C11.5335017,14.234375 10.75,13.4508733 10.75,12.484375 C10.75,11.6915557 11.2772131,11.0218736 12.0001754,10.8067925 L12,8.87228125 L8,7.708 L4,8.87228125 L4.00081842,10.8070885 C4.72327007,11.0224875 5.25,11.6919191 5.25,12.484375 C5.25,13.4508733 4.46649831,14.234375 3.5,14.234375 C2.53350169,14.234375 1.75,13.4508733 1.75,12.484375 C1.75,11.6915557 2.27721305,11.0218736 3.00017544,10.8067925 L3,8.68464636 C3,8.3544789 3.2158369,8.06357715 3.53086199,7.96737411 L3.54043844,7.96451862 L7.5,6.81226563 L7.50017544,5.17758253 C6.77721305,4.96250138 6.25,4.29281929 6.25,3.5 C6.25,2.53350169 7.03350169,1.75 8,1.75 Z M3.5,11.734375 C3.08578644,11.734375 2.75,12.0701614 2.75,12.484375 C2.75,12.8985886 3.08578644,13.234375 3.5,13.234375 C3.91421356,13.234375 4.25,12.8985886 4.25,12.484375 C4.25,12.0701614 3.91421356,11.734375 3.5,11.734375 Z M12.5,11.734375 C12.0857864,11.734375 11.75,12.0701614 11.75,12.484375 C11.75,12.8985886 12.0857864,13.234375 12.5,13.234375 C12.9142136,13.234375 13.25,12.8985886 13.25,12.484375 C13.25,12.0701614 12.9142136,11.734375 12.5,11.734375 Z M8,2.75 C7.58578644,2.75 7.25,3.08578644 7.25,3.5 C7.25,3.91421356 7.58578644,4.25 8,4.25 C8.41421356,4.25 8.75,3.91421356 8.75,3.5 C8.75,3.08578644 8.41421356,2.75 8,2.75 Z" />
  </svg>
);

export interface DetailProps {
  match: {
    params: {
      taskId: number;
    };
  };

  location: {
    pathname: string;
    query: {
      backUrl?: string;
    };
  };
}

const Detail: React.FC<DetailProps> = ({
  match: {
    params: { taskId },
  },

  location: {
    query: { backUrl },
  },
}) => {
  const { styles } = useStyles();
  const [mode, setMode] = useState<'log' | 'flow'>('log');
  const [subtaskId, setSubtaskId] = useState<number | string | undefined>(undefined);
  const logRef = useRef<TaskGraphRef>(null);
  const flowRef = useRef<TaskGraphRef>(null);

  // 获取任务详情
  const { data, refresh, loading } = useRequest(TaskController.getTaskInstance, {
    defaultParams: [
      {
        taskInstanceId: taskId,
      },
    ],
  });

  const taskData = data?.data || {};
  const lockRefresh = useLockFn(refresh);

  useDocumentTitle(taskData?.name);

  const statusItem = findByValue(TASK_STATUS_LIST, taskData.status);
  // 任务是否处于轮询状态
  const polling = taskData?.status === 'RUNNING';
  // 当前选中的子任务
  const subtask = find(
    taskData?.subtasks || [],
    (item) => !isNullValue(subtaskId) && item.id === toNumber(subtaskId),
  );

  // 子任务处在运行中的状态才发起日志轮询，待执行的任务不需要发起轮询，因为待执行的任务无法查看日志
  const logPolling = subtask?.status === 'RUNNING';

  // 获取子任务日志
  const {
    data: logData,
    run: getSubtaskLog,
    loading: logLoading,
  } = useRequest(TaskController.getSubtaskLog, {
    manual: true,
  });

  const log = logData?.data?.log || '';
  const getSubtaskLogWithLock = useLockFn(getSubtaskLog);

  // 由于 ready 仅在第一次生效，当 ready 和 refreshDeps 包含同一变量时，ready 的拦截逻辑不符合预期
  // 属于 ahooks 的一个问题，因此先用 useEffect 实现
  useEffect(() => {
    if (!isNullValue(subtask?.id)) {
      getSubtaskLog({
        taskInstanceId: taskId,
        subtaskInstanceId: subtask?.id,
      });
    }
  }, [
    subtask?.id,
    // 子任务状态改变时，需要重新请求日志，避免任务成功或失败时，由于不处于轮询态导致无法拉取最新日志的问题
    subtask?.status,
  ]);

  // 任务轮询
  useInterval(
    () => {
      lockRefresh();
    },
    polling ? 1000 : null,
  );

  // 子任务日志轮询
  useInterval(
    () => {
      getSubtaskLogWithLock({
        taskInstanceId: taskId,
        subtaskInstanceId: subtask?.id,
      });
    },
    logPolling ? 1000 : null,
  );

  // 放弃任务
  const { runAsync: rollbackTask } = useRequest(TaskController.rollbackTask, {
    manual: true,
    onSuccess: (res) => {
      if (res.successful) {
        message.success(
          formatMessage({
            id: 'ocp-express.Task.Detail.TaskAbandoned',
            defaultMessage: '任务放弃成功',
          }),
        );

        refresh();
      }
    },
  });

  // 重试任务
  const { runAsync: retryTask } = useRequest(TaskController.retryTask, {
    manual: true,
    onSuccess: (res) => {
      if (res.successful) {
        message.success(
          formatMessage({
            id: 'ocp-express.Task.Detail.TheTaskIsRetried',
            defaultMessage: '任务重试成功',
          }),
        );

        refresh();
      }
    },
  });

  // 下载任务日志
  const { run: startDownloadTaskLog, loading: downloadLogLoading } = useRequest(
    TaskController.downloadTaskDiagnosis,
    {
      manual: true,
      onSuccess: (res) => {
        download(res, `log_task_${taskId}.zip`);
      },
    },
  );

  const handleOperation = (key: string) => {
    if (key === 'downloadLog') {
      // 下载任务日志
      startDownloadTaskLog({
        taskInstanceId: taskId,
      });
    } else if (key === 'retry') {
      // 重试任务
      Modal.confirm({
        title: formatMessage({
          id: 'ocp-express.Task.Detail.AreYouSureYouWant.1',
          defaultMessage: '确定要重试当前任务吗？',
        }),

        content: formatMessage({
          id: 'ocp-express.Task.Detail.ThisWillRollBackAll.1',
          defaultMessage: '重试操作会重试所有失败节点，并从失败节点开始继续向下执行',
        }),

        onOk: () => {
          return retryTask({
            taskInstanceId: taskId,
          });
        },
      });
    } else if (key === 'giveup') {
      // 放弃任务 = 回滚任务
      Modal.confirm({
        title: formatMessage({
          id: 'ocp-express.Task.Detail.AreYouSureYouWant',
          defaultMessage: '确定要放弃当前任务吗？',
        }),

        content: formatMessage({
          id: 'ocp-express.Task.Detail.ThisWillRollBackAll',
          defaultMessage: '这将从失败处开始回滚所有已执行过的任务',
        }),

        okText: formatMessage({ id: 'ocp-express.Task.Detail.GiveUp', defaultMessage: '放弃' }),
        okButtonProps: {
          danger: true,
          ghost: true,
        },

        onOk: () => {
          return rollbackTask({
            taskInstanceId: taskId,
          });
        },
      });
    }
  };

  const routes: Route[] = [
    {
      path: '/task',
      breadcrumbName: formatMessage({ id: 'ocp-express.Task.Detail.Task', defaultMessage: '任务' }),
    },

    {
      breadcrumbName: taskData.name,
    },
  ];

  return (
    <PageContainer
      className={`${styles.container} ${mode === 'flow' ? styles.flow : ''}`}
      // 轮询状态下不展示 loading 态
      loading={polling ? false : loading}
      ghost={true}
      header={{
        breadcrumb: { routes, itemRender: breadcrumbItemRender },
        title: (
          <ContentWithReload
            // 轮询状态下不展示 loading 态
            spin={polling ? false : loading}
            content={taskData.name}
            onClick={() => {
              refresh();
            }}
          />
        ),

        onBack: () => {
          if (backUrl) {
            history.push(backUrl);
          } else {
            history.goBack();
          }
        },
        extra: (
          <span>
            <Radio.Group
              data-aspm-click="c304251.d308748"
              data-aspm-desc="任务详情-切换视图"
              data-aspm-param={``}
              data-aspm-expo
              value={mode}
              onChange={(e) => {
                setMode(e.target.value);
                // 重置选中的 subtask
                setSubtaskId(undefined);
              }}
            >
              <Tooltip
                title={formatMessage({
                  id: 'ocp-express.Task.Detail.LogView',
                  defaultMessage: '日志视图',
                })}
              >
                <Radio.Button value="log" style={{ zIndex: 1 }}>
                  <Icon
                    component={LogSvg}
                    style={mode === 'log' ? { color: token.colorInfo } : {}}
                  />
                </Radio.Button>
              </Tooltip>
              <Tooltip
                title={formatMessage({
                  id: 'ocp-express.Task.Detail.FlowView',
                  defaultMessage: '流转视图',
                })}
              >
                <Radio.Button value="flow">
                  <Icon
                    component={FlowSvg}
                    style={mode === 'flow' ? { color: token.colorInfo } : {}}
                  />
                </Radio.Button>
              </Tooltip>
            </Radio.Group>
            <Divider
              type="vertical"
              style={{ height: 32, margin: '0px 16px', display: 'inline-block' }}
            />

            <Space>
              <Button
                data-aspm-click="c304251.d308749"
                data-aspm-desc="任务详情-定位当前进度"
                data-aspm-param={``}
                data-aspm-expo
                onClick={() => {
                  if (mode === 'log') {
                    if (isFunction(logRef?.current?.setLatestSubtask)) {
                      logRef?.current?.setLatestSubtask();
                    }
                  } else if (mode === 'flow') {
                    if (isFunction(flowRef?.current?.setLatestSubtask)) {
                      flowRef?.current?.setLatestSubtask();
                    }
                  }
                }}
              >
                {formatMessage({
                  id: 'ocp-express.Task.Detail.LocateTheCurrentProgress',
                  defaultMessage: '定位当前进度',
                })}
              </Button>
              {getOperationComponent({
                mode: 'button',
                displayCount: 3,
                operations: (statusItem.operations || []).map((item) => {
                  if (item.value === 'downloadLog') {
                    return {
                      ...item,
                      buttonProps: {
                        // 下载日志的操作按钮增加 loading 效果
                        loading: downloadLogLoading,
                      },
                    };
                  }
                  if (item.value === 'giveup' || item.value === 'retry') {
                    if (taskData.isRemote) {
                      // 如果属于远程 OCP 发起的任务，则禁止回滚和重试
                      return {
                        ...item,
                        tooltip: {
                          placement: 'topRight',
                          title: formatMessage({
                            id: 'ocp-express.Task.Detail.TheCurrentTaskIsInitiated',
                            defaultMessage: '当前任务为远程 OCP 发起，请到发起端的 OCP 进行操作',
                          }),
                        },

                        buttonProps: {
                          disabled: true,
                        },
                      };
                    } else if (item.value === 'giveup' && taskData.prohibitRollback) {
                      // 不支持回滚 (放弃) 的任务，则 `放弃任务` 的按钮置灰，并提示用户
                      return {
                        ...item,
                        tooltip: {
                          placement: 'topRight',
                          title: formatMessage({
                            id: 'ocp-express.Task.Detail.TheCurrentTaskCannotBe',
                            defaultMessage: '当前任务不支持放弃',
                          }),
                        },

                        buttonProps: {
                          disabled: true,
                        },
                      };
                    }
                  }
                  return item;
                }),
                record: taskData,
                handleOperation,
              })}
            </Space>
          </span>
        ),
      }}
    >
      <div
        data-aspm="c304180"
        data-aspm-desc="任务信息"
        data-aspm-expo
        // 扩展参数
        data-aspm-param={tracert.stringify({
          // 任务名
          taskName: taskData.name,
          // 任务子任务数
          taskSubtaskCount: taskData.subtasks?.length,
        })}
      >
        <Descriptions column={24} className={styles.descriptions}>
          <Descriptions.Item
            label={formatMessage({
              id: 'ocp-express.Task.Detail.TaskId',
              defaultMessage: '任务 ID',
            })}
            span={isEnglish() ? 6 : 3}
          >
            {taskData.id}
          </Descriptions.Item>
          <Descriptions.Item
            label={formatMessage({
              id: 'ocp-express.Task.Detail.TaskStatus',
              defaultMessage: '任务状态',
            })}
            span={isEnglish() ? 6 : 3}
          >
            {statusItem.label}
          </Descriptions.Item>
          <Descriptions.Item
            label={formatMessage({
              id: 'ocp-express.Task.Detail.CurrentProgress',
              defaultMessage: '当前进度',
            })}
            span={isEnglish() ? 6 : 3}
          >
            {getTaskProgress(taskData)}
          </Descriptions.Item>
          <Descriptions.Item
            label={formatMessage({
              id: 'ocp-express.Task.Detail.StartTime',
              defaultMessage: '开始执行时间',
            })}
            span={6}
          >
            <Text
              ellipsis={{
                tooltip: formatTime(taskData.startTime),
              }}
            >
              {formatTime(taskData.startTime)}
            </Text>
          </Descriptions.Item>
          <Descriptions.Item
            label={
              <ContentWithQuestion
                content={formatMessage({
                  id: 'ocp-express.Task.Detail.AccumulatedTaskTime',
                  defaultMessage: '任务累计耗时',
                })}
                tooltip={{
                  title: (
                    <div>
                      <div>
                        {formatMessage({
                          id: 'ocp-express.Task.Detail.AccumulatedTaskTimeLastRun',
                          defaultMessage:
                            '1.任务累计耗时 = 最近一次执行的结束时间 - 第一次执行的开始时间',
                        })}
                      </div>
                      <div>
                        {formatMessage({
                          id: 'ocp-express.Task.Detail.TheTimeConsumedBySubtasks',
                          defaultMessage: '2.子任务的耗时也是同一计算逻辑',
                        })}
                      </div>
                    </div>
                  ),
                }}
              />
            }
            span={isEnglish() ? 6 : 5}
          >
            <Text
              ellipsis={{
                tooltip: getTaskDuration(taskData),
              }}
            >
              {getTaskDuration(taskData)}
            </Text>
          </Descriptions.Item>
          <Descriptions.Item
            label={formatMessage({
              id: 'ocp-express.Task.Detail.TaskInitiator',
              defaultMessage: '任务发起人',
            })}
            span={isEnglish() ? 6 : 3}
            className="descriptions-item-with-ellipsis"
          >
            <Text
              ellipsis={{
                tooltip: true,
              }}
            >
              {taskData.creator?.name}
            </Text>
          </Descriptions.Item>
        </Descriptions>
      </div>

      {mode === 'log' ? (
        <Log
          ref={logRef}
          taskData={taskData}
          onOperationSuccess={refresh}
          subtask={subtask}
          log={log}
          logLoading={logLoading}
          logPolling={logPolling}
          onSubtaskChange={(newSubtaskId) => {
            setSubtaskId(newSubtaskId);
          }}
        />
      ) : (
        <Flow
          ref={flowRef}
          taskData={taskData}
          onOperationSuccess={refresh}
          subtask={subtask}
          log={log}
          logLoading={logLoading}
          logPolling={logPolling}
          onSubtaskChange={(newSubtaskId) => {
            setSubtaskId(newSubtaskId);
          }}
        />
      )}
    </PageContainer>
  );
};

export default Detail;
