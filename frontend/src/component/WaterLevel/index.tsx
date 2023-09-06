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
import type { ProgressProps } from 'antd/es/progress';
import styles from './index.less';
import MouseTooltip from '@/component/MouseTooltip';

export interface WaterLevelProps extends ProgressProps {
  title: React.ReactNode;
  description: React.ReactNode;
  tooltip: React.ReactNode;
}

const WaterLevel: React.FC<WaterLevelProps> = ({ title, description, tooltip, ...restProps }) => {
  return (
    <MouseTooltip
      overlay={tooltip}
      style={{
        color: 'rgba(0, 0, 0, 0.65)',
        fontSize: 14,
      }}
    >
      <Progress
        type="circle"
        width={90}
        strokeLinecap="square"
        format={() => (
          <div style={{ width: '80%', margin: '0 auto' }}>
            <div className={styles.title}>{title}</div>
            <div className={styles.description}>{description}</div>
          </div>
        )}
        {...restProps}
      />
    </MouseTooltip>
  );
};

export default WaterLevel;
