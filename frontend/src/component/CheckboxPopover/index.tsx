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
import { Typography, Row, Col, Divider, Checkbox, Space, Popover, token } from '@oceanbase/design';
import type { PopoverProps } from 'antd/es/popover';
import type { CheckboxOptionType } from 'antd/es/checkbox';
import { groupBy, some, uniq } from 'lodash';
import { isNullValue } from '@oceanbase/util';
import { InfoCircleFilled } from '@oceanbase/icons';
import ContentWithQuestion from '@/component/ContentWithQuestion';
import useStyles from './index.style';

export interface OptionType extends CheckboxOptionType {
  description?: string;
  span?: number;
  group?: string;
}

export type OptionValue = string | number | boolean;

export interface CheckboxPopoverProps extends PopoverProps {
  options?: OptionType[];
  defaultValue: OptionValue[];
  value: OptionValue[];
  onChange: (value: OptionValue[]) => void;
  /* 最多可选中的对象数，为空时不限制选中数 */
  maxSelectCount?: number;
  maxSelectCountLabel?: string;
  children: React.ReactNode;
}

const CheckboxPopover = ({
  title,
  options,
  defaultValue,
  value,
  onChange,
  maxSelectCount,
  maxSelectCountLabel,
  children,
  overlayClassName,
  ...restProps
}: CheckboxPopoverProps) => {
  const { styles } = useStyles();

  // 分组列表
  const groupList = uniq(options?.map(item => item.group));
  const groupByData = groupBy(options, 'group');
  // 分组选项存在多于 3 个的情况下，设置最大宽度，否则设置最小宽度
  const width = some(Object.keys(groupByData), key => groupByData[key].length > 3) ? 608 : 480;
  return (
    <Popover
      overlayStyle={{
        minWidth: width,
        maxWidth: width,
        padding: 0,
      }}
      overlayClassName={`${styles.container} ${overlayClassName}`}
      {...restProps}
      content={
        <Row>
          <Space style={{ width: '100%', justifyContent: 'space-between', padding: 16 }}>
            <Space>
              <Typography.Title level={5} style={{ margin: 0 }}>
                <span>{title}</span>
              </Typography.Title>
              {!isNullValue(maxSelectCount) && (
                <span>
                  <InfoCircleFilled style={{ color: token.colorPrimary }} />
                  <span style={{ marginLeft: 4, fontSize: 12, color: 'rgba(0, 0, 0, 0.45)' }}>
                    {maxSelectCountLabel ||
                      formatMessage(
                        {
                          id: 'ocp-express.component.CheckboxPopover.YouCanSelectUpTo',
                          defaultMessage: '最多可选择 {maxSelectCount} 个对象',
                        },
                        { maxSelectCount: maxSelectCount }
                      )}
                  </span>
                </span>
              )}
            </Space>
            <a
              style={{ float: 'right' }}
              onClick={() => {
                onChange(defaultValue);
              }}
            >
              {formatMessage({
                id: 'ocp-express.component.CheckboxPopover.Reset',
                defaultMessage: '重置',
              })}
            </a>
          </Space>
          <Divider style={{ margin: 0 }} />
          <Checkbox.Group
            value={value}
            onChange={newValue => {
              onChange(newValue);
            }}
            style={{
              width: '100%',
              padding: 16,
              maxHeight: 400,
              overflow: 'auto',
            }}
          >
            {groupList.map((group, index) => {
              return (
                <>
                  {group && (
                    <h4 style={{ marginTop: index === 0 ? 0 : 16, marginBottom: 8 }}>{group}</h4>
                  )}

                  <Row gutter={[16, 16]}>
                    {options
                      ?.filter(item => !group || item.group === group)
                      ?.map(item => {
                        return (
                          <Col key={item.value} span={item.span || 6}>
                            <Checkbox
                              value={item.value}
                              disabled={
                                !isNullValue(maxSelectCount) && !value.includes(item.value)
                                  ? value.length >= (maxSelectCount || 0)
                                  : false
                              }
                            >
                              <ContentWithQuestion
                                content={item.label}
                                tooltip={
                                  item.description
                                    ? {
                                        title: item.description,
                                      }
                                    : false
                                }
                              />
                            </Checkbox>
                          </Col>
                        );
                      })}
                  </Row>
                </>
              );
            })}
          </Checkbox.Group>
        </Row>
      }
    >
      {children}
    </Popover>
  );
};

export default CheckboxPopover;
