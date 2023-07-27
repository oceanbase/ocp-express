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

package com.oceanbase.ocp.core.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BeanUtils implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
        log.info("BeanUtils initialized");
    }

    public static <T> T getBean(Class<T> beanClass) {
        if (context == null) {
            throw new RuntimeException("unable to getBean before applicationContext initialized!");
        }
        return context.getBean(beanClass);
    }

    public static Object getBean(String beanName) {
        if (context == null) {
            throw new RuntimeException("unable to getBean before applicationContext initialized!");
        }
        return context.getBean(beanName);
    }

    public static <T> T getBean(String beanName, Class<T> requireType) {
        if (context == null) {
            throw new RuntimeException("unable to getBean before applicationContext initialized!");
        }
        return context.getBean(beanName, requireType);
    }

}
