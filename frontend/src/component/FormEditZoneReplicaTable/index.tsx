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
import React from 'react';
import { InputNumber } from '@oceanbase/design';
import { findByValue, findBy } from '@oceanbase/util';
import { REPLICA_TYPE_LIST } from '@/constant/oceanbase';
import { getUnitSpecLimit } from '@/util/cluster';
import type { FormEditTableProps } from '@/component/FormEditTable';
import FormEditTable from '@/component/FormEditTable';
import MySelect from '@/component/MySelect';
import UnitSpec from '@/component/UnitSpec';

const { Option } = MySelect;

export interface FormEditZoneReplicaTableProps<T> extends FormEditTableProps<T> {
  className?: string;
  value: T[];
  tenantData: API.TenantInfo;
  clusterData: API.ClusterInfo;
  unitSpecLimit?: any;
  saveLoading?: boolean;
  dispatch?: any;
}

class FormEditZoneReplicaTable<T> extends FormEditTable<FormEditZoneReplicaTableProps<T>> {
  constructor(props: FormEditZoneReplicaTableProps) {
    super(props);
    this.state = {
      ...(super.state || {}),
    };
  }

  // 删除 zone
  public handleDeleteRecord = (record: any) => {
    const { value } = this.props;
    const newValue = value.filter(item => item.key !== record.key);
    this.handleValueChange(newValue);
  };

