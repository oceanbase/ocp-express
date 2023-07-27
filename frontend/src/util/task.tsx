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
import React from 'react';
import { Modal, message } from '@oceanbase/design';
import { find, flatten, isArray, uniq } from 'lodash';
import moment from 'moment';
import { directTo, toPercent, joinComponent, sortByNumber, formatNumber } from '@oceanbase/util';
import * as TaskController from '@/service/ocp-express/TaskController';
import { secondToTime } from '@/util';

export function getTaskProgress(task: API.TaskInstance) {
  const subtasks = task.subtasks || [];
  const total = subtasks.length;
  const finishedCount = subtasks.filter(item => item && item.status === 'SUCCESSFUL').length;
  return `${finishedCount}/${total}`;
}

export function getTaskPercent(task: API.TaskInstance, withUnit = true) {
  const subtasks = task.subtasks || [];
  const total = subtasks.length;
  const finishedCount = subtasks.filter(item => item && item.status === 'SUCCESSFUL').length;
  // 是否带 % 单位
  return withUnit
    ? toPercent(finishedCount / total, 0)
    : // 四舍五入，不保留小数位
    Math.round((finishedCount / total) * 10000) / 100;
}

/* 获取任务或子任务的执行耗时 = 最近一次执行的结束时间 - 第一次执行的开始时间。时间单位会通过 secondToTime 自适应转换 */
export function getTaskDuration(task?: API.TaskInstance | API.SubtaskInstance) {
  // 第一次执行的开始时间
  const startTime = task?.startTime;
  // 最近一次执行的结束时间，包括成功和失败，会随着任务或子任务状态的变化而变化
  const finishTime = task?.finishTime;
  // 只有执行过，执行耗时才有意义
  if (startTime) {
    // 当任务正在执行中时，直接取当前时间，这样可以实时变化
    const milliseconds = moment(task.status === 'RUNNING' ? undefined : finishTime).diff(
      moment(startTime),
      // 由于部分子任务的执行速度非常快，因此这里获取毫秒数
      'millisecond'
    );

    return milliseconds > 0
      ? secondToTime(
        formatNumber(
          milliseconds / 1000,
          // 秒数 >= 1 仅保留整数
          // 秒数 < 1 的最多保留两位小数
          milliseconds >= 1000 ? 0 : 2
        )
      )
      : '-';
  }
  return '-';
}

export function taskSuccess({
  taskId,
  message: taskMessage = formatMessage({
    id: 'ocp-express.src.util.task.TaskSubmittedSuccessfully',
    defaultMessage: '任务提交成功',
  }),
}: {
  taskId: number | undefined | (number | undefined)[];
  message?: string;
}) {
  const taskIdList = isArray(taskId) ? taskId : [taskId];
  // 是否为多个任务
  const isMultipleTask = taskIdList.length > 1;
  Modal.success({
    closable: true,
    title: taskMessage,
    content: (
      <span>
        {isMultipleTask ? (
          <span>
            {formatMessage({
              id: 'ocp-express.src.util.task.ATaskHasBeenGenerated',
              defaultMessage: '已生成任务，任务 ID：',
            })}{' '}
            {joinComponent(taskIdList, item => (
              <a
                key={item}
                onClick={() => {
                  directTo(`/task/${item}?backUrl=/task`);
                }}
              >
                {item}
              </a>
            ))}
          </span>
        ) : (
          formatMessage(
            {
              id: 'ocp-express.src.util.task.ATaskHasBeenGenerated.1',
              defaultMessage: '已生成任务，任务 ID：{taskId}',
            },
            { taskId }
          )
        )}
      </span>
    ),

    okText: isMultipleTask
      ? formatMessage({ id: 'ocp-express.src.util.task.ISee', defaultMessage: '我知道了' })
      : formatMessage({
        id: 'ocp-express.src.util.component.ViewTasks',
        defaultMessage: '查看任务',
      }),
    okButtonProps: {
      // 关闭 loading，避免 onOk 返回 promise 时出现短暂的 loading 效果 (实际不应该 loading)
      loading: false,
    },

    onOk: () => {
      if (!isMultipleTask) {
        directTo(`/task/${taskId}?backUrl=/task`);
        // 通过模拟 promsie reject 实现击查看任务后，不关闭弹窗
        return new Promise((resolve, reject) => {
          reject();
        });
      }
      return null;
    },
  });
}

