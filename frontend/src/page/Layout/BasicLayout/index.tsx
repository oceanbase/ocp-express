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
import { getLocale, history, useDispatch, useSelector } from 'umi';
import React, { useEffect, useState } from 'react';
import { Alert, Badge, Dropdown, Menu, Modal, Space, Tooltip, token } from '@oceanbase/design';
import { BasicLayout as OBUIBasicLayout } from '@oceanbase/ui';
import type { BasicLayoutProps as OBUIBasicLayoutProps } from '@oceanbase/ui/es/BasicLayout';
import { find } from 'lodash';
import moment from 'moment';
import { LoadingOutlined, UnorderedListOutlined } from '@oceanbase/icons';
import { DATE_FORMAT_DISPLAY } from '@/constant/datetime';
import { useBasicMenu } from '@/hook/useMenu';
import { useRequest } from 'ahooks';
import * as InfoController from '@/service/ocp-express/InfoController';
import * as TaskController from '@/service/ocp-express/TaskController';
import { isEnglish } from '@/util';
import { formatTime } from '@/util/datetime';
import tracert from '@/util/tracert';
import ModifyUserPasswordModal from '@/component/ModifyUserPasswordModal';
import TenantAdminPasswordModal from '@/component/TenantAdminPasswordModal';
import useStyles from './index.style';

interface BasicLayoutProps extends OBUIBasicLayoutProps {
  children: React.ReactNode;
  location: {
    pathname: string;
  };
}

