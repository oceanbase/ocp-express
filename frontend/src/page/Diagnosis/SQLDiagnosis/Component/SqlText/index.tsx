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
import * as ObSqlStatController from '@/service/ocp-express/ObSqlStatController';
import { formatSql } from '@/util';
import { Spin, Tooltip, Typography, theme } from '@oceanbase/design';
import React, { useState } from 'react';
import { useRequest } from 'ahooks';

interface IProps {
  node?: any;
  title?: string;
  sqlId?: string;
  tenantId?: number;
  dbName?: string;
  sqlText?: string;
  // 限制时间段为了提高查询速度
  startTime?: string;
  endTime?: string;
  copyable?: boolean;
  record: API.SqlAuditStatSummary;
  onClick: (record: API.SqlAuditStatSummary) => void;
  requestInner?: boolean;
  canJump?: boolean;
  isLimit?: boolean;
}

export const SqlText = ({
  startTime,
  endTime,
  node,
  sqlId,
  dbName,
  tenantId,
  onClick,
  sqlText: outerSqltext,
  record,
  title,
  copyable,
  // 是否内部请求数据，默认为 true
  requestInner = true,
  canJump = true,
  isLimit = false,
}: IProps) => {
  const { token } = theme.useToken();

  const [sqlText, setSqlText] = useState(record?.isCompleted ? record?.sqlTextShort : '');

  const { run: getSqlText } = useRequest(ObSqlStatController.sqlText, {
    manual: true,
    onSuccess: res => {
      if (res.successful) {
        setSqlText(res.data?.fulltext as string);
      }
    },
  });

  const TyprographyEle = canJump ? Typography.Link : Typography.Text;
  const realSqlText = outerSqltext || sqlText;

  const contentWidth = true ? 'calc(100% - 70px)' : 'calc(100% - 40px)';

  return (
    <>
      <TyprographyEle
        copyable={
          copyable
            ? { text: title ? `${title}\n` + formatSql(realSqlText) : formatSql(realSqlText) }
            : false
        }
        onClick={e => {
          // 点击 SPAN 标签时才进行跳转
          if (e?.target?.tagName === 'SPAN') {
            onClick?.(record);
          }
          // 存在标题时，点击标题也进行跳转
          if (title && e?.target?.tagName === 'DIV') {
            onClick?.(record);
          }
        }}
      >
        {/* 为了解决 hover copy 按钮时不显示 tooltip 内容, 所以将 Tootlip 包裹在 Link 中, 自定义实现了 ellipsis  */}
        <Tooltip
          placement="topLeft"
          title={
            realSqlText || title ? (
              <>
                {title && <div>{title}</div>}
                {realSqlText && <div>{formatSql(realSqlText)}</div>}
              </>
            ) : (
              <Spin />
            )
          }
          overlayStyle={{
            maxWidth: 520,
          }}
        >
          <span
            onMouseEnter={() => {
              // SQL 文本存在时，不再发起请求
              if (!sqlText && requestInner) {
                getSqlText({
                  tenantId,
                  sqlId,
                  startTime,
                  endTime,
                  dbName,
                });
              }
            }}
          >
            {/* 如果不存在 node，那么将只显示 title，title 为了保持和复制按钮同行，所以使用 span 标签 */}
            {title &&
              (node ? (
                <div
                  className="ellipsis"
                  style={{
                    width: 'calc(100%)',
                  }}
                >
                  {title}
                </div>
              ) : (
                <span
                  className="ellipsis"
                  style={{
                    maxWidth: contentWidth,
                    display: 'inline-block',
                    verticalAlign: 'bottom',
                  }}
                >
                  {title}
                </span>
              ))}

            {/* verticalAlign 为了解决和 copy 图标对不齐的问题  */}
            {node && (
              <span
                style={{
                  maxWidth: contentWidth,
                  display: 'inline-block',
                  verticalAlign: 'bottom',
                }}
                className="ellipsis"
              >
                {node}
              </span>
            )}
          </span>
        </Tooltip>
      </TyprographyEle>
      {isLimit && (
        <span
          style={{
            marginLeft: 8,
            display: 'inline-block',
            backgroundColor: '#9254DE',
            color: token.colorTextLightSolid,
            width: '18px',
            height: '18px',
            fontSize: '12px',
            borderRadius: '10px',
            textAlign: 'center',
          }}
        >
          {formatMessage({ id: 'ocp-express.Component.SqlText.Limit', defaultMessage: '限' })}
        </span>
      )}
    </>
  );
};
