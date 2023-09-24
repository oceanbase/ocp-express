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
import React, { useState, useEffect, useRef, useImperativeHandle } from 'react';
import { Dropdown, Menu, Space, Tooltip, Typography, token } from '@oceanbase/design';
import { debounce, some } from 'lodash';
import Icon, { MoreOutlined } from '@oceanbase/icons';
import { findByValue, isNullValue } from '@oceanbase/util';
import { Canvas } from '@antv/g';
import { useSize, useUpdateEffect } from 'ahooks';
import scrollIntoView from 'scroll-into-view';
import useDeepCompareEffect from 'use-deep-compare-effect';
import * as TaskController from '@/service/ocp-express/TaskController';
import { TIME_FORMAT } from '@/constant/datetime';
import { isEnglish } from '@/util';
import { formatTime } from '@/util/datetime';
import { downloadLog } from '@/util/log';
import type { Node, SubtaskOperationKey } from '@/util/task';
import { getNodes, getLatestNode, getTaskDuration, handleSubtaskOperate } from '@/util/task';
import useStyles from './TaskGraph.style';

const { Text } = Typography;

export interface TaskGraphProps {
  taskData?: API.TaskInstance;
  onOperationSuccess: () => void;
  subtask?: API.SubtaskInstance;
  onSubtaskChange: (subtaskId: number | undefined) => void;
}

export interface TaskGraphRef {
  setLatestSubtask: () => void;
}

