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

package com.oceanbase.ocp.bootstrap.db;

import java.util.concurrent.CountDownLatch;

import com.google.common.base.Strings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetaPropertyInitializer {

    private final CountDownLatch latch = new CountDownLatch(1);

    public MetaPropertyInitializer() {}

    public boolean isPropertyReady() {
        String jdbcUrl = getProperty("JDBC_URL");
        String jdbcUsername = getProperty("JDBC_USERNAME");
        return !Strings.isNullOrEmpty(jdbcUrl) && !Strings.isNullOrEmpty(jdbcUsername);
    }

    private static String getProperty(String key) {
        return System.getProperty(key, System.getenv(key));
    }

    public void initialize(String jdbcUrl, String jdbcUsername, String jdbcPassword, String metaPubKey) {
        log.info("MetaDB properties notified");
        if (Strings.isNullOrEmpty(jdbcUrl) || Strings.isNullOrEmpty(jdbcUsername)
                || Strings.isNullOrEmpty(jdbcPassword)) {
            if (isPropertyReady()) {
                log.info("use existed properties");
                return;
            }
            log.error("should provide jdbcUrl, jdbcUsername, jdbcPassword");
            return;
        }
        System.setProperty("JDBC_URL", jdbcUrl);
        System.setProperty("JDBC_USERNAME", jdbcUsername);
        System.setProperty("JDBC_PASSWORD", jdbcPassword);
        latch.countDown();
    }

    public void initialize(String address, String database, String username, String password, String metaPubKey) {
        String jdbcUrl = String.format("jdbc:oceanbase://%s/%s", address, database);
        initialize(jdbcUrl, username, password, metaPubKey);
    }
}
