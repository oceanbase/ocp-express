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

import React, { useState } from 'react';
import { theme, Typography, TypographyProps } from '@oceanbase/design';

const { Paragraph } = Typography;

interface MyParagraphProps extends React.FC<TypographyProps> {
  children: React.ReactNode;
  style?: React.CSSProperties;
}

const MyParagraph: React.FC<MyParagraphProps> = ({ children, style }) => {
  const { token } = theme.useToken();
  const [ellipsis, setEllipsis] = useState(true);

  return (
    <div>
      <Paragraph
        style={style}
        ellipsis={
          ellipsis
            ? {
                rows: 10,
                expandable: true,
                // symbol: <DownSquareOutlined />,
              }
            : false
        }
      >
        {children}
      </Paragraph>
      <span
        style={{
          color: token.colorTextSecondary,
          cursor: 'pointer',
        }}
        onClick={() => {
          setEllipsis(!ellipsis);
        }}
      ></span>
    </div>
  );
};

export default MyParagraph;
