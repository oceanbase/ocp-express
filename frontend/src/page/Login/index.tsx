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
import { history, useDispatch, useSelector } from 'umi';
import React, { useState, useEffect } from 'react';
import { message } from '@oceanbase/design';
import { Login } from '@oceanbase/ui';
import { isNullValue } from '@oceanbase/util';
import { useRequest } from 'ahooks';
import * as IamController from '@/service/ocp-express/IamController';
import * as ProfileController from '@/service/ocp-express/ProfileController';
import useDocumentTitle from '@/hook/useDocumentTitle';
import { isEnglish } from '@/util';
import encrypt from '@/util/encrypt';

interface LoginPageProps {
  location: {
    query: {
      callback?: string;
    };
  };
}

interface Values {
  username: string;
  password: string;
}

const LoginPage: React.FC<LoginPageProps> = ({
  location: {
    query: { callback },
  },
}) => {
  const [alertMessage, setAlertMessage] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [showActivate, setShowActivate] = useState(false);
  const dispatch = useDispatch();

  useDocumentTitle(formatMessage({ id: 'ocp-express.page.Login.Login', defaultMessage: '登录' }));

  // 进入登录页，获取一次当前登录用户数据
  useEffect(() => {
    dispatch({
      type: 'profile/getUserData',
    }).then(res => {
      // 若已登录，则直接跳转到 callback
      if (res.successful && !isNullValue(res.data?.id)) {
        handleCallback();
      }
    });
  }, []);

  // callback 参数处理
  const handleCallback = () => {
    if (callback) {
      history.push(callback.startsWith('/login') ? '/' : callback);
    } else {
      history.push('/');
    }
  };

  const loading = useSelector((state: DefaultRootState) => state.loading.effects['iam/login']);

  const { runAsync: getLoginKey, loading: getLoginKeyLoading } = useRequest(
    IamController.getLoginKey,
    {
      manual: true,
    }
  );

  // 修改密码
  const { run: changePassword, loading: changePasswordLoading } = useRequest(
    ProfileController.changePassword,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage({
              id: 'ocp-express.page.Login.ThePasswordHasBeenModifiedPleaseLogOn',
              defaultMessage: '密码修改成功，请重新登录',
            })
          );

          setShowActivate(false);
        }
      },
    }
  );

  const login = (values: Values) => {
    const { username, password } = values;
    // 先获取 RSA 公钥，再用公钥加密 password
    // 这里不采用预先获取存储到全局状态，而是使用即时请求的方式，否则在登录页停留太久
    // OCP Server 重启或者手动配置都可能会导致公钥更新，导致登录 (高频操作) 失败
    getLoginKey().then(response => {
      const publicKey = response?.data?.publicKey || '';
      dispatch({
        type: 'iam/login',
        payload: {
          username,
          password: encrypt(password, publicKey),
        },

        onSuccess: (res: any) => {
          const { location, needChangePassword } = (res && res.data) || {};
          if (res.successful) {
            setAlertMessage('');
            // 强制修改密码
            if (needChangePassword) {
              setCurrentPassword(password);
              setShowActivate(true);
            } else if (callback) {
              // 优先级: callback > location
              // callback: 前端记录的上一次访问的页面路径，支持历史库等第三方登录对接
              // location: 后端记录的上一次访问的页面路径
              handleCallback();
            } else if (location) {
              if (location === '/doc/swagger-ui.html' || location.startsWith('/docs')) {
                // 由于接口文档和用户文档均不是主应用的页面，使用 history.push 跳转后路由无法匹配，需要使用 location.href 进行跳转 (会刷新整个页面)
                window.location.href = location;
              } else if (
                location.startsWith('/login') ||
                // 如果之前是接口请求，则登陆后默认跳转到首页
                location.startsWith('/api') ||
                location.startsWith('/services')
              ) {
                history.push('/overview');
              } else {
                history.push(location);
              }
            } else {
              // 跳转逻辑兜底
              history.push('/overview');
            }
          }
        },
        onFail: (res: any) => {
          const errorMessage = res && res.error && res.error.message;
          setAlertMessage(errorMessage);
        },
      });
    });
  };

  const modifyUserPassword = (values: { confirmPassword: string }) => {
    getLoginKey().then(response => {
      const publicKey = response?.data?.publicKey || '';
      changePassword({
        currentPassword: encrypt(currentPassword, publicKey),
        newPassword: encrypt(values?.confirmPassword, publicKey),
      });
    });
  };

  return (
    <Login
      logo={`/assets/logo/${isEnglish() ? 'ocp_express_logo_en.svg' : 'ocp_express_logo_zh.svg'}`}
      bgImage="/assets/login/background_img.svg"
      title={
        <div>
          Welcome to <div>OCP Express !</div>
        </div>
      }
      description="Let's start a happy journey"
      showLocale={true}
      locales={['zh-CN', 'en-US']}
      showActivate={showActivate}
      onShowActivateChange={() => {
        setShowActivate(false);
      }}
      alertProps={{
        message: alertMessage,
      }}
      loginProps={{
        loading: getLoginKeyLoading || loading,
        onFinish: login,
      }}
      activateFormProps={{
        onFinish: values => {
          modifyUserPassword(values);
        },
        loading: getLoginKeyLoading || changePasswordLoading,
      }}
    />
  );
};

export default LoginPage;
