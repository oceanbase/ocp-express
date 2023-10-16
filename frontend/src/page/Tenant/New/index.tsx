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
  Checkbox,
  Col,
  Form,
  InputNumber,
  Row,
  Space,
  Spin,
  Card,
  Switch,
  Table,
  Tooltip,
} from '@oceanbase/design';
import type { Route } from '@oceanbase/design/es/breadcrumb/Breadcrumb';
import React, { useEffect, useState } from 'react';
import { uniq } from 'lodash';
import { findBy, isNullValue } from '@oceanbase/util';
import { PageContainer } from '@oceanbase/ui';
import { useRequest } from 'ahooks';
import * as IamController from '@/service/ocp-express/IamController';
import * as ObTenantController from '@/service/ocp-express/ObTenantController';
import * as ObClusterController from '@/service/ocp-express/ObClusterController';
import { NAME_RULE, PASSWORD_REGEX } from '@/constant';
import { REPLICA_TYPE_LIST } from '@/constant/oceanbase';
import { TENANT_MODE_LIST } from '@/constant/tenant';
import { getTextLengthRule, validatePassword } from '@/util';
import { getUnitSpecLimit, getResourcesLimit } from '@/util/cluster';
import encrypt from '@/util/encrypt';
import { getMinServerCount } from '@/util/tenant';
import { validateUnitCount } from '@/util/oceanbase';
import { breadcrumbItemRender } from '@/util/component';
import useDocumentTitle from '@/hook/useDocumentTitle';
import MyInput from '@/component/MyInput';
import MyCard from '@/component/MyCard';
import MySelect from '@/component/MySelect';
import Password from '@/component/Password';
import WhitelistInput from '@/component/WhitelistInput';
import ContentWithQuestion from '@/component/ContentWithQuestion';
import FormPrimaryZone from '@/component/FormPrimaryZone';
import SetParameterEditableProTable from '@/component/ParameterTemplate/SetParameterEditableProTable';
import useStyles from './index.style';

const { Option } = MySelect;

interface collation {
  isDefault: boolean;
  name: string;
}

interface NewProps {
  location: {
    query: {
      tenantId?: number;
    };
  };
}

