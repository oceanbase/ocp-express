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
import { Dropdown, Menu, Tooltip, Typography, theme } from '@oceanbase/design';
import { find, isEqual, toLower, toNumber } from 'lodash';
import { sortByNumber, findByValue, isNullValue } from '@oceanbase/util';
import type { Graph } from '@antv/g6';
import G6 from '@antv/g6';
import { SUBTASK_STATUS_LIST } from '@/constant/task';
import type { SubtaskOperationKey } from '@/util/task';
import { getNodes, getLatestNode, handleSubtaskOperate } from '@/util/task';
import { fittingString } from '@/util/graph';
import GraphToolbar from '@/component/GraphToolbar';
import styles from './TaskGraph.less';

const { Text } = Typography;

G6.registerNode(
  'subtaskNode',
  {
    drawShape(cfg, group) {
      const defaultNodeWidth = 200;
      let nodeWidth = defaultNodeWidth;
      let offsetX = 0;
      // 任务名
      if (cfg?.name) {
        // 使用 ellipsisName 字段用于超长文本截断
        const ellipsisName = fittingString(cfg?.name, 180, 12);
        const nameShape = group?.addShape('text', {
          attrs: {
            // 使用裁剪后的 name
            text: ellipsisName,
            x: -30,
            y: -5,
            fill: 'rgba(0, 0, 0, 0.85)',
            fontSize: 14,
            fontFamily: 'SFProText-Medium',
          },
        });

        const nameBBox = nameShape?.getBBox();
        nodeWidth = nameBBox.width + 100;
        if (nodeWidth > 280) {
          // 节点宽度最大为 280
          nodeWidth = 280;
        } else if (nodeWidth < 200) {
          // 节点宽度最小为 200
          nodeWidth = 200;
        }
        offsetX = -(nodeWidth - defaultNodeWidth) / 2;
        nameShape?.translate(offsetX, 0);
      }
      const rect = group?.addShape('rect', {
        name: 'rect',
        attrs: {
          x: -nodeWidth / 2,
          y: -34,
          width: nodeWidth,
          height: 62,
          radius: 30,
          fill: '#fff',
          lineWidth: 3,
          shadowColor: '#e1e3e6',
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowOffsetY: 10,
        },
      });

      // 状态图片
      group?.addShape('image', {
        name: 'statusImage',
        attrs: {
          x: -89 + offsetX,
          y: -23,
          width: 42,
          height: 42,
          // 任务状态图标的命名需要与状态名保持一致
          img: `/assets/task/subtask_${toLower(cfg?.status)}.svg`,
        },
      });

      if (cfg?.status === 'RUNNING') {
        // TODO: 图片旋转动画
      } // 任务状态
      group?.addShape('text', {
        name: 'statusName',
        attrs: {
          text: findByValue(SUBTASK_STATUS_LIST, cfg?.status).labelInGraph,
          x: -30 + offsetX,
          y: 15,
          fill: 'rgba(0, 0, 0, 0.45)',
          fontSize: 12,
          fontFamily: 'PingFangSC-Regular',
        },
      });

      const moreGroup = group?.addGroup({
        name: 'moreGroup',
      });

      // ellipsis 图片
      moreGroup?.addShape('image', {
        attrs: {
          // 右边距为 34
          x: nodeWidth / 2 - 34 - 16,
          y: 8,
          width: 16,
          height: 2.5,
          cursor: 'pointer',
          img: '/assets/icon/ellipsis.svg',
        },
      });

      moreGroup?.addShape('rect', {
        attrs: {
          x: nodeWidth / 2 - 34 - 16,
          y: 0,
          width: 16,
          height: 16,
          cursor: 'pointer',
          fill: 'transparent',
        },
      });

      // rect 在节点名称之后渲染，需要对 rect 进行置底操作
      rect?.toBack();
      return rect;
    },
    update(cfg, node) {
      const group = node.get('group');
      const statusImage =
        group.findAllByName('statusImage') && group.findAllByName('statusImage')[0];
      const statusName = group.findAllByName('statusName') && group.findAllByName('statusName')[0];
      // 更新状态图标
      if (statusImage) {
        statusImage.attr('img', `/assets/task/subtask_${toLower(cfg?.status)}.svg`);
      }
      // 更新状态文本
      if (statusName) {
        statusName.attr('text', findByValue(SUBTASK_STATUS_LIST, cfg?.status).labelInGraph);
      }
    },
  },

  'single-node',
);

G6.registerEdge(
  'subtaskEdge',
  {
    curvePosition: [9 / 10, 1 / 10],
    getShapeStyle(cfg) {
      const targetNodeModel = (cfg.targetNode && cfg.targetNode.get('model')) || {};
      const { status } = targetNodeModel;
      const statusItem = findByValue(SUBTASK_STATUS_LIST, status);

      const { startPoint, endPoint } = cfg;
      const controlPoints = this.getControlPoints(cfg);
      let points = [startPoint]; // 添加起始点
      // 添加控制点
      if (controlPoints) {
        points = points.concat(controlPoints);
      }
      // 添加结束点
      points.push(endPoint);
      const path = this.getPath(points, cfg);
      const style = {
        ...G6.Global.defaultEdge.style,
        path,
        // 自定义结束箭头，箭头的边长为 10，夹角为 60 度
        endArrow: {
          path: `M${10 * Math.cos(Math.PI / 6)},${10 * Math.sin(Math.PI / 6)} L0,0 L${
            10 * Math.cos(Math.PI / 6)
          },-${10 * Math.sin(Math.PI / 6)}`,
          fill: statusItem.color,
        },

        lineWidth: 2,
        stroke: statusItem.color,
        ...cfg.style,
      };

      return style;
    },
  },

  'cubic-vertical',
);

