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

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import com.oceanbase.ocp.bootstrap.Bootstrap;
import com.oceanbase.ocp.bootstrap.core.ProgressHandler;
import com.oceanbase.ocp.bootstrap.spi.AfterOcpServerReadyHook;
import com.oceanbase.ocp.bootstrap.util.ServiceUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BootstrapRunListener implements SpringApplicationRunListener {

    private final Bootstrap bootstrap;
    private static final AtomicBoolean SPRING_READY = new AtomicBoolean(false);

    public BootstrapRunListener(SpringApplication springApplication, String[] args) {
        log.info("new: {}", Arrays.asList(args));
        this.bootstrap = Bootstrap.getInstance();
        this.bootstrap.initialize(args);
        if (this.bootstrap.isEnabled()) {
            initializeSpring(springApplication);
        }
    }

    private void initializeSpring(SpringApplication application) {
        application.addInitializers(appContext -> {
            ConfigurableListableBeanFactory beanFactory = appContext.getBeanFactory();
            beanFactory.addBeanPostProcessor(DataSourceInterceptor.getInstance());
        });
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        log.info("starting");
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext,
            ConfigurableEnvironment environment) {
        log.info("environmentPrepared");
        bootstrap.waitDbPropertiesReady();
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        log.info("contextPrepared");
        registerBeans(context);
    }

    void registerBeans(ConfigurableApplicationContext context) {
        log.info("register bean ProgressHandler");
        context.getBeanFactory().registerSingleton(ProgressHandler.class.getCanonicalName(),
                Bootstrap.getInstance().getProgressHandler());
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        log.info("contextLoaded");
    }

    @Override
    public void started(ConfigurableApplicationContext context, Duration timeTaken) {
        log.info("started");
    }

    @Override
    public void ready(ConfigurableApplicationContext context, Duration timeTaken) {
        log.info("Ready, timeTaken={}", timeTaken);
        boolean rootServletInitialized = bootstrap.getProgress().isRootServletInitialized();
        if (rootServletInitialized) {
            if (SPRING_READY.compareAndSet(false, true)) {
                log.info("ocp is ready!");
                bootstrap.getProgressHandler().onApplicationReady();
                ServiceUtils.loadServices(AfterOcpServerReadyHook.class)
                        .forEach(AfterOcpServerReadyHook::ocpServerReady);
            }
        }
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        log.info("BootstrapRunListener failed");
        bootstrap.getProgressHandler().onApplicationFailed(exception);
    }
}
