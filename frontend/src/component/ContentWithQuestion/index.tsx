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
import { token } from '@oceanbase/design';
import type { TooltipProps } from '@oceanbase/design/es/tooltip';
import { QuestionCircleOutlined } from '@oceanbase/icons';
import ContentWithIcon from '@/component/ContentWithIcon';

export interface ContentWithQuestionProps {
  content?: React.ReactNode;
  /* tooltip 为空，则不展示 quertion 图标和 Tooltip */
  tooltip?: TooltipProps;
  /* 是否作为 label */
  inLabel?: boolean;
  onClick?: (e: React.SyntheticEvent) => void;
  style?: React.CSSProperties;
  className?: string;
}

const ContentWithQuestion: React.FC<ContentWithQuestionProps> = ({
  content,
  tooltip,
  inLabel,
  ...restProps
}) => {
  return (
    <ContentWithIcon
      content={content}
      affixIcon={
        tooltip && {
          component: QuestionCircleOutlined,
          pointable: true,
          tooltip,
          style: {
            color: token.colorIcon,
            cursor: 'help',
            marginTop: '-4px',
          },
        }
      }
      {...restProps}
    />
  );
};

export default ContentWithQuestion;
