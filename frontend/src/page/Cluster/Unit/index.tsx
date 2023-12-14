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
import { history } from 'umi';
import {
  Badge,
  Card,
  Col,
  Popconfirm,
  Row,
  Space,
  Spin,
  Switch,
  Tooltip,
  message,
} from '@oceanbase/design';
import { FullscreenBox } from '@oceanbase/ui';
import React, { useRef, useState } from 'react';
import { find, flatten, some } from 'lodash';
import { PageContainer } from '@oceanbase/ui';
import { BellOutlined, DownOutlined, FullscreenOutlined, SyncOutlined } from '@oceanbase/icons';
import { useInterval, useKeyPress, useLockFn, useRequest, useScroll } from 'ahooks';
import * as ObUnitController from '@/service/custom/ObUnitController';
import * as ObResourceController from '@/service/ocp-express/ObResourceController';
import ContentWithReload from '@/component/ContentWithReload';
import type { FilterValue } from '@/component/FilterDropdown';
import FilterDropdown from '@/component/FilterDropdown';
// import ContentWithInfo from '@/component/ContentWithInfo';
import ContentWithQuestion from '@/component/ContentWithQuestion';
import type { BlockType } from './Component/Block';
import Block from './Component/Block';
import UnitBlock from './Component/UnitBlock';
// import UnitMigrateModal from './Component/UnitMigrateModal';
import useStyles from './index.style';
import { breadcrumbItemRender } from '@/util/component';

export interface UnitProps {
  clusterId: number;
}

