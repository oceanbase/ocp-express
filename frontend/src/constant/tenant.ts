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

export const TENANT_RESOURCE_METRIC_LIST = [
  {
    label: formatMessage({ id: 'ocp-express.src.constant.tenant.Disk', defaultMessage: '磁盘' }),
    value: 'disk',
    children: [
      {
        label: formatMessage({
          id: 'ocp-express.src.constant.tenant.Data',
          defaultMessage: '数据量',
        }),
        value: 'data_size',
        unit: 'G',
      },

      {
        label: formatMessage({
          id: 'ocp-express.src.constant.tenant.DiskUsage',
          defaultMessage: '磁盘占用量',
        }),

        value: 'disk_used',
        unit: 'G',
      },

      {
        label: formatMessage({
          id: 'ocp-express.src.constant.tenant.UsagePercentage',
          defaultMessage: '使用百分比',
        }),

        value: 'disk_used_percentage',
        unit: '%',
      },
    ],
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.PartitionCopy',
      defaultMessage: '分区副本',
    }),

    value: 'partition_replica',
    children: [
      {
        label: formatMessage({
          id: 'ocp-express.src.constant.tenant.PartitionCopy',
          defaultMessage: '分区副本',
        }),

        value: 'partition_replica_count',
        unit: '',
      },
    ],
  },

  {
    label: 'CPU',
    value: 'cpu',
    children: [
      {
        label: formatMessage({
          id: 'ocp-express.src.constant.tenant.Allocation',
          defaultMessage: '分配量',
        }),

        value: 'cpu_assigned',
        unit: 'G',
      },

      {
        label: formatMessage({
          id: 'ocp-express.src.constant.tenant.MaximumUsage',
          defaultMessage: '最大使用量',
        }),

        value: 'cpu_used',
        unit: 'G',
      },

      {
        label: formatMessage({
          id: 'ocp-express.src.constant.tenant.MaximumUsagePercentage',
          defaultMessage: '最大使用百分比',
        }),

        value: 'cpu_used_percentage',
        unit: '%',
      },
    ],
  },

  {
    label: formatMessage({ id: 'ocp-express.src.constant.tenant.Memory', defaultMessage: '内存' }),
    value: 'memory',
    children: [
      {
        label: formatMessage({
          id: 'ocp-express.src.constant.tenant.Allocation',
          defaultMessage: '分配量',
        }),

        value: 'memory_assigned',
        unit: 'G',
      },

      {
        label: formatMessage({
          id: 'ocp-express.src.constant.tenant.MaximumUsage',
          defaultMessage: '最大使用量',
        }),

        value: 'memory_used',
        unit: 'G',
      },

      {
        label: formatMessage({
          id: 'ocp-express.src.constant.tenant.MaximumUsagePercentage',
          defaultMessage: '最大使用百分比',
        }),

        value: 'memory_used_percentage',
        unit: '%',
      },
    ],
  },
];

// 用户 全局权限
export const DATABASE_GLOBAL_PRIVILEGE_LIST = [
  {
    value: 'ALTER',
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.AlterTablePermissions',
      defaultMessage: 'ALTER TABLE 的权限',
    }),
  },

  {
    value: 'CREATE',
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.CreateTablePermissions',
      defaultMessage: 'CREATE TABLE 的权限',
    }),
  },

  {
    value: 'DELETE',
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.DeletePermissions',
      defaultMessage: 'DELETE 的权限',
    }),
  },

  {
    value: 'DROP',
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.DropPermissions',
      defaultMessage: 'DROP 的权限',
    }),
  },

  {
    value: 'INSERT',
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.InsertPermissions',
      defaultMessage: 'INSERT 的权限',
    }),
  },

  {
    value: 'SELECT',
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.SelectPermissions',
      defaultMessage: 'SELECT 的权限',
    }),
  },

  {
    value: 'UPDATE',
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.UpdatePermissions',
      defaultMessage: 'UPDATE 的权限',
    }),
  },

  {
    value: 'INDEX',
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.CreateIndexDropIndexPermissions',
      defaultMessage: 'CREATE INDEX, DROP INDEX 的权限',
    }),
  },

  {
    value: 'CREATE_VIEW',
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.PermissionToCreateAndDelete',
      defaultMessage: '创建删除视图的权限',
    }),
  },

  {
    value: 'SHOW_VIEW',
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.HowCreateViewPermissions',
      defaultMessage: 'HOW CREATE VIEW 的权限',
    }),
  },

  {
    value: 'CREATE_USER',
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.CreateUserDropuserRenameUser',
      defaultMessage: 'CREATE USER，DROPUSER，RENAME USER 和 REVOKE ALLPRIVILEGES 的权限',
    }),
  },

  {
    value: 'PROCESS',
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.ProcessPermissions',
      defaultMessage: 'PROCESS 权限',
    }),
  },

  {
    value: 'SUPER',
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.SetGlobalPermissionToModify',
      defaultMessage: 'SET GLOBAL 修改全局系统配置的权限',
    }),
  },

  {
    value: 'SHOW_DATABASES',
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.ThePermissionsOfTheGlobal',
      defaultMessage: '全局 SHOW DATABASES 的权限。',
    }),
  },

  {
    value: 'GRANT_OPTION',
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.GrantOptionPermissions',
      defaultMessage: 'GRANT OPTION 的权限',
    }),
  },
];

