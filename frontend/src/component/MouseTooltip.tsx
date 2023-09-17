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

import React, { useState, useRef } from 'react';
import { token } from '@oceanbase/design';
import ReactStickyMouseTooltip from 'react-sticky-mouse-tooltip';
import { useMouse, useSize } from 'ahooks';

interface MouseTooltipProps {
  children: React.ReactElement;
  overlay: React.ReactNode;
  /* 外部传入的 visible 并不完全控制显示和隐藏，只是作为是否显示的前提条件 */
  visible?: boolean;
  offsetX?: number;
  offsetY?: number;
  style?: React.CSSProperties;
}

const MouseTooltip: React.FC<MouseTooltipProps> = ({
  children,
  overlay,
  visible: outerVisible = true,
  style,
  ...restProps
}) => {
  const [visible, setVisible] = useState(false);

  // 获取鼠标位置
  const mouse = useMouse();
  const ref = useRef<HTMLDivElement>(null);
  const size = useSize(ref);
  // tooltip 宽高，由于 ref 是设置在内容区上的，因此还需要加上外部的 padding
  const tooltipWidth = (size?.width || 0) + 24;
  const tooltipHeight = (size?.height || 0) + 16;
  // 页面宽高
  const pageWidth = document.body.scrollWidth || 0;
  const pageHeight = document.body.scrollHeight || 0;

  // 避免横向超出浏览器，需要计算出横向偏移量，24 为多留出的间隙
  const offsetWidth =
    mouse.pageX + tooltipWidth > pageWidth ? mouse.pageX + tooltipWidth - pageWidth + 24 : 0;
  // 避免纵向超出浏览器，需要计算出纵向偏移量，16 为多留出的间隙
  const offsetHeight =
    mouse.pageY + tooltipHeight > pageHeight ? mouse.pageY + tooltipHeight - pageHeight + 16 : 0;

  const realChildren = React.cloneElement(children, {
    onMouseEnter: () => {
      if (outerVisible) {
        setVisible(true);
      }
    },
    onMouseLeave: () => {
      setVisible(false);
    },
  });

  return (
    <span>
      {realChildren}
      <ReactStickyMouseTooltip
        visible={visible}
        offsetX={15 - offsetWidth}
        offsetY={10 - offsetHeight}
        style={{
          // 需要大于 Popover 的 1030 index 值，否则会被遮挡
          zIndex: 1100,
          boxShadow: token.boxShadowSecondary,
          padding: '16px 24px',
          borderRadius: token.borderRadius,
          background: '#fff',
          maxWidth: 300,
          ...style,
        }}
        {...restProps}
      >
        <div ref={ref}>{overlay}</div>
      </ReactStickyMouseTooltip>
    </span>
  );
};

export default MouseTooltip;
