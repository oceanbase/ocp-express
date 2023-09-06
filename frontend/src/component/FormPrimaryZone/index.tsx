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
import { Checkbox, Col, Row, Spin, Typography, Card, Tooltip } from '@oceanbase/design';
import classNames from 'classnames';
import { flatten, isEqual, uniq } from 'lodash';
import { sortByEnum } from '@oceanbase/util';
import { RightOutlined, DeleteOutlined } from '@oceanbase/icons';
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd';
import { isEnglish } from '@/util';
import ContentWithIcon from '@/component/ContentWithIcon';
import { ReactComponent as DragIconSvg } from '@/asset/drag_icon.svg';
import styles from './index.less';

const { Text } = Typography;

export interface FormPrimaryZoneProps {
  zoneList: string[];
  loading: boolean;
  value?: string;
  onChange?: (value: string) => void;
}

export interface FormPrimaryZoneState {
  zoneList: string[];
  checkedList: string[];
  checkAll: boolean;
  indeterminate: boolean;
  priorityList: string[];
}

class FormPrimaryZone extends React.Component<FormPrimaryZoneProps, FormPrimaryZoneState> {
  public constructor(props: FormPrimaryZoneProps) {
    super(props);
    this.state = {
      // 候选 zone 列表
      zoneList: props.zoneList,
      // 选中的 zone 列表
      checkedList: [],
      // 是否全部选中
      checkAll: false,
      // 是否部分选中
      indeterminate: false,
      // zone 优先级列表，形如 zone1,zone2;zone3
      priorityList: [],
    };
  }

  public static getDerivedStateFromProps(props: FormPrimaryZoneProps, state: FormPrimaryZoneState) {
    const newState: Partial<FormPrimaryZoneState> = state;
    const { value, zoneList } = props;
    if ('value' in props) {
      const nestPriorityList =
        (value && value.split(';') && value.split(';').map(item => item.split(','))) || [];
      const priorityList = nestPriorityList
        // 将不在 zone 列表中的优先级去除
        .map(item => (item || []).filter(subItem => zoneList.indexOf(subItem) !== -1))
        // 筛选掉空数组
        .filter(item => item && item.length > 0)
        // 以逗号连接优先级相同的 zone
        .map(item => item.join(','));
      // 更新右侧的优先级列表
      newState.priorityList = priorityList;
      // 已经设置了优先级的 zone 列表
      const zoneListInPriority = flatten(priorityList.map(item => item.split(',')));
      // 更新左侧的 zone 列表
      // 最后需要做去重，否则在某些交互条件下，会出现预期外的重复 zone
      // TODO: 可能是 zone 相同时，会触发 Checkbox 的相关 bug，具体原因待排查
      newState.zoneList = uniq(
        zoneList
          // 筛掉空值
          .filter(item => item)
          .filter(item => zoneListInPriority.indexOf(item) === -1)
      );
    }
    return newState;
  }

  public componentDidUpdate(prevProps: FormPrimaryZoneProps) {
    const { zoneList, onChange } = this.props;
    // priorityList 已经在 getDerivedStateFromProps 做了处理，是最新的值，直接使用即可
    const { priorityList } = this.state;
    // 当上层设置的 zone 列表变化时，zone 的优先级也需要联动
    if (!isEqual(zoneList, prevProps.zoneList)) {
      if (onChange) {
        onChange(priorityList.join(';'));
      }
    }
  }

  public handleChange = (value: string) => {
    const { onChange } = this.props;
    if (onChange) {
      onChange(value);
    }
  };

  // 根据原始的 zone 列表进行排序
  public sortByZoneList = (list: string[]) => {
    const { zoneList } = this.props;
    return list.sort((a, b) => sortByEnum(a, b, '', zoneList));
  };

  // 勾选 zone
  public handleCheckboxGroupChange = (checkedList: string[]) => {
    const { zoneList } = this.state;
    this.setState({
      checkedList: this.sortByZoneList(checkedList),
      checkAll: checkedList.length === zoneList.length,
      indeterminate: !!checkedList.length && checkedList.length < zoneList.length,
    });
  };

  // 全选/不选 zone
  public handleCheckAllChange = (e: any) => {
    const { zoneList } = this.state;
    this.setState({
      checkedList: e.target.checked ? zoneList : [],
      checkAll: e.target.checked,
      indeterminate: false,
    });
  };

  // 新增一项 zone 优先级
  public handleTransfer = () => {
    const { checkedList, priorityList } = this.state;
    // 只有选中了 zone，才能允许新增 zone 优先级
    if (checkedList.length) {
      this.setState(
        {
          checkedList: [],
          checkAll: false,
          indeterminate: false,
        },

        // 更新组件值
        () => {
          this.handleChange(
            [...priorityList, this.sortByZoneList(checkedList).join(',')].join(';')
          );
        }
      );
    }
  };

  public getItemStyle = (isDragging, draggableStyle) => ({
    userSelect: 'none',
    backgroundColor: isDragging ? '#fafafa' : 'transparent',
    // 需要应用的拖拽样式
    ...draggableStyle,
  });

