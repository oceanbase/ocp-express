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
import { connect, useSelector } from 'umi';
import React, { useState } from 'react';
import { Checkbox, Form, Modal } from '@oceanbase/design';
import Password from '@/component/Password';
import MyInput from '@/component/MyInput';
import { validatePassword } from '@/util';
import encrypt from '@/util/encrypt';
import { MODAL_FORM_ITEM_LAYOUT } from '@/constant';

export interface ModifyPasswordModalProps {
  dispatch: any;
  clusterData: API.ClusterInfo;
  onSuccess: () => void;
}

const ModifyPasswordModal: React.FC<ModifyPasswordModalProps> = ({
  dispatch,
  onSuccess,
  clusterData,
  ...restProps
}) => {
  const { publicKey } = useSelector((state: DefaultRootState) => state.global);

  const [form] = Form.useForm();
  const { validateFields, getFieldsValue } = form;
  const { id } = clusterData;
  const [passed, setPassed] = useState(true);

  const validateConfirmPassword = (rule, value, callback) => {
    const { newPassword } = getFieldsValue();
    if (value && value !== newPassword) {
      callback(
        formatMessage({
          id: 'ocp-express.Detail.Component.ModifyPasswordModal.TheNewPasswordsEnteredTwice',
          defaultMessage: '两次输入的新密码不一致，请重新输入',
        })
      );
    } else {
      callback();
    }
  };

  const handleSubmit = () => {
    validateFields().then(values => {
      const { newPassword, saveToCredential } = values;
      // 对新密码加密
      const encryptNewPassword = encrypt(newPassword, publicKey);
      dispatch({
        type: 'cluster/changePassword',
        payload: {
          id,
          newPassword: encryptNewPassword,
          saveToCredential,
        },

        onSuccess: () => {
          if (onSuccess) {
            onSuccess();
          }
        },
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
      onOk={handleSubmit}
      {...restProps}
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
            id: 'ocp-express.Detail.Component.ModifyPasswordModal.OperationObject',
            defaultMessage: '操作对象',
          })}
        >
          {formatMessage(
            {
              id: 'ocp-express.Detail.Component.ModifyPasswordModal.ThePasswordOfTheRoot',
              defaultMessage: 'OB 集群 {clusterDataName} 的 sys 租户的 root 账号的密码',
            },

            { clusterDataName: clusterData.name }
          )}
        </Form.Item>
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
                id: 'ocp-express.Detail.Component.ModifyPasswordModal.PleaseEnterANewPassword',
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
                id: 'ocp-express.Detail.Component.ModifyPasswordModal.PleaseEnterANewPassword.2',
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
              id: 'ocp-express.Detail.Component.ModifyPasswordModal.PleaseEnterANewPassword.2',
              defaultMessage: '请再次输入新密码',
            })}
          />
        </Form.Item>
        <Form.Item name="saveToCredential" initialValue={true} valuePropName="checked">
          <Checkbox>
            {formatMessage({
              id: 'ocp-express.Detail.Component.ModifyPasswordModal.SaveToPasswordBox',
              defaultMessage: '保存到密码箱',
            })}
          </Checkbox>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default connect(() => ({}))(ModifyPasswordModal);
