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
import { Tooltip, Typography, Space } from '@oceanbase/design';
import { FolderOpenOutlined } from '@ant-design/icons';

const { Text } = Typography;

interface RenderConnectionStringProps {
  connectionStrings: API.ObproxyAndConnectionString[];
  callBack: () => void;
}

const RenderConnectionString: React.FC<RenderConnectionStringProps> = ({
  connectionStrings,
  callBack,
}) => {
  if (connectionStrings?.length && connectionStrings?.length > 0) {
    const connectionString = connectionStrings[0]?.connectionString;
    return (
      <Tooltip placement="topLeft" title={connectionString}>
        <Space>
          <Text style={{ maxWidth: 180 }} ellipsis={true} copyable={true}>
            {connectionString}
          </Text>
          {connectionStrings?.length > 1 && (
            <Tooltip
              title={formatMessage({
                id: 'ocp-express.Detail.Component.RenderConnectionString.ViewMore',
                defaultMessage: '查看更多',
              })}
            >
              <a onClick={callBack}>
                <FolderOpenOutlined />
              </a>
            </Tooltip>
          )}
        </Space>
      </Tooltip>
    );
  }
  return <>-</>;
};

export default RenderConnectionString;
