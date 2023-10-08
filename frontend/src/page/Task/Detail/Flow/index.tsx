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
import React, { useState, useEffect, useImperativeHandle } from 'react';
import { Empty, Spin, Tabs, token } from '@oceanbase/design';
import { DownOutlined, UpOutlined } from '@oceanbase/icons';
import { find, isFunction } from 'lodash';
import SplitPane from 'react-split-pane';
import { isEnglish } from '@/util';
import TaskGraph from './TaskGraph';
import useStyles from './index.style';

const { TabPane } = Tabs;

export interface DetailProps {
  taskData?: API.TaskInstance;
  onOperationSuccess: () => void;
  subtask?: API.SubtaskInstance;
  log?: string;
  logLoading?: boolean;
  logPolling?: boolean;
  onSubtaskChange: (subtaskId?: number | undefined) => void;
}

const Detail: React.FC<DetailProps> = React.forwardRef(
  (
    { taskData, onOperationSuccess, subtask, log, logLoading, logPolling, onSubtaskChange },
    ref
  ) => {
    const { styles } = useStyles();
    const MIN_SIZE = 32;
    // 英文环境下，任务详情会占据两行，需要减去额外的 38px，给流程图留出足够空间
    const DEFAULT_SIZE = isEnglish() ? 240 - 38 : 240;
    const [size, setSize] = useState(DEFAULT_SIZE);
    const [panes, setPanes] = useState<API.SubtaskInstance[]>(subtask ? [subtask] : []);
    const [collapsed, setCollapsed] = useState(!subtask);
    const [taskGraph, setTaskGraph] = useState<TaskGraph>();

    useImperativeHandle(ref, () => ({
      setLatestSubtask: () => {
        if (taskGraph && isFunction(taskGraph?.setLatestSubtask)) {
          taskGraph?.setLatestSubtask();
        }
      },
    }));

    // 日志变化后，需要滚动到底部
    useEffect(() => {
      // 实际展示了日志之后才滚动，且需要异步滚动，否则 scrollHeight 的值为 0
      if (!logLoading || logPolling) {
        setTimeout(() => {
          const logWrapper = document.getElementById('ocp-subtask-log-wrapper');
          if (logWrapper) {
            logWrapper.scrollTo({
              left: 0,
              top: logWrapper.scrollHeight,
            });
          }
        }, 0);
      }
    }, [
      log,
      // 将 logLoading 和 logPolling 作为依赖项，是因为日志的展示取决于这两项
      logLoading,
      logPolling,
    ]);

    function handleEditLogWindow(targetKey: string, action: 'add' | 'remove') {
      // 只处理关闭标签页的行为
      if (action === 'remove') {
        let lastIndex = -1;
        panes.forEach((item, i) => {
          // id 为数值，需要转为字符串才能与 targetKey 进行比较判断
          if (`${item.id}` === targetKey) {
            lastIndex = i - 1;
          }
        });
        const newPanes = panes.filter(item => `${item.id}` !== targetKey);
        // 关闭当前打开的 tab 页
        if (newPanes.length > 0 && `${subtask?.id}` === targetKey) {
          if (lastIndex >= 0) {
            onSubtaskChange(newPanes[lastIndex]?.id);
          } else {
            onSubtaskChange(newPanes[0]?.id);
          }
        }
        setPanes(newPanes);
        if (newPanes.length === 0) {
          // 关闭全部 tab 页时，选中的 tab 页为空
          onSubtaskChange(undefined);
          setCollapsed(true);
        }
      }
    }

    return (
      <SplitPane
        id="task-graph-split-pane"
        split="horizontal"
        primary="second"
        // 折叠状态最小高度为 MIN_SIZE，否则为 160
        minSize={collapsed ? MIN_SIZE : 160}
        maxSize={480}
        size={collapsed ? MIN_SIZE : size}
        onChange={value => {
          setSize(value);
          if (value === MIN_SIZE) {
            // 如果高度等于最小值，则设为收缩状态
            setCollapsed(true);
          } else if (value > MIN_SIZE && collapsed) {
            // 如果高度大于最小值，且为收缩状态，则设为展开状态
            setCollapsed(false);
          }
        }}
        className={`${styles.splitPane} ${isEnglish() ? styles.english : ''}`}
      >
        <div style={{ position: 'absolute', width: '100%' }}>
          <TaskGraph
            onRef={newTaskGraph => {
              setTaskGraph(newTaskGraph);
            }}
            taskData={taskData}
            subtask={subtask}
            onLogAdd={newSubtask => {
              const isExisted = find(panes, item => item.id === newSubtask?.id);
              // 如果对应子任务日志已在 Tab 中，则选中对应 Tab
              if (isExisted) {
                onSubtaskChange(newSubtask?.id);
              } else if (!isExisted && newSubtask) {
                // 如果对应子任务日志未在 Tab 中，则追加到末尾，并选中对应 Tab
                setPanes([...panes, newSubtask]);
                onSubtaskChange(newSubtask?.id);
              }
              // 就算点击查看的 pane 已存在，仍然需要展开日志 Tab
              setCollapsed(false);
              if (size === MIN_SIZE) {
                setSize(DEFAULT_SIZE);
              }
            }}
            onSuccess={() => {
              onOperationSuccess();
            }}
          />
        </div>
        <div className={styles.tabsWrapper}>
          <Tabs
            type="editable-card"
            hideAdd={true}
            activeKey={`${subtask?.id}`}
            onChange={key => {
              onSubtaskChange(key);
            }}
            onEdit={handleEditLogWindow}
            tabBarExtraContent={
              collapsed ? (
                <UpOutlined
                  onClick={() => {
                    setCollapsed(!collapsed);
                    if (size === MIN_SIZE) {
                      setSize(DEFAULT_SIZE);
                    }
                  }}
                />
              ) : (
                <DownOutlined
                  onClick={() => {
                    setCollapsed(!collapsed);
                    if (size === MIN_SIZE) {
                      setSize(DEFAULT_SIZE);
                    }
                  }}
                />
              )
            }
          >
            {panes.map(item => (
              <TabPane key={`${item.id}`} tab={`${item.name} (ID: ${item.id})`} />
            ))}
          </Tabs>
          <div
            id="ocp-subtask-log-wrapper"
            style={{
              overflow: 'auto',
              height: 'calc(100% - 32px)',
              backgroundColor: token.colorBgContainer,
            }}
          >
            {/* 不使用 Spin 对日志进行包裹，否则 loading 图标会出现在滚动区域的中间，切换 tab 时会看不到，而只能看到蒙层 */}
            {logLoading && !logPolling ? (
              <Spin />
            ) : log ? (
              <>
                <div className={styles.log}>{log}</div>
                <Spin spinning={logPolling} />
              </>
            ) : (
              <Empty
                description={formatMessage({
                  id: 'ocp-express.Detail.Flow.NoLog',
                  defaultMessage: '暂无日志',
                })}
                style={{ marginTop: 40 }}
              />
            )}
          </div>
        </div>
      </SplitPane>
    );
  }
);

export default Detail;
