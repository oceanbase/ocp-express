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
import React, { useEffect, useRef, useState } from 'react';
import {
  Button,
  Card,
  Tooltip,
  Col,
  Form,
  Radio,
  Row,
  Space,
  Spin,
  message,
} from '@oceanbase/design';
import { Ranger, FullscreenBox } from '@oceanbase/ui'
import { LoadingOutlined } from '@oceanbase/icons';
import { flatten, find } from 'lodash';
import type { Moment } from 'moment';
import moment from 'moment';
import { useRequest, useKeyPress, useInViewport } from 'ahooks';
import * as ComputeController from '@/service/ocp-express/HostController';
import { FORM_ITEM_LAYOUT, SELECT_TOKEN_SPEARATORS } from '@/constant';
import { DATE_TIME_FORMAT_DISPLAY, RFC3339_DATE_TIME_FORMAT } from '@/constant/datetime';
import { getSelects, LOG_TYPE_LIST, LOG_LEVEL } from '@/constant/log';
import useDocumentTitle from '@/hook/useDocumentTitle';
import { download } from '@/util/export';
import { isEnglish } from '@/util';
import MySelect from '@/component/MySelect';
import Empty from '@/component/Empty';
import LogTypeSelect from './Component/LogTypeSelect';
import IpSelect from './Component/IpSelect';
import LogHighlight from './Component/LogHighlight';

const FormItem = Form.Item;

export interface QueryLogProps {
  location?: {
    query: {
      defaultOCPType?: string;
    };
  };
}

