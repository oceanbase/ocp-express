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
import { message } from '@oceanbase/design';
import * as PropertyService from '@/service/ocp-express/PropertyController';
import { DEFAULT_LIST_DATA } from '@/constant';

export const namespace = 'property';

const model = {
  namespace,
  state: {
    propertyListData: DEFAULT_LIST_DATA,
  },

  effects: {
    *getPropertyListData({ payload }, { call, put }) {
      const res = yield call(PropertyService.findNonFatalProperties, payload);
      if (res.successful) {
        yield put({
          type: 'update',
          payload: {
            propertyListData: res.data || DEFAULT_LIST_DATA,
          },
        });
      }
    },
    *editProperty({ payload, onSuccess }, { call, put }) {
      const res = yield call(PropertyService.updateProperty, payload);
      if (res.successful) {
        message.success(
          formatMessage({
            id: 'ocp-express.src.model.property.ParameterUpdatedSuccessfully',
            defaultMessage: '参数更新成功',
          })
        );
        if (onSuccess) {
          onSuccess();
        }
        yield put({
          type: 'getPropertyListData',
          payload: {},
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
