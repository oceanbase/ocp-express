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
import { Alert, Form, Modal, message } from '@oceanbase/design';
import { useRequest } from 'ahooks';
import * as ObTenantController from '@/service/ocp-express/ObTenantController';
import WhitelistInput from '@/component/WhitelistInput';

const FormItem = Form.Item;

export interface ModifyWhitelistModalProps {
  tenantData: API.TenantInfo;
  onSuccess: () => void;
  loading: boolean;
}

const ModifyWhitelistModal: React.FC<ModifyWhitelistModalProps> = ({
  tenantData,
  onSuccess,
  ...restProps
}) => {
  const [form] = Form.useForm();
  const { validateFields } = form;

  const dispatch = useDispatch();

  const { run, loading } = useRequest(ObTenantController.modifyWhitelist, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        message.success(
          formatMessage({
            id: 'ocp-express.Detail.Component.ModifyWhitelistModal.IpAddressWhitelistModified',
            defaultMessage: 'IP 白名单修改成功',
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
      const { whitelist } = values;
      run(
        {
          tenantId: tenantData?.obTenantId,
        },

        { whitelist }
      );
    });
  };

  return (
    <Modal
      width={600}
      destroyOnClose={true}
      title={formatMessage({
        id: 'ocp-express.Detail.Component.ModifyWhitelistModal.ModifyTheWhitelist',
        defaultMessage: '修改白名单',
      })}
      onOk={handleSubmit}
      confirmLoading={loading}
      {...restProps}
    >
      <Alert
        type="info"
        showIcon={true}
        style={{ marginBottom: 24 }}
        message={formatMessage({
          id: 'ocp-express.Detail.Component.ModifyWhitelistModal.OnlyIpAddressesThatHave',
          defaultMessage: '只有已添加到白名单中的 IP 地址才可以访问该租户',
        })}
        description={
          <ul>
            <li>
              {formatMessage({
                id: 'ocp-express.Detail.Component.ModifyWhitelistModal.YouCanEnterAnIp',
                defaultMessage: '· 可以填写 IP 地址（如 127.0.0.1）或 IP 段（如 127.0.0.1/24）',
              })}
            </li>
            <li>
              {formatMessage({
                id: 'ocp-express.Detail.Component.ModifyWhitelistModal.MultipleIpAddressesNeedTo',
                defaultMessage: '· 多个 IP 需要用英文逗号隔开，如 127.0.0.1,127.0.0.1/24',
              })}
            </li>
            <li>
              {formatMessage({
                id: 'ocp-express.Detail.Component.ModifyWhitelistModal.IndicatesThatAccessToAnyIpAddressIs',
                defaultMessage: '127.0.0.1 表示禁止任何 IP 地址访问',
              })}
            </li>
          </ul>
        }
      />

      <Form form={form} preserve={false} layout="vertical" hideRequiredMark={true}>
        <FormItem
          name="whitelist"
          rules={[
            {
              validator: WhitelistInput.validate,
            },
          ]}
          initialValue={tenantData.whitelist}
        >
          <WhitelistInput layout="vertical" />
        </FormItem>
      </Form>
    </Modal>
  );
};

export default ModifyWhitelistModal;
