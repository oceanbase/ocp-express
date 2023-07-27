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

package com.oceanbase.ocp.bootstrap.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.oceanbase.ocp.bootstrap.core.Keys;
import com.oceanbase.ocp.bootstrap.core.def.DataDefinition;
import com.oceanbase.ocp.bootstrap.core.def.Migration;
import com.oceanbase.ocp.bootstrap.core.def.TableDefinition;
import com.oceanbase.ocp.bootstrap.spi.DataDefinitionProvider;
import com.oceanbase.ocp.bootstrap.util.ResourceUtils;
import com.oceanbase.ocp.bootstrap.util.ServiceUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigLoader {

    public static final String DIR_NAME = "ocp_bootstrap_definitions";
    private final YamlLoader yamlLoader;
    private volatile AllDataConfig allDataConfig;

    public ConfigLoader() {
        this.yamlLoader = new YamlLoader();
    }

    public AllDataConfig loadAll() {
        AllDataConfig loaded = this.allDataConfig;
        if (loaded != null) {
            return loaded;
        }
        loaded = toDataConfig(loadAllYaml());
        this.allDataConfig = loaded;
        return loaded;
    }

    public DataConfig load(String name) {
        LinkedHashMap<String, Object> aConfig = loadFile(name);
        return toDataConfig(aConfig);
    }

    @SuppressWarnings("unchecked")
    static DataConfig toDataConfig(Map<String, Object> dataSourceYaml) {
        DataConfig ret = new DataConfig((String) dataSourceYaml.get(Keys.DATA_SOURCE));
        ((Map<String, Map<String, Object>>) dataSourceYaml.getOrDefault(Keys.TABLE_DEFINITIONS, Collections.emptyMap()))
                .forEach((name, conf) -> {
                    ret.tableDefinitions.put(name, TableDefinition.fromConfig(name, conf));
                });
        ((Map<String, Map<String, Object>>) dataSourceYaml.getOrDefault(Keys.DATA_DEFINITIONS, Collections.emptyMap()))
                .forEach((name, conf) -> {
                    ret.dataDefinitions.put(name, DataDefinition.fromConfig(name, conf));
                });
        ((Map<String, Map<String, Object>>) dataSourceYaml.getOrDefault(Keys.MIGRATIONS, Collections.emptyMap()))
                .forEach((name, conf) -> {
                    ret.migrations.put(name, Migration.fromConfig(name, conf));
                });
        return ret;
    }

    static AllDataConfig toDataConfig(AllYaml yamlConfig) {
        AllDataConfig ret = new AllDataConfig();
        // load config
        yamlConfig.forEach((dataSource, dataSourceYaml) -> {
            ret.put(dataSource, toDataConfig(dataSourceYaml));
        });

        // load spi
        dataDefinitionProviders().forEach(provider -> {
            List<DataDefinition> dataDefinitions = provider.dataDefinitions();
            DataConfig dataConfig = ret.computeIfAbsent(provider.dataSourceName(), DataConfig::new);
            dataDefinitions.forEach(dataDefinition -> {
                dataConfig.dataDefinitions.put(dataDefinition.getName(), dataDefinition);
            });
        });
        return ret;
    }

    static class AllYaml extends LinkedHashMap<String, LinkedHashMap<String, Object>> {
    }

    AllYaml loadAllYaml() {
        AllYaml ret = new AllYaml();
        AtomicReference<RuntimeException> exRef = new AtomicReference<>();
        configFiles().parallelStream().forEach(fileName -> {
            try {
                LinkedHashMap<String, Object> aConfig = loadFile(fileName);
                if (aConfig == null) {
                    return;
                }
                String dataSourceName = (String) aConfig.get(Keys.DATA_SOURCE);
                synchronized (this) {
                    LinkedHashMap<String, Object> dataSourceYaml =
                            ret.computeIfAbsent(dataSourceName, k -> {
                                LinkedHashMap<String, Object> newSubConfig = new LinkedHashMap<>();
                                newSubConfig.put(Keys.DATA_SOURCE, dataSourceName);
                                return newSubConfig;
                            });
                    mergeConfigMap(dataSourceYaml, aConfig, Keys.TABLE_DEFINITIONS, fileName);
                    mergeConfigMap(dataSourceYaml, aConfig, Keys.DATA_DEFINITIONS, fileName);
                    mergeConfigMap(dataSourceYaml, aConfig, Keys.MIGRATIONS, fileName);
                }
            } catch (RuntimeException e) {
                exRef.set(e);
            }
        });
        RuntimeException ex = exRef.get();
        if (ex != null) {
            throw ex;
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    static void mergeConfigMap(Map<String, Object> dest, Map<String, Object> src, String key, String fromFileName) {
        Map<String, Object> srcSubConfig = (Map<String, Object>) (src.getOrDefault(key, Collections.emptyMap()));
        Map<String, Object> destSubConfig =
                ((Map<String, Object>) dest.computeIfAbsent(key, k -> new LinkedHashMap<>()));
        srcSubConfig.forEach((k, v) -> {
            if (v instanceof Map) {
                ((Map<String, Object>) v).put(Keys.FROM_CONFIG_FILE, fromFileName);
            }
            Object orig = destSubConfig.put(k, v);
            if (orig != null) {
                String msg = "duplicated " + key + " " + k + " in " + fromFileName;
                if (orig instanceof Map) {
                    String existsFileName = Objects.toString(((Map<String, Object>) orig).get(Keys.FROM_CONFIG_FILE));
                    msg += " - " + existsFileName;
                }
                throw new IllegalStateException(msg);
            }
        });
    }

    LinkedHashMap<String, Object> loadFile(String name) {
        log.info("load config from {}", name);
        return yamlLoader.loadResourceAs(DIR_NAME + "/" + name, LinkedHashMap.class);
    }

    public static List<String> configFiles() {
        return ResourceUtils.listResourceDir(DIR_NAME, name -> name.endsWith(".yaml"));
    }

    static List<DataDefinitionProvider> dataDefinitionProviders() {
        return ServiceUtils.loadServices(DataDefinitionProvider.class);
    }
}
