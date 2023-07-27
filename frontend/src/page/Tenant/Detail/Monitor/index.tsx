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
import { connect, history } from 'umi';
import React, { useEffect } from 'react';
import { Card, Col, Row } from '@oceanbase/design';
import { flatten } from 'lodash';
import { PageContainer } from '@ant-design/pro-components';
import { findBy } from '@oceanbase/util';
import { useRequest } from 'ahooks';
import * as ObTenantController from '@/service/ocp-express/ObTenantController';
import ContentWithReload from '@/component/ContentWithReload';
import MonitorSearch from '@/component/MonitorSearch';
import MetricChart from '@/component/MetricChart';

interface MonitorProps {
  location: {
    query: {
      tab: string;
    };
  };

  match: {
    params: {
      tenantId: number;
    };
  };

  dispatch: any;
  loading: boolean;
  metricGroupList: API.MetricClass[];
}

const Monitor: React.FC<MonitorProps> = ({
  location,
  match: {
    params: { tenantId },
  },
  dispatch,
  loading,
  metricGroupList,
}) => {
  const { query } = location || {};
  const { tab } = query || {};
  const activeKey = tab || metricGroupList[0]?.key;
  const queryData = MonitorSearch.getQueryData(query);

  const { data, loading: listTenantsLoading } = useRequest(ObTenantController.listTenants, {
    defaultParams: [{}],
  });

  const tenants = data?.data?.contents || [];

  const tenantData = tenants.find(item => item.obTenantId === Number(tenantId)) || {};

  const zoneNameList = tenantData.zones?.map(item => item.name);
  const serverList = flatten(
    tenantData.zones?.map(
      item =>
        // 根据租户的 unit 分布获取 server 列表
        item.units?.map(unit => ({
          // 需要 ip 加 port 来区分 server
          ip: `${unit.serverIp}:${unit.serverPort}`,
          zoneName: unit.zoneName,
          // TODO: 待接口扩展 hostId 信息
          hostId: unit.hostId,
        })) || []
    ) || []
  );

  useEffect(() => {
    getMetricGroupList();
  }, []);

  function getMetricGroupList() {
    dispatch({
      type: 'monitor/getMetricGroupListData',
      payload: {
        scope: 'TENANT',
      },
    });
  }

  return (
    <PageContainer
      ghost={true}
      loading={listTenantsLoading}
      header={{
        title: (
          <ContentWithReload
            content={formatMessage({
              id: 'ocp-express.Detail.Monitor.PerformanceMonitoring',
              defaultMessage: '性能监控',
            })}
            spin={loading}
            onClick={() => {
              getMetricGroupList();
            }}
          />
        ),
      }}
    >
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <MonitorSearch location={location} zoneNameList={zoneNameList} serverList={serverList} />
        </Col>
        <Col span={24}>
          <Card
            className="card-without-padding card-with-grid-card"
            bordered={false}
            activeTabKey={activeKey}
            onTabChange={key => {
              history.push({
                pathname: `/tenant/${tenantId}/monitor`,
                query: {
                  ...query,
                  tab: key,
                },
              });
            }}
            tabList={metricGroupList.map(item => ({
              key: item.key,
              tab: item.name,
            }))}
          >
            <MetricChart
              metricGroupList={findBy(metricGroupList, 'key', activeKey).metricGroups || []}
              app="OB"
              tenantName={tenantData.name}
              clusterName={tenantData.clusterName}
              obClusterId={tenantData.obClusterId}
              zoneNameList={zoneNameList}
              serverList={serverList}
              {...queryData}
            />
          </Card>
        </Col>
      </Row>
    </PageContainer>
  );
};

function mapStateToProps({ loading, monitor }) {
  return {
    loading: loading.effects['monitor/getMetricGroupListData'],
    metricGroupList: (monitor.metricGroupListData && monitor.metricGroupListData.contents) || [],
  };
}

export default connect(mapStateToProps)(Monitor);
