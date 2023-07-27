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
package com.oceanbase.ocp.core.event;

import java.util.HashMap;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

public class SystemParameterNotTakenEffectEvent extends ApplicationEvent {

    private static final long serialVersionUID = 6875040277712769389L;

    @Getter
    private final HashMap<String, String> labels;

    public SystemParameterNotTakenEffectEvent(HashMap<String, String> labels) {
        super(labels);
        this.labels = labels;
    }

}
