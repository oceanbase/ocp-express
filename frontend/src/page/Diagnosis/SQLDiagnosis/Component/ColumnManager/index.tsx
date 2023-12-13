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

import ContentWithQuestion from '@/component/ContentWithQuestion';
import { ATTRIBUTE_GROUPS } from '@/constant/sqlDiagnosis';
import { isEnglish } from '@/util';
import { formatMessage } from '@/util/intl';
import { Button, Checkbox, Col, Divider, Drawer, Input, Row, theme } from '@oceanbase/design';
import React, { useEffect, useState } from 'react';
import { groupBy, isNumber, uniqBy } from 'lodash';
import { FilterOutlined } from '@oceanbase/icons';
import useStyles from './index.style';

interface IProps {
  sqlType: SQLDiagnosis.SqlType;
  queryValues?: SQLDiagnosis.QueryValues;
  setQueryValues?: any;
  fields: SQLDiagnosis.SqlAuditStatDetailAttribute[];
  attributes: SQLDiagnosis.SqlAuditStatDetailAttribute[];
  defaultFields: SQLDiagnosis.SqlAuditStatDetailAttribute[];
  onOk: (
    fileds: SQLDiagnosis.SqlAuditStatDetailAttribute[],
    actives: SQLDiagnosis.SqlAuditStatDetailAttribute[]
  ) => void;
}

