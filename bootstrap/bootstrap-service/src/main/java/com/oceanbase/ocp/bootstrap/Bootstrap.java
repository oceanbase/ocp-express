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

package com.oceanbase.ocp.bootstrap;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.boot.info.BuildProperties;

import com.oceanbase.ocp.bootstrap.Params.PropertyPair;
import com.oceanbase.ocp.bootstrap.config.ConfigLoader;
import com.oceanbase.ocp.bootstrap.config.env.Version;
import com.oceanbase.ocp.bootstrap.core.Action;
import com.oceanbase.ocp.bootstrap.db.DataSourceProvider;
import com.oceanbase.ocp.bootstrap.db.MetaPropertyInitializer;
import com.oceanbase.ocp.bootstrap.progress.Progress;
import com.oceanbase.ocp.bootstrap.progress.ProgressHandlerImpl;
import com.oceanbase.ocp.bootstrap.progress.ProgressWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Bootstrap {

    private static final int DEFAULT_PORT = 8180;

    private static final Bootstrap INSTANCE = new Bootstrap();

    public static Bootstrap getInstance() {
        return INSTANCE;
    }

    private final MetaPropertyInitializer propertyInitializer;
    private volatile ProgressHandlerImpl progressHandler;
    private final Progress progress;
    private volatile Params params;
    private final ConfigLoader configLoader;
    private final DataSourceProvider dataSourceProvider;
    private final Map<String, String> configProperties;
    private final BuildInfo buildInfo;

    Bootstrap() {
        this.propertyInitializer = new MetaPropertyInitializer();
        this.configLoader = new ConfigLoader();
        this.dataSourceProvider = new DataSourceProvider();
        this.configProperties = new ConcurrentHashMap<>();
        this.buildInfo = BuildInfo.load();
        this.progress = new Progress();
    }

    public boolean isEnabled() {
        return params.isEnabled();
    }

    public boolean metaDbPropertiesReady() {
        return propertyInitializer.isPropertyReady();
    }

    public void waitDbPropertiesReady() {
        if (!isEnabled()) {
            return;
        }
        if (!propertyInitializer.isPropertyReady()) {
            throw new IllegalStateException("meta db properties are not given");
        }
    }

    public void initialize(String[] args) {
        synchronized (this) {
            if (params == null) {
                initializeParams(new ArgParser().parse(args));
            }
        }
    }

    private void initializeParams(Params params) {
        this.params = params;
        for (PropertyPair pair : params.configProperties) {
            setConfigProperty(pair.getName(), pair.getValue());
        }
        MetaPropertyInitializer metaPropertyInitializer = getMetaPropertyInitializer();
        metaPropertyInitializer.initialize(params.metaAddress, params.metaDatabase, params.metaUsername,
                params.metaPassword, params.metaPubKey);
        Writer writer = createWriterForProgress(params);
        ProgressWriter progressWriter = new ProgressWriter(writer);
        this.progressHandler = new ProgressHandlerImpl(progress, progressWriter);
        this.progressHandler.setAction(params.action);
    }

    private Writer createWriterForProgress(Params params) {
        Writer writer;
        if (params.progressLogPath.isEmpty()) {
            log.info("progress log path is empty, using stdout instead.");
            writer = new OutputStreamWriter(System.out);
        } else {
            try {
                writer = new FileWriter(params.progressLogPath, true);
            } catch (IOException e) {
                log.error("create progress writer for {} failed. using stdout instead.", params.progressLogPath, e);
                writer = new OutputStreamWriter(System.out);
            }
        }
        return writer;
    }

    public Progress getProgress() {
        return progress;
    }

    public ProgressHandlerImpl getProgressHandler() {
        if (progressHandler == null) {
            return ProgressHandlerImpl.nopProgressHandler();
        }
        return progressHandler;
    }

    public MetaPropertyInitializer getMetaPropertyInitializer() {
        return propertyInitializer;
    }

    public Version getOcpVersion() {
        String buildVersion = buildInfo.getVersion();
        if (buildVersion == null) {
            return null;
        }
        Version version = new Version(buildVersion);
        if ("SNAPSHOT".equals(version.getNote()) || version.getNote().isEmpty()) {
            String buildDate = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                    .format(LocalDateTime.ofInstant(buildInfo.getBuildTime(), ZoneOffset.of("+08:00")));
            return new Version(version.getSimpleVersionString() + "-" + buildDate);
        }
        return version;
    }

    public Instant getOcpBuildTime() {
        return buildInfo.getBuildTime();
    }

    public Action getAction() {
        return progress.getAction();
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

    public Map<String, String> getConfigProperties() {
        return configProperties;
    }

    public void setConfigProperty(String key, String value) {
        configProperties.put(key, value);
    }

    public DataSourceProvider getDataSourceProvider() {
        return dataSourceProvider;
    }

    public int getPort() {
        if (params == null || params.getPort() <= 0) {
            return DEFAULT_PORT;
        }
        return params.getPort();
    }

    public static class BuildInfo {

        public BuildInfo() {

        }

        private String version = "";
        private Instant buildTime = null;

        public static BuildInfo load() {
            ProjectInfoAutoConfiguration configuration = new ProjectInfoAutoConfiguration(new ProjectInfoProperties());
            BuildInfo ret = new BuildInfo();
            try {
                BuildProperties buildProperties = configuration.buildProperties();
                ret.version = buildProperties.getVersion();
                ret.buildTime = buildProperties.getTime();
            } catch (Exception e) {
                // ignore
            }
            return ret;
        }

        public String getVersion() {
            return version;
        }

        public Instant getBuildTime() {
            return buildTime;
        }
    }
}
