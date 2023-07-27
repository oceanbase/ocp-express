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
import { Badge, Space, Modal, message } from '@oceanbase/design';
import React from 'react';
import { flatten, isEqual, toLower } from 'lodash';
import { directTo, findByValue } from '@oceanbase/util';
import modifyCSS from '@antv/dom-util/lib/modify-css';
import G6 from '@antv/g6';
import {
  OB_CLUSTER_STATUS_LIST,
  OB_SERVER_STATUS_LIST,
  ZONE_STATUS_LIST,
} from '@/constant/oceanbase';
import { fittingString } from '@/util/graph';
import FullScreen from '@/component/FullScreen';
import ModifyPasswordModal from '../../../ModifyPasswordModal';
import styles from './index.less';

G6.registerNode(
  'clusterTopoNode',
  {
    drawShape(cfg, group) {
      const { nodeType, collapsed } = cfg;
      const x = -101;
      const y = -34;
      const width = 250;
      const height = 68;
      // 根节点处渲染一个空 group
      if (cfg.nodeType === 'root') {
        return group;
      }
      const keyShape = group.addShape('rect', {
        attrs: {
          x,
          y,
          width,
          height,
          radius: 4,
          fill: '#fff',
          lineWidth: 3,
          /* 同样的 shadow 参数在 Canvas 和 SVG 渲染下效果不同，因此需要分别设置 */
          // Canvas 渲染
          shadowOffsetX: 5,
          shadowOffsetY: 10,
          shadowColor: 'rgba(0,0,0,0.12)',
          shadowBlur: 30,
          // SVG 渲染: 暂不使用，可能导致节点丢失背景色
          // shadowOffsetX: 5,
          // shadowOffsetY: 5,
          // shadowColor: 'rgba(0,0,0,0.1)',
          // shadowBlur: 50,
        },
      });

      group.addShape('image', {
        attrs: {
          text: cfg.name || cfg.clusterName,
          x: -89,
          y: -55,
          width: 72,
          height: 72,
          img: `/assets/${cfg.nodeType}/${toLower(
            (cfg.status === 'DISCONNECTING' ? 'running' : cfg.status) || 'stopped'
          )}.svg`,
        },
      });

      const maxWidthMap = {
        cluster: 110,
        // zone 的名称大多数是英文字母，单个字符的宽度要大于数字，因此最大宽度设小一点
        zone: 120,
        // server 展示的是数字 + 点号的字符串，单个字符的宽度较小
        server: 156,
      };

      // 使用 ellipsisName 字段用于超长文本截断
      const ellipsisName =
        cfg.name || cfg.clusterName
          ? fittingString(cfg.name || cfg.clusterName, maxWidthMap[nodeType] || 120, 14)
          : '-';
      // 添加节点名称
      group.addShape('text', {
        attrs: {
          // 集群节点展示集群 ID，而不是集群名
          text: ellipsisName,
          x: 0,
          y: -8,
          fill: 'rgba(0, 0, 0, 0.85)',
          fontSize: 14,
          fontFamily: 'SFProText-Medium',
          textAlign: 'left',
          textBaseline: 'middle',
        },
      });
      if (
        // 本地 OCP 下的集群节点或者有可操作项的其他节点，展示下拉菜单入口
        nodeType === 'cluster' ||
        // zone 和 server 节点的下拉菜单，需要更新集群的操作权限
        cfg.status === 'RUNNING' ||
        cfg.status === 'STOPPED' ||
        cfg.status === 'UNAVAILABLE' ||
        cfg.status === 'PROCESS_STOPPED' ||
        cfg.status === 'SERVICE_STOPPED'
      ) {
        const moreGroup = group.addGroup({
          name: 'moreGroup',
        });

        // more 图片
        // moreGroup.addShape('image', {
        //   attrs: {
        //     x: 130,
        //     y: -16,
        //     width: 2.5,
        //     height: 16,
        //     cursor: 'pointer',
        //     img: '/assets/icon/more.svg',
        //   },
        // });

        moreGroup.addShape('rect', {
          attrs: {
            x: 122,
            y: -16,
            width: 16,
            height: 16,
            cursor: 'pointer',
            fill: 'transparent',
          },
        });
      }
      const statusGroup = group.addGroup({
        name: 'statusGroup',
      });

      const statusListMap = {
        cluster: OB_CLUSTER_STATUS_LIST,
        zone: ZONE_STATUS_LIST,
        server: OB_SERVER_STATUS_LIST,
      };

      const statusItem = findByValue(statusListMap[cfg.nodeType], cfg.status);
      statusGroup.addShape({
        type: 'image',
        attrs: {
          x: 0,
          y: 8,
          width: 14,
          height: 14,
          img: `/assets/badge/${statusItem.badgeStatus || 'default'}.svg`,
        },
      });

      statusGroup.addShape({
        type: 'text',
        attrs: {
          x: 18,
          y: 14,
          text:
            statusItem.label ||
            formatMessage({
              id: 'ocp-express.Topo.TopoGraph.UnknownStatus',
              defaultMessage: '状态未知',
            }),

          fill: '#b0b0b0',
          fontSize: 12,
          textAlign: 'left',
          textBaseline: 'middle',
        },
      });

      const edgeStartPoint = {
        x: x + width / 2,
        y: y + height,
      };

      const collapsePoint = {
        x: edgeStartPoint.x,
        y: edgeStartPoint.y + 24,
      };

      // 只有 zone 节点支持展开/收缩
      if (nodeType === 'zone') {
        const collapseGroup = group.addGroup({
          name: 'collapseGroup',
        });

        collapseGroup.addShape({
          type: 'path',
          attrs: {
            path: [
              ['M', edgeStartPoint.x, edgeStartPoint.y],
              ['L', collapsePoint.x, collapsePoint.y],
            ],

            stroke: '#c5cbd4',
            lineWidth: 1.5,
          },
        });

        collapseGroup.addShape({
          type: 'marker',
          attrs: {
            symbol: 'circle',
            x: collapsePoint.x,
            y: collapsePoint.y,
            r: 8,
            fill: '#8f9aac',
            cursor: 'pointer',
          },
        });

        collapseGroup.addShape({
          type: 'path',
          attrs: {
            path: [
              ['M', collapsePoint.x - 6, collapsePoint.y],
              ['L', collapsePoint.x + 6, collapsePoint.y],
            ],

            lineJoin: 'round',
            stroke: '#ffffff',
            cursor: 'pointer',
          },
        });

        collapseGroup.addShape({
          type: 'path',
          name: 'collapse-circle-verticle-line',
          attrs: {
            path: [
              ['M', collapsePoint.x, collapsePoint.y - 6],
              ['L', collapsePoint.x, collapsePoint.y + 6],
            ],

            lineJoin: 'round',
            stroke: collapsed ? '#ffffff' : null,
            cursor: 'pointer',
          },
        });
      }
      return keyShape;
    },
  },

  'single-node'
);

