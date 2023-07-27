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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.google.common.io.CharStreams;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class YamlUtils {

    private static final DumperOptions OPTIONS = new DumperOptions();

    static {
        OPTIONS.setIndent(2);
        OPTIONS.setPrettyFlow(true);
        OPTIONS.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    }

    public static <T> T loadResourceAs(File file, Class<? super T> cls) throws FileNotFoundException {
        log.info("Load yaml, path={}", file.getAbsolutePath());
        String content = loadResource(file);
        try {
            return loadAs(content, cls);
        } catch (Exception e) {
            throw new IllegalStateException("load yaml from resource failed: " + file.getAbsolutePath(), e);
        }
    }

    public static <T> T loadAs(String content, Class<? super T> cls) {
        return new Yaml(OPTIONS).loadAs(content, cls);
    }

    public static <T> void dump(File file, T obj) throws IOException {
        log.info("Dump yaml, path={}", file.getAbsolutePath());
        Yaml yaml = new Yaml(OPTIONS);
        FileWriter writer = new FileWriter(file);
        yaml.dump(obj, writer);
    }

    private static String loadResource(File file) throws FileNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        try (Reader reader = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
            return CharStreams.toString(reader);
        } catch (IOException e) {
            throw new IllegalStateException("read resource as string failed: " + file.getAbsolutePath(), e);
        }
    }

}
