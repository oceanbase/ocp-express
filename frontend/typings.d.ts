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

import type { ButtonProps } from '@oceanbase/design/es/button';
import type { PopconfirmProps } from '@oceanbase/design/es/popconfirm';
import type { TooltipProps } from '@oceanbase/design/es/tooltip';

declare module '*.less';
declare module '@/config/*';

declare global {
  namespace NodeJS {
    interface ProcessEnv {
      NODE_ENV?: 'development' | 'production';
    }
  }

  interface Window {
    __OCP_REPORT_DATA?: any;
    __OCP_REPORT_LOCALE?: 'zh-CN' | 'en-US';
    Tracert: any;
  }

  namespace Global {
    interface AppInfo {
      buildVersion: string;
      buildTime: string;
    }

    interface ChartDataItem {
      value?: number;
      [key: string]: any;
    }

    type ChartData = ChartDataItem[];

    type AccessibleField = '';

    type MonitorApp = 'OB' | 'ODP' | 'HOST';

    type MonitorScope =
      | 'app'
      | 'obregion'
      | 'ob_cluster_id'
      | 'obproxy_cluster'
      | 'obproxy_cluster_id'
      | 'tenant_name'
      | 'obzone'
      | 'svr_ip'
      | 'mount_point'
      | 'mount_label'
      | 'task_type'
      | 'process'
      | 'device'
      | 'cpu';

    interface OperationItem {
      value: string;
      label: string;
      modalTitle?: string;
      divider?: boolean;
      isDanger?: boolean;
      accessibleField?:
      | 'readAccessible'
      | 'createAccessible'
      | 'updateAccessible'
      | 'deleteAccessible'
      // 下载主机日志，属于主机的通用权限
      | 'readHostAccessible'
      // 查看任务详情，属于任务的通用权限
      | 'readTaskAccessible';
      buttonProps?: ButtonProps;
      tooltip?: Omit<TooltipProps, 'overlay'>;
      popconfirm?: PopconfirmProps;
    }

    interface StatusItem {
      value: string | boolean;
      label: string;
      badgeStatus?: 'success' | 'processing' | 'error' | 'default' | 'warning';
      tagColor?: string;
      color?: string;
      operations?: OperationItem[];
      [key: string]: any;
    }
  }
}
