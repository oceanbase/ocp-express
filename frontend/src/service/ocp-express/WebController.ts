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

/** 此处后端没有提供注释 GET / */
export async function index(options?: { [key: string]: any }) {
  return request<API.ModelAndView>('/', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 DELETE / */
export async function indexUsingDELETE(options?: { [key: string]: any }) {
  return request<API.ModelAndView>('/', {
    method: 'DELETE',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 HEAD / */
export async function indexUsingHEAD(options?: { [key: string]: any }) {
  return request<API.ModelAndView>('/', {
    method: 'HEAD',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 PATCH / */
export async function indexUsingPATCH(options?: { [key: string]: any }) {
  return request<API.ModelAndView>('/', {
    method: 'PATCH',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST / */
export async function indexUsingPOST(options?: { [key: string]: any }) {
  return request<API.ModelAndView>('/', {
    method: 'POST',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 PUT / */
export async function indexUsingPUT(options?: { [key: string]: any }) {
  return request<API.ModelAndView>('/', {
    method: 'PUT',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /login */
export async function login(options?: { [key: string]: any }) {
  return request<API.ModelAndView>('/login', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 DELETE /login */
export async function loginUsingDELETE(options?: { [key: string]: any }) {
  return request<API.ModelAndView>('/login', {
    method: 'DELETE',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 HEAD /login */
export async function loginUsingHEAD(options?: { [key: string]: any }) {
  return request<API.ModelAndView>('/login', {
    method: 'HEAD',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 PATCH /login */
export async function loginUsingPATCH(options?: { [key: string]: any }) {
  return request<API.ModelAndView>('/login', {
    method: 'PATCH',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 POST /login */
export async function loginUsingPOST(options?: { [key: string]: any }) {
  return request<API.ModelAndView>('/login', {
    method: 'POST',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 PUT /login */
export async function loginUsingPUT(options?: { [key: string]: any }) {
  return request<API.ModelAndView>('/login', {
    method: 'PUT',
    ...(options || {}),
  });
}
