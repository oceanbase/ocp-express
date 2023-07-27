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
import classNames from 'classnames';
import styles from './index.less';

export interface BatchOperationBarProps {
  className?: string;
  style?: React.CSSProperties;
  size?: 'small' | 'default' | 'large';
  description?: React.ReactNode;
  selectedCount?: number;
  visible?: boolean;
  actions?: React.ReactNode[];
  onCancel?: () => void;
  selectedText?: string;
  cancelText?: string;
}

const BatchOperationBar: React.FC<BatchOperationBarProps> = ({
  description,
  className,
  style,
  actions = [],
  selectedCount = 0,
  visible,
  onCancel,
  size = 'default',
}) => {
  const realVisible = visible === undefined ? selectedCount > 0 : visible;
  return (
    <div
      style={{
        display: realVisible ? 'flex' : 'none',
        ...style,
      }}
      className={classNames(styles.container, styles[size], className)}
    >
      <div className={styles.left}>
        <div className={styles.title}>
          {formatMessage(
            {
              id: 'ocp-express.component.BatchOperationBar.SelectedcountSelected',
              defaultMessage: '已选 {selectedCount} 项',
            },
            { selectedCount }
          )}
          {onCancel && (
            <span className={styles.cancel} onClick={onCancel}>
              {formatMessage({
                id: 'ocp-express.component.BatchOperationBar.Deselect',
                defaultMessage: '取消选择',
              })}
            </span>
          )}
        </div>
        {description && <div className={styles.subtitle}>{description}</div>}
      </div>
      <div className={styles.right}>
        {actions.map((action, index) => {
          return (
            <div key={index} className={styles.actionItem}>
              {action}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default BatchOperationBar;
