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

/**
 * 中文和英文、数字之间使用空格分隔，并去除中文之间的空格，以保证文本展示符合内容规范
 * @param{string} text 原始文本
 * @return{string} 格式化后的文本
 */
export function formatTextWithSpace(text?: string) {
  /* 中文和英文、数字之间使用空格分隔 */
  // 中文在前，英文和数字在后
  const regex1 = /([\u4e00-\u9fa5]+)([a-zA-Z0-9]+)/g;
  // 中文在后，英文和数字在前
  const regex2 = /([a-zA-Z0-9]+)([\u4e00-\u9fa5]+)/g;
  let str = text;
  str = str?.replace(regex1, '$1 $2');
  str = str?.replace(regex2, '$1 $2');
  /* 去除中文之间的空格 (即空白字符，包括空格、制表符、换页符等)，需要使用断言 */
  const regex3 = /([\u4e00-\u9fa5]+)\s+(?=[\u4e00-\u9fa5]+)/g;
  str = str?.replace(regex3, '$1');
  return str;
}
