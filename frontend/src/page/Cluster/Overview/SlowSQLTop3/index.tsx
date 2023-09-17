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
import moment from 'moment';
import { max } from 'lodash';
import { Empty, Col, Row, token } from '@oceanbase/design';
import { TinyColumn } from '@oceanbase/charts';
import { useRequest } from 'ahooks';
import * as ObSqlStatController from '@/service/ocp-express/ObSqlStatController';
import MyCard from '@/component/MyCard';
import ContentWithQuestion from '@/component/ContentWithQuestion';
import { RFC3339_DATE_TIME_FORMAT } from '@/constant/datetime';
import { NEAR_6_HOURS } from '@/component/OCPRangePicker/constant';
import styles from './index.less';

export interface SlowSQLTop3Props {
  typeButton?: React.ReactNode;
  range?: React.ReactNode;
}

const SlowSQLTop3: React.FC<SlowSQLTop3Props> = () => {

  // 获取租户 SlowSQL 数 Top3 (最近 6 小时)
  const startTime = moment().subtract(6, 'hour').format(RFC3339_DATE_TIME_FORMAT);
  const endTime = moment().format(RFC3339_DATE_TIME_FORMAT);
  const { data: topSlowSqlListData, loading } = useRequest(ObSqlStatController.slowSqlRank, {
    defaultParams: [
      {
        startTime,
        endTime,
        top: 3,
      },
    ],
  });
  let topSlowSqlList = topSlowSqlListData?.data?.contents || [];
  const maxSlowSqlCount = max(topSlowSqlList.map(item => item.count));

  // 数据不够，补足三列
  if (topSlowSqlList.length === 1) {
    topSlowSqlList = [...topSlowSqlList, {}, {}];
  } else if (topSlowSqlList.length === 2) {
    topSlowSqlList = [...topSlowSqlList, {}];
  }

  const Statistic = ({ value, unit }) => (
    <span>
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
            id: 'ocp-express.Component.SlowSQLTop3.TenantSlowsqlTop',
            defaultMessage: '租户 SlowSQL 数 Top3',
          })}
          tooltip={{
            title: formatMessage({
              id: 'ocp-express.Component.SlowSQLTop3.SortBySlowsqlInTheLastHours',
              defaultMessage: '按最近 6 小时内 SlowSQL 数排序',
            }),
          }}
        />
      }
      style={{
        height: 150,
      }}
    >
      {topSlowSqlList.length > 0 ? (
        <Row gutter={48}>
          {topSlowSqlList.map((item, index) => {
            const chartData = [item.count || 0];
            return (
              <>
                <Col
                  key={item.tenantId}
                  span={8}
                  className={index !== topSlowSqlList.length - 1 ? styles.borderRight : ''}
                >
                  <div
                    data-aspm-click="c304261.d308769"
                    data-aspm-desc="租户 Slow 数 Top3-跳转 SlowSQL"
                    data-aspm-param={``}
                    data-aspm-expo
                    onClick={() => {
                      if (item.tenantName) {
                        history.push({
                          pathname: '/diagnosis/sql',
                          query: {
                            tenantId: item.tenantId,
                            tab: 'slowSql',
                            startTime,
                            endTime,
                            rangeKey: NEAR_6_HOURS.name,
                          },
                        });
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
                      <Statistic value={item.count || 0} />
                    </Col>
                    {/* 存在 SlowSQL 才展示趋势图 */}
                    {chartData.length > 0 && (
                      <Col span={14}>
                        <TinyColumn
                          height={18}
                          data={chartData}
                          meta={{
                            y: {
                              max: maxSlowSqlCount,
                            },
                          }}
                          color="#CDDDFF"
                          columnStyle={{
                            fill: 'l(270) 0:rgba(205,221,255,0) 0.83:#CDDDFF',
                            radius: 0,
                          }}
                          maxColumnWidth={200}
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
                              }[]
                            ) => {
                              const data = items?.[0]?.data || {};
                              const text = formatMessage(
                                {
                                  id: 'ocp-express.Component.SlowSQLTop3.Datay',
                                  defaultMessage: '{dataY} 条',
                                },
                                { dataY: data.y }
                              );

                              return `<div style="padding: 4px">${text}</div>`;
                            },
                          }}
                          style={{
                            marginTop: 14,
                            height: 18,
                          }}
                        />
                      </Col>
                    )}
                  </Row>
                </Col>
              </>
            );
          })}
        </Row>
      ) : (
        <Empty imageStyle={{ height: 70 }} description="" />
      )}
    </MyCard>
  );
};

export default SlowSQLTop3;
