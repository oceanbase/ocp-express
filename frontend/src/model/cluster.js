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
import { message } from '@oceanbase/design';
import * as ObClusterController from '@/service/ocp-express/ObClusterController';
import { DEFAULT_LIST_DATA } from '@/constant';
import { taskSuccess } from '@/util/task';

export const namespace = 'cluster';

const model = {
  namespace,
  state: {
    clusterListData: DEFAULT_LIST_DATA,
    clusterData: {},
    zoneListData: DEFAULT_LIST_DATA,
    serverListData: DEFAULT_LIST_DATA,
    compactionLatestData: {},
    compactionSettings: {},
    parameterListData: DEFAULT_LIST_DATA,
    importPreCheckResult: {},
    importResult: {},
    clusterConfig: {},
    startupParameterList: [],
  },

  effects: {
    *getClusterData({ onSuccess }, { call, put }) {
      const res = yield call(ObClusterController.getClusterInfo, {});
      if (res.successful) {
        const clusterData = res.data || {};
        yield put({
          type: 'update',
          payload: { clusterData },
        });

        // 成功的回调函数需要在 update 之后执行，以在组件中拿到 clusterData 的最新值
        if (onSuccess) {
          onSuccess(res.data || {});
        }
      }
    },
  },

  reducers: {
    update(state, { payload }) {
      return { ...state, ...payload };
    },
  },
};

export default model;
