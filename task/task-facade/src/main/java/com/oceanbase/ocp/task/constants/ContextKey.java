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
package com.oceanbase.ocp.task.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ContextKey {

    /**
     * Task context key.
     */
    TASK_OPERATION("task_operation"),
    CLUSTER_ID("cluster_id"),
    CLUSTER_IDS("cluster_ids"),

    PROHIBIT_ROLLBACK("prohibit_rollback"),

    OLD_PASSWORD("old_password"),
    NEW_PASSWORD("new_password"),

    ZONE_NAME("zone_name"),
    ZONE_NAMES("zone_names"),

    OB_TENANT_ID("ob_tenant_id"),
    TENANT_NAME("tenant_name"),
    TENANT_MODE("tenant_mode"),
    TARGET_TENANT_STATUS("target_tenant_status"),
    FORMER_TENANT_STATUS("former_tenant_status"),
    CREATE_TENANT_PARAM_JSON("create_tenant_param_json"),
    RESOURCE_POOL_LIST_JSON("resource_pool_list_json"),

    UNIT_SPEC_JSON_LIST("unit_spec_json_list"),
    UNIT_COUNT("unit_count"),
    WHITELIST("whitelist"),
    SYSTEM_VARIABLE_MAP("system_variable_map"),

    OB_TENANT_PARAMETER_MAP("ob_tenant_parameter_map"),

    TASK_INSTANCE_ID("task_instance_id"),
    SUB_TASK_INSTANCE_ID("sub_task_instance_id"),

    LATEST_EXECUTION_START_TIME("latest_execution_start_time"),

    AGENT_ASYNC_TASK_TOKEN_MAP("agent_async_task_token_map"),

    OB_AGENT_ID("ob_agent_id"),
    ;

    private final String value;

    ContextKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static ContextKey fromValue(String text) {
        for (ContextKey b : ContextKey.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
