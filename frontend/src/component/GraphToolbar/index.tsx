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
import React, { useState, useEffect } from 'react';
import { Divider, Space, Tooltip } from '@oceanbase/design';
import { FullscreenOutlined, MinusOutlined, PlusOutlined, SyncOutlined } from '@oceanbase/icons';
import { debounce } from 'lodash';
import { toPercent } from '@oceanbase/util';
import type { Graph } from '@antv/g6';
import { getCenterPointByGraph } from '@/util/graph';
import styles from './index.less';

export interface GraphToolbarProps {
  // 模式: 悬浮模式 | 嵌入模式
  mode?: 'fixed' | 'embed';
  graph: Graph;
  // 是否展示进入全屏的入口
  showFullscreen?: boolean;
  // 点击进入全屏的回调函数
  onFullscreen?: () => void;
  onReload?: () => void;
  className?: string;
}

const GraphToolbar: React.FC<GraphToolbarProps> = ({
  mode = 'fixed',
  graph,
  onReload,
  showFullscreen,
  onFullscreen,
  className,
}) => {
  const [zoom, setZoom] = useState(0);
  useEffect(() => {
    const initialZoom = graph.getZoom() > 2 ? 2 : graph.getZoom();
    // 上层调用 graph.fitView() 后缩放可能超过 100%，因此这里再做一次缩放，避免缩放超过 100%
    graph.zoomTo(initialZoom, getCenterPointByGraph(graph));
    setZoom(initialZoom);
    // Graph 缩放时，更新 zoom
    graph.on(
      'wheelzoom',
      // 使用防抖函数，避免状态改变过于频繁，影响页面交互流畅
      // 设置延时时间为 16ms，为渲染一帧所需时间
      debounce(() => {
        setZoom(graph.getZoom());
      }, 16)
    );
  }, []);

  // 非 Graph 缩放引起的画布变化，也需要更新 zoom
  // 该依赖监听和 wheelzoom 的事件监听不会同时执行，前者是 Graph 更新 + 组件渲染导致更新 zoom，后者是 Graph 内部事件导致更新 zoom
  useEffect(() => {
    setZoom(graph.getZoom());
  }, [graph.getZoom()]);

  return (
    <Space size={16} className={`${styles.toolbar} ${styles[mode]} ${className}`}>
      {showFullscreen && (
        <Tooltip
          title={formatMessage({
            id: 'ocp-express.component.GraphToolbar.EnterFullScreen',
            defaultMessage: '进入全屏',
          })}
        >
          <FullscreenOutlined
            className="pointable"
            onClick={() => {
              if (onFullscreen) {
                onFullscreen();
              }
            }}
          />
        </Tooltip>
      )}

      <span className={styles.zoomWrapper}>
        <Tooltip
          title={formatMessage({
            id: 'ocp-express.component.GraphToolbar.Narrow',
            defaultMessage: '缩小',
          })}
        >
          <MinusOutlined
            className={zoom >= 0.3 ? 'pointable' : 'disabled'}
            onClick={() => {
              if (zoom >= 0.3) {
                const newZoom = zoom - 0.1;
                setZoom(newZoom);
                graph.zoomTo(newZoom, getCenterPointByGraph(graph));
              }
            }}
          />
        </Tooltip>
        {toPercent(zoom)}
        <Tooltip
          title={formatMessage({
            id: 'ocp-express.component.GraphToolbar.ZoomIn',
            defaultMessage: '放大',
          })}
        >
          <PlusOutlined
            className={zoom <= 1.9 ? 'pointable' : 'disabled'}
            onClick={() => {
              if (zoom <= 1.9) {
                const newZoom = zoom + 0.1;
                setZoom(newZoom);
                graph.zoomTo(newZoom, getCenterPointByGraph(graph));
              }
            }}
          />
        </Tooltip>
      </span>
      <Tooltip
        title={formatMessage({
          id: 'ocp-express.component.GraphToolbar.DisplayOriginalRatio',
          defaultMessage: '显示原始比例',
        })}
      >
        <img
          src="/assets/icon/reset.svg"
          alt=""
          className="pointable"
          onClick={() => {
            setZoom(1);
            graph.zoomTo(1, getCenterPointByGraph(graph));
          }}
        />
      </Tooltip>
      <Tooltip
        title={formatMessage({
          id: 'ocp-express.component.GraphToolbar.AdaptiveCanvas',
          defaultMessage: '自适应画布',
        })}
      >
        <img
          src="/assets/icon/fit_canvas.svg"
          alt=""
          className="pointable"
          onClick={() => {
            graph.fitView();
            setZoom(graph.getZoom());
          }}
        />
      </Tooltip>
      {onReload && (
        <span>
          <Divider type="vertical" className={styles.divider} />
        </span>
      )}

      {onReload && (
        <Tooltip
          title={formatMessage({
            id: 'ocp-express.component.GraphToolbar.Refresh',
            defaultMessage: '刷新',
          })}
        >
          <span>
            <SyncOutlined
              className="pointable"
              onClick={() => {
                onReload();
              }}
            />
          </span>
        </Tooltip>
      )}
    </Space>
  );
};

export default GraphToolbar;
