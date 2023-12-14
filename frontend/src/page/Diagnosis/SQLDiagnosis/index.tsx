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
import {
  Alert,
  Button,
  Card,
  Checkbox,
  Col,
  Form,
  Input,
  message,
  Row,
  Space,
  theme,
} from '@oceanbase/design';
import { PageContainer } from '@oceanbase/ui';
import React, { Fragment, useEffect, useState } from 'react';
import { flatten, isArray, isEqual, isObject, omit } from 'lodash';
import moment from 'moment';
import { useMount, useRequest } from 'ahooks';
import useDocumentTitle from '@/hook/useDocumentTitle';
import useUrlState from '@ahooksjs/use-url-state';
import { findByValue, isNullValue, jsonParse, toBoolean } from '@oceanbase/util';
import CollapseSwicther from '@/component/CollapseSwitcher';
import ServerSelect from '@/component/common/ServerSelect';
import ContentWithQuestion from '@/component/ContentWithQuestion';
import { OCPRangePicker } from '@/component/OCPRangePicker';
import { NEAR_30_MINUTES } from '@/component/OCPRangePicker/constant';
import { RFC3339_DATE_TIME_FORMAT } from '@/constant/datetime';
import { DEFAULT_RANGE, SQL_ATTRIBUTE_LIST, SQL_TYPE_LIST } from '@/constant/sqlDiagnosis';
import * as ObTenantController from '@/service/ocp-express/ObTenantController';
import { isEnglish } from '@/util';
// import { formatTime } from '@/util/datetime';
import MyDropdown from '@/component/MyDropdown';
import ColumnManager from './Component/ColumnManager';
import SQLCondition from './Component/SQLCondition';
import TopSQL from './TopSQL';
import SlowSQL from './SlowSQL';

const FormItem = Form.Item;

// 格式化 query 参数，将数组和对象转换为字符串
const formatQueryValues = (queryValues: Record<string, any>) => {
  const result = {};
  Object.entries(queryValues).map(([key, value]) => {
    if (isArray(value) || isObject(value)) {
      result[key] = JSON.stringify(value);
    } else {
      result[key] = value;
    }
  });

  return result;
};

const getDefaultSorter = (activeKey: SQLDiagnosis.SqlType) => {
  const defaultSorterItem: SQLDiagnosis.QueryValues['sorter'] = {};

  // 可疑 SQL 的默认排序列为 `平均响应时间`，高危 SQL 为 `执行时间`，其他 SQL 的默认排序列为 `总响应时间`
  if (activeKey === 'suspiciousSql') {
    defaultSorterItem.field = 'avgElapsedTime';
  } else if (activeKey === 'highRiskSql') {
    defaultSorterItem.field = 'executeTime';
  } else {
    defaultSorterItem.field = 'sumElapsedTime';
  }

  defaultSorterItem.order = 'descend';

  return defaultSorterItem;
};

export interface IProps {
  location: {
    query: SQLDiagnosis.QueryValues & { tenantId: string };
    pathname: string;
  };
}

const DEFAULT_SEARCH = {
  range: { key: 'customize', range: [] },
  inner: false,
  serverId: 'all',
  sqlText: '',
  filterExpression: undefined,
  filterExpressionList: [],
};

