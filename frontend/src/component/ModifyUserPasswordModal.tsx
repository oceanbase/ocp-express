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
import { useDispatch, useSelector } from 'umi';
import React, { useState } from 'react';
import { Form, Modal } from '@oceanbase/design';
import type { ModalProps } from '@oceanbase/design/es/modal';
import { noop } from 'lodash';
import { MODAL_FORM_ITEM_LAYOUT } from '@/constant';
import { validatePassword } from '@/util';
import encrypt from '@/util/encrypt';
import Password from '@/component/Password';
import MyInput from '@/component/MyInput';

const FormItem = Form.Item;

export interface ModifyUserPasswordModalProps extends ModalProps {
  userData: Partial<API.User>;
  /* 是否为修改当前登录用户的密码 */
  isSelf: boolean;
  // 修改密码成功的回调函数
  onSuccess: () => void;
}

const ModifyUserPasswordModal: React.FC<ModifyUserPasswordModalProps> = ({
  userData = {},
  isSelf,
  onSuccess = noop,
  ...restProps
}) => {
  const dispatch = useDispatch();
  const { publicKey } = useSelector((state: DefaultRootState) => state.global);
  const modifyUserPasswordLoading = useSelector(
    (state: DefaultRootState) =>
      state.loading.effects['profile/modifyUserPassword'] ||
      state.loading.effects['iam/modifyUserPassword']
  );

  const [form] = Form.useForm();
  const { validateFields, getFieldsValue } = form;
  const [passed, setPassed] = useState(true);

  const validateNewPassword = (rule, value, callback) => {
    const { currentPassword } = getFieldsValue();
    if (value && value === currentPassword) {
      callback(
        formatMessage({
          id: 'ocp-express.src.component.ModifyUserPasswordModal.TheNewPasswordCannotBe',
          defaultMessage: '新密码不能与旧密码相同，请重新输入',
        })
      );
    } else {
      callback();
    }
  };

  const validateConfirmPassword = (rule, value, callback) => {
    const { newPassword } = getFieldsValue();
    if (value && value !== newPassword) {
      callback(
        formatMessage({
          id: 'ocp-express.src.component.ModifyUserPasswordModal.TheNewPasswordsEnteredTwice',
          defaultMessage: '两次输入的新密码不一致，请重新输入',
        })
      );
    } else {
      callback();
    }
  };

  const handleSubmit = () => {
    validateFields().then(values => {
      const { currentPassword, newPassword } = values;
      // 对新密码加密
      const encryptNewPassword = encrypt(newPassword, publicKey);
      if (isSelf) {
        dispatch({
          type: 'profile/modifyUserPassword',
          payload: {
            currentPassword: encrypt(currentPassword, publicKey),
            newPassword: encryptNewPassword,
          },

          onSuccess: () => {
            onSuccess();
          },
        });
      } else {
        dispatch({
          type: 'iam/modifyUserPassword',
          payload: {
            id: userData.id,
            newPassword: encryptNewPassword,
          },

          onSuccess: () => {
            onSuccess();
          },
        });
      }
    });
  };

  return (
    <Modal
      title={
        isSelf
          ? formatMessage({
              id: 'ocp-express.src.component.ModifyUserPasswordModal.ChangePassword',
              defaultMessage: '修改密码',
            })
          : formatMessage(
              {
                id: 'ocp-express.src.component.ModifyUserPasswordModal.ModifyThePasswordForUserdatausername',
                defaultMessage: '修改 {userDataUsername} 的密码',
              },

              { userDataUsername: userData.username }
            )
      }
      destroyOnClose={true}
      onOk={handleSubmit}
      confirmLoading={modifyUserPasswordLoading}
      {...restProps}
    >
      <Form
        form={form}
        layout="vertical"
        preserve={false}
        hideRequiredMark={true}
        {...MODAL_FORM_ITEM_LAYOUT}
      >
        {isSelf ? (
          <>
            <FormItem
              label={formatMessage({
                id: 'ocp-express.src.component.ModifyUserPasswordModal.OldPassword',
                defaultMessage: '旧密码',
              })}
              name="currentPassword"
              rules={[
                {
                  required: true,
                  message: formatMessage({
                    id: 'ocp-express.src.component.ModifyUserPasswordModal.PleaseEnterTheOldPassword',
                    defaultMessage: '请输入旧密码',
                  }),
                },
              ]}
            >
              <MyInput.Password
                autoComplete="new-password"
                placeholder={formatMessage({
                  id: 'ocp-express.src.component.ModifyUserPasswordModal.PleaseEnterTheOldPassword',
                  defaultMessage: '请输入旧密码',
                })}
              />
            </FormItem>

            <FormItem
              label={formatMessage({
                id: 'ocp-express.src.component.ModifyUserPasswordModal.NewPassword',
                defaultMessage: '新密码',
              })}
              name="newPassword"
              rules={[
                {
                  required: true,
                  message: formatMessage({
                    id: 'ocp-express.src.component.ModifyUserPasswordModal.PleaseEnterANewPassword',
                    defaultMessage: '请输入新密码',
                  }),
                },

                {
                  // 只对新密码进行密码校验，旧密码不做校验，避免老数据密码格式校验不通过时无法修改密码
                  validator: validatePassword(passed),
                },

                {
                  validator: validateNewPassword,
                },
              ]}
            >
              <Password onValidate={setPassed} />
            </FormItem>

            <FormItem
              label={formatMessage({
                id: 'ocp-express.src.component.ModifyUserPasswordModal.ConfirmNewPassword',
                defaultMessage: '确认密码',
              })}
              name="confirmPassword"
              dependencies={['newPassword']}
              rules={[
                {
                  required: true,
                  message: formatMessage({
                    id: 'ocp-express.src.component.ModifyUserPasswordModal.PleaseEnterANewPassword.2',
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
                  id: 'ocp-express.src.component.ModifyUserPasswordModal.PleaseEnterANewPassword.2',
                  defaultMessage: '请再次输入新密码',
                })}
              />
            </FormItem>
          </>
        ) : (
          <>
            <FormItem
              label={formatMessage({
                id: 'ocp-express.src.component.ModifyUserPasswordModal.NewPassword',
                defaultMessage: '新密码',
              })}
              name="newPassword"
              rules={[
                {
                  required: true,
                  message: formatMessage({
                    id: 'ocp-express.src.component.ModifyUserPasswordModal.PleaseEnterANewPassword',
                    defaultMessage: '请输入新密码',
                  }),
                },

                {
                  // 只对新密码进行密码校验，旧密码不做校验，避免老数据密码格式校验不通过时无法修改密码
                  validator: validatePassword(passed),
                },
              ]}
            >
              <Password onValidate={setPassed} />
            </FormItem>
            <FormItem
              label={formatMessage({
                id: 'ocp-express.src.component.ModifyUserPasswordModal.ConfirmPassword',
                defaultMessage: '确认密码',
              })}
              name="confirmPassword"
              dependencies={['newPassword']}
              rules={[
                {
                  required: true,
                  message: formatMessage({
                    id: 'ocp-express.src.component.ModifyUserPasswordModal.EnterANewPasswordAgain',
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
                  id: 'ocp-express.src.component.ModifyUserPasswordModal.EnterANewPasswordAgain',
                  defaultMessage: '请再次输入新密码',
                })}
              />
            </FormItem>
          </>
        )}
      </Form>
    </Modal>
  );
};

export default ModifyUserPasswordModal;
