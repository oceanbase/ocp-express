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

/** 此处后端没有提供注释 GET /error */
export async function handleError(options?: { [key: string]: any }) {
  return request<API.ModelAndView>('/error', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 DELETE /error */
export async function handleErrorUsingDELETE(options?: { [key: string]: any }) {
  return request<API.ModelAndView>('/error', {
    method: 'DELETE',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 HEAD /error */
export async function handleErrorUsingHEAD(options?: { [key: string]: any }) {
  return request<API.ModelAndView>('/error', {
    method: 'HEAD',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 PATCH /error */
export async function handleErrorUsingPATCH(options?: { [key: string]: any }) {
  return request<API.ModelAndView>('/error', {
    method: 'PATCH',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /error */
export async function handleErrorUsingPOST(options?: { [key: string]: any }) {
  return request<API.ModelAndView>('/error', {
    method: 'POST',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 PUT /error */
export async function handleErrorUsingPUT(options?: { [key: string]: any }) {
  return request<API.ModelAndView>('/error', {
    method: 'PUT',
    ...(options || {}),
  });
}