const BasicLayout: React.FC<BasicLayoutProps> = props => {
  const { styles } = useStyles();
  const dispatch = useDispatch();
  const {
    themeMode,
    appInfo,
    systemInfo: { monitorInfo: { collectInterval } = {} },
    showTenantAdminPasswordModal,
    tenantAdminPasswordErrorData,
  } = useSelector((state: DefaultRootState) => state.global);
  const { userData } = useSelector((state: DefaultRootState) => state.profile);

  // 全局菜单
  const basicMenus = useBasicMenu();
  const { location, sideHeader, menus = basicMenus, children, ...restProps } = props;

  const { pathname } = location;
  // 是否展示时间差提示
  const [offsetAlertVisible, setOffsetAlertVisible] = useState(false);
  const [validating, setValidating] = useState(false);
  // 客户端与服务端的时间差 (以秒为单位)
  const [offsetSeconds, setOffsetSeconds] = useState(0);
  // 是否展示修改密码的弹窗
  const [passwordVisible, setPasswordVisible] = useState(false);

  const logoUrl = isEnglish()
    ? '/assets/logo/ocp_express_logo_en.svg'
    : '/assets/logo/ocp_express_logo_zh.svg';
  const simpleLogoUrl = isEnglish()
    ? '/assets/logo/ocp_express_simple_logo_en.svg'
    : themeMode === 'dark'
    ? '/assets/logo/ocp_express_simple_logo_zh_dark.svg'
    : '/assets/logo/ocp_express_simple_logo_zh.svg';

  useEffect(() => {
    // 获取当前登录用户数据
    dispatch({
      type: 'profile/getUserData',
    });

    // 获取公钥
    dispatch({
      type: 'global/getPublicKey',
    });

    // 获取应用信息
    dispatch({
      type: 'global/getAppInfo',
    });

    // 获取系统配置
    dispatch({
      type: 'global/getSystemInfo',
    });
    getFailedTaskList({
      status: 'FAILED',
    });
    getRunningTaskLis({
      status: 'RUNNING',
    });
  }, []);

  const { refresh, loading } = useRequest(InfoController.time, {
    onSuccess: res => {
      // 客户端时间: 取当前时间即可
      const clientDateTime = moment();
      // 服务端时间: data 为后端返回的 RFC3339 格式的时间戳字符串
      const serverDateTime = moment(res);
      const newOffsetSeconds = clientDateTime.diff(serverDateTime, 'seconds');
      setOffsetSeconds(newOffsetSeconds);
      // 如果时间差大于等于 60s，则展示 Alert
      if (Math.abs(newOffsetSeconds) >= 60) {
        setOffsetAlertVisible(true);
      }
    },
  });

  // 失败任务
  const { data: failedTaskListData, run: getFailedTaskList } = useRequest(
    TaskController.listTaskInstances,
    {
      defaultParams: [
        {
          status: 'FAILED',
        },
      ],

      pollingInterval: 10000,
    }
  );
  const failedTaskList = failedTaskListData?.data?.contents || [];

  // 正在运行中的任务
  const { data: runningTaskListData, run: getRunningTaskLis } = useRequest(
    TaskController.listTaskInstances,
    {
      defaultParams: [
        {
          status: 'RUNNING',
        },
      ],

      pollingInterval: 10000,
    }
  );
  const runningTaskList = runningTaskListData?.data?.contents || [];

  // 时间差是否超出最大限制 60 秒
  const overThreshold = Math.abs(offsetSeconds) >= 60;
  const message = overThreshold
    ? formatMessage(
        {
          id: 'ocp-express.Layout.BasicLayout.TheTimeDifferenceBetweenThe',
          defaultMessage:
            '客户端与服务器时间差过大，时间差为 {offsetSeconds} 秒。请矫正客户端或服务器时间，时间差需小于 60 秒',
        },

        { offsetSeconds }
      )
    : formatMessage(
        {
          id: 'ocp-express.Layout.BasicLayout.TheTimeBetweenTheClient',
          defaultMessage: '客户端与服务器时间已同步，时间差为 {offsetSeconds} 秒，OCP 可正常使用',
        },

        { offsetSeconds }
      );

  const handleUserMenuClick = (key: string) => {
    if (key === 'profile') {
      history.push('/settings/profile');
    } else if (key === 'modifyPassword') {
      setPasswordVisible(true);
    } else if (key === 'credential') {
      history.push('/settings/credential');
    } else if (key === 'logout') {
      Modal.confirm({
        title: formatMessage({
          id: 'ocp-express.Layout.Header.ExitLogin',
          defaultMessage: '退出登录',
        }),
        content: formatMessage({
          id: 'ocp-express.Layout.Header.AreYouSureYouWant',
          defaultMessage: '你确定要退出登录吗？退出后，需要重新登录',
        }),

        onOk: () => {
          dispatch({
            type: 'iam/logout',
          });
        },
      });
    }
  };

  const userMenu = (
    <Menu
      onClick={({ key }) => {
        handleUserMenuClick(key as string);
      }}
    >
      <Menu.Item key="modifyPassword">
        {formatMessage({
          id: 'ocp-express.Layout.Header.ChangePassword',
          defaultMessage: '修改密码',
        })}
      </Menu.Item>
      <Menu.Item key="logout">
        {formatMessage({ id: 'ocp-express.Layout.Header.ExitLogin', defaultMessage: '退出登录' })}
      </Menu.Item>
    </Menu>
  );

  const defaultOpenKey = find(menus, item =>
    // 只要子菜单的路径与 pathname 相对应，则当前菜单默认展开
    // 当前只支持一级菜单的默认展开，不过也可以满足需求了，因为现在并没有 >= 三级菜单的场景
    (item.children || []).map(child => child.link).includes(pathname)
  )?.link;

  const lightThemText = formatMessage({
    id: 'ocp-express.Layout.BasicLayout.LightThemText',
    defaultMessage: '浅色主题',
  });
  const darkThemText = formatMessage({
    id: 'ocp-express.Layout.BasicLayout.DarkThemText',
    defaultMessage: '暗黑主题',
  });
  return (
    <OBUIBasicLayout
      className={styles.container}
      data-aspm="c304179"
      data-aspm-desc="系统信息"
      data-aspm-expo
      // 扩展参数
      data-aspm-param={tracert.stringify({
        // OCP 构建版本号，格式为 1.0.0-rc.1
        ocpBuildVersion: appInfo.buildVersion,
        // OCP 版本号
        ocpVersion: appInfo.buildVersion?.split('-')?.[0],
        // OCP 语言
        ocpLocale: getLocale(),
        // OCP 主机
        ocpHost: window.location.host,
        // OCP 监控采集间隔
        ocpMonitorCollectInterval: collectInterval,
      })}
      location={location}
      banner={
        offsetAlertVisible && (
          <Alert
            message={message}
            type={overThreshold ? 'warning' : 'success'}
            banner={true}
            showIcon={true}
            icon={
              loading || validating ? (
                <LoadingOutlined style={{ color: token.colorPrimary }} />
              ) : (
                false
              )
            }
            action={
              <a
                onClick={() => {
                  // 由于接口请求较快，为了保证 loading 的展示效果，增加 1s 的持续时间
                  setValidating(true);
                  setTimeout(() => {
                    setValidating(false);
                  }, 1000);
                  refresh();
                }}
              >
                {formatMessage({
                  id: 'ocp-express.Layout.BasicLayout.VerifyAgain',
                  defaultMessage: '再次校验',
                })}
              </a>
            }
            // 时间差过大不允许关闭提示
            closable={overThreshold ? false : true}
            onClose={() => {
              setOffsetAlertVisible(false);
            }}
          />
        )
      }
      logoUrl={logoUrl}
      simpleLogoUrl={simpleLogoUrl}
      menus={menus}
      defaultOpenKeys={defaultOpenKey ? [defaultOpenKey] : []}
      sideHeader={sideHeader}
      topHeader={{
        title: (
          <div style={{ float: 'right' }}>
            <Dropdown
              overlay={
                <Menu
                  onClick={({ key }) => {
                    dispatch({
                      type: 'global/setThemeMode',
                      payload: {
                        themeMode: key,
                      },
                    });
                  }}
                >
                  <Menu.Item key="light">{lightThemText}</Menu.Item>
                  <Menu.Item key="dark">{darkThemText}</Menu.Item>
                </Menu>
              }
            >
              <span
                style={{
                  marginRight: 28,
                  fontSize: 12,
                  cursor: 'pointer',
                }}
              >
                🎉
                <span
                  style={{
                    marginLeft: 8,
                  }}
                >
                  {themeMode === 'light' ? lightThemText : darkThemText}
                </span>
              </span>
            </Dropdown>
            <Tooltip
              title={
                failedTaskList.length > 0
                  ? formatMessage(
                      {
                        id: 'ocp-express.Layout.BasicLayout.FailedTaskCount',
                        defaultMessage: '有 {failedTaskCount} 条失败任务',
                      },

                      { failedTaskCount: failedTaskList.length }
                    )
                  : runningTaskList.length > 0
                  ? formatMessage(
                      {
                        id: 'ocp-express.Layout.BasicLayout.RunningTaskCount',
                        defaultMessage: '有 {runningTaskCount} 条正在运行中的任务',
                      },

                      { runningTaskCount: runningTaskList.length }
                    )
                  : formatMessage({
                      id: 'ocp-express.Layout.BasicLayout.TaskCenter',
                      defaultMessage: '任务中心',
                    })
              }
            >
              <span
                data-aspm-click="c304248.d308744"
                data-aspm-desc="顶部导航-任务中心入口"
                onClick={() => {
                  history.push('/task');
                }}
                style={{
                  cursor: 'pointer',
                  display: 'inline-block',
                  textAlign: 'center',
                  marginRight: 8,
                  fontSize: 12,
                }}
              >
                <Badge
                  size="small"
                  offset={[4, 0]}
                  // count={failedTaskList.length || runningTaskList.length}
                  style={{
                    backgroundColor:
                      // 失败任务，展示红色圆点
                      failedTaskList.length > 0
                        ? token.colorError
                        : // 存在执行中的任务，展示蓝色圆点
                        runningTaskList.length > 0
                        ? token.colorPrimary
                        : undefined,
                  }}
                >
                  <Space>
                    <UnorderedListOutlined
                      style={
                        {
                          // color: '#5c6b8a',
                        }
                      }
                    />

                    <span
                      style={{
                        // color: token.colorTextTertiary,
                        fontSize: 12,
                      }}
                    >
                      任务
                    </span>
                  </Space>
                </Badge>
              </span>
            </Tooltip>
          </div>
        ),

        username: userData.username,
        userMenu,
        showLocale: true,
        locales: ['zh-CN', 'en-US'],
        appData: {
          shortName: 'OCP Express',
          version: appInfo.buildVersion,
          releaseTime: formatTime(appInfo.buildTime, DATE_FORMAT_DISPLAY),
        },
      }}
      {...restProps}
    >
      {children}

      <ModifyUserPasswordModal
        visible={passwordVisible}
        isSelf={true}
        userData={userData}
        onCancel={() => {
          setPasswordVisible(false);
        }}
        onSuccess={() => {
          setPasswordVisible(false);
        }}
      />

      <TenantAdminPasswordModal
        visible={showTenantAdminPasswordModal}
        type={tenantAdminPasswordErrorData?.type}
        errorMessage={tenantAdminPasswordErrorData?.errorMessage}
        tenantName={tenantAdminPasswordErrorData?.tenantName}
        onCancel={() => {
          dispatch({
            type: 'global/update',
            payload: {
              showTenantAdminPasswordModal: false,
              tenantAdminPasswordErrorData: {},
            },
          });
        }}
        onSuccess={() => {
          dispatch({
            type: 'global/update',
            payload: {
              showTenantAdminPasswordModal: false,
              tenantAdminPasswordErrorData: {},
            },
          });
        }}
      />
    </OBUIBasicLayout>
  );
};

export default BasicLayout;
