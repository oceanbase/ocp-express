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
import {
  Card,
  Divider,
  Dropdown,
  Empty,
  Menu,
  Space,
  Spin,
  Tooltip,
  Typography,
  token,
} from '@oceanbase/design';
import { FullscreenBox } from '@oceanbase/ui';
import React, { useEffect, useRef, useState } from 'react';
import {
  CarryOutOutlined,
  DownloadOutlined,
  EllipsisOutlined,
  FullscreenExitOutlined,
  FullscreenOutlined,
  PauseOutlined,
  ReloadOutlined,
} from '@oceanbase/icons';
import { useKeyPress } from 'ahooks';
import { isEnglish } from '@/util';
import { downloadLog } from '@/util/log';
import { handleSubtaskOperate, splitTaskLog } from '@/util/task';
import HighlightWithLineNumbers from '@/component/HighlightWithLineNumbers';
import useStyles from './LogCard.style';

const { Text, Paragraph } = Typography;

export interface LogCardProps {
  taskData?: API.TaskInstance;
  subtask?: API.SubtaskInstance;
  // 子任务操作成功的回调函数
  onOperationSuccess: () => void;
  log?: string;
  logLoading?: boolean;
  logPolling?: boolean;
}

const LogCard: React.FC<LogCardProps> = ({
  taskData,
  subtask,
  onOperationSuccess,
  log,
  logLoading,
  logPolling,
}) => {
  const { styles } = useStyles();
  // 是否全屏展示
  const [fullscreen, setFullscreen] = useState(false);
  const boxRef = useRef<FullscreenBox>();
  // 选中的日志节点
  const [logNode, setLogNode] = useState('');
  // 是否允许自动滚动到日志底部
  const [allowScroll, setAllowScroll] = useState(true);

  const { logNodeList, logList } = splitTaskLog(log);
  // 日志节点 (最多 4 个)
  const outerLogNodeList = logNodeList.slice(0, 3);
  const firstLogNode = outerLogNodeList && outerLogNodeList[0];
  // 收起的日志节点
  const collapsedLogNodeList = logNodeList.slice(3, logNodeList.length);
  // 展示的日志，需要兼容 3.1.0 版本之前的日志格式 (无分隔符)，日志节点为空则表明为旧版日志
  const displayLog = logNodeList.length > 0 ? logList[logNodeList.indexOf(logNode)] : log;

  // firstLogNode 改变意味着是通过刷新整个页面或者切换子任务导致的日志更新，仅这两种场景才将其设置为当前选中的日志节点
  useEffect(() => {
    setLogNode(firstLogNode);
  }, [firstLogNode]);

  // 选中的子任务或者日志节点改变后，重置 allowScroll
  useEffect(() => {
    setAllowScroll(true);
  }, [subtask?.id, logNode]);

  // 日志变化后，如果允许自动滚动，则需要一直滚动到底部保证最新日志在可视范围内
  useEffect(() => {
    // 实际展示了日志之后才滚动，且需要异步滚动，否则 scrollHeight 的值为 0
    setTimeout(() => {
      const logWrapper = document.getElementById('ocp-subtask-log-wrapper');
      if (
        logWrapper &&
        allowScroll &&
        // 仅 firstLogNode 节点自动滚动才有意义，其他节点的日志已固定，无需自动滚动
        logNode === firstLogNode
      ) {
        logWrapper.scrollTo({
          left: 0,
          top: logWrapper.scrollHeight,
        });
      }
    }, 0);
  }, [log]);

  // 选中的日志节点变化后，直接滚动到底部
  useEffect(() => {
    setTimeout(() => {
      const logWrapper = document.getElementById('ocp-subtask-log-wrapper');
      if (logWrapper) {
        logWrapper.scrollTo({
          left: 0,
          top: logWrapper.scrollHeight,
        });
      }
    }, 0);
  }, [logNode]);

  function toggleFullscreen() {
    if (boxRef.current && boxRef.current.changeFullscreen) {
      boxRef.current.changeFullscreen(!fullscreen);
    }
  }

  function handleFullscreenChange(fs) {
    setFullscreen(fs);
  }

  // 全屏状态按下 ESC 退出全屏
  useKeyPress(27, () => {
    if (fullscreen) {
      toggleFullscreen();
    }
  });

  return (
    <FullscreenBox
      ref={boxRef}
      defaultMode="viewport"
      header={false}
      onChange={handleFullscreenChange}
      className={styles.container}
    >
      <Card
        bordered={false}
        title={
          <div>
            <Space size={16}>
              <Space size={8}>
                {fullscreen ? (
                  <FullscreenExitOutlined
                    onClick={() => {
                      toggleFullscreen();
                    }}
                    className={styles.fullscreenIcon}
                  />
                ) : (
                  <FullscreenOutlined
                    onClick={() => {
                      toggleFullscreen();
                    }}
                    className={styles.fullscreenIcon}
                  />
                )}

                <Paragraph
                  ellipsis={
                    // 全屏展示不需要 ellipsis
                    fullscreen
                      ? false
                      : {
                          rows: 1,
                          tooltip: subtask?.name,
                        }
                  }
                  className={styles.subtaskName}
                  style={
                    // 全屏展示不需要 ellipsis，因此不设置最大宽度
                    fullscreen
                      ? {}
                      : {
                          // 只能设置最大宽度，如果设置宽度，则子任务名称较短的情况下，与 ID 之间的距离就会比较大
                          maxWidth: `calc(${boxRef.current?.containerRef?.current?.clientWidth}px - 440px)`,
                        }
                  }
                >
                  {subtask?.name}
                </Paragraph>
              </Space>
              <Text
                copyable={{
                  text: `${subtask?.id}`,
                }}
                className={styles.subtaskId}
              >
                {`ID: ${subtask?.id}`}
              </Text>
            </Space>
            <div className={styles.logNodeWrapper}>
              {/* 日志节点 */}
              {outerLogNodeList.map((item, index) => (
                <span key={item}>
                  <span
                    onClick={() => {
                      setLogNode(item);
                    }}
                    className={`${styles.logNode} ${item === logNode ? styles.active : ''}`}
                  >
                    {item}
                  </span>
                  {(outerLogNodeList[index + 1] || collapsedLogNodeList.length > 0) && (
                    <Divider
                      style={{
                        margin: 0,
                        marginBottom: 4,
                        width: 12,
                        minWidth: 12,
                        display: 'inline-block',
                        background: ' #7c8ca3',
                      }}
                    />
                  )}
                </span>
              ))}

              {/* 收起的日志节点 */}
              {collapsedLogNodeList.length > 0 && (
                <Dropdown
                  placement="bottomRight"
                  overlay={
                    // 下拉菜单的最大高度为 300px，超过则滚动展示，避免节点数过多超出屏幕外
                    <Menu style={{ maxHeight: 300, overflow: 'auto' }}>
                      {collapsedLogNodeList.map(item => (
                        <Menu.Item
                          key={item}
                          onClick={() => {
                            setLogNode(item);
                          }}
                          style={
                            // 设置 active 样式
                            item === logNode
                              ? {
                                  backgroundColor: token.colorPrimary,
                                  color: token.colorTextLightSolid,
                                }
                              : {}
                          }
                        >
                          {item}
                        </Menu.Item>
                      ))}
                    </Menu>
                  }
                >
                  <span
                    // 只要当前查看的日志节点收起，则高亮 EllipsisOutlined
                    className={`${styles.logNode} ${
                      collapsedLogNodeList.includes(logNode) ? styles.active : ''
                    }`}
                  >
                    <EllipsisOutlined />
                  </span>
                </Dropdown>
              )}
            </div>
          </div>
        }
        extra={
          <Space size={16}>
            <span
              onClick={() => {
                downloadLog(log, `subtask_${subtask?.id}.log`);
              }}
              className="pointable"
            >
              <Space size={4}>
                <DownloadOutlined />
                <span>
                  {formatMessage({
                    id: 'ocp-express.Detail.Log.LogCard.DownloadLogs',
                    defaultMessage: '下载日志',
                  })}
                </span>
              </Space>
            </span>
            {subtask?.status === 'RUNNING' && (
              <span
                onClick={() => {
                  handleSubtaskOperate('stop', taskData, subtask, onOperationSuccess);
                }}
                className="pointable"
              >
                <Space size={4}>
                  <PauseOutlined />
                  <span>
                    {formatMessage({
                      id: 'ocp-express.Detail.Log.LogCard.StopRunning',
                      defaultMessage: '终止运行',
                    })}
                  </span>
                </Space>
              </span>
            )}

            {subtask?.status === 'FAILED' && (
              <>
                <Tooltip
                  placement="topRight"
                  // 如果属于远程 OCP 发起任务下的子任务，则禁止重试
                  title={
                    taskData?.isRemote &&
                    formatMessage({
                      id: 'ocp-express.Detail.Log.LogCard.TheCurrentTaskIsInitiated',
                      defaultMessage: '当前任务为远程 OCP 发起，请到发起端的 OCP 进行操作',
                    })
                  }
                >
                  <span
                    onClick={() => {
                      handleSubtaskOperate('retry', taskData, subtask, onOperationSuccess);
                    }}
                    className={`pointable ${taskData?.isRemote ? 'disabled' : ''}`}
                  >
                    <Space size={4}>
                      <ReloadOutlined />
                      <span>
                        {formatMessage({
                          id: 'ocp-express.Detail.Log.LogCard.ReRun',
                          defaultMessage: '重新运行',
                        })}
                      </span>
                    </Space>
                  </span>
                </Tooltip>
                <span
                  onClick={() => {
                    handleSubtaskOperate('skip', taskData, subtask, onOperationSuccess);
                  }}
                  className="pointable"
                >
                  <Space size={4}>
                    <CarryOutOutlined />
                    <span>
                      {formatMessage({
                        id: 'ocp-express.Detail.Log.LogCard.SetToSuccessful',
                        defaultMessage: '设置为成功',
                      })}
                    </span>
                  </Space>
                </span>
              </>
            )}
          </Space>
        }
      >
        <div
          id="ocp-subtask-log-wrapper"
          onScroll={e => {
            const { scrollTop, clientHeight, scrollHeight } = e.currentTarget;
            // 当滚动到日志底部，才允许自动滚动
            if (
              scrollTop +
                clientHeight +
                // 12 为 Spin 距卡片底部的距离
                12 >=
              scrollHeight
            ) {
              setAllowScroll(true);
            } else {
              setAllowScroll(false);
            }
          }}
          className={styles.log}
          style={{
            overflow: 'auto',
            // 英文环境下，任务详情会占据两行，需要减去额外的 38px
            height: fullscreen
              ? 'calc(100vh - 84px)'
              : `calc(100vh - 298px - ${isEnglish() ? 38 : 0}px)`,
            padding: '12px 16px',
            // 为了避免 Card header 的 border-bottom 在日志滚动时被遮挡，需要留出 1px 间距
            marginTop: 1,
          }}
        >
          {/* 不使用 Spin 对日志进行包裹，否则 loading 图标会出现在滚动区域的中间，切换 tab 时会看不到，而只能看到蒙层 */}
          {logLoading && !logPolling ? (
            <Spin spinning={logPolling} />
          ) : displayLog ? (
            <>
              {/* 超过 100000 个字符长度 (大概 100KB) 的日志，直接原文展示，不做格式化高亮，避免页面卡死 */}
              {displayLog.length > 100000 ? (
                <pre
                  style={{
                    whiteSpace: 'break-spaces',
                    color: 'rgba(0, 0, 0, 0.65)',
                    textAlign: 'left',
                    margin: 0,
                    padding: '0.5em',
                  }}
                >
                  {displayLog}
                </pre>
              ) : (
                <HighlightWithLineNumbers language="javaLog" content={displayLog} />
              )}

              {logNode === firstLogNode && <Spin spinning={logPolling} />}
            </>
          ) : (
            <Empty
              description={formatMessage({
                id: 'ocp-express.Detail.Log.LogCard.NoLog',
                defaultMessage: '暂无日志',
              })}
              style={{ marginTop: 80 }}
            />
          )}
        </div>
      </Card>
    </FullscreenBox>
  );
};

export default LogCard;