  // zone 优先级拖拽排序
  public onDragEnd = (result: any) => {
    const { priorityList } = this.state;
    const { source, destination } = result;
    // dropped outside the list
    if (!destination) {
      return;
    }
    const newPriorityList = this.reorder(priorityList, source.index, destination.index);
    this.handleChange(newPriorityList.join(';'));
  };

  public reorder = (list: any[], startIndex: number, endIndex: number) => {
    const result = Array.from(list);
    const [removed] = result.splice(startIndex, 1);
    result.splice(endIndex, 0, removed);
    return result;
  };

  // 删除一项 zone 优先级
  public handleDeletePriority = (priority: string) => {
    const { checkedList, priorityList } = this.state;
    this.setState(
      {
        checkAll: false,
        indeterminate: !!checkedList.length,
      },

      // 更新组件值
      () => {
        this.handleChange(priorityList.filter(item => item !== priority).join(';'));
      }
    );
  };

  public resetPrioritization = () => {
    this.setState(
      {
        zoneList: this.props.zoneList,
        priorityList: [],
      },

      () => {
        this.handleChange('');
      }
    );
  };

  public render() {
    const { loading = false } = this.props;
    const { zoneList, checkAll, indeterminate, checkedList, priorityList } = this.state;
    return (
      <div className={styles.container}>
        <Card
          className={styles.zone}
          size="small"
          type="inner"
          title={
            <span>
              <Checkbox
                checked={checkAll}
                indeterminate={indeterminate}
                onChange={this.handleCheckAllChange}
              >
                <span style={{ fontWeight: 'normal' }}>Zone</span>
              </Checkbox>
              <Text ellipsis={{ tooltip: true }} className={styles.description}>
                {formatMessage({
                  id: 'ocp-express.component.FormPrimaryZone.AfterYouSelectMultipleZones',
                  defaultMessage: '同时选择多个 Zone 穿梭到右侧后，可设置为同一优先级',
                })}
              </Text>
            </span>
          }
        >
          <Spin spinning={loading}>
            <Checkbox.Group value={checkedList} onChange={this.handleCheckboxGroupChange}>
              {/**
               * 栅格布局在内容为空时，高度会出现异常
               * **/}
              <Row gutter={[16, 16]} style={{ height: zoneList?.length > 0 ? 'auto' : 0 }}>
                {zoneList.map(item => (
                  <Col key={item} span={24}>
                    <Checkbox key={item} value={item}>
                      {item}
                    </Checkbox>
                  </Col>
                ))}
              </Row>
            </Checkbox.Group>
          </Spin>
        </Card>
        <span className={styles.transferWrapper}>
          <span
            className={classNames(styles.transfer, {
              [styles.active]: !!checkedList.length,
              [styles.disabled]: !checkedList.length,
            })}
            onClick={this.handleTransfer}
          >
            <RightOutlined className={styles.icon} />
          </span>
        </span>
        <Card
          className={styles.priorityList}
          size="small"
          type="inner"
          title={
            <span>
              <span style={{ fontWeight: 'normal' }}>
                {formatMessage({
                  id: 'ocp-express.component.FormPrimaryZone.PrioritySorting',
                  defaultMessage: '优先级排序',
                })}
              </span>
              <Tooltip
                placement="topLeft"
                title={formatMessage({
                  id: 'ocp-express.component.FormPrimaryZone.PriorityFromTopToBottom',
                  defaultMessage: '优先级从上到下代表从高到低，可拖拽排序',
                })}
              >
                <Text
                  ellipsis={true}
                  className={styles.description}
                  style={{ display: 'inline-block', width: isEnglish() ? '50%' : '60%' }}
                >
                  {formatMessage({
                    id: 'ocp-express.component.FormPrimaryZone.PriorityFromTopToBottom',
                    defaultMessage: '优先级从上到下代表从高到低，可拖拽排序',
                  })}
                </Text>
              </Tooltip>
              <a
                onClick={() => this.resetPrioritization()}
                style={{ float: 'right', fontWeight: 'normal' }}
              >
                {formatMessage({
                  id: 'ocp-express.component.FormPrimaryZone.RestoreToDefault',
                  defaultMessage: '恢复为默认',
                })}
              </a>
            </span>
          }
        >
          <DragDropContext onDragEnd={this.onDragEnd}>
            <Droppable droppableId="droppable">
              {({ droppableProps, innerRef }) => (
                <div {...droppableProps} ref={innerRef} className={styles.dragList}>
                  {priorityList.map((item, index) => (
                    <Draggable key={item} draggableId={item} index={index}>
                      {(
                        { draggableProps, dragHandleProps, innerRef: subInnerRef },
                        { isDragging }
                      ) => (
                        <div
                          {...draggableProps}
                          {...dragHandleProps}
                          ref={subInnerRef}
                          style={this.getItemStyle(isDragging, draggableProps.style)}
                          className={styles.dragItem}
                        >
                          <ContentWithIcon
                            content={item}
                            prefixIcon={{
                              component: DragIconSvg,
                              style: {
                                fontSize: 10,
                              },
                            }}
                          />

                          <DeleteOutlined
                            className="weak"
                            onClick={() => this.handleDeletePriority(item)}
                          />
                        </div>
                      )}
                    </Draggable>
                  ))}
                </div>
              )}
            </Droppable>
          </DragDropContext>
        </Card>
      </div>
    );
  }
}

export default FormPrimaryZone;
