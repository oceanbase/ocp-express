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

/** 此处后端没有提供注释 POST /api/v1/ob/tenants/${param0}/clearCompactionError */
export async function clearCompactionError(
  params: {
    // path
    tenantId?: number;
  },
  options?: { [key: string]: any },
) {
  const { tenantId: param0 } = params;
  return request<API.NoDataResponse>(`/api/v1/ob/tenants/${param0}/clearCompactionError`, {
    method: 'POST',
    params: { ...params },
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /api/v1/ob/tenants/${param0}/compaction */
export async function getTenantCompaction(
  params: {
    // path
    tenantId?: number;
  },
  options?: { [key: string]: any },
) {
  const { tenantId: param0 } = params;
  return request<API.SuccessResponse_TenantCompaction_>(`/api/v1/ob/tenants/${param0}/compaction`, {
    method: 'GET',
    params: { ...params },
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /api/v1/ob/tenants/topCompactions */
export async function topCompactions(
  params: {
    // query
    top?: number;
    times?: number;
  },
  options?: { [key: string]: any },
) {
  return request<API.IterableResponse_TenantCompactionHistory_>(
    '/api/v1/ob/tenants/topCompactions',
    {
      method: 'GET',
      params: {
        ...params,
      },
      ...(options || {}),
    },
  );
}

/** 此处后端没有提供注释 POST /api/v1/ob/tenants/${param0}/triggerCompaction */
export async function triggerTenantCompaction(
  params: {
    // path
    tenantId?: number;
  },
  options?: { [key: string]: any },
) {
  const { tenantId: param0 } = params;
  return request<API.NoDataResponse>(`/api/v1/ob/tenants/${param0}/triggerCompaction`, {
    method: 'POST',
    params: { ...params },
    ...(options || {}),
  });
}
