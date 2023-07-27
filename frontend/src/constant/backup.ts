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
import { range } from 'lodash';
import moment from 'moment';

export const AGENT_TYPE_LIST = [
  {
    label: formatMessage({ id: 'ocp-express.src.constant.backup.Backup', defaultMessage: '备份' }),
    value: 'BACKUP',
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.Recovery',
      defaultMessage: '恢复',
    }),
    value: 'RESTORE',
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.BackupAndRecovery',
      defaultMessage: '备份 + 恢复',
    }),

    value: 'BACKUP_RESTORE',
  },
];

export const AGENT_SERVICE_STATUS_LIST: Global.StatusItem[] = [
  {
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.Installing',
      defaultMessage: '安装中',
    }),
    value: 'INSTALLING',
    badgeStatus: 'processing',
    operations: [
      {
        value: 'clone',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Copy',
          defaultMessage: '复制',
        }),
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.Running',
      defaultMessage: '运行中',
    }),
    value: 'RUNNING',
    badgeStatus: 'success',
    operations: [
      {
        value: 'addMachine',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.AddNode',
          defaultMessage: '添加节点',
        }),
      },

      {
        value: 'upgrade',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.UpgradedVersion',
          defaultMessage: '升级版本',
        }),
      },

      {
        value: 'edit',
        // 更新配置，即编辑服务的配置，因此 key 值设为 edit
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.UpdateConfigurations',
          defaultMessage: '更新配置',
        }),
      },

      {
        value: 'clone',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Copy',
          defaultMessage: '复制',
        }),
      },

      {
        value: 'delete',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Delete',
          defaultMessage: '删除',
        }),
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.Upgrading',
      defaultMessage: '版本升级中',
    }),

    value: 'UPGRADING',
    badgeStatus: 'processing',
    operations: [
      {
        value: 'clone',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Copy',
          defaultMessage: '复制',
        }),
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.UpdatingConfiguration',
      defaultMessage: '配置更新中',
    }),

    value: 'UPDATING_CONFIG',
    badgeStatus: 'processing',
    operations: [
      {
        value: 'clone',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Copy',
          defaultMessage: '复制',
        }),
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.Exception',
      defaultMessage: '异常',
    }),
    value: 'ABNORMAL',
    badgeStatus: 'error',
    operations: [
      {
        // 服务无节点也属于异常状态，允许添加节点
        value: 'addMachine',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.AddNode',
          defaultMessage: '添加节点',
        }),
      },

      {
        // 异常状态下，允许更新配置
        value: 'edit',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.UpdateConfigurations',
          defaultMessage: '更新配置',
        }),
      },

      {
        value: 'clone',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Copy',
          defaultMessage: '复制',
        }),
      },

      {
        value: 'delete',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Delete',
          defaultMessage: '删除',
        }),
      },
    ],
  },
];

export const AGENT_STATUS_LIST: Global.StatusItem[] = [
  {
    label: formatMessage({ id: 'ocp-express.src.constant.backup.Online', defaultMessage: '在线' }),
    value: 'ONLINE',
    badgeStatus: 'success',
    operations: [
      {
        value: 'stop',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Stop',
          defaultMessage: '停止',
        }),
      },

      {
        value: 'restart',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Restart',
          defaultMessage: '重启',
        }),
      },

      {
        // 由于查看运维任务属于任务模块的权限，在任务页面会进行权限控制的，因此这里的入口不需要设置 accessibleField
        value: 'viewTask',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.ViewOMTasks',
          defaultMessage: '查看运维任务',
        }),
      },

      {
        value: 'uninstall',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Uninstall',
          defaultMessage: '卸载',
        }),
      },
    ],
  },

  {
    label: formatMessage({ id: 'ocp-express.src.constant.backup.Offline', defaultMessage: '离线' }),
    value: 'OFFLINE',
    badgeStatus: 'default',
    operations: [
      {
        value: 'start',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Start',
          defaultMessage: '启动',
        }),
      },

      {
        value: 'viewTask',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.ViewOMTasks',
          defaultMessage: '查看运维任务',
        }),
      },

      {
        value: 'uninstall',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Uninstall',
          defaultMessage: '卸载',
        }),
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.Deploying',
      defaultMessage: '部署中',
    }),
    value: 'DEPLOYING',
    badgeStatus: 'processing',
    operations: [
      {
        value: 'viewTask',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.ViewOMTasks',
          defaultMessage: '查看运维任务',
        }),
      },

      {
        value: 'uninstall',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Uninstall',
          defaultMessage: '卸载',
        }),
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.Upgrading',
      defaultMessage: '版本升级中',
    }),

    value: 'UPGRADING',
    badgeStatus: 'processing',
    operations: [
      {
        value: 'viewTask',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.ViewOMTasks',
          defaultMessage: '查看运维任务',
        }),
      },

      {
        value: 'uninstall',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Uninstall',
          defaultMessage: '卸载',
        }),
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.UpdatingConfiguration',
      defaultMessage: '配置更新中',
    }),

    value: 'UPDATING_CONFIG',
    badgeStatus: 'processing',
    operations: [
      {
        value: 'viewTask',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.ViewOMTasks',
          defaultMessage: '查看运维任务',
        }),
      },

      {
        value: 'uninstall',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Uninstall',
          defaultMessage: '卸载',
        }),
      },
    ],
  },
];