export interface TaskGraphProps {
  dispatch: any;
  taskData?: API.TaskInstance;
  subtask?: API.SubtaskInstance;
  onLogAdd: (subtask?: API.SubtaskInstance) => void;
  onSuccess: () => void;
  onRef?: (taskGraph: TaskGraph) => void;
}

// CurrentSubtask 为从 G6 获取的数据模型，由于 G6 会将 id 转换成 string，因此需要定制类型
type CurrentSubtask = Omit<API.SubtaskInstance, 'id'> & {
  id?: string;
};

interface TaskGraphState {
  currentSubtask?: CurrentSubtask;
}

class TaskGraph extends React.PureComponent<TaskGraphProps, TaskGraphState> {
  public main: HTMLElement | null = null;

  public menu: HTMLElement | null = null;

  public graph: Graph | undefined;

  public state = {
    currentSubtask: undefined as CurrentSubtask | undefined,
  };

  public componentDidMount() {
    const { onRef } = this.props;
    this.menu = document.getElementById('menu');
    this.drawGraph();
    if (onRef) {
      onRef(this);
    }
  }

  public componentDidUpdate(prevProps: TaskGraphProps) {
    const { taskData } = this.props;
    if (!isEqual(taskData, prevProps.taskData)) {
      this.drawGraph();
    }
  }

  // 定位到目标节点
  public setTargetSubtask = (targetSubtaskId?: number, showLog = false) => {
    const { taskData, onLogAdd } = this.props;
    const targetSubtask = find(taskData?.subtasks || [], (item) => item.id === targetSubtaskId);
    if (this.graph) {
      this.graph.fitView();
      this.graph.focusItem(`${targetSubtaskId}`);
      this.graph.translate(0, -(this.graph.get('height') || 0) / 2 + 50);
      // 需要强制渲染一次，否则 GraphToolbar 的缩放比例不会更新
      this.forceUpdate();
    }
    if (showLog && onLogAdd && targetSubtask) {
      onLogAdd(targetSubtask);
    }
  };

  // 定位到当前节点
  public setLatestSubtask = () => {
    const { taskData } = this.props;
    const nodes = getNodes(taskData);
    const latestNode = getLatestNode(nodes);
    this.setTargetSubtask(
      latestNode?.id,
      // 当前节点执行失败或正在执行，才默认展示日志
      ['FAILED', 'RUNNING'].includes(latestNode?.status),
    );
  };

