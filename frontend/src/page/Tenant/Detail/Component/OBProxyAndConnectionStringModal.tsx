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
import { Typography, Table, Button, Modal } from '@oceanbase/design';
import type { ModalProps } from '@oceanbase/design/es/modal';
import type { ColumnProps } from '@oceanbase/design/es/table';

const { Text } = Typography;

interface OBProxyAndConnectionStringModalProps extends ModalProps {
  obproxyAndConnectionStrings: API.ObproxyAndConnectionString[];
  onSuccess: () => void;
  onCancel: () => void;
  userName?: string;
}

const OBProxyAndConnectionStringModal: React.FC<OBProxyAndConnectionStringModalProps> = ({
  obproxyAndConnectionStrings,
  onSuccess,
  onCancel,
  userName,
  ...restProps
}) => {
  const columns: ColumnProps<API.ObproxyAndConnectionString>[] = [
    {
      title: 'OBProxy',
      dataIndex: 'OBProxy',
      width: 270,
      render: (value: string, record) => {
        const text = `${record?.obProxyAddress}:${record?.obProxyPort}`;
        return (
          <Text style={{ width: 240 }} ellipsis={{ tooltip: text }} copyable={{ text }}>
            {text}
          </Text>
        );
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.Detail.Component.OBProxyAndConnectionStringModal.ConnectionString',
        defaultMessage: '连接串',
      }),
      dataIndex: 'connectionString',
      width: 570,
      render: (connectionString: string) => (
        <Text
          style={{ width: 540 }}
          ellipsis={{ tooltip: connectionString }}
          copyable={{ text: connectionString }}
        >
          {connectionString}
        </Text>
      ),
    },
  ];

  return (
    <>
      <Modal
        title={
          userName
            ? formatMessage(
                {
                  id: 'ocp-express.Detail.Component.OBProxyAndConnectionStringModal.ConnectionStringOfUserUsername',
                  defaultMessage: '用户 {userName} 的连接串',
                },
                { userName }
              )
            : formatMessage({
                id: 'ocp-express.Detail.Component.OBProxyAndConnectionStringModal.ObproxyConnectionString',
                defaultMessage: 'OBProxy/连接串',
              })
        }
        destroyOnClose={true}
        onOk={() => {
          onSuccess();
        }}
        onCancel={onCancel}
        footer={
          <Button type="primary" onClick={onCancel}>
            {formatMessage({
              id: 'ocp-express.Detail.Component.OBProxyAndConnectionStringModal.Closed',
              defaultMessage: '关闭',
            })}
          </Button>
        }
        {...restProps}
      >
        <Table
          columns={columns}
          pagination={{
            size: 'small',
            pageSize: 5,
            showTotal: total =>
              formatMessage(
                {
                  id: 'ocp-express.Detail.Component.OBProxyAndConnectionStringModal.TotalTotal',
                  defaultMessage: '共 {total} 条',
                },
                { total }
              ),
          }}
          dataSource={obproxyAndConnectionStrings}
          rowKey={(record: API.ObproxyAndConnectionString) => record?.connectionString}
        />
      </Modal>
    </>
  );
};
export default OBProxyAndConnectionStringModal;
