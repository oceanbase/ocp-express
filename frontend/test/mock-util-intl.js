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

/* mock @/util/intl */
jest.mock('@/util/intl', () => {
  const reactIntl = jest.requireActual('react-intl');
  const { default: zh_CN } = require('@/locale/zh-CN.js');
  const intlInstance = reactIntl.createIntl({
    locale: 'zh-CN',
    messages: zh_CN,
  });
  return {
    formatMessage: intlInstance.formatMessage,
  };
});