export const WEEK_OPTIONS = [
  {
    label: formatMessage({ id: 'ocp-express.src.constant.backup.A', defaultMessage: '一' }),
    fullLabel: formatMessage({
      id: 'ocp-express.src.constant.backup.Monday',
      defaultMessage: '星期一',
    }),
    value: 1,
  },

  {
    label: formatMessage({ id: 'ocp-express.src.constant.backup.Two', defaultMessage: '二' }),
    fullLabel: formatMessage({
      id: 'ocp-express.src.constant.backup.Tuesday',
      defaultMessage: '星期二',
    }),

    value: 2,
  },

  {
    label: formatMessage({ id: 'ocp-express.src.constant.backup.Three', defaultMessage: '三' }),
    fullLabel: formatMessage({
      id: 'ocp-express.src.constant.backup.Wednesday',
      defaultMessage: '星期三',
    }),

    value: 3,
  },

  {
    label: formatMessage({ id: 'ocp-express.src.constant.backup.Four', defaultMessage: '四' }),
    fullLabel: formatMessage({
      id: 'ocp-express.src.constant.backup.Thursday',
      defaultMessage: '星期四',
    }),

    value: 4,
  },

  {
    label: formatMessage({ id: 'ocp-express.src.constant.backup.Five', defaultMessage: '五' }),
    fullLabel: formatMessage({
      id: 'ocp-express.src.constant.backup.Friday',
      defaultMessage: '星期五',
    }),
    value: 5,
  },

  {
    label: formatMessage({ id: 'ocp-express.src.constant.backup.Six', defaultMessage: '六' }),
    fullLabel: formatMessage({
      id: 'ocp-express.src.constant.backup.Saturday',
      defaultMessage: '星期六',
    }),

    value: 6,
  },

  {
    label: formatMessage({ id: 'ocp-express.src.constant.backup.Day', defaultMessage: '日' }),
    fullLabel: formatMessage({
      id: 'ocp-express.src.constant.backup.Sunday',
      defaultMessage: '星期日',
    }),
    value: 7,
  },
];

export const MONTH_OPTIONS = range(1, 32).map(item => ({
  label: item < 10 ? `0${item}` : item,
  fullLabel: item < 10 ? `0${item}` : item,
  value: item,
}));

export const DATA_BACKUP_MODE_LIST = [
  {
    label: formatMessage({ id: 'ocp-express.src.constant.backup.Total', defaultMessage: '全量' }),
    fullLabel: formatMessage({
      id: 'ocp-express.src.constant.backup.FullBackup',
      defaultMessage: '全量备份',
    }),

    value: 'FULL_BACKUP',
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.Incremental',
      defaultMessage: '增量',
    }),
    fullLabel: formatMessage({
      id: 'ocp-express.src.constant.backup.IncrementalBackup',
      defaultMessage: '增量备份',
    }),

    value: 'INCREMENTAL_BACKUP',
  },
];

export const STORAGE_TYPE_LIST = [
  {
    value: 'BACKUP_STORAGE_FILE',
    label: 'File',
    protocol: 'file://',
  },
  {
    value: 'BACKUP_STORAGE_OSS',
    label: 'OSS',
    protocol: 'oss://',
  },

  {
    value: 'BACKUP_STORAGE_COS',
    label: 'COS',
    protocol: 'cos://',
  },
];