// 用户数据库权限
export const DATABASE_PRIVILEGE_LIST: API.DbPrivType[] = [
  // 说明
  'ALTER', // ALTER TABLE 的权限
  'CREATE', // CREATE TABLE 的权限
  'DELETE', // DELETE 的权限
  'DROP', // DROP 的权限
  'INSERT', // INSERT  的权限
  'SELECT', // SELECT  的权限
  'UPDATE', // UPDATE  的权限
  'INDEX', // CREATE INDEX, DROP INDEX 的权限
  'CREATE_VIEW', // 创建删除视图 的权限
  'SHOW_VIEW', // SHOW CREATE VIEW 的权限
];

// 内置数据库
export const FORBID_OPERATION_DBLIST = [
  'oceanbase',
  // 'information_schema',  不能授权，隐藏掉
  'mysql',
  'SYS',
  'LBACSYS',
  'ORAAUDITOR',
];

// 内置角色
export const ORACLE_BUILT_IN_ROLE_LIST = ['CONNECT', 'RESOURCE', 'DBA', 'SYS', 'PUBLIC'];
// Oracle用户管理  系统权限
export const ORACLE_SYS_PRIVS = [
  'CREATE_SESSION', // 创建连接
  'CREATE_TABLE', // 为用户创建表
  'CREATE_ANY_TABLE', // 为任意用户创建表
  'ALTER_ANY_TABLE', // 修改任意表
  'BACKUP_ANY_TABLE', // 备份任意表
  'DROP_ANY_TABLE', // 删除任意表
  'LOCK_ANY_TABLE', // 锁定任意表
  'COMMENT_ANY_TABLE', // 在任意表添加注释
  'SELECT_ANY_TABLE', // 在任意表查询记录
  'INSERT_ANY_TABLE', // 在任意表插入记录
  'UPDATE_ANY_TABLE', // 在任意表更新记录
  'DELETE_ANY_TABLE', // 在任意表删除记录
  'FLASHBACK_ANY_TABLE',
  'CREATE_ROLE', // 创建角色
  'DROP_ANY_ROLE', // 删除任意角色
  'GRANT_ANY_ROLE', // 授予任意角色
  'ALTER_ANY_ROLE', // 修改任意角色
  'AUDIT_ANY', // 审计任意对象
  'GRANT_ANY_PRIVILEGE', // 授予任意系统权限
  'GRANT_ANY_OBJECT_PRIVILEGE', // 授予任务对象权限
  'CREATE_ANY_INDEX', // 创建任意索引
  'ALTER_ANY_INDEX', // 修改任意索引
  'DROP_ANY_INDEX', // 删除任意索引
  'CREATE_ANY_VIEW', // 创建任意视图
  'DROP_ANY_VIEW', // 删除任意视图
  'CREATE_VIEW', // 创建视图
  'SELECT_ANY_DICTIONARY',
  'CREATE_PROCEDURE', // 为用户创建存储过程
  'CREATE_ANY_PROCEDURE', // 为任意用户创建存储过程
  'ALTER_ANY_PROCEDURE', // 修改任意存储过程
  'DROP_ANY_PROCEDURE', // 删除任意存储过程
  'EXECUTE_ANY_PROCEDURE', // 执行任意存储过程
  'CREATE_SYNONYM', // 为用户创建存储过程
  'CREATE_ANY_SYNONYM', // 为任意用户创建同义词
  'DROP_ANY_SYNONYM', // 删除任意同义词
  'CREATE_PUBLIC_SYNONYM', // 创建公共同义词
  'DROP_PUBLIC_SYNONYM', // 删除公共同义词
  'CREATE_SEQUENCE', // 为用户创建序列
  'CREATE_ANY_SEQUENCE', // 为任意用户创建序列
  'ALTER_ANY_SEQUENCE', // 修改任意序列
  'DROP_ANY_SEQUENCE', // 删除任意序列
  'SELECT_ANY_SEQUENCE', // 使用任意序列
  'CREATE_TRIGGER', // 为用户创建触发器
  'CREATE_ANY_TRIGGER', // 为任意用户创建触发器
  'ALTER_ANY_TRIGGER', // 修改任意触发器
  'DROP_ANY_TRIGGER', // 删除任意触发器
  'CREATE_PROFILE', // 创建环境资源文件
  'ALTER_PROFILE', // 修改环境资源文件
  'DROP_PROFILE', // 删除环境资源文件
  'CREATE_USER', // 创建用户
  // 'BECOME_USER', // 成为另一个用户  observer 3.1 不支持 BECOME USER 权限，2.2.7 版本测试无问题
  'ALTER_USER', // 修改用户
  'DROP_USER', // 删除用户
  'CREATE_TYPE', // 为用户创建类型
  'CREATE_ANY_TYPE', // 为任意用户创建类型
  'ALTER_ANY_TYPE', // 修改任意类型
  'DROP_ANY_TYPE', // 删除任意类型
  'EXECUTE_ANY_TYPE', // 执行任意类型
  'UNDER_ANY_TYPE',
  'PURGE_DBA_RECYCLEBIN', // 清除回收站
  'CREATE_ANY_OUTLINE',
  'ALTER_ANY_OUTLINE',
  'DROP_ANY_OUTLINE',
  'SYSKM',
  'CREATE_TABLESPACE', // 创建表空间
  'ALTER_TABLESPACE', // 修改表空间
  'DROP_TABLESPACE', // 删除表空间
  'SHOW_PROCESS', // |
  'ALTER_SYSTEM', // 执行 ALTER SYSTEM 语句
  'CREATE_DATABASE_LINK', // 创建数据库链接
  'CREATE_PUBLIC_DATABASE_LINK', // 创建公共数据库链接
  'DROP_DATABASE_LINK', // 删除数据库链接
  'ALTER_SESSION', // 执行 ALTER SESSION 语句
  'ALTER_DATABASE', // 改变数据库
  'EXEMPT_REDACTION_POLICY',
  'SYSDBA',
  'SYSOPER',
  'SYSBACKUP',
];

