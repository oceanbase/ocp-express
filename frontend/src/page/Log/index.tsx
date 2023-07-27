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
import { PageContainer } from '@ant-design/pro-components';
import useDocumentTitle from '@/hook/useDocumentTitle';
import useReload from '@/hook/useReload';
import ContentWithReload from '@/component/ContentWithReload';
import Query from './Query';
import type { QueryLogProps } from './Query';
interface Props {
  location: QueryLogProps['location'];
}

export default (props: Props) => {
  useDocumentTitle(
    formatMessage({
      id: 'ocp-express.page.Log.LogQuery',
      defaultMessage: '日志查询',
    })
  );

  const [reloading, reload] = useReload(false);

  return (
    <PageContainer
      loading={reloading}
      ghost={true}
      style={{ minHeight: '102vh' }}
      header={{
        title: (
          <ContentWithReload
            content={formatMessage({
              id: 'ocp-express.page.Log.LogQuery',
              defaultMessage: '日志查询',
            })}
            spin={reloading}
            onClick={reload}
          />
        ),
      }}
    >
      <Query {...props} />
    </PageContainer>
  );
};
