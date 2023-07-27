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

/* eslint-disable */
// 该文件由 OneAPI 自动生成，请勿手动修改！
import request from '@/util/request';

/** 此处后端没有提供注释 POST /api/v1/ob/tenants/${param0}/outlines */
export async function batchCreateOutline(
  params: {
    // path
    tenantId?: number;
  },
  body?: API.BatchConcurrentLimitRequest,
  options?: { [key: string]: any },
) {
  const { tenantId: param0 } = params;
  return request<API.SuccessResponse_BatchConcurrentLimitResult_>(
    `/api/v1/ob/tenants/${param0}/outlines`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      params: { ...params },
      data: body,
      ...(options || {}),
    },
  );
}

/** 此处后端没有提供注释 POST /api/v1/ob/tenants/${param0}/outlines/batchDrop */
export async function batchDropSqlOutline(
  params: {
    // path
    tenantId?: number;
  },
  body?: API.BatchDropOutlineRequest,
  options?: { [key: string]: any },
) {
  const { tenantId: param0 } = params;
  return request<API.SuccessResponse_BatchDropOutlineResult_>(
    `/api/v1/ob/tenants/${param0}/outlines/batchDrop`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      params: { ...params },
      data: body,
      ...(options || {}),
    },
  );
}

/** 此处后端没有提供注释 GET /api/v1/ob/tenants/${param0}/outlines */
export async function getSqlOutline(
  params: {
    // query
    dbName?: string;
    sqlId?: string;
    startTime?: string;
    endTime?: string;
    attachPerfData?: boolean;
    // path
    tenantId?: number;
  },
  options?: { [key: string]: any },
) {
  const { tenantId: param0, ...queryParams } = params;
  return request<API.IterableResponse_Outline_>(`/api/v1/ob/tenants/${param0}/outlines`, {
    method: 'GET',
    params: {
      ...queryParams,
    },
    ...(options || {}),
  });
}
