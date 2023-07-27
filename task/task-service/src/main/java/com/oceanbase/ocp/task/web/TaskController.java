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
package com.oceanbase.ocp.task.web;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oceanbase.ocp.core.response.NoDataResponse;
import com.oceanbase.ocp.core.response.PaginatedResponse;
import com.oceanbase.ocp.core.response.ResponseBuilder;
import com.oceanbase.ocp.core.response.SuccessResponse;
import com.oceanbase.ocp.task.model.BasicTaskInstance;
import com.oceanbase.ocp.task.model.SubtaskInstance;
import com.oceanbase.ocp.task.model.SubtaskLog;
import com.oceanbase.ocp.task.model.WrappedTaskInstance;

import lombok.extern.slf4j.Slf4j;

@RequestMapping("/api/v1/tasks")
@RestController
@Slf4j
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * Query task instances.
     *
     * @param keyword keyword like clusterName or taskName
     * @param pageable pageable
     * @param status status of task
     * @return task instances
     */
    @GetMapping(value = "/instances", produces = MediaType.APPLICATION_JSON_VALUE)
    public PaginatedResponse<BasicTaskInstance> listTaskInstances(
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault(sort = {"id"}, size = Integer.MAX_VALUE,
                    direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "status", required = false) String status) {
        TaskInstanceQueryParam param = TaskInstanceQueryParam.builder()
                .keyword(keyword)
                .status(status)
                .build();
        return ResponseBuilder.paginated(taskService.listBasicTaskInstances(param, pageable));
    }

    /**
     * Get task instance by specified task instance id.
     *
     * @param taskInstanceId task id
     * @return Task info
     */
    @GetMapping(value = "/instances/{taskInstanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public SuccessResponse<WrappedTaskInstance> getTaskInstance(@PathVariable("taskInstanceId") Long taskInstanceId) {
        return ResponseBuilder.single(taskService.getTaskInstance(taskInstanceId));
    }

    /**
     * Download task log.
     *
     * @param taskInstanceId id
     * @return task log
     */
    @PostMapping(value = "/instances/{taskInstanceId}/downloadDiagnosis", produces = {"application/zip"})
    public ResponseEntity<Resource> downloadTaskDiagnosis(
            @PathVariable Long taskInstanceId) throws IOException {
        Resource resource = taskService.getTaskDiagnosis(taskInstanceId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * Get subtask log by subtask instance id.
     *
     * @param taskInstanceId task instance id
     * @param subtaskInstanceId subtask instance id
     * @return subtask log.
     */
    @GetMapping(value = "/instances/{taskInstanceId}/subtasks/{subtaskInstanceId}/log",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public SuccessResponse<SubtaskLog> getSubtaskLog(
            @PathVariable("taskInstanceId") Long taskInstanceId,
            @PathVariable("subtaskInstanceId") Long subtaskInstanceId) {
        return ResponseBuilder.single(taskService.getSubtaskDetail(taskInstanceId, subtaskInstanceId));
    }

    /**
     * Retry task by task instance id.
     *
     * @param taskInstanceId task instance id
     * @return Task info
     */
    @PostMapping(value = "/instances/{taskInstanceId}/retry", produces = MediaType.APPLICATION_JSON_VALUE)
    public SuccessResponse<WrappedTaskInstance> retryTask(
            @PathVariable("taskInstanceId") Long taskInstanceId) {
        return ResponseBuilder.single(taskService.retryTask(taskInstanceId));
    }

    /**
     * Rollback task.
     *
     * @param taskInstanceId id
     * @return rollback task
     */
    @PostMapping(value = "/instances/{taskInstanceId}/rollback", produces = MediaType.APPLICATION_JSON_VALUE)
    public NoDataResponse rollbackTask(@PathVariable("taskInstanceId") Long taskInstanceId) {
        taskService.rollbackTask(taskInstanceId);
        return ResponseBuilder.noData();
    }

    /**
     * Retry subtask.
     *
     * @param taskInstanceId task instance id
     * @param subtaskInstanceId subtask instance id
     * @return subtask instance info
     */
    @PostMapping(value = "/instances/{taskInstanceId}/subtasks/{subtaskInstanceId}/retry",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public SuccessResponse<SubtaskInstance> retrySubtask(
            @PathVariable("taskInstanceId") Long taskInstanceId,
            @PathVariable("subtaskInstanceId") Long subtaskInstanceId) {
        return ResponseBuilder.single(taskService.retrySubtask(taskInstanceId, subtaskInstanceId));
    }

    /**
     * Skip subtask.
     *
     * @param taskInstanceId task instance id
     * @param subtaskInstanceId subtask instance id
     */
    @PostMapping(value = "/instances/{taskInstanceId}/subtasks/{subtaskInstanceId}/skip",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public NoDataResponse skipSubtask(
            @PathVariable("taskInstanceId") Long taskInstanceId,
            @PathVariable("subtaskInstanceId") Long subtaskInstanceId) {
        taskService.skipSubtask(taskInstanceId, subtaskInstanceId);
        return ResponseBuilder.noData();
    }

    /**
     * Cancel subtask.
     *
     * @param taskInstanceId task instance id
     * @param subtaskInstanceId subtask instance id
     */
    @PostMapping(value = "/instances/{taskInstanceId}/subtasks/{subtaskInstanceId}/cancel",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public NoDataResponse cancelSubtask(@PathVariable("taskInstanceId") Long taskInstanceId,
            @PathVariable("subtaskInstanceId") Long subtaskInstanceId) {
        taskService.cancelSubtask(taskInstanceId, subtaskInstanceId);
        return ResponseBuilder.noData();
    }

}
