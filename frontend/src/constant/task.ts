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

import { theme } from '@oceanbase/design';
import { formatMessage } from '@/util/intl';

// 任务状态列表
export const TASK_STATUS_LIST: Global.StatusItem[] = [
  {
    label: formatMessage({
      id: 'ocp-express.src.constant.task.Completed',
      defaultMessage: '已完成',
    }),
    value: 'SUCCESSFUL',
    badgeStatus: 'success',
    operations: [
      {
        value: 'downloadLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.task.DownloadLogs',
          defaultMessage: '下载日志',
        }),
      },
    ],
  },

  {
    label: formatMessage({ id: 'ocp-express.src.constant.task.Running', defaultMessage: '运行中' }),
    value: 'RUNNING',
    badgeStatus: 'processing',
    operations: [
      {
        value: 'downloadLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.task.DownloadLogs',
          defaultMessage: '下载日志',
        }),
      },
    ],
  },

  {
    label: formatMessage({ id: 'ocp-express.src.constant.task.Failed', defaultMessage: '失败' }),
    value: 'FAILED',
    badgeStatus: 'error',
    operations: [
      {
        value: 'retry',
        label: formatMessage({
          id: 'ocp-express.src.constant.task.TryAgain',
          defaultMessage: '重试',
        }),
      },

      {
        value: 'giveup',
        label: formatMessage({
          id: 'ocp-express.src.constant.task.AbandonATask',
          defaultMessage: '放弃任务',
        }),
      },

      {
        value: 'downloadLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.task.DownloadLogs',
          defaultMessage: '下载日志',
        }),
      },
    ],
  },
];

// 子任务状态列表
export const SUBTASK_STATUS_LIST: Global.StatusItem[] = [
  {
    label: formatMessage({ id: 'ocp-express.src.constant.task.Complete', defaultMessage: '完成' }),
    // 在流程图中的文本描述
    labelInGraph: formatMessage({
      id: 'ocp-express.src.constant.task.Completed',
      defaultMessage: '已完成',
    }),

    value: 'SUCCESSFUL',
    badgeStatus: 'success',
    color: theme.token.colorSuccess,
    operations: [
      {
        value: 'viewLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.task.ViewLogs',
          defaultMessage: '查看日志',
        }),
      },
    ],
  },

  {
    label: formatMessage({ id: 'ocp-express.src.constant.task.Running', defaultMessage: '运行中' }),
    labelInGraph: formatMessage({
      id: 'ocp-express.src.constant.task.TaskExecution',
      defaultMessage: '任务执行中',
    }),

    value: 'RUNNING',
    badgeStatus: 'processing',
    color: theme.token.colorPrimary,
    operations: [
      {
        value: 'viewLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.task.ViewLogs',
          defaultMessage: '查看日志',
        }),
      },

      {
        value: 'stop',
        label: formatMessage({
          id: 'ocp-express.src.constant.task.StopRunning',
          defaultMessage: '终止运行',
        }),
      },
    ],
  },

  {
    label: formatMessage({ id: 'ocp-express.src.constant.task.Failed', defaultMessage: '失败' }),
    labelInGraph: formatMessage({
      id: 'ocp-express.src.constant.task.TaskFailed',
      defaultMessage: '任务失败',
    }),

    value: 'FAILED',
    badgeStatus: 'error',
    color: theme.token.colorError,
    operations: [
      {
        value: 'viewLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.task.ViewLogs',
          defaultMessage: '查看日志',
        }),
      },

      {
        value: 'retry',
        label: formatMessage({
          id: 'ocp-express.src.constant.task.ReRun',
          defaultMessage: '重新运行',
        }),
      },

      {
        value: 'skip',
        label: formatMessage({
          id: 'ocp-express.src.constant.task.SetToSuccessful',
          defaultMessage: '设置为成功',
        }),
      },
    ],
  },

  {
    value: 'PENDING',
    label: formatMessage({
      id: 'ocp-express.src.constant.task.ToBeExecuted',
      defaultMessage: '待执行',
    }),
    labelInGraph: formatMessage({
      id: 'ocp-express.src.constant.task.ExecutionNotStarted',
      defaultMessage: '未开始执行',
    }),

    badgeStatus: 'default',
    color: '#cccccc',
    operations: [
      {
        value: 'viewLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.task.ViewLogs',
          defaultMessage: '查看日志',
        }),
      },
    ],
  },

  {
    value: 'READY',
    label: formatMessage({
      id: 'ocp-express.src.constant.task.PrepareForExecution',
      defaultMessage: '准备执行',
    }),
    labelInGraph: formatMessage({
      id: 'ocp-express.src.constant.task.PrepareForExecution',
      defaultMessage: '准备执行',
    }),
    badgeStatus: 'default',
    color: '#cccccc',
    operations: [
      {
        value: 'viewLog',
        label: formatMessage({
          id: 'ocp-express.src.constant.task.ViewLogs',
          defaultMessage: '查看日志',
        }),
      },
    ],
  },
];
