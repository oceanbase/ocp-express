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
import { Progress } from '@oceanbase/design';
import type { ProgressProps } from '@oceanbase/design/es/progress';
import { useTheme } from '@oceanbase/charts';
import useStyles from './index.style';

export interface MyProgressProps extends ProgressProps {
  /* 外层容器的类型 */
  wrapperClassName?: string;
  /* 前缀 */
  prefix?: React.ReactNode;
  prefixWidth?: number;
  /* 后缀 */
  affix?: React.ReactNode;
  affixWidth?: number;
  prefixStyle?: boolean;
}

const MyProgress: React.FC<MyProgressProps> = ({
  wrapperClassName,
  strokeColor,
  strokeLinecap = 'square',
  prefix,
  prefixWidth = 40,
  affix,
  affixWidth = 40,
  prefixStyle,
  ...restProps
}: MyProgressProps) => {
  const { styles } = useStyles();
  const theme = useTheme();
  return (
    <span className={`${styles.progress} ${wrapperClassName}`}>
      {prefix && (
        <span className={prefixStyle ? null : styles.prefix} style={{ width: prefixWidth }}>
          {prefix}
        </span>
      )}

      <span className={styles.wrapper}>
        <Progress
          strokeColor={strokeColor || theme.defaultColor}
          strokeLinecap={strokeLinecap}
          {...restProps}
        />
      </span>
      {affix && (
        <span className={styles.affix} style={{ width: affixWidth }}>
          {affix}
        </span>
      )}
    </span>
  );
};

export default MyProgress;