const ColumnManager = ({
  sqlType,
  queryValues,
  setQueryValues,
  fields,
  onOk,
  attributes,
  defaultFields,
}: IProps) => {
  const { styles } = useStyles();
  const [visible, setVisible] = useState(false);

  const { token } = theme.useToken();

  // 筛选后的 attributes
  const [filterContents, setFilterContents] = useState<SQLDiagnosis.SqlAuditStatDetailAttribute[]>(
    []
  );

  const [selected, setSelected] = useState<SQLDiagnosis.SqlAuditStatDetailAttribute[]>([]);

  const reset = () => {
    setSelected(defaultFields);
    onOk(defaultFields, []);
  };

  const handleItemChange = (name: string, checked: boolean) => {
    if (checked) {
      const nextChecked = attributes.find(attr => attr.name === name);
      if (nextChecked) {
        setSelected(attributes.filter(attr => [...selected, nextChecked].indexOf(attr) !== -1));
      }
    } else {
      setSelected(selected.filter(item => item.name !== name));
    }
  };

  const onGroupChange = (checked: boolean, group: string) => {
    setSelected(
      checked
        ? // 选中当前组
        attributes.filter(
          attr =>
            uniqBy(
              [...selected, ...filterContents.filter(item => item.group === group)],
              i => i.name
            ).indexOf(attr) !== -1
        )
        : // 取消选中当前组
        selected.filter(item => {
          // displayAlways 表示常驻项，不可取消
          if (item.displayAlways === true) {
            return true;
          }
          return item.group !== group || !filterContents.map(i => i.name).includes(item.name);
        })
    );
  };

  const search = (word: string) => {
    if (!word) {
      setFilterContents(attributes.slice(0));
    } else {
      setFilterContents(
        attributes.filter(item => {
          return (
            item?.name?.toLocaleUpperCase().includes(word.toLocaleUpperCase()) ||
            item?.title?.toLocaleUpperCase().includes(word.toLocaleUpperCase()) ||
            (item?.group as string).toLocaleUpperCase().includes(word.toLocaleUpperCase())
          );
        })
      );
    }
  };

  useEffect(() => {
    if (Array.isArray(queryValues?.fields)) {
      setSelected(attributes.filter(attr => queryValues?.fields?.includes(attr.name)));
    } else {
      reset();
    }
    setFilterContents([...attributes]);
  }, [sqlType]);

  const groups = Object.entries(groupBy(filterContents, item => item.group)).sort((a, b) => {
    // 需要对 group 数据进行固定排序
    const indexA = ATTRIBUTE_GROUPS.findIndex(group => group.name === a[0]);
    const indexB = ATTRIBUTE_GROUPS.findIndex(group => group.name === b[0]);
    if (!isNumber(indexA) || !isNumber(indexB)) return 0;
    return indexA - indexB;
  });

  return (
    <>
      <span
        data-aspm-click="c304257.d308763"
        data-aspm-desc="SQL 列表-列管理"
        data-aspm-param={``}
        data-aspm-expo
        className={styles.tableExtra}
        onClick={() => {
          setVisible(true);
        }}
      >
        <FilterOutlined />
        {formatMessage({
          id: 'ocp-express.SQLDiagnosis.Component.ColumnManager.ColumnManagement',
          defaultMessage: '列管理',
        })}
      </span>
      <Drawer
        width={isEnglish() ? 1200 : 1056}
        title={formatMessage({
          id: 'ocp-express.SQLDiagnosis.Component.ColumnManager.ColumnManagement',
          defaultMessage: '列管理',
        })}
        open={visible}
        onClose={() => {
          setVisible(false);
        }}
        className={styles.drawer}
        bodyStyle={{
          marginBottom: 56,
        }}
      >
        <div className={styles.fieldSearch}>
          <Input.Search
            allowClear={true}
            placeholder={formatMessage({
              id: 'ocp-express.SQLDiagnosis.Component.FieldListDrawer.Enter',
              defaultMessage: '请输入',
            })}
            enterButton={true}
            onSearch={search}
          />
        </div>
        {groups.map(([key, items], index) => {
          const isLast = index === Object.keys(groups).length - 1;
          const groupValues = items
            .filter(item => !!selected.find(s => s.name === item.name))
            .map(({ name }) => name as string);
          const groupSelected = selected.filter(
            s => s.group === key && filterContents.find(i => i.name === s.name)
          );

          // 常驻指标不影响 Checkbox 的逻辑
          const checkedItems = items?.filter(item => item.displayAlways !== true);
          const realGroupSelected = groupSelected?.filter(item => item.displayAlways !== true);

          return (
            <div key={key}>
              <Row>
                <Col span={24} style={{ marginBottom: 24, marginTop: 24, fontWeight: 500 }}>
                  <Checkbox
                    indeterminate={
                      !!realGroupSelected.length && checkedItems.length !== realGroupSelected.length
                    }
                    checked={
                      !!realGroupSelected && checkedItems.length === realGroupSelected.length
                    }
                    value={key}
                    onChange={e => onGroupChange(e.target.checked, key)}
                  >
                    {ATTRIBUTE_GROUPS.find(group => group.name === key)?.title || key}
                  </Checkbox>
                </Col>
                <Checkbox.Group
                  value={groupValues}
                  // onChange={values => handleChange(key, values as string[])}
                  className={styles.checkboxGroupGrid}
                >
                  {items?.map(item => {
                    return (
                      <Col span={isEnglish() ? 8 : 6} style={{ marginBottom: 16 }}>
                        <Checkbox
                          value={item.name}
                          key={item.name}
                          disabled={item.displayAlways}
                          onChange={e => handleItemChange(item.name as string, e.target.checked)}
                        >
                          <ContentWithQuestion
                            content={
                              <span style={{ color: token.colorText }}>{item.title}</span>
                            }
                            tooltip={{
                              title: item.tooltip,
                            }}
                          />
                        </Checkbox>
                      </Col>
                    );
                  })}
                </Checkbox.Group>
              </Row>
              {isLast ? null : <Divider style={{ margin: 0 }} />}
            </div>
          );
        })}
        <div className={styles.listFooter}>
          <Button
            onClick={() => {
              setVisible(false);
            }}
          >
            {formatMessage({
              id: 'ocp-express.SQLDiagnosis.Component.FieldListDrawer.Cancel',
              defaultMessage: '取消',
            })}
          </Button>
          <Button onClick={reset}>
            {formatMessage({
              id: 'ocp-express.SQLDiagnosis.Component.FieldListDrawer.Reset',
              defaultMessage: '重置',
            })}
          </Button>
          <Button
            onClick={() => {
              // 新增的元数据
              const increment = selected.filter(
                attr => !fields.find(field => field.name === attr.name)
              );
              if (setQueryValues) {
                setQueryValues({ ...queryValues, fields: selected.map(attr => attr.name) });
              }

              // increment 用来做 table 新增列的动画渲染
              onOk(selected, increment);
              setVisible(false);
            }}
            type="primary"
          >
            {formatMessage({
              id: 'ocp-express.SQLDiagnosis.Component.FieldListDrawer.Determine',
              defaultMessage: '确定',
            })}
          </Button>
        </div>
      </Drawer>
    </>
  );
};

export default ColumnManager;