G6.registerEdge(
  'clusterTopoEdge',
  {
    drawShape(cfg, group) {
      const { startPoint, endPoint } = cfg;
      const sourceNodeModel = (cfg && cfg.sourceNode && cfg.sourceNode.get('model')) || {};
      const keyShape = group.addShape('path', {
        attrs: this.getShapeStyle(cfg),
      });

      if (sourceNodeModel.nodeType === 'root') {
        const childrenLength = (sourceNodeModel.children && sourceNodeModel.children.length) || 0;
        // root 节点处会有多余的连线，需要用一条和背景色相同的折线进行覆盖，以模拟去掉多余连线的效果
        group.addShape('path', {
          attrs: {
            // 与背景色相同
            stroke: '#fff',
            // 线宽需要大于连线的宽度 1.5，才能完全覆盖
            lineWidth: 2,
            path:
              childrenLength > 1
                ? // 多集群 (主备集群) 只需要将 root 节点凸出的连线覆盖掉即可
                [
                  ['M', startPoint.x, startPoint.y],
                  ['L', startPoint.x, (startPoint.y + endPoint.y) / 2],
                ]
                : // 单集群需要将 root 节点的连线全部覆盖掉
                [
                  ['M', startPoint.x, startPoint.y],
                  ['L', startPoint.x, (startPoint.y + endPoint.y) / 2],
                  ['L', endPoint.x, (startPoint.y + endPoint.y) / 2],
                  ['L', endPoint.x, endPoint.y],
                ],
          },
        });
      }
      // 由于自定义了边的 drawShape 逻辑，为了使 edge 在布局变化时仍能正常更新，需要返回对应边的 keyShape，而不是整个 group
      return keyShape;
    },
    getPath(points) {
      const startPoint = points[0];
      const endPoint = points[1];
      // TODO: 根据不同条件返回不同的 path 路径实际不生效，可能是 G6 的问题，具体原因待排查
      return [
        ['M', startPoint.x, startPoint.y],
        ['L', startPoint.x, (startPoint.y + endPoint.y) / 2],
        ['L', endPoint.x, (startPoint.y + endPoint.y) / 2],
        ['L', endPoint.x, endPoint.y],
      ];
    },
    getShapeStyle(cfg) {
      const sourceNodeModel = (cfg.sourceNode && cfg.sourceNode.get('model')) || {};
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
      const childrenLength = (sourceNodeModel.children && sourceNodeModel.children.length) || 0;
      let stroke = '#c5cbd4';
      if (sourceNodeModel.nodeType === 'root' && childrenLength === 1) {
        // 单集群拓扑图，虽然 root 节点多余的连线最终会被覆盖掉，但在布局动画的过程中，多余的连线仍然会展示，因此需要设置其连线颜色为背景色，以模拟去掉多余连线的效果
        stroke = '#f0f2f5';
      }
      const style = {
        ...G6.Global.defaultEdge.style,
        stroke,
        lineWidth: 1.5,
        path,
        // 根节点处的连线为虚线
        lineDash: sourceNodeModel.nodeType === 'root' ? [5, 5] : [],
        ...cfg.style,
      };

      return style;
    },
  },

  'polyline'
);

