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
package com.oceanbase.ocp.partitioning.model;

import java.util.List;

import com.oceanbase.ocp.partitioning.constants.PartitioningType;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

/**
 * Partition definition rule.
 */
@Data
@Builder
public class PartitioningDefinition {

    /**
     * First level partition type.
     */
    public PartitioningType partitioningType;

    /**
     * First level partition columns.
     */
    @Singular
    public List<String> partitioningColumns;

    /**
     * Second level partition type.
     */
    public PartitioningType subpartitioningType;

    /**
     * Second level partition columns.
     */
    public List<String> subpartitioningColumns;
}
