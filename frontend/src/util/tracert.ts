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

const tracert = {
  // 设置 Tracert 属性
  set(params: Record<string, any>) {
    if (window.Tracert) {
      window.Tracert.call('set', params);
    }
  },
  // 将对象解析为符合埋点参数格式的字符串
  stringify(params: Record<string, any>) {
    if (window.Tracert) {
      return window.Tracert.call('stringify', params);
    }
    return '';
  },
};

export default tracert;
