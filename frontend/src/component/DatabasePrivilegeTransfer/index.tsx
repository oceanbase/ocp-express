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
import { Modal } from '@oceanbase/design';
import { Card, Checkbox, Col, Row, Spin, Button, Empty } from '@oceanbase/design';
import { RightOutlined } from '@ant-design/icons';
import classNames from 'classnames';
import MyInput from '@/component/MyInput';
import { differenceBy, findIndex, includes } from 'lodash';
import { FORBID_OPERATION_DBLIST } from '@/constant/tenant';
import PrivilegesCheckbox from './PrivilegesCheckbox';
import styles from './index.less';

const { confirm } = Modal;

export interface DatabasePrivilegeTransferProps {
  dbUserList?: API.DbPrivilege[];
  dbPrivilegedList?: API.DbPrivilege[];
  loading: boolean;
  onChange?: (value: API.DbPrivilege[]) => void;
}

interface DatabasePrivilegeTransferState {
  checkAll: boolean;
  checkedList: API.Database[];
  indeterminate: boolean;
  searchLeftKey: string;
  searchRightKey: string;
  privilegedList: API.DbPrivilege[];
  unPrivilegedList: API.Database[];
  dbPrivilegeData: API.DbPrivilege[];
}

class DatabasePrivilegeTransfer extends React.PureComponent<
  DatabasePrivilegeTransferProps,
  DatabasePrivilegeTransferState
