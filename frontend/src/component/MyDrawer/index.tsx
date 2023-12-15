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
import { Drawer } from '@oceanbase/design';
import type { DrawerProps } from '@oceanbase/design/es/drawer';

export type EventType = MouseEvent<HTMLElement, MouseEvent<Element, MouseEvent>>;

export interface MyDrawerProps extends DrawerProps {
  onOk?: (e: EventType) => void;
  onCancel?: (e: EventType) => void;
}

const MyDrawer: React.FC<MyDrawerProps> = ({
  children,
  onClose,
  onOk,
  onCancel,
  footer,
  ...restProps
}) => {

  return (
    <Drawer
      destroyOnClose={true}
      cancelText={formatMessage({ id: 'ocp-v2.component.MyDrawer.Cancel', defaultMessage: '取消' })}
      okText={formatMessage({ id: 'ocp-v2.component.MyDrawer.Determine', defaultMessage: '确定' })}
      onOk={onOk}
      onClose={onClose || onCancel}
      onCancel={onClose || onCancel}
      footer={footer}
      {...restProps}
    >
      {children}
    </Drawer>
  );
};

export default MyDrawer;
