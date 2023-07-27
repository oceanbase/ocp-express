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
import { connect } from 'umi';
import React, { useState } from 'react';
import { Form, Modal, message } from '@oceanbase/design';
import { useRequest } from 'ahooks';
import * as IamController from '@/service/ocp-express/IamController';
import * as ObUserController from '@/service/ocp-express/ObUserController';
import { MODAL_FORM_ITEM_LAYOUT } from '@/constant';
import { isEnglish, validatePassword } from '@/util';
import Password from '@/component/Password';
import MyInput from '@/component/MyInput';
import encrypt from '@/util/encrypt';

export interface ModifyPasswordModalProps {
  ClusterInfo: API.ClusterInfo;
  tenantData: API.TenantInfo;
  onSuccess: () => void;
}

const ModifyPasswordModal: React.FC<ModifyPasswordModalProps> = ({
  onSuccess,
  tenantData,
  ...restProps
}) => {
  const [passed, setPassed] = useState(true);

  const [form] = Form.useForm();
  const { validateFields, getFieldsValue } = form;

  const validateConfirmPassword = (rule, value, callback) => {
    const { newPassword } = getFieldsValue();
    if (value && value !== newPassword) {
      callback(
        formatMessage({
          id: 'ocp-express.Detail.Component.ModifyPasswordModal.TheNewPasswordEnteredTwice',
          defaultMessage: '两次输入的新密码不一致，请重新输入',
        })
      );
    } else {
      callback();
    }
  };

  const { runAsync: getLoginKey } = useRequest(IamController.getLoginKey, {
    manual: true,
  });

  const { run: changeDbUserPassword, loading } = useRequest(ObUserController.changeDbUserPassword, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        message.success(
          formatMessage({
            id: 'ocp-express.Detail.Component.ModifyPasswordModal.PasswordModifiedSuccessfully',
            defaultMessage: '密码修改成功',
          })
        );
        if (onSuccess) {
          onSuccess();
        }
      }
    },
  });

  const handleSubmit = () => {
    validateFields().then(values => {
      const { newPassword, saveToCredential } = values;
      getLoginKey().then(response => {
        const publicKey = response?.data?.publicKey || '';
        changeDbUserPassword(
          {
            tenantId: tenantData.obTenantId,
            username: tenantData.mode === 'ORACLE' ? 'SYS' : 'root',
          },
          {
            newPassword: encrypt(newPassword, publicKey),
            saveToCredential,
          }
        );
      });
    });
  };

  return (
    <Modal
      title={formatMessage({
        id: 'ocp-express.Detail.Component.ModifyPasswordModal.ChangePassword',
        defaultMessage: '修改密码',
      })}
      destroyOnClose={true}
      {...restProps}
      confirmLoading={loading}
      onOk={handleSubmit}
      {...(isEnglish() ? { width: 600 } : {})}
    >
      <Form
        form={form}
        layout="vertical"
        hideRequiredMark
        preserve={false}
        {...MODAL_FORM_ITEM_LAYOUT}
      >
        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Detail.Component.ModifyPasswordModal.NewPassword',
            defaultMessage: '新密码',
          })}
          name="newPassword"
          rules={[
            {
              required: true,
              message: formatMessage({
                id: 'ocp-express.Detail.Component.ModifyPasswordModal.EnterANewPassword',
                defaultMessage: '请输入新密码',
              }),
            },
            {
              validator: validatePassword(passed),
            },
          ]}
        >
          <Password onValidate={setPassed} />
        </Form.Item>
        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Detail.Component.ModifyPasswordModal.ConfirmPassword',
            defaultMessage: '确认密码',
          })}
          name="confirmPassword"
          dependencies={['newPassword']}
          rules={[
            {
              required: true,
              message: formatMessage({
                id: 'ocp-express.Detail.Component.ModifyPasswordModal.EnterANewPasswordAgain',
                defaultMessage: '请再次输入新密码',
              }),
            },

            {
              validator: validateConfirmPassword,
            },
          ]}
        >
          <MyInput.Password
            autoComplete="new-password"
            placeholder={formatMessage({
              id: 'ocp-express.Detail.Component.ModifyPasswordModal.EnterANewPasswordAgain',
              defaultMessage: '请再次输入新密码',
            })}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};

function mapStateToProps({ loading }) {
  return {
    loading: loading.effects['tenant/changePassword'],
  };
}

export default connect(mapStateToProps)(ModifyPasswordModal);
