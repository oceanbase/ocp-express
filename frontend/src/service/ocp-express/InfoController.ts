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

/** 此处后端没有提供注释 GET /api/v1/druid/stat */
export async function druidStat(options?: { [key: string]: any }) {
  return request<API.OneApiResult_object_>('/api/v1/druid/stat', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /api/v1/git-info */
export async function gitInfo(options?: { [key: string]: any }) {
  return request<API.Map_String_Object_>('/api/v1/git-info', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /api/v1/ */
export async function home(options?: { [key: string]: any }) {
  return request<API.OneApiResult_string_>('/api/v1/', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /api/v1/info */
export async function info(options?: { [key: string]: any }) {
  return request<API.Map_String_Object_>('/api/v1/info', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /api/v1/status */
export async function status(options?: { [key: string]: any }) {
  return request<API.Map_String_Object_>('/api/v1/status', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 此处后端没有提供注释 GET /api/v1/time */
export async function time(options?: { [key: string]: any }) {
  return request<API.OneApiResult_offsetdatetime_>('/api/v1/time', {
    method: 'GET',
    ...(options || {}),
  });
}
