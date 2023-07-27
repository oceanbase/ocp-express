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
import { history, useSelector } from 'umi';
import React, { useEffect } from 'react';
import { Form, Col, Row, Switch, Ranger } from '@oceanbase/design';
import { isEqual, omitBy } from 'lodash';
import moment from 'moment';
import { isNullValue, toBoolean } from '@oceanbase/util';
import { useInterval, useUpdate } from 'ahooks';
import { DATE_TIME_FORMAT_DISPLAY, RFC3339_DATE_TIME_FORMAT } from '@/constant/datetime';
import { FORM_ITEM_LAYOUT, FREQUENCY } from '@/constant';
import MySelect from '@/component/MySelect';
import MyCard from '@/component/MyCard';
import styles from './index.less';
import { getSelects } from '@/constant/log';

const FormItem = Form.Item;
const { Option } = MySelect;

export interface MonitorSearchQuery {
  tab?: string;
  isRealtime?: string | boolean;
  startTime?: string;
  endTime?: string;
  zoneName?: string;
  serverIp?: string;
  serverPort?: string;
}

export interface MonitorSearchQueryData {
  tab?: string;
  isRealtime?: boolean;
  startTime?: string;
  endTime?: string;
  zoneName?: string;
  serverIp?: string;
  serverPort?: string;
}

export interface MonitorServer {
  ip: string;
  zoneName: string;
}

interface MonitorSearchProps {
  location?: {
    pathname?: string;
    query?: MonitorSearchQuery;
  };
  zoneNameList?: string[];
  serverList?: MonitorServer[];
  onSearch?: (queryData: MonitorSearchQueryData) => void;
}

function getQueryData(query: MonitorSearchQuery): MonitorSearchQueryData {
  const { tab, isRealtime, startTime, endTime, zoneName, serverIp, serverPort } = query;

  const queryData = {
    tab,
    isRealtime: toBoolean(isRealtime),
    startTime,
    endTime,
    zoneName,
    serverIp,
    serverPort,
  };
  return queryData;
}

