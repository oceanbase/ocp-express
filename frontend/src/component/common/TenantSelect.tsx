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
import { Select } from '@oceanbase/design';
import type { SelectProps } from 'antd/es/select';
import { useRequest } from 'ahooks';
import * as ObTenantController from '@/service/ocp-express/ObTenantController';

const { Option } = Select;

export interface TenantSelectProps extends SelectProps<string | number> {
  /* 值对应的属性字段 */
  valueProp?: 'obTenantId' | 'name';
  showAllOption?: boolean;
  allOption?: React.ReactNode;
  // 租户列表更新成功后的回调
  onSuccess?: (hostList: API.Tenant[]) => void;
}

const TenantSelect: React.FC<TenantSelectProps> = ({
  // 用作 value 的属性字段，默认为 obTenantId
  valueProp = 'obTenantId',
  showAllOption = false,
  allOption,
  onSuccess,
  ...restProps
}) => {
  const { data, loading } = useRequest(ObTenantController.listTenants, {
    // 需要设置 defaultParams，否则请求不发起
    defaultParams: [{}],
    onSuccess: res => {
      if (res.successful) {
        if (onSuccess) {
          onSuccess(res?.data?.contents || []);
        }
      }
    },
  });
  const tenantList = data?.data?.contents || [];

  return (
    <Select
      loading={loading}
      showSearch={true}
      optionFilterProp="children"
      dropdownMatchSelectWidth={false}
      placeholder={formatMessage({
        id: 'ocp-express.component.common.TenantSelect.SelectATenant',
        defaultMessage: '请选择租户',
      })}
      {...restProps}
    >
      {showAllOption && (
        <Option value="all">
          {formatMessage({
            id: 'ocp-express.component.common.TenantSelect.All',
            defaultMessage: '全部',
          })}
        </Option>
      )}
      {allOption}
      {tenantList.map(item => (
        <Option key={item.obTenantId} value={item[valueProp]} disabled={item.status === 'CREATING'}>
          {item.name}
        </Option>
      ))}
    </Select>
  );
};

export default TenantSelect;
