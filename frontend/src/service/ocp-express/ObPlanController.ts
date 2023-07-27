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

/** 查询Plan的计划
@param tenantId 租户Id
@param planUid 计划的唯一Id
@param startTime 期间开始时间
@param endTime 期间结束时间
 GET /api/v1/ob/tenants/${param0}/plans/${param1}/explain */
export async function planExplain(
  params: {
    // query
    /** 期间开始时间 */
    startTime?: string;
    /** 期间结束时间 */
    endTime?: string;
    // path
    /** 租户Id */
    tenantId?: number;
    /** 计划的唯一Id */
    planUid?: string;
  },
  options?: { [key: string]: any },
) {
  const { tenantId: param0, planUid: param1, ...queryParams } = params;
  return request<API.SuccessResponse_PlanExplain_>(
    `/api/v1/ob/tenants/${param0}/plans/${param1}/explain`,
    {
      method: 'GET',
      params: {
        ...queryParams,
      },
      ...(options || {}),
    },
  );
}

/** 查询SQL的TopPlan， 按plan hash聚合
@param tenantId 租户Id
@param sqlId SQL_ID
@param startTime 期间开始时间
@param endTime 期间结束时间
@return
 GET /api/v1/ob/tenants/${param0}/sqls/${param1}/topPlanGroup */
export async function topPlanGroup(
  params: {
    // query
    dbName?: string;
    /** 期间开始时间 */
    startTime?: string;
    /** 期间结束时间 */
    endTime?: string;
    // path
    /** 租户Id */
    tenantId?: number;
    /** SQL_ID */
    sqlId?: string;
  },
  options?: { [key: string]: any },
) {
  const { tenantId: param0, sqlId: param1, ...queryParams } = params;
  return request<API.IterableResponse_PlanStatGroup_>(
    `/api/v1/ob/tenants/${param0}/sqls/${param1}/topPlanGroup`,
    {
      method: 'GET',
      params: {
        ...queryParams,
      },
      ...(options || {}),
    },
  );
}
