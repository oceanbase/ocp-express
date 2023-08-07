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

import MyDrawer from '@/component/MyDrawer';
import MyInput from '@/component/MyInput';
import MySelect from '@/component/MySelect';
import SelectAllAndClearRender from '@/component/SelectAllAndClearRender';
import * as ObClusterParameterController from '@/service/ocp-express/ObClusterParameterController';
import * as ObTenantController from '@/service/ocp-express/ObTenantController';
import {
  getDetailComponentByParameterValue,
  getSimpleComponentByClusterParameterValue,
} from '@/util/component';
import { formatMessage } from '@/util/intl';
import { useSelector, useDispatch } from 'umi';
import {
  Alert,
  Button,
  Col,
  Form,
  Row,
  Table,
  Tree,
  Typography,
  Descriptions,
  message,
} from '@oceanbase/design';
import React, { useEffect, useState } from 'react';
import { flatten, isEqual, unionWith, uniq, uniqBy } from 'lodash';
import { DeleteOutlined, PlusOutlined } from '@ant-design/icons';
import { useRequest } from 'ahooks';
import useStyles from './index.style';

const { Option, OptGroup } = MySelect;
const { Text } = Typography;

export interface ModifyClusterParameterDrawerProps {
  parameter?: API.ClusterParameter;
  onSuccess?: () => void;
}

interface obTenantParametersType {
  value?: number | string | boolean;
  tenantName?: string;
  tenantId?: number;
  isNewValue?: boolean;
}

interface TreeNode {
  key?: string;
  title?: string;
  children?: TreeNode[];
}

