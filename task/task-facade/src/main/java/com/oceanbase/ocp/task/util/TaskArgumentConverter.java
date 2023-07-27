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

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.common.util.EncodeUtils;
import com.oceanbase.ocp.common.util.json.JsonUtils;
import com.oceanbase.ocp.task.model.Argument;

@Converter
public class TaskArgumentConverter implements AttributeConverter<Argument, String> {

    @Override
    public String convertToDatabaseColumn(Argument attribute) {
        if (Objects.isNull(attribute)) {
            return null;
        }
        String json = JsonUtils.toJsonString(attribute);
        return EncodeUtils.base64EncodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Argument convertToEntityAttribute(String base64String) {
        if (StringUtils.isEmpty(base64String)) {
            return null;
        }
        byte[] bytes = EncodeUtils.base64DecodeFromString(base64String);
        Validate.notNull(bytes, "deserialize failed due null bytes");
        String json = new String(bytes, StandardCharsets.UTF_8);
        return JsonUtils.fromJson(json, Argument.class);
    }
}
