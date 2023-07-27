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
import { validatePassword } from '@/util';
import { MODAL_FORM_ITEM_LAYOUT } from '@/constant';
import Password from '@/component/Password';
import MyInput from '@/component/MyInput';
import encrypt from '@/util/encrypt';
import { useRequest } from 'ahooks';
import * as IamController from '@/service/ocp-express/IamController';

import './ModifyPasswordModal.less';

export interface ModifyDbUserPasswordProps {
  dispatch: any;
  userStats: any;
  ClusterInfo: API.ClusterInfo;
  tenantData: API.TenantInfo;
  dbUser: API.DbUser;
  onSuccess: () => void;
  loading: boolean;
}

const ModifyDbUserPassword: React.FC<ModifyDbUserPasswordProps> = ({
  dispatch,
  onSuccess,
  userStats,
  tenantData,
  dbUser,
  loading,
  ...restProps
}) => {
  const [passed, setPassed] = useState(true);

  const [form] = Form.useForm();
  const { getFieldsValue, validateFields } = form;

  const validateConfirmPassword = (rule, value, callback) => {
    const { newPassword } = getFieldsValue();
    if (value && value !== newPassword) {
      callback(
        formatMessage({
          id: 'ocp-express.Detail.Component.ModifyDbUserPassword.TheNewPasswordEnteredTwice',
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

  const handleSubmit = () => {
    validateFields().then(values => {
      const { newPassword, saveToCredential } = values;
      getLoginKey().then(response => {
        const publicKey = response?.data?.publicKey || '';
        dispatch({
          type: 'database/changeDbUserPassword',
          payload: {
            tenantId: tenantData.obTenantId,
            username: dbUser.username,
            newPassword: encrypt(newPassword, publicKey),
            saveToCredential,
          },

          onSuccess: () => {
            if (onSuccess) {
              message.success(
                formatMessage(
                  {
                    id: 'ocp-express.Detail.Component.ModifyDbUserPassword.DbuserusernamePasswordChanged',
                    defaultMessage: '{dbUserUsername} 密码修改成功',
                  },

                  { dbUserUsername: dbUser.username }
                )
              );

              onSuccess();
            }
          },
        });
      });
    });
  };

  return (
    <Modal
      width={480}
      title={formatMessage({
        id: 'ocp-express.Detail.Component.ModifyDbUserPassword.ChangePassword',
        defaultMessage: '修改密码',
      })}
      destroyOnClose={true}
      {...restProps}
      confirmLoading={loading}
      onOk={handleSubmit}
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
            id: 'ocp-express.Detail.Component.ModifyDbUserPassword.NewPassword',
            defaultMessage: '新密码',
          })}
          name="newPassword"
          rules={[
            {
              required: true,
              message: formatMessage({
                id: 'ocp-express.Detail.Component.ModifyDbUserPassword.EnterANewPassword',
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
            id: 'ocp-express.Detail.Component.ModifyDbUserPassword.ConfirmPassword',
            defaultMessage: '确认密码',
          })}
          name="confirmPassword"
          dependencies={['newPassword']}
          rules={[
            {
              required: true,
              message: formatMessage({
                id: 'ocp-express.Detail.Component.ModifyDbUserPassword.EnterANewPasswordAgain',
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
              id: 'ocp-express.Detail.Component.ModifyDbUserPassword.EnterANewPasswordAgain',
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
    loading: loading.effects['database/changeDbUserPassword'],
  };
}

export default connect(mapStateToProps)(ModifyDbUserPassword);
