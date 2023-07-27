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
package com.oceanbase.ocp.task.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.endpoint.Sanitizer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;

import com.oceanbase.ocp.core.i18n.ErrorCodes;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.constants.TaskConstants;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@EqualsAndHashCode
public class Context {

    private static final Sanitizer SANITIZER = new Sanitizer("password", "secret",
            "key", "token", ".*credentials.*", "config", "storage.*");

    /**
     * Context need skipped when merge upstream subtasks.
     */
    private static Set<String> SUBTASK_EXCLUSIVE_CONTEXT_KEY_SET =
            Sets.newHashSet(ContextKey.LATEST_EXECUTION_START_TIME.getValue(), ContextKey.TASK_INSTANCE_ID.getValue(),
                    ContextKey.AGENT_ASYNC_TASK_TOKEN_MAP.getValue());

    private int parallelIdx;
    private Map<String, String> stringMap;
    private Map<String, List<String>> listMap;

    public Context() {
        this.parallelIdx = TaskConstants.NO_PARALLEL_IDX;
        this.stringMap = new HashMap<>();
        this.listMap = new HashMap<>();
    }

    public Context(int idx, Map<String, String> stringMap, Map<String, List<String>> listMap) {
        this.parallelIdx = idx;
        this.stringMap = stringMap;
        this.listMap = listMap;
    }

    public Context(Context c) {
        if (c == null) {
            return;
        }
        this.parallelIdx = c.parallelIdx;
        this.stringMap = c.getStringMap();
        this.listMap = c.getListMap();
    }

    public void merge(Context c) {
        if (c == null) {
            return;
        }
        // merge stringMap, throw exception when found different value for same key
        for (Map.Entry<String, String> entry : c.getStringMap().entrySet()) {
            if (this.stringMap.containsKey(entry.getKey())
                    && !SUBTASK_EXCLUSIVE_CONTEXT_KEY_SET.contains(entry.getKey())) {
                if (!StringUtils.equals(this.stringMap.get(entry.getKey()), entry.getValue())) {
                    throw new RuntimeException("found different value for same key");
                }
            } else {
                this.stringMap.put(entry.getKey(), entry.getValue());
            }
        }

        // merge listMap, merge List if there's same key, else add a new entry
        for (Map.Entry<String, List<String>> entry : c.getListMap().entrySet()) {
            String key = entry.getKey();
            if (this.listMap.containsKey(key)) {
                for (String e : entry.getValue()) {
                    if (!listMap.get(key).contains(e)) {
                        listMap.get(key).add(e);
                    }
                }
            } else {
                this.listMap.put(key, entry.getValue());
            }
        }
    }

    public void putLong(ContextKey key, Long id) {
        put(key, String.valueOf(id));
    }

    public void put(ContextKey key, String str) {
        put(key.getValue(), str);
    }

    public void put(ContextKey key, Long value) {
        put(key.getValue(), Long.toString(value));
    }

    public void put(ContextKey key, Boolean value) {
        put(key.getValue(), Boolean.toString(value));
    }

    public void put(String key, String str) {
        this.stringMap.put(key, str);
    }

    public void remove(ContextKey key) {
        this.stringMap.remove(key.getValue());
    }

    public void putList(ContextKey key, List<String> strList) {
        putList(key.getValue(), strList);
    }

    public void putList(String key, List<String> strList) {
        this.listMap.put(key, strList);
    }

    public boolean isParallel() {
        return this.parallelIdx != TaskConstants.NO_PARALLEL_IDX;
    }

    public boolean contains(ContextKey key) {
        return this.stringMap.containsKey(key.getValue());
    }

    public String get(String key) {
        return this.stringMap.get(key);
    }

    public String get(ContextKey key) {
        return get(key.getValue());
    }

    public Long getLong(ContextKey key) {
        return Long.parseLong(get(key.getValue()));
    }

    public String getParallelValue(String listKey) {
        List<String> strList = this.listMap.get(listKey);
        ExceptionUtils.unExpected(CollectionUtils.isNotEmpty(strList), ErrorCodes.TASK_CONTEXT_ERROR);
        return strList.get(this.parallelIdx);
    }

    public String get(String key, String listKey) {
        if (isParallel()) {
            return getParallelValue(listKey);
        }
        return this.stringMap.get(key);
    }

    public String get(ContextKey key, ContextKey listKey) {
        return get(key.getValue(), listKey.getValue());
    }

    @JsonIgnore
    public String getContextString() {
        return toString();
    }

    @Override
    @JsonIgnore
    public String toString() {
        Map<String, Object> tmpMap = new HashMap<>();
        for (String key : this.stringMap.keySet()) {
            tmpMap.put(key, SANITIZER.sanitize(key, this.stringMap.get(key)));
        }
        return MoreObjects.toStringHelper(this)
                .add("parallelIdx", this.parallelIdx)
                .add("stringMap", tmpMap)
                .add("listMap", this.listMap)
                .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Context context;

        public Builder() {
            this.context = new Context();
        }

        public Builder put(ContextKey key, String value) {
            this.context.put(key, value);
            return this;
        }

        public Builder put(String key, String value) {
            this.context.put(key, value);
            return this;
        }

        public Context build() {
            return context;
        }

    }

}
