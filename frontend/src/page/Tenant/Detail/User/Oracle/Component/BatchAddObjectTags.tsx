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
import { Tag, Input } from '@oceanbase/design';
import { uniq, findIndex } from 'lodash';

/**
 * 说明
 * Input Tag
 *  */
interface BatchAddObjectTagsProps {}

const BatchAddObjectTags: React.FC<BatchAddObjectTagsProps> = () => {
  const [tags, setTags] = useState<string[]>([]);
  const [inputValue, setInputValue] = useState('');

  const handleInputChange = e => {
    if (e.target.value.replace(/\s*/g, '') !== '') {
      setInputValue(e.target.value);
    }
  };

  const handleInputConfirm = () => {
    if (inputValue.indexOf(',') !== -1) {
      setTags(uniq([...tags, ...inputValue.split(',').map(item => item.replace(/\s*/g, ''))]));
    } else if (inputValue !== '' && findIndex(tags, item => item === inputValue) === -1) {
      setTags(uniq([...tags, inputValue]));
    }
    setInputValue('');
  };

  const paddingValue = tags?.length > 0 ? 5 : 0;
  return (
    <>
      <div
        style={{
          border: '1px solid #d9d9d9',
          maxHeight: 560,
          padding: paddingValue,
          overflow: 'auto',
        }}
      >
        {tags.map(
          item =>
            item !== '' && (
              <Tag key={item} closable={true}>
                {item}
              </Tag>
            )
        )}

        <Input
          type="text"
          placeholder={formatMessage({
            id: 'ocp-express.Oracle.Component.BatchAddObjectTags.Enter',
            defaultMessage: '请输入',
          })}
          value={inputValue}
          bordered={false}
          onChange={handleInputChange}
          onBlur={handleInputConfirm}
          onPressEnter={handleInputConfirm}
        />
      </div>
    </>
  );
};
export default BatchAddObjectTags;
