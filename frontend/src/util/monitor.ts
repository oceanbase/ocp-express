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

import { flatten, groupBy as _groupBy, uniqBy, sum } from 'lodash';
import { isNullValue, sortByNumber } from '@oceanbase/util';

export interface MonitorParamter {
  scope?: Global.MonitorScope;
  app?: Global.MonitorApp;
  clusterName?: string;
  tenantName?: string;
  zoneName?: string;
  serverIp?: string;
  serverPort?: string;
  mount_point?: string;
  mount_label?: string;
  task_type?: string;
  process?: string;
  device?: string;
  cpu?: string;
}

/* 根据对象参数，获取监控查询的 labels 和 groupBy */
export function getLabelsAndGroupBy({
  // scope 用于显式指定最后的聚合维度
  scope,
  app = 'OB',
  clusterName,
  tenantName,
  zoneName,
  serverIp,
  serverPort,
  mount_point,
  mount_label,
  task_type,
  process,
  device,
  cpu,
}: MonitorParamter) {
  const labelsObjMap = {
    OB: {
      app,
      ob_cluster_name: clusterName,
      tenant_name: tenantName,
      obzone: zoneName,
      svr_ip: serverIp,
      svr_port: serverPort,
      mount_point,
      mount_label,
      task_type,
      process,
      device,
      cpu,
    },
    HOST: {
      app,
      obzone: zoneName,
      svr_ip: serverIp,
      svr_port: serverPort,
      mount_point,
      mount_label,
      task_type,
      process,
      device,
      cpu,
    },
  };
  // 设置每类监控数据必须包含的 groupBy label，这样监控数据才会返回相关的 label 信息用于前端展示
  const groupByListMap = {
    OB: ['app', 'ob_cluster_name'],
    HOST: ['app'],
  };
  const labelsObj = labelsObjMap[app] || {};
  // 获取 labels
  const labelKeyList = Object.keys(labelsObj);
  const labels = labelKeyList
    .filter(key => !isNullValue(labelsObj[key]))
    .map(key => `${key}:${labelsObj[key]}`)
    .join(',');
  // 获取 groupBy
  const groupByList = groupByListMap[app] || [];
  labelKeyList.forEach(key => {
    // 当维度值不为空，将对应维度追加到 groupBy 中
    if (!isNullValue(labelsObj[key]) && !groupByList.includes(key)) {
      groupByList.push(key);
    }
  });
  // scope 是最终的聚合维度，将 scope 追加到 groupBy 中
  if (scope && !groupByList.includes(scope)) {
    groupByList.push(scope);
  }
  const groupBy = groupByList.join(',');
  return {
    labels,
    groupBy,
  };
}

export interface TopDataItem {
  app?: Global.MonitorApp;
  obregion?: string;
  ob_cluster_id?: string;
  obproxy_cluster?: string;
  obproxy_cluster_id?: string;
  tenant_name?: string;
  obzone?: string;
  svr_ip?: string;
  mount_point?: string;
  mount_label?: string;
  task_type?: string;
  process?: string;
  device?: string;
  cpu?: string;
  cluster?: string;
  data?: {
    timestamp: number;
    [metricKey: string]: number;
  }[];
}

