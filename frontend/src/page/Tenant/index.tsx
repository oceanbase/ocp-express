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
import { history } from 'umi';
// import { EllipsisOutlined } from '@oceanbase/icons';
import React from 'react';
import { PageContainer } from '@oceanbase/ui';
import { Button, Col, Row } from '@oceanbase/design';
import { useRequest } from 'ahooks';
import * as ObTenantController from '@/service/ocp-express/ObTenantController';
import useDocumentTitle from '@/hook/useDocumentTitle';
import ContentWithReload from '@/component/ContentWithReload';
import Empty from '@/component/Empty';
import TenantList from './TenantList';
import useStyles from './index.style';

interface TenantProps {
  location: {
    query: {
      status?: string;
    };
  };
}

const Tenant: React.FC<TenantProps> = ({
  location: {
    query: { status },
  },
}: TenantProps) => {
  const { styles } = useStyles();
  const statusList = status?.split(',') || [];

  useDocumentTitle(
    formatMessage({
      id: 'ocp-express.Detail.Tenant.TenantManagement',
      defaultMessage: '租户管理',
    })
  );

  // 预先获取租户列表
  const {
    data: tenantListData,
    loading: tenantListLoading,
    refresh: listTenantsRefresh,
  } = useRequest(ObTenantController.listTenants, {
    defaultParams: [{}],
  });
  const tenantList = tenantListData?.data?.contents || [];

  // const handleMenuClick = (key: string) => {
  //   if (key === 'parameterTemplate') {
  //     history.push('/tenant/parameterTemplate');
  //   }
  //   if (key === 'unitSpec') {
  //     history.push('/tenant/unitSpec');
  //   }
  // };

  return !tenantListLoading && tenantList.length === 0 ? (
    <Empty
      title={formatMessage({ id: 'ocp-express.page.Tenant.NoTenant', defaultMessage: '暂无租户' })}
      description={formatMessage({
        id: 'ocp-express.page.Tenant.ThereIsNoDataRecord',
        defaultMessage: '暂无任何数据记录，立即新建一个租户吧！',
      })}
    >
      {/* <Button
        style={{ marginRight: 12 }}
        onClick={() => {
          history.push('/tenant/unitSpec');
        }}
      >
        {formatMessage({
          id: 'ocp-express.page.Tenant.UnitSpecificationManagement',
          defaultMessage: 'Unit 规格管理',
        })}
      </Button> */}
      {/* <Button
        style={{ marginRight: 12 }}
        onClick={() => {
          history.push('/tenant/parameterTemplate');
        }}
      >
        {formatMessage({
          id: 'ocp-express.page.Tenant.TenantParameterTemplateManagement',
          defaultMessage: '租户参数模板管理',
        })}
      </Button> */}
      <Button
        data-aspm-click="c318544.d343271"
        data-aspm-desc="暂无租户-新建租户"
        data-aspm-param={``}
        data-aspm-expo
        type="primary"
        onClick={() => {
          history.push('/tenant/new');
        }}
      >
        {formatMessage({ id: 'ocp-express.page.Tenant.NewTenant', defaultMessage: '新建租户' })}
      </Button>
    </Empty>
  ) : (
    <PageContainer
      className={styles.container}
      ghost={true}
      header={{
        title: (
          <ContentWithReload
            spin={tenantListLoading}
            content={formatMessage({
              id: 'ocp-express.Detail.Tenant.TenantManagement',
              defaultMessage: '租户管理',
            })}
            onClick={() => {
              // 刷新租户列表 (集群数据)
              listTenantsRefresh();
              // 无需刷新租户监控 Top5，TenantMonitorTop5 重新卸载、挂载后会自动发送请求
            }}
          />
        ),

        extra: (
          <>
            <Button
              data-aspm-click="c304184.d308814"
              data-aspm-desc="租户列表-新建租户"
              data-aspm-param={``}
              data-aspm-expo
              type="primary"
              onClick={() => {
                history.push('/tenant/new');
              }}
            >
              {formatMessage({
                id: 'ocp-express.page.Tenant.NewTenant',
                defaultMessage: '新建租户',
              })}
            </Button>
            {/* 二期 */}
            {/* <Dropdown
              overlay={
                <Menu
                  onClick={({ key }) => {
                    handleMenuClick(key);
                  }}
                >
                  <Menu.Item key="parameterTemplate">
                    <a>
                      {formatMessage({
                        id: 'ocp-express.page.Tenant.TenantParameterTemplateManagement',
                        defaultMessage: '租户参数模板管理',
                      })}
                    </a>
                  </Menu.Item>
                  <Menu.Item key="unitSpec">
                    <a>Unit 规格管理</a>
                  </Menu.Item>
                </Menu>
              }
            >
              <Button>
                <EllipsisOutlined />
              </Button>
            </Dropdown> */}
          </>
        ),
      }}
    >
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <TenantList statusList={statusList as API.TenantStatus[]} />
        </Col>
      </Row>
    </PageContainer>
  );
};

export default Tenant;
