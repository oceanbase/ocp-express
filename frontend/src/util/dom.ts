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

export function getOffset(dom: HTMLElement, container: HTMLElement | null) {
  //计算x坐标
  let offsetLeft = dom.offsetLeft;
  let offsetTop = dom.offsetTop;
  let current = dom.offsetParent as HTMLElement;
  while (current !== container) {
    offsetLeft += current.offsetLeft + current.clientLeft;
    offsetTop += current.offsetTop + current.clientTop;
    current = current.offsetParent as HTMLElement;
  }
  return { offsetLeft, offsetTop };
}