export interface TopoGraphProps {
  dispatch: any;
  clusterData: API.ClusterInfo;
}

class TopoGraph extends React.PureComponent<TopoGraphProps> {
  public main: HTMLDivElement | null = null;

  public graph: any;

  public state = {
    currentNode: null,
    modifyPasswordVisible: false,
    serverVisible: false,
    serverOperation: null, // server 操作对象，形如 { value: 'start', label: '启动' }
  };

  public componentDidMount() {
    this.menu = document.getElementById('menu');
    this.drawGraph();
    window.addEventListener('resize', () => {
      this.updateGraphSize();
    });
  }

  public componentDidUpdate(prevProps: TopoGraphProps) {
    const { clusterData } = this.props;
    if (!isEqual(clusterData, prevProps.clusterData)) {
      this.drawGraph();
    }
  }

  // eslint-disable-next-line
  public getClusterByNode(nodeData): API.ClusterInfo | API.Cluster {
    return {
      ...(nodeData || {}),
    };
  }

  // eslint-disable-next-line
  public getServerByNode(nodeData) {
    return {
      ...(nodeData || {}),
      // 将 serverId 重新赋值给 server id
      id: nodeData && nodeData.serverId,
    };
  }

  public updateGraphSize = () => {
    if (this.graph) {
      // 这里需要使用 clientWidth 和 clientHeight，而不是 scrollWidth 和 scrollHeight
      // 是因为拓扑图本身不存在滚动，否则全屏切换后宽度不会变化
      const width = (this.main && this.main.clientWidth) || 1200;
      const height = (this.main && this.main.clientHeight) || 700;
      this.graph.changeSize(width, height);
    }
  };

