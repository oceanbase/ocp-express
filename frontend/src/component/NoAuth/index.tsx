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
import type { EmptyProps } from '@/component/Empty';
import Empty from '@/component/Empty';

type NoAuthProps = EmptyProps;

export default ({
  image = '/assets/common/no_auth.svg',
  title = formatMessage({
    id: 'ocp-express.component.NoAuth.NoPermissionToView',
    defaultMessage: '暂无权限查看',
  }),
  description = formatMessage({
    id: 'ocp-express.component.NoAuth.PleaseContactTheAdministratorTo',
    defaultMessage: '请联系管理员开通权限',
  }),
  ...restProps
}: NoAuthProps) => <Empty image={image} title={title} description={description} {...restProps} />;
