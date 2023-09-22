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
import { getLocale, history, useSelector } from 'umi';
import React, { useEffect } from 'react';
import { ConfigProvider, theme } from '@oceanbase/design';
import { ChartProvider } from '@oceanbase/charts';
import en_US from 'antd/es/locale/en_US';
import zh_CN from 'antd/es/locale/zh_CN';
import BlankLayout from './BlankLayout';
import ErrorBoundary from '@/component/ErrorBoundary';
import GlobalStyle from './GlobalStyle';

interface LayoutProps {
  children: React.ReactNode;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  const { themeMode } = useSelector((state: DefaultRootState) => state.global);
  console.log(themeMode);

  const locale = getLocale();
  const antdLocaleMap = {
    'en-US': en_US,
    'zh-CN': zh_CN,
  };

  useEffect(() => {
    // 设置标签页的 title
    document.title = formatMessage({
      id: 'ocp-express.config.title',
      defaultMessage: 'OceanBase 云平台',
    });
  }, []);

  return (
    <ConfigProvider
      navigate={history.push}
      locale={antdLocaleMap[locale] || zh_CN}
      theme={{
        isDark: themeMode === 'dark',
        algorithm: themeMode === 'dark' ? theme.darkAlgorithm : theme.defaultAlgorithm,
      }}
    >
      <ChartProvider theme={themeMode}>
        <GlobalStyle themeMode={themeMode} />
        <ErrorBoundary>
          <BlankLayout>{children}</BlankLayout>
        </ErrorBoundary>
      </ChartProvider>
    </ConfigProvider>
  );
};

export default Layout;
