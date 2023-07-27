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
import type { MouseEvent } from 'react';
import React from 'react';
import { Button, Drawer, Space } from '@oceanbase/design';
import type { ButtonProps } from 'antd/es/button';
import type { DrawerProps } from 'antd/es/drawer';
import { isBoolean } from 'lodash';

export type EventType = MouseEvent<HTMLElement, MouseEvent<Element, MouseEvent>>;

export interface MyDrawerProps extends DrawerProps {
  onOk?: (e: EventType) => void;
  onCancel?: (e: EventType) => void;
  confirmLoading?: boolean;
  footer?: React.ReactNode;
  extra?: React.ReactNode;
  cancelText?: string;
  okText?: string;
  okButtonProps?: ButtonProps;
}

const MyDrawer: React.FC<MyDrawerProps> = ({
  children,
  onClose,
  onOk,
  onCancel,
  cancelText = formatMessage({
    id: 'ocp-express.component.MyDrawer.Cancel',
    defaultMessage: '取消',
  }),
  okText = formatMessage({
    id: 'ocp-express.component.MyDrawer.Determine',
    defaultMessage: '确定',
  }),
  okButtonProps,
  confirmLoading = false,
  footer = true,
  extra,
  bodyStyle = {},
  ...restProps
}) => {
  return (
    <Drawer
      destroyOnClose={true}
      onClose={onClose || onCancel}
      bodyStyle={{
        // 存在底部操作栏时才追加底部 margin
        marginBottom: footer ? 53 : 0,
        ...bodyStyle,
      }}
      {...restProps}
    >
      {children}
      {(footer || extra) && (
        <div
          style={{
            position: 'absolute',
            right: 0,
            bottom: 0,
            width: 'calc(100% - 32px)',
            borderTop: '1px solid #e9e9e9',
            padding: '10px 16px',
            background: '#fff',
            zIndex: 10,
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <span>{extra}</span>
          <span>
            {isBoolean(footer) ? (
              <Space>
                <Button onClick={onClose || onCancel}>{cancelText}</Button>
                <Button onClick={onOk} type="primary" loading={confirmLoading} {...okButtonProps}>
                  {okText}
                </Button>
              </Space>
            ) : (
              footer
            )}
          </span>
        </div>
      )}
    </Drawer>
  );
};

export default MyDrawer;
