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
import { isEnglish } from '@/util';

export interface OCPLogoProps {
  onClick?: (e: React.SyntheticEvent) => void;
  height?: number;
  mode?: 'default' | 'simple';
  style?: React.CSSProperties;
  className?: string;
}

const OCPLogo: React.FC<OCPLogoProps> = ({
  mode = 'default',
  height = mode === 'default' ? 80 : 24,
  style,
  ...restProps
}) => {
  const logoUrl = isEnglish()
    ? '/assets/logo/ocp_express_logo_en.svg'
    : '/assets/logo/ocp_express_logo_zh.svg';
  const simpleLogoUrl = isEnglish()
    ? '/assets/logo/ocp_express_simple_logo_en.svg'
    : '/assets/logo/ocp_express_simple_logo_zh.svg';
  return (
    <img
      src={mode === 'default' ? logoUrl : simpleLogoUrl}
      alt="logo"
      {...restProps}
      style={{
        height,
        ...(style || {}),
      }}
    />
  );
};

export default OCPLogo;
