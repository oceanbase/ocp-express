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
import { getLocale, history, getDvaApp } from 'umi';
import { message } from '@oceanbase/design';
/**
 * request 网络请求工具
 * 提供诸如参数序列号, 缓存, 超时, 字符编码处理, 错误处理等常用功能,
 */
import { extend } from 'umi-request';
import Cookies from 'js-cookie';
import tracert from '@/util/tracert';

const statusCodeMessage = {
  400: formatMessage({
    id: 'ocp-express.src.util.request.TheErrorOccurredInThe',
    defaultMessage: '发出的请求有错误，服务器没有进行新建或修改数据的操作。',
  }),

  401: formatMessage({
    id: 'ocp-express.src.util.request.TheUserIsNotLogged',
    defaultMessage: '用户未登录，或者登录使用的用户名和密码错误。',
  }),

  403: formatMessage({
    id: 'ocp-express.src.util.request.YouDoNotHaveThe',
    defaultMessage: '没有权限进行对应操作，请联系管理员。',
  }),

  404: formatMessage({
    id: 'ocp-express.src.util.request.TheRequestIsForA',
    defaultMessage: '发出的请求针对的是不存在的记录，服务器没有进行操作。',
  }),

  405: formatMessage({
    id: 'ocp-express.src.util.request.TheRequestMethodCannotBe',
    defaultMessage: '请求方法不能被用于请求相应的资源，或者请求路径不正确。',
  }),
  406: formatMessage({
    id: 'ocp-express.src.util.request.TheRequestFormatIsNot',
    defaultMessage: '请求的格式不可得。',
  }),

  410: formatMessage({
    id: 'ocp-express.src.util.request.TheRequestedResourceIsPermanently',
    defaultMessage: '请求的资源被永久删除，且不会再得到的。',
  }),

  422: formatMessage({
    id: 'ocp-express.src.util.request.AValidationErrorOccursWhen',
    defaultMessage: '当创建一个对象时，发生一个验证错误。',
  }),

  500: formatMessage({
    id: 'ocp-express.src.util.request.AnErrorOccurredOnThe',
    defaultMessage: '服务器发生错误，请检查服务器。',
  }),

  502: formatMessage({
    id: 'ocp-express.src.util.request.GatewayError',
    defaultMessage: '网关错误。',
  }),
  503: formatMessage({
    id: 'ocp-express.src.util.request.TheServiceIsUnavailableAnd',
    defaultMessage: '服务不可用，服务器暂时过载或维护。',
  }),

  504: formatMessage({
    id: 'ocp-express.src.util.request.TheGatewayTimedOut',
    defaultMessage: '网关超时。',
  }),
};

/**
 * 异常处理程序
 * response 为浏览器的 Response 对象，而 data 才是后端实际返回的响应数据
 */
const errorHandler = ({ request, response, data }) => {
  // 因为 errorHandler 不处在组件中，所以只能通过获取 dva 实例来修改 model 中的值
  const dvaApp = getDvaApp();
  const dispatch = dvaApp?._store?.dispatch;

  const {
    options: {
      HIDE_ERROR_MESSAGE,
      // 请求失败时，需要隐藏错误信息的错误码列表，根据返回的错误码是否在列表中
      // 来决定是否隐藏全局的错误提示，常用于定制化处理特定请求错误的场景
      HIDE_ERROR_MESSAGE_CODE_LIST,
      // 出现 403、404 等错误码时，是否重定向到错误页面
      SHOULD_ERROR_PAGE = true,
    } = {},
  } = request || {};
  // 当遇到 500 错误时，response 为 null，因此需要做容错处理，避免前端页面崩溃
  const { status } = response || {};
  // 401 状态为未登录情况，不展示接口错误信息，直接跳转登录页，因此需要单独处理
  if (status === 401) {
    // 未登录状态，清空 tracert 的用户标识
    tracert.set({
      roleId: null,
    });
    if (window.location.pathname !== '/login') {
      // 将当前的 pathname 和 search 记录在 state 中，以便登录后能跳转到之前访问的页面
      // 如果当前已经是登录页了，则没必要跳转
      history.push({
        pathname: '/login',
        query: {
          callback: `${window.location.pathname}${window.location.search}`,
        },
      });
    }
  } else {
    const { error = {} } = data || {};
    const { code, message: errorMessage } = error;
    // 错误展示一定要在 throw err 之前执行，否则抛错之后就无法展示了
    // 优先展示后端返回的错误信息，如果没有，则根据 status 进行展示
    const msg = errorMessage || statusCodeMessage[status];
    // 是否隐藏错误信息
    const hideErrorMessage =
      HIDE_ERROR_MESSAGE ||
      (HIDE_ERROR_MESSAGE_CODE_LIST && HIDE_ERROR_MESSAGE_CODE_LIST.includes(code));
    // 是否展示错误信息
    const showErrorMessage = !hideErrorMessage;
    // 有对应的错误信息才进行展示，避免遇到 204 等状态码(退出登录) 时，报一个空错误
    if (
      msg &&
      code !== 3010 &&
      code !== 301000 &&
      code !== 15100 &&
      code !== 15101 &&
      showErrorMessage
    ) {
      message.error(msg, 3);
    }
    // 403 状态为无权限情况，跳转到 403 页面
    if (status === 403 && SHOULD_ERROR_PAGE) {
      history.push('/error/403');
    } else if (status === 404 && SHOULD_ERROR_PAGE) {
      if (code === 15101) {
        // 15101 租户密码不存在，提示录入租户管理员密码密码。
        return dispatch({
          type: 'global/update',
          payload: {
            showTenantAdminPasswordModal: true,
            tenantAdminPasswordErrorData: {
              type: 'ADD',
              errorMessage: msg,
              ...(error?.target || {}),
            },
          },
        });
      } else {
        history.push('/error/404');
      }
    } else if (status === 500) {
      if (code === 15100) {
        return dispatch({
          type: 'global/update',
          payload: {
            showTenantAdminPasswordModal: true,
            tenantAdminPasswordErrorData: {
              type: 'EDIT',
              errorMessage: msg,
              ...(error?.target || {}),
            },
          },
        });
      }
    }
  }
  // 一定要返回 data，否则就会在错误处理这一层断掉，后续无法获取响应的数据
  return data;
};

/**
 * 配置request请求时的默认参数
 */
const request = extend({
  errorHandler, // 默认错误处理
  credentials: 'include', // 默认请求是否带上cookie
});

request.interceptors.request.use((url, options) => {
  // 由于 one-api 接口生成能力不足，需要对日志下载的接口手动设置 responseType 和 Accept 字段
  if (
    // 任务的日志下载
    url.includes('downloadDiagnosis') ||
    // 日志查询的日志下载
    url.includes('logs/download')
  ) {
    options.responseType = 'arrayBuffer';
    options.headers.Accept = '*/*';
  }

  return {
    url,
    options: {
      ...options,
      headers: {
        ...(options.headers || {}),
        // 因为 token 在每次请求中可能都会变化，因此不能在新建 request 实例时直接配置 headers (因为如果页面不刷新，headers 就不变)
        // 而是需要每次请求都单独设置，以保证总是传递最新的 CSRF Token
        'X-XSRF-TOKEN': Cookies.get('XSRF-TOKEN'),
        // umi 3.x 的 locale 插件初始化可能会在 request 插件之后，导致 getLocale 可能为空
        // 为了避免前端页面崩溃，这里对 getLocale 判断处理
        'Accept-Language': getLocale ? getLocale() : 'zh-CN',
      },
    },
  };
});

export default request;