const New: React.FC<NewProps> = ({
  location: {
    query: { tenantId: defaultTenantId },
  },
}) => {
  // const { clusterData } = useSelector((state: DefaultRootState) => state.cluster);
  const { styles } = useStyles();

  const [form] = Form.useForm();
  const { getFieldValue, validateFields, setFieldsValue } = form;
  // 是否在指定集群下复制租户
  const isClone = !isNullValue(defaultTenantId);

  const [passed, setPassed] = useState(true);
  const [currentMode, setCurrentMode] = useState('MYSQL');
  const [collations, setCollations] = useState<API.Collation[]>([]);

  // const [initParameters, setInitParameters] = useState([]);
  // 参数设置开关
  const [advanceSettingSwitch, setAdvanceSettingSwitch] = useState(false);
  const [compatibleOracleAlret, setCompatibleOracleAlret] = useState(false);

  // 推荐
  useDocumentTitle(
    isClone
      ? formatMessage({ id: 'ocp-express.Tenant.New.CopyTenant', defaultMessage: '复制租户' })
      : formatMessage({ id: 'ocp-express.Tenant.New.CreateATenant', defaultMessage: '新建租户' })
  );

  const {
    data,
    loading: clusterDataLoading,
    runAsync: getClusterData,
  } = useRequest(ObClusterController.getClusterInfo, {
    manual: isClone,
    defaultParams: [{}],
  });

  const clusterData = data?.data || {};
  const minServerCount = getMinServerCount(clusterData?.zones);

  // 获取 unit 规格的限制规则
  const { data: clusterUnitSpecLimitData } = useRequest(
    ObClusterController.getClusterUnitSpecLimit,
    {
      manual: false,
      defaultParams: [{}],
    }
  );

  const clusterUnitSpecLimit = clusterUnitSpecLimitData?.data || {};

  const { data: charsetListData, runAsync: listCharsets } = useRequest(
    ObClusterController.listCharsets,
    {
      manual: false,
    }
  );

  const charsetList = charsetListData?.data?.contents || [];

  useEffect(() => {
    getClusterData({}).then(() => {
      // 获取字符集列表后再设置 collation 列表，因为两者有先后依赖关系
      listCharsets({
        tenantMode: 'MYSQL',
      }).then(charsetRes => {
        handleCharsetChange('utf8mb4', charsetRes.data?.contents || []);
      });
      if (isClone) {
        getTenantData({
          tenantId: defaultTenantId,
        });
      }
    });
  }, []);

  useEffect(() => {
    if (!isClone) {
      setFieldsValue({
        zones: (clusterData?.zones || []).map(item => ({
          checked: true,
          name: item.name,
          replicaType: 'FULL',
          unitCount: undefined,
        })),

        mode: 'MYSQL',
        charset: 'utf8mb4',
        primaryZone: '',
      });
    }
  }, [clusterData]);

  // 获取租户数据: 仅在复制租户时调用
  const { data: tenantInfo, run: getTenantData } = useRequest(ObTenantController.getTenant, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        const sourceTenantData = res?.data || {};

        const sourceZones = (sourceTenantData?.zones || []).map(item => ({
          name: item.name,
          replicaType: item.replicaType,
          unitCount: item.resourcePool?.unitCount,
          unitConfig: item.resourcePool?.unitConfig,
          ...item?.unitSpec,
        }));

        const charset = sourceTenantData.charset || 'utf8mb4';
        setFieldsValue({
          name: `${sourceTenantData.name}_clone`,
          zonesUnitCount: tenantData.zones?.map(item => item.units?.length)?.[0] || 1,
          zones: (clusterData.zones || []).map(zone => {
            const replicaZone = sourceZones.find(item => item.name === zone.name);

            return {
              name: zone.name,
              cpuCore: replicaZone?.unitConfig?.maxCpuCoreCount,
              memorySize: replicaZone?.unitConfig?.maxMemorySize,
              ...(replicaZone
                ? {
                    // 已分布副本的 Zone，已勾选
                    checked: true,
                    ...replicaZone,
                  }
                : {
                    // 未分布副本的 Zone，未勾选
                    checked: false,
                    replicaType: 'FULL',
                  }),
            };
          }),
          mode: sourceTenantData.mode,
          charset,
          collation: sourceTenantData.collation || 'utf8mb4_general_ci',
          primaryZone: sourceTenantData.primaryZone,
          description: sourceTenantData.description,
          whitelist: sourceTenantData.whitelist,
        });

        // 获取字符集列表后再设置 collation 列表，因为两者有先后依赖关系
        listCharsets({
          tenantMode: sourceTenantData.mode,
        }).then(charsetRes => {
          // 需要从 charsetRes 获取最新的字符集列表
          const newChartsetList = charsetRes.data?.contents || [];
          setCollations(findBy(newChartsetList, 'name', charset)?.collations || []);
        });
      }
    },
  });

  const tenantData = tenantInfo?.data || {};

  const { runAsync: getLoginKey } = useRequest(IamController.getLoginKey, {
    manual: true,
  });

  // 创建租户更改为异步
  const { run: addTenant, loading: createTenantLoading } = useRequest(
    ObTenantController.createTenant,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          const taskId = res?.data?.id;
          history.push(`/tenant/result/${taskId}`);
        }
      },
    }
  );

  const handleSubmit = () => {
    validateFields().then(values => {
      const { zones, zonesUnitCount, rootPassword, ...restValues } = values;
      getLoginKey().then(response => {
        const publicKey = response?.data?.publicKey || '';
        addTenant({
          ...restValues,
          rootPassword: encrypt(rootPassword, publicKey),
          zones: zones
            // 仅提交勾选的 Zone 副本
            ?.filter(item => item.checked)
            ?.map(item => {
              const { name, replicaType, cpuCore, memorySize, unitCount } = item;
              return {
                name,
                replicaType,
                resourcePool: {
                  unitSpec: {
                    cpuCore,
                    memorySize,
                  },
                  unitCount,
                },
              };
            }),
        });
      });
    });
  };

  const handleCharsetChange = (charset: string, newCharsetList: API.Charset[]) => {
    const newCollations = findBy(newCharsetList, 'name', charset)?.collations || [];
    const defaultCollation = findBy(newCollations, 'isDefault', true);
    setCollations(newCollations);
    setFieldsValue({ collation: defaultCollation.name });
  };

  const replicaTypeTooltipConfig = {
    color: '#fff',
    overlayStyle: {
      maxWidth: 400,
    },
    title: formatMessage({
      id: 'ocp-express.Tenant.New.CurrentlyOnlyFullFunctionReplicasAreSupported',
      defaultMessage: '当前仅支持全功能型副本',
    }),
  };

  const routes: Route[] = [
    {
      path: '/tenant',
      breadcrumbName: formatMessage({
        id: 'ocp-express.Tenant.New.Tenant',
        defaultMessage: '租户',
      }),
    },

    {
      breadcrumbName: isClone
        ? formatMessage({ id: 'ocp-express.Tenant.New.CopyTenant', defaultMessage: '复制租户' })
        : formatMessage({ id: 'ocp-express.Tenant.New.CreateATenant', defaultMessage: '新建租户' }),
    },
  ];

  const columns = [
    {
      title: ' ',
      dataIndex: 'checked',
      width: 24,
    },
    {
      title: formatMessage({ id: 'ocp-express.Tenant.New.ZoneName', defaultMessage: 'Zone 名称' }),
      dataIndex: 'name',
    },

    {
      title: (
        <ContentWithQuestion
          content={formatMessage({
            id: 'ocp-express.Tenant.New.ReplicaType',
            defaultMessage: '副本类型',
          })}
          tooltip={replicaTypeTooltipConfig}
        />
      ),

      dataIndex: 'replicaType',
    },

    {
      title: formatMessage({
        id: 'ocp-express.Tenant.New.UnitSpecifications',
        defaultMessage: 'Unit 规格',
      }),

      dataIndex: 'unitSpecName',
    },

    {
      title: formatMessage({
        id: 'ocp-express.Tenant.New.UnitQuantity',
        defaultMessage: 'Unit 数量',
      }),
      dataIndex: 'unitCount',
    },
  ];

  return (
    <PageContainer
      className={styles.container}
      ghost={true}
      header={{
        title: isClone
          ? formatMessage({
              id: 'ocp-express.Tenant.New.ReplicationTenant',
              defaultMessage: '复制租户',
            })
          : formatMessage({ id: 'ocp-express.Tenant.New.NewTenant', defaultMessage: '新建租户' }),
        breadcrumb: { routes, itemRender: breadcrumbItemRender },
        onBack: () => {
          history.push('/tenant');
        },
      }}
      footer={[
        <Button
          key="cancel"
          data-aspm-click="c318538.d343245"
          data-aspm-desc="新建租户-取消"
          data-aspm-param={``}
          data-aspm-expo
          onClick={() => {
            history.push('/tenant');
          }}
        >
          {formatMessage({ id: 'ocp-express.Tenant.New.Cancel', defaultMessage: '取消' })}
        </Button>,
        <Button
          key="submit"
          data-aspm-click="c318538.d343250"
          data-aspm-desc="新建租户-提交"
          data-aspm-param={``}
          data-aspm-expo
          type="primary"
          loading={createTenantLoading}
          onClick={handleSubmit}
        >
          {formatMessage({ id: 'ocp-express.Tenant.New.Submitted', defaultMessage: '提交' })}
        </Button>,
      ]}
    >
      <Form
        form={form}
        layout="vertical"
        colon={false}
        hideRequiredMark={true}
        requiredMark="optional"
      >
        <Row gutter={[16, 16]}>
          <Col span={24}>
            <MyCard
              title={formatMessage({
                id: 'ocp-express.Tenant.New.BasicInformation',
                defaultMessage: '基本信息',
              })}
              bordered={false}
            >
              <Row gutter={24}>
                <Col span={8}>
                  <Form.Item
                    label={formatMessage({
                      id: 'ocp-express.Tenant.New.TenantMode',
                      defaultMessage: '租户模式',
                    })}
                    tooltip={{
                      title: formatMessage({
                        id: 'ocp-express.Tenant.New.OracleTenantModeIsSupported',
                        defaultMessage: '社区版 OceanBase 不支持 Oracle 租户模式',
                      }),
                    }}
                    name="mode"
                    initialValue="MYSQL"
                    rules={[
                      {
                        required: true,
                        message: formatMessage({
                          id: 'ocp-express.Tenant.New.SelectTenantMode',
                          defaultMessage: '请选择租户模式',
                        }),
                      },
                    ]}
                  >
                    <MySelect
                      onChange={value => {
                        setFieldsValue({
                          charset: 'utf8mb4',
                        });

                        setCurrentMode(value);
                        const clusterId = getFieldValue('clusterId');
                        // 以获取最新的 mode 值
                        if (!isNullValue(clusterId)) {
                          listCharsets({
                            tenantMode: value,
                          }).then(charsetRes => {
                            handleCharsetChange('utf8mb4', charsetRes.data?.contents || []);
                          });
                        }
                      }}
                    >
                      {TENANT_MODE_LIST.map(item => (
                        <Option
                          key={item.value}
                          value={item.value}
                          disabled={clusterData?.communityEdition && item.value === 'ORACLE'}
                        >
                          {clusterData?.communityEdition && item.value === 'ORACLE' ? (
                            <Tooltip
                              placement="topLeft"
                              title={formatMessage({
                                id: 'ocp-express.Tenant.New.CommunityEditionOceanbaseDoesNotSupportOracleTenants',
                                defaultMessage: '社区版 OceanBase 暂不支持 Oracle 租户',
                              })}
                            >
                              <div style={{ width: '100%' }}>{item.label}</div>
                            </Tooltip>
                          ) : (
                            item.label
                          )}
                        </Option>
                      ))}
                    </MySelect>
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item
                    label={formatMessage({
                      id: 'ocp-express.Tenant.New.TenantName',
                      defaultMessage: '租户名称',
                    })}
                    name="name"
                    rules={[
                      {
                        required: true,
                        message: formatMessage({
                          id: 'ocp-express.Tenant.New.EnterTheTenantName',
                          defaultMessage: '请输入租户名称',
                        }),
                      },

                      NAME_RULE,
                    ]}
                  >
                    <MyInput
                      placeholder={formatMessage({
                        id: 'ocp-express.Tenant.New.PleaseEnter',
                        defaultMessage: '请输入',
                      })}
                    />
                  </Form.Item>
                </Col>
              </Row>
              <Row gutter={24}>
                <Col span={8}>
                  <Form.Item
                    label={formatMessage({
                      id: 'ocp-express.Tenant.New.AdministratorInitialPassword',
                      defaultMessage: '管理员初始密码',
                    })}
                    name="rootPassword"
                    rules={[
                      {
                        required: true,
                        message: formatMessage({
                          id: 'ocp-express.Tenant.New.PleaseEnterOrRandomlyGenerate',
                          defaultMessage: '请输入或随机生成管理员初始密码',
                        }),
                      },

                      {
                        validator: validatePassword(passed),
                      },
                    ]}
                  >
                    <Password generatePasswordRegex={PASSWORD_REGEX} onValidate={setPassed} />
                  </Form.Item>
                </Col>

                <Col span={8}>
                  <Form.Item
                    label={formatMessage({
                      id: 'ocp-express.Tenant.New.CharacterSet',
                      defaultMessage: '字符集',
                    })}
                    name="charset"
                    rules={[
                      {
                        required: true,
                        message: formatMessage({
                          id: 'ocp-express.Tenant.New.SelectACharacterSet',
                          defaultMessage: '请选择字符集',
                        }),
                      },
                    ]}
                  >
                    <MySelect
                      onChange={value => {
                        handleCharsetChange(value, charsetList);
                      }}
                      showSearch={true}
                      optionLabelProp="label"
                      dropdownClassName="select-dropdown-with-description"
                    >
                      {charsetList.map(item => (
                        <Option key={item.name} value={item.name} label={item.name}>
                          <span>{item.name}</span>
                        </Option>
                      ))}
                    </MySelect>
                  </Form.Item>
                </Col>
                {/* 如果是 oracle 模式，不展示 collation */}
                {currentMode === 'MYSQL' && (
                  <Col span={8}>
                    <Form.Item
                      label="Collation"
                      name="collation"
                      rules={[
                        {
                          required: true,
                          message: formatMessage({
                            id: 'ocp-express.Tenant.New.SelectCollation',
                            defaultMessage: '请选择 Collation',
                          }),
                        },
                      ]}
                    >
                      <MySelect>
                        {collations?.map(item => (
                          <Option key={item.name} value={item.name} label={item.name}>
                            <span>{item.name}</span>
                          </Option>
                        ))}
                      </MySelect>
                    </Form.Item>
                  </Col>
                )}
              </Row>
              <Row>
                <Col span={10}>
                  <Form.Item
                    label={formatMessage({
                      id: 'ocp-express.Tenant.New.Remarks',
                      defaultMessage: '备注',
                    })}
                    name="description"
                    rules={[getTextLengthRule(0, 200)]}
                  >
                    <MyInput.TextArea
                      rows={3}
                      showCount={true}
                      maxLength={200}
                      placeholder={formatMessage({
                        id: 'ocp-express.Tenant.New.PleaseEnter',
                        defaultMessage: '请输入',
                      })}
                    />
                  </Form.Item>
                </Col>
              </Row>
            </MyCard>
          </Col>
          <Col span={24}>
            <MyCard
              title={formatMessage({
                id: 'ocp-express.Tenant.New.ZoneInformation',
                defaultMessage: 'Zone 信息',
              })}
              bordered={false}
            >
              <Spin spinning={false}>
                {clusterData?.zones?.length > 0 && (
                  <Row gutter={8}>
                    <Col span={12}>
                      <Form.Item
                        label={formatMessage({
                          id: 'ocp-express.Tenant.New.NumberOfUnitsInEachZone',
                          defaultMessage: '各 Zone 的 Unit 数量',
                        })}
                        extra={formatMessage(
                          {
                            id: 'ocp-express.Tenant.New.TheMaximumNumberOfCurrentKesheIsMinservercount',
                            defaultMessage:
                              '当前可设最大个数为 {minServerCount} (Zone 中最少 OBServer 数决定 Unit 可设最大个数)',
                          },
                          { minServerCount: minServerCount }
                        )}
                        name="zonesUnitCount"
                        initialValue={1}
                        rules={[
                          {
                            // 仅勾选行需要必填
                            required: true,
                            message: formatMessage({
                              id: 'ocp-express.component.FormZoneReplicaTable.UnitQuantityCannotBeEmpty',
                              defaultMessage: '请输入 Unit 数量',
                            }),
                          },
                        ]}
                      >
                        <InputNumber
                          min={1}
                          max={minServerCount}
                          onChange={value => {
                            const currentZones = getFieldValue('zones');
                            setFieldsValue({
                              zones: currentZones?.map(item => ({
                                ...item,
                                unitCount: value,
                              })),
                            });
                          }}
                          style={{ width: '30%' }}
                          placeholder={formatMessage({
                            id: 'ocp-express.Tenant.New.Enter',
                            defaultMessage: '请输入',
                          })}
                        />
                      </Form.Item>
                    </Col>
                  </Row>
                )}

                {clusterData?.zones ? (
                  <Form.Item style={{ marginBottom: 24 }}>
                    <Form.List
                      name="zones"
                      rules={[
                        {
                          required: true,
                          message: formatMessage({
                            id: 'ocp-express.Tenant.New.PleaseSetACopy',
                            defaultMessage: '请设置副本',
                          }),
                        },
                      ]}
                    >
                      {(fields, {}) => {
                        return (
                          <>
                            {fields.map((field, index: number) => {
                              const currentChecked = getFieldValue([
                                'zones',
                                field.name,
                                'checked',
                              ]);

                              const currentZoneName = getFieldValue(['zones', field.name, 'name']);
                              const zoneData = findBy(
                                clusterData?.zones || [],
                                'name',
                                currentZoneName
                              );

                              let idleCpuCore, idleMemoryInBytes;
                              if (zoneData?.servers?.length > 0 && zoneData?.servers[0]?.stats) {
                                const { idleCpuCoreTotal, idleMemoryInBytesTotal } =
                                  getUnitSpecLimit(zoneData?.servers[0]?.stats);
                                idleCpuCore = idleCpuCoreTotal;
                                idleMemoryInBytes = idleMemoryInBytesTotal;
                              }

                              const { cpuLowerLimit, memoryLowerLimit } = clusterUnitSpecLimit;

                              return (
                                <div
                                  key={field.key}
                                  style={{
                                    display: 'flex',
                                  }}
                                >
                                  <Form.Item
                                    style={{ marginBottom: 8, width: 24 }}
                                    {...field}
                                    label={index === 0 && ' '}
                                    name={[field.name, 'checked']}
                                    rules={[
                                      {
                                        required: true,
                                      },
                                    ]}
                                    valuePropName="checked"
                                  >
                                    <Checkbox />
                                  </Form.Item>
                                  <Row
                                    gutter={16}
                                    style={{
                                      flex: 1,
                                    }}
                                  >
                                    <Col span={5}>
                                      <Form.Item
                                        style={{ marginBottom: 8 }}
                                        {...field}
                                        label={
                                          index === 0 &&
                                          formatMessage({
                                            id: 'ocp-express.Tenant.New.ZoneName',
                                            defaultMessage: 'Zone 名称',
                                          })
                                        }
                                        name={[field.name, 'name']}
                                        rules={[
                                          {
                                            // 仅勾选行需要必填
                                            required: currentChecked,
                                          },
                                        ]}
                                      >
                                        <MyInput disabled={true} />
                                      </Form.Item>
                                    </Col>
                                    <Col span={5}>
                                      <Form.Item
                                        style={{ marginBottom: 8 }}
                                        {...field}
                                        label={
                                          index === 0 &&
                                          formatMessage({
                                            id: 'ocp-express.Tenant.New.ReplicaType',
                                            defaultMessage: '副本类型',
                                          })
                                        }
                                        tooltip={replicaTypeTooltipConfig}
                                        name={[field.name, 'replicaType']}
                                        rules={[
                                          {
                                            // 仅勾选行需要必填
                                            required: currentChecked,
                                            message: formatMessage({
                                              id: 'ocp-express.Tenant.New.SelectAReplicaType',
                                              defaultMessage: '请选择副本类型',
                                            }),
                                          },
                                        ]}
                                      >
                                        <MySelect
                                          disabled={!currentChecked}
                                          optionLabelProp="label"
                                        >
                                          {REPLICA_TYPE_LIST.map(item => (
                                            <Option
                                              key={item.value}
                                              value={item.value}
                                              label={item.label}
                                            >
                                              <Tooltip
                                                placement="right"
                                                title={item.description}
                                                overlayStyle={{
                                                  maxWidth: 400,
                                                }}
                                              >
                                                <div style={{ width: '100%' }}>{item.label}</div>
                                              </Tooltip>
                                            </Option>
                                          ))}
                                        </MySelect>
                                      </Form.Item>
                                    </Col>
                                    <Col span={5}>
                                      <Form.Item
                                        style={{ marginBottom: 8 }}
                                        {...field}
                                        label={
                                          index === 0 &&
                                          formatMessage({
                                            id: 'ocp-express.Tenant.New.UnitSpecifications',
                                            defaultMessage: 'Unit 规格',
                                          })
                                        }
                                        extra={
                                          cpuLowerLimit &&
                                          idleCpuCore &&
                                          formatMessage(
                                            {
                                              id: 'ocp-express.Tenant.New.CurrentConfigurableRangeValueCpulowerlimitIdlecpucore',
                                              defaultMessage:
                                                '当前可配置范围值 {cpuLowerLimit}~{idleCpuCore}',
                                            },
                                            {
                                              cpuLowerLimit: cpuLowerLimit,
                                              idleCpuCore: idleCpuCore,
                                            }
                                          )
                                        }
                                        initialValue={
                                          getResourcesLimit({
                                            idleCpuCore,
                                            idleMemoryInBytes,
                                          })
                                            ? 4
                                            : null
                                        }
                                        name={[field.name, 'cpuCore']}
                                        rules={[
                                          {
                                            // 仅勾选行需要必填
                                            required: currentChecked,
                                            message: formatMessage({
                                              id: 'ocp-express.Tenant.New.EnterTheUnitSpecification',
                                              defaultMessage: '请输入 unit 规格',
                                            }),
                                          },
                                          {
                                            type: 'number',
                                            min: cpuLowerLimit,
                                            max: idleCpuCore,
                                            message: formatMessage(
                                              {
                                                id: 'ocp-express.Tenant.New.CurrentConfigurableRangeValueCpulowerlimitIdlecpucore',
                                                defaultMessage:
                                                  '当前可配置范围值 {cpuLowerLimit}~{idleCpuCore}',
                                              },
                                              {
                                                cpuLowerLimit: cpuLowerLimit,
                                                idleCpuCore: idleCpuCore,
                                              }
                                            ),
                                          },
                                        ]}
                                      >
                                        <InputNumber
                                          disabled={!currentChecked}
                                          addonAfter={formatMessage({
                                            id: 'ocp-express.Tenant.New.Nuclear',
                                            defaultMessage: '核',
                                          })}
                                          step={0.5}
                                          style={{ width: '100%' }}
                                        />
                                      </Form.Item>
                                    </Col>
                                    <Col span={5} style={{ paddingLeft: 0 }}>
                                      <Form.Item
                                        style={{ marginBottom: 8 }}
                                        {...field}
                                        label={index === 0 && ' '}
                                        name={[field.name, 'memorySize']}
                                        extra={
                                          memoryLowerLimit &&
                                          idleMemoryInBytes &&
                                          formatMessage(
                                            {
                                              id: 'ocp-express.Tenant.New.CurrentConfigurableRangeValueMemorylowerlimitIdlememoryinbytes',
                                              defaultMessage:
                                                '当前可配置范围值 {memoryLowerLimit}~{idleMemoryInBytes}',
                                            },
                                            {
                                              memoryLowerLimit: memoryLowerLimit,
                                              idleMemoryInBytes: idleMemoryInBytes,
                                            }
                                          )
                                        }
                                        initialValue={
                                          getResourcesLimit({
                                            idleCpuCore,
                                            idleMemoryInBytes,
                                          })
                                            ? 8
                                            : null
                                        }
                                        rules={[
                                          {
                                            // 仅勾选行需要必填
                                            required: currentChecked,
                                            message: formatMessage({
                                              id: 'ocp-express.Tenant.New.EnterTheUnitSpecification',
                                              defaultMessage: '请输入 unit 规格',
                                            }),
                                          },
                                          {
                                            type: 'number',
                                            min: memoryLowerLimit || 0,
                                            max: idleMemoryInBytes || 0,
                                            message: formatMessage(
                                              {
                                                id: 'ocp-express.Tenant.New.CurrentConfigurableRangeValueMemorylowerlimitIdlememoryinbytes',
                                                defaultMessage:
                                                  '当前可配置范围值 {memoryLowerLimit}~{idleMemoryInBytes}',
                                              },
                                              {
                                                memoryLowerLimit: memoryLowerLimit,
                                                idleMemoryInBytes: idleMemoryInBytes,
                                              }
                                            ),
                                          },
                                        ]}
                                      >
                                        <InputNumber
                                          disabled={!currentChecked}
                                          style={{ width: '100%' }}
                                          addonAfter="GB"
                                        />
                                      </Form.Item>
                                    </Col>
                                    <Col span={3}>
                                      <Form.Item noStyle shouldUpdate={true}>
                                        {() => {
                                          return (
                                            <Form.Item
                                              style={{ marginBottom: 8 }}
                                              {...field}
                                              label={
                                                index === 0 &&
                                                formatMessage({
                                                  id: 'ocp-express.Tenant.New.UnitQuantity',
                                                  defaultMessage: 'Unit 数量',
                                                })
                                              }
                                              initialValue={1}
                                              name={[field.name, 'unitCount']}
                                              rules={[
                                                {
                                                  required: true,
                                                  message: formatMessage({
                                                    id: 'ocp-express.component.FormZoneReplicaTable.UnitQuantityCannotBeEmpty',
                                                    defaultMessage: '请输入 Unit 数量',
                                                  }),
                                                },
                                                {
                                                  validator: (rule, value, callback) =>
                                                    validateUnitCount(
                                                      rule,
                                                      value,
                                                      callback,
                                                      findBy(
                                                        clusterData?.zones || [],
                                                        'name',
                                                        currentZoneName
                                                      )
                                                    ),
                                                },
                                              ]}
                                            >
                                              <InputNumber
                                                min={1}
                                                // 设为只读模式，会去掉 InputNumber 的大部分样式
                                                readOnly={true}
                                                // 其余样式需要手动去掉
                                                className="input-number-readonly"
                                                style={{ width: '100%' }}
                                                placeholder={formatMessage({
                                                  id: 'ocp-express.Tenant.New.Enter',
                                                  defaultMessage: '请输入',
                                                })}
                                              />
                                            </Form.Item>
                                          );
                                        }}
                                      </Form.Item>
                                    </Col>
                                  </Row>
                                </div>
                              );
                            })}
                          </>
                        );
                      }}
                    </Form.List>
                  </Form.Item>
                ) : (
                  <Table className={styles.table} dataSource={[]} columns={columns} />
                )}

                <Form.Item noStyle shouldUpdate={true}>
                  {() => {
                    const currentZones = getFieldValue('zones');
                    const checkedZones = (currentZones || []).filter(item => item.checked);
                    const uniqueCheckedUnitSpecNameList = uniq(
                      checkedZones
                        // 去掉空值
                        .filter(item => !!item.unitSpecName)
                        .map(item => item.unitSpecName)
                    );

                    const uniqueCheckedUnitCountList = uniq(
                      checkedZones
                        // 去掉空值
                        .filter(item => !isNullValue(item.unitCount))
                        .map(item => item.unitCount)
                    );

                    // 勾选 Zone 的副本类型或副本数量不同时，展示 alert 提示
                    return (
                      (uniqueCheckedUnitSpecNameList.length > 1 ||
                        uniqueCheckedUnitCountList.length > 1) && (
                        <Alert
                          message={formatMessage({
                            id: 'ocp-express.Tenant.New.WeRecommendThatYouSetTheSameUnit',
                            defaultMessage:
                              '建议为全能型副本设置相同的 Unit 规格，不同的 Unit 规格可能造成性能或稳定性问题。',
                          })}
                          type="info"
                          showIcon={true}
                          style={{ marginTop: -8, marginBottom: 16 }}
                        />
                      )
                    );
                  }}
                </Form.Item>
              </Spin>
              <Form.Item noStyle shouldUpdate={true}>
                {() => {
                  const currentZones = getFieldValue('zones');
                  return (
                    <Form.Item
                      style={{ marginBottom: 0 }}
                      label={formatMessage({
                        id: 'ocp-express.Tenant.New.ZonePrioritySorting',
                        defaultMessage: 'Zone 优先级排序',
                      })}
                      tooltip={{
                        title: formatMessage({
                          id: 'ocp-express.Tenant.New.PriorityOfTheDistributionOf',
                          defaultMessage: '租户中主副本分布的优先级',
                        }),
                      }}
                      extra={formatMessage({
                        id: 'ocp-express.Tenant.New.IfThisParameterIsEmpty',
                        defaultMessage: '如果为空，则默认继承 sys 租户的 Zone 优先级',
                      })}
                      name="primaryZone"
                    >
                      <FormPrimaryZone
                        loading={clusterDataLoading}
                        zoneList={(currentZones || [])
                          // 仅勾选的 zone 副本可以设置优先级
                          .filter(item => item.checked)
                          .map(item => item.name)}
                      />
                    </Form.Item>
                  );
                }}
              </Form.Item>
            </MyCard>
          </Col>
          <Col span={24}>
            <MyCard
              title={formatMessage({
                id: 'ocp-express.Tenant.New.SecuritySettings',
                defaultMessage: '安全设置',
              })}
              bordered={false}
            >
              <Row>
                <Col span={10}>
                  <Form.Item
                    style={{ marginBottom: 0 }}
                    name="whitelist"
                    initialValue={tenantData.whitelist}
                    rules={[
                      {
                        validator: WhitelistInput.validate,
                      },
                    ]}
                  >
                    <WhitelistInput layout="vertical" />
                  </Form.Item>
                </Col>
              </Row>
            </MyCard>
          </Col>
          <Col span={24}>
            <Card
              className={advanceSettingSwitch ? '' : 'card-without-padding'}
              title={
                <Space>
                  {formatMessage({
                    id: 'ocp-express.Cluster.New.ParameterSettings',
                    defaultMessage: '参数设置',
                  })}

                  <Switch
                    checked={advanceSettingSwitch}
                    onChange={checked => {
                      setAdvanceSettingSwitch(checked);
                    }}
                  />
                </Space>
              }
              bordered={false}
            >
              {advanceSettingSwitch && (
                <>
                  {compatibleOracleAlret && currentMode === 'MYSQL' && (
                    <Alert
                      message={formatMessage({
                        id: 'ocp-express.Tenant.New.DeleteTheParametersMarkedWith',
                        defaultMessage:
                          '请删除红色边框标记的参数，这些参数仅适用于 Oracle 模式的租户，当前租户为MySQL',
                      })}
                      type="error"
                      showIcon={true}
                      style={{ marginBottom: 16 }}
                    />
                  )}

                  <Form.Item
                    className={styles.parameters}
                    label={
                      <div
                        style={{
                          width: '100%',
                          display: 'flex',
                          justifyContent: 'space-between',
                        }}
                      >
                        <span>
                          {formatMessage({
                            id: 'ocp-express.Tenant.New.ParameterModification',
                            defaultMessage: '参数修改',
                          })}
                        </span>
                        {/* 一期暂不开放此功能 */}
                        {/* <ParameterTemplateDropdown
                      type="tenant"
                      tenantMode={getFieldValue('mode')}
                      parameters={getFieldValue('parameters')}
                      onSetParameter={value => {
                      setFieldsValue({
                      parameters: value,
                      });
                      setInitParameters(value);
                      }}
                      /> */}
                      </div>
                    }
                    name="parameters"
                    rules={[
                      {
                        required: true,
                        message: formatMessage({
                          id: 'ocp-express.Tenant.New.SetStartupParameters',
                          defaultMessage: '请设置租户参数',
                        }),
                      },
                    ]}
                  >
                    <SetParameterEditableProTable
                      type="tenant"
                      draweType="NEW_TENANT"
                      addButtonText={formatMessage({
                        id: 'ocp-express.Tenant.New.AddStartupParameters',
                        defaultMessage: '添加租户参数',
                      })}
                      // initialParameters={initParameters}
                      tenantMode={currentMode}
                      setCompatibleOracleAlret={val => {
                        setCompatibleOracleAlret(val);
                      }}
                    />
                  </Form.Item>
                </>
              )}
            </Card>
          </Col>
        </Row>
      </Form>
    </PageContainer>
  );
};
export default New;
