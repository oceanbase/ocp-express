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

import React from 'react';
import { formatMessage } from '@/util/intl';
import { connect } from 'umi';
import { Descriptions, Modal } from '@oceanbase/design';

export interface DeleteUnitSpecModalProps {
  dispatch: any;
  visible: boolean;
  unitSpec: API.UnitSpec;
  onSuccess?: Function;
  onCancel?: (e: any) => void;
}

const DeleteUnitSpecModal: React.FC<DeleteUnitSpecModalProps> = ({
  dispatch,
  visible,
  unitSpec = {},
  onSuccess,
  ...restProps
}) => {
  const handleDelete = () => {
    dispatch({
      type: 'tenant/deleteUnitSpec',
      payload: {
        specId: unitSpec.id,
      },

      name: unitSpec.name,
      onSuccess: () => {
        if (typeof onSuccess === 'function') {
          onSuccess();
        }
      },
    });
  };

  return (
    <Modal
      title={formatMessage({
        id: 'ocp-express.src.component.DeleteUnitSpecModal.DeleteAUnitSpecification',
        defaultMessage: '删除 Unit 规格',
      })}
      visible={visible}
      onOk={handleDelete}
      okText={formatMessage({
        id: 'ocp-express.src.component.DeleteUnitSpecModal.Delete',
        defaultMessage: '删除',
      })}
      okButtonProps={{ danger: true, ghost: true }}
      destroyOnClose={true}
      {...restProps}
    >
      <Descriptions column={1}>
        <Descriptions.Item
          label={formatMessage({
            id: 'ocp-express.src.component.AddUnitSpecModal.SpecificationName',
            defaultMessage: '规格名称',
          })}
        >
          {unitSpec?.name}
        </Descriptions.Item>
        <Descriptions.Item
          label={formatMessage({
            id: 'ocp-express.src.component.DeleteUnitSpecModal.CpuCores',
            defaultMessage: 'CPU 核数',
          })}
        >
          {`${unitSpec?.minCpuCoreCount} ~ ${unitSpec?.maxCpuCoreCount}`}
        </Descriptions.Item>
        <Descriptions.Item
          label={formatMessage({
            id: 'ocp-express.src.component.DeleteUnitSpecModal.Memory',
            defaultMessage: '内存',
          })}
          style={{ paddingBottom: 0 }}
        >
          {`${unitSpec?.minMemorySize || 0} ~ ${unitSpec?.maxMemorySize || 0} GB`}
        </Descriptions.Item>
      </Descriptions>
    </Modal>
  );
};

export default connect(() => ({}))(DeleteUnitSpecModal);