/* 根据 Top 监控的接口数据，获取监控查询的图表数据 */
export function getTopChartData({
  dataList,
  groupBy,
  metricKeys = [],
  targetWithMetric = false,
  clusterName,
  othersMetricKeys = [],
}: {
  dataList: TopDataItem[];
  groupBy: string;
  // 指标 key 的列表
  metricKeys: string[];
  // target 中是否展示指标，常用于包括多个细分指标的 Top 监控
  targetWithMetric?: boolean;
  clusterName?: string;
  othersMetricKeys?: string[];
}) {
  const groupByList = groupBy.split(',');
  const scope = groupByList[groupByList.length - 1];
  // 不参与绘图的监控数据，但会用于展示额外信息
  const otherData = {};

  Object.entries(_groupBy(dataList, scope)).map(([key, list]) => {
    if (Array.isArray(list)) {
      const result = list
        .map(item => {
          return item.data || [];
        })
        .filter(myList => {
          return myList.some(item => {
            Object.keys(item).some(_key => {
              othersMetricKeys.includes(_key);
            });
          });
        });

      otherData[key] = result;
    }
  });

  // 图表绘制使用的数据
  const chartDataList = dataList.filter(dataItem => {
    return dataItem.data?.some(item => {
      return Object.keys(item).some(_key => metricKeys.includes(_key));
    });
  });

  return (
    flatten(
      chartDataList.map(dataItem => {
        return flatten(
          (dataItem.data || []).map(item => {
            const targetMap = {
              // 如果按集群 ID 聚合，则需要追加 obregion 集群名作为对象名称
              ob_cluster_id: `${dataItem.obregion}:${dataItem[scope]}`,
              // 如果按租户聚合，没有指定集群名时，需要追加集群名和集群 ID 作为对象名称
              tenant_name: clusterName
                ? dataItem[scope]
                : dataItem.obregion
                ? `${dataItem.obregion}:${dataItem.ob_cluster_id}:${dataItem[scope]}`
                : `${dataItem.cluster}_${dataItem[scope]}`,
            };
            const target = targetMap[scope] || dataItem[scope];
            const mergeObject = {};
            const key = dataItem[scope];
            const otherDataList = otherData[key] || [];

            if (Array.isArray(otherDataList)) {
              otherDataList.forEach(list => {
                // 获取其它维度下同时间段的数据
                const metricItem = list?.find(metric => metric?.timestamp === item.timestamp);
                othersMetricKeys.forEach(metricKey => {
                  if (metricItem && !isNullValue(metricItem[metricKey])) {
                    mergeObject[metricKey] = metricItem[metricKey];
                  }
                });
              });
            }

            return metricKeys.map(metricKey => ({
              timestamp: item.timestamp * 1000,
              value: item[metricKey],
              target: targetWithMetric ? `${target}:${metricKey}` : target,
              ...mergeObject,
            }));
          })
        );
      })
    )
      // 去掉 value 为空的数据，属于中间数据处理产生的无效点
      .filter(item => !isNullValue(item.value))
      // Top 数据需要根据 value 从大到小排序，以对应图表折现的展示顺序，方便用户直观比较
      .sort((a, b) => sortByNumber(b, a, 'value'))
  );
}

/* 根据 Top 监控的接口数据，获取对象列表 */
export function getTopTargetList({
  dataList,
  groupBy,
  metricKeys = [],
  limit,
}: {
  dataList: TopDataItem[];
  groupBy: string;
  // 指标 key 的列表
  metricKeys: string[];
  limit?: number;
}) {
  const groupByList = groupBy.split(',');
  const scope = groupByList[groupByList.length - 1];

  return (
    flatten(
      uniqBy(dataList, scope).map(dataItem => {
        const scopeTarget = dataItem[scope] as string;
        const targetMap = {
          // 如果按集群 ID 聚合，则需要追加 obregion 集群名作为对象名称
          ob_cluster_id: `${dataItem.obregion}:${scopeTarget}`,
          // 如果按租户聚合，没有指定 obregion 集群名时，需要追加集群名和集群 ID 作为对象名称
          tenant_name: dataItem.obregion
            ? scopeTarget
            : `${dataItem.obregion}:${dataItem.ob_cluster_id}:${scopeTarget}`,
        };
        const target = (targetMap[scope] as string) || scopeTarget;

        return metricKeys.map(metricKey => ({
          totalValue: sum(dataItem.data?.map(item => item[metricKey])),
          // 如果是多指标，则 target 带上指标后缀
          // 如果是单指标，则展示原始的 target
          target: metricKeys.length > 1 ? `${target}:${metricKey}` : target,
        }));
      })
    )
      // 根据 value 之和进行排序，等价于 value 平均值，因为时间跨度都是一样的
      .sort((a, b) => sortByNumber(b, a, 'totalValue'))
      // 取前 limit 个对象，limit 为空则获取全部对象
      .slice(0, limit)
      .map(item => item.target)
  );
}
