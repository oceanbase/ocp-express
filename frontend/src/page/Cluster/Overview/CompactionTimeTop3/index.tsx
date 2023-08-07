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
import { history } from 'umi';
import React from 'react';
import { Empty, Col, Row, useToken } from '@oceanbase/design';
import { TinyArea, theme } from '@oceanbase/charts';
import { orderBy, maxBy } from 'lodash';
import { useRequest } from 'ahooks';
import { formatTime } from '@/util/datetime';
import * as ObTenantCompactionController from '@/service/ocp-express/ObTenantCompactionController';
import MyCard from '@/component/MyCard';
import ContentWithQuestion from '@/component/ContentWithQuestion';
import useStyles from './index.style';
import { formatDuration } from '@/util';

export interface CompactionTimeTop3Props {}

const CompactionTimeTop3: React.FC<CompactionTimeTop3Props> = () => {
  const { styles } = useStyles();
  const { token } = useToken();
  // 获取合并时间 Top3 的租户合并数据
  const { data: topCompactionListData, loading } = useRequest(
    ObTenantCompactionController.topCompactions,
    {
      defaultParams: [
        {
          top: 3,
          times: 5,
        },
      ],
    },
  );

  let topCompactionList = topCompactionListData?.data?.contents || [];

  // 对数据根据costTime进行降序排序
  topCompactionList = orderBy(
    topCompactionList.map((item) => ({
      ...item,
      costTime: maxBy(item.compactionList, 'costTime').costTime,
    })),
    ['costTime'],
    ['desc'],
  );

  // 数据不够，补足三列
  if (topCompactionList.length === 1) {
    topCompactionList = [...topCompactionList, {}, {}];
  } else if (topCompactionList.length === 2) {
    topCompactionList = [...topCompactionList, {}];
  }

  const Statistic = ({ value, unit }) => (
    <span
      style={{
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
      }}
    >
      <span
        style={{
          fontFamily: 'Avenir-Heavy',
          fontSize: 28,
        }}
      >
        {value || '-'}
      </span>
      {unit && (
        <span
          style={{
            fontSize: 12,
            color: token.colorTextTertiary,
            marginLeft: 4,
            marginTop: 12,
          }}
        >
          {unit}
        </span>
      )}
    </span>
  );

  return (
    <MyCard
      loading={loading}
      title={
        <ContentWithQuestion
          content={formatMessage({
            id: 'ocp-express.Component.CompactionTimeTop3.TenantMergeTimeTop',
            defaultMessage: '租户合并时间 Top3',
          })}
          tooltip={{
            title: (
              <div>
                <div>
                  {formatMessage({
                    id: 'ocp-express.Component.CompactionTimeTop3.SortByLastMergeTime',
                    defaultMessage: '按最近 1 次合并时间排序',
                  })}
                </div>
                <div>
                  {formatMessage({
                    id: 'ocp-express.Component.CompactionTimeTop3.GreenTheLastTwoMergingTimesHaveDecreased',
                    defaultMessage: '绿色：最近 2 次合并时间下降',
                  })}
                </div>
                <div>
                  {formatMessage({
                    id: 'ocp-express.Component.CompactionTimeTop3.RedTheLastTwoMergingTimesHaveIncreased',
                    defaultMessage: '红色：最近 2 次合并时间上升',
                  })}
                </div>
              </div>
            ),
          }}
        />
      }
      style={{
        height: 150,
      }}
    >
      {topCompactionList.length > 0 ? (
        <Row gutter={48}>
          {topCompactionList.map((item, index) => {
            // 最近一次合并
            const latestCompaction = item.compactionList?.[0];
            // 倒数第二次合并
            const latest2Compaction = item.compactionList?.[1];
            // 合并时间是否增加: 最近一次合并时间 > 倒数第二次合并时间
            const isAscend =
              latest2Compaction &&
              (latestCompaction?.costTime || 0) > (latest2Compaction.costTime || 0);
            const color = isAscend ? theme.semanticRed : theme.semanticGreen;
            const durationData = formatDuration(latestCompaction?.costTime, 1);
            const chartData = [...(item.compactionList || [])]
              .map((compaction) => compaction.costTime as number)
              // 用于图表，数据顺序需要翻转下，把最近一次合并数据放最后
              .reverse();

            return (
              <Col
                key={item.obTenantId}
                span={8}
                className={index !== topCompactionList.length - 1 ? styles.borderRight : ''}
              >
                <div
                  data-aspm-click="c304253.d308755"
                  data-aspm-desc="租户合并时间 Top3-跳转租户详情"
                  data-aspm-param={``}
                  data-aspm-expo
                  onClick={() => {
                    if (item.tenantName) {
                      history.push(`/tenant/${item.obTenantId}`);
                    }
                  }}
                  className={item.tenantName ? 'ocp-link-hover' : ''}
                  style={{
                    color: token.colorTextTertiary,
                    display: 'inline-block',
                    marginBottom: 8,
                  }}
                >
                  {item.tenantName || '-'}
                </div>
                <Row gutter={14}>
                  <Col span={10}>
                    <Statistic value={durationData.value} unit={durationData.unitLabel} />
                  </Col>
                  {/* 合并次数 > 1 时才展示趋势图 */}
                  {chartData.length > 1 && (
                    <Col span={14}>
                      <TinyArea
                        height={54}
                        data={chartData}
                        tooltip={{
                          customContent: (
                            title: string,
                            items: {
                              color: string;
                              name: string;
                              value: number;
                              data: {
                                x: string;
                                y: number;
                              };
                            }[],
                          ) => {
                            const data = items?.[0]?.data || {};
                            const currentDurationData = formatDuration(data.y);
                            // return `<div style="padding: 4px">${currentDurationData.value} ${currentDurationData.unitLabel}</div>`;
                            return `<div style="padding: 4px"><div></div>合并开始时间：${
                              topCompactionList[0]?.compactionList[0]?.startTime
                                ? formatTime(topCompactionList[0]?.compactionList[0]?.startTime)
                                : '-'
                            }<div>合并耗时：${currentDurationData.value} ${
                              currentDurationData.unitLabel
                            }</div></div>`;
                          },
                        }}
                        color={color}
                        point={{
                          style: {
                            fill: color,
                          },
                        }}
                        areaStyle={{
                          fill: isAscend ? theme.semanticRedGradient : theme.semanticGreenGradient,
                        }}
                        style={{
                          marginTop: 10,
                          height: 24,
                        }}
                      />
                    </Col>
                  )}
                </Row>
              </Col>
            );
          })}
        </Row>
      ) : (
        <Empty imageStyle={{ height: 70 }} description="" />
      )}
    </MyCard>
  );
};

export default CompactionTimeTop3;
