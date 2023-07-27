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

import * as MonitorService from '@/service/ocp-express/MonitorController';
import { DEFAULT_LIST_DATA } from '@/constant';

const model = {
  namespace: 'monitor',
  state: {
    metricGroupListData: DEFAULT_LIST_DATA,
  },
  effects: {
    *getMetricGroupListData({ payload }, { call, put }) {
      const res = yield call(MonitorService.listMetricClasses, payload);
      if (res.successful) {
        yield put({
          type: 'update',
          payload: {
            metricGroupListData: res.data || [],
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
