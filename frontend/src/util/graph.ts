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

export function getCenterPointByGraph(graph) {
  const group = graph.get('group');
  if (group) {
    const { minX, minY, maxX, maxY } = group.getCanvasBBox();
    return {
      x: (minX + maxX) / 2,
      y: (minY + maxY) / 2,
    };
  }
  // 默认返回 undefined，这样传入 G6 时会使用默认的参数值
  return undefined;
}

/**
 * 计算字符串的长度
 * @param {string} str 指定的字符串
 * @return {number} 字符串长度
 */
export function calcStrLen(str: string) {
  let len = 0;
  // eslint-disable-next-line
  for (let i = 0; i < str.length; i++) {
    if (str.charCodeAt(i) > 0 && str.charCodeAt(i) < 128) {
      // eslint-disable-next-line
      len++;
    } else {
      len += 2;
    }
  }
  return len;
}

/**
 * 计算显示的字符串
 * @param {string} str 要裁剪的字符串
 * @param {number} maxWidth 最大宽度
 * @param {number} fontSize 字体大小
 * @return {string} 处理后的字符串
 */
export function fittingString(str: string, maxWidth: number, fontSize: number) {
  const fontWidth = fontSize * 1.3; // 字号+边距
  maxWidth *= 2; // 需要根据自己项目调整
  const width = calcStrLen(str) * fontWidth;
  const ellipsis = '…';
  if (width > maxWidth) {
    const actualLen = Math.floor((maxWidth - 10) / fontWidth);
    const result = str.substring(0, actualLen) + ellipsis;
    return result;
  }
  return str;
}