  public drawGraph = () => {
    const { taskData: { subtasks = [] } = {}, subtask } = this.props;
    const edges = [];
    subtasks
      // 后端返回的 subtasks 列表顺序会变化，因此前端需要对子任务列表按照 id 大小进行排序，以固定列表顺序
      .sort((a, b) => sortByNumber(a, b, 'id'))
      // 还需要将上下游的子任务列表也进行排序，这样才能保证解析得到的 data 才是固定的
      .map((item) => ({
        ...item,
        // 从大到小排序
        upstreams: (item.upstreams || []).sort((a, b) => a - b),
        downstreams: (item.downstreams || []).sort((a, b) => a - b),
      }))
      .forEach((item) => {
        (item.upstreams || []).forEach((id) => {
          const edge = {
            source: `${id}`,
            target: `${item.id}`,
          };

          const isExisted = find(
            edges,
            (item2) => item2.source === edge.source && item2.target === edge.target,
          );

          if (!isExisted) {
            edges.push(edge);
          }
        });
        (item.downstreams || []).forEach((id) => {
          const edge = {
            source: `${item.id}`,
            target: `${id}`,
          };

          const isExisted = find(
            edges,
            (item2) => item2.source === edge.source && item2.target === edge.target,
          );

          if (!isExisted) {
            edges.push(edge);
          }
        });
      });

    const data = {
      nodes: subtasks.map((item) => ({
        ...item,
        id: `${item.id}`,
      })),

      edges,
    };

    if (this.graph && this.graph.changeData) {
      this.graph.changeData(data);
    } else {
      const splitPane =
        document.getElementsByClassName('SplitPane') &&
        document.getElementsByClassName('SplitPane')[0];
      const width = (this.main && this.main.scrollWidth) || 1200;
      // 高度优先级: 元素本身的高度 => 分隔面板的高度 => 500
      // 还需要减去下方的 log 条的最小高度 32px
      const height = ((this.main && this.main.scrollHeight) || splitPane.scrollHeight || 500) - 32;
      const graph = new G6.Graph({
        container: 'container',
        width,
        height,
        minZoom: 0.2,
        maxZoom: 2,
        layout: {
          type: 'dagre',
          nodesep: 90,
          ranksep: 40,
          controlPoints: false,
        },

        defaultNode: {
          type: 'subtaskNode',
          anchorPoints: [
            [0.5, 0],
            [0.5, 1],
          ],
        },

        defaultEdge: {
          type: 'subtaskEdge',
        },

        nodeStateStyles: {
          hover: {
            lineWidth: 2,
            stroke: theme.token.colorPrimary,
            fill: theme.token.colorPrimaryBg,
          },
        },

        modes: {
          default: ['drag-canvas', 'click-select'],
        },
      });

      const canvas = graph.get('canvas');
      // 关闭局部渲染，避免渲染出现拖影
      canvas.set('localRefresh', false);
      graph.data(data);
      graph.render();

      // 监听 moreGroup 的 click 事件
      canvas.on('moreGroup:click', (e) => {
        const subtaskNode = e.currentTarget && e.currentTarget.getParent();
        const model = subtaskNode && subtaskNode.get('item') && subtaskNode.get('item').getModel();
        this.setState(
          {
            currentSubtask: model,
          },

          () => {
            // 如果 this.menu 为空，则重新获取
            if (!this.menu) {
              this.menu = document.getElementById('menu');
            }
            // 不能和上面的逻辑判断合并，因为并不是二选一，而是前置兜底逻辑
            if (this.menu) {
              this.menu.style.left = `${e.x}px`;
              this.menu.style.top = `${e.y}px`;
            }
          },
        );
      });

      // 监听节点的 mouseleave 事件
      graph.on('node:mouseleave', () => {
        if (!this.menu) {
          this.menu = document.getElementById('menu');
        }
        if (this.menu) {
          this.menu.style.left = '-1500px';
        }
      });

      this.graph = graph;
      // 强制更新一次视图，否则 this.graph 不会更新
      this.forceUpdate();

      // 如果当前没有选中子任务节点，则自动定位到当前节点
      if (isNullValue(subtask?.id)) {
        this.setLatestSubtask();
      } else {
        // 否则自动定位到已选中的子任务节点，目的是为了页面刷新时能够自动定位到之前选中的节点
        this.setTargetSubtask(subtask?.id, true);
      }
    }
  };

  public handleMenuClick = (key: 'viewLog' | SubtaskOperationKey) => {
    const { taskData, onSuccess } = this.props;
    const { currentSubtask } = this.state;
    if (key === 'viewLog') {
      this.viewLog();
    } else {
      handleSubtaskOperate(key, taskData, currentSubtask, () => {
        this.setState({
          currentSubtask: undefined,
        });

        if (onSuccess) {
          onSuccess();
        }
      });
    }
    if (this.menu) {
      this.menu.style.left = '-1500px';
    }
  };

  public viewLog = () => {
    const { onLogAdd } = this.props;
    const { currentSubtask } = this.state;
    if (onLogAdd) {
      onLogAdd({
        // currentSubtask 为从 G6 获取的数据模型，由于 G6 会将 id 转换成 string，因此需要转换回来
        ...currentSubtask,
        id: toNumber(currentSubtask?.id),
      });
    }
  };

  public render() {
    const { taskData } = this.props;
    const { currentSubtask } = this.state;
    const operations = [
      {
        label: formatMessage({
          id: 'ocp-express.Task.Detail.TaskGraph.TaskId',
          defaultMessage: '任务 ID',
        }),

        value: 'taskId',
      },

      ...(findByValue(SUBTASK_STATUS_LIST, currentSubtask?.status).operations || []),
    ];

    const menu = (
      <Menu
        id="menu"
        onClick={({ key }) => {
          this.handleMenuClick(key as 'viewLog' | SubtaskOperationKey);
        }}
        style={{ left: -1500 }}
      >
        {operations.map((item) => {
          const disabled = taskData?.isRemote && ['retry'].includes(item.value);
          return item.value === 'taskId' ? (
            <div
              className={styles.taskIdWrapper}
              // 下拉菜单数 > 1 时才有下边框
              style={operations.length > 1 ? { borderBottom: '1px solid #e8e8e8' } : {}}
            >
              <Text
                copyable={{
                  text: `${currentSubtask?.id}`,
                }}
              >
                {`ID: ${currentSubtask?.id}`}
              </Text>
            </div>
          ) : (
            <Menu.Item key={item.value} disabled={disabled}>
              <Tooltip
                placement="right"
                // 如果属于远程 OCP 发起任务下的子任务，则禁止重试
                title={
                  disabled &&
                  formatMessage({
                    id: 'ocp-express.Detail.Flow.TaskGraph.TheCurrentTaskIsInitiated',
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
    );

    return (
      <div>
        {this.graph && <GraphToolbar mode="fixed" graph={this.graph} />}
        <Dropdown visible={true} overlay={menu} overlayClassName={styles.menu}>
          <span />
        </Dropdown>
        <div
          id="container"
          ref={(node) => {
            this.main = node;
          }}
        />
      </div>
    );
  }
}

export default TaskGraph;
