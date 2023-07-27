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
import { history, useSelector, useDispatch } from 'umi';
import React, { useEffect } from 'react';
import { Badge, Tooltip } from '@oceanbase/design';
import { toNumber } from 'lodash';
import { findByValue } from '@oceanbase/util';
import { CaretDownFilled } from '@ant-design/icons';
import { TENANT_STATUS_LIST } from '@/constant/tenant';
import { useBasicMenu, useTenantMenu } from '@/hook/useMenu';
import useDocumentTitle from '@/hook/useDocumentTitle';
import BasicLayout from '@/page/Layout/BasicLayout';
import TaskBubble from '@/component/TaskBubble';
import TenantSelect from '@/component/common/TenantSelect';
import styles from './index.less';

interface DetailProps {
  location: {
    pathname: string;
  };
  match: {
    params: {
      tenantId: number;
    };
  };
  children: React.ReactNode;
}

const Detail: React.FC<DetailProps> = (props: DetailProps) => {
  const {
    children,
    match: {
      params: { tenantId },
    },
    ...restProps
  } = props;
  const { tenantData } = useSelector((state: DefaultRootState) => state.tenant);
  const dispatch = useDispatch();

  // 集群详情里面，会返回 oraclePrivilegeManagementSupprted，只对Oracle租户有效
  // 租户详情里面，Oracle租户有oraclePrivilegeManagementSupprted字段，MySQL租户没有这个字段
  const menus = useTenantMenu(
    tenantId,
    tenantData?.mode,
    tenantData?.oraclePrivilegeManagementSupported,
    tenantData?.obVersion
  );

  const subSideMenus = useBasicMenu();

  useEffect(() => {
    dispatch({
      type: 'tenant/getTenantData',
      payload: {
        tenantId,
      },
    });
    return () => {
      dispatch({
        type: 'tenant/update',
        payload: {
          tenantData: {},
        },
      });
    };
  }, [tenantId]);

  useDocumentTitle(tenantData?.name && `${tenantData.name} | ${tenantData.clusterName}`);

  const statusItem = findByValue(TENANT_STATUS_LIST, tenantData.status);

  return (
    <BasicLayout
      menus={menus}
      subSideMenus={subSideMenus}
      subSideMenuProps={{ selectedKeys: ['/tenant'] }}
      sideHeader={
        <div className={styles.sideHeader}>
          <div className={styles.tenantWrapper}>
            <Tooltip title={statusItem.label}>
              <Badge
                status={statusItem.badgeStatus}
                style={{
                  cursor: 'pointer',
                  // 由于向右偏移了 3px，为了避免被 TenantSelect 覆盖无法选中，需要提升其 zIndex
                  zIndex: 1,
                  marginRight: -3,
                }}
              />
            </Tooltip>
            <Tooltip
              placement="right"
              title={
                <>
                  <div>
                    {formatMessage({
                      id: 'ocp-express.Tenant.Detail.TenantName',
                      defaultMessage: '租户名：',
                    })}
                    {tenantData?.name}
                  </div>
                  <div>
                    {formatMessage(
                      {
                        id: 'ocp-express.Tenant.Detail.TenantIdTenantid',
                        defaultMessage: '租户 ID：{tenantId}',
                      },
                      { tenantId: tenantId }
                    )}
                  </div>
                </>
              }
            >
              <TenantSelect
                valueProp="obTenantId"
                value={toNumber(tenantId)}
                bordered={false}
                suffixIcon={
                  <CaretDownFilled
                    className="ant-select-suffix"
                    style={{
                      fontSize: 12,
                      color: '#8592AD',
                    }}
                  />
                }
                onChange={value => {
                  history.push(`/tenant/${value}`);
                }}
                className={styles.tenantSelect}
              />
            </Tooltip>
          </div>
        </div>
      }
      {...restProps}
    >
      {children}
      <TaskBubble tenantId={tenantId} />
    </BasicLayout>
  );
};

export default Detail;
