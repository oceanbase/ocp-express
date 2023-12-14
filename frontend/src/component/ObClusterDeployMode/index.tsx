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
import { Tag } from '@oceanbase/design';
import useStyles from './index.style';

interface ObClusterDeployModeProps {
  clusterData: API.ClusterInfo;
  // 集群部署模式的展示样式: 标签 | 文本
  mode?: 'tag' | 'text';
  className?: string;
}

const ObClusterDeployMode: React.FC<ObClusterDeployModeProps> = ({
  clusterData,
  mode = 'tag',
  className,
}: ObClusterDeployModeProps) => {
  const { styles } = useStyles();
  const deployMode = (clusterData.zones || []).map(item => ({
    regionName: item.regionName,
    serverCount: (item.servers || []).length,
  }));
  return (
    <span className={`${styles.container} ${className}`}>
      {mode === 'tag'
        ? deployMode.map(item => <Tag color="blue">{`${item.regionName} ${item.serverCount}`}</Tag>)
        : deployMode.map(item => item.serverCount).join('-')}
    </span>
  );
};

export default ObClusterDeployMode;
