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

import { formatMessage } from '@/util/intl';
import React from 'react';
import { Cascader } from '@oceanbase/design';
import type { CascaderProps, DefaultOptionType } from '@oceanbase/design/es/cascader';
import { some, uniq } from 'lodash';
import { useRequest } from 'ahooks';
import * as ObTenantController from '@/service/ocp-express/ObTenantController';
import ClusterLabel from '@/component/ClusterLabel';

export interface DataNodeType {
  value: number;
  label: React.ReactNode;
  filterLabel: string;
  children: DataNodeType[];
}

export type TenantCascaderProps = CascaderProps<DataNodeType> & {
  /* 租户模式 */
  mode?: 'ORACLE' | 'MYSQL';
  /* 自定义租户筛选 */
  filter?: (tenant?: API.Tenant) => boolean;
};

const TenantCascader: React.FC<TenantCascaderProps> = ({
  children,
  mode,
  filter,
  ...restProps
}) => {
  // 获取租户列表
  const { data, loading } = useRequest(ObTenantController.listTenants, {
    defaultParams: [{}],
  });
  const tenantList = (data?.data?.contents || [])
    .filter(item => !mode || item.mode === mode)
    .filter(item => !filter || filter(item));

  const clusterIdList = uniq(tenantList.map(item => item.clusterId));
  // 根据集群 ID 分组后的租户数据
  const options = clusterIdList
    .map(clusterId => {
      // 对应集群下的其中一个租户，用户获取集群相关信息
      const tenantOfClusterId = tenantList.find(item => item.clusterId === clusterId);
      // 对应集群下的租户列表
      const tenantListOfClusterId = tenantList.filter(item => item.clusterId === clusterId);

      return {
        value: tenantOfClusterId?.clusterId,
        label: (
          <ClusterLabel
            cluster={{
              name: tenantOfClusterId?.clusterName,
              status: tenantOfClusterId?.clusterStatus,
            }}
          />
        ),
        // 扩展出 filterLabel 属性用于筛选
        filterLabel: tenantOfClusterId?.clusterName,
        children: tenantListOfClusterId.map(item => ({
          value: item.id,
          label: item.name,
          filterLabel: item.name,
        })),
      };
    })
    .filter(item => {
      // 只显示集群下存在租户的选项
      return item.children.length > 0;
    });
  const displayRender = (labels: string[], selectedOptions: DefaultOptionType[]) => {
    return labels.map((label, i) => {
      const option = selectedOptions[i];
      if (i === labels.length - 1) {
        return <span key={option?.value}>{label}</span>;
      }

      return <span key={option?.value}>{label} / </span>;
    });
  };

  return (
    <Cascader
      loading={loading}
      expandTrigger="hover"
      showSearch={{
        filter: (inputValue, path) => {
          return some(path, item =>
            item.filterLabel?.toLowerCase().includes(inputValue?.toLowerCase())
          );
        },
      }}
      options={options}
      placeholder={formatMessage({
        id: 'ocp-express.src.component.TenantCascader.SelectATenant',
        defaultMessage: '请选择租户',
      })}
      displayRender={displayRender}
      {...restProps}
    >
      {children}
    </Cascader>
  );
};

export default TenantCascader;
