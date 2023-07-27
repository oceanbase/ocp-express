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

/** 此处后端没有提供注释 GET /api/v1/ob/cluster */
export async function getClusterInfo(options?: { [key: string]: any }) {
  return request<API.SuccessResponse_ClusterInfo_>('/api/v1/ob/cluster', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /api/v1/ob/cluster/unitSpecLimit */
export async function getClusterUnitSpecLimit(options?: { [key: string]: any }) {
  return request<API.SuccessResponse_ClusterUnitSpecLimit_>('/api/v1/ob/cluster/unitSpecLimit', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /api/v1/ob/cluster/server */
export async function getServerInfo(
  params: {
    // query
    ip?: string;
    obSvrPort?: number;
  },
  options?: { [key: string]: any },
) {
  return request<API.SuccessResponse_Server_>('/api/v1/ob/cluster/server', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /api/v1/ob/cluster/init */
export async function initObCluster(body?: API.ClusterInitParam, options?: { [key: string]: any }) {
  return request<API.SuccessResponse_BasicCluster_>('/api/v1/ob/cluster/init', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /api/v1/ob/cluster/charsets */
export async function listCharsets(
  params: {
    // query
    /** Tenant compatible mode. */
    tenantMode?: API.TenantMode;
  },
  options?: { [key: string]: any },
) {
  return request<API.IterableResponse_Charset_>('/api/v1/ob/cluster/charsets', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
