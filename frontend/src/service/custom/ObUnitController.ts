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

/** 此处后端没有提供注释 POST /api/v2/ob/clusters/${param0}/units/${param1}/migrate */
export async function migrateUnit(
  params: {
    // path
    id?: number;
    obUnitId?: number;
  },
  body?: API.ComOceanbaseOcpObopsClusterParamMigrateUnitParam,
  options?: { [key: string]: any }
) {
  const { id: param0, obUnitId: param1 } = params;
  return request<API.ComOceanbaseOcpCoreResponseNoDataResponse>(
    `/api/v2/ob/clusters/${param0}/units/${param1}/migrate`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      params: { ...params },
      data: body,
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 POST /api/v2/ob/clusters/${param0}/units/${param1}/rollbackMigration */
export async function rollbackMigrateUnit(
  params: {
    // path
    id?: number;
    obUnitId?: number;
  },
  options?: { [key: string]: any }
) {
  const { id: param0, obUnitId: param1 } = params;
  return request<API.ComOceanbaseOcpCoreResponseNoDataResponse>(
    `/api/v2/ob/clusters/${param0}/units/${param1}/rollbackMigration`,
    {
      method: 'POST',
      params: { ...params },
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 POST /api/v2/ob/clusters/${param0}/units/tryDeleteUnusedUnit */
export async function tryDeleteUnusedUnit(
  params: {
    // path
    id?: number;
  },
  options?: { [key: string]: any }
) {
  const { id: param0 } = params;
  return request<API.ComOceanbaseOcpCoreResponseNoDataResponse>(
    `/api/v2/ob/clusters/${param0}/units/tryDeleteUnusedUnit`,
    {
      method: 'POST',
      params: { ...params },
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/v2/ob/clusters/${param0}/units/${param1} */
export async function getUnitStats(
  params: {
    // query
    serverIp: string;
    // path
    id?: number;
    obUnitId: number;
  },
  options?: { [key: string]: any }
) {
  const { id: param0, obUnitId: param1, ...queryParams } = params;
  return request<API.ComOceanbaseOcpCoreResponseSuccessResponse_ComOceanbaseOcpObopsClusterModelClusterUnitViewOfUnit_>(
    `/api/v2/ob/clusters/${param0}/units/${param1}`,
    {
      method: 'GET',
      params: {
        ...queryParams,
      },
      ...(options || {}),
    }
  );
}

/** 此处后端没有提供注释 GET /api/v2/ob/clusters/${param0}/units/${param1}/migrateDestinations */
export async function getUnitMigrateDestination(
  params: {
    // path
    id?: number;
    obUnitId?: number;
  },
  options?: { [key: string]: any }
) {
  const { id: param0, obUnitId: param1 } = params;
  return request<API.ComOceanbaseOcpCoreResponseSuccessResponse_ComOceanbaseOcpObopsClusterModelUnitMigrateDestination_>(
    `/api/v2/ob/clusters/${param0}/units/${param1}/migrateDestinations`,
    {
      method: 'GET',
      params: { ...params },
      ...(options || {}),
    }
  );
}