  public handleMenuClick = ({ key }) => {
    const { dispatch, clusterData } = this.props;
    const { currentNode } = this.state;
    if (currentNode && currentNode.nodeType === 'cluster') {
      if (key === 'addTenant') {
        directTo(`/tenant/new`);
      } else if (key === 'addZone') {
        this.setState({
          addZoneVisible: true,
        });
      } else if (key === 'addServer') {
        this.setState({
          addServerVisible: true,
        });
      } else if (key === 'switchover') {
        this.setState({
          switchoverVisible: true,
        });
      } else if (key === 'failover') {
        this.setState({
          failoverVisible: true,
        });
      } else if (key === 'disconnect') {
        this.setState({
          disconnectVisible: true,
        });
      } else if (key === 'modifyPassword') {
        this.setState({
          modifyPasswordVisible: true,
        });
      } else if (key === 'start') {
        Modal.confirm({
          title: formatMessage(
            {
              id: 'ocp-express.Topo.TopoGraph.AreYouSureYouWant.StartCluster',
              defaultMessage: '确定要启动 OB 集群 {currentNodeName} 吗？',
            },

            { currentNodeName: currentNode.clusterName }
          ),

          okText: formatMessage({ id: 'ocp-express.Topo.TopoGraph.Start', defaultMessage: '启动' }),
          onOk: () => {
            dispatch({
              type: 'cluster/startCluster',
              payload: {
                id: currentNode?.obClusterId,
              },
            });
          },
        });
      } else if (key === 'restart') {
        this.setState({
          clusterRestartVisible: true,
        });
      } else if (key === 'delete') {
        this.setState({
          clusterDeleteVisible: true,
        });
      } else if (key === 'stop') {
        Modal.confirm({
          title: formatMessage(
            {
              id: 'ocp-express.Topo.TopoGraph.AreYouSureYouWant.StopCluster',
              defaultMessage: '确定要停止 OB 集群 {currentNodeName} 吗？',
            },

            { currentNodeName: currentNode.clusterName }
          ),

          content: formatMessage({
            id: 'ocp-express.Topo.TopoGraph.StoppingTheClusterWillCause',
            defaultMessage: '停止集群会导致集群中所有的服务被终止，请谨慎操作',
          }),

          okText: formatMessage({ id: 'ocp-express.Topo.TopoGraph.Stop', defaultMessage: '停止' }),
          okButtonProps: {
            danger: true,
            ghost: true,
          },

          onOk: () => {
            dispatch({
              type: 'cluster/stopCluster',
              payload: {
                id: currentNode?.obClusterId,
              },
            });
          },
        });
      } else if (key === 'openAutoDeadlockDetection') {
        Modal.confirm({
          title: formatMessage({
            id: 'ocp-express.Topo.TopoGraph.AreYouSureYouWant.EnableDeadlockDetection',
            defaultMessage: '确定要开启死锁自动检测吗？',
          }),

          content: formatMessage({
            id: 'ocp-express.Topo.TopoGraph.TheSystemAutomaticallyDetectsAnd',
            defaultMessage:
              '将会自动检测并解决死锁，并保存 7 天的死锁记录。以上操作需要消耗 2% 左右的性能，请谨慎操作',
          }),

          onOk: () => {
            dispatch({
              type: 'cluster/deadlockDetectionSwitcher',
              payload: {
                id: clusterData?.obClusterId,
                enabled: true,
              },

              onSuccess: () => {
                message.success(
                  formatMessage({
                    id: 'ocp-express.Topo.TopoGraph.AutomaticDeadlockDetectionEnabled',
                    defaultMessage: '死锁自动检测已开启',
                  })
                );
              },
            });
          },
        });
      } else if (key === 'closeAutoDeadlockDetection') {
        Modal.confirm({
          title: formatMessage({
            id: 'ocp-express.Topo.TopoGraph.AreYouSureYouWant.DisableDeadlockDetection',
            defaultMessage: '确定要关闭死锁自动检测吗？',
          }),

          content: formatMessage({
            id: 'ocp-express.Topo.TopoGraph.AfterTheAutomaticDetectionOf',
            defaultMessage: '关闭死锁自动检测后，不再自动检测和解决死锁',
          }),

          onOk: () => {
            dispatch({
              type: 'cluster/deadlockDetectionSwitcher',
              payload: {
                id: clusterData?.obClusterId,
                enabled: false,
              },

              onSuccess: () => {
                message.success(
                  formatMessage({
                    id: 'ocp-express.Detail.Overview.AutomaticDeadlockDetectionDisabled',
                    defaultMessage: '死锁自动检测已关闭',
                  })
                );
              },
            });
          },
        });
      } else if (key === 'downloadLog') {
        const hostIds = flatten((clusterData.zones || []).map(item => item.servers || [])).map(
          item => item.hostId
        );
        directTo(
          `/log/query?hostIds=${hostIds.toString()}&defaultOCPType=CLUSTER&id=${currentNode?.obClusterId
          }`
        );
      }
    } else if (currentNode && currentNode.nodeType === 'zone') {
      if (key === 'addServer') {
        this.setState({
          addServerVisibleForZone: true,
        });
      }
    } else if (currentNode && currentNode.nodeType === 'server') {
      const operations = findByValue(OB_SERVER_STATUS_LIST, currentNode.status).operations || [];
      const operation = findByValue(operations, key);
      this.setState({
        serverVisible: true,
        serverOperation: operation,
      });
    }
    // if (this.menu) {
    //   this.menu.style.left = '-1500px';
    // }
  };

