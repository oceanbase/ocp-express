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
package com.oceanbase.ocp.task.util;

import java.util.List;
import java.util.Optional;

import com.oceanbase.ocp.task.constants.ContextKey;
import com.oceanbase.ocp.task.model.Context;

public class ContextUtils {


    public static String getContextParallelValue(Context c, ContextKey k) {
        return c.getParallelValue(k.getValue());
    }


    public static Long getContextLongValue(Context c, ContextKey k) {
        return Long.parseLong(c.get(k.getValue()));
    }

    public static String get(Context c, ContextKey key, ContextKey listKey) {
        if (c.isParallel()) {
            List<String> strList = c.getListMap().get(listKey.getValue());
            return Optional.ofNullable(strList).map(list -> list.get(c.getParallelIdx())).orElse(null);
        } else {
            return c.get(key.getValue());
        }
    }

    /**
     * Get subtask instance id from context.
     *
     * @param context context of subtask
     * @return subtask instance id
     */
    public static Long getSubTaskInstanceId(Context context) {
        return Long.parseLong(context.get(ContextKey.SUB_TASK_INSTANCE_ID));
    }

}
