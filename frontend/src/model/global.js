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

import * as InfoController from '@/service/ocp-express/InfoController';
import * as IamController from '@/service/ocp-express/IamController';
import * as PropertyController from '@/service/ocp-express/PropertyController';

export const namespace = 'global';

const model = {
  namespace,
  state: {
    // RSA 加密用的公钥
    publicKey: '',
    // 应用信息
    appInfo: {},
    // 系统配置
    systemInfo: {},
    showTenantAdminPasswordModal: false,
    showCredentialModal: false,
    tenantAdminPasswordErrorData: {},
  },
  effects: {
    *getPublicKey(_, { call, put }) {
      const res = yield call(IamController.getLoginKey);
      yield put({
        type: 'update',
        payload: {
          publicKey: res.data?.publicKey || '',
        },
      });
    },
    // 获取应用信息
    *getAppInfo(_, { call, put }) {
      const res = yield call(InfoController.info);
      yield put({
        type: 'update',
        payload: {
          // /api/v1/info 接口的 res 并没有 data 字段，res 本身就包含实际数据
          appInfo: res || {},
        },
      });
    },
    // 获取系统配置
    *getSystemInfo(_, { call, put }) {
      const res = yield call(PropertyController.getSystemInfo);
      yield put({
        type: 'update',
        payload: {
          systemInfo: res.data || {},
        },
      });
    },
  },
  reducers: {
    update(state, { payload }) {
      return { ...state, ...payload };
    },
  },
};

export default model;
