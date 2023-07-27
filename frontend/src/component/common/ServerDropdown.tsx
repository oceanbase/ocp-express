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
import React, { useEffect, useState } from 'react';
import { connect } from 'umi';
import { Dropdown, Menu } from 'antd';
import { isNullValue } from '@oceanbase/util';
import { DEFAULT_LIST_DATA, ALL } from '@/constant';
import { DownOutlined } from '@ant-design/icons';
import { noop } from 'lodash';

export interface ServerDropdownProps {
  dispatch?: any;
  clusterId?: number;
  // 上层组件传入的 server 列表
  realServerList?: API.Server[];
  // 根据集群 ID 获取的 server 列表
  serverList: API.Server[];
  onChange?: (server: API.Server | null) => void;
  value?: { id: string };
  allLabel?: string;
}

const ServerDropdown: React.FC<ServerDropdownProps> = ({
  dispatch,
  clusterId,
  realServerList,
  serverList,
  onChange = noop,
  value = {},
  allLabel = formatMessage({
    id: 'ocp-express.component.common.ServerDropdown.All',
    defaultMessage: '全部',
  }),
}) => {
  const [serverId, setServerId] = useState<string>(ALL);

  useEffect(() => {
    if (!realServerList) {
      if (isNullValue(clusterId)) {
        resetServerList();
      } else {
        getServerList();
      }
    }
  }, [clusterId]);

  useEffect(() => {
    if (value?.id) {
      setServerId(`${value.id}`);
    }
  }, [value]);

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

  const serverChange = (server: API.Server | null) => {
    setServerId(server ? `${server.id}` : ALL);
    onChange(server);
  };

  const myRealServerList = realServerList || serverList;

  return (
    <Dropdown
      overlayStyle={{ marginRight: 24 }}
      overlay={
        <Menu selectedKeys={[serverId]}>
          <Menu.Item key={ALL} onClick={() => serverChange(null)}>
            {allLabel}
          </Menu.Item>
          {myRealServerList.map(server => (
            <Menu.Item key={`${server.id}`} onClick={() => serverChange(server)}>
              {server.ip}
            </Menu.Item>
          ))}
        </Menu>
      }
    >
      <span style={{ cursor: 'pointer' }}>
        {serverId === ALL
          ? allLabel
          : myRealServerList.find(server => `${server.id}` === serverId)?.ip || '-'}
        <DownOutlined style={{ marginLeft: 8 }} />
      </span>
    </Dropdown>
  );
};

function mapStateToProps({ cluster }) {
  return {
    serverList: (cluster.serverListData && cluster.serverListData.contents) || [],
  };
}

export default connect(mapStateToProps)(ServerDropdown);
