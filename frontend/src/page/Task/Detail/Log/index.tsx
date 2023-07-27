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
import { Col, Row } from '@oceanbase/design';
import TaskGraph from './TaskGraph';
import LogCard from './LogCard';

export interface LogProps {
  ref?: any;
  taskData?: API.TaskInstance;
  onOperationSuccess: () => void;
  subtask?: API.SubtaskInstance;
  log?: string;
  logLoading?: boolean;
  logPolling?: boolean;
  onSubtaskChange: (subtaskId?: number | undefined) => void;
}

const Log: React.FC<LogProps> = React.forwardRef(
  (
    { taskData, onOperationSuccess, subtask, log, logLoading, logPolling, onSubtaskChange },
    ref
  ) => {
    return (
      <Row>
        <Col xxl={{ span: 9 }} xl={{ span: 12 }} md={{ span: 12 }} style={{ paddingRight: 8 }}>
          <TaskGraph
            ref={ref}
            taskData={taskData}
            onOperationSuccess={onOperationSuccess}
            subtask={subtask}
            onSubtaskChange={onSubtaskChange}
          />
        </Col>
        <Col xxl={{ span: 15 }} xl={{ span: 12 }} md={{ span: 12 }}>
          <LogCard
            taskData={taskData}
            onOperationSuccess={onOperationSuccess}
            subtask={subtask}
            log={log}
            logLoading={logLoading}
            logPolling={logPolling}
          />
        </Col>
      </Row>
    );
  }
);

export default Log;
