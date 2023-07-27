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
package com.oceanbase.ocp.common.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;

public class FileUtils {

    /**
     * read file from file system or class path
     *
     * @param fileName absolute path of file
     * @return InputStream of file
     */
    public static InputStream readAsInputStream(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            throw new IllegalArgumentException("fileName is null or empty");
        }
        File file = new File(fileName);
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new RuntimeException("file is directory, given fileName=" + fileName);
            }
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("file not found in file system, given fileName=" + fileName, e);
            }
        }
        InputStream inputStream = FileUtils.class.getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            throw new RuntimeException("file not found in classpath, given fileName=" + fileName);
        }
        return inputStream;
    }
}
