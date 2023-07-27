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

/** 此处后端没有提供注释 PUT /api/v1/profiles/me/changePassword */
export async function changePassword(
  body?: API.ChangePasswordRequest,
  options?: { [key: string]: any },
) {
  return request<API.SuccessResponse_Map_String_String__>('/api/v1/profiles/me/changePassword', {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /api/v1/profiles/me */
export async function userInfo(options?: { [key: string]: any }) {
  return request<API.SuccessResponse_AuthenticatedUser_>('/api/v1/profiles/me', {
    method: 'GET',
    ...(options || {}),
  });
}