export const DATA_BACKUP_TASK_STATUS_LIST: Global.StatusItem[] = [
  {
    value: 'BEGINNING',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.Starting',
      defaultMessage: '启动中',
    }),
    badgeStatus: 'warning',
  },

  {
    value: 'INITIALIZING',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.Initializing',
      defaultMessage: '初始化中',
    }),

    badgeStatus: 'warning',
  },

  {
    value: 'DOING',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.InProgress',
      defaultMessage: '进行中',
    }),
    badgeStatus: 'processing',
  },

  {
    value: 'DONE',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.Completed',
      defaultMessage: '已完成',
    }),
    badgeStatus: 'success',
  },

  {
    value: 'FAILED',
    label: formatMessage({ id: 'ocp-express.src.constant.backup.Failed', defaultMessage: '失败' }),
    badgeStatus: 'error',
    operations: [
      {
        // 查看失败原因，是基于当前行数据、无需请求新的接口，因此不需要做权限控制
        value: 'viewErrorMessage',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.ViewReasons',
          defaultMessage: '查看原因',
        }),
      },
    ],
  },
];

export const LOG_BACKUP_TASK_STATUS_LIST: Global.StatusItem[] = [
  {
    value: 'BEGINNING',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.Starting',
      defaultMessage: '启动中',
    }),
    badgeStatus: 'warning',
  },

  {
    value: 'RUNNING',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.InProgress',
      defaultMessage: '进行中',
    }),
    badgeStatus: 'processing',
    operations: [
      {
        value: 'stop',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Stop',
          defaultMessage: '停止',
        }),
      },
    ],
  },

  {
    // 物理备份有效
    value: 'STOPPING',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.Stopping',
      defaultMessage: '停止中',
    }),
    badgeStatus: 'warning',
  },

  {
    value: 'STOPPED',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.Stopped',
      defaultMessage: '已停止',
    }),
    badgeStatus: 'default',
    operations: [
      {
        value: 'start',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Start',
          defaultMessage: '启动',
        }),
      },
    ],
  },

  {
    // 逻辑备份有效
    value: 'FAILED',
    label: formatMessage({ id: 'ocp-express.src.constant.backup.Failed', defaultMessage: '失败' }),
    badgeStatus: 'error',
    operations: [
      {
        value: 'viewErrorMessage',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.ViewReasons',
          defaultMessage: '查看原因',
        }),
      },
    ],
  },

  {
    // 物理备份有效
    // INTERRUPTED 为异常状态，无法重新启动任务，也没有错误原因，需要手动排查
    value: 'INTERRUPTED',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.Interrupted',
      defaultMessage: '已中断',
    }),

    badgeStatus: 'error',
    // INTERRUPTED 状态的日志备份任务允许停止
    operations: [
      {
        value: 'stop',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Stop',
          defaultMessage: '停止',
        }),
      },
    ],
  },
];