export function taskSuccessOfInspection({
  taskId,
  message: taskMessage = formatMessage({
    id: 'ocp-express.src.util.task.TaskSubmittedSuccessfully',
    defaultMessage: '任务提交成功',
  }),
  okText = formatMessage({
    id: 'ocp-express.src.util.component.ViewTasks',
    defaultMessage: '查看任务',
  }),
  onOk = () => { },
  onCancel = () => { },
}: {
  taskId: number | undefined | (number | undefined)[];
  onOk: () => void;
  onCancel?: () => void;
  message?: string;
  okText?: string;
}) {
  const taskIdList = isArray(taskId) ? taskId : [taskId];
  Modal.success({
    closable: true,
    title: taskMessage,
    content: (
      <span>
        <span>
          {formatMessage({
            id: 'ocp-express.src.util.task.ATaskHasBeenGenerated',
            defaultMessage: '已生成任务，任务 ID：',
          })}{' '}
          {joinComponent(taskIdList, item => (
            <a
              key={item}
              onClick={() => {
                directTo(`/task/${item}?backUrl=/task`);
              }}
            >
              {item}
            </a>
          ))}
        </span>
      </span>
    ),

    okText,
    okButtonProps: {
      // 关闭 loading，避免 onOk 返回 promise 时出现短暂的 loading 效果 (实际不应该 loading)
      loading: false,
    },

    onOk,
    onCancel,
  });
}

export type SubtaskOperationKey = 'stop' | 'retry' | 'skip';

/* 处理子任务的操作: 终止运行、重新运行、设置为成功 (跳过) */
export function handleSubtaskOperate(
  key: SubtaskOperationKey,
  taskData?: API.TaskInstance,
  subtask?: API.SubtaskInstance,
  onSuccess?: () => void
) {
  if (key === 'stop') {
    Modal.confirm({
      title: formatMessage({
        id: 'ocp-express.Task.Detail.TaskGraph.AreYouSureYouWant',
        defaultMessage: '确定要终止运行吗？',
      }),

      content: formatMessage({
        id: 'ocp-express.Task.Detail.TaskGraph.ThisWillTerminateTheTask',
        defaultMessage: '这将会终止当前节点的任务',
      }),

      onOk: () => {
        const promise = TaskController.cancelSubtask({
          taskInstanceId: taskData?.id,
          subtaskInstanceId: subtask?.id,
        });

        promise.then(res => {
          if (res.successful) {
            message.success(
              formatMessage({
                id: 'ocp-express.src.util.task.TheSubtaskIsTerminated',
                defaultMessage: '子任务终止成功',
              })
            );
            if (onSuccess) {
              onSuccess();
            }
          }
        });
        return promise;
      },
    });
  } else if (key === 'retry') {
    Modal.confirm({
      title: formatMessage({
        id: 'ocp-express.Task.Detail.TaskGraph.AreYouSureYouWant.1',
        defaultMessage: '确定要重新运行吗？',
      }),

      content: formatMessage({
        id: 'ocp-express.Task.Detail.TaskGraph.ThisWillReExecuteThe',
        defaultMessage: '这将会重新执行当前节点的任务',
      }),

      onOk: () => {
        const promise = TaskController.retrySubtask({
          taskInstanceId: taskData?.id,
          subtaskInstanceId: subtask?.id,
        });

        promise.then(res => {
          if (res.successful) {
            message.success(
              formatMessage({
                id: 'ocp-express.src.util.task.TheSubtaskIsRetried',
                defaultMessage: '子任务重试成功',
              })
            );
            if (onSuccess) {
              onSuccess();
            }
          }
        });
        return promise;
      },
    });
  } else if (key === 'skip') {
    Modal.confirm({
      title: formatMessage({
        id: 'ocp-express.Task.Detail.TaskGraph.AreYouSureTheSettings',
        defaultMessage: '确定设置为成功吗？',
      }),

      content: formatMessage({
        id: 'ocp-express.Task.Detail.TaskGraph.ThisSkipsTheCurrentTask',
        defaultMessage: '这将会跳过当前任务，并把任务节点的状态设为成功',
      }),

      onOk: () => {
        const promise = TaskController.skipSubtask({
          taskInstanceId: taskData?.id,
          subtaskInstanceId: subtask?.id,
        });

        promise.then(res => {
          if (res.successful) {
            message.success(
              formatMessage({
                id: 'ocp-express.src.util.task.TheSubtaskStatusIsSet',
                defaultMessage: '子任务状态已设置为成功',
              })
            );
            if (onSuccess) {
              onSuccess();
            }
          }
        });
        return promise;
      },
    });
  }
}

/**
 * 按照分隔符对任务日志进行分割
 * @param log   日志字符串
 * @param regex 匹配分隔符的正则表达式
 * */
