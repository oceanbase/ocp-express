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

/** 此处后端没有提供注释 GET /api/v1/config/properties */
export async function findNonFatalProperties(
  params: {
    // query
    keyLike?: string;
    pageable?: API.Pageable;
  },
  options?: { [key: string]: any },
) {
  return request<API.PaginatedResponse_PropertyMeta_>('/api/v1/config/properties', {
    method: 'GET',
    params: {
      ...params,
      pageable: undefined,
      ...params['pageable'],
    },
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /api/v1/config/systemInfo */
export async function getSystemInfo(options?: { [key: string]: any }) {
  return request<API.SuccessResponse_SystemInfo_>('/api/v1/config/systemInfo', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /api/v1/config/refresh */
export async function refreshProperties(options?: { [key: string]: any }) {
  return request<API.NoDataResponse>('/api/v1/config/refresh', {
    method: 'POST',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 PUT /api/v1/config/properties/${param0} */
export async function updateProperty(
  params: {
    // query
    newValue?: string;
    // path
    id?: number;
  },
  options?: { [key: string]: any },
) {
  const { id: param0, ...queryParams } = params;
  return request<API.SuccessResponse_PropertyMeta_>(`/api/v1/config/properties/${param0}`, {
    method: 'PUT',
    params: {
      ...queryParams,
    },
    ...(options || {}),
  });
}
