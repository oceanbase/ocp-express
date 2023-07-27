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

/** 获取用户登录相关的 key，此接口不在登录验证范围内，且当前返回的是公钥，此公钥可以通过配置管理来修改.
@return 登录相关 key 信息
 GET /api/v1/loginKey */
export async function getLoginKey(options?: { [key: string]: any }) {
  return request<API.SuccessResponse_LoginKey_>('/api/v1/loginKey', {
    method: 'GET',
    ...(options || {}),
  });
}
