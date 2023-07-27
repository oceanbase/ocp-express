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
import { useDispatch } from 'umi';
import React from 'react';
import { Form, message } from '@oceanbase/design';
import * as ObTenantController from '@/service/ocp-express/ObTenantController';
import { useRequest } from 'ahooks';
import MyDrawer from '@/component/MyDrawer';
import FormPrimaryZone from '@/component/FormPrimaryZone';

const FormItem = Form.Item;

export interface ModifyPrimaryZoneDrawerProps {
  tenantData?: API.TenantInfo;
  onSuccess?: () => void;
}

const ModifyPrimaryZoneDrawer: React.FC<ModifyPrimaryZoneDrawerProps> = ({
  tenantData,
  onSuccess,
  ...restProps
}) => {
  const [form] = Form.useForm();
  const { validateFields } = form;

  const dispatch = useDispatch();

  const { run, loading } = useRequest(ObTenantController.modifyPrimaryZone, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        message.success(
          formatMessage({
            id: 'ocp-express.Detail.Component.ModifyPrimaryZoneDrawer.TheZonePriorityHasBeen',
            defaultMessage: 'Zone 优先级修改成功',
          })
        );
        if (onSuccess) {
          onSuccess();
        }
        dispatch({
          type: 'tenant/getTenantData',
          payload: {
            tenantId: tenantData?.obTenantId,
          },
        });
      }
    },
  });

  const handleSubmit = () => {
    validateFields().then(values => {
      const { primaryZone } = values;
      run(
        {
          tenantId: tenantData?.obTenantId,
        },

        { primaryZone }
      );
    });
  };

  return (
    <MyDrawer
      width={1184}
      destroyOnClose={true}
      title={formatMessage({
        id: 'ocp-express.Detail.Component.ModifyPrimaryZoneDrawer.ModifyZonePriority',
        defaultMessage: '修改 Zone 优先级',
      })}
      onOk={handleSubmit}
      confirmLoading={loading}
      {...restProps}
    >
      <Form layout="inline" hideRequiredMark={true}>
        <FormItem
          label={formatMessage({
            id: 'ocp-express.Detail.Component.ModifyPrimaryZoneDrawer.TenantName',
            defaultMessage: '租户名',
          })}
          style={{ marginBottom: 24 }}
        >
          {tenantData.name}
        </FormItem>
      </Form>
      <Form form={form} preserve={false} layout="vertical" hideRequiredMark={true}>
        <FormItem
          label={formatMessage({
            id: 'ocp-express.Detail.Component.ModifyPrimaryZoneDrawer.ZonePrioritySorting',
            defaultMessage: 'Zone 优先级排序',
          })}
          name="primaryZone"
          initialValue={tenantData.primaryZone}
        >
          <FormPrimaryZone zoneList={(tenantData.zones || []).map(item => item.name)} />
        </FormItem>
      </Form>
    </MyDrawer>
  );
};

export default ModifyPrimaryZoneDrawer;
