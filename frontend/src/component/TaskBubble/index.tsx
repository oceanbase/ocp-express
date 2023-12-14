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
import { connect } from 'umi';
import React, { useState, useEffect } from 'react';
import { Badge, Popover, Table, Tooltip } from '@oceanbase/design';
import { SideTip } from '@oceanbase/ui';
import { CloseOutlined, ClockCircleOutlined } from '@oceanbase/icons';
import { findByValue, directTo } from '@oceanbase/util';
import { useInterval } from 'ahooks';
import { DEFAULT_LIST_DATA } from '@/constant';
import { TASK_STATUS_LIST } from '@/constant/task';
import { formatTime } from '@/util/datetime';
import { getTaskPercent } from '@/util/task';
import MyCard from '@/component/MyCard';
import MyProgress from '@/component/MyProgress';
import useStyles from './index.style';

export interface TaskBubbleProps {
  dispatch: any;
  loading: boolean;
  clusterId?: number | '' | null;
  tenantId?: number | '' | null;
  runningTaskList: API.TaskInstance[];
  runningTaskListDataRefreshDep: number | null;
  style?: React.CSSProperties;
  className?: string;
}

const TaskBubble: React.FC<TaskBubbleProps> = ({
  dispatch,
  loading,
  clusterId,
  tenantId,
  runningTaskList,
  runningTaskListDataRefreshDep,
  className,
  ...restProps
}) => {
  const { styles } = useStyles();
  const [visible, setVisible] = useState(false);
  const total = runningTaskList.length;
  // 当前如果还有正在执行中的任务，则发起轮询
  const polling = total > 0;

  const getRunningTaskList = () => {
    dispatch({
      type: 'task/getRunningTaskListData',
      payload: {
        clusterId,
        tenantId,
        // 获取运行中和失败的任务
        status: 'RUNNING,FAILED',
      },
    });
  };

  useEffect(() => {
    getRunningTaskList();
    // 组件下载后重置运行中的任务列表
    return () => {
      dispatch({
        type: 'task/update',
        payload: {
          runningTaskListData: DEFAULT_LIST_DATA,
        },
      });
    };
  }, [runningTaskListDataRefreshDep]);

  useInterval(
    () => {
      getRunningTaskList();
    },
    polling ? 1000 : null
  );

  const columns = [
    {
      title: formatMessage({
        id: 'ocp-express.component.TaskBubble.TaskName',
        defaultMessage: '任务名',
      }),
      dataIndex: 'name',
      ellipsis: true,
      render: (text: string, record: API.TaskInstance) => (
        <Tooltip placement="topLeft" title={text}>
          <a
            onClick={() => {
              directTo(`/task/${record.id}?backUrl=/task`);
            }}
          >
            {text}
          </a>
        </Tooltip>
      ),
    },
    {
      title: 'ID',
      dataIndex: 'id',
      width: 100,
    },
    {
      title: formatMessage({
        id: 'ocp-express.component.TaskBubble.ExecutionProgress',
        defaultMessage: '执行进度',
      }),
      dataIndex: 'tasks',
      width: 220,
      render: (text: API.SubtaskInstance[], record: API.TaskInstance) => (
        <MyProgress
          showInfo={false}
          // 带 % 单位的字符串
          affix={getTaskPercent(record, true)}
          affixWidth={40}
          // 不带 % 单位的数值
          percent={getTaskPercent(record, false) as number}
        />
      ),
    },
    {
      title: formatMessage({
        id: 'ocp-express.component.TaskBubble.State',
        defaultMessage: '状态',
      }),
      dataIndex: 'status',
      width: 100,
      render: (text: API.State) => {
        const statusItem = findByValue(TASK_STATUS_LIST, text);
        return <Badge status={statusItem.badgeStatus} text={statusItem.label} />;
      },
    },
    {
      title: formatMessage({
        id: 'ocp-express.component.TaskBubble.StartTime',
        defaultMessage: '开始时间',
      }),
      dataIndex: 'startTime',
      width: 220,
      render: (text: string) => <span>{formatTime(text)}</span>,
    },
  ];

  return (
    <Popover
      overlayClassName={styles.popover}
      placement="topRight"
      visible={visible}
      // 由于 SideTip 是 fixed 定位，因此需要将任务卡片挂载到 SideTip 上，否则滚动时两者会分离
      getPopupContainer={triggerNode => triggerNode}
      content={
        <MyCard
          className="card-without-padding"
          title={formatMessage(
            {
              id: 'ocp-express.component.TaskBubble.CurrentlyTotalTasksAreIn',
              defaultMessage: '当前共有 {total} 个任务正在进行中',
            },

            { total }
          )}
        >
          <Table
            loading={loading && !polling}
            dataSource={runningTaskList}
            columns={columns}
            rowKey={record => record.id}
            scroll={{
              y: 300,
            }}
            pagination={false}
          />
        </MyCard>
      }
    >
      <SideTip
        loading={loading && !polling}
        // 有正在执行中的任务，气泡按钮才显示为蓝色
        type={total > 0 ? 'primary' : 'default'}
        icon={visible ? <CloseOutlined /> : <ClockCircleOutlined />}
        onClick={() => {
          setVisible(!visible);
        }}
        badge={{
          count: total,
        }}
        tooltip={
          loading && !polling
            ? {
                title: formatMessage({
                  id: 'ocp-express.component.TaskBubble.InTheTaskRequest',
                  defaultMessage: '任务请求中',
                }),
                placement: 'left',
              }
            : total > 0
            ? undefined
            : {
                title: formatMessage({
                  id: 'ocp-express.component.TaskBubble.NoOngoingTasks',
                  defaultMessage: '没有正在进行的任务',
                }),
                placement: 'left',
              }
        }
        position={{
          right: 32,
          bottom: 104,
        }}
        {...restProps}
      />
    </Popover>
  );
};

function mapStateToProps({ loading, task }) {
  return {
    loading: loading.effects['task/getRunningTaskListData'],
    runningTaskList: (task.runningTaskListData && task.runningTaskListData.contents) || [],
    runningTaskListDataRefreshDep: task.runningTaskListDataRefreshDep,
  };
}

export default connect(mapStateToProps)(TaskBubble);