  public drawGraph() {
    const { clusterData } = this.props;

    // 主集群: 只有一个
    const primaryCluster = clusterData;
    const clusterList = [primaryCluster];
    const data = {
      id: 'root',
      collapsed: false,
      // 根节点
      nodeType: 'root',
      children: clusterList
        .filter(cluster => cluster)
        .map(cluster => {
          const serverCount = flatten(cluster?.zones?.map(zone => zone.servers || [])).length;
          return {
            ...cluster,
            id: cluster?.obClusterId,
            // 集群本身就有 type 字段，会覆盖掉节点类型，需要手动设置节点类型
            type: 'clusterTopoNode',
            // 只有当前查看集群才会默认展开
            // 多集群模式下，远程 OCP 下 OB 集群的 clusterId 可能相同，因此需要对 ocpClusterId 进行判断
            collapsed: cluster?.obClusterId !== clusterData?.obClusterId,
            nodeType: 'cluster',
            children: (cluster?.zones || []).map((zone, zoneIndex) => ({
              ...zone,
              id: zone.name,
              nodeType: 'zone',
              // 对于当前的 OB 集群:
              // OBServer 节点数 > 9 时，仅默认展开第一个 zone 节点
              // OBServer 节点数 <= 9 时，默认展开所有的 zone 节点
              collapsed: serverCount > 9 ? zoneIndex !== 0 : false,
              children: (zone.servers || []).map(server => ({
                ...server,
                // G6 绘图要求: 节点 id 不能相同，为了避免与集群节点 id 重复，需要加上 server 前缀
                id: `server_${server.id}`,
                // 使用 serverId 作为实际的 id
                serverId: server.id,
                name: server.ip,
                nodeType: 'server',
              })),
            })),
          };
        }),
    };

    if (this.graph && this.graph.changeData) {
      this.graph.changeData(data);
      this.graph.fitView();
    } else {
      const width = (this.main && this.main.clientWidth) || 1200;
      const height = (this.main && this.main.clientHeight) || 700;
      const self = this;
      const graph = new G6.TreeGraph({
        container: 'container',
        width,
        height,
        minZoom: 0.2,
        maxZoom: 2,
        linkCenter: true,
        // Canvas 渲染
        fitViewPadding: [
          // 主备集群上移 50px (因为存在 root 节点和连线，虽然被覆盖不展示，但也会占据空间)
          // 单集群拓扑图上移 150px
          data.children.length > 1 ? -50 : -150,
          24,
          24,
          24,
        ],

        // SVG 渲染: 暂不使用，可能导致节点丢失背景色
        // 使用 SVG 渲染，以便文本可被选中复制
        // renderer: 'svg',
        // fitViewPadding: [
        //   // 主备集群上移 50px (因为存在 root 节点和连线，虽然被覆盖不展示，但也会占据空间)
        //   // 单集群拓扑图上移 150px
        //   data.children.length > 1 ? -50 : -150,
        //   0,
        //   0,
        //   0,
        //   0,
        // ],
        modes: {
          default: [
            'drag-canvas',
            {
              type: 'tooltip',
              shouldBegin(e) {
                const model = (e.item && e.item.get('model')) || {};
                // 非集群节点才展示 tooltip
                return model.nodeType !== 'cluster';
              },
              shouldUpdate() {
                // 下拉菜单为空或者下拉菜单的 style.left='-1500px' 时才展示 tooltip
                return (
                  !self.menu || (self.menu && self.menu.style && self.menu.style.left === '-1500px')
                );
              },
              formatText(model) {
                const sessionCountLabel = formatMessage({
                  id: 'ocp-express.Topo.TopoGraph.Connections',
                  defaultMessage: '连接数',
                });

                const portInfo =
                  model.nodeType === 'server'
                    ? formatMessage(
                      {
                        id: 'ocp-express.Topo.TopoGraph.LtLiGtPortModelPort',
                        defaultMessage: '端口：{modelPort}',
                      },

                      { modelPort: model.port }
                    )
                    : '';
                return `
                <ul>
                  <li>QPS: ${(model.performanceStats && model.performanceStats.qps) || 0}</li>
                  <li>${sessionCountLabel}: ${(model.performanceStats && model.performanceStats.active_session) || 0
                  }</li>
                    <li>Unit: ${(model.performanceStats && model.performanceStats.unit_num) || 0
                  }</li>
                    ${portInfo ? `<li>${portInfo}</li>` : ''}
                </ul>`;
              },
              // 不能使用箭头函数，否则无法调用 G6 的 behavior 对象上的方法和属性
              updatePosition(e) {
                // 现在锁定了 G6 的版本，但之后如果升级到 G6 的最新版后需要使用下面的写法
                const OFFSET = 30;
                const x = e.canvasX;
                const y = e.canvasY;
                // const OFFSET = 6;
                // const x = e.clientX;
                // const y = e.clientY;
                const left = `${x + OFFSET}px`;
                const top = `${y + OFFSET}px`;
                self.g6TooltipContainer = this.container;
                // 这里的 this 指当前 G6 的 behavior 对象
                modifyCSS(this.container, { left, top, visibility: 'visible' });
              },
            },
          ],
        },

        defaultNode: {
          type: 'clusterTopoNode',
          anchorPoints: [
            [0.5, 0],
            [0.5, 1],
          ],
        },

        defaultEdge: {
          type: 'clusterTopoEdge',
        },

        nodeStateStyles: {
          'collapseStatus:collapsed': {
            'collapse-circle-verticle-line': {
              stroke: '#ffffff',
            },
          },

          'collapseStatus:expanded': {
            'collapse-circle-verticle-line': {
              stroke: null,
            },
          },
        },

        layout: {
          type: 'compactBox',
          direction: 'TB',
          getId: function getId(d) {
            return d.id;
          },
          getHeight: function getHeight() {
            return 16;
          },
          getWidth: function getWidth() {
            return 16;
          },
          // 节点的水平间隙
          getHGap: function getHGap() {
            return 150;
          },
          // 节点的垂直间隙
          getVGap: function getVGap() {
            return 100;
          },
        },
      });

      const canvas = graph.get('canvas');
      canvas.set('localRefresh', false);
      graph.data(data);
      graph.render();
      graph.fitView();

      // 监听 moreGroup 的 click 事件
      canvas.on('moreGroup:click', e => {
        // 阻止事件冒泡，否则会触发节点的 click 事件，下拉菜单就无法弹出了
        e.stopPropagation();
        const node = e.currentTarget && e.currentTarget.getParent();
        const model = (node && node.get('item') && node.get('item').getModel()) || {};
        // 隐藏 G6 tooltip
        if (this.g6TooltipContainer) {
          modifyCSS(this.g6TooltipContainer, {
            visibility: 'hidden',
          });
        }
        this.setState(
          {
            currentNode: model,
          }

          // () => {
          //   if (!this.menu) {
          //     this.menu = document.getElementById('menu');
          //   }
          //   this.menu.style.left = `${e.x}px`;
          //   // 由于高度会变化，因此需要使用最新的高度值
          //   this.menu.style.top = `${e.y}px`;
          // }
        );
      });

      // 监听节点的 mouseleave 事件
      // graph.on('node:mouseleave', () => {
      //   if (!this.menu) {
      //     this.menu = document.getElementById('menu');
      //   }
      //   this.menu.style.left = '-1500px';
      //   // 重置 currentNode，否则会出现下拉菜单的定位问题
      //   this.setState({
      //     currentNode: null,
      //   });
      // });

      // 监听节点的 click 事件
      // graph.on('node:click', () => {
      //   if (!this.menu) {
      //     this.menu = document.getElementById('menu');
      //   }

      //   this.menu.style.left = '-1500px';
      // });

      canvas.on('collapseGroup:click', e => {
        const node = e.currentTarget && e.currentTarget.getParent();
        const item = node && node.get('item');
        const model = (item && item.getModel()) || {};
        model.collapsed = !model.collapsed;
        // 修改 collapsed 属性后，需要手动触发布局才会生效
        graph.layout();
        graph.setItemState(item, 'collapseStatus', model.collapsed ? 'collapsed' : 'expanded');
      });

      this.graph = graph;
      // 强制更新一次视图，否则 this.graph 不会更新
      this.forceUpdate();
    }
  }

