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

package com.oceanbase.ocp.analyzer;

import java.util.Arrays;
import java.util.stream.StreamSupport;

import org.springframework.boot.actuate.endpoint.Sanitizer;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * A logger class that listener to Spring ContextRefreshedEvent (both init and
 * refresh) and prints currently active profiles and properties.
 */
@Slf4j
@Component
public class PropertyLogger {

    private final String[] keysToSanitize =
            {"password", "secret", "key", "token", "code", ".*credentials.*", "vcap_services", "sun.java.command"};

    final Sanitizer sanitizer = new Sanitizer();

    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        printActiveProperties(event.getApplicationContext().getEnvironment());
    }

    private void printActiveProperties(Environment env) {
        log.info("************************* ACTIVE OCP PROPERTIES ******************************");
        log.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
        final MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();
        StreamSupport.stream(sources.spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource && ps.getName().contains("bootstrapProperties"))
                .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .distinct()
                .sorted()
                .filter(prop -> !(prop.contains("code") || "version".equalsIgnoreCase(prop)))
                .forEach(key -> {
                    try {
                        log.info(key + "=" + sanitizer.sanitize(key, env.getProperty(key)));
                    } catch (Exception e) {
                        log.warn("{} -> {}", key, e.getMessage());
                    }
                });
        log.info("******************************************************************************");
    }
}
