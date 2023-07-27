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

/** 取消任务指定节点执行，只能取消执行中的节点.
@param taskInstanceId 任务实例 id
@param subtaskInstanceId 子任务实例id
 POST /api/v1/tasks/instances/${param0}/subtasks/${param1}/cancel */
export async function cancelSubtask(
  params: {
    // path
    /** 任务实例 id */
    taskInstanceId?: number;
    /** 子任务实例id */
    subtaskInstanceId?: number;
  },
  options?: { [key: string]: any },
) {
  const { taskInstanceId: param0, subtaskInstanceId: param1 } = params;
  return request<API.NoDataResponse>(
    `/api/v1/tasks/instances/${param0}/subtasks/${param1}/cancel`,
    {
      method: 'POST',
      params: { ...params },
      ...(options || {}),
    },
  );
}

/** 下载指定任务诊断信息.
@param taskInstanceId 任务实例 id
@return 任务诊断信息
@throws IOException 下载失败
 POST /api/v1/tasks/instances/${param0}/downloadDiagnosis */
export async function downloadTaskDiagnosis(
  params: {
    // path
    /** 任务实例 id */
    taskInstanceId?: number;
  },
  options?: { [key: string]: any },
) {
  const { taskInstanceId: param0 } = params;
  return request<API.ResponseEntity_Resource_>(
    `/api/v1/tasks/instances/${param0}/downloadDiagnosis`,
    {
      method: 'POST',
      params: { ...params },
      responseType: 'application/zip',
      ...(options || {}),
    },
  );
}

/** 获取任务某个节点详细信息.
@param taskInstanceId 任务实例 id
@param subtaskInstanceId 子任务实例id
@return 指定子任务的日志信息
 GET /api/v1/tasks/instances/${param0}/subtasks/${param1}/log */
export async function getSubtaskLog(
  params: {
    // path
    /** 任务实例 id */
    taskInstanceId?: number;
    /** 子任务实例id */
    subtaskInstanceId?: number;
  },
  options?: { [key: string]: any },
) {
  const { taskInstanceId: param0, subtaskInstanceId: param1 } = params;
  return request<API.SuccessResponse_SubtaskLog_>(
    `/api/v1/tasks/instances/${param0}/subtasks/${param1}/log`,
    {
      method: 'GET',
      params: { ...params },
      ...(options || {}),
    },
  );
}

/** 获取指定任务实例信息.
@param taskInstanceId 任务实例 id
@return 任务详细信息
 GET /api/v1/tasks/instances/${param0} */
export async function getTaskInstance(
  params: {
    // path
    /** 任务实例 id */
    taskInstanceId?: number;
  },
  options?: { [key: string]: any },
) {
  const { taskInstanceId: param0 } = params;
  return request<API.SuccessResponse_WrappedTaskInstance_>(`/api/v1/tasks/instances/${param0}`, {
    method: 'GET',
    params: { ...params },
    ...(options || {}),
  });
}

/** 查询任务创建者列表.
@param type 任务类型，可选参数，默认是 MANUAL
@return 任务创建者列表
 GET /api/v1/tasks/instances/creators */
export async function listCreatorInfo(
  params: {
    // query
    type?: API.TaskType;
  },
  options?: { [key: string]: any },
) {
  return request<API.IterableResponse_string_>('/api/v1/tasks/instances/creators', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 查询任务实例列表.
@param keyword 集群、任务名称关键字
@param pageable 分页信息
@param creators 任务创建者
@param status 任务执行状态
@param type 任务类型，可选参数，默认是 MANUAL
@param tenantName 租户名称
@return 任务执行实例列表
 GET /api/v1/tasks/instances */
export async function listTaskInstances(
  params: {
    // query
    /** 集群、任务名称关键字 */
    keyword?: string;
    /** 分页信息 */
    pageable?: API.Pageable;
    /** 任务创建者 */
    creator?: Array<string>;
    /** 任务执行状态 */
    status?: string;
    type?: API.TaskType;
    /** 租户名称 */
    tenantName?: string;
  },
  options?: { [key: string]: any },
) {
  return request<API.PaginatedResponse_BasicTaskInstance_>('/api/v1/tasks/instances', {
    method: 'GET',
    params: {
      ...params,
      pageable: undefined,
      ...params['pageable'],
    },
    ...(options || {}),
  });
}

/** 重新执行任务指定节点，只有当前节点是失败状态才能重新执行.
@param taskInstanceId 任务实例 id
@param subtaskInstanceId 子任务实例 id
@return 子任务实例信息
 POST /api/v1/tasks/instances/${param0}/subtasks/${param1}/retry */
export async function retrySubtask(
  params: {
    // path
    /** 任务实例 id */
    taskInstanceId?: number;
    /** 子任务实例 id */
    subtaskInstanceId?: number;
  },
  options?: { [key: string]: any },
) {
  const { taskInstanceId: param0, subtaskInstanceId: param1 } = params;
  return request<API.SuccessResponse_SubtaskInstance_>(
    `/api/v1/tasks/instances/${param0}/subtasks/${param1}/retry`,
    {
      method: 'POST',
      params: { ...params },
      ...(options || {}),
    },
  );
}

/** 触发失败任务的重新执行.
@param taskInstanceId 任务实例 id
@return 重试任务
 POST /api/v1/tasks/instances/${param0}/retry */
export async function retryTask(
  params: {
    // path
    /** 任务实例 id */
    taskInstanceId?: number;
  },
  options?: { [key: string]: any },
) {
  const { taskInstanceId: param0 } = params;
  return request<API.SuccessResponse_WrappedTaskInstance_>(
    `/api/v1/tasks/instances/${param0}/retry`,
    {
      method: 'POST',
      params: { ...params },
      ...(options || {}),
    },
  );
}

/** 回滚整个任务.
@param taskInstanceId 任务实例 id
@return 重试任务
 POST /api/v1/tasks/instances/${param0}/rollback */
export async function rollbackTask(
  params: {
    // path
    /** 任务实例 id */
    taskInstanceId?: number;
  },
  options?: { [key: string]: any },
) {
  const { taskInstanceId: param0 } = params;
  return request<API.NoDataResponse>(`/api/v1/tasks/instances/${param0}/rollback`, {
    method: 'POST',
    params: { ...params },
    ...(options || {}),
  });
}

/** 跳过任务指定节点执行，只能跳过失败节点.
@param taskInstanceId 任务实例 id
@param subtaskInstanceId 子任务实例id
 POST /api/v1/tasks/instances/${param0}/subtasks/${param1}/skip */
export async function skipSubtask(
  params: {
    // path
    /** 任务实例 id */
    taskInstanceId?: number;
    /** 子任务实例id */
    subtaskInstanceId?: number;
  },
  options?: { [key: string]: any },
) {
  const { taskInstanceId: param0, subtaskInstanceId: param1 } = params;
  return request<API.NoDataResponse>(`/api/v1/tasks/instances/${param0}/subtasks/${param1}/skip`, {
    method: 'POST',
    params: { ...params },
    ...(options || {}),
  });
}

/** 手动触发某个定时任务的执行.
@param taskDefinitionId 定时调度任务原始 id
@return 任务信息
 POST /api/v1/tasks/${param0}/trigger */
export async function triggerScheduleTask(
  params: {
    // path
    /** 定时调度任务原始 id */
    taskDefinitionId?: number;
  },
  options?: { [key: string]: any },
) {
  const { taskDefinitionId: param0 } = params;
  return request<API.SuccessResponse_WrappedTaskInstance_>(`/api/v1/tasks/${param0}/trigger`, {
    method: 'POST',
    params: { ...params },
    ...(options || {}),
  });
}
