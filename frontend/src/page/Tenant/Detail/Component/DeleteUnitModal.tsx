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
import React from 'react';
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Modal } from '@oceanbase/design';
import { MODAL_HORIZONTAL_FORM_ITEM_LAYOUT } from '@/constant';

const FormItem = Form.Item;

export interface DeleteUnitModalProps {
  dispatch: any;
  tenantData: API.TenantInfo;
  unit: API.Unit;
  onSuccess: () => void;
}

class DeleteUnitModal extends React.PureComponent<DeleteUnitModalProps> {
  public handleSubmit = () => {
    const { dispatch, tenantData, unit, onSuccess } = this.props;
    dispatch({
      type: 'tenant/deleteUnit',
      payload: {
        id: tenantData.clusterId,
        tenantId: tenantData.id,
        unitId: unit.id,
      },

      onSuccess: () => {
        if (onSuccess) {
          onSuccess();
        }
      },
    });
  };

  public render() {
    const { tenantData, unit, ...restProps } = this.props;
    return (
      <Modal
        title={formatMessage({
          id: 'ocp-express.Detail.Component.DeleteUnitModal.AreYouSureYouWant',
          defaultMessage: '确定要删除 Unit 吗？',
        })}
        destroyOnClose={true}
        onOk={this.handleSubmit}
        okText={formatMessage({
          id: 'ocp-express.Detail.Component.DeleteUnitModal.Delete',
          defaultMessage: '删除',
        })}
        okButtonProps={{
          ghost: true,
          danger: true,
        }}
        {...restProps}
      >
        <Form
          className="form-with-small-margin"
          layout="horizontal"
          hideRequiredMark={true}
          {...MODAL_HORIZONTAL_FORM_ITEM_LAYOUT}
        >
          <FormItem
            label={formatMessage({
              id: 'ocp-express.Detail.Component.DeleteUnitModal.Tenant',
              defaultMessage: '所属租户',
            })}
          >
            {tenantData.name}
          </FormItem>
          <FormItem
            label={formatMessage({
              id: 'ocp-express.Detail.Component.DeleteUnitModal.BelongsZone',
              defaultMessage: '所属 Zone',
            })}
          >
            {unit.zoneName}
          </FormItem>
          <FormItem label="Unit ID">{unit.id}</FormItem>
        </Form>
      </Modal>
    );
  }
}

export default connect(() => ({}))(DeleteUnitModal);
