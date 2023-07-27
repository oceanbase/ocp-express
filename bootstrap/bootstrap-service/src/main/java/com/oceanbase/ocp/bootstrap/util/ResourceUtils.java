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

package com.oceanbase.ocp.bootstrap.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.io.CharStreams;

public class ResourceUtils {

    public static Reader resourceReader(String path) {
        ClassLoader classLoader = ResourceUtils.class.getClassLoader();
        InputStream stream = classLoader.getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalArgumentException("Resource file not found: " + path);
        }
        return new InputStreamReader(stream, Charsets.UTF_8);
    }

    public static String loadResource(String path) {
        try (Reader reader = resourceReader(path)) {
            return CharStreams.toString(reader);
        } catch (IOException e) {
            throw new IllegalStateException("read resource as string failed: " + path, e);
        }
    }

    public static List<String> listResourceDir(String path, Predicate<String> fileFilter) {
        List<String> ret = new ArrayList<>();
        try {
            if (!path.endsWith("/")) {
                path += "/";
            }
            String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + path + "**/*";
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(pattern);
            for (Resource resource : resources) {
                String name = resource.getFilename();
                if (!resource.isReadable()) {
                    continue;
                }
                if (fileFilter == null || fileFilter.apply(name)) {
                    ret.add(filePathFromResource(resource, path));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return ret;
    }

    static String filePathFromResource(Resource resource, String path) {
        if (!path.endsWith("/")) {
            path += "/";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        try {
            String uriString = resource.getURI().toString();
            int i = uriString.indexOf(path);
            return uriString.substring(i + path.length());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
