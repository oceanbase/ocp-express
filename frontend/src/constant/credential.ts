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

export const OPERATION_LIST = [
  {
    value: 'share',
    label: formatMessage({
      id: 'ocp-express.src.constant.credential.Share',
      defaultMessage: '分享',
    }),
  },

  {
    value: 'edit',
    label: formatMessage({
      id: 'ocp-express.src.constant.credential.Edit',
      defaultMessage: '编辑',
    }),
  },

  {
    value: 'export',
    label: formatMessage({
      id: 'ocp-express.src.constant.credential.Export',
      defaultMessage: '导出',
    }),
  },

  {
    value: 'delete',
    label: formatMessage({
      id: 'ocp-express.src.constant.credential.Delete',
      defaultMessage: '删除',
    }),
  },
];

export const TAB_LIST = [
  {
    value: 'OB',
    label: formatMessage({
      id: 'ocp-express.src.constant.credential.ObCluster',
      defaultMessage: 'OB 集群',
    }),
  },

  {
    value: 'OB_PROXY',
    label: 'OBProxy',
  },

  {
    value: 'HOST',
    label: formatMessage({
      id: 'ocp-express.src.constant.credential.Host',
      defaultMessage: '主机',
    }),
  },
];

export const HOST_CONNECTION_RESULT = [
  {
    value: 'CONNECT_FAILED',
    label: formatMessage({
      id: 'ocp-express.src.constant.credential.ConnectionFailed',
      defaultMessage: '连接失败',
    }),
  },

  {
    value: 'SUDO_NOT_ALLOWED',
    label: formatMessage({
      id: 'ocp-express.src.constant.credential.NoSudoPermission',
      defaultMessage: '无 sudo 权限',
    }),
  },

  {
    value: 'HOST_NOT_EXISTS',
    label: formatMessage({
      id: 'ocp-express.src.constant.credential.TheHostDoesNotExist',
      defaultMessage: '主机不存在',
    }),
  },
];