export const RESTORE_TASK_STATUS_LIST: Global.StatusItem[] = [
  {
    value: 'RESTORE_CREATE_TENANT',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.CreatingATenant',
      defaultMessage: '租户创建中',
    }),

    badgeStatus: 'processing',
  },

  {
    value: 'RESTORE_SYS_REPLICA',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.RestoringSystemReplica',
      defaultMessage: '系统副本恢复中',
    }),

    badgeStatus: 'processing',
  },

  {
    value: 'RESTORE_UPGRADE_PRE',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.PreUpgradeCheck',
      defaultMessage: '升级前检查',
    }),

    badgeStatus: 'processing',
  },

  {
    value: 'RESTORE_UPGRADE_POST',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.PostUpgradeCheck',
      defaultMessage: '升级后检查',
    }),

    badgeStatus: 'processing',
  },

  {
    value: 'RESTORE_MODIFY_SCHEMA',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.SchemaModification',
      defaultMessage: 'schema 修改中',
    }),

    badgeStatus: 'processing',
  },

  {
    value: 'RESTORE_CREATE_USER_PARTITIONS',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.CreatingUserPartitions',
      defaultMessage: '用户分区创建中',
    }),

    badgeStatus: 'processing',
  },

  {
    value: 'RESTORE_USER_REPLICA',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.RestoringUserReplicas',
      defaultMessage: '用户副本恢复中',
    }),

    badgeStatus: 'processing',
  },

  {
    value: 'RESTORE_REBUILD_INDEX',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.CreatingAnIndex',
      defaultMessage: '索引创建中',
    }),

    badgeStatus: 'processing',
  },

  {
    value: 'RESTORE_POST_CHECK',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.CheckingIndexes',
      defaultMessage: '索引检查中',
    }),

    badgeStatus: 'processing',
  },

  {
    value: 'RESTORE_SUCCESS',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.Completed',
      defaultMessage: '已完成',
    }),
    badgeStatus: 'success',
  },

  {
    value: 'RESTORE_FAIL',
    label: formatMessage({ id: 'ocp-express.src.constant.backup.Failed', defaultMessage: '失败' }),
    badgeStatus: 'error',
    operations: [
      {
        value: 'clear',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.ClearResourcePools',
          defaultMessage: '清理资源池',
        }),
      },

      {
        value: 'viewErrorMessage',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.ViewReasons',
          defaultMessage: '查看原因',
        }),
      },
    ],
  },

  // OB 4.0 新增的恢复任务状态
  {
    value: 'RESTORE_PRE',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.SetConfigurationItems',
      defaultMessage: '设置配置项',
    }),
    badgeStatus: 'processing',
  },

  {
    value: 'RESTORE_CREATE_INIT_LS',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.CreateABaselineLogStream',
      defaultMessage: '创建基线日志流',
    }),
    badgeStatus: 'processing',
  },

  {
    value: 'RESTORE_WAIT_LS',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.TheBaselineLogStreamIsBeingRestored',
      defaultMessage: '基线日志流恢复中',
    }),
    badgeStatus: 'processing',
  },

  {
    value: 'POST_CHECK',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.SetUpASecondaryTenant',
      defaultMessage: '设置备租户',
    }),
    badgeStatus: 'processing',
  },

  {
    value: 'UPGRADE',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.TenantUpgrading',
      defaultMessage: '租户升级中',
    }),
    badgeStatus: 'processing',
  },

  {
    value: 'WAIT_TENANT_RESTORE_FINISH',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.WaitForTenantRecoveryToEnd',
      defaultMessage: '等待租户恢复结束',
    }),
    badgeStatus: 'processing',
  },
];

export const SAMPLING_STRATEGY_STATUS_LIST: Global.StatusItem[] = [
  {
    value: true,
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.Running',
      defaultMessage: '运行中',
    }),
    badgeStatus: 'processing',
    operations: [
      {
        value: 'stop',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Stop',
          defaultMessage: '停止',
        }),
      },

      {
        value: 'edit',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Editing',
          defaultMessage: '编辑',
        }),
      },

      {
        value: 'delete',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Delete',
          defaultMessage: '删除',
        }),
      },
    ],
  },

  {
    value: false,
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.Stopped',
      defaultMessage: '已停止',
    }),
    badgeStatus: 'default',
    operations: [
      {
        value: 'start',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Start',
          defaultMessage: '启动',
        }),
      },

      {
        value: 'edit',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Editing',
          defaultMessage: '编辑',
        }),
      },

      {
        value: 'delete',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.Delete',
          defaultMessage: '删除',
        }),
      },
    ],
  },
];

export const BACKUP_SCHEDULE_MODE_LIST = [
  {
    value: 'WEEK',
    label: formatMessage({ id: 'ocp-express.src.constant.backup.Weeks', defaultMessage: '周' }),
  },

  {
    value: 'MONTH',
    label: formatMessage({ id: 'ocp-express.src.constant.backup.Months', defaultMessage: '月' }),
  },
];