const SQLDiagnosis: React.FC<IProps> = ({ location }) => {
  const { token } = theme.useToken();
  useDocumentTitle(
    formatMessage({
      id: 'ocp-express.Diagnosis.SQLDiagnosis.SqlDiagnosis',
      defaultMessage: 'SQL 诊断',
    })
  );
  const [collapsed, setCollapsed] = useState(
    isNullValue(location.query.collapsed) ? false : toBoolean(location.query.collapsed)
  );

  const [form] = Form.useForm();

  // 每次都会去更新 location.query, queryValues 内部值一样,但是引用地址不同，setQueryValues 设置空字符串等于删除这个字段
  // 使用了 useUrlState 后，不在使用 history + query 的方式去改变值，统一使用 setQueryValues 去改变 query 的值
  const [queryValues, _setQueryValues] = useUrlState();

  const setQueryValues = (data: Record<string, any>) => {
    _setQueryValues(
      formatQueryValues({
        ...location.query,
        ...data,
      })
    );
  };

  const activeKey = (location.query.tab ||
    SQL_TYPE_LIST.map(item => item.value)?.[0] ||
    'suspiciousSql') as SQLDiagnosis.SqlType;

  const attributeSqlType = activeKey === 'newSql' ? 'topSql' : activeKey;
  const attributes = SQL_ATTRIBUTE_LIST.filter(
    item => !item.sqlType || item.sqlType === attributeSqlType
  );

  const { data: listTenantsRes, loading: listTenantsLoading } = useRequest(
    ObTenantController.listTenants,
    {
      defaultParams: [{}],
    }
  );

  const tenants = listTenantsRes?.data?.contents || [];
  const tenantId =
    (location?.query?.tenantId && Number(location?.query?.tenantId)) || tenants[0]?.obTenantId;

  const tenantData = tenants.find(item => item.obTenantId === tenantId) || {};

  const onSearch = (queryV = {}) => {
    const formValues = form.getFieldsValue();
    if (!formValues?.range?.range || formValues?.range?.range?.length === 0) {
      message.warning(
        formatMessage({
          id: 'ocp-express.Detail.SQLDiagnosis.EnterASearchTime',
          defaultMessage: '请输入搜索时间',
        })
      );

      return;
    }

    const params = {
      startTime: formValues.range?.range[0].format(RFC3339_DATE_TIME_FORMAT),
      endTime: formValues.range?.range[1].format(RFC3339_DATE_TIME_FORMAT),
      rangeKey: formValues.range?.key,
      serverId: formValues.serverId,
      inner: formValues.inner,
      sqlText: formValues.sqlText,
      filterExpression: formValues.filterExpression,
      filterExpressionList: formValues.filterExpressionList,
      page: 1,
      // 导出时也应该应用表格默认排序
      sorter: getDefaultSorter(activeKey),
    };

    setQueryValues({ ...params, ...queryV });
  };

  const resetSearch = () => {
    form.setFieldsValue({ ...DEFAULT_SEARCH, range: DEFAULT_RANGE });
  };

  // 高亮的新增列
  const [actives, setActives] = useState([]);

  // 设置表格默认展示的列，兼容 SQL 下钻时高亮并排序展示列
  const getDefaultFields = (key: SQLDiagnosis.SqlType, drilldownable?: boolean) => {
    const querySorter = jsonParse(location.query?.sorter || '', {});

    const fields =
      findByValue(SQL_TYPE_LIST, key).defaultFieldList?.map(
        item => SQL_ATTRIBUTE_LIST.find(attr => attr.name === item) || {}
      ) || [];

    if (drilldownable) {
      // 下钻情况下，如果存在排序项，且不为自定义列，同时当前 fields 中不存在此项（默认不展示），需要将此项添加到 fields 中
      if (querySorter.field && fields.findIndex(item => item.name === querySorter.field) === -1) {
        // 存在自定义列的情况，这种情况无法检索到对应 field，不需要添加
        const newField = SQL_ATTRIBUTE_LIST.find(item => item.name === querySorter.field);
        if (newField) {
          // 插入到第 3 个位置
          fields.splice(2, 0, newField);
        }
      }
    }

    return fields;
  };

  const defaultFields = getDefaultFields(activeKey, true);
  // 选中的全部列
  const [fields, setFields] = useState<SQLDiagnosis.SqlAuditStatDetailAttribute[]>(defaultFields);

  useEffect(() => {
    if (actives.length) {
      // 3 秒后清除高亮状态
      setTimeout(() => {
        setActives([]);
      }, 3000);
    }
  }, [actives]);

  useEffect(() => {
    // 切换 Tab 时在 Form 中重置高级条件，点击左侧 Menu 中的 SQL 诊断也需要触发
    form.setFieldsValue({
      filterExpression: '',
      filterExpressionList: [],
    });
  }, [activeKey]);

  useEffect(() => {
    // 如果 query 为 {} 时，需要触发查询，避免 query={} 时导致后续请求出现问题
    if (isEqual(location.query, {} && !!tenantId)) {
      setTimeout(() => {
        onSearch();
      }, 0);
    }
  }, [location.query]);

  useMount(() => {
    // query 中带下来的 search 参数回填到 form 中，localStorage 中存在自定义列内容也需回填入 form
    const {
      startTime,
      endTime,
      rangeKey,
      inner,
      sqlText,
      serverId,
      filterExpression,
      filterExpressionList,
      sorter,
    } = location.query;

    const currentRange = {
      key: NEAR_30_MINUTES.name,
      range: NEAR_30_MINUTES.range(),
    };

    if (rangeKey) {
      currentRange.key = rangeKey;
    }
    if (startTime && endTime) {
      currentRange.range = [moment(startTime), moment(endTime)];
    }

    form.setFieldsValue({
      range: currentRange,
      inner: toBoolean(inner),
      sqlText,
      serverId: isNullValue(serverId) || serverId === 'all' ? 'all' : serverId,
      filterExpression,
      filterExpressionList: jsonParse(filterExpressionList, []),
    });

    // 不使用 setTimeout 包裹，让 setQueryValues 异步执行会出现同步 value 到 url 后，url 又被重置为空，导致 queryValues 为空对象的情况，检查 setQueryValues 各个使用处并未发现问题， 猜测是路由相关导致的
    // FIX: 直接点击 SQL 诊断重新进入，不会再次触发 useMount 的逻辑，但是会让 url 清空
    setTimeout(() => {
      setQueryValues({
        ...location.query,
        startTime: currentRange.range[0]?.format(RFC3339_DATE_TIME_FORMAT),
        endTime: currentRange.range[1]?.format(RFC3339_DATE_TIME_FORMAT),
        inner: toBoolean(inner),
        sqlText,
        serverId: isNullValue(serverId) || serverId === 'all' ? 'all' : serverId,
        filterExpression,
        filterExpressionList,
        sorter: sorter || getDefaultSorter(activeKey),
      });
    }, 100);
  });

  const searchAttrList = attributes;

  const SQLProps = {
    fields,
    actives,
    form,
    setQueryValues,
    queryValues: omit(queryValues, 'collapsed'),
    location,
    setActives: () => {
      const querySorter = jsonParse(location.query?.sorter || '', {});
      if (querySorter.field && querySorter.highlight) {
        setActives([{ name: querySorter.field }]);
      }
    },
  };

  return (
    <PageContainer
      ghost={true}
      header={{
        title: formatMessage({
          id: 'ocp-express.Diagnosis.SQLDiagnosis.SqlDiagnosis',
          defaultMessage: 'SQL 诊断',
        }),
        subTitle: (
          <Space
            style={{
              fontWeight: 500,
              fontSize: 16,
              marginLeft: 12,
              color: token.colorTextSecondary,
            }}
          >
            <span>
              {formatMessage({
                id: 'ocp-express.Diagnosis.SQLDiagnosis.Tenant',
                defaultMessage: '租户:',
              })}
            </span>
            {!listTenantsLoading && (
              <MyDropdown
                menuList={tenants
                  .filter(tenant => tenant.status !== 'CREATING')
                  .map(item => {
                    return {
                      value: item.obTenantId,
                      label: item.name,
                    };
                  })}
                isSolidIcon={true}
                defaultMenuKey={tenantId}
                onChange={(v: string) => {
                  history.replace({
                    pathname: location.pathname,
                    query: {
                      ...location.query,
                      tenantId: v,
                    },
                  });
                }}
              />
            )}
          </Space>
        ),
      }}
    >
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card bordered={false} bodyStyle={{ paddingBottom: 0 }}>
            <Form form={form} initialValues={DEFAULT_SEARCH}>
              <Row>
                <Col span={12} style={{ height: 56 }}>
                  <FormItem
                    name="range"
                    label={formatMessage({
                      id: 'ocp-express.Detail.SQLDiagnosis.TimeRange',
                      defaultMessage: '时间范围',
                    })}
                    labelCol={{ span: 3 }}
                    wrapperCol={{ span: 21 }}
                  >
                    <OCPRangePicker />
                  </FormItem>
                </Col>
                <Col span={collapsed ? 6 : 12} style={{ height: 56 }}>
                  <FormItem
                    label={collapsed ? undefined : 'OBServer'}
                    {...(isEnglish()
                      ? { labelCol: { span: 6 }, wrapperCol: { span: 18 } }
                      : {
                          labelCol: { span: 4 },
                          wrapperCol: { span: 20 },
                        })}
                  >
                    <Row>
                      {/* 收缩情况下也不展示 OBServer */}
                      {!collapsed && (
                        <Col span={collapsed ? 24 : 12}>
                          <FormItem name="serverId">
                            <ServerSelect
                              showAllOption={true}
                              allLabel={formatMessage({
                                id: 'ocp-express.Detail.SQLDiagnosis.AllObserver',
                                defaultMessage: '全部 OBServer',
                              })}
                              realServerList={flatten(
                                tenantData.zones?.map(
                                  item =>
                                    // 根据租户的 unit 分布获取 server 列表
                                    item.units?.map(unit => ({
                                      id: `${unit.serverIp}:${unit.serverPort}`,
                                      ip: unit.serverIp,
                                      zoneName: unit.zoneName,
                                    })) || []
                                ) || []
                              )}
                            />
                          </FormItem>
                        </Col>
                      )}

                      <Col span={collapsed ? 24 : 8} offset={2} style={{ height: 56 }}>
                        <FormItem name="inner" valuePropName="checked">
                          <Checkbox>
                            <ContentWithQuestion
                              content={formatMessage({
                                id: 'ocp-express.Detail.SQLDiagnosis.InternalSql',
                                defaultMessage: '内部 SQL',
                              })}
                              tooltip={{
                                placement: 'topRight',
                                title: formatMessage({
                                  id: 'ocp-express.Detail.SQLDiagnosis.IndicatesWhetherTheSqlStatement',
                                  defaultMessage: '是否包含由 OceanBase 内部发起的 SQL',
                                }),
                              }}
                            />
                          </Checkbox>
                        </FormItem>
                      </Col>
                    </Row>
                  </FormItem>
                </Col>

                {/* 可疑 SQL 只支持 时间范围查询 */}
                {!collapsed && (
                  <Fragment>
                    <Col span={19} style={{ height: 56 }}>
                      <FormItem
                        label={formatMessage({
                          id: 'ocp-express.Detail.SQLDiagnosis.KeyWords',
                          defaultMessage: '关键词',
                        })}
                        name="sqlText"
                        labelCol={{ span: 2 }}
                        wrapperCol={{ span: 22 }}
                      >
                        <Input
                          placeholder={formatMessage({
                            id: 'ocp-express.Detail.SQLDiagnosis.RemoveLiteralCharactersSuchAs',
                            defaultMessage: '输入的关键词请去掉字符串、数值等字面量',
                          })}
                        />
                      </FormItem>
                    </Col>
                    <Col span={19} style={{ minHeight: 56 }}>
                      {/* 后端使用 filterExpression 字段接收字符串作为值，前端使用数组作为渲染的数据结构，所以需要一个额外字段 */}
                      <FormItem noStyle={true} name="filterExpression" />

                      <FormItem
                        label={formatMessage({
                          id: 'ocp-express.Detail.SQLDiagnosis.AdvancedConditions',
                          defaultMessage: '高级条件',
                        })}
                        name="filterExpressionList"
                        labelCol={{ span: 2 }}
                        wrapperCol={{ span: 22 }}
                      >
                        <SQLCondition
                          // 排除 内部SQL 指标
                          searchAttrList={searchAttrList.filter(item => item.name !== 'inner')}
                          onChange={list => {
                            const nameListByStringType = searchAttrList
                              .filter(item => item.dataType === 'STRING')
                              .map(item => item.name);

                            form.setFieldsValue({
                              filterExpressionList: list,
                              filterExpression: list
                                .map(({ searchAttr, searchOp, searchVal }) => {
                                  // 字符串类型的指标，需要在输入的值两侧加上单引号拼接出字符串格式
                                  if (nameListByStringType.includes(searchAttr)) {
                                    return `@${searchAttr} ${searchOp} '${searchVal}'`;
                                  }
                                  // 同后端约定格式，变量前需要加 @
                                  return `@${searchAttr} ${searchOp} ${searchVal}`;
                                })
                                .join(' and '),
                            });
                          }}
                        />
                      </FormItem>
                    </Col>
                  </Fragment>
                )}

                <Col span={5}>
                  <FormItem wrapperCol={{ span: 24 }} style={{ float: 'right' }}>
                    <Space style={{ marginRight: 8 }}>
                      <Button
                        data-aspm-click="c318543.d343269"
                        data-aspm-desc="SQL 查询-重置"
                        data-aspm-param={``}
                        data-aspm-expo
                        onClick={resetSearch}
                      >
                        {formatMessage({
                          id: 'ocp-express.Detail.SQLDiagnosis.Reset',
                          defaultMessage: '重置',
                        })}
                      </Button>
                      <Button
                        data-aspm-click="c318543.d343268"
                        data-aspm-desc="SQL 查询-查询"
                        data-aspm-param={``}
                        data-aspm-expo
                        type="primary"
                        onClick={() => {
                          onSearch();
                        }}
                      >
                        {formatMessage({
                          id: 'ocp-express.Detail.SQLDiagnosis.Query',
                          defaultMessage: '查询',
                        })}
                      </Button>
                    </Space>
                    <CollapseSwicther
                      collapsed={collapsed}
                      onCollapse={() => {
                        setCollapsed(!collapsed);
                        setQueryValues({ ...queryValues, collapsed: !collapsed });
                      }}
                    />
                  </FormItem>
                </Col>
              </Row>
              <FormItem name="customColumns" noStyle />
              <FormItem name="customColumnName" noStyle />
            </Form>
          </Card>
        </Col>
        <Col span={24}>
          <Card
            bordered={false}
            activeTabKey={activeKey}
            onTabChange={(key: SQLDiagnosis.SqlType) => {
              if (key === activeKey) {
                return;
              }
              // 去掉排序、筛选、分页、选中列等参数，切换 SQL 类型这些参数都需要重置
              const {
                sorter,
                filters,
                page,
                size,
                fields: myFields,
                ...restQueryValues
              } = queryValues;
              // 重置选中的列

              setFields(getDefaultFields(key));

              // 清空高亮的列
              setActives([]);

              setQueryValues({
                ...restQueryValues,
                // 设置 serverId 的默认值
                serverId: 'all',
                // 切换 Tab 时重置高级条件
                filterExpression: undefined,
                filterExpressionList: [],
                sorter: getDefaultSorter(key),
                tab: key,
              });
            }}
            tabList={SQL_TYPE_LIST.map(item => ({
              key: item.value,
              tab: item.label,
            }))}
            tabBarExtraContent={
              <ColumnManager
                sqlType={activeKey}
                defaultFields={defaultFields}
                queryValues={queryValues}
                setQueryValues={setQueryValues}
                attributes={attributes}
                fields={fields}
                onOk={(myFields, myActives) => {
                  setFields(myFields);
                  setActives(myActives);
                }}
              />
            }
          >
            {activeKey === 'topSql' && <TopSQL {...SQLProps} />}

            {activeKey === 'slowSql' && <SlowSQL {...SQLProps} />}
          </Card>
        </Col>
      </Row>
    </PageContainer>
  );
};

export default SQLDiagnosis;
