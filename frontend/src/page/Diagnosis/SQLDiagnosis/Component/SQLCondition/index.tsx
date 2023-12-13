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
import React, { useEffect } from 'react';
import { Button, Col, Form, Input, Modal, Row, Tag, theme } from '@oceanbase/design';
import { find, flatMap, uniqueId } from 'lodash';
import { PlusOutlined, DeleteOutlined } from '@oceanbase/icons';
import MySelect from '@/component/MySelect';
import useStyles from './index.style';

const { Option } = MySelect;

interface SQLConditionProps {
  onChange?: (value: SQLDiagnosis.FilterExpressionList) => void;
  searchAttrList: SQLDiagnosis.SqlAuditStatDetailAttribute[];
  value?: SQLDiagnosis.FilterExpressionList;
}

const SQLCondition: React.FC<SQLConditionProps> = ({
  searchAttrList = [],
  onChange,
  value: propValue,
}) => {
  const { styles } = useStyles();
  const { token } = theme.useToken();

  const [list, setList] = React.useState<SQLDiagnosis.FilterExpressionList>(
    generateTagValue(propValue),
  );

  const [visible, setVisible] = React.useState(false);
  const [form] = Form.useForm();

  useEffect(() => {
    if (propValue?.length === 0) {
      setList([]);
    }
  }, [propValue]);

  // 运算符列表
  const searchOpList = ['=', '!=', '>', '>=', '<', '<='];

  // 将值转换为 TagRender 所需要的值
  function generateTagValue(filterExpressionList?: SQLDiagnosis.FilterExpressionList) {
    if (!Array.isArray(filterExpressionList)) {
      return [];
    }

    return filterExpressionList.map((item) => {
      // 使用 title 来展示
      const searchAttrTitle = find(searchAttrList, (select) => select.name === item.searchAttr)
        ?.title as string;

      return {
        ...item,
        label: `${searchAttrTitle} ${item.searchOp} ${item.searchVal}`,
        // value 是为了在 tagRender 的时候作为唯一值，在删除的时候进行匹配
        value: `OCP_TAG_VALUE_${searchAttrTitle}_${item.searchOp}_${item.searchVal}_${uniqueId()}`,
      };
    });
  }

  function tagRender(props: { label: string; value: string; onClose: (...arg: any) => void }) {
    const { label, value } = props;

    const onPreventMouseDown = (event) => {
      event.preventDefault();
      event.stopPropagation();
    };

    return value === 'add' ? (
      <Tag
        onMouseDown={onPreventMouseDown}
        style={{ marginRight: 3, borderStyle: 'dashed', color: token.colorPrimary }}
        onClick={() => {
          setVisible(true);
        }}
      >
        <PlusOutlined /> {label}
      </Tag>
    ) : (
      <Tag
        key={value}
        closable={true}
        onClose={() => {
          // 删除这个选项
          const result = list.filter((item) => item.value !== value);

          setList(result);
          if (onChange) {
            onChange(result);
          }
        }}
        onMouseDown={onPreventMouseDown}
        style={{ marginRight: 3 }}
      >
        {label}
      </Tag>
    );
  }

  return (
    <>
      <MySelect
        className={styles['add-condition-select']}
        mode="multiple"
        tagRender={tagRender}
        value={[
          {
            label: formatMessage({
              id: 'ocp-express.Component.SQLCondition.Add',
              defaultMessage: '添加',
            }),
            value: 'add',
          },

          ...list,
        ]}
        style={{ width: '100%' }}
        open={false}
      />

      <Modal
        title={formatMessage({
          id: 'ocp-express.Component.SQLCondition.AddAdvancedConditions',
          defaultMessage: '添加高级条件',
        })}
        width={752}
        bodyStyle={{ maxHeight: 512 }}
        visible={visible}
        onCancel={() => setVisible(false)}
        onOk={() => {
          form.validateFields().then((value) => {
            const { filterExpressionList = [] } = value;
            const result = [...list, ...generateTagValue(filterExpressionList)];
            if (onChange) {
              onChange(result);
            }

            setList(result);
            setVisible(false);
          });
        }}
        destroyOnClose={true}
      >
        <Form form={form} layout="vertical" preserve={false} colon={false} requiredMark="optional">
          {/*  initialValue={[{}]} 默认有一个占位符 */}
          <Form.List name="filterExpressionList" initialValue={[{}]}>
            {(fields, { add, remove }) => {
              return (
                <>
                  {fields.map((field, index) => {
                    return (
                      <Row key={field.key}>
                        <Col span={22}>
                          <Row gutter={[8, 0]} className={styles.topSqlAdvanceSearchGroup}>
                            <Col span={12}>
                              <Form.Item noStyle={true} shouldUpdate={true}>
                                {({ getFieldValue }) => {
                                  const params = getFieldValue('filterExpressionList');
                                  const otherSearchAttrs: string[] = flatMap(
                                    // 已选择的指标包括当前 Modal 中的选择的指标和已存在 Input 中的指标，list（已存在 Input 中的指标）
                                    [
                                      ...list,
                                      ...params?.filter(
                                        (item, ii) => ii !== index && item?.searchAttr,
                                      ),
                                    ],

                                    (item) => item.searchAttr,
                                  );

                                  return (
                                    <Form.Item
                                      name={[field.name, 'searchAttr']}
                                      label={
                                        index === 0 &&
                                        formatMessage({
                                          id: 'ocp-express.Component.SQLCondition.Metric',
                                          defaultMessage: '指标',
                                        })
                                      }
                                      rules={[
                                        {
                                          required: true,
                                          message: formatMessage({
                                            id: 'ocp-express.Component.SQLCondition.SelectAMetric',
                                            defaultMessage: '请选择指标',
                                          }),
                                        },
                                      ]}
                                    >
                                      <MySelect
                                        allowClear={true}
                                        showSearch={true}
                                        optionFilterProp="children"
                                      >
                                        {searchAttrList
                                          .filter(
                                            (item) =>
                                              // 已选择的筛选指标需要过滤掉
                                              !otherSearchAttrs.includes(item.name) &&
                                              // SQL 文本不能作为筛选指标
                                              item.name !== 'sqlTextShort' &&
                                              // 诊断结果不能作为筛选指标
                                              item.name !== 'diagTypes',
                                          )
                                          .map((item) => (
                                            <Option key={item.name} value={item.name as string}>
                                              {item.title}
                                            </Option>
                                          ))}
                                      </MySelect>
                                    </Form.Item>
                                  );
                                }}
                              </Form.Item>
                            </Col>
                            <Col span={4}>
                              <Form.Item
                                name={[field.name, 'searchOp']}
                                label={
                                  index === 0 &&
                                  formatMessage({
                                    id: 'ocp-express.Component.SQLCondition.Operator',
                                    defaultMessage: '运算符',
                                  })
                                }
                                rules={[
                                  {
                                    required: true,
                                    message: formatMessage({
                                      id: 'ocp-express.Component.SQLCondition.SelectAnOperator',
                                      defaultMessage: '请选择运算符',
                                    }),
                                  },
                                ]}
                              >
                                <MySelect
                                  allowClear={true}
                                  showSearch={true}
                                  optionFilterProp="children"
                                >
                                  {searchOpList.map((item) => (
                                    <Option key={item} value={item}>
                                      {item}
                                    </Option>
                                  ))}
                                </MySelect>
                              </Form.Item>
                            </Col>
                            <Col span={8}>
                              <Form.Item noStyle={true} shouldUpdate={true}>
                                {({ getFieldValue }) => {
                                  const searchAttr = getFieldValue([
                                    'filterExpressionList',
                                    field.name,
                                    'searchAttr',
                                  ]);

                                  const attrItem = searchAttrList.find(
                                    (item) => item.name === searchAttr,
                                  );

                                  return (
                                    <Form.Item
                                      name={[field.name, 'searchVal']}
                                      label={
                                        index === 0 &&
                                        formatMessage({
                                          id: 'ocp-express.Component.SQLCondition.Value',
                                          defaultMessage: '值',
                                        })
                                      }
                                      rules={[
                                        {
                                          required: true,
                                          message: formatMessage({
                                            id: 'ocp-express.Component.SQLCondition.EnterAValue',
                                            defaultMessage: '请输入值',
                                          }),
                                        },
                                      ]}
                                    >
                                      <Input
                                        addonAfter={attrItem?.unit}
                                        style={{
                                          // 为了避免 addonAfter 导致表单控件无法对齐，需要将 top 设置为 0
                                          top: 0,
                                        }}
                                        placeholder={formatMessage({
                                          id: 'ocp-express.Component.SQLCondition.Enter',
                                          defaultMessage: '请输入',
                                        })}
                                      />
                                    </Form.Item>
                                  );
                                }}
                              </Form.Item>
                            </Col>
                          </Row>
                        </Col>
                        {fields.length > 1 && (
                          <Col span={1} style={{ textAlign: 'center' }}>
                            <Form.Item
                              // 使用一个空格作为占位符，避免删除 icon 顶到分组表头上来
                              label={index === 0 && ' '}
                              // 设置必填，否则上层 Form 设置 requiredMark="optional" 后，label 后会出现`（可选）`的标记
                              // 由于并不是包裹一个表单控件，因此设置必填并不影响表单提交
                              required={true}
                              style={{ marginBottom: 12 }}
                            >
                              <DeleteOutlined
                                style={{ color: token.colorTextTertiary }}
                                onClick={() => {
                                  remove(field.name);
                                }}
                              />
                            </Form.Item>
                          </Col>
                        )}
                      </Row>
                    );
                  })}
                  <Row gutter={24}>
                    <Col span={22}>
                      <Button
                        data-aspm-click="c318543.d343270"
                        data-aspm-desc="SQL 查询-添加高级条件"
                        data-aspm-param={``}
                        data-aspm-expo
                        type="dashed"
                        onClick={() => {
                          add();
                        }}
                        style={{ width: '100%' }}
                      >
                        <PlusOutlined />
                        {formatMessage({
                          id: 'ocp-express.Component.SQLCondition.Add',
                          defaultMessage: '添加',
                        })}
                      </Button>
                    </Col>
                  </Row>
                </>
              );
            }}
          </Form.List>
        </Form>
      </Modal>
    </>
  );
};

export default SQLCondition;
