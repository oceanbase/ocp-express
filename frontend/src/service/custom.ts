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

import request from '@/util/request';

/** 登录接口由于并非是后端显式定义的，因此 OneAPI 无法解析生成，需要手动定义 service 函数 */
export async function login(params: {
  // 用户名
  username: string;
  // 密码
  password: string;
}): Promise<any> {
  return request('/api/v1/login', {
    method: 'POST',
    requestType: 'form',
    data: params,
  });
}

/** 退出登录接口由于并非是后端显式定义的，因此 OneAPI 无法解析生成，需要手动定义 service 函数 */
export async function logout(): Promise<any> {
  return request('/api/v1/logout', {
    method: 'POST',
  });
}

export async function getSqlTraceTemplate() {
  return request<Blob>('/templates/sqlTraceReportTemplate.html', {
    method: 'GET',
    responseType: 'blob',
  });
}

export async function getInspectionTemplate() {
  return request<Blob>('/templates/inspectionReportTemplate.html', {
    method: 'GET',
    responseType: 'blob',
  });
}

// 由于OneApi生成的service函数，FormData中不含参数
// 密码箱导入预检查
export async function importCredentialPreCheck(
  params: {
    // query
    secret?: string;
  },
  files?: File[],
  options?: Record<string, any>
) {
  const formData = new FormData();
  if (files) {
    formData.append('file', files || '');
  }
  if (params.secret) {
    formData.append('secret', params.secret || '');
  }
  return request<API.SuccessResponse_CredentialValidateResult_>(
    '/api/v1/profiles/me/credentials/importPreCheck',
    {
      method: 'POST',
      params: {
        // ...params,
      },
      data: formData,
      ...(options || {}),
    }
  );
}

// 密码箱导入
export async function importCredential(
  params: {
    // query
    secret?: string;
    numbers?: number[];
  },
  files?: File[],
  options?: Record<string, any>
) {
  const formData = new FormData();
  if (files) {
    formData.append('file', files || '');
  }
  if (params.secret) {
    formData.append('secret', params.secret || '');
  }
  if (params.numbers) {
    formData.append('numbers', params.numbers || []);
  }
  return request<API.SuccessResponse_CredentialValidateResult_>(
    '/api/v1/profiles/me/credentials/import',
    {
      method: 'POST',
      params: {
        // ...params,
      },
      data: formData,
      ...(options || {}),
    }
  );
}