> {
  constructor(props: DatabasePrivilegeTransferProps) {
    super(props);
    this.state = {
      checkAll: false,
      checkedList: [],
      indeterminate: false,
      searchLeftKey: '',
      searchRightKey: '',
      privilegedList: props.dbPrivilegedList || [],
      unPrivilegedList: props.dbUserList || [],
      dbPrivilegeData: props.dbPrivilegedList || [],
    };
  }

  // 全选/不选 DB
  handleCheckAllChange = (e: any) => {
    const { unPrivilegedList } = this.state;
    this.setState({
      checkedList: e.target.checked ? unPrivilegedList : [],
      checkAll: e.target.checked,
      indeterminate: false,
    });
  };

  handleCheckboxGroupChange = (checkedData: API.DbPrivilege[]) => {
    const { unPrivilegedList } = this.state;
    this.setState({
      checkedList: checkedData,
      checkAll: checkedData.length === unPrivilegedList.length,
      indeterminate: !!checkedData.length && checkedData.length < unPrivilegedList.length,
    });
  };

  handleTransfer = () => {
    const { checkedList, privilegedList, unPrivilegedList } = this.state;
    if (checkedList.length > 0) {
      const checkedDbList = checkedList.map(item => {
        return {
          dbName: item.dbName,
          privileges: [],
        };
      });
      this.setState({
        checkedList: [],
        checkAll: false,
        indeterminate: false,
        privilegedList: [...privilegedList, ...checkedDbList],
        unPrivilegedList: differenceBy(unPrivilegedList, checkedList, 'dbName'),
      });
    }
  };

  deleteAllAuthed = () => {
    const { dbUserList, dbPrivilegedList, onChange } = this.props;
    const removeList: API.DbPrivilege[] = [...(dbUserList || []), ...(dbPrivilegedList || [])];
    confirm({
      title: formatMessage({
        id: 'ocp-express.component.DatabasePrivilegeTransfer.AreYouSureYouWant',
        defaultMessage: '确定要移除全部已选择对象吗？',
      }),
      okText: formatMessage({
        id: 'ocp-express.component.DatabasePrivilegeTransfer.Determine',
        defaultMessage: '确定',
      }),
      cancelText: formatMessage({
        id: 'ocp-express.component.DatabasePrivilegeTransfer.Cancel',
        defaultMessage: '取消',
      }),
      onOk: () => {
        this.setState({
          unPrivilegedList: removeList,
          dbPrivilegeData: [],
          privilegedList: [],
        });

        if (onChange) {
          onChange([]);
        }
      },
    });
  };

  // 左侧搜索
  setUnPrivilegedList = (key: string) => {
    this.setState({
      searchLeftKey: key,
    });
  };

  // 右侧搜索
  searchPrivilegedList = (key: string) => {
    this.setState({
      searchRightKey: key,
    });
  };

  // 删除一项 DB 权限
  handleDeleteAuth = (dbName: string) => {
    const { dbPrivilegedList, dbUserList, onChange } = this.props;
    const { checkedList, unPrivilegedList, privilegedList, dbPrivilegeData } = this.state;
    const privilegedListFilter: API.DbPrivilege[] = privilegedList.filter(
      item => item.dbName !== dbName
    );

    // 收集到当前授权的数据库
    const dbPrivilegesData: API.DbPrivilege[] = dbPrivilegeData?.filter(
      item => item.dbName !== dbName
    );

    const index = findIndex(dbPrivilegedList, o => o.dbName === dbName);
    const count = findIndex(dbUserList, o => o.dbName === dbName);
    const deleteDbPrivilegeParam = [];
    // 匹配查找到移除的权限，整理后放入待添加的数据库列表
    if (index !== -1) {
      deleteDbPrivilegeParam.push(dbPrivilegedList[index]);
    } else if (count !== -1) {
      deleteDbPrivilegeParam.push(dbUserList[count]);
    } else {
      deleteDbPrivilegeParam.push({
        dbName,
        privileges: [],
      });
    }
    this.setState({
      checkAll: false,
      indeterminate: !!checkedList?.length,
      // 将被删除的放回未授权数组
      unPrivilegedList: [...unPrivilegedList, ...deleteDbPrivilegeParam],
      privilegedList: privilegedListFilter,
      dbPrivilegeData: dbPrivilegesData,
    });

    if (onChange) {
      onChange(
        dbPrivilegeData?.map(item => ({
          dbName: item?.dbName,
          privileges: item.dbName === dbName ? [] : item.privileges,
        }))
      );
    }
  };

  public updateDbPrivilege = (dbPrivilege: API.DbPrivilege) => {
    const { dbPrivilegeData, privilegedList } = this.state;
    const privilegedIndex = findIndex(privilegedList, o => o.dbName === dbPrivilege.dbName);
    if (privilegedIndex !== -1) {
      this.setState({
        privilegedList: privilegedList?.map(item => ({
          dbName: item?.dbName,
          privileges:
            item?.dbName === dbPrivilege?.dbName ? dbPrivilege.privileges : item.privileges,
        })),
      });
    } else {
      this.setState({
        privilegedList,
      });
    }
    // 收集到当前授权的数据库
    const dbPrivilegeIndex = findIndex(dbPrivilegeData, o => o.dbName === dbPrivilege.dbName);
    if (dbPrivilegeIndex !== -1) {
      const dbPrivileges = dbPrivilegeData?.map(item => ({
        dbName: item?.dbName,
        privileges: item?.dbName === dbPrivilege?.dbName ? dbPrivilege.privileges : item.privileges,
      }));

      this.setState({
        dbPrivilegeData: dbPrivileges,
      });

      this.props.onChange(dbPrivileges);
    } else {
      this.setState({
        dbPrivilegeData: [...dbPrivilegeData, dbPrivilege],
        privilegedList,
      });

      this.props.onChange([...dbPrivilegeData, dbPrivilege]);
    }
  };

  public render() {
    const { loading } = this.props;
    const {
      searchLeftKey,
      searchRightKey,
      checkAll,
      indeterminate,
      unPrivilegedList,
      checkedList,
      privilegedList,
    } = this.state;

    return (
      <div className={styles.container}>
        <Card
          className={styles.db}
          size="small"
          type="inner"
          title={
            <span>
              <Checkbox
                checked={checkAll}
                style={{ color: '#000', fontWeight: 400 }}
                indeterminate={indeterminate}
                onChange={this.handleCheckAllChange}
              >
                {formatMessage({
                  id: 'ocp-express.component.DatabasePrivilegeTransfer.Unauthorized',
                  defaultMessage: '未授权',
                })}
              </Checkbox>
              <span className={styles.description}>
                <span>{unPrivilegedList.length}</span>
                {formatMessage({
                  id: 'ocp-express.component.DatabasePrivilegeTransfer.Item',
                  defaultMessage: '项',
                })}
              </span>
            </span>
          }
        >
          <Spin spinning={loading}>
            {/* Search 组件需要用 div 组件包裹，因为 Search 组件是 inline-block，当授权所有数据库权限时，Search 会滑落到卡片底部，需要包裹一层 block 组件 */}
            <div>
              <MyInput.Search
                allowClear={true}
                onSearch={(value: string) => this.setUnPrivilegedList(value)}
                placeholder={formatMessage({
                  id: 'ocp-express.component.DatabasePrivilegeTransfer.Enter',
                  defaultMessage: '请输入',
                })}
              />
            </div>
            <Checkbox.Group
              value={checkedList}
              onChange={this.handleCheckboxGroupChange}
              style={{
                marginTop: 10,
                // 与右侧卡片内容区的高度要一致
                height: 340,
                overflow: 'auto',
              }}
            >
              <Row style={{ display: 'block' }}>
                {unPrivilegedList
                  ?.filter(o => includes(o.dbName, searchLeftKey))
                  ?.map(item => {
                    if (item.dbName !== 'information_schema') {
                      return (
                        <Col key={item.dbName} span={24} style={{ lineHeight: '24px' }}>
                          <Checkbox key={item.dbName} value={item}>
                            {item.dbName}
                          </Checkbox>
                        </Col>
                      );
                    }
                  })}
              </Row>
            </Checkbox.Group>
          </Spin>
        </Card>
        <span className={styles.transferWrapper}>
          <span
            className={classNames(styles.transfer, {
              [styles.active]: !!checkedList?.length,
              [styles.disabled]: !checkedList?.length,
            })}
            onClick={this.handleTransfer}
          >
            <RightOutlined className={styles.icon} />
          </span>
        </span>
        <Card
          className={styles.privilegedListContainer}
          size="small"
          type="inner"
          title={
            <span>
              <span style={{ marginRight: 8, color: '#000', fontWeight: 400 }}>
                {formatMessage({
                  id: 'ocp-express.component.DatabasePrivilegeTransfer.Authorized',
                  defaultMessage: '已授权',
                })}
              </span>
              <span className={styles.description}>
                <span>{privilegedList.length}</span>
                {formatMessage({
                  id: 'ocp-express.component.DatabasePrivilegeTransfer.Item',
                  defaultMessage: '项',
                })}
              </span>
              {privilegedList.length !== 0 && (
                <Button
                  type="link"
                  style={{ float: 'right', height: 22, padding: 0 }}
                  onClick={this.deleteAllAuthed}
                >
                  {formatMessage({
                    id: 'ocp-express.component.DatabasePrivilegeTransfer.RemoveAll',
                    defaultMessage: '移除全部',
                  })}
                </Button>
              )}
            </span>
          }
        >
          {privilegedList.length === 0 ? (
            <Empty
              description={formatMessage({
                id: 'ocp-express.component.DatabasePrivilegeTransfer.NoAuthorizedAccountIsAvailable',
                defaultMessage: '暂无已授权账号',
              })}
            />
          ) : (
            <>
              <MyInput.Search
                allowClear={true}
                onSearch={(value: string) => this.searchPrivilegedList(value)}
                placeholder={formatMessage({
                  id: 'ocp-express.component.DatabasePrivilegeTransfer.Enter',
                  defaultMessage: '请输入',
                })}
              />

              <div className={styles.privilegedList}>
                {privilegedList
                  .filter(o => o.dbName.indexOf(searchRightKey) > -1)
                  ?.map(item => (
                    <PrivilegesCheckbox
                      key={item.dbName}
                      dbPrivilegeParam={item}
                      insideDb={FORBID_OPERATION_DBLIST.includes(item?.dbName)}
                      updateDbPrivilege={this.updateDbPrivilege}
                      deleteFn={this.handleDeleteAuth}
                    />
                  ))}
              </div>
            </>
          )}
        </Card>
      </div>
    );
  }
}

export default DatabasePrivilegeTransfer;
