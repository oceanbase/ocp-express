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

package com.oceanbase.ocp.bootstrap.spring;

import java.util.HashSet;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.oceanbase.ocp.bootstrap.spi.AfterDataSourceCreationHook;
import com.oceanbase.ocp.bootstrap.util.ServiceUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataSourceInterceptor implements BeanPostProcessor {

    private static final DataSourceInterceptor INSTANCE = new DataSourceInterceptor();

    private DataSourceInterceptor() {}

    private final HashSet<String> once = new HashSet<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource) {
            synchronized (this) {
                if (!once.add(beanName)) {
                    log.info("dataSource {} already initialized. skipped", beanName);
                    return bean;
                }
            }
            DataSource dataSource = (DataSource) bean;
            afterDataSourceCreationHook(beanName, dataSource);
        }
        return bean;
    }

    void afterDataSourceCreationHook(String beanName, DataSource dataSource) {
        log.info("begin AfterDataSourceCreationHook");
        ServiceUtils.loadServices(AfterDataSourceCreationHook.class).forEach(hook -> {
            log.info("call AfterDataSourceCreationHook {}", hook.getClass().getName());
            hook.afterDataSourceCreation(beanName, dataSource);
        });
        log.info("end AfterDataSourceCreationHook");
    }

    public static DataSourceInterceptor getInstance() {
        return INSTANCE;
    }
}
