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

package com.oceanbase.ocp.bootstrap.core.def;

import java.util.EnumSet;

public enum SqlCompatible {

    /**
     * Compatible definitions of different database.
     */
    OCEANBASE(EnumSet.of(Capability.TABLE_COMMENT, Capability.LOCAL_GLOBAL_INDEX, Capability.PARTITION,
            Capability.SYNC_DDL, Capability.CHANGE_TYPE, Capability.INSERT_IGNORE));

    private final EnumSet<Capability> capabilities;

    public enum Capability {
        /**
         * Capability of database.
         */
        TABLE_COMMENT, LOCAL_GLOBAL_INDEX, PARTITION, CHANGE_TYPE, SYNC_DDL, INSERT_IGNORE
    }

    SqlCompatible(EnumSet<Capability> capabilities) {
        this.capabilities = capabilities;
    }

    public boolean haveCapability(Capability capability) {
        return capabilities.contains(capability);
    }
}