const Unit: React.FC<UnitProps> = ({ clusterId }) => {
  const { styles } = useStyles();
  const [obTenantIdList, setObTenantIdList] = useState<FilterValue[]>([]);
  const [regionList, setRegionList] = useState<FilterValue[]>([]);
  const [zoneList, setZoneList] = useState<FilterValue[]>([]);
  const [realtime, setRealtime] = useState(false);

  // 是否全屏展示
  const [fullscreen, setFullscreen] = useState(false);
  const boxRef = useRef();
  const unitRef = React.useRef<HTMLDivElement>(null);
  const scroll = useScroll(unitRef);

  function toggleFullscreen() {
    if (boxRef.current && boxRef.current.changeFullscreen) {
      boxRef.current.changeFullscreen(!fullscreen);
    }
  }

  function handleFullscreenChange(fs) {
    setFullscreen(fs);
  }

  // 27 === Esc， 全屏状态下按下 Esc 退出全屏
  useKeyPress(27, () => {
    if (boxRef.current && boxRef.current.changeFullscreen && fullscreen) {
      boxRef.current.changeFullscreen(false);
    }
  });

  // 是否展示迁移 Unit 的弹窗
  // const [visible, setVisible] = useState(false);
  // const [currentUnitInfo, setCurrentUnitInfo] = useState<API.ClusterUnitViewOfUnit | undefined>(
  //   undefined
  // );

  const [popconfirmVisible, setPopconfirmVisible] = useState(false);

  // 获取 Unit 视图全量数据，由前端完成筛选
  const {
    data,
    refresh: refreshUnitView,
    loading,
  } = useRequest(ObResourceController.clusterUnitView, {
    defaultParams: [{}],
    onSuccess: res => {
      if (res.successful && (res.data?.deletableUnitCount || 0) > 0) {
        setPopconfirmVisible(true);
      }
    },
  });

  const { runAsync: tryDeleteUnusedUnit, loading: deleteUnUseUnitLoading } = useRequest(
    ObUnitController.tryDeleteUnusedUnit,
    {
      manual: true,
      onSuccess: res => {
        setPopconfirmVisible(false);
        if (res.successful) {
          message.success(
            formatMessage({
              id: 'ocp-express.Detail.Resource.Unit.TheUnassociatedUnitIsReleased',
              defaultMessage: '未关联的 Unit 释放成功',
            })
          );

          refreshUnitView();
        }
      },
    }
  );

  const unitViewData = data?.data;
  const viewData = unitViewData;

  // 全部的 Zone 列表
  const allZoneInfos = flatten(
    (viewData?.regionInfos || []).map(item => {
      return (item.zoneInfos || []).map(zoneInfo => ({
        ...zoneInfo,
        obRegionName: item.obRegionName,
      }));
    })
  ) as ((API.ClusterUnitViewOfZone | API.ClusterReplicaViewOfZone) & {
    obRegionName?: string;
  })[];

  // 全部的 Server 列表
  const allServerInfos = flatten(allZoneInfos.map(item => item.serverInfos || []));

  // 查询视图中是否存在运行中的状态，如果存在需要进行轮询
  const polling = some(allServerInfos, serverInfo => {
    // Unit 轮询条件: 迁移中
    const unitPolling = serverInfo?.unitInfos?.some(unitInfo =>
      ['MIGRATE_IN', 'MIGRATE_OUT', 'ROLLBACK_MIGRATE_IN', 'ROLLBACK_MIGRATE_OUT'].includes(
        unitInfo.migrateType
      )
    );

    // Server 轮询条件: 服务停止中、进程停止中、启动中、重启中
    const serverPolling = [
      'SERVICE_STOPPING',
      'PROCESS_STOPPING',
      'STARTING',
      'RESTARTING',
    ].includes(serverInfo.status as API.ObServerStatus);
    return unitPolling || serverPolling;
  });
  const isPolling = polling || realtime;

  // Unit 视图接口响应较快，实时频率为 1 秒，副本视图为 30 秒
  const frequency = 1;
  const lockRefresh = useLockFn(refreshUnitView);
  // 请求轮询
  useInterval(
    () => {
      lockRefresh();
    },
    isPolling ? frequency * 1000 : null
  );

  // 用于筛选的对象列表
  const tenantFilters = (viewData?.tenantInfos || []).map(item => ({
    value: item.obTenantId,
    label: item.tenantName,
  }));

  const regionFilters = (viewData?.regionInfos || []).map(item => ({
    value: item.obRegionName,
    label: item.obRegionName,
  }));

  const zoneFilters = allZoneInfos.map(item => ({
    value: item.obZoneName,
    label: item.obZoneName,
  }));

  // 筛选后的租户列表
  const tenantInfos = (viewData?.tenantInfos || []).filter(
    item =>
      !obTenantIdList || obTenantIdList.length === 0 || obTenantIdList.includes(item.obTenantId)
  );

  // 筛选后的 Zone 列表，实际上 Region 和 Zone 是一对一的，所以也能用于渲染 Region 列表
  const zoneInfos = allZoneInfos
    // 根据 region 进行筛选
    .filter(
      item => !regionList || regionList.length === 0 || regionList.includes(item.obRegionName)
    )
    // 根据 zone 进行筛选
    .filter(
      zoneInfo => !zoneList || zoneList.length === 0 || zoneList.includes(zoneInfo.obZoneName)
    );

  const routes = [
    {
      path: '/overview',
      breadcrumbName: formatMessage({
        id: 'ocp-express.Cluster.Unit.ClusterOverview',
        defaultMessage: '集群总览',
      }),
    },

    {
      breadcrumbName: formatMessage({
        id: 'ocp-express.Detail.Resource.UnitDistribution',
        defaultMessage: 'Unit 分布',
      }),
    },
  ];

  return (
    <FullscreenBox
      ref={boxRef}
      defaultMode="viewport"
      header={false}
      onChange={handleFullscreenChange}
      className={`${styles.container} ${fullscreen ? styles.fullscreen : ''}`}
    >
      <PageContainer
        ghost={true}
        header={{
          breadcrumb: fullscreen ? undefined : { routes, itemRender: breadcrumbItemRender },
          title: !fullscreen && (
            <ContentWithReload
              spin={loading}
              content={formatMessage({
                id: 'ocp-express.Detail.Resource.UnitDistribution',
                defaultMessage: 'Unit 分布',
              })}
              onClick={() => {
                refreshUnitView();
              }}
            />
          ),

          onBack: fullscreen
            ? undefined
            : () => {
                history.push('/overview');
              },
          extra: (
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
              }}
            >
              <div />
              <Space size={24}>
                <Space size={16}>
                  <FilterDropdown
                    placement="bottomLeft"
                    value={obTenantIdList}
                    filters={tenantFilters}
                    onChange={value => {
                      setObTenantIdList(value);
                    }}
                    inputProps={{
                      placeholder: formatMessage({
                        id: 'ocp-express.Detail.Resource.Unit.EnterTheTenantNameTo',
                        defaultMessage: '请输入租户名搜索',
                      }),
                    }}
                  >
                    <Space className="pointable">
                      <span>
                        {formatMessage({
                          id: 'ocp-express.Detail.Resource.Unit.Tenant',
                          defaultMessage: '租户',
                        })}
                      </span>
                      <DownOutlined />
                    </Space>
                  </FilterDropdown>
                  <FilterDropdown
                    placement="bottomLeft"
                    value={regionList}
                    filters={regionFilters}
                    onChange={value => {
                      setRegionList(value);
                    }}
                    inputProps={{
                      placeholder: formatMessage({
                        id: 'ocp-express.Detail.Resource.Unit.EnterARegionNameTo',
                        defaultMessage: '请输入区域名搜索',
                      }),
                    }}
                  >
                    <Space className="pointable">
                      <span>
                        {formatMessage({
                          id: 'ocp-express.Detail.Resource.Unit.Area',
                          defaultMessage: '区域',
                        })}
                      </span>
                      <DownOutlined />
                    </Space>
                  </FilterDropdown>
                  <FilterDropdown
                    placement="bottomLeft"
                    value={zoneList}
                    filters={zoneFilters}
                    onChange={value => {
                      setZoneList(value);
                    }}
                    inputProps={{
                      placeholder: formatMessage({
                        id: 'ocp-express.Detail.Resource.Unit.EnterAZoneNameTo',
                        defaultMessage: '请输入 Zone 名称搜索',
                      }),
                    }}
                  >
                    <Space className="pointable">
                      <span>Zone</span>
                      <DownOutlined />
                    </Space>
                  </FilterDropdown>
                </Space>
                <div>
                  <span>
                    <ContentWithQuestion
                      content={formatMessage({
                        id: 'ocp-express.Detail.Resource.Unit.AutomaticRefresh',
                        defaultMessage: '自动刷新',
                      })}
                      tooltip={{
                        title: formatMessage(
                          {
                            id: 'ocp-express.Detail.Resource.Unit.TheAutomaticRefreshFrequencyIs',
                            defaultMessage: '自动刷新频率为 {frequency} 秒',
                          },
                          { frequency: frequency }
                        ),
                      }}
                    />
                    ：
                  </span>
                  <Switch
                    size="small"
                    style={{ marginTop: -4 }}
                    checked={realtime}
                    onChange={value => {
                      setRealtime(value);
                      if (value) {
                        refreshUnitView();
                      }
                    }}
                  />
                </div>

                {unitViewData?.deletableUnitCount > 0 ? (
                  <Popconfirm
                    data-aspm-click="c304249.d308746"
                    data-aspm-desc="Unit 分布-空闲 Unit 提示"
                    data-aspm-param={``}
                    data-aspm-expo
                    title={formatMessage(
                      {
                        id: 'ocp-express.Detail.Resource.Unit.UnitviewdatadeletableunitcountUnitsHaveBeenCreated',
                        defaultMessage:
                          '检测到 {unitViewDataDeletableUnitCount} 个 unit 已经创建超过 {unitViewDataUnusedUnitMaxReserveHour} 小时，但未关联到租户或任务',
                      },

                      {
                        unitViewDataDeletableUnitCount: unitViewData.deletableUnitCount,
                        unitViewDataUnusedUnitMaxReserveHour: unitViewData.unusedUnitMaxReserveHour,
                      }
                    )}
                    placement="bottomRight"
                    okButtonProps={{ loading: deleteUnUseUnitLoading }}
                    visible={popconfirmVisible}
                    onVisibleChange={() => {
                      setPopconfirmVisible(true);
                    }}
                    onConfirm={() => {
                      return tryDeleteUnusedUnit({ id: clusterId });
                    }}
                    onCancel={() => setPopconfirmVisible(false)}
                    okText={formatMessage({
                      id: 'ocp-express.Detail.Resource.Unit.OneClickRelease',
                      defaultMessage: '一键释放',
                    })}
                  >
                    <Badge
                      data-aspm-click="c304249.d308745"
                      data-aspm-desc="Unit 分布-空闲 Unit 提示入口"
                      data-aspm-param={``}
                      data-aspm-expo
                      dot={true}
                    >
                      <BellOutlined className="pointable" />
                    </Badge>
                  </Popconfirm>
                ) : (
                  <Tooltip
                    title={formatMessage({
                      id: 'ocp-express.Detail.Resource.Unit.NoNotification',
                      defaultMessage: '暂无通知',
                    })}
                  >
                    <BellOutlined className="pointable" />
                  </Tooltip>
                )}

                <Tooltip
                  title={formatMessage({
                    id: 'ocp-express.Detail.Resource.Unit.Refresh',
                    defaultMessage: '刷新',
                  })}
                >
                  <SyncOutlined
                    className="pointable"
                    onClick={() => {
                      refreshUnitView();
                    }}
                  />
                </Tooltip>
                <Tooltip
                  title={
                    fullscreen
                      ? formatMessage({
                          id: 'ocp-express.Detail.Resource.Unit.ExitFullScreen',
                          defaultMessage: '退出全屏',
                        })
                      : formatMessage({
                          id: 'ocp-express.Detail.Resource.Unit.FullScreen',
                          defaultMessage: '全屏',
                        })
                  }
                >
                  <FullscreenOutlined
                    onClick={() => {
                      toggleFullscreen();
                    }}
                    className="pointable"
                  />
                </Tooltip>
              </Space>
            </div>
          ),
        }}
      >
        <Card bordered={false}>
          <Spin spinning={isPolling ? false : loading}>
            {/* <div
               style={{
                 display: 'flex',
                 justifyContent: 'space-between',
                 alignItems: 'center',
                 padding: '16px 0px 0px 24px',
                 minHeight: 41,
                 boxSizing: 'border-box',
               }}
              >
              Unit 视图展示，在内部判断是为了保留 Space 适应 'space-between' 的布局
              <Space size={16}>
               <>
                 <Space>
                   <div
                     className={styles.tag}
                     style={{ backgroundColor: 'rgba(255,192,105,0.65)' }}
                   />
                    <span>
                     {formatMessage({
                       id: 'ocp-express.Detail.Resource.Unit.MoveOut',
                       defaultMessage: '迁出',
                     })}
                   </span>
                 </Space>
                 <Space>
                   <div
                     className={styles.tag}
                     style={{ backgroundColor: 'rgba(89,126,247,0.65)' }}
                   />
                    <span>
                     {formatMessage({
                       id: 'ocp-express.Detail.Resource.Unit.Move',
                       defaultMessage: '迁入',
                     })}
                   </span>
                 </Space>
                 <Space>
                   <div className={styles.tag} style={{ backgroundColor: 'rgba(0,0,0,0.15)' }} />
                   <span>
                     {formatMessage({
                       id: 'ocp-express.Detail.Resource.Unit.RollingBack',
                       defaultMessage: '回滚中',
                     })}
                   </span>
                 </Space>
               </>
              </Space> */}

            {/* 存在集群操作权限时，才显示对应的提示 */}
            {/* <ContentWithInfo
                 content={
                   <span style={{ fontSize: 14 }}>
                     {formatMessage({
                       id: 'ocp-express.Detail.Resource.ClickTheUnitCardTo',
                       defaultMessage: '鼠标点击 Unit 卡片发起主动迁移',
                     })}
                   </span>
                 }
               /> */}
            {/* </div> */}
            <div
              ref={unitRef}
              style={{
                display: 'flex',
                overflow: 'auto',
                // 全屏时底部 16px 的高度没有被覆盖，这里手动处理下
                ...(fullscreen ? { marginBottom: 16 } : {}),
              }}
            >
              <Row
                gutter={[0, 12]}
                className={styles.unitRow}
                style={{
                  // 未开始滚动时，隐藏 box-shadow 的样式，类似于 antd Table 的滚动效果
                  boxShadow: scroll?.left === 0 ? 'none' : '2px 0px 10px rgba(0, 0, 0, 0.12)',
                }}
              >
                {['region', 'zone', 'server'].map(item => (
                  <Col key={item} span={24}>
                    <Block key={item} type={item as BlockType} />
                  </Col>
                ))}

                <Col span={24}>
                  <Row gutter={12}>
                    <Col span={10}>
                      <Block type="tenant" />
                    </Col>
                    <Col span={14}>
                      <Row gutter={[0, 12]}>
                        {tenantInfos.map(tenantInfo => (
                          <Col key={tenantInfo.tenantName} span={24}>
                            <Block
                              type="tenant"
                              target={tenantInfo.tenantName}
                              tenantInfo={tenantInfo}
                              // 单个租户 Block 需要跳转到对应的总览页，因此需要传 clusterId
                              clusterId={clusterId}
                            />
                          </Col>
                        ))}
                      </Row>
                    </Col>
                  </Row>
                </Col>
                {['memory', 'cpu', 'disk', 'unit'].map(item => (
                  <Col key={item} span={24}>
                    <Block key={item} type={item as BlockType} />
                  </Col>
                ))}
              </Row>
              {zoneInfos.map(item => {
                const serverInfos = item.serverInfos || [];
                return (
                  <Row
                    key={item.obZoneName}
                    gutter={[0, 12]}
                    className={styles.zoneCol}
                    style={{
                      // 根据 server 数目计算每个 Zone 列的宽度
                      width:
                        serverInfos.length * 176 +
                        // 中间间隔
                        (serverInfos.length - 1) * 12 +
                        // 左侧 padding
                        24,
                    }}
                  >
                    {/* 区域 */}
                    <Col span={24}>
                      <Block type="region" target={item.obRegionName} />
                    </Col>
                    {/* Zone */}
                    <Col span={24}>
                      <Block type="zone" target={item.obZoneName} zoneInfo={item} />
                    </Col>
                    <Col span={24}>
                      {/* server */}
                      <Row gutter={12} style={{ flexWrap: 'nowrap' }}>
                        {serverInfos.map(serverInfo => {
                          const server = `${serverInfo.serverIp}:${serverInfo.serverPort}`;

                          return (
                            <Col key={server}>
                              <Block type="server" target={server} serverInfo={serverInfo} />
                            </Col>
                          );
                        })}
                      </Row>
                    </Col>
                    {/* Unit */}
                    <Col span={24}>
                      <Row gutter={[0, 12]}>
                        {tenantInfos.map(tenantInfo => (
                          <Col key={tenantInfo.tenantName} span={24}>
                            <Row gutter={12} style={{ flexWrap: 'nowrap' }}>
                              {serverInfos.map(serverInfo => {
                                const targetUnitInfo = find(
                                  serverInfo.unitInfos,
                                  // 通过 obTenantId 将所属租户的 Unit 筛选出来，可能为空，表明租户在当前 server 上没有 Unit 分布
                                  unitInfo => unitInfo.obTenantId === tenantInfo.obTenantId
                                );

                                return (
                                  <Col key={serverInfo.serverIp}>
                                    <UnitBlock
                                      clusterId={clusterId}
                                      unitInfo={targetUnitInfo}
                                      // onClick={() => {
                                      //   // 拥有更新权限时才展示 unit 迁移 Modal
                                      //   if (targetUnitInfo?.migrateType === 'NOT_IN_MIGRATE') {
                                      //     setVisible(true);
                                      //     setCurrentUnitInfo(targetUnitInfo);
                                      //   }
                                      // }}
                                      onSuccess={() => {
                                        refreshUnitView();
                                      }}
                                    />
                                  </Col>
                                );
                              })}
                            </Row>
                          </Col>
                        ))}
                      </Row>
                    </Col>
                    <Col span={24}>
                      {/* 内存 */}
                      <Row gutter={12} style={{ flexWrap: 'nowrap' }}>
                        {serverInfos.map(serverInfo => (
                          <Col key={serverInfo.serverIp}>
                            <Block type="memory" serverInfo={serverInfo} />
                          </Col>
                        ))}
                      </Row>
                    </Col>
                    <Col span={24}>
                      {/* CPU */}
                      <Row gutter={12} style={{ flexWrap: 'nowrap' }}>
                        {serverInfos.map(serverInfo => (
                          <Col key={serverInfo.serverIp}>
                            <Block type="cpu" serverInfo={serverInfo} />
                          </Col>
                        ))}
                      </Row>
                    </Col>
                    <Col span={24}>
                      {/* 磁盘 */}
                      <Row gutter={12} style={{ flexWrap: 'nowrap' }}>
                        {serverInfos.map(serverInfo => (
                          <Col key={serverInfo.serverIp}>
                            <Block type="disk" serverInfo={serverInfo} />
                          </Col>
                        ))}
                      </Row>
                    </Col>
                    <Col span={24}>
                      <Row gutter={12} style={{ flexWrap: 'nowrap' }}>
                        {serverInfos.map(serverInfo => (
                          <Col key={serverInfo.serverIp}>
                            {/* Unit 数目 */}
                            <Block
                              type="unit"
                              serverInfo={serverInfo}
                              target={serverInfo.unitCount}
                            />
                          </Col>
                        ))}
                      </Row>
                    </Col>
                  </Row>
                );
              })}
            </div>
          </Spin>
        </Card>
        {/* <UnitMigrateModal
           clusterId={clusterId}
           unitInfo={currentUnitInfo}
           serverInfos={allServerInfos}
           visible={visible}
           onCancel={() => {
             setVisible(false);
             setCurrentUnitInfo(undefined);
           }}
           onSuccess={() => {
             setVisible(false);
             setCurrentUnitInfo(undefined);
             refreshUnitView();
           }}
          /> */}
      </PageContainer>
    </FullscreenBox>
  );
};

export default Unit;
