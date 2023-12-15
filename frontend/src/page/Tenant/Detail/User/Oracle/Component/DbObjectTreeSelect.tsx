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
import React, { useState } from 'react';
import { TreeSelect, Tag, Space, theme } from '@oceanbase/design';
import { TableOutlined, FundViewOutlined, DeliveredProcedureOutlined } from '@oceanbase/icons';
import { uniq, findIndex } from 'lodash';

const { TreeNode } = TreeSelect;

interface TreeNodeType {
  key?: string;
  value: string;
  title: string;
  children?: [];
}

/**
 * 说明
 * TreeSelect
 * 用户的对象权限选择框
 *  */
interface DbObjectTreeSelectProps {
  value: string[];
  treeData: TreeNodeType[];
  objectType: string;
  dbObjectList: API.ObjectPrivilege[];
  addedDbObjects?: API.ObjectPrivilege[];
  onChange?: (value) => void;
}

const DbObjectTreeSelect: React.FC<DbObjectTreeSelectProps> = ({
  value,
  treeData,
  objectType,
  dbObjectList,
  addedDbObjects,
  onChange,
}) => {
  const { token } = theme.useToken();

  const [searchValue, setSearchValue] = useState('');

  const handleChange = newValue => {
    if (onChange) {
      onChange(newValue);
    }
  };

  const checkDbObjects = (val: string) => {
    let color = 'default';
    // 判断是否是否存在且正确  存在为赋权 标记为灰色
    if (
      findIndex(
        dbObjectList.filter(dbObject => dbObject?.objectType === objectType),
        item => item?.schemaName === val || item.fullName === val
      ) !== -1
    ) {
      //  已赋权 标记为黄色
      if (findIndex(addedDbObjects, item => item.object?.fullName === val) !== -1) {
        color = 'gold';
      }
    } else {
      // 不存在 标记为红色
      color = 'red';
    }
    return color;
  };
  const tagRender = (props: {
    label: string;
    value: string;
    closable: boolean;
    onClose: Function;
  }) => {
    const { closable, onClose } = props;
    return (
      <Tag
        key={props.value}
        color={checkDbObjects(props.value)}
        closable={closable}
        onClose={onClose}
      >
        {props.value}
      </Tag>
    );
  };

  const onPaste = e => {
    const pasteValue =
      (e.clipboardData?.getData('Text') && e.clipboardData?.getData('Text')?.split(',')) || [];
    const newValue = value ? uniq([...value, ...pasteValue]) : uniq(pasteValue);
    handleChange(newValue);
    setSearchValue('');
  };

  const OBJECT_TYPE = {
    TABLE: formatMessage({
      id: 'ocp-express.Oracle.Component.DbObjectTreeSelect.Table',
      defaultMessage: '表',
    }),
    VIEW: formatMessage({
      id: 'ocp-express.Oracle.Component.DbObjectTreeSelect.View',
      defaultMessage: '视图',
    }),
    STORED_PROCEDURE: formatMessage({
      id: 'ocp-express.Oracle.Component.DbObjectTreeSelect.StoredProcedure',
      defaultMessage: '存储过程',
    }),
  };

  return (
    <>
      <TreeSelect
        treeIcon={true}
        treeCheckable={true}
        tagRender={tagRender}
        value={value}
        searchValue={searchValue}
        placeholder={formatMessage(
          {
            id: 'ocp-express.Oracle.Component.DbObjectTreeSelect.SelectOrPasteObjecttypeobjecttype',
            defaultMessage: '选择或粘贴{OBJECTTYPEObjectType}',
          },
          { OBJECTTYPEObjectType: OBJECT_TYPE[objectType] }
        )}
        onChange={e => {
          handleChange(e);
        }}
        onSearch={v => {
          setSearchValue(v);
        }}
        onBlur={() => {
          setSearchValue(null);
        }}
        onPaste={e => onPaste(e)}
      >
        {treeData.map(item => {
          const treeNodeList = item?.children?.filter(object => object?.objectType === objectType);
          if (treeNodeList?.length > 0) {
            return (
              <TreeNode key={item.key} value={item.value} title={item.title}>
                {item?.children
                  ?.filter(object => object.objectType === objectType)
                  ?.map(treeItem => {
                    if (treeItem?.objectType === 'TABLE') {
                      return (
                        <TreeNode
                          key={treeItem?.value}
                          value={treeItem?.value}
                          title={
                            <Space>
                              <TableOutlined style={{ color: token.colorPrimary }} />
                              {treeItem.title}
                            </Space>
                          }
                        />
                      );
                    }
                    if (treeItem?.objectType === 'VIEW') {
                      return (
                        <TreeNode
                          key={treeItem?.value}
                          value={treeItem?.value}
                          title={
                            <Space>
                              <FundViewOutlined style={{ color: '#FA8C15' }} />
                              {treeItem.title}
                            </Space>
                          }
                        />
                      );
                    }
                    if (treeItem?.objectType === 'STORED_PROCEDURE') {
                      return (
                        <TreeNode
                          key={treeItem?.value}
                          value={treeItem?.value}
                          title={
                            <Space>
                              <DeliveredProcedureOutlined style={{ color: token.colorSuccess }} />
                              {treeItem.title}
                            </Space>
                          }
                        />
                      );
                    }
                    return null;
                  })}
              </TreeNode>
            );
          }
          return null;
        })}
      </TreeSelect>
    </>
  );
};
export default DbObjectTreeSelect;
