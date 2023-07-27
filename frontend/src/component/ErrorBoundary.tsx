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
import React from 'react';
import { Alert, Button } from '@oceanbase/design';
import Empty from '@/component/Empty';

interface ErrorBoundaryProps {
  message?: React.ReactNode;
  description?: React.ReactNode;
}

interface ErrorBoundaryState {
  error?: Error | null;
  info: {
    componentStack?: string;
  };
}

class ErrorBoundary extends React.Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state = {
    error: undefined,
    info: {
      componentStack: '',
    },
  };

  componentDidCatch(error: Error | null, info: object) {
    this.setState({ error, info });
  }

  render() {
    const { message, description, children } = this.props;
    const { error, info } = this.state;
    const componentStack = info && info.componentStack ? info.componentStack : null;
    const errorMessage = typeof message === 'undefined' ? (error || '').toString() : message;
    const errorDescription = typeof description === 'undefined' ? componentStack : description;
    if (error) {
      return (
        <Empty
          spm="错误处理"
          mode="page"
          image="/assets/common/crash.svg"
          title={formatMessage({
            id: 'ocp-express.src.component.ErrorBoundary.PageExecutionError',
            defaultMessage: '页面执行出错',
          })}
          data-aspm-expo
          data-aspm-param={``}
        >
          <Button
            spm="错误处理-返回首页"
            type="primary"
            onClick={() => {
              history.push('/');
              // 刷新页面，以便重新加载应用、避免停留在当前的 ErrorBoundary 页面
              window.location.reload();
            }}
            data-aspm-expo
            data-aspm-param={``}
          >
            {formatMessage({
              id: 'ocp-express.src.component.ErrorBoundary.ReturnToHomePage',
              defaultMessage: '返回首页',
            })}
          </Button>
          {/* 展示报错详情，方便定位问题原因 */}
          <Alert
            spm="错误处理-错误详情"
            type="error"
            showIcon={true}
            message={errorMessage}
            description={errorDescription}
            style={{
              marginTop: 24,
              overflow: 'auto',
              maxHeight: '50vh',
              // 为了避免被 Empty 的水平居中样式影响，需要设置 textAlign
              textAlign: 'left',
            }}
            data-aspm-expo
            data-aspm-param={``}
          />
        </Empty>
      );
    }
    return children;
  }
}

export default ErrorBoundary;
