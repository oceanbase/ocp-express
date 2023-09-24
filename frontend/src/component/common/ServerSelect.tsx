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
import { connect } from 'umi';
import React, { useEffect } from 'react';
import { Select } from '@oceanbase/design';
import type { SelectProps } from '@oceanbase/design/es/select';
import { isNullValue } from '@oceanbase/util';
import { flatten } from 'lodash';
import { DEFAULT_LIST_DATA } from '@/constant';

export interface ServerSelectProps extends SelectProps<number | string> {
  dispatch?: any;
  loading: boolean;
  clusterId?: number;
  zoneName?: string;
  valueProp?: 'id' | 'ip';
  showAllOption?: boolean;
  // 上层组件传入的 server 列表
  realServerList?: API.Server[];
  // 根据集群 ID 获取的 server 列表
  serverList: API.Server[];
  clusterList: API.ClusterInfo[];
  allLabel?: string;
}

const { Option } = Select;

const ServerSelect: React.FC<ServerSelectProps> = ({
  dispatch,
  loading,
  clusterId,
  zoneName,
  // 用作 value 的属性字段，默认为 id
  valueProp = 'id',
  showAllOption = false,
  realServerList,
  serverList,
  clusterList,
  allLabel = formatMessage({
    id: 'ocp-express.component.common.ServerSelect.All',
    defaultMessage: '全部',
  }),
  ...restProps
}) => {
  useEffect(() => {
    // clusterId 为空，则 server 列表的获取依赖于 clusterList，因此需要额外请求集群列表，重置掉 serverList
    if (isNullValue(clusterId) && !realServerList) {
      dispatch({
        type: 'cluster/getClusterListData',
        payload: {},
      });
      resetServerList();
    } else {
      getServerList();
    }
  }, [clusterId]);

  function resetServerList() {
    dispatch({
      type: 'cluster/update',
      payload: {
        serverListData: DEFAULT_LIST_DATA,
      },
    });
  }

  function getServerList() {
    dispatch({
      type: 'cluster/getServerListData',
      payload: {
        id: clusterId,
      },
    });
  }

  // 外部传入的 server 列表优先级高于内部请求的 server 列表
  let myRealServerList = [];
  if (isNullValue(clusterId) && !realServerList) {
    const realClusterList = flatten(clusterList.map(item => [item]));
    myRealServerList = realClusterList
      ?.filter(item => !isNullValue(item?.zones))
      ?.flatMap(item => item?.zones)
      ?.filter(item => !isNullValue(item?.servers))
      ?.flatMap(item => item?.servers);
  } else if (isNullValue(clusterId) && realServerList) {
    // 没有传递集群 id，但是外部传入了 serverList ,使用外部的 serverList
    myRealServerList = realServerList;
  } else {
    myRealServerList = serverList;
  }

  return (
    <Select
      loading={loading}
      showSearch={true}
      optionFilterProp="children"
      placeholder={formatMessage({
        id: 'ocp-express.component.common.ServerSelect.SelectAServer',
        defaultMessage: '请选择服务器',
      })}
      {...restProps}
    >
      {showAllOption && <Option value="all">{allLabel}</Option>}

      {myRealServerList
        .filter(item => !zoneName || item.zoneName === zoneName)
        .map(item => (
          <Option key={item.id} value={item[valueProp]}>
            {item[valueProp]}
          </Option>
        ))}
    </Select>
  );
};

function mapStateToProps({ loading, cluster }) {
  return {
    clusterList: (cluster.clusterListData && cluster.clusterListData.contents) || [],
    serverList: (cluster.serverListData && cluster.serverListData.contents) || [],
    loading: loading.effects['cluster/getServerListData'],
  };
}

export default connect(mapStateToProps)(ServerSelect);
