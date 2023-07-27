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

import React from 'react';

interface Props {
  data: string;
  title: string;
}

/**
 * 使用 iframe 用于展示后端返回的文件内容，例如 ASH、AWR 报告文件
 */
const FrameBox: React.FC<Props> = ({ title, data }) => {
  return (
    <iframe
      title={title}
      src={window.URL.createObjectURL(new Blob([data], { type: 'text/html' }))}
      // 高度+宽度
      style={{ width: '100%', border: '0px', height: '100vh' }}
      sandbox="allow-same-origin allow-scripts allow-popups allow-forms"
      scrolling="auto"
    />
  );
};

export default FrameBox;
