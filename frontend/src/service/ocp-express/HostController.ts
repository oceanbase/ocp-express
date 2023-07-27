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

/** 此处后端没有提供注释 POST /api/v1/hosts/logs/download */
export async function downloadLog(body?: API.DownloadLogRequest, options?: { [key: string]: any }) {
  return request<API.ResponseEntity_Resource_>('/api/v1/hosts/logs/download', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /api/v1/obagent */
export async function getAgentDetail(
  params: {
    // query
    ip?: string;
    obSvrPort?: number;
  },
  options?: { [key: string]: any },
) {
  return request<API.SuccessResponse_ObAgentDetail_>('/api/v1/obagent', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /api/v1/hosts */
export async function getHostInfo(
  params: {
    // query
    ip?: string;
    obSvrPort?: number;
  },
  options?: { [key: string]: any },
) {
  return request<API.SuccessResponse_HostInfo_>('/api/v1/hosts', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /api/v1/hosts/logs/query */
export async function queryLog(body?: API.QueryLogRequest, options?: { [key: string]: any }) {
  return request<API.SuccessResponse_QueryLogResult_>('/api/v1/hosts/logs/query', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /api/v1/obagent/restart */
export async function restartHostAgent(
  params: {
    // query
    ip?: string;
    obSvrPort?: number;
  },
  options?: { [key: string]: any },
) {
  return request<API.SuccessResponse_TaskInstance_>('/api/v1/obagent/restart', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}
