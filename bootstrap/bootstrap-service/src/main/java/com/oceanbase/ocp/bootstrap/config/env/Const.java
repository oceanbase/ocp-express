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

package com.oceanbase.ocp.bootstrap.config.env;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;

public class Const extends TypeDescription {

    public Const() {
        super(com.oceanbase.ocp.bootstrap.core.def.Const.class, "!const");
    }

    @Override
    public Object newInstance(Node node) {
        if (node instanceof ScalarNode) {
            String name = ((ScalarNode) node).getValue();
            return com.oceanbase.ocp.bootstrap.core.def.Const.valueOf(name);
        }
        throw new IllegalArgumentException("Const must be a const name");
    }
}
