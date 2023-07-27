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
import React, { useState, useEffect } from 'react';
import { Select, Radio, Button, Form, message } from '@oceanbase/design';
import * as ObClusterController from '@/service/ocp-express/ObClusterController';
import * as ObDatabaseController from '@/service/ocp-express/ObDatabaseController';
import { useRequest } from 'ahooks';
import type { MyDrawerProps } from '@/component/MyDrawer';
import MyDrawer from '@/component/MyDrawer';
import MyInput from '@/component/MyInput';
import MySelect from '@/component/MySelect';
import { DATABASE_NAME_RULE } from '@/constant';

const { Option } = Select;

interface AddDatabaseDrawerProps extends MyDrawerProps {
  database?: API.Database;
  visible: boolean;
  tenantId: number;
  tenantData: API.TenantInfo;
  onSuccess: () => void;
  onCancel: () => void;
}

const AddDatabaseDrawer: React.FC<AddDatabaseDrawerProps> = ({
  visible,
  database,
  tenantId,
  tenantData,
  onSuccess,
  onCancel,
  ...restProps
}) => {
  const isEdit = !!database;

  const [form] = Form.useForm();
  const { validateFields, setFieldsValue } = form;

  const [collations, setCollations] = useState<API.Collation[]>([]);

  // 获取租户字符集列表
  const { data, loading } = useRequest(ObClusterController.listCharsets, {
    defaultParams: [
      {
        tenantMode: tenantData?.mode,
      },
    ],
  });

  const charsetList = data?.data?.contents || [];

  // 默认字符集，优先级为: 数据库 > 租户 > utf8mb4
  const defaultCharset = database?.charset || tenantData.charset || 'utf8mb4';
  const defaultCollations =
    charsetList.find(item => item.name === defaultCharset)?.collations || [];
  // 默认 collation，优先级为: 数据库 > 租户 > 默认字符集的默认 collation
  const defaultCollation =
    database?.collation ||
    tenantData.collation ||
    defaultCollations.find(item => item.isDefault === true)?.name;

  // 打开抽屉时设置默认 collation 列表
  // 由于 defaultCollations 依赖于接口请求的结果，并不是静态值
  // 因此不能直接作为 useState 的初始值，需要通过 useEffect 来实现
  useEffect(() => {
    if (visible) {
      setCollations(defaultCollations);
    }
  }, [visible]);

  const { runAsync: createDatabase, loading: createDatabaseLoading } = useRequest(
    ObDatabaseController.createDatabase,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage({
              id: 'ocp-express.Detail.Component.AddDatabaseDrawer.DatabaseCreatedSuccessfully',
              defaultMessage: '数据库创建成功',
            })
          );
          if (onSuccess) {
            onSuccess();
          }
        }
      },
    }
  );

  const { runAsync: modifyDatabase, loading: modifyDatabaseLoading } = useRequest(
    ObDatabaseController.modifyDatabase,
    {
      manual: true,
      onSuccess: res => {
        if (res.successful) {
          message.success(
            formatMessage({
              id: 'ocp-express.Detail.Component.AddDatabaseDrawer.TheDatabaseHasBeenModified',
              defaultMessage: '数据库修改成功',
            })
          );
          if (onSuccess) {
            onSuccess();
          }
        }
      },
    }
  );

  const handleSubmit = () => {
    validateFields().then(values => {
      // 编辑数据库
      if (isEdit) {
        const { collation, dbName, readonly } = values;
        modifyDatabase(
          {
            tenantId,
            dbName,
          },
          {
            collation,
            readonly,
          }
        );
      } else {
        createDatabase(
          {
            tenantId,
          },
          values
        );
      }
    });
  };

  const handleCharsetChange = (value: string) => {
    const charset = charsetList.find(item => item.name === value);
    const collationList = charset?.collations || [];
    setCollations(collationList);
    // 选中默认的 collation
    setFieldsValue({ collation: collationList.find(item => item.isDefault === true)?.name });
  };

  return (
    <MyDrawer
      visible={visible}
      width={520}
      title={
        database
          ? formatMessage({
              id: 'ocp-express.Detail.Component.AddDatabaseDrawer.EditDatabase',
              defaultMessage: '编辑数据库',
            })
          : formatMessage({
              id: 'ocp-express.Detail.Component.AddDatabaseModal.CreateADatabase',
              defaultMessage: '新建数据库',
            })
      }
      destroyOnClose={true}
      onCancel={onCancel}
      footer={
        <>
          <Button
            style={{ marginRight: 10 }}
            onClick={() => {
              onCancel();
            }}
          >
            {formatMessage({
              id: 'ocp-express.Detail.Component.AddDatabaseModal.Cancel',
              defaultMessage: '取消',
            })}
          </Button>
          <Button
            type="primary"
            loading={createDatabaseLoading || modifyDatabaseLoading}
            onClick={() => handleSubmit()}
          >
            {formatMessage({
              id: 'ocp-express.Detail.Component.AddDatabaseModal.Submitted',
              defaultMessage: '提交',
            })}
          </Button>
        </>
      }
      {...restProps}
    >
      <Form form={form} layout="vertical" hideRequiredMark preserve={false}>
        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Detail.Component.AddDatabaseModal.DatabaseName',
            defaultMessage: '数据库名',
          })}
          name="dbName"
          initialValue={database?.dbName}
          rules={[
            {
              required: true,
              message: formatMessage({
                id: 'ocp-express.Detail.Component.AddDatabaseModal.EnterADatabaseName',
                defaultMessage: '请输入数据库名',
              }),
            },

            DATABASE_NAME_RULE,
          ]}
        >
          <MyInput
            style={{ width: 350 }}
            disabled={!!database}
            placeholder={formatMessage({
              id: 'ocp-express.Detail.Component.AddDatabaseModal.EnterADatabaseName',
              defaultMessage: '请输入数据库名',
            })}
          />
        </Form.Item>

        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Detail.Component.AddDatabaseModal.CharacterSet',
            defaultMessage: '字符集',
          })}
          name="charset"
          rules={[
            {
              required: true,
              message: formatMessage({
                id: 'ocp-express.Detail.Component.AddDatabaseModal.SelectACharacterSet',
                defaultMessage: '请选择字符集',
              }),
            },
          ]}
          initialValue={defaultCharset}
        >
          <MySelect
            loading={loading}
            showSearch={true}
            onChange={handleCharsetChange}
            style={{ width: 350 }}
          >
            {charsetList.map(item => (
              <Option key={item.name} value={item.name}>
                {item.name}
              </Option>
            ))}
          </MySelect>
        </Form.Item>
        <Form.Item
          label="Collation"
          name="collation"
          rules={[
            {
              required: true,
              message: formatMessage({
                id: 'ocp-express.Detail.Component.AddDatabaseModal.SelectCollation',
                defaultMessage: '请选择 Collation',
              }),
            },
          ]}
          initialValue={defaultCollation}
        >
          <Select
            style={{ width: 350 }}
            placeholder={formatMessage({
              id: 'ocp-express.Detail.Component.AddDatabaseModal.SelectCollation',
              defaultMessage: '请选择 Collation',
            })}
          >
            {collations.map(item => (
              <Option key={item.name} value={item.name}>
                {item.name}
              </Option>
            ))}
          </Select>
        </Form.Item>
        <Form.Item
          label={formatMessage({
            id: 'ocp-express.Detail.Component.AddDatabaseModal.ReadOnly',
            defaultMessage: '只读',
          })}
          name="readonly"
          rules={[
            {
              required: true,
            },
          ]}
          initialValue={(database && database.readonly) || false}
        >
          <Radio.Group>
            <Radio value={true}>
              {formatMessage({
                id: 'ocp-express.Detail.Component.AddDatabaseModal.Is',
                defaultMessage: '是',
              })}
            </Radio>
            <Radio value={false}>
              {formatMessage({
                id: 'ocp-express.Detail.Component.AddDatabaseModal.No',
                defaultMessage: '否',
              })}
            </Radio>
          </Radio.Group>
        </Form.Item>
      </Form>
    </MyDrawer>
  );
};

export default AddDatabaseDrawer;