export function splitTaskLog(log?: string) {
  const regex = /([#]{12}{[\S]*}{[\S]*}[#]{12})/;
  // 日志是顺序写入的，但日志节点是倒序展示，因此需要 reverse
  const splitList = (log || '').split(regex).reverse();
  // 日志节点列表
  const logNodeList = splitList
    .filter(item => item.match(regex))
    .map(item => {
      const innerRegex = /[#]{12}{([\S]*)}{([\S]*)}[#]{12}/;
      const match = innerRegex.exec(item);
      const type = (match && match[1]) || '';
      const timestamp = (match && match[2]) || '';
      return `${type} ${moment(timestamp).format('HH:mm:ss')}`;
    });
  // 日志列表，与日志节点的顺序关系一致
  const logList = splitList
    .filter(item => item && !item.match(regex))
    // 去掉日志的首尾空行
    .map(item => item.replace(/^[\n]+|[\n]+$/g, ''));
  return {
    logNodeList,
    logList,
  };
}

export type Node =
  | (API.SubtaskInstance & {
    children?: Node[];
  })
  | undefined;

/**
 * 根据任务获取子任务节点列表，其中分叉节点作为上一节点的 children
 * */
export function getNodes(taskData?: API.TaskInstance) {
  const subtasks = (taskData?.subtasks || [])
    // 后端返回的 tasks 列表顺序会变化，因此前端需要对子任务列表按照 id 大小进行排序，以固定列表顺序
    .sort((a, b) => sortByNumber(a, b, 'id'))
    // 还需要将上下游的子任务列表也进行排序，这样才能保证解析得到的 data 才是固定的
    .map(item => ({
      ...item,
      // 从大到小排序
      upstreams: (item.upstreams || []).sort((a, b) => a - b),
      downstreams: (item.downstreams || []).sort((a, b) => a - b),
    }));

  const nodes = [];
  // 起始节点列表
  const firstNodeList = subtasks.filter(item => !item.upstreams || item.upstreams.length === 0);
  let currentNode =
    // 如果起始节点数大于 1，则新建一个空节点作为当前节点，并将起始节点列表都作为其下游节点
    firstNodeList.length > 1
      ? {
        downstreams: firstNodeList.map(item => item.id),
      }
      : // 否则，直接将起始节点作为当前节点
      firstNodeList[0];
  let nextNode: Node;
  while (currentNode) {
    // 下游任务数大于 1，则说明存在分叉
    if ((currentNode?.downstreams?.length as number) > 1) {
      const downstreamNodes: Node[] = subtasks.filter(item =>
        currentNode?.downstreams?.includes(item.id as number)
      );

      currentNode = {
        ...currentNode,
        children: downstreamNodes,
      };

      const nextNodeIdList = uniq(flatten(downstreamNodes.map(item => item?.downstreams || [])));
      // 如果存在公共下游节点，那么就是当前节点的下一个节点
      if (nextNodeIdList.length === 1) {
        nextNode = find(subtasks, item => item.id === nextNodeIdList[0]);
      } else {
        // 如果不存在公共下游节点，则说明任务已结束
        // TODO: 这里实际上先简单处理了，因为目前 OCP 的任务还不存在子节点嵌套子节点的场景，因此未考虑递归逻辑
        nextNode = undefined;
      }
    } else {
      // 不存在分叉节点时，下游节点数要么是 1，要么是 0，第一个节点就是下一个节点
      nextNode = find(subtasks, item => item.id === currentNode?.downstreams?.[0]);
    }
    nodes.push(currentNode);
    // 下游任务数为 1，则直接 push 到节点数组上
    if (nextNode) {
      currentNode = nextNode;
    } else {
      currentNode = undefined;
    }
  }
  return nodes;
}

/**
 * 获取节点的当前进度，即获取已失败或者正在运行的第一个节点
 * */
export function getLatestNode(nodes: Node[]) {
  const flattenNodes = flatten(nodes.map(item => [item, ...(item?.children || [])]));
  // 最新的失败节点
  const latestFailedNode = find(flattenNodes, item => item?.status === 'FAILED');
  // 最新的运行中节点
  const latestRunningNode = find(flattenNodes, item => item?.status === 'RUNNING');
  // 最新的准备执行节点
  const latestReadyNode = find(flattenNodes, item => item?.status === 'READY');
  // 最新的待执行节点
  const latestPendingNode = find(flattenNodes, item => item?.status === 'PENDING');
  // 最新的已完成节点
  const latestSuccessfulNode = find(flattenNodes, item => item?.status === 'SUCCESSFUL');
  // 第一个节点
  const firstNode = flattenNodes && flattenNodes[0];
  // 最新节点的优先级: 失败节点 > 运行中节点 > 准备执行节点 > 待执行节点 > 第一个节点
  const latestNode =
    latestFailedNode ||
    latestRunningNode ||
    latestReadyNode ||
    latestPendingNode ||
    latestSuccessfulNode ||
    firstNode;
  return latestNode as Node | undefined;
}
