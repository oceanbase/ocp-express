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

import FileSaver from 'file-saver';

/**
 * 下载日志
 * @param log      日志字符串
 * @param fileName 下载后生成的文件名
 * */
export function downloadLog(log?: string, fileName?: string) {
  const blob = new Blob([log || ''], { type: 'text/plain;charset=utf-8' });
  FileSaver.saveAs(blob, fileName || 'log.log');
}
