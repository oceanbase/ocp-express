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
import * as ProfileService from '@/service/ocp-express/ProfileController';
import { DEFAULT_LIST_DATA } from '@/constant';
import { isURL } from '@/util';
import tracert from '@/util/tracert';

export const namespace = 'profile';

const model = {
  namespace,
  state: {
    userData: {},
    credentialListData: DEFAULT_LIST_DATA,
  },

  effects: {
    *getUserData(_, { call, put }) {
      const res = yield call(ProfileService.userInfo);
      if (res.successful) {
        const userData = res.data || {};
        // 获取当前登录用户详情后，设置 tracert 的用户标识
        tracert.set({
          roleId: `${window.location.host}_${userData.id}`,
        });
        yield put({
          type: 'update',
          payload: {
            userData,
          },
        });

        yield put({
          type: 'auth/setAuthData',
        });
      }
      return res;
    },
    *modifyUserPassword({ payload }, { call }) {
      const res = yield call(ProfileService.changePassword, payload);
      if (res.successful) {
        message.success(
          formatMessage({
            id: 'ocp-express.src.model.profile.PasswordModificationIsSuccessfulYou',
            defaultMessage: '密码修改成功，需要重新登录',
          })
        );

        const location = res.data && res.data.location;
        if (isURL(location)) {
          window.location.href = location;
        } else {
          history.push('/login');
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