const ModifyClusterParameterDrawer: React.FC<ModifyClusterParameterDrawerProps> = ({
  parameter = {},
  onSuccess,
  ...restProps
}) => {
  const { styles } = useStyles();
  const rangeList = [
    {
      name: formatMessage({
        id: 'ocp-express.component.ModifyClusterParameterDrawer.Cluster',
        defaultMessage: '集群',
      }),
      value: 'cluster',
    },

    {
      name: 'Zone',
      value: 'Zone',
    },

    {
      name: 'OBServer',
      value: 'OBServer',
    },
  ];

  const [form] = Form.useForm();
  const { getFieldValue, setFieldsValue, validateFields } = form;

  // 确认抽屉是否可见
  const [confirmVisible, setConfirmVisible] = useState(false);
  // 查看参数值抽屉是否可见
  const [checkVisible, setCheckVisible] = useState(false);
  // 选择全部租户
  const [allTenantSelectedStatus, setAllTenantSelectedStatus] = useState('allowed');
  // 判断是否有当前参数对应多个生效对象
  let hasSameScope = false;

  const { clusterData } = useSelector((state: DefaultRootState) => state.cluster);
  const dispatch = useDispatch();

  const getClusterData = () => {
    dispatch({
      type: 'cluster/getClusterData',
      payload: {},
    });
  };

  useEffect(() => {
    if (!clusterData?.clusterName) {
      getClusterData();
    }
  }, []);

  useEffect(() => {
    if (parameter?.parameterType === 'OB_TENANT_PARAMETER') {
      getTenantList({});
    }
  }, [parameter]);

  const { run: updateClusterParameter, loading } = useRequest(
    ObClusterParameterController.updateClusterParameter,
    {
      manual: true,
      onSuccess: (res) => {
        if (res?.successful) {
          message.success(
            formatMessage({
              id: 'ocp-express.component.ModifyClusterParameterDrawer.ParameterValueModified',
              defaultMessage: '参数值修改成功',
            }),
          );
          setConfirmVisible(false);
          if (onSuccess) {
            onSuccess();
          }
        }
      },
    },
  );

  const handleModifyValue = () => {
    validateFields().then((values) => {
      const { parameter: modifyParameter } = values;
      let otherrParam = {};
      // 修改租户类型参数 整理接口数据
      if (parameter?.parameterType === 'OB_TENANT_PARAMETER') {
        otherrParam = modifyParameter?.map((item) => {
          if (item?.target?.filter((o) => o.value === 'all')?.length !== 0) {
            return {
              name: parameter?.name,
              value: item.value,
              parameterType: parameter?.parameterType,
              allTenants: true,
            };
          } else {
            return {
              name: parameter?.name,
              value: item.value,
              parameterType: parameter?.parameterType,
              tenants: item.target.map((o) => o.label),
            };
          }
        });
      } else {
        // 修改集群类型参数 整理接口数据
        otherrParam = modifyParameter?.map((item) => {
          // 含有 Zone 级别
          if (item.applyTo === 'Zone') {
            return {
              name: parameter?.name,
              value: item.value,
              parameterType: parameter?.parameterType,
              zones: item.target.map((o) => o.label),
            };
          } else if (item?.applyTo === 'OBServer') {
            // 含有 OBServer 级别
            return {
              name: parameter?.name,
              value: item.value,
              parameterType: parameter?.parameterType,
              servers: item.target.map((o) => o.label),
            };
          } else {
            // 集群级别
            return {
              name: parameter?.name,
              value: item.value,
              parameterType: parameter?.parameterType,
            };
          }
        });
      }

      updateClusterParameter(otherrParam);
    });
  };

  // 对集群下的 Zone 格式整理
  const zoneList = (clusterData?.zones || []).map((item) => ({
    label: item.name,
    value: item.name,
    serverList: (item.servers || []).map((server) => ({
      ip: server.ip,
      id: server.id,
      port: server.port,
    })),
  }));

  // 获取当前集群下的租户
  const { data, run: getTenantList } = useRequest(ObTenantController.listTenants, {
    manual: true,
    defaultParams: [{}],
  });

  const tenantList = data?.data?.contents || [];
  const editParameter = getFieldValue(['parameter']);

  if (parameter?.parameterType === 'OB_CLUSTER_PARAMETER') {
    const serverList: string[] = [];
    if (editParameter?.length > 0 && editParameter[0] !== undefined) {
      editParameter.map((item) => {
        if (item?.applyTo === 'cluster') {
          zoneList.map((zone) => zone.serverList.map((server) => serverList.push(server.ip)));
        } else if (item?.applyTo === 'Zone') {
          zoneList.map((zone) => {
            item?.target?.map((target) => {
              if (target.label === zone.label) {
                zone.serverList.map((server) => serverList.push(server.ip));
              }
            });
          });
        } else if (item?.applyTo === 'OBServer') {
          zoneList.map((zone) => {
            item?.target?.map((target) => {
              zone.serverList.map((server) => {
                if (target.label?.split(':')[0] === server.ip) {
                  serverList.push(server.ip);
                }
              });
            });
          });
        }
      });
    }
    hasSameScope = uniq(serverList).length < serverList.length;
  }

  // 将整理数据为适合 Tree 渲染的数据
  const getParameterValue = () => {
    const { currentValue } = parameter;
    if (editParameter?.length > 0 && editParameter[0] !== undefined) {
      // 修改租户类型参数 数据整理
      if (parameter.parameterType === 'OB_TENANT_PARAMETER') {
        const obTenantParameters: obTenantParametersType[] = [];
        currentValue?.tenantValues?.map((tenantValue) => {
          editParameter.map((item) => {
            // 如果选择了 全部租户
            if (item?.target?.filter((o) => o.value === 'all')?.length !== 0) {
              obTenantParameters.push({
                tenantName: tenantValue.tenantName,
                value: item.value,
                tenantId: tenantValue.tenantId,
                isNewValue: true,
              });
            } else {
              item?.target?.map((target) => {
                if (tenantValue.tenantName === target.label) {
                  obTenantParameters.push({
                    tenantName: target.label,
                    value: item.value,
                    tenantId: tenantValue.tenantId,
                    isNewValue: true,
                  });
                }
              });
            }
          });
        });
        return uniqBy(
          unionWith(obTenantParameters, currentValue?.tenantValues, isEqual),
          'tenantName',
        );
      } else {
        // 修改参数值，取原参数值生成 treeData
        let parametersTreeData: TreeNode[] = [];
        let treeData: TreeNode[] = [];
        const treeNoeList: TreeNode[] = [];
        // 收集所修改参数值 生效范围的 server
        treeData = [
          {
            title: 'cluster',
            key: '1',
            children: clusterData?.zones?.map((zone, index) => ({
              title: zone.name,
              key: `1-${index}`,
              children: zone?.servers?.map((server, key) => ({
                title: `${server?.ip}:${server.port}`,
                key: `1-${index}-${key}`,
              })),
            })),
          },
        ];

        treeData[0]?.children?.map((item) => {
          currentValue?.obParameters?.map((obParameter) => {
            if (obParameter.zone === item.title) {
              treeNoeList.push({
                title: item?.title,
                key: item.key,
                children: item?.children?.map((server) => ({
                  title: obParameter?.value
                    ? `${server?.title}：${obParameter?.value}`
                    : `${server?.title}`,
                  key: server.key,
                })),
              });
            }
          });
        });

        parametersTreeData = [
          {
            title: 'cluster',
            key: '1',
            children: uniqBy(treeNoeList, 'title'),
          },
        ];

        editParameter?.map((item) => {
          // 修改参数值，生效范围包含集群
          if (item?.applyTo === 'cluster') {
            parametersTreeData = [
              {
                title: 'cluster',
                key: '1',
                children: clusterData?.zones?.map((zone, index) => ({
                  title: zone.name,
                  key: `1-${index}`,
                  children: zone?.servers?.map((server, key) => {
                    return {
                      title: `${server?.ip}:${server?.port}：${item?.value}`,
                      key: `1-${index}-${key}`,
                      isNewValue:
                        parametersTreeData[0]?.children[index]?.children[key]?.title?.split(
                          ': ',
                        )[1] !== item?.value,
                    };
                  }),
                })),
              },
            ];
          } else if (item?.applyTo === 'Zone') {
            item?.target?.forEach((targetZone) => {
              parametersTreeData = parametersTreeData.map((param) => ({
                title: 'cluster',
                key: '1',
                children: param?.children?.map((zone, index) => {
                  if (targetZone.label === zone?.title) {
                    return {
                      key: zone.key,
                      title: zone.title,
                      children: zone?.children?.map((server, key) => {
                        return {
                          title: `${server?.title?.split('：')[0]}：${item?.value}`,
                          key: `1-${index}-${key}`,
                          isNewValue: server?.title?.split('：')[1] !== item?.value,
                        };
                      }),
                    };
                  } else {
                    return zone;
                  }
                }),
              }));
            });
          } else if (item?.applyTo === 'OBServer') {
            item?.target?.map((targetZone) => {
              parametersTreeData = parametersTreeData.map((param) => ({
                title: 'cluster',
                key: '1',
                children: param?.children?.map((zone) => ({
                  title: zone.title,
                  key: zone.key,
                  children: zone?.children?.map((server) => {
                    if (
                      targetZone?.label?.split('：')[0] === server?.title?.split('：')[0] ||
                      `${targetZone?.label?.split('：')[0]}:${targetZone?.port}` ===
                        server?.title?.split('：')[0]
                    ) {
                      return {
                        key: server.key,
                        title: `${server?.title?.split('：')[0]}：${item?.value}`,
                        isNewValue: server?.title?.split('：')[1] !== item?.value,
                      };
                    } else {
                      return server;
                    }
                  }),
                })),
              }));
            });
          }
        });
        return parametersTreeData;
      }
    }
  };

  const confirmColumns = [
    {
      title: formatMessage({
        id: 'ocp-express.component.ModifyClusterParameterDrawer.ValueBeforeChange',
        defaultMessage: '值-变更前',
      }),

      width: '50%',
      dataIndex: 'oldValue',
      render: () => getDetailComponentByParameterValue(parameter, clusterData),
    },

    {
      title: formatMessage({
        id: 'ocp-express.component.ModifyClusterParameterDrawer.ValueAfterChange',
        defaultMessage: '值-变更后',
      }),

      width: '50%',
      dataIndex: 'newValue',
      render: (text) => {
        if (parameter?.parameterType === 'OB_TENANT_PARAMETER') {
          return text
            ?.sort((a, b) => {
              const nameA = a?.tenantName?.toUpperCase();
              const nameB = b?.tenantName?.toUpperCase();
              if (nameA < nameB) {
                return -1;
              }
              if (nameA > nameB) {
                return 1;
              }
              return 0;
            })
            ?.map((item) => (
              <div className={item?.isNewValue ? styles.newValue : styles.tenantName}>
                {`${item?.tenantName}: ${item?.value}`}
              </div>
            ));
        }
        return (
          <Tree
            defaultExpandAll={true}
            treeData={text}
            titleRender={(node) => {
              // 改动的值标记颜色
              if (node?.isNewValue) {
                return <div className={styles.treeNewValue}>{node.title}</div>;
              }
              return <div>{node.title}</div>;
            }}
          />
        );
      },
    },
  ];

  const getSelectAllOptions = (applyTo: string) => {
    if (applyTo === 'OBServer') {
      const selectAllOptions = [];
      zoneList?.map((zone) => {
        zone?.serverList.map((server) =>
          selectAllOptions.push({
            value: server.id,
            label: `${server.ip}:${server.port}`,
            port: server.port,
          }),
        );
      });
      return selectAllOptions;
    } else {
      return zoneList.map((zone) => ({ value: zone.value, label: zone.label }));
    }
  };

  const filterTenantList = (tenantData, text, tenants) => {
    const selectedtenantList: number[] = flatten(
      (tenants && tenants?.map((item) => item?.target?.map((o) => o?.value) || [])) || [],
    );

    return tenantData.filter(
      (item) =>
        !selectedtenantList
          .filter((tenant) => !(text || []).includes(tenant))
          .includes(item.obTenantId),
    );
  };

  return (
    <MyDrawer
      className={styles.drawer}
      width={966}
      title={formatMessage({
        id: 'ocp-express.component.ModifyClusterParameterDrawer.ModifyParameterValues',
        defaultMessage: '修改参数值',
      })}
      onOk={() => {
        validateFields().then(() => {
          setConfirmVisible(true);
        });
      }}
      {...restProps}
    >
      {parameter?.parameterType === 'OB_CLUSTER_PARAMETER' && (
        <Alert
          message={formatMessage({
            id: 'ocp-express.component.ModifyClusterParameterDrawer.RelationshipBetweenValueAndEffective',
            defaultMessage: '值和生效范围的关系',
          })}
          description={
            <>
              <div>
                {formatMessage({
                  id: 'ocp-express.component.ModifyClusterParameterDrawer.WhenTheEffectiveRangeIs',
                  defaultMessage:
                    '·生效范围为集群时，表示集群当前 OBServer 和此集群后续添加的 OBServer 都使用此值',
                })}
              </div>
              <div>
                {formatMessage({
                  id: 'ocp-express.component.ModifyClusterParameterDrawer.WhenTheEffectiveRangeIs.1',
                  defaultMessage:
                    '·生效范围为 Zone 时，表示所选择 Zone 的当前 OBServer 和此 Zone 后续添加的 OBServer\n                都使用此值',
                })}
              </div>
              <div>
                {formatMessage({
                  id: 'ocp-express.component.ModifyClusterParameterDrawer.WhenTheEffectiveRangeIs.2',
                  defaultMessage: '·生效范围为 OBServer 时，表示所选择的 OBServer 使用此值',
                })}
              </div>
            </>
          }
          type="info"
          showIcon={true}
          style={{ marginBottom: 24 }}
        />
      )}

      <Descriptions column={3}>
        <Descriptions.Item
          span={2}
          label={formatMessage({
            id: 'ocp-express.component.ModifyClusterParameterDrawer.Parameter',
            defaultMessage: '参数名',
          })}
        >
          {parameter.name}
        </Descriptions.Item>
        <Descriptions.Item
          span={1}
          label={formatMessage({
            id: 'ocp-express.component.ModifyClusterParameterDrawer.CurrentValue',
            defaultMessage: '当前值',
          })}
          className="descriptions-item-with-ellipsis"
        >
          {parameter.currentValue && (
            <Text ellipsis={{ tooltip: true, placement: 'topRight ' }}>
              {getSimpleComponentByClusterParameterValue(
                parameter.currentValue,
                parameter?.parameterType,
                () => {
                  setCheckVisible(true);
                },
              )}
            </Text>
          )}
        </Descriptions.Item>
      </Descriptions>
      <Form form={form} layout="vertical" requiredMark={false} preserve={false}>
        <Form.List name="parameter" initialValue={[{ key: 0 }]}>
          {(fields, { add, remove: removeItem }) => {
            return (
              <>
                {fields.map((field, index) => (
                  <div key={field.key}>
                    <Row gutter={[8, 4]}>
                      <Col span={9}>
                        <Form.Item
                          {...field}
                          label={
                            index === 0 &&
                            formatMessage({
                              id: 'ocp-express.component.ModifyClusterParameterDrawer.Value',
                              defaultMessage: '值',
                            })
                          }
                          name={[field.name, 'value']}
                          fieldKey={[field.fieldKey, 'value']}
                          rules={[
                            {
                              required: true,
                              message: formatMessage({
                                id: 'ocp-express.component.ModifyClusterParameterDrawer.EnterAValue',
                                defaultMessage: '请输入值',
                              }),
                            },
                          ]}
                        >
                          <MyInput
                            placeholder={formatMessage({
                              id: 'ocp-express.component.ModifyClusterParameterDrawer.Value',
                              defaultMessage: '值',
                            })}
                            style={{ width: '100%' }}
                            allowClear={true}
                          />
                        </Form.Item>
                      </Col>
                      {parameter?.parameterType === 'OB_TENANT_PARAMETER' ? (
                        <Col span={14}>
                          <Form.Item noStyle shouldUpdate={true}>
                            {() => {
                              const currentParameter = getFieldValue(['parameter']);
                              const params = currentParameter[index];
                              return (
                                <Form.Item
                                  {...field}
                                  label={
                                    index === 0 &&
                                    formatMessage({
                                      id: 'ocp-express.component.ModifyClusterParameterDrawer.EffectiveObject',
                                      defaultMessage: '生效对象',
                                    })
                                  }
                                  name={[field.name, 'target']}
                                  fieldKey={[field.fieldKey, 'target']}
                                  rules={[
                                    {
                                      required: true,
                                      message: formatMessage({
                                        id: 'ocp-express.component.ModifyClusterParameterDrawer.EnterAnEffectiveObject',
                                        defaultMessage: '请输入生效对象',
                                      }),
                                    },
                                  ]}
                                >
                                  <MySelect
                                    mode="tags"
                                    allowClear={true}
                                    labelInValue={true}
                                    placeholder={formatMessage({
                                      id: 'ocp-express.component.ModifyClusterParameterDrawer.SelectAnActiveObjectFirst',
                                      defaultMessage: '请先选择生效对象',
                                    })}
                                    onChange={(val) => {
                                      const values = val?.map((item) => item.value);
                                      if (values.length === 0) {
                                        setAllTenantSelectedStatus('allowed');
                                      } else if (values[0] === 'all') {
                                        setAllTenantSelectedStatus('selected');
                                      } else {
                                        setAllTenantSelectedStatus('disabled');
                                      }
                                    }}
                                  >
                                    <Option
                                      value="all"
                                      key="all"
                                      label={formatMessage({
                                        id: 'ocp-express.component.ModifyClusterParameterDrawer.AllTenants',
                                        defaultMessage: '全部租户',
                                      })}
                                      disabled={allTenantSelectedStatus === 'disabled'}
                                    >
                                      {formatMessage({
                                        id: 'ocp-express.component.ModifyClusterParameterDrawer.AllTenants',
                                        defaultMessage: '全部租户',
                                      })}
                                    </Option>
                                    {
                                      // 租户类型参数，租户只可赋值一次
                                      filterTenantList(
                                        tenantList,
                                        params?.target?.map((item) => item?.value),
                                        currentParameter,
                                      )
                                        ?.sort((a, b) => {
                                          const nameA = a?.name?.toUpperCase();
                                          const nameB = b?.name?.toUpperCase();
                                          if (nameA < nameB) {
                                            return -1;
                                          }
                                          if (nameA > nameB) {
                                            return 1;
                                          }
                                          return 0;
                                        })
                                        ?.map((item) => {
                                          return (
                                            <Option
                                              value={item.obTenantId}
                                              key={item.obTenantId}
                                              label={item.name}
                                              disabled={allTenantSelectedStatus === 'selected'}
                                            >
                                              {item.name}
                                            </Option>
                                          );
                                        })
                                    }
                                  </MySelect>
                                </Form.Item>
                              );
                            }}
                          </Form.Item>
                        </Col>
                      ) : (
                        <>
                          <Col span={4}>
                            <Form.Item
                              {...field}
                              label={
                                index === 0 &&
                                formatMessage({
                                  id: 'ocp-express.component.ModifyClusterParameterDrawer.EffectiveRange',
                                  defaultMessage: '生效范围',
                                })
                              }
                              name={[field.name, 'applyTo']}
                              fieldKey={[field.fieldKey, 'applyTo']}
                              rules={[
                                {
                                  required: true,
                                  message: formatMessage({
                                    id: 'ocp-express.component.ModifyClusterParameterDrawer.EnterAValidRange',
                                    defaultMessage: '请输入生效范围',
                                  }),
                                },
                              ]}
                            >
                              <MySelect
                                allowClear={true}
                                onChange={(val) => {
                                  const currentParameter = getFieldValue(['parameter']);
                                  setFieldsValue({
                                    parameter: currentParameter?.map((param, key) =>
                                      index === key
                                        ? {
                                            applyTo: val,
                                            value: param.value,
                                            target: [],
                                          }
                                        : param,
                                    ),
                                  });
                                }}
                              >
                                {rangeList.map((item) => {
                                  return (
                                    <Option value={item.value} key={item.value}>
                                      {item.name}
                                    </Option>
                                  );
                                })}
                              </MySelect>
                            </Form.Item>
                          </Col>
                          <Col span={10}>
                            <Form.Item noStyle shouldUpdate={true}>
                              {() => {
                                const currentParameter = getFieldValue(['parameter']);
                                const params = getFieldValue(['parameter'])[index];
                                return (
                                  <Form.Item
                                    {...field}
                                    label={
                                      index === 0 &&
                                      formatMessage({
                                        id: 'ocp-express.component.ModifyClusterParameterDrawer.EffectiveObject',
                                        defaultMessage: '生效对象',
                                      })
                                    }
                                    name={[field.name, 'target']}
                                    fieldKey={[field.fieldKey, 'target']}
                                    rules={[
                                      {
                                        required: params?.applyTo !== 'cluster',
                                        message: formatMessage({
                                          id: 'ocp-express.component.ModifyClusterParameterDrawer.EnterAnEffectiveObject',
                                          defaultMessage: '请输入生效对象',
                                        }),
                                      },
                                    ]}
                                  >
                                    <MySelect
                                      mode="tags"
                                      allowClear={true}
                                      labelInValue={true}
                                      maxTagCount={params?.applyTo === 'OBServer' ? 2 : 3}
                                      disabled={!params?.applyTo || params?.applyTo === 'cluster'}
                                      placeholder={
                                        params?.applyTo !== 'cluster' &&
                                        formatMessage({
                                          id: 'ocp-express.component.ModifyClusterParameterDrawer.SelectAValidRange',
                                          defaultMessage: '请先选择生效范围',
                                        })
                                      }
                                      dropdownRender={(menu) => (
                                        <SelectAllAndClearRender
                                          menu={menu}
                                          onSelectAll={() => {
                                            setFieldsValue({
                                              parameter: currentParameter?.map((param, key) => {
                                                return index === key
                                                  ? {
                                                      applyTo: param?.applyTo,
                                                      value: param.value,
                                                      target: getSelectAllOptions(param?.applyTo),
                                                    }
                                                  : param;
                                              }),
                                            });
                                          }}
                                          onClearAll={() => {
                                            setFieldsValue({
                                              parameter: currentParameter?.map((param, key) =>
                                                index === key
                                                  ? {
                                                      applyTo: param?.applyTo,
                                                      value: param.value,
                                                      target: [],
                                                    }
                                                  : param,
                                              ),
                                            });
                                          }}
                                        />
                                      )}
                                    >
                                      {params?.applyTo === 'Zone' &&
                                        zoneList.map((item) => {
                                          return (
                                            <Option value={item.value} key={item.value}>
                                              {item.label}
                                            </Option>
                                          );
                                        })}
                                      {params?.applyTo === 'OBServer' &&
                                        zoneList?.map((zone) => {
                                          return (
                                            <OptGroup label={zone.label}>
                                              {zone?.serverList.map((server) => (
                                                <Option key={server.id} value={server.id}>
                                                  {`${server.ip}:${server.port}`}
                                                </Option>
                                              ))}
                                            </OptGroup>
                                          );
                                        })}
                                    </MySelect>
                                  </Form.Item>
                                );
                              }}
                            </Form.Item>
                          </Col>
                        </>
                      )}

                      <Col span={1}>
                        <Form.Item noStyle shouldUpdate={true}>
                          {() => {
                            const currentParameter = getFieldValue(['parameter']);
                            return currentParameter?.length > 1 ? (
                              <Form.Item label={index === 0 && ' '} style={{ marginBottom: 8 }}>
                                <DeleteOutlined
                                  style={{ color: 'rgba(0,0,0, .45)' }}
                                  onClick={() => {
                                    removeItem(field.name);
                                  }}
                                />
                              </Form.Item>
                            ) : null;
                          }}
                        </Form.Item>
                      </Col>
                    </Row>
                  </div>
                ))}

                <Row gutter={8}>
                  <Col span={23}>
                    <Form.Item style={{ marginBottom: 8 }}>
                      <Button
                        type="dashed"
                        // 修改租户级别参数，选择全部租户后，不可再新增一行
                        disabled={
                          parameter?.parameterType === 'OB_TENANT_PARAMETER' &&
                          allTenantSelectedStatus === 'selected'
                        }
                        onClick={() => {
                          validateFields().then(() => {
                            add();
                          });
                        }}
                        style={{ width: '100%' }}
                      >
                        <PlusOutlined />
                        {formatMessage({
                          id: 'ocp-express.component.ModifyClusterParameterDrawer.AddValue',
                          defaultMessage: '添加值',
                        })}
                      </Button>
                    </Form.Item>
                  </Col>
                </Row>
              </>
            );
          }}
        </Form.List>
      </Form>
      <MyDrawer
        className={styles.confirmDrawer}
        width={688}
        title={formatMessage({
          id: 'ocp-express.component.ModifyClusterParameterDrawer.ConfirmValueModificationInformation',
          defaultMessage: '确认值修改信息',
        })}
        visible={confirmVisible}
        confirmLoading={loading}
        onCancel={() => {
          setConfirmVisible(false);
        }}
        onOk={handleModifyValue}
      >
        {parameter.needRestart && (
          <Alert
            type="warning"
            message={formatMessage({
              id: 'ocp-express.component.ModifyClusterParameterDrawer.NoteThatTheCurrentParameter',
              defaultMessage: '请注意，当前参数修改需要重启 OBServer 才能生效。',
            })}
            showIcon={true}
            className={styles.restartAlert}
          />
        )}

        {hasSameScope && (
          <Alert
            type="warning"
            message={formatMessage({
              id: 'ocp-express.component.ModifyClusterParameterDrawer.CurrentlyMultipleValuesGouXuan',
              defaultMessage: '当前存在多个值勾选同一生效范围，系统将按最新修改项自动分配结果。',
            })}
            showIcon={true}
            className={styles.alert}
          />
        )}

        <Table
          bordered={true}
          columns={confirmColumns}
          dataSource={[
            {
              key: '1',
              // oldValue: valueToServers,
              newValue: getParameterValue(),
            },
          ]}
          rowKey={(record) => record.key}
          pagination={false}
          className="table-without-hover-style"
        />
      </MyDrawer>
      <MyDrawer
        className={styles.confirmDrawer}
        width={480}
        title={formatMessage(
          {
            id: 'ocp-express.component.ModifyClusterParameterDrawer.ViewParameternameParameterValues',
            defaultMessage: '查看 {parameterName} 参数值',
          },
          { parameterName: parameter.name },
        )}
        visible={checkVisible}
        footer={false}
        onCancel={() => {
          setCheckVisible(false);
        }}
      >
        {getDetailComponentByParameterValue(parameter, clusterData)}
      </MyDrawer>
    </MyDrawer>
  );
};

export default ModifyClusterParameterDrawer;
