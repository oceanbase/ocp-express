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

import MyInput from '@/component/MyInput';
import MySelect from '@/component/MySelect';
import * as ObTenantParameterController from '@/service/ocp-express/ObTenantParameterController';
import { formatMessage } from '@/util/intl';
import { Alert, Form, InputNumber, Typography } from '@oceanbase/design';
import { differenceBy, find, includes, uniqBy, uniqueId } from 'lodash';
import { ProForm } from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-table';
import { EditableProTable } from '@ant-design/pro-table';
import { useRequest } from 'ahooks';
import React, { useEffect, useState } from 'react';

const { Option } = MySelect;

export interface SetParameterEditableProTableProps {
  type?: string;
  draweType?: string;
  obVersion?: string;
  tenantMode?: string; // 租户模式  Oracle | MySQL
  addButtonText?: string;
  defaultTmplate?: API.ClusterParameterTemplateObVersionRelation;
  template?: API.ClusterParameterTemplate | API.TenantParameterTemplate;
  initialParameters?: API.ClusterParameterWithValue[] | API.TenantParameterWithValue[];
  onChange?: (value: API.ClusterParameterParam[]) => void;
  setCompatibleOracleAlret?: (value: boolean) => void;
}

const defaultData: API.ParameterInfo[] = new Array(1).fill(1).map(() => {
  return {
    key: uniqueId(),
    valueRange: {
      type: '-',
    },
  };
});

/**
 * 这些参数是集群创建的关键参数，OCP会自己设置，不能让用户乱填。
 * @中林 与 PD 讨论后，前端在：集群参数模板 & 创建集群，在参数列表中屏蔽以上参数
 */
const clusterKeyParameters = [
  'obconfig_url',
  'rootservice_list',
  'config_additional_dir',
  'cluster_id',
];

