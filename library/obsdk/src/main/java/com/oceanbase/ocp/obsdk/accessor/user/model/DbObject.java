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

package com.oceanbase.ocp.obsdk.accessor.user.model;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.oceanbase.ocp.obsdk.enums.ObjectType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DbObject {

    private ObjectType objectType;

    private String objectName;

    private String schemaName;

    @JsonProperty
    public String getFullName() {
        return String.format("%s.%s", schemaName, objectName);
    }

    @JsonIgnore
    public String getDescription() {
        return String.format("%s.%s (%s)", schemaName, objectName, objectType);
    }

    public void validate() {
        Validate.notNull(objectType, "objectType should not be null");
        Validate.notEmpty(objectName, "objectName should not be null");
        Validate.notEmpty(schemaName, "schemaName should not be null");
    }
}
