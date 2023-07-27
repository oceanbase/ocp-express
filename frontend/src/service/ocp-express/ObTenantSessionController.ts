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

/** 此处后端没有提供注释 POST /api/v1/ob/tenants/${param0}/sessions/closeQuery */
export async function closeTenantQuery(
  params: {
    // path
    tenantId?: number;
  },
  body?: API.CloseSessionParam,
  options?: { [key: string]: any },
) {
  const { tenantId: param0 } = params;
  return request<API.NoDataResponse>(`/api/v1/ob/tenants/${param0}/sessions/closeQuery`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: { ...params },
    data: body,
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /api/v1/ob/tenants/${param0}/sessions/close */
export async function closeTenantSession(
  params: {
    // path
    tenantId?: number;
  },
  body?: API.CloseSessionParam,
  options?: { [key: string]: any },
) {
  const { tenantId: param0 } = params;
  return request<API.NoDataResponse>(`/api/v1/ob/tenants/${param0}/sessions/close`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: { ...params },
    data: body,
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /api/v1/ob/tenants/${param0}/sessions/stats */
export async function getSessionStats(
  params: {
    // path
    tenantId?: number;
  },
  options?: { [key: string]: any },
) {
  const { tenantId: param0 } = params;
  return request<API.SuccessResponse_SessionStats_>(`/api/v1/ob/tenants/${param0}/sessions/stats`, {
    method: 'GET',
    params: { ...params },
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /api/v1/ob/tenants/${param0}/sessions/${param1} */
export async function getTenantSession(
  params: {
    // path
    tenantId?: number;
    sessionId?: number;
  },
  options?: { [key: string]: any },
) {
  const { tenantId: param0, sessionId: param1 } = params;
  return request<API.SuccessResponse_TenantSession_>(
    `/api/v1/ob/tenants/${param0}/sessions/${param1}`,
    {
      method: 'GET',
      params: { ...params },
      ...(options || {}),
    },
  );
}

/** 此处后端没有提供注释 GET /api/v1/ob/tenants/${param0}/sessions */
export async function listTenantSessions(
  params: {
    // query
    pageable?: API.Pageable;
    dbUser?: string;
    dbName?: string;
    clientIp?: string;
    activeOnly?: boolean;
    // path
    tenantId?: number;
  },
  options?: { [key: string]: any },
) {
  const { tenantId: param0, ...queryParams } = params;
  return request<API.PaginatedResponse_TenantSession_>(`/api/v1/ob/tenants/${param0}/sessions`, {
    method: 'GET',
    params: {
      ...queryParams,
      pageable: undefined,
      ...queryParams['pageable'],
    },
    ...(options || {}),
  });
}