const SetParameterEditableProTable: React.FC<SetParameterEditableProTableProps> = ({
  type,
  draweType,
  tenantMode,
  addButtonText = formatMessage({
    id: 'ocp-express.component.ParameterTemplate.SetParameterEditableProTable.AddParameters',
    defaultMessage: '添加参数',
  }),
  defaultTmplate,
  template,
  obVersion,
  initialParameters,
  onChange,
  setCompatibleOracleAlret,
}) => {
  const [form] = Form.useForm();
  const [editableKeys, setEditableRowKeys] = useState<React.Key[]>(() =>
    defaultData.map(item => item.key)
  );

  const [dataSource, setDataSource] = useState<API.ParameterInfo[]>(() => defaultData);
  const [parameterList, setParameterList] = useState<API.ParameterInfo[]>([]);
  const [compatibleOracle, setCompatibleOracle] = useState(false);

  const { run, loading } = useRequest(ObTenantParameterController.listTenantParameterInfo, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        let parameterData = [];
        if (type === 'cluster') {
          res?.data?.contents?.map(item => {
            if (!includes(clusterKeyParameters, item?.name)) {
              parameterData.push(item);
            }
          });
        } else {
          const tenantParameterList = res?.data?.contents || [];
          // 创建租户，租户参数模板限制只读参数
          parameterData = tenantParameterList.filter(
            item =>
              (item.readonly === false && item.name !== 'nls_comp') ||
              (tenantMode === 'MYSQL' && item.name === 'lower_case_table_names')
          );
        }
        if (parameterData.length !== 0) {
          // 编辑、复制
          if (draweType !== 'NEW' && template) {
            setBackfill(parameterData);
          }
          // 新建集群、租户中参数设置
          if (initialParameters && initialParameters.length !== 0) {
            setInitialDataSource(parameterData);
          }
        }
        setParameterList(parameterData);
      }
    },
  });

  useEffect(() => {
    run({});
  }, [initialParameters]);

  // 向上更新 Parameters 时需过滤去未填值的项
  const saveParameters = (currentParameterList: API.ParameterInfo[]) => {
    const values = currentParameterList
      ?.filter(item => item && item.defaultValue && item.name)
      ?.map(item => ({
        name: item.name,
        value: item.defaultValue,
        parameterType: item.parameterType,
      }));

    if (onChange) {
      onChange(values);
    }
  };

  // 新建集群、租户时采用某模板参数时，以初始值设定到dataSource
  const setInitialDataSource = (parameterInfoList: API.ParameterInfo[]) => {
    const dataList: API.ParameterInfo[] = [];
    initialParameters?.map(item => {
      const selectedParameter = find(parameterInfoList, ['name', item?.name]);
      if (selectedParameter) {
        return dataList.push({
          ...selectedParameter,
          defaultValue: item.value,
          key: uniqueId(),
        });
      } else {
        return dataList.push({
          ...item,
          defaultValue: item.value,
          isInvalidParameterForObVersion: true,
          key: uniqueId(),
        });
      }
    });
    setDataSource(dataList);
    setEditableRowKeys(() => dataList.map(item => item?.key));
    saveParameters(dataList);
  };

  // 新建时不需要回填模板参数值，编辑与复制需要回填
  const setBackfill = (parameterInfoList: API.ParameterInfo[]) => {
    const dataList: API.ParameterInfo[] = [];
    template?.parameters?.map(item => {
      const selectedParameter = find(parameterInfoList, ['name', item?.parameter?.name]);
      dataList.push({
        ...selectedParameter,
        defaultValue: item.value,
        key: uniqueId(),
      });
    });
    setDataSource(dataList);
    setEditableRowKeys(() => dataList.map(item => item?.key));
    saveParameters(dataList);
  };

  const columns: ProColumns<API.ParameterInfo>[] = [
    {
      title: formatMessage({
        id: 'ocp-express.component.ParameterTemplate.SetParameterEditableProTable.Parameter',
        defaultMessage: '参数名',
      }),

      dataIndex: 'name',
      // width: '35%',
      valueType: 'select',
      renderFormItem: (record, { recordKey } = config) => {
        return (
          <MySelect
            showSearch={true}
            optionFilterProp="optionFilter"
            optionLabelProp="optionLabel"
            dropdownClassName="select-dropdown-with-description"
            onChange={val => {
              const selectedParameter = find(uniqBy(parameterList, 'id'), ['name', val]);
              form.setFieldsValue({
                [recordKey]: {
                  defaultValue:
                    selectedParameter?.defaultValue === ''
                      ? undefined
                      : selectedParameter?.defaultValue,
                },
              });
            }}
          >
            {differenceBy(
              uniqBy(parameterList, 'name'),
              dataSource.filter(item => item.name !== record?.entity?.name),
              'name'
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
              ?.map(item => {
                const oracleLabel =
                  item?.compatibleType === 'ORACLE'
                    ? formatMessage({
                        id: 'ocp-express.component.ParameterTemplate.SetParameterEditableProTable.ApplicableOnlyToOracleTenant',
                        defaultMessage: '仅适用于 Oracle 租户模式',
                      })
                    : '';
                return (
                  <Option
                    key={item?.id}
                    value={item?.name}
                    // 用于筛选
                    optionFilter={`${item?.name}${oracleLabel}${item.description}`}
                    // 用于选中后填充
                    optionLabel={item?.name}
                  >
                    <span>{oracleLabel ? `${item?.name} (${oracleLabel})` : item?.name}</span>
                    <Typography.Text
                      ellipsis={{ tooltip: item?.description }}
                      style={{ maxWidth: 200 }}
                    >
                      <span>{item?.description}</span>
                    </Typography.Text>
                  </Option>
                );
              })}
          </MySelect>
        );
      },
      formItemProps: (_form, config) => {
        // 创建租户时，选择 MySQL 模式 需校验出只适用于 Oracle 模式的参数
        if (tenantMode === 'MYSQL' && config?.entity?.compatibleType === 'ORACLE') {
          return { validateStatus: 'error' };
        }
        if (obVersion && config?.entity?.isInvalidParameterForObVersion) {
          return {
            help: formatMessage(
              {
                id: 'ocp-express.component.ParameterTemplate.SetParameterEditableProTable.ThisParameterIsInvalidInTheClusterOf',
                defaultMessage: '此参数在 {obVersion} 版本 OceanBase 集群无效，请删除',
              },
              { obVersion: obVersion }
            ),
            validateStatus: 'error',
          };
        }
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.component.ParameterTemplate.SetParameterEditableProTable.ParameterValue',
        defaultMessage: '参数值',
      }),

      key: 'defaultValue',
      dataIndex: 'defaultValue',
      // width: '20%',
      fieldProps: {
        style: { width: '100%' },
      },

      renderFormItem: record => {
        if (record?.entity?.valueRange?.type === 'ENUM') {
          return (
            <MySelect>
              {record?.entity?.valueRange?.allowedValues?.split(',').map(item => (
                <Option key={item} value={item}>
                  {item}
                </Option>
              ))}
            </MySelect>
          );
        } else if (
          record?.entity?.valueRange?.type === 'INT' ||
          record?.entity?.valueRange?.type === 'DOUBLE'
        ) {
          return (
            <InputNumber
              placeholder={formatMessage({
                id: 'ocp-express.component.ParameterTemplate.SetParameterEditableProTable.Enter',
                defaultMessage: '请输入',
              })}
              style={{ width: '100%' }}
              max={record?.entity?.valueRange?.maxValue}
              min={record?.entity?.valueRange?.minValue}
            />
          );
        }
        return <MyInput />;
      },
      formItemProps: {
        rules: [
          {
            required: true,
            // whitespace: true,
            message: formatMessage({
              id: 'ocp-express.component.ParameterTemplate.SetParameterEditableProTable.ThisParameterIsRequired',
              defaultMessage: '此项为必填项',
            }),
          },
        ],
      },
    },

    {
      title: formatMessage({
        id: 'ocp-express.component.ParameterTemplate.SetParameterEditableProTable.Operation',
        defaultMessage: '操作',
      }),

      width: 80,
      valueType: 'option',
      render: () => [
        <a key="delete" disabled={dataSource.length < 1}>
          {formatMessage({
            id: 'ocp-express.component.ParameterTemplate.SetParameterEditableProTable.Delete',
            defaultMessage: '删除',
          })}
        </a>,
      ],
    },
  ];

  if (tenantMode === 'MYSQL' && draweType === 'NEW_TENANT' && setCompatibleOracleAlret) {
    const compatibleOracleList = dataSource.filter(item => item.compatibleType === 'ORACLE');
    setCompatibleOracleAlret(compatibleOracleList?.length > 0);
  }

  return (
    <>
      {defaultTmplate?.obVersion === '2.2.77' && (
        <Alert
          message={formatMessage({
            id: 'ocp-express.component.ParameterTemplate.SetParameterEditableProTable.TheFollowingParameterSettingsAreRecommendedForThe',
            defaultMessage: '2.2.77 版本 OceanBase 集群推荐以下参数设置，可根据需要修改',
          })}
          type="info"
          showIcon={true}
          style={{ marginBottom: 16 }}
        />
      )}

      {compatibleOracle && !tenantMode && draweType !== 'NEW_TENANT' && (
        <Alert
          message={formatMessage({
            id: 'ocp-express.component.ParameterTemplate.SetParameterEditableProTable.TheCurrentTemplateHasParameters',
            defaultMessage:
              '当前模板存在仅适用于 Oracle 模式租户的参数，因此创建 Oracle 模式租户才可使用该模板',
          })}
          type="info"
          showIcon={true}
          style={{ marginBottom: 16 }}
        />
      )}

      <ProForm submitter={false}>
        <EditableProTable<API.ParameterInfo>
          cardProps={{
            bodyStyle: {
              padding: 0,
            },
          }}
          loading={loading}
          columns={columns}
          rowKey="key"
          value={dataSource}
          onChange={setDataSource}
          recordCreatorProps={{
            newRecordType: 'dataSource',
            creatorButtonText: addButtonText,
            record: () => ({
              key: uniqueId(),
            }),
          }}
          editable={{
            form,
            type: 'multiple',
            editableKeys,
            deletePopconfirmMessage: formatMessage({
              id: 'ocp-express.component.ParameterTemplate.SetParameterEditableProTable.DeleteThisParameter',
              defaultMessage: '确定要删除该参数吗？',
            }),
            actionRender: (row, config, defaultDoms) => {
              return [defaultDoms.delete];
            },
            onValuesChange: (record, recordList) => {
              const dataList: API.ParameterInfo[] = [];
              recordList.map(item => {
                const selectedParameter = find(parameterList, ['name', item?.name]);
                if (item?.key === record?.key) {
                  return dataList.push({
                    ...selectedParameter,
                    // 如果未填写参数值，用默认值填充
                    defaultValue:
                      record.id !== selectedParameter?.id
                        ? selectedParameter?.defaultValue
                        : record?.defaultValue
                        ? record?.defaultValue
                        : item?.defaultValue,
                    key: item?.key,
                  });
                } else {
                  return dataList.push({
                    ...selectedParameter,
                    // 新增一行，用默认值填充
                    defaultValue: item?.defaultValue
                      ? item?.defaultValue
                      : selectedParameter?.defaultValue,
                    key: item?.key,
                  });
                }
              });
              setDataSource(dataList.slice());
              if (parameterList.length > 0) {
                saveParameters(dataList);
              }

              if (type === 'tenant') {
                setCompatibleOracle(
                  dataList.filter(item => item?.compatibleType === 'ORACLE').length !== 0
                    ? true
                    : false
                );
              }
            },
            onDelete: (key: Key, row: T) => {
              const dataList = dataSource.filter(item => item.name !== row.name);
              setDataSource(dataList);
              saveParameters(dataList);
            },
            onChange: setEditableRowKeys,
          }}
        />
      </ProForm>
    </>
  );
};

export default SetParameterEditableProTable;
