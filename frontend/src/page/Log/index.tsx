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
import React, { useRef } from 'react';
import { PageContainer } from '@oceanbase/ui';
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

  // 为了实现页面滚动到底部时，日志查询页能自动加载更多，因此需要获取上层父容器的 ref 用于滚动监听
  const containerRef = useRef<HTMLDivElement>(null);

  return (
    <div ref={containerRef} style={{ height: 'calc(100% - 48px)' }}>
      <PageContainer
        loading={reloading}
        ghost={true}
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
        <Query containerRef={containerRef} {...props} />
      </PageContainer>
    </div>
  );
};
