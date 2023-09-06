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
import { Typography, Row, Col, Divider, Checkbox, Space, token } from '@oceanbase/design';
import { groupBy } from 'lodash';
import { InfoCircleFilled } from '@oceanbase/icons';
import { isEnglish } from '@/util';
import ContentWithQuestion from '@/component/ContentWithQuestion';
import styles from './index.less';

interface IProps {
  onChange: (picked: API.SqlAuditStatSampleAttribute[]) => void;
  onReset: () => void;
  picked: API.SqlAuditStatSampleAttribute[];
  attributes: API.SqlAuditStatSampleAttribute[];
}

const SampleStatisticCard = ({ onChange, picked, attributes, onReset, ...restProps }: IProps) => {

  const handleChange = (keys: string[]) => {
    const next = attributes.filter(f => keys.includes(f.name as string));
    onChange(next);
  };

  const units = Object.keys(groupBy(picked, field => field.unit));

  const shouldDisable = (attr: API.SqlAuditStatSampleAttribute) => {
    // 已经被选中的指标，不禁用
    if (picked.find(field => field.name === attr.name)) {
      return false;
    }
    // 在已选中指标类型中，且选中指标数小于 10，则不禁用
    if (units.includes(attr.unit as string) && picked.length < 10) {
      return false;
    }
    // 未在已选中指标类型中，且选中指标数小于 10，选中指标类型小于 2，则不禁用
    if (!units.includes(attr.unit as string) && units.length < 2 && picked.length < 10) {
      return false;
    }
    return true;
  };

  return (
    <Row className={styles.statisticCard} {...restProps}>
      <Space style={{ width: '100%', justifyContent: 'space-between' }}>
        <Space>
          <Typography.Title level={5} style={{ margin: 0 }}>
            {formatMessage({
              id: 'ocp-express.SQLDiagnosis.Component.SampleStatisticCard.IndicatorManagement',
              defaultMessage: '指标管理',
            })}
          </Typography.Title>
          <InfoCircleFilled style={{ color: token.colorPrimary }} />
          <span style={{ color: 'rgba(0, 0, 0, .45)' }}>
            {formatMessage({
              id: 'ocp-express.SQLDiagnosis.Component.SampleStatisticCard.YouCanSelectAMaximum',
              defaultMessage: '同时可选择 2 种单位的指标，最多可选择 10 个指标',
            })}
          </span>
        </Space>
        <a style={{ float: 'right' }} onClick={onReset}>
          {formatMessage({
            id: 'ocp-express.SQLDiagnosis.Component.SampleStatisticCard.Reset',
            defaultMessage: '重置',
          })}
        </a>
      </Space>
      <Divider style={{ margin: '12px 0px' }} />
      <Row style={{ width: '100%' }}>
        <Checkbox.Group
          value={picked.map(f => f.name as string)}
          onChange={vs => handleChange(vs as string[])}
          className={styles.checkboxGroupGrid}
        >
          {attributes?.map(attr => {
            return (
              <Col span={isEnglish() ? 8 : 6} style={{ marginBottom: 16 }}>
                <Checkbox
                  style={{ color: 'rgba(0, 0, 0, 0.65)' }}
                  value={attr.name}
                  key={attr.name}
                  disabled={shouldDisable(attr)}
                >
                  <ContentWithQuestion
                    content={attr.title}
                    tooltip={{
                      placement: 'right',
                      title: attr.tooltip,
                    }}
                  />
                </Checkbox>
              </Col>
            );
          })}
        </Checkbox.Group>
      </Row>
    </Row>
  );
};

export default SampleStatisticCard;
