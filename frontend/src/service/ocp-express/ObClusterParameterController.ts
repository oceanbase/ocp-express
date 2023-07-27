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

/** 此处后端没有提供注释 GET /api/v1/ob/cluster/parameters */
export async function listClusterParameters(options?: { [key: string]: any }) {
  return request<API.IterableResponse_ClusterParameter_>('/api/v1/ob/cluster/parameters', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 PATCH /api/v1/ob/cluster/parameters */
export async function updateClusterParameter(
  body?: Array<API.UpdateClusterParameterParam>,
  options?: { [key: string]: any },
) {
  return request<API.NoDataResponse>('/api/v1/ob/cluster/parameters', {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
