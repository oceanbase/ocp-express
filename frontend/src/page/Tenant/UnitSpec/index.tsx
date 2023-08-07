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
import React, { useState } from 'react';
import type { Route } from 'antd/es/breadcrumb/Breadcrumb';
import { Col, Row, Button, Card, Input, Table } from '@oceanbase/design';
import { PageContainer } from '@ant-design/pro-components';
import { findByValue } from '@oceanbase/util';
import * as ObUnitSpecController from '@/service/ocp-express/ObUnitSpecController';
import { getTableData } from '@/util';
import { UNIT_SPEC_TYPE_LIST } from '@/constant/oceanbase';
import useDocumentTitle from '@/hook/useDocumentTitle';
import { getOperationComponent, breadcrumbItemRender } from '@/util/component';
import ContentWithReload from '@/component/ContentWithReload';
import AddUnitSpecModal from '@/component/AddUnitSpecModal';
import DeleteUnitSpecModal from '@/component/DeleteUnitSpecModal';

import useStyles from './index.style';

export interface UnitProps {}
const Unit: React.FC<UnitProps> = () => {
  const { styles } = useStyles();
  useDocumentTitle(
    formatMessage({
      id: 'ocp-express.Tenant.UnitSpec.UnitSpecificationManagement',
      defaultMessage: 'Unit 规格管理',
    }),
  );
  const [visible, setVisible] = useState(false);
  const [currentUnitSpec, setCurrentUnitSpec] = useState<API.UnitSpec | null>(null);
  const [deleteModalVisible, setDeleteModalVisible] = useState(false);

  const [keyword, setKeyword] = useState('');
  const [inputKeyword, setInputKeyword] = useState('');
  const openUnitModal = (unitSpec: API.UnitSpec | null) => {
    setCurrentUnitSpec(unitSpec);
    setVisible(true);
  };

  const closeUnitModal = () => {
    setCurrentUnitSpec(null);
    setVisible(false);
  };

  const handleEidt = (record: API.UnitSpec) => {
    openUnitModal(record);
  };
  const handleDelete = (record: API.UnitSpec) => {
    setCurrentUnitSpec(record);
    setDeleteModalVisible(true);
  };

  const handleOperation = (type: 'delete' | 'modify', record: API.UnitSpec) => {
    if (type === 'delete') {
      handleDelete(record);
    } else if (type === 'modify') {
      handleEidt(record);
    }
  };

  const { tableProps, refresh, loading } = getTableData({
    fn: ObUnitSpecController.listUnitSpecs,
    params: {
      name: keyword,
    },

    deps: [keyword],
  });

  const columns = [
    {
      title: formatMessage({
        id: 'ocp-express.Tenant.UnitSpec.Specification',
        defaultMessage: '规格名',
      }),
      dataIndex: 'name',
    },

    {
      title: formatMessage({
        id: 'ocp-express.Tenant.UnitSpec.CpuCores',
        defaultMessage: 'CPU 核数',
      }),
      dataIndex: 'maxCpuCoreCount',
      sorter: true,
    },

    {
      title: formatMessage({
        id: 'ocp-express.Tenant.UnitSpec.MemoryGb',
        defaultMessage: '内存（GB）',
      }),
      dataIndex: 'maxMemorySize',
      sorter: true,
    },

    {
      title: formatMessage({ id: 'ocp-express.Tenant.UnitSpec.Type', defaultMessage: '规格类型' }),
      dataIndex: 'type',
      render: (text: API.UnitSpecType) => (
        <span>{findByValue(UNIT_SPEC_TYPE_LIST, text).label}</span>
      ),

      filters: UNIT_SPEC_TYPE_LIST.map((item) => ({
        text: item.label,
        value: item.value,
      })),
    },

    {
      title: formatMessage({ id: 'ocp-express.Tenant.UnitSpec.Operation', defaultMessage: '操作' }),
      dataIndex: 'operation',
      render: (text: string, record: API.UnitSpec) => {
        const operations = findByValue(UNIT_SPEC_TYPE_LIST, record.type).operations || [];
        return getOperationComponent({
          operations,
          handleOperation,
          record,
          displayCount: 2,
        });
      },
    },
  ];

  const routes: Route[] = [
    {
      path: '/tenant',
      breadcrumbName: formatMessage({
        id: 'ocp-express.Tenant.UnitSpec.Tenant',
        defaultMessage: '租户',
      }),
    },

    {
      breadcrumbName: formatMessage({
        id: 'ocp-express.Tenant.UnitSpec.UnitSpecificationManagement',
        defaultMessage: 'Unit 规格管理',
      }),
    },
  ];

  return (
    <PageContainer
      ghost={true}
      header={{
        title: (
          <ContentWithReload
            content={formatMessage({
              id: 'ocp-express.Tenant.UnitSpec.UnitSpecificationManagement',
              defaultMessage: 'Unit 规格管理',
            })}
            spin={loading}
            onClick={() => {
              setKeyword(inputKeyword);
              refresh();
            }}
          />
        ),

        extra: (
          <Button
            type="primary"
            onClick={() => {
              openUnitModal(null);
            }}
          >
            {formatMessage({
              id: 'ocp-express.Tenant.UnitSpec.NewUnitSpecifications',
              defaultMessage: '新增 Unit 规格',
            })}
          </Button>
        ),

        breadcrumb: { routes, itemRender: breadcrumbItemRender },
        onBack: () => {
          history.goBack();
        },
      }}
    >
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card
            bordered={false}
            title={formatMessage({
              id: 'ocp-express.Tenant.UnitSpec.UnitList',
              defaultMessage: 'Unit 列表',
            })}
            className={`card-without-padding ${styles.container}`}
            extra={
              <span className={styles.extra}>
                <Input.Search
                  onChange={(e) => {
                    setInputKeyword(e.target.value);
                  }}
                  onSearch={(value: string) => {
                    setKeyword(value);
                  }}
                  className="search-input-small"
                  placeholder={formatMessage({
                    id: 'ocp-express.Tenant.UnitSpec.SearchForASpecificationName',
                    defaultMessage: '请搜索规格名',
                  })}
                  allowClear={true}
                />
              </span>
            }
          >
            <Table columns={columns} rowKey={(record: API.UnitSpec) => record.id} {...tableProps} />
          </Card>
        </Col>
      </Row>
      <AddUnitSpecModal
        visible={visible}
        unitSpec={currentUnitSpec}
        onCancel={() => {
          closeUnitModal();
        }}
        onSuccess={() => {
          closeUnitModal();
          refresh();
        }}
      />

      <DeleteUnitSpecModal
        visible={deleteModalVisible}
        unitSpec={currentUnitSpec as API.UnitSpec}
        onSuccess={() => {
          setDeleteModalVisible(false);
          refresh();
        }}
        onCancel={() => setDeleteModalVisible(false)}
      />
    </PageContainer>
  );
};

export default Unit;
