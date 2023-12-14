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
import { connect, useDispatch } from 'umi';
import React, { useState, useEffect } from 'react';
import { Card, Table, Tooltip, Typography, Form, Tag, Modal } from '@oceanbase/design';
import { PageContainer } from '@oceanbase/ui';
import { sortByString, sortByMoment } from '@oceanbase/util';
import { PAGINATION_OPTION_10 } from '@/constant';
import useDocumentTitle from '@/hook/useDocumentTitle';
import { isEnglish } from '@/util';
import { formatTime } from '@/util/datetime';
import MyInput from '@/component/MyInput';
import ContentWithReload from '@/component/ContentWithReload';
import ContentWithQuestion from '@/component/ContentWithQuestion';

const FormItem = Form.Item;

const { Paragraph } = Typography;

export interface PropertyProps {
  location: {
    query: {
      keyword?: string;
    };
  };
  loading: boolean;
  propertyList: API.PropertyMeta[];
  submitLoading: boolean;
}

const PropertyPage: React.FC<PropertyProps> = ({
  location: {
    query: { keyword: defaultKeyword },
  },
  loading,
  propertyList,
  submitLoading,
}) => {
  useDocumentTitle(
    formatMessage({ id: 'ocp-express.page.Property.SystemParameters', defaultMessage: '系统配置' })
  );

  // 更新 Property 要求具有 PROPERTY:*:UPDATE 的权限，而不仅仅是单个 Property 的更新权限
  const dispatch = useDispatch();
  const [form] = Form.useForm();
  const { validateFields } = form;
  const [keyword, setKeyword] = useState(defaultKeyword);
  const [visible, setVisible] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<API.PropertyMeta | null>(null);

  const getPropertyList = () => {
    dispatch({
      type: 'property/getPropertyListData',
      payload: {},
    });
  };

  useEffect(() => {
    getPropertyList();
  }, []);

  function validateOcpSiteUrl(rule, value, callback) {
    if (value && value[value.length - 1] === '/') {
      callback(
        formatMessage({
          id: 'ocp-express.page.Property.TheUrlCannotBeFollowed',
          defaultMessage: 'URL 后面不能带 /',
        })
      );
    }
    callback();
  }

  function handleSubmit() {
    validateFields().then(values => {
      const { newValue } = values;
      dispatch({
        type: 'property/editProperty',
        payload: {
          id: currentRecord && currentRecord.id,
          newValue,
        },
        onSuccess: () => {
          setVisible(false);
          setCurrentRecord(null);
        },
      });
    });
  }

  // 增加是否重启生效筛选
  const restartList = [
    {
      value: false,
      label: formatMessage({ id: 'ocp-express.page.Property.No', defaultMessage: '否' }),
    },
    {
      value: true,
      label: formatMessage({ id: 'ocp-express.page.Property.Yes', defaultMessage: '是' }),
    },
  ];

  // 增加已生效和未生效参数筛选
  const valueChangeList = [
    {
      value: false,
      label: formatMessage({
        id: 'ocp-express.page.Property.NotEffective',
        defaultMessage: '未生效',
      }),
    },
    {
      value: true,
      label: formatMessage({ id: 'ocp-express.page.Property.Effective', defaultMessage: '已生效' }),
    },
  ];

  const columns = [
    // 目前分组都为 default，暂时不展示
    // {
    //   title: '分组',
    //   dataIndex: 'profile',
    //   sorter: (a: API.PropertyMeta, b: API.PropertyMeta) => sortByString(a, b, 'profile'),
    //   width: 120,
    // },
    {
      title: formatMessage({
        id: 'ocp-express.page.Property.ParameterName',
        defaultMessage: '参数名',
      }),
      dataIndex: 'key',
      width: '25%',
      ellipsis: true,
      sorter: (a: API.PropertyMeta, b: API.PropertyMeta) => sortByString(a, b, 'key'),
      render: (text: string) => {
        return (
          <Tooltip placement="topLeft" title={text}>
            <span>{text}</span>
          </Tooltip>
        );
      },
    },

    {
      title: formatMessage({ id: 'ocp-express.page.Property.Value', defaultMessage: '参数值' }),
      dataIndex: 'value',
      filters: valueChangeList.map(({ label, value }) => ({
        text: label,
        value,
      })),
      // runningValue 与 record.value 对比后，再与 filters 筛选值做对比
      onFilter: (value, record) => (record.value === record.runningValue) === value,

      render: (text: string, record: API.PropertyMeta) => {
        return (
          <span className="editable-text-wrapper">
            <Paragraph
              ellipsis={{
                tooltip: {
                  placement: 'topLeft',
                  title: text,
                },
              }}
              editable={
                // 具有编辑 Property 的权限才展示编辑入口
                {
                  editing: false,
                  onStart: () => {
                    setVisible(true);
                    setCurrentRecord(record);
                  },
                }
              }
            >
              {text || <span />}
            </Paragraph>
            {record.value !== record.runningValue && (
              <Tooltip
                placement="topLeft"
                title={
                  record.needRestart
                    ? formatMessage({
                        id: 'ocp-express.page.Property.RestartRequiredToTakeEffect',
                        defaultMessage: '需要重启生效',
                      })
                    : formatMessage({
                        id: 'ocp-express.page.Property.EffectiveWithinMinutes',
                        defaultMessage: '3分钟内生效',
                      })
                }
              >
                <Tag
                  color="volcano"
                  style={{
                    borderRadius: 10,
                    margin: '0 8px',
                    position: 'absolute',
                    top: 12,
                    right: 32,
                    ...(isEnglish()
                      ? {
                          whiteSpace: 'break-spaces',
                          wordBreak: 'break-all',
                        }
                      : {}),
                  }}
                >
                  {formatMessage({
                    id: 'ocp-express.page.Property.NotEffective',
                    defaultMessage: '未生效',
                  })}
                </Tag>
              </Tooltip>
            )}
          </span>
        );
      },
    },

    {
      title: formatMessage({ id: 'ocp-express.page.Property.Description', defaultMessage: '描述' }),
      dataIndex: 'description',
      width: '25%',
      ellipsis: true,
      render: (text: string) => (
        <Tooltip placement="topLeft" title={text}>
          <span>{text}</span>
        </Tooltip>
      ),
    },

    {
      title: (
        <ContentWithQuestion
          content={formatMessage({
            id: 'ocp-express.page.Property.WhetherTheRestartTakesEffect',
            defaultMessage: '是否重启生效',
          })}
          tooltip={{
            title: formatMessage({
              id: 'ocp-express.page.Property.WhetherTheParameterTakesEffectAfterTheOcp',
              defaultMessage: '参数是否在 OCP 服务器重启之后才能生效',
            }),
          }}
        />
      ),

      dataIndex: 'needRestart',
      width: isEnglish() ? 240 : undefined,
      filters: restartList.map(({ label, value }) => ({
        text: label,
        value,
      })),
      onFilter: (value, record) => record.needRestart === value,
      render: (text: string) => {
        return (
          <span>
            {text
              ? formatMessage({ id: 'ocp-express.page.Property.Yes', defaultMessage: '是' })
              : formatMessage({ id: 'ocp-express.page.Property.No', defaultMessage: '否' })}
          </span>
        );
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.page.Property.ModificationTime',
        defaultMessage: '修改时间',
      }),

      dataIndex: 'updateTime',
      sorter: (a: API.PropertyMeta, b: API.PropertyMeta) => sortByMoment(a, b, 'updateTime'),
      render: (text: string) => <span>{formatTime(text)}</span>,
    },
  ];

  return (
    <PageContainer
      ghost={true}
      header={{
        title: (
          <ContentWithReload
            content={formatMessage({
              id: 'ocp-express.page.Property.SystemParameters',
              defaultMessage: '系统配置',
            })}
            spin={loading}
            onClick={getPropertyList}
          />
        ),
      }}
    >
      <Card
        bordered={false}
        title={formatMessage({
          id: 'ocp-express.page.Property.ParameterList',
          defaultMessage: '参数列表',
        })}
        extra={
          <MyInput.Search
            data-aspm-click="c304243.d308723"
            data-aspm-desc="系统参数列表-搜索参数"
            data-aspm-param={``}
            data-aspm-expo
            value={keyword}
            allowClear={true}
            onChange={e => {
              setKeyword(e.target.value);
            }}
            placeholder={formatMessage({
              id: 'ocp-express.page.Property.SearchParameterName',
              defaultMessage: '搜索参数名称',
            })}
            className="search-input"
          />
        }
        className="card-without-padding"
      >
        <Table
          data-aspm="c304243"
          data-aspm-desc="系统参数列表"
          data-aspm-param={``}
          data-aspm-expo
          loading={loading}
          dataSource={propertyList.filter(
            item => !keyword || (item.key && item.key.includes(keyword))
          )}
          columns={columns}
          rowKey={record => record.id}
          pagination={PAGINATION_OPTION_10}
        />
      </Card>
      <Modal
        title={formatMessage({
          id: 'ocp-express.page.Property.ModifyTheValue',
          defaultMessage: '修改参数值',
        })}
        visible={visible}
        destroyOnClose={true}
        onCancel={() => {
          setVisible(false);
          setCurrentRecord(null);
        }}
        onOk={handleSubmit}
        confirmLoading={submitLoading}
      >
        <Form preserve={false}>
          <FormItem
            label={formatMessage({
              id: 'ocp-express.page.Property.ParameterName',
              defaultMessage: '参数名',
            })}
          >
            {currentRecord && currentRecord.key}
          </FormItem>
        </Form>
        <Form form={form} preserve={false} layout="vertical">
          <FormItem
            label={formatMessage({ id: 'ocp-express.page.Property.Value', defaultMessage: '取值' })}
            name="newValue"
            initialValue={currentRecord?.value}
            rules={
              currentRecord?.key === 'ocp.site.url'
                ? [
                    {
                      validator: validateOcpSiteUrl,
                    },
                  ]
                : []
            }
          >
            <MyInput.TextArea
              autoSize={{
                minRows: 3,
                // 最大高度为 388，与行数 17 的高度相近
                maxRows: 17,
              }}
            />
          </FormItem>
        </Form>
      </Modal>
    </PageContainer>
  );
};

function mapStateToProps({ loading, property }) {
  return {
    loading: loading.effects['property/getPropertyListData'],
    submitLoading: loading.effects['property/editProperty'],
    propertyList: (property.propertyListData && property.propertyListData.contents) || [],
  };
}

export default connect(mapStateToProps)(PropertyPage);
