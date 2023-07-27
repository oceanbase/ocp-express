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

package com.oceanbase.ocp.perf.sql;

import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import com.oceanbase.ocp.common.pattern.Lazy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SqlStatAttributeServiceImpl implements SqlStatAttributeService {

    private final Lazy<List<SqlStatAttributeSpec>> SqlStatAttributeSpecs =
            Lazy.of(() -> read(SqlStatAttributeSpec.class, "sql_stat_attribute_spec.yaml"));


    private <T> List<T> read(Class<T> type, String resource) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream stream = new ClassPathResource(resource).getInputStream()) {
            CollectionLikeType listType = mapper.getTypeFactory().constructCollectionLikeType(List.class, type);
            JsonNode json = mapper.readTree(stream);
            return mapper.convertValue(json, listType);
        } catch (Exception e) {
            throw new IllegalStateException(
                    String.format("Couldn't initialize sql stat resource spec: %s", resource), e);
        }
    }


    @Override
    public SqlStatAttributeSpec getSqlStatAttribute(String name) {
        return SqlStatAttributeSpecs.get().stream().filter(it -> it.getName().equals(name)).findFirst().orElse(null);
    }
}
