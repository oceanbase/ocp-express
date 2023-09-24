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

import React, { useEffect } from 'react';
import { connect } from 'umi';
import { Dropdown, Menu } from '@oceanbase/design';
import type { DropDownProps } from '@oceanbase/design/es/dropdown';
import { isNullValue, findBy } from '@oceanbase/util';
import { DEFAULT_LIST_DATA } from '@/constant';

export interface TenantDropdownProps extends DropDownProps {
  dispatch: any;
  clusterId?: number;
  cluster?: string;
  valueProp?: 'id' | 'name';
  onMenuClick?: (key: string) => void;
  children: React.ReactNode;
  tenantList: API.TenantInfo[];
  clusterList: API.ClusterInfo[];
}

const TenantDropdown: React.FC<TenantDropdownProps> = ({
  dispatch,
  clusterId,
  // 集群名
  cluster,
  // 用作 value 的属性字段，默认为 id
  valueProp = 'id',
  onMenuClick,
  children,
  tenantList,
  clusterList,
  ...restProps
}) => {
  const realClusterId = isNullValue(clusterId)
    ? findBy(clusterList, 'name', cluster).id
    : clusterId;
  useEffect(() => {
    if (isNullValue(realClusterId)) {
      resetTenantList();
    } else {
      getTenantList();
    }
  }, [realClusterId]);

  function resetTenantList() {
    dispatch({
      type: 'tenant/update',
      payload: {
        tenantListData: DEFAULT_LIST_DATA,
      },
    });
  }

  function getTenantList() {
    dispatch({
      type: 'tenant/getTenantListData',
      payload: {
        id: realClusterId,
      },
    });
  }
  const menu = (
    <Menu
      onClick={({ key }) => {
        if (onMenuClick) {
          onMenuClick(key);
        }
      }}
    >
      {tenantList.map(item => (
        <Menu.Item key={item[valueProp]}>{item.name}</Menu.Item>
      ))}
    </Menu>
  );
  return (
    <Dropdown overlay={menu} {...restProps}>
      {children}
    </Dropdown>
  );
};

function mapStateToProps({ cluster, tenant }) {
  return {
    clusterList: (cluster.clusterListData && cluster.clusterListData.contents) || [],
    tenantList: (tenant.tenantListData && tenant.tenantListData.contents) || [],
  };
}

export default connect(mapStateToProps)(TenantDropdown);
