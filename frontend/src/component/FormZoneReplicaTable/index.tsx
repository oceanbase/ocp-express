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
import { Checkbox, InputNumber } from '@oceanbase/design';
import { some } from 'lodash';
import { findBy } from '@oceanbase/util';
import { REPLICA_TYPE_LIST } from '@/constant/oceanbase';
import { validateUnitCount } from '@/util/oceanbase';
import type { FormEditTableProps } from '@/component/FormEditTable';
import FormEditTable from '@/component/FormEditTable';
import MySelect from '@/component/MySelect';
import UnitSpecSelect from '@/component/UnitSpecSelect';

const { Option } = MySelect;

export interface FormZoneReplicaTableProps<T> extends FormEditTableProps<T> {
  className?: string;
  value: T[];
  clusterData: API.ClusterInfo;
  zonesUnitCount?: number;
}

class FormZoneReplicaTable<T> extends FormEditTable<FormZoneReplicaTableProps<T>> {
  static validate = (rule, value, callback) => {
    if (
      value &&
      // 仅校验勾选行
      (some(value, item => item.checked && !item?.replicaType) ||
        some(value, item => item.checked && !item?.resourcePool?.unitSpecName) ||
        some(value, item => item.checked && !item?.resourcePool?.unitCount))
    ) {
      callback(
        formatMessage({
          id: 'ocp-express.component.FormZoneReplicaTable.IncompleteReplicaSettings',
          defaultMessage: '副本设置不完整',
        })
      );
    }
    callback();
  };

  public render() {
    const { clusterData, zonesUnitCount, ...restProps } = this.props;
    const columnWidth = '30%';

    const columns = [
      {
        title: '',
        dataIndex: 'checked',
        width: 24,
        editable: true,
        fieldProps: () => ({
          valuePropName: 'checked',
        }),
        fieldComponent: () => <Checkbox />,
      },
      {
        title: formatMessage({
          id: 'ocp-express.component.FormZoneReplicaTable.ZoneName',
          defaultMessage: 'Zone 名称',
        }),

        dataIndex: 'name',
        width: columnWidth,
        editable: true,
        fieldComponent: () => (
          // 由于每个 zone 都需要设置副本，直接从集群的 zones 同步过来即可，并置为不可编辑
          <MySelect disabled={true}>
            {((clusterData as API.ClusterInfo).zones || []).map(item => (
              <Option key={item.name} value={item.name}>
                {item.name}
              </Option>
            ))}
          </MySelect>
        ),
      },

      {
        title: formatMessage({
          id: 'ocp-express.component.FormZoneReplicaTable.CopyType',
          defaultMessage: '副本类型',
        }),

        dataIndex: 'replicaType',
        width: columnWidth,
        editable: true,
        fieldProps: (text, record) => {
          const zoneData = findBy(clusterData.zones || [], 'name', record.name);
          return {
            rules: [
              {
                // 仅勾选行需要必填
                required: zoneData.checked,
                message: formatMessage({
                  id: 'ocp-express.component.FormZoneReplicaTable.SelectAReplicaType',
                  defaultMessage: '请选择副本类型',
                }),
              },
            ],
          };
        },

        fieldComponent: () => (
          <MySelect>
            {REPLICA_TYPE_LIST.map(item => (
              <Option key={item.value} value={item.value}>
                {item.label}
              </Option>
            ))}
          </MySelect>
        ),
      },

      {
        title: formatMessage({
          id: 'ocp-express.component.FormZoneReplicaTable.UnitSpecification',
          defaultMessage: 'Unit 规格',
        }),

        // 嵌套的数据格式
        dataIndex: 'resourcePool.unitSpecName',
        width: columnWidth,
        editable: true,
        fieldProps: (text, record) => {
          const zoneData = findBy(clusterData.zones || [], 'name', record.name);
          return {
            rules: [
              {
                // 仅勾选行需要必填
                required: zoneData.checked,
                message: formatMessage({
                  id: 'ocp-express.component.FormZoneReplicaTable.SelectAUnit',
                  defaultMessage: '请选择 Unit 规格',
                }),
              },
            ],
          };
        },

        fieldComponent: () => <UnitSpecSelect obVersion={clusterData?.obVersion} />,
      },
      {
        title: formatMessage({
          id: 'ocp-express.component.FormZoneReplicaTable.UnitQuantity',
          defaultMessage: 'Unit 数量',
        }),

        // 嵌套的数据格式
        dataIndex: 'resourcePool.unitCount',
        editable: true,
        fieldProps: (text, record) => {
          const zoneData = findBy(clusterData.zones || [], 'name', record.name);
          return {
            initialValue: zonesUnitCount,
            rules: [
              {
                // 仅勾选行需要必填
                required: zoneData.checked,
                message: formatMessage({
                  id: 'ocp-express.component.FormZoneReplicaTable.UnitQuantityCannotBeEmpty',
                  defaultMessage: '请输入 Unit 数量',
                }),
              },

              {
                validator: (rule, value, callback) =>
                  validateUnitCount(rule, value, callback, zoneData),
              },
            ],
          };
        },
        fieldComponent: () => (
          <InputNumber
            min={1}
            // 设为只读模式，会去掉 InputNumber 的大部分样式
            readOnly={true}
            // 其余样式需要手动去掉
            className={'input-number-readonly'}
            // 只读模式下不展示 placeholder
            placeholder=""
          />
        ),
      },
    ];

    return (
      <FormEditTable
        mode="list"
        allowSwitch={false}
        allowAdd={false}
        allowDelete={false}
        columns={columns}
        {...restProps}
      />
    );
  }
}

export default FormZoneReplicaTable;
