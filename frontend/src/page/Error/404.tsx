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
import AobException from '@/component/AobException';

export default () => {
  return (
    <AobException
      title="404"
      desc={formatMessage({
        id: 'ocp-express.page.Error.404.SorryThePageYouVisited',
        defaultMessage: '抱歉，你访问的页面不存在',
      })}
      img="/assets/common/404.svg"
      backText={formatMessage({
        id: 'ocp-express.page.Error.404.ReturnToHomePage',
        defaultMessage: '返回首页',
      })}
      onBack={() => {
        history.push(`/`);
      }}
      style={{
        paddingTop: 50,
        height: '100%',
      }}
    />
  );
};
