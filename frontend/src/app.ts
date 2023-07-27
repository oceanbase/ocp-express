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

export const dva = {
  config: {
    // 在 onError 函数当中做的处理，需要同步到 UseRequestProvider（全局搜索）当中进行相同处理。使用 useRequest 抛的错暂时不能被 onError 函数捕获到。
    onError(err: ErrorEvent, dispatch) {
      // 用来捕获全局错误，避免页面崩溃
      if (err.preventDefault) {
        err.preventDefault();
      }
      message.error(
        formatMessage(
          {
            id: 'ocp-express.src.app.DvaErrorErrmessage',
            defaultMessage: 'dva error：{errMessage}',
          },
          { errMessage: err.message }
        ),
        3
      );
      // 3010 为新版接口找不到密码箱连接的错误码
      if (err.message === '3010') {
        dispatch({
          type: 'global/update',
          payload: {
            showCredentialModal: true,
            credentialErrorData: err?.data || {},
          },
        });
      } else if (err.message === '301000') {
        // 301000 为旧版接口找不到密码箱连接的错误码
        dispatch({
          type: 'global/update',
          payload: {
            showCredentialModal: true,
          },
        });
      }
    },
  },
};