export const ORACLE_OBJECT_TYPE_LIST = [
  {
    label: formatMessage({ id: 'ocp-express.src.constant.tenant.Table', defaultMessage: '表' }),
    value: 'TABLE',
  },

  {
    label: formatMessage({ id: 'ocp-express.src.constant.tenant.View', defaultMessage: '视图' }),
    value: 'VIEW',
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.StoredProcedure',
      defaultMessage: '存储过程',
    }),

    value: 'STORED_PROCEDURE',
  },
];

// 表权限列表（7个）
export const ORACLE_TABLE_PRIVILEGE_LIST = [
  'SELECT',
  'UPDATE',
  'INSERT',
  'DELETE',
  'ALTER',
  'INDEX',
  'REFERENCES',
];

// 视图权限列表（4个）：
export const ORACLE_VIEW_PRIVILEGE_LIST = ['SELECT', 'INSERT', 'DELETE', 'UPDATE'];
// 存储过程权限列表（1个）：
export const ORACLE_STORED_PROCEDURE_PRIVILEGE_LIST = ['EXECUTE'];

/* 租户相关 */

export const TENANT_MODE_LIST = [
  {
    value: 'MYSQL',
    label: 'MySQL',
  },
  {
    value: 'ORACLE',
    label: 'Oracle',
  },
];

export const TENANT_STATUS_LIST = [
  {
    label: formatMessage({
      id: 'ocp-express.src.constant.oceanbase.Running',
      defaultMessage: '运行中',
    }),
    value: 'NORMAL',
    badgeStatus: 'success',
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.oceanbase.Creating',
      defaultMessage: '创建中',
    }),

    value: 'CREATING',
    badgeStatus: 'processing',
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.oceanbase.OperationsIn',
      defaultMessage: '运维中',
    }),

    value: 'MODIFYING',
    badgeStatus: 'processing',
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.oceanbase.Deleting',
      defaultMessage: '删除中',
    }),

    value: 'DELETING',
    badgeStatus: 'warning',
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.Unavailable',
      defaultMessage: '不可用',
    }),

    value: 'UNAVAILABLE',
    badgeStatus: 'error',
  },
];

/** SQL 诊断详情页 PlanType */
export const PLAN_TYPE_LIST = [
  {
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.LocalExecutionPlan',
      defaultMessage: '本地执行计划',
    }),
    value: 'LOCAL',
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.RemoteExecutionPlan',
      defaultMessage: '远程执行计划',
    }),
    value: 'REMOTE',
  },

  {
    label: formatMessage({
      id: 'ocp-express.src.constant.tenant.DistributedExecutionPlan',
      defaultMessage: '分布式执行计划',
    }),
    value: 'DIST',
  },
];
