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
package com.oceanbase.ocp.task.dao;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.oceanbase.ocp.task.entity.SubtaskLogEntity;

@Component
public class SubtaskLogRepo {

    @Autowired
    private JdbcTemplate metaJdbcTemplate;

    /**
     * Save subtask log to db.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int save(SubtaskLogEntity entity) {
        Validate.notNull(entity);
        String sql = "INSERT INTO `subtask_log`(`subtask_id`, `log_content`, `run_time`) VALUES (?, ?, ?)";
        return metaJdbcTemplate.update(sql, entity.getSubtaskInstanceId(), entity.getLogContent(), entity.getRunTime());
    }

}