const TaskGraph: React.FC<TaskGraphProps> = React.forwardRef<TaskGraphRef, TaskGraphProps>(
  ({ taskData, onOperationSuccess, subtask, onSubtaskChange }, ref) => {
    const SuccessfulSVG = () => (
      <svg width="16px" height="16px" viewBox="0 0 16 16">
        <g stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
          <g transform="translate(-361.000000, -348.000000)">
            <rect x="0" y="0" width="1832" height="2338" />
            <g transform="translate(361.000000, 348.000000)">
              <circle id="Oval" fill={token.colorSuccess} cx="8" cy="8" r="8" />
              <path
                d="M11.628125,5.256875 L7.679375,10.731875 C7.44125,11.06375 6.948125,11.06375 6.71,10.731875 L4.371875,7.491875 C4.300625,7.3925 4.371875,7.25375 4.49375,7.25375 L5.373125,7.25375 C5.564375,7.25375 5.74625,7.345625 5.85875,7.503125 L7.19375,9.355625 L10.14125,5.268125 C10.25375,5.1125 10.43375,5.01875 10.626875,5.01875 L11.50625,5.01875 C11.628125,5.01875 11.699375,5.1575 11.628125,5.256875 Z"
                id="Path"
                fill="#FFFFFF"
              />
            </g>
          </g>
        </g>
      </svg>
    );

    const RunningSVG = () => (
      <svg width="16px" height="16px" viewBox="0 0 16 16">
        <defs>
          <path
            id="task-running-path"
            d="M9.43262271,4.42882477 C9.59660387,4.49682156 9.67441458,4.68487685 9.60641779,4.84885801 C9.538421,5.01283917 9.35036571,5.09064988 9.18638455,5.02265309 C8.80642835,4.86509962 8.39753625,4.7829647 7.97663966,4.7829647 C6.23102536,4.7829647 4.81592537,6.19806469 4.81592537,7.94367899 C4.81592537,9.68929329 6.23102536,11.1043933 7.97663966,11.1043933 C8.82661157,11.1043933 9.62247019,10.7677735 10.2116022,10.1786415 C10.5051023,9.88514133 10.7387085,9.53676499 10.8976656,9.15342388 C11.055219,8.77346768 11.1373539,8.36457558 11.1373539,7.94367899 C11.1373539,7.76615889 11.2812624,7.62225042 11.4587825,7.62225042 C11.6363026,7.62225042 11.7802111,7.76615889 11.7802111,7.94367899 C11.7802111,8.44945253 11.6812751,8.94198533 11.4914939,9.39966203 C11.3001098,9.86120416 11.0190847,10.2802962 10.6661708,10.6332101 C9.95767487,11.3417061 8.99886036,11.7472504 7.97663966,11.7472504 C5.87598517,11.7472504 4.17306823,10.0443335 4.17306823,7.94367899 C4.17306823,5.84302449 5.87598517,4.14010756 7.97663966,4.14010756 C8.4824132,4.14010756 8.974946,4.23904354 9.43262271,4.42882477 Z"
          />
        </defs>
        <g stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
          <g transform="translate(-294.000000, -348.000000)">
            <rect x="0" y="0" width="1832" height="2338" />
            <g transform="translate(294.000000, 348.000000)">
              <circle id="Oval" fill={token.colorInfo} cx="8" cy="8" r="8" />
              <mask id="mask-2" fill="white">
                <use xlinkHref="#task-running-path" />
              </mask>
              <use id="Shape" fill="#FFFFFF" xlinkHref="#task-running-path" />
            </g>
          </g>
        </g>
      </svg>
    );

    const FailedSVG = () => (
      <svg width="16px" height="16px" viewBox="0 0 16 16">
        <g stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
          <g transform="translate(-412.000000, -522.000000)">
            <rect x="0" y="0" width="1832" height="2338" />
            <g
              id={formatMessage({
                id: 'ocp-express.Detail.Log.TaskGraph.Group',
                defaultMessage: '编组',
              })}
              transform="translate(412.000000, 522.000000)"
            >
              <circle id="Oval" fill={token.colorError} cx="8" cy="8" r="8" />
              <path
                d="M10.6785714,5.51969382 C10.6785714,5.45157055 10.6229609,5.39583333 10.5549926,5.39583333 L9.53546713,5.4004781 L8,7.23516151 L6.46607761,5.40202636 L5.44500741,5.39738159 C5.37703905,5.39738159 5.32142857,5.45157055 5.32142857,5.52124207 C5.32142857,5.55065894 5.33224172,5.57852755 5.35077855,5.60175139 L7.36047949,8.00154826 L5.35077855,10.3997969 C5.33224172,10.4230207 5.32142857,10.4508893 5.32142857,10.4803062 C5.32142857,10.5484294 5.37703905,10.6041667 5.44500741,10.6041667 L6.46607761,10.5995219 L8,8.76483849 L9.53392239,10.5979736 L10.5534478,10.6026184 C10.6214162,10.6026184 10.6770267,10.5484294 10.6770267,10.4787579 C10.6770267,10.4493411 10.6662135,10.4214725 10.6476767,10.3982486 L8.64106525,8 L10.6507662,5.60020313 C10.669303,5.57852755 10.6785714,5.54911068 10.6785714,5.51969382 Z"
                id="Shape"
                fill="#FFFFFF"
              />
            </g>
          </g>
        </g>
      </svg>
    );

    const PendingSVG = () => (
      <svg width="14px" height="14px" viewBox="0 0 14 14">
        <g stroke="none" strokeWidth="1" fill="none" fillRule="evenodd">
          <g transform="translate(-346.000000, -323.000000)">
            <rect x="0" y="0" width="1832" height="2338" />
            <g id="Oval" transform="translate(345.000000, 322.000000)">
              <circle stroke="#7C8CA3" fill="#FFFFFF" cx="8" cy="8" r="6.5" />
              <circle fill="#7C8CA3" cx="8" cy="8" r="2" />
            </g>
          </g>
        </g>
      </svg>
    );

    const { styles } = useStyles();

    const [canvas, setCanvas] = useState<Canvas>();
    const graphRef = useRef<HTMLDivElement>(null);
    const graphSize = useSize(graphRef);

    // 子任务是否有分叉
    const hasBranch = some(
      taskData?.subtasks || [],
      item => (item.upstreams?.length || 0) > 1 || (item.downstreams?.length || 0) > 1
    );

    // 对任务进行结构化处理，方便绘图
    const nodes = getNodes(taskData);

    // 定位到目标节点
    const setTargetSubtask = (targetSubtaskId?: number) => {
      onSubtaskChange(targetSubtaskId);
      const targetElement = document.getElementById(`ocp-subtask-node-${targetSubtaskId}`);
      if (targetElement) {
        // 这里不能使用原生的 Element.scrollIntoView 方法，这是因为 LogCard 同时也有滚动，会中断 Element.scrollIntoView 的执行
        // 导致目标元素不一定会出现在视图内，因此采用封装 scroll-into-view 实现，不存在中断执行的问题
        scrollIntoView(targetElement, {
          time: 50,
          align: {
            // 目标元素与滚动容器顶部对齐
            top: 0,
          },
        });
      }
    };

    // 定位到当前节点
    const setLatestSubtask = () => {
      const latestNode = getLatestNode(nodes);
      setTargetSubtask(latestNode?.id);
    };

    // 将 setLatestSubtask 函数挂载到 ref 上
    useImperativeHandle(ref, () => ({
      setLatestSubtask: () => {
        setLatestSubtask();
      },
    }));

    useEffect(() => {
      // 如果当前没有选中子任务节点，则自动定位到当前节点
      if (isNullValue(subtask?.id)) {
        setLatestSubtask();
      } else {
        // 否则自动定位到已选中的子任务节点，目的是为了页面刷新时能够自动定位到之前选中的节点
        setTargetSubtask(subtask?.id);
      }
      setTimeout(() => {
        // 绘制 path
        const graphWidth = graphRef.current?.scrollWidth || 0;
        const graphHeight = graphRef.current?.scrollHeight || 0;
        const pathCanvas = new Canvas({
          container: 'ocp-subtask-path',
          width: graphWidth,
          height: graphHeight,
        });

        setCanvas(pathCanvas);
        renderPath(pathCanvas);
      }, 0);
    }, []);

    // 这里采用深对比，只要 taskData 发生变化，就重新绘制任务的路径图
    // 这是因为任务的节点图也是这样的绘制频率，为了路径和节点的绘制结果是对齐的，两者的绘制频率也需要对齐
    useDeepCompareEffect(() => {
      if (canvas) {
        canvas.clear();
        setCanvas(canvas);
        renderPath(canvas);
      }
    }, [taskData]);

    // 自适应宽度
    useUpdateEffect(
      // 使用防抖函数，避免状态改变过于频繁，影响页面交互流畅
      debounce(() => {
        if (canvas) {
          const graphWidth = graphRef.current?.scrollWidth || 0;
          const graphHeight = graphRef.current?.scrollHeight || 0;
          canvas.clear();
          canvas.changeSize(graphWidth, graphHeight);
          renderPath(canvas);
        }
      }, 16),
      // graphSize 实际没有用到，仅为了触发函数组件的执行
      // 如果直接取 graphRef.current 的宽度，由于组件不会重新渲染，导致宽度不变化，监听逻辑不生效
      [graphSize?.width, graphSize?.height]
    );

    // 自定义子任务的状态列表，用于当前任务流程图的渲染
    const subtaskStatusList = [
      {
        label: formatMessage({
          id: 'ocp-express.Detail.Log.TaskGraph.Complete',
          defaultMessage: '完成',
        }),

        value: 'SUCCESSFUL',
        // 节点选中时的 border 颜色
        color: token.colorSuccess,
        // 节点选中时的背景颜色
        backgroundColor: token.colorSuccessBg,
        // 节点状态对应的 icon，完成状态没有使用 antd 内置的 CheckCircleFilled，是因为会被节点 border 贯穿
        icon: <Icon component={SuccessfulSVG} />,
        operations: [
          {
            value: 'downloadLog',
            label: formatMessage({
              id: 'ocp-express.Detail.Log.TaskGraph.DownloadLogs',
              defaultMessage: '下载日志',
            }),
          },
        ],
      },

      {
        label: formatMessage({
          id: 'ocp-express.Detail.Log.TaskGraph.Running',
          defaultMessage: '运行中',
        }),

        value: 'RUNNING',
        color: token.colorInfo,
        backgroundColor: token.colorInfoBg,
        icon: <Icon component={RunningSVG} />,
        operations: [
          {
            value: 'downloadLog',
            label: formatMessage({
              id: 'ocp-express.Detail.Log.TaskGraph.DownloadLogs',
              defaultMessage: '下载日志',
            }),
          },

          {
            value: 'stop',
            label: formatMessage({
              id: 'ocp-express.Detail.Log.TaskGraph.StopRunning',
              defaultMessage: '终止运行',
            }),
          },
        ],
      },

      {
        label: formatMessage({
          id: 'ocp-express.Detail.Log.TaskGraph.Failed',
          defaultMessage: '失败',
        }),
        value: 'FAILED',
        color: token.colorError,
        backgroundColor: token.colorErrorBg,
        // 节点状态对应的 icon，完成状态没有使用 antd 内置的 CloseCircleFilled，是因为会被节点 border 贯穿
        icon: <Icon component={FailedSVG} />,
        operations: [
          {
            value: 'downloadLog',
            label: formatMessage({
              id: 'ocp-express.Detail.Log.TaskGraph.DownloadLogs',
              defaultMessage: '下载日志',
            }),
          },

          {
            value: 'retry',
            label: formatMessage({
              id: 'ocp-express.Detail.Log.TaskGraph.ReRun',
              defaultMessage: '重新运行',
            }),
          },

          {
            value: 'skip',
            label: formatMessage({
              id: 'ocp-express.Detail.Log.TaskGraph.SetToSuccessful',
              defaultMessage: '设置为成功',
            }),
          },
        ],
      },

      {
        label: formatMessage({
          id: 'ocp-express.Detail.Log.TaskGraph.ToBeExecuted',
          defaultMessage: '待执行',
        }),

        value: 'PENDING',
        color: '#7c8ca3',
        backgroundColor: '#fafafa',
        icon: <Icon component={PendingSVG} />,
        operations: [],
      },

      {
        label: formatMessage({
          id: 'ocp-express.Detail.Log.TaskGraph.PrepareForExecution',
          defaultMessage: '准备执行',
        }),

        value: 'READY',
        // 与 PENDING 状态的颜色和图标保持一致
        color: '#7c8ca3',
        backgroundColor: '#fafafa',
        icon: <Icon component={PendingSVG} />,
        operations: [],
      },
    ];

    const renderNode = (node: Node, isSubNode = false) => {
      const statusItem = findByValue(subtaskStatusList, node?.status);
      const subTaskDuration = getTaskDuration(node);
      const subTaskStartTime = formatTime(node?.startTime, TIME_FORMAT);
      // 子任务 ID 为空，表明是空节点，不进行绘制
      return isNullValue(node?.id) ? null : (
        <div
          id={`ocp-subtask-node-${node?.id}`}
          key={node?.id}
          onClick={() => {
            onSubtaskChange(node?.id);
          }}
          className={isSubNode ? styles.subNode : styles.node}
          style={{
            // 设置选中节点的样式
            ...(node?.id === subtask?.id
              ? {
                  border: `1px solid ${statusItem.color}`,
                  borderRight: `4px solid ${statusItem.color}`,
                  backgroundColor: statusItem.backgroundColor,
                  // 节点的右侧 padding 为 16px，需要将选中节点的右侧 padding 减小为 11px，以抵消左右两侧 5px 的 border 影响
                  paddingRight: 11,
                }
              : {}),
          }}
        >
          <div className={styles.icon}>{statusItem.icon}</div>
          <div className={styles.left}>
            <Text
              ellipsis={{
                tooltip: node?.name,
              }}
              className={styles.name}
            >
              {node?.name}
            </Text>
            <div>
              <span className={styles.id}>{`ID: ${node?.id}`}</span>
              <Text
                ellipsis={{
                  tooltip: node?.description,
                }}
                className={styles.description}
              >
                {node?.description}
              </Text>
            </div>
          </div>
          <Space size={4} className={styles.right}>
            <Space size={14}>
              <span style={{ display: 'inline-block', width: 90, fontSize: 12 }}>
                {formatMessage(
                  {
                    id: 'ocp-express.Detail.Log.TaskGraph.StartSubtaskstarttime',
                    defaultMessage: '开始：{subTaskStartTime}',
                  },

                  { subTaskStartTime }
                )}
              </span>
              <Text
                ellipsis={{
                  tooltip: subTaskDuration,
                }}
                style={{ color: token.colorTextTertiary, fontSize: 12 }}
              >
                {formatMessage(
                  {
                    id: 'ocp-express.Detail.Log.TaskGraph.TimeConsumedSubtaskduration',
                    defaultMessage: '耗时：{subTaskDuration}',
                  },

                  { subTaskDuration }
                )}
              </Text>
            </Space>
            <Dropdown
              overlay={
                <Menu
                  style={{ marginBottom: 0, color: 'rgba(0, 0, 0, 0.45)' }}
                  onClick={({ key, domEvent }) => {
                    // 点击菜单时阻止事件冒泡，避免触发整个节点的 click 事件导致被选中
                    if (domEvent) {
                      domEvent.stopPropagation();
                    }
                    // 下载日志
                    if (key === 'downloadLog') {
                      const promise = TaskController.getSubtaskLog({
                        taskInstanceId: taskData?.id,
                        subtaskInstanceId: node?.id,
                      });

                      promise.then(res => {
                        if (res.successful) {
                          const log = res.data?.log;
                          downloadLog(log, `subtask_${node?.id}.log`);
                        }
                      });
                    } else {
                      // 其他操作
                      handleSubtaskOperate(
                        key as SubtaskOperationKey,
                        taskData,
                        node,
                        onOperationSuccess
                      );
                    }
                  }}
                >
                  <div
                    className={styles.taskIdWrapper}
                    style={
                      statusItem.operations?.length > 0 ? { borderBottom: '1px solid #e8e8e8' } : {}
                    }
                  >
                    <Text
                      copyable={{
                        text: `${node?.id}`,
                      }}
                      onClick={e => {
                        // 点击 ID 或者复制 icon 时阻止事件冒泡，避免触发整个节点的 click 事件导致被选中
                        e?.stopPropagation();
                      }}
                    >
                      {`ID: ${node?.id}`}
                    </Text>
                  </div>
                  {statusItem.operations?.map(item => {
                    // 如果属于远程 OCP 发起任务下的子任务，则禁止重试
                    const disabled = taskData?.isRemote && ['retry'].includes(item.value);
                    return (
                      <Menu.Item key={item.value} disabled={disabled}>
                        <Tooltip
                          placement="right"
                          title={
                            disabled &&
                            formatMessage({
                              id: 'ocp-express.Detail.Log.TaskGraph.TheCurrentTaskIsInitiated',
                              defaultMessage: '当前任务为远程 OCP 发起，请到发起端的 OCP 进行操作',
                            })
                          }
                        >
                          <span>{item.label}</span>
                        </Tooltip>
                      </Menu.Item>
                    );
                  })}
                </Menu>
              }
            >
              <MoreOutlined
                style={{
                  fontSize: 18,
                  cursor: 'pointer',
                  // 保证图标垂直对齐
                  marginTop: 4,
                }}
              />
            </Dropdown>
          </Space>
        </div>
      );
    };

    // 采用传入的 canvas 对象进行绘制
    const renderPath = (pathCanvas: Canvas) => {
      nodes.forEach((item, index) => {
        const currentDom = document.getElementById(`ocp-subtask-node-${item.id}`) as HTMLDivElement;
        const currentDomLeftPoint = {
          x: currentDom?.offsetLeft,
          y: currentDom?.offsetTop + 31,
        };

        const children = item.children || [];
        // 下一个节点，可能为空
        const nextNode = nodes[index + 1] as Node;
        const nextDom = document.getElementById(`ocp-subtask-node-${nextNode?.id}`) as
          | HTMLDivElement
          | undefined;
        const nextDomLeftPoint = {
          x: nextDom?.offsetLeft || 0,
          y: (nextDom?.offsetTop || 0) + 31,
        };

        const nextNodeStatusItem = findByValue(subtaskStatusList, nextNode?.status);

        // 分叉
        if (children.length > 0) {
          // 由于 reverse 会修改原数组，因此不对 children 本身进行操作
          [...children]
            // 为了保证顺序靠后的子节点 path 覆盖顺序靠前的子节点 path，需要逆序绘制
            .reverse()
            .forEach(child => {
              const childDom = document.getElementById(
                `ocp-subtask-node-${child?.id}`
              ) as HTMLDivElement;
              const childDomLeftPoint = {
                x: childDom?.offsetLeft,
                y: childDom?.offsetTop + 31,
              };

              const childDomRightPoint = {
                x: childDom?.offsetLeft + childDom?.offsetWidth,
                y: childDom?.offsetTop + 31,
              };

              const childNodeStatusItem = findByValue(subtaskStatusList, child?.status);
              // 分叉: 绘制父节点与子节点之间的连线
              pathCanvas.addShape('path', {
                attrs: {
                  // path 的起始点为父节点的左侧 anchor，这样计算起来比较方便，但需要处理 path 覆盖的问题
                  path: [
                    ['M', currentDomLeftPoint.x, currentDomLeftPoint.y],
                    ['L', currentDomLeftPoint.x, childDomLeftPoint.y - 4],
                    ['a', 4, 4, 0, 0, 0, 4, 4],
                    ['L', childDomLeftPoint.x, childDomLeftPoint.y],
                  ],

                  lineWidth: 2,
                  stroke: childNodeStatusItem.color,
                },
              });

              // 分叉聚合: 绘制子节点与下一节点之间的连线
              if (nextNode) {
                pathCanvas.addShape('path', {
                  attrs: {
                    // path 的起始点为父节点的左侧 anchor，这样计算起来比较方便，但需要处理 path 覆盖的问题
                    path: [
                      ['M', childDomRightPoint.x, childDomRightPoint.y],
                      ['L', childDomRightPoint.x + 24 - 4, childDomRightPoint.y],
                      ['a', 4, 4, 0, 0, 1, 4, 4],
                      ['L', childDomRightPoint.x + 24, nextDomLeftPoint.y - 31 - 12 - 4],
                      ['a', 4, 4, 0, 0, 1, -4, 4],
                      ['L', nextDomLeftPoint.x + 4, nextDomLeftPoint.y - 31 - 12],
                      ['a', 4, 4, 0, 0, 0, -4, 4],
                      ['L', nextDomLeftPoint.x, nextDomLeftPoint.y],
                    ],

                    lineWidth: 2,
                    stroke: nextNodeStatusItem.color,
                  },
                });
              }
            });
        } else if (nextNode) {
          // 未分叉: 绘制当前节点与下一节点之间的连线
          // 此时连线不会相互覆盖，因此顺序绘制即可，不需要处理覆盖的问题
          pathCanvas.addShape('path', {
            attrs: {
              path: [
                ['M', currentDomLeftPoint.x, currentDomLeftPoint.y],
                ['L', nextDomLeftPoint.x, nextDomLeftPoint.y],
              ],

              lineWidth: 2,
              stroke: nextNodeStatusItem.color,
            },
          });
        }
      });
    };

    return (
      <div
        ref={graphRef}
        id="ocp-task-graph"
        className={`${styles.container} ${hasBranch ? styles.containerWithBranch : ''}`}
        style={{
          // 英文环境下，任务详情会占据两行，需要减去额外的 38px
          height: `calc(100vh - 212px - ${isEnglish() ? 38 : 0}px)`,
        }}
      >
        {/* 任务的执行路径图，使用 g-svg 进行绘制 */}
        <div
          id="ocp-subtask-path"
          style={{
            position: 'absolute',
            top: 0,
            // SVG 层关闭事件响应，这样事件就会触达到下一层，以模拟事件穿透的效果
            pointerEvents: 'none',
            // 撑开父元素，且不超过父元素高度
            height: '100%',
          }}
        />

        {/* 任务的节点图，使用 DOM 进行绘制，与路径图进行图层叠加，可模拟出整个任务流程图 */}
        <div id="ocp-subtask-node-container">
          {nodes.map((item, index) => (
            <div key={item.id}>
              {renderNode(item)}
              {item.children?.map(child => renderNode(child, true))}
              {/* 存在分支节点，且下一节点不为空，则留出 20px 高度的空间来绘制 path 路径 */}
              {(item.children?.length as number) > 0 && nodes[index + 1] && (
                <div style={{ height: 20 }} />
              )}
            </div>
          ))}
        </div>
      </div>
    );
  }
);

export default TaskGraph;
