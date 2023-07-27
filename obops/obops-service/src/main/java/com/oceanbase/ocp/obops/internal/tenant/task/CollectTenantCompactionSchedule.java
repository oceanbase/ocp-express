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

package com.oceanbase.ocp.obops.internal.tenant.task;

import com.oceanbase.ocp.task.model.Argument;
import com.oceanbase.ocp.task.runtime.Node;
import com.oceanbase.ocp.task.runtime.Template;
import com.oceanbase.ocp.task.runtime.TemplateBuilder;
import com.oceanbase.ocp.task.schedule.ISchedule;

public class CollectTenantCompactionSchedule implements ISchedule {

    @Override
    public Template getTemplate() {
        TemplateBuilder templateBuilder = new TemplateBuilder();
        return templateBuilder.name("Collect all tenants major compactions")
                .addNode(new Node(new CollectAllTenantCompactionTask())).build();
    }

    @Override
    public Argument getArgument() {
        return new Argument();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