const QueryLog: React.FC<QueryLogProps> = ({ location: { query = {} } = {} }) => {
  useDocumentTitle(
    formatMessage({ id: 'ocp-express.page.QueryLog.LogQuery', defaultMessage: '日志查询' })
  );

  const [form] = Form.useForm();
  const { validateFields, getFieldsValue, resetFields } = form;
  const { hostIps, COPLogType, keyword } = getFieldsValue();
  const [range, setRange] = useState<Moment[]>([moment().subtract(1, 'hours'), moment()]);
  const startTime = range && range[0] && range[0].format(RFC3339_DATE_TIME_FORMAT);
  const endTime = range && range[1] && range[1].format(RFC3339_DATE_TIME_FORMAT);
  const [allHostIps, getAllHostIps] = useState<any[] | null>(null);

  // 未选择 IP 节点时，展示所有 IP 节点
  const realHostIps = hostIps?.length > 0 ? hostIps : allHostIps;

  const [hostId, setHostId] = useState<string | null>(null);
  const [address, setAddress] = useState<string | null>(null);
  const [logType, setLogType] = useState<string | null>(null);
  const [language, setLanguage] = useState<string>();
  const [queryLogRequestParam, setQueryLogRequestParam] = useState({});

  const [logData, setLogData] = useState(null);
  const [isNoData, setIsNoData] = useState(false);
  // 是否全屏展示
  const [fullscreen, setFullscreen] = useState(false);
  const boxRef = useRef<FullscreenBox>();
  const [loadingMore, setLoadingMore] = useState(false);

  const [defaultCOPLogType, serDefaultCOPLogType] = useState<string | undefined>('CLUSTER');
  const [fileType, setFileType] = useState<string>();
  const limitHours = fileType === 'CLUSTER' ? 1 : 24;
  const notAllowDownload = moment(endTime).diff(moment(startTime), 'hours') > limitHours;

  const handleFullscreenChange = fs => {
    setFullscreen(fs);
  };
  const toggleFullscreen = () => {
    if (boxRef.current && boxRef.current.changeFullscreen) {
      boxRef.current.changeFullscreen(!fullscreen);
    }
  };
  // 全屏状态按下 ESC 退出全屏
  useKeyPress(27, () => {
    if (fullscreen) {
      toggleFullscreen();
    }
  });

  const { run: queryLogFn, loading } = useRequest(ComputeController.queryLog, {
    manual: true,
    onSuccess: (res, params) => {
      if (res?.successful) {
        setLoadingMore(false);
        if (params[0]?.position) {
          setIsNoData(res?.data?.logEntries?.length === 0);
          setLogData({
            ...res.data,
            logEntries: [...logData?.logEntries, res?.data?.logEntries],
          });
        } else {
          setLogData({
            ...res.data,
            logEntries: [res?.data?.logEntries],
          });
        }
      }
    },
  });

  // useRequest 加载更多 传递的 id 为上次请求的返回对象结果
  async function queryLog(prev, params) {
    // 采用前端状态判断
    if (!isNoData) {
      let queryParams = params;
      if (prev !== params?.id && prev?.successful) {
        queryParams = {
          ...params,
          limit: 10,
          position: logData?.position,
        };
      }

      return await queryLogFn(queryParams);
    }
  }

  // 查询日志
  const { run } = useRequest(queryLog, {
    manual: true,
    debounceWait: logData?.logEntries?.length > 0 ? 1000 : 0,
  });

  // 下载日志
  const {
    run: downloadLog,
    loading: downloadLoading,
    // cancel,
  } = useRequest(ComputeController.downloadLog, {
    manual: true,
    onSuccess: res => {
      if (res) {
        try {
          message.success(
            formatMessage({
              id: 'ocp-express.page.QueryLog.LogDownloadedSuccessfully',
              defaultMessage: '日志下载成功',
            })
          );

          /**
           * 日志文件命名规则：
           * OB_LOG_${集群名}_${主机 IP}_${开始时间}_${结束时间}
           * HOST_LOG_${主机 IP}_${开始时间}_${结束时间}*/
          download(
            res,
            `${fileType}_${address}_${range[0].format('YYYYMMDDHHmmss')}_${range[1].format(
              'YYYYMMDDHHmmss'
            )}.zip`
          );
        } catch (err) {
          throw new Error(
            formatMessage(
              {
                id: 'ocp-express.page.QueryLog.LogDownloadErrorErr',
                defaultMessage: '日志下载出错：{err}',
              },

              { err: err }
            )
          );
        }
      }
    },
  });

  const getLog = (id?: string | null, type?: string | null) => {
    if (loading) {
      return;
    }
    validateFields().then(values => {
      // 手动加载 先清空数据
      setIsNoData(false);
      setLogData({});
      const { logLevel, hostIps: _hostIps } = values;

      const myHostIps = _hostIps?.length > 0 ? _hostIps : allHostIps;

      const _queryLogRequestParam = {
        logType: type || values.COPLogType[0],
        startTime,
        endTime,
        keyword: values?.keyword?.length > 0 ? values?.keyword : null,
        excludeKeyword: values?.excludeKeyword,
        logLevel,
        position: '', // 上次查询到的文件 & offset（文件 id:文件 offset）
        limit: 10,
        // reqID  // 本次请求的唯一标识（前端生成）停止查询的标识，暂时不用
      };

      if (myHostIps?.length > 0) {
        // 如果存在传入的 HostId，则使用 id 去匹配一下
        const hostIpItem = id ? find(myHostIps, o => o.value === id) : myHostIps[0];
        setAddress(hostIpItem.label);

        // 注入 ip 和 port
        const [ip, port] = hostIpItem?.value?.split?.(':') || [];
        _queryLogRequestParam.ip = ip;
        _queryLogRequestParam.port = Number(port);
      }

      setLogType(type ? type : values.COPLogType[0]);

      setQueryLogRequestParam(_queryLogRequestParam);
      renderLanguage(type || values.COPLogType[0]);
      run(null, _queryLogRequestParam);
    });
  };

  const loadingMoreContainerRef = useRef<HTMLDivElement>(null);

  const [inViewport] = useInViewport(loadingMoreContainerRef);

  useEffect(() => {
    if (
      !loading &&
      !loadingMore &&
      logData?.position !== queryLogRequestParam?.position &&
      logData &&
      logData?.logEntries?.length > 0 &&
      inViewport
    ) {
      setLoadingMore(true);
      run(null, {
        ...queryLogRequestParam,
        position: logData?.position,
      });
    }
  }, [inViewport, logData]);

  useEffect(() => {
    // 当从其他页面跳转而来、且带有默认查询参数时，自动发起一次请求
    if (query.defaultOCPType) {
      // TODO: 日志查询的表单实现太过复杂，完全改不动，后需要大幅改造以简化逻辑
      // 目前为了实现从 OB、OBProxy、主机跳转而来可以自动触发日志查询，先使用延迟请求
      setTimeout(() => {
        getLog();
      }, 500);
    }
  }, []);

  const downloadAllLog = () => {
    validateFields().then(values => {
      const { logLevel, excludeKeyword, hostIps: _hostIps } = values;
      const params = {
        logType: values.COPLogType,
        startTime,
        endTime,
        keyword: values.values,
        excludeKeyword,
        logLevel,
      };

      // 如果不存在选中的 IP，则使用全部 节点 IP 的第一个
      const myHostIps = _hostIps?.length > 0 ? _hostIps : allHostIps;

      const hostValue = hostId || myHostIps?.[0].value;
      if (hostValue) {
        const [ip, port] = hostValue?.split?.(':') || [];
        params.ip = ip;
        params.port = Number(port);
      }

      downloadLog(params);
    });
  };

  const renderLanguage = (type: string) => {
    let languageType = 'javaLog';
    if (LOG_TYPE_LIST) {
      if (LOG_TYPE_LIST[0].types?.includes(type) || LOG_TYPE_LIST[1].types?.includes(type)) {
        languageType = 'cpp';
      } else if (LOG_TYPE_LIST[2].types?.includes(type)) {
        languageType = 'go';
      }
    }
    setLanguage(languageType);
  };

  return (
    <Row gutter={[16, 16]}>
      <Col span={24}>
        <Card bordered={false} bodyStyle={{ paddingBottom: 0 }}>
          <Form form={form} requiredMark={false} {...FORM_ITEM_LAYOUT}>
            <Row>
              <Col span={12}>
                <FormItem
                  name="range"
                  label={formatMessage({
                    id: 'ocp-express.page.QueryLog.TimeRange',
                    defaultMessage: '时间范围',
                  })}
                >
                  <Ranger
                    allowClear={false}
                    style={{ width: '100%' }}
                    format={DATE_TIME_FORMAT_DISPLAY}
                    defaultQuickValue={formatMessage({
                      id: 'ocp-express.src.constant.log.NearlyHour',
                      defaultMessage: '近 1 小时',
                    })}
                    selects={getSelects()}
                    onChange={(value: Moment[]) => {
                      setRange(value || []);
                    }}
                  />
                </FormItem>
              </Col>
              <Col span={12}>
                <FormItem
                  name="COPLogType"
                  label={formatMessage({
                    id: 'ocp-express.page.QueryLog.LogType',
                    defaultMessage: '日志类型',
                  })}
                  rules={[
                    {
                      required: true,
                      message: formatMessage({
                        id: 'ocp-express.page.QueryLog.SelectTheLogTypeTo',
                        defaultMessage: '请选择需要查询的日志类型',
                      }),
                    },
                  ]}
                >
                  <LogTypeSelect
                    defaultLogType={defaultCOPLogType}
                    onChangeLogType={val => {
                      setFileType(val);
                    }}
                  />
                </FormItem>
              </Col>
            </Row>
            <Row>
              <Col span={12}>
                <FormItem
                  name="logLevel"
                  label={formatMessage({
                    id: 'ocp-express.page.QueryLog.LogLevel',
                    defaultMessage: '日志级别',
                  })}
                  initialValue={['WARN', 'ERROR']}
                >
                  <MySelect
                    mode="tags"
                    maxTagCount="responsive"
                    allowClear={true}
                    options={LOG_LEVEL.map((item: string) => ({
                      label: item,
                      value: item,
                    }))}
                  />
                </FormItem>
              </Col>
              <Col span={12}>
                <FormItem
                  name="hostIps"
                  label={formatMessage({
                    id: 'ocp-express.Log.Query.NodeIp',
                    defaultMessage: '节点 IP',
                  })}
                >
                  <IpSelect
                    getAllHostIps={v => {
                      getAllHostIps(v);
                    }}
                  />
                </FormItem>
              </Col>
            </Row>
            <Row>
              <Col span={12}>
                <FormItem
                  name="keyword"
                  label={formatMessage({
                    id: 'ocp-express.page.QueryLog.Keyword',
                    defaultMessage: '关键字',
                  })}
                >
                  <MySelect
                    mode="tags"
                    open={false}
                    allowClear={true}
                    tokenSeparators={SELECT_TOKEN_SPEARATORS}
                    maxTagCount="responsive"
                    placeholder={formatMessage({
                      id: 'ocp-express.Log.Query.ClickEnterToEnterMultiple',
                      defaultMessage: '点击回车可以输入多个',
                    })}
                  />
                </FormItem>
              </Col>
              <Col span={8}>
                <FormItem
                  labelCol={{ span: isEnglish() ? 8 : 6 }}
                  wrapperCol={{ span: isEnglish() ? 16 : 18 }}
                  name="excludeKeyword"
                  label={formatMessage({
                    id: 'ocp-express.Log.Query.ExcludeKeywords',
                    defaultMessage: '排除关键字',
                  })}
                >
                  <MySelect
                    mode="tags"
                    open={false}
                    allowClear={true}
                    tokenSeparators={SELECT_TOKEN_SPEARATORS}
                    maxTagCount="responsive"
                    placeholder={formatMessage({
                      id: 'ocp-express.Log.Query.ClickEnterToEnterMultiple',
                      defaultMessage: '点击回车可以输入多个',
                    })}
                  />
                </FormItem>
              </Col>
              <Col span={4}>
                <FormItem wrapperCol={{ span: 24 }}>
                  <Space style={{ float: 'right' }}>
                    <Button
                      data-aspm-click="c304250.d334073"
                      data-aspm-desc="日志查询-重置"
                      data-aspm-param={``}
                      data-aspm-expo
                      onClick={() => {
                        if (query.defaultOCPType) {
                          history.push('/log/query');
                        }
                        serDefaultCOPLogType(undefined);
                        resetFields();
                        setLogData(null);
                      }}
                    >
                      {formatMessage({
                        id: 'ocp-express.page.QueryLog.Reset',
                        defaultMessage: '重置',
                      })}
                    </Button>
                    <Tooltip
                      title={
                        allHostIps?.length === 0 &&
                        formatMessage({
                          id: 'ocp-express.Log.Query.TheAvailableNodeIpAddressIsMissingAnd',
                          defaultMessage: '缺少可用的节点 IP，无法进行日志查询',
                        })
                      }
                      placement="bottomLeft"
                    >
                      <Button
                        data-aspm-click="c304250.d334072"
                        data-aspm-desc="日志查询-查询"
                        data-aspm-param={``}
                        data-aspm-expo
                        loading={loading && !loadingMore}
                        type="primary"
                        disabled={allHostIps?.length === 0}
                        onClick={() => {
                          setLogData(null);
                          getLog();
                          if (COPLogType) {
                            setLogType(COPLogType[0]);
                          }
                        }}
                        style={loading ? { width: 94 } : {}}
                      >
                        {loading && !loadingMore
                          ? formatMessage({
                            id: 'ocp-express.page.QueryLog.Querying',
                            defaultMessage: '查询中',
                          })
                          : formatMessage({
                            id: 'ocp-express.page.QueryLog.Query',
                            defaultMessage: '查询',
                          })}
                      </Button>
                    </Tooltip>
                  </Space>
                </FormItem>
              </Col>
            </Row>
          </Form>
        </Card>
      </Col>
      <Col span={24}>
        {!logData ? (
          <Empty
            style={{ height: 'calc(100vh - 352px)' }}
            mode="pageCard"
            image="/assets/common/guide.svg"
            title={formatMessage({
              id: 'ocp-express.page.QueryLog.WelcomeToLogQuery',
              defaultMessage: '欢迎使用日志查询',
            })}
            description={formatMessage({
              id: 'ocp-express.Log.Query.SelectATimeRangeAndLogTypeFirst',
              defaultMessage: '请先选择时间范围、日志类型',
            })}
          />
        ) : (
          <FullscreenBox
            ref={boxRef}
            defaultMode="viewport"
            header={false}
            style={{ overflowY: 'auto' }}
            onChange={handleFullscreenChange}
          >
            <Card
              bordered={false}
              style={{ minHeight: 'calc(100vh - 352px)' }}
              tabBarExtraContent={
                <Space size={16}>
                  <Tooltip
                    placement="topRight"
                    title={
                      (notAllowDownload &&
                        formatMessage(
                          {
                            id: 'ocp-express.page.QueryLog.TheCurrentQueryTimeRange',
                            defaultMessage:
                              '当前查询时间范围超过 {limitHours} 小时，不支持下载。请将查询时间范围调整到 {limitHours} 小时以内',
                          },

                          { limitHours: limitHours }
                        )) ||
                      formatMessage(
                        {
                          id: 'ocp-express.page.QueryLog.DownloadOnlyLogsOnHost',
                          defaultMessage: '仅下载主机 {address} 上的日志',
                        },
                        { address: address }
                      )
                    }
                  >
                    <Button
                      data-aspm-click="c304250.d308747"
                      data-aspm-desc="日志查询-日志下载"
                      data-aspm-param={``}
                      data-aspm-expo
                      // disabled={notAllowDownload}
                      loading={downloadLoading}
                      onClick={downloadAllLog}
                    >
                      {formatMessage({
                        id: 'ocp-express.page.QueryLog.DownloadAllLogs',
                        defaultMessage: '下载日志',
                      })}
                    </Button>
                  </Tooltip>
                  {/*
              以现有的方式，暂时支持不了 全屏下的滚动，先暂时隐藏全屏入口，待实现全屏滚动后放开
              */}
                  {/* {fullscreen ? (
              <FullscreenExitOutlined
              onClick={() => {
              toggleFullscreen();
              }}
              />
              ) : (
              <FullscreenOutlined
              onClick={() => {
              toggleFullscreen();
              }}
              />
              )} */}
                </Space>
              }
              tabList={realHostIps?.map(item => ({
                key: item.value,
                tab: item.label,
              }))}
              activeTabKey={
                hostId?.toString() || (realHostIps && realHostIps[0]?.value?.toString())
              }
              onTabChange={key => {
                setHostId(key);
                getLog(key, logType);
              }}
            >
              {/* 切换对象范围，未选择主机查询前，不展示日志类型 */}
              {COPLogType?.length > 1 && realHostIps?.length !== 0 ? (
                <Radio.Group
                  // 查询时禁止点击，避免发送错误请求
                  disabled={loading}
                  style={{ marginBottom: 10 }}
                  options={COPLogType}
                  value={logType || COPLogType[0]}
                  optionType="button"
                  onChange={e => {
                    setLogType(e.target.value);
                    renderLanguage(e.target.value);
                    getLog(hostId, e.target.value);
                  }}
                />
              ) : null}

              {loading && !loadingMore ? (
                <Spin
                  spinning={loading}
                  tip={formatMessage({
                    id: 'ocp-express.page.QueryLog.Searching',
                    defaultMessage: '正在搜索中',
                  })}
                >
                  <div style={{ height: 'calc(100vh - 600px)' }} />
                </Spin>
              ) : flatten(logData?.logEntries || [])?.length === 0 ? (
                <Empty
                  mode="pageCard"
                  style={{ height: 'calc(100vh - 600px)' }}
                  description={formatMessage({
                    id: 'ocp-express.page.QueryLog.NoDataIsFound',
                    defaultMessage: '未查到符合条件的数据',
                  })}
                />
              ) : (
                <>
                  {/* observer obproxy 都是 cpp
                ocp-agent 是 go
                主机日志是操作系统生成的
                */}
                  {/*
                Highlight 组件有性能问题，分多个组件来渲染日志信息，借助react diff 每次只需计算新增加的部分，避免因性能问题致使浏览器崩溃
                */}
                  {logData?.logEntries?.map((item, index) => (
                    <LogHighlight
                      key={`${hostId + index}`}
                      keywordList={keyword}
                      language={language}
                      content={item && item.map(logs => logs.logLine)?.join('')}
                    />
                  ))}

                  <div style={{ textAlign: 'center', marginTop: 16, color: 'rgba(0, 0, 0, 0.45)' }}>
                    {isNoData ? (
                      <div>
                        {formatMessage({
                          id: 'ocp-express.page.QueryLog.NoMoreLogs',
                          defaultMessage: '没有更多日志',
                        })}
                      </div>
                    ) : (
                      <div ref={loadingMoreContainerRef}>
                        {inViewport ? (
                          <div>
                            <LoadingOutlined style={{ marginRight: 8 }} />
                            {formatMessage({
                              id: 'ocp-express.page.QueryLog.Loading',
                              defaultMessage: '正在加载',
                            })}
                          </div>
                        ) : null}
                      </div>
                    )}
                  </div>
                </>
              )}
            </Card>
          </FullscreenBox>
        )}
      </Col>
    </Row>
  );
};

export default QueryLog;
