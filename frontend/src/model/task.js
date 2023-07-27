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

import * as TaskController from '@/service/ocp-express/TaskController';
import { DEFAULT_LIST_DATA } from '@/constant';

export const namespace = 'task';

const model = {
  namespace,
  state: {
    runningTaskListData: DEFAULT_LIST_DATA,
    runningTaskListDataRefreshDep: null,
  },

  effects: {
    *getRunningTaskListData({ payload }, { call, put }) {
      const res = yield call(TaskController.listTaskInstances, payload);
      if (res.successful) {
        yield put({
          type: 'update',
          payload: {
            runningTaskListData: res.data || DEFAULT_LIST_DATA,
          },
        });
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
