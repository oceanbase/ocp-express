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
import { Col, Row, InputNumber } from '@oceanbase/design';

export interface unitSpec {
  cpuCore: number;
  memorySize: number;
}

export interface UnitSpecProps {
  unitSpecLimit?: any;
  idleUnitSpec?: any;
  defaultUnitSpec?: API.UnitConfig;
  onChange?: (value?: unitSpec) => void;
}

const UnitSpec: React.FC<UnitSpecProps> = ({
  unitSpecLimit,
  idleUnitSpec,
  defaultUnitSpec,
  onChange,
}) => {
  const [cpuCoreValue, setCpuCoreValue] = useState(defaultUnitSpec?.maxCpuCoreCount);
  const [memorySizeValue, setMemorySizeValue] = useState(defaultUnitSpec?.maxMemorySize);

  const onValueChange = (cpuCore: number, memorySize: number) => {
    if (onChange) {
      onChange({ cpuCore, memorySize });
    }
  };
  const extraStyle = {
    height: 22,
    fontSize: 14,
    color: '#8592AD',
    lineHeight: '22px',
  };

  const { cpuLowerLimit, memoryLowerLimit } = unitSpecLimit;
  const { idleCpuCore, idleMemoryInBytes } = idleUnitSpec;

  // 修改 unit 时，可配置范围上限，当前 unit 已分配内存 + 剩余空闲内存 
  const currentMaxMemorySize = idleMemoryInBytes + defaultUnitSpec?.maxMemorySize;

  return (
    <Row
      gutter={8}
      style={{
        flex: 1,
        paddingTop: 16,
      }}
    >
      <Col span={12}>
        <InputNumber
          min={cpuLowerLimit || 0.5}
          max={idleCpuCore}
          step={0.5}
          addonAfter={formatMessage({
            id: 'ocp-express.component.UnitSpec.Nuclear',
            defaultMessage: '核',
          })}
          defaultValue={defaultUnitSpec?.maxCpuCoreCount}
          onChange={value => {
            setCpuCoreValue(value);
            onValueChange(value, memorySizeValue);
          }}
        />

        {cpuLowerLimit && idleCpuCore && (
          <div style={extraStyle}>
            {formatMessage(
              {
                id: 'ocp-express.component.UnitSpec.CurrentConfigurableRangeValueCpulowerlimitIdlecpucore',
                defaultMessage: '当前可配置范围值 {cpuLowerLimit}~{idleCpuCore}',
              },
              { cpuLowerLimit: cpuLowerLimit, idleCpuCore: idleCpuCore }
            )}
          </div>
        )}
      </Col>
      <Col span={12}>
        <InputNumber
          addonAfter="GB"
          min={memoryLowerLimit}
          max={currentMaxMemorySize}
          defaultValue={defaultUnitSpec?.maxMemorySize}
          onChange={(value: number) => {
            setMemorySizeValue(value);
            onValueChange(cpuCoreValue, value);
          }}
        />

        {memoryLowerLimit !== undefined && currentMaxMemorySize !== undefined && (
          <div style={extraStyle}>

            {memoryLowerLimit < currentMaxMemorySize ? formatMessage(
              {
                id: 'ocp-express.component.UnitSpec.CurrentConfigurableRangeValueMemorylowerlimitIdlememoryinbytes',
                defaultMessage: '当前可配置范围值 {memoryLowerLimit}~{idleMemoryInBytes}',
              },
              { memoryLowerLimit, idleMemoryInBytes: currentMaxMemorySize }
            ) : formatMessage(
              {
                id: 'ocp-express.component.UnitSpec.CurrentConfigurableRangeValueMemorylowerlimitIdlememoryinbytes2',
                defaultMessage: '当前可配置资源不足',
              }
            )}
          </div>
        )}
      </Col>
    </Row>
  );
};

export default UnitSpec;
