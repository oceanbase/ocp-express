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

import { createIntl } from 'react-intl';
// import { getLocale } from 'umi';
import en_US from '@/locale/en-US';
import zh_CN from '@/locale/zh-CN';

const messages = {
  'en-US': en_US,
  'zh-CN': zh_CN,
};

export const getLocale = () => {
  const lang =
    typeof localStorage !== 'undefined'
      ? window.localStorage.getItem('umi_locale')
      : '';
  return lang || 'zh-CN';
};
export const locale = getLocale();

// const locale = getLocale();

const intl = createIntl({
  locale,
  messages: messages[locale],
});

export const { formatMessage } = intl;
export default intl;
