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

import { getLocale } from 'umi';

// 在应用挂载点后将 locale 信息以 script 标签的形式插入到报告中，以便下载的报告能以默认语言进行展示
export const insertLocaleScript = (html: string) => {
  return html.replace(
    '<div id="root"></div>',
    `<div id="root"></div>
    <script>
      window.__OCP_REPORT_LOCALE = "${getLocale() || 'zh-CN'}";
    </script>`
  );
};

// 下载文件和HTML
export const download = (content: string, fileName?: string) => {
  const blob = new Blob([content]);
  const blobUrl = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.download = fileName;
  a.href = blobUrl;
  a.click();
  a.remove();
  window.URL.revokeObjectURL(blobUrl);
};
