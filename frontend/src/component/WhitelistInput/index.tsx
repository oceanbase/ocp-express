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
import { Form } from '@ant-design/compatible';
import '@ant-design/compatible/assets/index.css';
import { Radio, Tag, Tooltip, token } from '@oceanbase/design';
import ContentWithIcon from '@/component/ContentWithIcon';
import { ExclamationCircleFilled } from '@oceanbase/icons';
import type { RadioChangeEvent } from 'antd/es/radio';
import type { SelectValue } from 'antd/es/select';
import { some } from 'lodash';
import validator from 'validator';
import { SELECT_TOKEN_SPEARATORS } from '@/constant';
import { isWhitelistWithWildCard } from '@/util';
import MySelect from '@/component/MySelect';
import styles from './index.less';

const FormItem = Form.Item;

export type Type = '%' | 'custom';

export interface WhitelistInputProps {
  value?: string;
  layout?: string;
  onChange?: (value: string) => void;
}

interface WhitelistInputState {
  type: Type;
}

/**
 * 是否为合法的租户白名单地址，支持四种格式
 * 1.% 通配符
 * 2.ipv4 地址
 * 3.子网/掩码格式
 * 4.通配符匹配格式
 */
function isWhitelistIP(str: string) {
  return (
    str === '%' ||
    // ipv4 地址
    validator.isIP(str, '4') ||
    // 子网/掩码格式
    validator.isIPRange(str) ||
    // 通配符匹配格式
    isWhitelistWithWildCard(str)
  );
}

class WhitelistInput extends React.Component<WhitelistInputProps, WhitelistInputState> {
  static getDerivedStateFromProps(props: WhitelistInputProps) {
    const newState: any = {
      prevProps: props,
    };

    const { value } = props;
    if ('value' in props) {
      // 将下发的 value 赋值给 FormEditTable
      newState.type = value === '%' ? '%' : 'custom';
    }
    return newState;
  }

  static validate = (rule, value: string, callback) => {
    if (!value || value === '') {
      callback(
        formatMessage({
          id: 'ocp-express.Tenant.New.EnterAnIpWhitelist',
          defaultMessage: '请输入 IP 白名单',
        })
      );
    }
    if (value && some(value.split(','), item => !isWhitelistIP(item))) {
      callback(
        formatMessage({
          id: 'ocp-express.component.WhitelistInput.InvalidIpAddress',
          defaultMessage: 'IP 地址不合法',
        })
      );
    }
    callback();
  };

  constructor(props: WhitelistInputProps) {
    super(props);
    this.state = {
      type: '%',
    };
  }

  public handleChange = (value: string) => {
    const { onChange } = this.props;
    if (onChange) {
      // 通过下发的 onChange 属性函数收集 WhitelistInput 的值
      onChange(value);
    }
  };

  public handleTypeChange = (e: RadioChangeEvent) => {
    const type = e.target.value;
    this.setState({
      type,
    });

    this.handleChange(type === '%' ? '%' : '');
  };

  public handleWhitelistChange = (val: SelectValue) => {
    this.handleChange((val as string[]).join(','));
  };

  public render() {
    const { value, layout } = this.props;
    const { type } = this.state;
    return (
      <Form colon={false} layout={layout || 'horizontal'} className={styles.container}>
        <FormItem
          label={formatMessage({
            id: 'ocp-express.component.WhitelistInput.IpAddressWhitelist',
            defaultMessage: 'IP 地址白名单',
          })}
          style={{ marginBottom: 24 }}
          extra={
            type === '%' && (
              <ContentWithIcon
                prefixIcon={{
                  component: ExclamationCircleFilled,
                  style: {
                    color: token.colorWarning,
                    marginTop: -1,
                  },
                }}
                content={formatMessage({
                  id: 'ocp-express.component.WhitelistInput.ExistAccessSafetyRisk',
                  defaultMessage: '存在访问安全风险，请谨慎操作',
                })}
                style={{
                  color: token.colorWarning,
                  // 与 icon 的 marginTop 配合，可以实现 icon 和文本的上对齐
                  alignItems: 'flex-start',
                }}
              />
            )
          }
        >
          <Radio.Group value={type} onChange={this.handleTypeChange}>
            <Radio value="custom">
              {formatMessage({
                id: 'ocp-express.component.WhitelistInput.Custom',
                defaultMessage: '自定义',
              })}
            </Radio>
            <Radio value="%">
              {formatMessage({
                id: 'ocp-express.component.WhitelistInput.AllIpAddressesAreAccessible',
                defaultMessage: '所有 IP 都可访问',
              })}
            </Radio>
          </Radio.Group>
        </FormItem>
        {type === 'custom' && (
          <FormItem
            label={formatMessage({
              id: 'ocp-express.component.WhitelistInput.CustomIpAddress',
              defaultMessage: '自定义 IP',
            })}
            extra={
              <>
                <Tooltip
                  placement="right"
                  title={
                    <ul>
                      <li>
                        {formatMessage({
                          id: 'ocp-express.component.WhitelistInput.SpecifyTheListOfClients',
                          defaultMessage: '在这里指定允许登陆的客户端列表，支持的格式有：',
                        })}
                      </li>
                      <li>
                        {formatMessage({
                          id: 'ocp-express.component.WhitelistInput.IpAddressExample',
                          defaultMessage: '127.0.0.1,127.0.0.2',
                        })}
                      </li>
                      <li>
                        {formatMessage({
                          id: 'ocp-express.component.WhitelistInput.SubnetMaskExample',
                          defaultMessage: '子网/掩码，示例：127.0.0.1/24',
                        })}
                      </li>
                      <li>
                        {formatMessage({
                          id: 'ocp-express.component.WhitelistInput.FuzzyMatchingExampleOr',
                          defaultMessage: '模糊匹配，示例：127.0.0.% 或 127.0.0._',
                        })}
                      </li>
                      <li>
                        {formatMessage({
                          id: 'ocp-express.component.WhitelistInput.MultipleFormatsAreMixedFor',
                          defaultMessage:
                            '多种格式混合，示例：127.0.0.10,127.0.0.2,127.0.0.%,127.0.0._,127.0.0.1/24',
                        })}
                      </li>
                      <li>
                        {formatMessage({
                          id: 'ocp-express.component.WhitelistInput.SpecialNoteIndicatesThatAll',
                          defaultMessage: '特殊说明：% 表示所有客户端都可以连接',
                        })}
                      </li>
                    </ul>
                  }
                >
                  <a>
                    {formatMessage({
                      id: 'ocp-express.component.WhitelistInput.ViewConfigurationInstructions',
                      defaultMessage: '查看配置说明',
                    })}
                  </a>
                </Tooltip>
              </>
            }
          >
            <MySelect
              mode="tags"
              allowClear={true}
              tokenSeparators={SELECT_TOKEN_SPEARATORS}
              value={(value && value.split(',')) || []}
              maxTagCount={20}
              onChange={this.handleWhitelistChange}
              placeholder={formatMessage({
                id: 'ocp-express.component.WhitelistInput.EnterAnIpAddressSeparate',
                defaultMessage: '请输入 IP 地址，多个 IP 地址请以逗号分隔',
              })}
              tagRender={props => {
                const { label, value: tagValue, closable, onClose } = props;
                return (
                  <Tag
                    color={isWhitelistIP(tagValue as string) ? 'default' : 'red'}
                    closable={closable}
                    onClose={onClose}
                    className="select-tag-render-item"
                  >
                    {label}
                  </Tag>
                );
              }}
            />
          </FormItem>
        )}
      </Form>
    );
  }
}

export default WhitelistInput;