const MonitorSearch: React.FC<MonitorSearchProps> = ({
  location: { query = {}, pathname } = {},
  zoneNameList,
  serverList,
  onSearch,
}) => {
  const [form] = Form.useForm();
  const { validateFields, setFieldsValue, getFieldsValue } = form;
  const update = useUpdate();
  const { systemInfo } = useSelector((state: DefaultRootState) => state.global);
  const collectInterval = systemInfo?.monitorInfo?.collectInterval || FREQUENCY;

  const queryData = getQueryData(query);

  const defaultRange =
    queryData.startTime && queryData.endTime
      ? [moment(queryData.startTime), moment(queryData.endTime)]
      : // 默认查看一小时的区间
      [moment().subtract(1, 'hours'), moment()];
  const defaultIsRealtime = queryData.isRealtime || false;

  const handleSearch = (mergeDefault?: boolean) => {
    validateFields().then(values => {
      const {
        range = defaultRange,
        isRealtime = defaultIsRealtime,
        zoneName = mergeDefault ? queryData.zoneName : undefined,
        serverIp = mergeDefault ? queryData.serverIp : undefined,
        serverPort = mergeDefault ? queryData.serverPort : undefined,
        serverIpWithPort,
        ...restValues
      } = values;

      // 需要 ip 加 port 来区分 server
      const [ip, port] = serverIpWithPort?.split(':') || [];
      const newQueryData = omitBy(
        {
          ...restValues,
          isRealtime,
          zoneName,
          serverIp: serverIp || ip,
          serverPort: serverPort || port,
          tab: queryData.tab,
          // 实时模式下起止时间设置为近 10 分钟
          startTime: isRealtime
            ? moment().subtract(10, 'minutes').format(RFC3339_DATE_TIME_FORMAT)
            : range && range[0] && range[0].format(RFC3339_DATE_TIME_FORMAT),
          endTime: isRealtime
            ? moment().format(RFC3339_DATE_TIME_FORMAT)
            : range && range[1] && range[1].format(RFC3339_DATE_TIME_FORMAT),
        },
        value => isNullValue(value)
      );

      // pathname 可能为空，此时查询条件不会与 URL 进行同步
      if (pathname) {
        // 使用 history.replace 解决多次查询后，使用浏览器后退按钮不会返回前一页面
        history.replace({
          pathname,
          query: newQueryData,
        });
      }
      if (onSearch) {
        onSearch(newQueryData);
      }
    });
  };

  // 组件挂载时触发查询
  useEffect(() => {
    setTimeout(() => {
      handleSearch(true);
    }, 0);
  }, []);

  useEffect(() => {
    // 如果 query 为 {} 时，触发查询，避免 query={} 时图表数据为空
    // TODO: 长期方案需要将图表中的 getData 改造为 useRequest，这样不满足请求条件时，不会用空数组兜底返回
    if (isEqual(query, {})) {
      setTimeout(() => {
        handleSearch(true);
      }, 0);
    }
  }, [query]);

  const { isRealtime = defaultIsRealtime, zoneName } = getFieldsValue();

  useInterval(
    () => {
      handleSearch();
    },
    isRealtime ? collectInterval * 1000 : undefined
  );

  const realServerList = serverList?.filter(
    // 根据选中的 zone 对 server 进行筛选
    item => !zoneName || item.zoneName === zoneName
  );

  const formItemLayout = {
    labelCol: {
      span: 8,
    },

    wrapperCol: {
      span: 16,
    },
  };

  return (
    <MyCard
      className={styles.container}
      bodyStyle={{ paddingBottom: 0 }}
      title={formatMessage({
        id: 'ocp-express.component.MonitorSearch.DataFiltering',
        defaultMessage: '数据筛选',
      })}
      bordered={false}
      extra={
        <Form layout="inline" form={form}>
          {isRealtime && (
            <FormItem
              label={formatMessage({
                id: 'ocp-express.component.MonitorSearch.UpdateTime',
                defaultMessage: '更新时间',
              })}
              className={styles.updateTime}
            >
              {moment().format(DATE_TIME_FORMAT_DISPLAY)}
            </FormItem>
          )}

          <FormItem
            name="isRealtime"
            initialValue={defaultIsRealtime}
            valuePropName="checked"
            label={formatMessage({
              id: 'ocp-express.component.MonitorSearch.AutoRefresh',
              defaultMessage: '自动刷新',
            })}
            htmlFor="none"
            style={{ marginRight: 0 }}
          >
            <Switch
              onChange={() => {
                // 为了保证获取切换实时后的最新表单值，切换后需要重新 render 表单，并异步触发查询操作
                update();
                setTimeout(handleSearch, 0);
              }}
            />
          </FormItem>
        </Form>
      }
    >
      <Form
        layout="horizontal"
        form={form}
        onValuesChange={() => {
          handleSearch();
        }}
        {...FORM_ITEM_LAYOUT}
      >
        <Row>
          {isRealtime ? (
            <Col span={3}>
              <FormItem
                label={formatMessage({
                  id: 'ocp-express.component.MonitorSearch.RefreshFrequency',
                  defaultMessage: '刷新频率',
                })}
                labelCol={{ span: 16 }}
                wrapperCol={{ span: 8 }}
              >
                {formatMessage(
                  {
                    id: 'ocp-express.component.MonitorSearch.FrequencySeconds',
                    defaultMessage: '{FREQUENCY} 秒',
                  },
                  { FREQUENCY: collectInterval }
                )}
              </FormItem>
            </Col>
          ) : (
            <>
              <Col span={13}>
                <FormItem
                  name="range"
                  // initialValue={defaultRange}
                  label={formatMessage({
                    id: 'ocp-express.component.MonitorSearch.SelectTime',
                    defaultMessage: '选择时间',
                  })}
                >
                  <Ranger
                    style={{ width: '100%' }}
                    allowClear={false}
                    format={DATE_TIME_FORMAT_DISPLAY}
                    defaultQuickValue={formatMessage({
                      id: 'ocp-express.src.constant.log.NearlyHour',
                      defaultMessage: '近 1 小时',
                    })}
                    selects={getSelects()}
                  />
                </FormItem>
              </Col>
            </>
          )}

          <Col span={5}>
            <FormItem
              name="zoneName"
              label="Zone"
              initialValue={queryData.zoneName}
              {...formItemLayout}
            >
              <MySelect
                allowClear={true}
                showSearch={true}
                onChange={() => {
                  setFieldsValue({
                    serverIpWithPort: undefined,
                  });
                }}
                placeholder={formatMessage({
                  id: 'ocp-express.component.MonitorSearch.All',
                  defaultMessage: '全部',
                })}
              >
                {zoneNameList?.map(item => (
                  <Option key={item} value={item}>
                    {item}
                  </Option>
                ))}
              </MySelect>
            </FormItem>
          </Col>
          <Col span={6}>
            <FormItem
              name="serverIpWithPort"
              initialValue={queryData.serverIp && `${queryData.serverIp}:${queryData.serverPort}`}
              label="OBServer"
              {...formItemLayout}
            >
              <MySelect
                allowClear={true}
                showSearch={true}
                placeholder={formatMessage({
                  id: 'ocp-express.component.MonitorSearch.All',
                  defaultMessage: '全部',
                })}
                style={{ width: '100%' }}
              >
                {realServerList?.map(item => (
                  <Option key={item.ip} value={item.ip}>
                    {item.ip}
                  </Option>
                ))}
              </MySelect>
            </FormItem>
          </Col>
        </Row>
      </Form>
    </MyCard>
  );
};

MonitorSearch.getQueryData = getQueryData;

export default MonitorSearch;