  public render() {
    const { dispatch, clusterData } = this.props;
    const { currentNode, modifyPasswordVisible } = this.state;
    // 当前访问集群的状态
    const statusItem = findByValue(OB_CLUSTER_STATUS_LIST, clusterData.status);

    let menus = [];
    if (currentNode) {
      if (currentNode?.nodeType === 'cluster') {
        if (currentNode?.obClusterId === clusterData?.obClusterId) {
          if (currentNode?.status === 'RUNNING') {
            menus = [
              ...menus,
              {
                label: formatMessage({
                  id: 'ocp-express.Topo.TopoGraph.CreateATenant',
                  defaultMessage: '新建租户',
                }),

                value: 'addTenant',
                tooltipTitle: formatMessage({
                  id: 'ocp-express.Topo.TopoGraph.TheTenantCannotBeCreated',
                  defaultMessage: '备集群下无法新建租户',
                }),
              },

              {
                label: formatMessage({
                  id: 'ocp-express.Topo.TopoGraph.NewZone',
                  defaultMessage: '新增 Zone',
                }),

                value: 'addZone',
              },

              {
                label: formatMessage({
                  id: 'ocp-express.Topo.TopoGraph.AddObserver',
                  defaultMessage: '添加 OBServer',
                }),

                value: 'addServer',
              },

              {
                isDivider: true,
              },
            ];
          }
        }
        // 当前查看集群
        if (currentNode?.obClusterId === clusterData?.obClusterId) {
          if (currentNode.status === 'RUNNING') {
            menus = [
              ...menus,
              {
                label: formatMessage({
                  id: 'ocp-express.Topo.TopoGraph.UpgradedVersion',
                  defaultMessage: '升级版本',
                }),

                value: 'upgrade',
              },

              {
                label: formatMessage({
                  id: 'ocp-express.Topo.TopoGraph.ChangePassword',
                  defaultMessage: '修改密码',
                }),

                value: 'modifyPassword',
              },

              {
                isDivider: true,
              },

              {
                label: formatMessage({
                  id: 'ocp-express.Topo.TopoGraph.RestartTheCluster',
                  defaultMessage: '重启集群',
                }),

                value: 'restart',
              },

              {
                label: formatMessage({
                  id: 'ocp-express.Topo.TopoGraph.StopTheCluster',
                  defaultMessage: '停止集群',
                }),

                value: 'stop',
              },

              {
                label: formatMessage({
                  id: 'ocp-express.Topo.TopoGraph.DeleteTheCluster',
                  defaultMessage: '删除该集群',
                }),

                value: 'delete',
              },
            ];
          } else if (currentNode.status === 'STOPPED') {
            menus = [
              ...menus,
              {
                label: formatMessage({
                  id: 'ocp-express.Topo.TopoGraph.UpgradedVersion',
                  defaultMessage: '升级版本',
                }),

                value: 'upgrade',
              },

              {
                isDivider: true,
              },

              {
                label: formatMessage({
                  id: 'ocp-express.Topo.TopoGraph.StartTheCluster',
                  defaultMessage: '启动集群',
                }),

                value: 'start',
              },

              {
                label: formatMessage({
                  id: 'ocp-express.Topo.TopoGraph.DeleteACluster',
                  defaultMessage: '删除集群',
                }),

                value: 'delete',
              },
            ];
          } else if (currentNode.status === 'UNAVAILABLE') {
            menus = [
              ...menus,
              {
                label: formatMessage({
                  id: 'ocp-express.Topo.TopoGraph.RestartTheCluster',
                  defaultMessage: '重启集群',
                }),

                value: 'restart',
              },

              {
                label: formatMessage({
                  id: 'ocp-express.Topo.TopoGraph.StopTheCluster',
                  defaultMessage: '停止集群',
                }),

                value: 'stop',
              },

              {
                label: formatMessage({
                  id: 'ocp-express.Topo.TopoGraph.DeleteTheCluster',
                  defaultMessage: '删除该集群',
                }),

                value: 'delete',
              },
            ];
          } else if (
            currentNode.status === 'ABANDONED' ||
            // 容灾切换的任务失败回滚后，备集群的状态仍是容灾切换中，应该允许删除，以便其他备集群能进行容灾切换
            currentNode.status === 'FAILOVER'
          ) {
            menus = [
              ...menus,
              {
                label: formatMessage({
                  id: 'ocp-express.Topo.TopoGraph.DeleteTheCluster',
                  defaultMessage: '删除该集群',
                }),

                value: 'delete',
              },
            ];
          }
          if (
            // 由于开启/关闭死锁检测要访问内核，因此需要保证内核可访问
            currentNode.status === 'RUNNING'
          ) {
            if (currentNode?.deadLockDetectionEnabled) {
              menus = [
                ...menus,
                {
                  label: formatMessage({
                    id: 'ocp-express.Topo.TopoGraph.DisableAutomaticDeadlockDetection',
                    defaultMessage: '关闭死锁自动检测',
                  }),

                  value: 'closeAutoDeadlockDetection',
                },
              ];
            } else {
              menus = [
                ...menus,
                {
                  label: formatMessage({
                    id: 'ocp-express.Topo.TopoGraph.EnableAutomaticDeadlockDetection',
                    defaultMessage: '开启死锁自动检测',
                  }),

                  value: 'openAutoDeadlockDetection',
                },
              ];
            }
          }
          menus = [
            ...menus,
            {
              label: formatMessage({
                id: 'ocp-express.Detail.Overview.DownloadLogs',
                defaultMessage: '下载日志',
              }),
              value: 'downloadLog',
            },
          ];
        }
      } else if (currentNode.nodeType === 'zone') {
        // 只读集群支持 停止，重启/启动
        const zoneStatusOperations =
          findByValue(ZONE_STATUS_LIST, currentNode.status).operations || [];
        menus = zoneStatusOperations.map(item => ({
          ...item,
          disabled: item.value !== 'restart' && item.value !== 'stop' && item.value !== 'start',
        }));
      } else if (currentNode.nodeType === 'server') {
        const serverStatusOperations =
          findByValue(OB_SERVER_STATUS_LIST, currentNode.status).operations || [];
        // 只读集群 支持 停止进程，停止服务，重启，下载日志
        menus = serverStatusOperations.map(item => ({
          ...item,
          disabled:
            item.value !== 'restart' &&
            item.value !== 'stopProcess' &&
            item.value !== 'stopService',
        }));
      }
    }

    // 先过滤出可用操作，如果无可用操作再给menu添加一个【无可用操作】提示占空，避免 Dropdown 展开为一个空条
    menus = menus.filter((item, index) => {
      // 如果第一个和最后一个菜单项是 divider，则直接去掉，不需要展示出来
      if (item.isDivider && (index === 0 || index === menus.length - 1)) {
        return false;
      }
      return true;
    });

    if (menus.length === 0) {
      menus = [
        {
          label: formatMessage({
            id: 'ocp-express.Topo.TopoGraph.NoAvailableOperation',
            defaultMessage: '无可用操作',
          }),

          value: 'noMoreOperations',
          disabled: true,
          tooltipTitle: formatMessage({
            id: 'ocp-express.Topo.TopoGraph.NoOperationAvailableInCurrent',
            defaultMessage: '当前状态无可用操作',
          }),
        },
      ];
    }
    // const menu = (
    //   <Menu id="menu" onClick={this.handleMenuClick} style={{ left: -1500 }}>
    //     {menus.map(item =>
    //       item.isDivider ? (
    //         <Menu.Divider />
    //       ) : (
    //         <Menu.Item key={item.value} disabled={item.disabled}>
    //           <Tooltip placement="left" title={item.disabled && item.tooltipTitle}>
    //             <span>{item.label}</span>
    //           </Tooltip>
    //         </Menu.Item>
    //       )
    //     )}
    //   </Menu>
    // );

    return (
      <FullScreen
        className={styles.container}
        header={{
          title: (
            <Space size={16} className={styles.title}>
              <span>{clusterData.clusterName}</span>
              <Badge text={statusItem.label} status={statusItem.badgeStatus} />
            </Space>
          ),
        }}
        onChange={() => {
          // 全屏切换时修改 graph 的宽高，避免出现 graph 无法完全覆盖容器的问题
          // TODO: 目前是不生效的，原因待排查
          this.updateGraphSize();
          // 先通过异步触发 resize 事件，来实现 updateGraphSize 的逻辑 (组件挂载时监听了 resize 事件)
          setTimeout(() => {
            window.dispatchEvent(new Event('resize'));
          }, 0);
        }}
        graph={this.graph}
        onReload={() => {
          dispatch({
            type: 'cluster/getClusterData',
            payload: {
              // id: 2,
            },
          });
        }}
      >
        {/* <Dropdown visible={true} overlay={menu} overlayClassName={styles.menu}>
          <span />
        </Dropdown> */}
        <div
          id="container"
          ref={node => {
            this.main = node;
          }}
          // 撑满父容器的高度，使得拓扑图占据全部剩余空间
          style={{ height: '100%' }}
        />

        <ModifyPasswordModal
          visible={modifyPasswordVisible}
          clusterData={this.getClusterByNode(currentNode)}
          onCancel={() => {
            this.setState({
              modifyPasswordVisible: false,
              currentNode: null,
            });
          }}
          onSuccess={() => {
            this.setState({
              modifyPasswordVisible: false,
              currentNode: null,
            });
          }}
        />
      </FullScreen>
    );
  }
}

export default TopoGraph;