export const BACKUP_SCHEDULE_STATUS_LIST: Global.StatusItem[] = [
  // 无备份调度意味着无备份策略
  {
    value: 'NONE',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.NoBackupScheduling',
      defaultMessage: '无备份策略',
    }),

    badgeStatus: 'default',
    operations: [
      {
        value: 'new',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.CreateABackupPolicy',
          defaultMessage: '新建备份策略',
        }),
      },

      {
        value: 'backupNow',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.BackUpNow',
          defaultMessage: '立即备份',
        }),
      },

      {
        value: 'restoreNow',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.InitiateRecovery',
          defaultMessage: '发起恢复',
        }),
      },
    ],
  },

  {
    value: 'NORMAL',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.BackupScheduling',
      defaultMessage: '备份调度中',
    }),

    badgeStatus: 'processing',
    operations: [
      {
        value: 'stop',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.PauseBackupScheduling',
          defaultMessage: '暂停备份调度',
        }),
      },

      {
        value: 'backupNow',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.BackUpNow',
          defaultMessage: '立即备份',
        }),
      },

      {
        value: 'restoreNow',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.InitiateRecovery',
          defaultMessage: '发起恢复',
        }),
      },

      {
        value: 'edit',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.EditABackupPolicy',
          defaultMessage: '编辑备份策略',
        }),
      },

      {
        value: 'delete',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.DeleteABackupPolicy',
          defaultMessage: '删除备份策略',
        }),
      },
    ],
  },

  {
    value: 'PAUSED',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.BackupSchedulingSuspended',
      defaultMessage: '备份调度已暂停',
    }),

    badgeStatus: 'warning',
    operations: [
      {
        value: 'start',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.StartBackupScheduling',
          defaultMessage: '启动备份调度',
        }),
      },

      {
        value: 'backupNow',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.BackUpNow',
          defaultMessage: '立即备份',
        }),
      },

      {
        value: 'restoreNow',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.InitiateRecovery',
          defaultMessage: '发起恢复',
        }),
      },

      {
        value: 'edit',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.EditABackupPolicy',
          defaultMessage: '编辑备份策略',
        }),
      },

      {
        value: 'delete',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.DeleteABackupPolicy',
          defaultMessage: '删除备份策略',
        }),
      },
    ],
  },

  {
    value: 'ABNORMAL',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.BackupScheduleException',
      defaultMessage: '备份调度异常',
    }),

    badgeStatus: 'error',
    operations: [
      {
        value: 'stop',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.PauseBackupScheduling',
          defaultMessage: '暂停备份调度',
        }),
      },

      {
        value: 'backupNow',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.BackUpNow',
          defaultMessage: '立即备份',
        }),
      },

      {
        value: 'restoreNow',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.InitiateRecovery',
          defaultMessage: '发起恢复',
        }),
      },

      {
        value: 'edit',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.EditABackupPolicy',
          defaultMessage: '编辑备份策略',
        }),
      },

      {
        value: 'delete',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.DeleteABackupPolicy',
          defaultMessage: '删除备份策略',
        }),
      },
    ],
  },

  {
    value: 'WAITING',
    label: formatMessage({
      id: 'ocp-express.src.constant.backup.WaitingForBackupScheduling',
      defaultMessage: '等待备份调度',
    }),

    badgeStatus: 'warning',
    operations: [
      {
        value: 'stop',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.PauseBackupScheduling',
          defaultMessage: '暂停备份调度',
        }),
      },

      {
        value: 'backupNow',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.BackUpNow',
          defaultMessage: '立即备份',
        }),
      },

      {
        value: 'restoreNow',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.InitiateRecovery',
          defaultMessage: '发起恢复',
        }),
      },

      {
        value: 'edit',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.EditABackupPolicy',
          defaultMessage: '编辑备份策略',
        }),
      },

      {
        value: 'delete',
        label: formatMessage({
          id: 'ocp-express.src.constant.backup.DeleteABackupPolicy',
          defaultMessage: '删除备份策略',
        }),
      },
    ],
  },
];

// 获取适用于 ob-ui Ranger 组件的 selects，该格式与 antd 的不同
export function getSelects() {
  return [
    {
      name: formatMessage({
        id: 'ocp-express.src.constant.backup.NearlyHours',
        defaultMessage: '近 24 小时',
      }),

      range: () => [moment().subtract(24, 'hours'), moment()],
    },

    {
      name: formatMessage({
        id: 'ocp-express.src.constant.backup.LastDays',
        defaultMessage: '近 7 天',
      }),

      range: () => [moment().subtract(7, 'days'), moment()],
    },

    {
      name: formatMessage({
        id: 'ocp-express.src.constant.backup.NearlyDays',
        defaultMessage: '近 30 天',
      }),

      range: () => [moment().subtract(30, 'days'), moment()],
    },
  ];
}
