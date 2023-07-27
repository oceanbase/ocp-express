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
package com.oceanbase.ocp.partitioning.policy;

import java.time.Instant;
import java.util.List;

import com.oceanbase.ocp.partitioning.model.Partition;
import com.oceanbase.ocp.partitioning.model.PartitioningDefinition;

/**
 * Partition rollover policy interface.
 */
public interface PartitionRolloverPolicy {

    enum Scope {

        /**
         * Data source.
         */
        Metadb,
    }

    /**
     * Scope of rollover strategy.
     */
    Scope getScope();

    /**
     * Table name.
     */
    String getTableName();

    /**
     * Partition defintion.
     */
    PartitioningDefinition getPartitioningDefinition();

    /**
     * Partition detect context.
     */
    interface Context {

        /**
         * The time of the current data source.
         */
        Instant now();

        /**
         * Obtain existing partitions of the table.
         */
        List<Partition> listPartitions();

        /**
         * Get partition by name.
         */
        Partition getPartition(String name);
    }

    /**
     * Get the expected partitions.
     *
     * @param context Strategy context
     * @return expected partitions.
     */
    List<Partition> claimPartitions(Context context);

    default String info() {
        return "" + getScope().name() + "." + getTableName();
    }
}