  public render() {
    const { saveLoading, clusterData, unitSpecLimit } = this.props;

    const columns = [
      {
        title: formatMessage({
          id: 'ocp-express.component.FormEditZoneReplicaTable.ZoneName',
          defaultMessage: 'Zone 名称',
        }),

        dataIndex: 'name',
      },

      {
        title: formatMessage({
          id: 'ocp-express.component.FormEditZoneReplicaTable.CopyType',
          defaultMessage: '副本类型',
        }),

        dataIndex: 'replicaType',
        width: '20%',
        editable: false,
        fieldProps: () => ({
          rules: [
            {
              required: true,
              message: formatMessage({
                id: 'ocp-express.component.FormEditZoneReplicaTable.SelectAReplicaType',
                defaultMessage: '请选择副本类型',
              }),
            },
          ],
        }),

        fieldComponent: () => (
          <MySelect>
            {REPLICA_TYPE_LIST.map(item => (
              <Option key={item.value} value={item.value}>
                {item.label}
              </Option>
            ))}
          </MySelect>
        ),

        render: (text: API.ReplicaType) => (
          <span>{findByValue(REPLICA_TYPE_LIST, text).label}</span>
        ),
      },
      {
        title: formatMessage({
          id: 'ocp-express.component.FormEditZoneReplicaTable.UnitSpecification',
          defaultMessage: 'Unit 规格',
        }),
        dataIndex: 'resourcePool.unitSpec',
        width: '40%',
        editable: true,
        fieldComponent: (text, record) => {
          const zoneData = findBy(clusterData?.zones || [], 'name', record.name);

          let idleCpuCore, idleMemoryInBytes;
          if (zoneData?.servers?.length > 0 && zoneData?.servers[0]?.stats) {
            const { idleCpuCoreTotal, idleMemoryInBytesTotal } = getUnitSpecLimit(
              zoneData?.servers[0]?.stats
            );
            debugger

            idleCpuCore = idleCpuCoreTotal;
            idleMemoryInBytes = idleMemoryInBytesTotal;
          }

          return (
            // <div></div>
            <UnitSpec
              unitSpecLimit={unitSpecLimit}
              idleUnitSpec={{ idleCpuCore, idleMemoryInBytes }}
              defaultUnitSpec={record?.resourcePool?.unitConfig}
            />
          );
        },
        // fieldProps: (text, record) => ({
        //   rules: [
        //     {
        //       validator: (rule, value = text, callback) => {
        //         if (value) {
        //           return validatorUnitResource(
        //             rule,
        //             record.resourcePool.unitConfig,
        //             callback,
        //             findBy(tenantData.zones || [], 'name', record.name)
        //           );
        //         } else {
        //           return callback();
        //         }
        //       },
        //     },
        //   ],
        // }),

        render: (text: string, record: API.TenantZone) => {
          const { unitConfig } = (record.resourcePool as API.ResourcePool) || {};
          const { maxCpuCoreCount, maxMemorySize: maxMemorySizeGB } =
            (unitConfig as API.UnitConfig) || {};

          return (
            <ul>
              <li>
                {formatMessage(
                  {
                    id: 'ocp-express.component.FormEditZoneReplicaTable.CpuCoreMincpucorecountMaxcpucorecount',
                    defaultMessage: 'CPU（核）： {maxCpuCoreCount}',
                  },

                  { maxCpuCoreCount }
                )}
              </li>
              <li>
                {formatMessage(
                  {
                    id: 'ocp-express.component.FormEditZoneReplicaTable.MemoryGbMinmemorysizegbMaxmemorysizegb',
                    defaultMessage: '内存（GB）：{maxMemorySizeGB}',
                  },

                  { maxMemorySizeGB }
                )}
              </li>
            </ul>
          );
        },
      },

      // {
      //   title: (
      //     <ContentWithQuestion
      //       content={formatMessage({
      //         id: 'ocp-express.component.FormEditZoneReplicaTable.UnitSpecification',
      //         defaultMessage: 'Unit 规格',
      //       })}
      //       tooltip={{
      //         title: unitSpecLimitRule && getUnitSpecLimitText(unitSpecLimitRule),
      //       }}
      //     />
      //   ),

      //   dataIndex: 'resourcePool.unitSpecName',
      //   width: '25%',
      //   editable: true,
      //   fieldComponent: () => (
      //     <UnitSpecSelect
      //       allowClear={true}
      //       obVersion={tenantData?.obVersion}
      //       placeholder={
      //         <Tooltip
      //           placement="topLeft"
      //           title={formatMessage({
      //             id: 'ocp-express.component.FormEditZoneReplicaTable.EmptyIndicatesThatTheUnit',
      //             defaultMessage: '为空表示不修改 Unit 规格',
      //           })}
      //         >
      //           <span>
      //             {formatMessage({
      //               id: 'ocp-express.component.FormEditZoneReplicaTable.EmptyIndicatesThatTheUnit',
      //               defaultMessage: '为空表示不修改 Unit 规格',
      //             })}
      //           </span>
      //         </Tooltip>
      //       }
      //     />
      //   ),
      //   fieldProps: (text, record) => ({
      //     rules: [
      //       {
      //         validator: (rule, value = text, callback) => {
      //           if (value) {
      //             return validatorUnitResource(
      //               rule,
      //               record.resourcePool.unitConfig,
      //               callback,
      //               findBy(tenantData.zones || [], 'name', record.name)
      //             );
      //           } else {
      //             return callback();
      //           }
      //         },
      //       },
      //     ],
      //   }),

      //   render: (text: string, record: API.TenantZone) => {
      //     const { unitConfig } = (record.resourcePool as API.ResourcePool) || {};
      //     const { maxCpuCoreCount, maxMemorySize: maxMemorySizeGB } =
      //       (unitConfig as API.UnitConfig) || {};

      //     return (
      //       <ul>
      //         <li>
      //           {formatMessage(
      //             {
      //               id: 'ocp-express.component.FormEditZoneReplicaTable.CpuCoreMincpucorecountMaxcpucorecount',
      //               defaultMessage: 'CPU（核）： {maxCpuCoreCount}',
      //             },

      //             { maxCpuCoreCount }
      //           )}
      //         </li>
      //         <li>
      //           {formatMessage(
      //             {
      //               id: 'ocp-express.component.FormEditZoneReplicaTable.MemoryGbMinmemorysizegbMaxmemorysizegb',
      //               defaultMessage: '内存（GB）：{maxMemorySizeGB}',
      //             },

      //             { maxMemorySizeGB }
      //           )}
      //         </li>
      //       </ul>
      //     );
      //   },
      // },

      {
        title: formatMessage({
          id: 'ocp-express.component.FormEditZoneReplicaTable.UnitQuantity',
          defaultMessage: 'Unit 数量',
        }),

        dataIndex: 'resourcePool.unitCount',
        editable: false,
        fieldProps: () => ({
          rules: [
            {
              required: true,
              message: formatMessage({
                id: 'ocp-express.component.FormEditZoneReplicaTable.EnterTheUnitQuantity',
                defaultMessage: '请输入 Unit 数量',
              }),
            },
          ],
        }),

        fieldComponent: () => (
          <InputNumber
            min={1}
            placeholder={formatMessage({
              id: 'ocp-express.component.FormEditZoneReplicaTable.NullIndicatesThatTheNumberOfUnitsIs',
              defaultMessage: '为空表示不修改 Unit 数量',
            })}
          />
        ),
      },
    ];

    return (
      <FormEditTable
        mode="table"
        allowSwitch={true}
        allowAdd={false}
        saveLoading={saveLoading}
        columns={columns}
        pagination={false}
        {...this.props}
      />
    );
  }
}

export default FormEditZoneReplicaTable;
