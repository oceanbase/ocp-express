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
import React, { useState } from 'react';
import { Card, Col, Row, Table, Tooltip, token } from '@oceanbase/design';
import { StatisticCard } from '@ant-design/pro-components';
import { sortByNumber } from '@oceanbase/util';
import { useRequest } from 'ahooks';
import * as TenantSessionService from '@/service/ocp-express/ObTenantSessionController';
import { getColumnSearchProps } from '@/util/component';
import MyDrawer from '@/component/MyDrawer';
import List from './List';
import useStyles from './index.style';

export interface StatisticsProps {
  tenantId: number;
}

const Statistics: React.FC<StatisticsProps> = ({ tenantId }) => {
  const [visible, setVisible] = useState(false);
  const [query, setQuery] = useState({
    dbUser: '',
    dbName: '',
    clientIp: '',
  });

  const { styles } = useStyles();

  // 获取租户的会话信息
  const { data, loading } = useRequest(
    () =>
      TenantSessionService.getSessionStats({
        tenantId,
      }),
    {
      refreshDeps: [tenantId],
    }
  );

  const {
    totalCount = 0,
    activeCount = 0,
    maxActiveTime = 0,
    userStats = [],
    clientStats = [],
    dbStats = [],
  } = (data && data.data) || {};

  const dbUserColumns = [
    {
      title: formatMessage({
        id: 'ocp-express.Detail.Session.Statistics.User',
        defaultMessage: '用户',
      }),
      dataIndex: 'dbUser',
      ellipsis: true,
      ...getColumnSearchProps({
        frontEndSearch: true,
        dataIndex: 'dbUser',
      }),
      render: (text: string) => (
        <Tooltip placement="topLeft" title={text}>
          <span>{text}</span>
        </Tooltip>
      ),
    },

    {
      title: formatMessage({
        id: 'ocp-express.Detail.Session.Statistics.ActiveNumber',
        defaultMessage: '活跃数',
      }),
      dataIndex: 'activeCount',
      sorter: (a, b) => sortByNumber(a, b, 'activeCount'),
    },

    {
      title: formatMessage({
        id: 'ocp-express.Detail.Session.Statistics.TotalNumber',
        defaultMessage: '总数',
      }),
      dataIndex: 'totalCount',
      sorter: (a, b) => sortByNumber(a, b, 'totalCount'),
      render: (text, record) => (
        <a
          onClick={() => {
            setQuery({
              ...query,
              dbUser: record.dbUser,
            });

            setVisible(true);
          }}
        >
          {text}
        </a>
      ),
    },
  ];

  const clientColumns = [
    {
      title: formatMessage({
        id: 'ocp-express.Detail.Session.Statistics.Source',
        defaultMessage: '来源',
      }),
      dataIndex: 'clientIp',
      width: 160,
      ...getColumnSearchProps({
        frontEndSearch: true,
        dataIndex: 'clientIp',
      }),
    },

    {
      title: formatMessage({
        id: 'ocp-express.Detail.Session.Statistics.ActiveNumber',
        defaultMessage: '活跃数',
      }),
      dataIndex: 'activeCount',
      sorter: (a, b) => sortByNumber(a, b, 'activeCount'),
    },

    {
      title: formatMessage({
        id: 'ocp-express.Detail.Session.Statistics.TotalNumber',
        defaultMessage: '总数',
      }),
      dataIndex: 'totalCount',
      sorter: (a, b) => sortByNumber(a, b, 'totalCount'),
      render: (text, record) => (
        <a
          onClick={() => {
            setQuery({
              ...query,
              clientIp: record.clientIp,
            });

            setVisible(true);
          }}
        >
          {text}
        </a>
      ),
    },
  ];

  const dbNameColumns = [
    {
      title: formatMessage({
        id: 'ocp-express.Detail.Session.Statistics.Database',
        defaultMessage: '数据库',
      }),
      dataIndex: 'dbName',
      ...getColumnSearchProps({
        frontEndSearch: true,
        dataIndex: 'dbName',
      }),
    },

    {
      title: formatMessage({
        id: 'ocp-express.Detail.Session.Statistics.ActiveNumber',
        defaultMessage: '活跃数',
      }),
      dataIndex: 'activeCount',
      sorter: (a, b) => sortByNumber(a, b, 'activeCount'),
    },

    {
      title: formatMessage({
        id: 'ocp-express.Detail.Session.Statistics.TotalNumber',
        defaultMessage: '总数',
      }),
      dataIndex: 'totalCount',
      sorter: (a, b) => sortByNumber(a, b, 'totalCount'),
      render: (text, record) => (
        <a
          onClick={() => {
            setQuery({
              ...query,
              dbName: record.dbName,
            });

            setVisible(true);
          }}
        >
          {text}
        </a>
      ),
    },
  ];

  // 当前查看会话详情的对象
  const target = query.dbUser || query.dbName || query.clientIp;

  return (
    <Row gutter={[16, 16]}>
      <Col span={24}>
        <StatisticCard.Group>
          <StatisticCard
            className={styles.sessionsInfo}
            statistic={{
              title: formatMessage({
                id: 'ocp-express.Detail.Session.Statistics.TotalNumberOfSessions',
                defaultMessage: '会话总数',
              }),
              value: totalCount,
            }}
          />
          <StatisticCard
            className={styles.sessionsInfo}
            statistic={{
              title: formatMessage({
                id: 'ocp-express.Detail.Session.Statistics.NumberOfActiveSessions',
                defaultMessage: '活跃会话数',
              }),
              value: activeCount,
            }}
          />
          <StatisticCard
            className={styles.sessionsInfo}
            statistic={{
              title: formatMessage({
                id: 'ocp-express.Detail.Session.Statistics.MaximumActiveSessionTimeIn',
                defaultMessage: '活跃会话最长时间（s）',
              }),
              value: maxActiveTime,
            }}
          />
        </StatisticCard.Group>
      </Col>
      <Col span={8}>
        <Card
          title={formatMessage({
            id: 'ocp-express.Detail.Session.Statistics.StatisticsByUser',
            defaultMessage: '按用户统计',
          })}
          bordered={true}
          className="card-without-padding"
        >
          <Table
            loading={loading}
            columns={dbUserColumns}
            dataSource={userStats}
            rowKey={record => record.dbUser}
            pagination={false}
            scroll={{ y: 432 }}
            style={{ minHeight: 486 }}
          />
        </Card>
      </Col>
      <Col span={8}>
        <Card
          title={formatMessage({
            id: 'ocp-express.Detail.Session.Statistics.StatisticsByAccessSource',
            defaultMessage: '按访问来源统计',
          })}
          bordered={true}
          className="card-without-padding"
        >
          <Table
            loading={loading}
            columns={clientColumns}
            dataSource={clientStats}
            rowKey={record => record.clientIp}
            pagination={false}
            scroll={{ y: 432 }}
            style={{ minHeight: 486 }}
          />
        </Card>
      </Col>
      <Col span={8}>
        <Card
          title={formatMessage({
            id: 'ocp-express.Detail.Session.Statistics.StatisticsByDatabase',
            defaultMessage: '按数据库统计',
          })}
          bordered={true}
          className="card-without-padding"
        >
          <Table
            loading={loading}
            columns={dbNameColumns}
            dataSource={dbStats}
            rowKey={record => record.dbName}
            pagination={false}
            scroll={{ y: 432 }}
            style={{ minHeight: 486 }}
          />
        </Card>
      </Col>
      <MyDrawer
        width={1232}
        title={formatMessage(
          {
            id: 'ocp-express.Detail.Session.Statistics.SessionDetailsForTarget',
            defaultMessage: '{target} 的会话详情',
          },
          { target }
        )}
        visible={visible}
        destroyOnClose={true}
        onCancel={() => {
          setVisible(false);
          setQuery({
            dbUser: '',
            dbName: '',
            clientIp: '',
          });
        }}
        footer={false}
      >
        <List mode="component" tenantId={tenantId} query={query} />
      </MyDrawer>
    </Row>
  );
};

export default Statistics;
