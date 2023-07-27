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
import { history, connect } from 'umi';
import React, { useEffect } from 'react';
import { Alert, Card, Col, Row } from '@oceanbase/design';
import { PageContainer } from '@ant-design/pro-components';
import { findBy, directTo } from '@oceanbase/util';
import { find } from 'lodash';
import { useRequest } from 'ahooks';
import useDocumentTitle from '@/hook/useDocumentTitle';
import * as ObClusterParameterController from '@/service/ocp-express/ObClusterParameterController';
import { getObServersByObCluster } from '@/util/oceanbase';
import ContentWithReload from '@/component/ContentWithReload';
import MonitorSearch from '@/component/MonitorSearch';
import MetricChart from '@/component/MetricChart';

interface MonitorProps {
  location: {
    query: {
      tab: string;
    };
  };

  dispatch: any;
  loading: boolean;
  clusterData: API.ClusterInfo;
  metricGroupList: API.MetricClass[];
}

const Monitor: React.FC<MonitorProps> = ({
  location,
  dispatch,
  loading,
  clusterData,
  metricGroupList,
}) => {
  useDocumentTitle(
    formatMessage({
      id: 'ocp-express.page.Monitor.ClusterMonitoring',
      defaultMessage: '集群监控',
    })
  );

  const { query } = location || {};
  const { tab } = query || {};
  const activeKey = tab || metricGroupList[0]?.key;

  const queryData = MonitorSearch.getQueryData(query);

  const zoneNameList = clusterData?.zones?.map(item => item.name);
  const serverList = getObServersByObCluster(clusterData).map(item => ({
    // 需要 ip 加 port 来区分 server
    ip: `${item.ip}:${item.port}`,
    zoneName: item.zoneName,
  }));

  // 指标类
  const metricClass = findBy(metricGroupList, 'key', activeKey);

  useEffect(() => {
    getMetricGroupList();
    dispatch({
      type: 'cluster/getClusterData',
    });
  }, []);

  function getMetricGroupList() {
    dispatch({
      type: 'monitor/getMetricGroupListData',
      payload: {
        scope: 'CLUSTER',
      },
    });
  }

  // 获取参数配置列表
  const { data } = useRequest(ObClusterParameterController.listClusterParameters, {
    defaultParams: [{}],
  });

  const parameterList = data?.data?.contents || [];

  // 获取 enable_perf_event 参数的值
  const perfEventData = find(
    parameterList,
    (item: API.ClusterParameter) => item.name === 'enable_perf_event'
  )?.currentValue;
  // 判断是否存在参数 TRUE，存在的话表明可以生成监控数据
  const perfEventEnabled = perfEventData?.values?.includes('True');

  return (
    <PageContainer
      ghost={true}
      header={{
        title: (
          <ContentWithReload
            content={formatMessage({
              id: 'ocp-express.page.Monitor.ClusterMonitoring',
              defaultMessage: '集群监控',
            })}
            spin={loading}
            onClick={() => {
              getMetricGroupList();
            }}
          />
        ),
      }}
    >
      {perfEventEnabled === false && (
        <Alert
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
          message={formatMessage({
            id: 'ocp-express.Detail.Monitor.BecauseTheEnablePerfEvent',
            defaultMessage:
              '因为当前集群的参数 enable_perf_event 已设置为 False，会有部分性能数据的缺失',
          })}
          action={
            <a
              onClick={() => {
                directTo(`/overview/parameter?keyword=enable_perf_event`);
              }}
            >
              {formatMessage({
                id: 'ocp-express.Detail.Monitor.ModifyClusterParameters',
                defaultMessage: '修改集群参数',
              })}
            </a>
          }
        />
      )}

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
                pathname: `/monitor`,
                query: {
                  ...query,
                  tab: key,
                },
              });
            }}
            tabList={metricGroupList.length > 0 ? metricGroupList.map(item => ({
              key: item?.key,
              tab: item?.name,
            })) : [{}]}
          >
            <MetricChart
              metricGroupList={metricClass.metricGroups || []}
              metricClass={metricClass}
              app={activeKey === 'database_metrics' ? 'OB' : 'HOST'}
              clusterName={clusterData.name}
              zoneNameList={zoneNameList}
              serverList={serverList}
              tenantList={clusterData.tenants}
              {...queryData}
            />
          </Card>
        </Col>
      </Row>
    </PageContainer >
  );
};

function mapStateToProps({ loading, cluster, monitor }) {
  return {
    loading: loading.effects['monitor/getMetricGroupListData'],
    clusterData: cluster.clusterData,
    metricGroupList: (monitor.metricGroupListData && monitor.metricGroupListData.contents) || [],
  };
}

export default connect(mapStateToProps)(Monitor);
