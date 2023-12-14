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
import React, { useState, useRef } from 'react';
import { FullscreenBox } from '@oceanbase/ui';
import type { FullscreenBoxProps } from '@oceanbase/ui/es/FullscreenBox';
import type { Graph } from '@antv/g6';
import GraphToolbar from '@/component/GraphToolbar';
import useStyles from './index.style';

interface FullScreenProps extends FullscreenBoxProps {
  // 左上角标题
  title?: React.ReactNode;
  // 左上角描述
  description?: React.ReactNode;
  graph?: Graph;
  onReload?: () => void;
  // 需要自定义 header 的类型定义，因为设置了默认的 extra，所以传入的 header 必须是对象
  header?: {
    title?: React.ReactNode;
    extra?: React.ReactNode;
  };
  className?: string;
  style?: React.CSSProperties;
}

const FullScreen: React.FC<FullScreenProps> = ({
  title,
  description,
  graph,
  onReload,
  header,
  onChange,
  children,
  className,
  style,
  ...restProps
}) => {
  const { styles } = useStyles();
  const [fullscreen, setFullscreen] = useState(false);
  const boxRef = useRef();

  function toggleFullscreen() {
    if (boxRef.current) {
      boxRef.current.changeFullscreen(!fullscreen);
    }
  }

  function handleFullscreenChange(fs) {
    setFullscreen(fs);
    if (onChange) {
      onChange(fs);
    }
  }

  return (
    <FullscreenBox
      ref={boxRef}
      defaultMode="viewport"
      // 全屏状态下才展示 header
      header={
        fullscreen && {
          extra: graph && (
            <GraphToolbar mode="embed" graph={graph} showFullscreen={false} onReload={onReload} />
          ),

          ...header,
        }
      }
      onChange={handleFullscreenChange}
      className={`${styles.container} ${className}`}
      style={{
        ...style,
        // 集群和租户拓扑图:
        // 全屏状态下，paddingTop 设为 0，正常展示
        // 非全屏状态下，paddingTop 设为 50，避免和全局 Header 出现重叠
        paddingTop: fullscreen ? 0 : 50,
      }}
      {...restProps}
    >
      {/* 左上角全屏入口: 未设置 title 和 description 才展示 */}
      {/* {!fullscreen && !(title || description) && (
        <div className={styles.bubble}>
          <Tooltip
            title={formatMessage({
              id: 'ocp-express.component.FullScreen.EnterFullScreen',
              defaultMessage: '进入全屏',
            })}
          >
            <FullscreenOutlined onClick={toggleFullscreen} className={styles.icon} />
          </Tooltip>
        </div>
      )} */}

      {/* 左上角标题和描述: 设置了 title 或 description 才展示 */}
      {!fullscreen && (title || description) && (
        <div className={styles.titleBar}>
          <span className={styles.title}>
            {formatMessage(
              {
                id: 'ocp-express.component.FullScreen.ProtectedModeTitle',
                defaultMessage: '保护模式：{title}',
              },
              { title }
            )}
          </span>
          <span className={styles.description}>{description}</span>
        </div>
      )}

      {!fullscreen && graph && (
        <GraphToolbar
          mode="fixed"
          graph={graph}
          showFullscreen={true}
          onFullscreen={() => {
            toggleFullscreen();
          }}
          onReload={onReload}
          className={styles.toolbar}
        />
      )}

      {children}
    </FullscreenBox>
  );
};

export default FullScreen;
