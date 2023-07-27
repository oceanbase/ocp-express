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

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import com.oceanbase.ocp.bootstrap.Bootstrap;
import com.oceanbase.ocp.bootstrap.core.def.Row;
import com.oceanbase.ocp.bootstrap.db.DataSourceName;
import com.oceanbase.ocp.bootstrap.util.SQLUtils;
import com.oceanbase.ocp.common.util.HostUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BootstrapLock {

    private final String clientId;
    private static final BootstrapLock INSTANCE = new BootstrapLock();
    private volatile boolean inited = false;

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `bootstrap_lock` (\n"
            + "  `lock_key` varchar(256) NOT NULL COMMENT 'name of bootstrap lock',\n"
            + "  `client_id` varchar(256) NOT NULL COMMENT 'Id of node obtained the lock, include ip and port',\n"
            + "  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'lock time',\n"
            + "  PRIMARY KEY (`lock_key`)\n"
            + ")";

    BootstrapLock() {
        this.clientId = clientId();
        log.info("BootstrapLock client id: {}", clientId);
    }

    public static BootstrapLock getInstance() {
        return INSTANCE;
    }

    static String clientId() {
        return HostUtils.getLocalIp() + ":" + Bootstrap.getInstance().getPort();
    }

    void init() {
        if (inited) {
            return;
        }
        synchronized (this) {
            try {
                SQLUtils.execute(dataSource(), CREATE_TABLE);
                log.info("distributed_lock table created");
                inited = true;
            } catch (SQLException e) {
                throw new IllegalStateException("init distributed_lock table failed", e);
            }
        }
    }

    DataSource dataSource() {
        DataSource ret = Bootstrap.getInstance().getDataSourceProvider().getDataSource(DataSourceName.SPRING);
        if (ret == null) {
            throw new IllegalStateException("can not get dataSource for BootstrapLock");
        }
        return ret;
    }

    public boolean tryLock() {
        init();
        String sql = "INSERT INTO bootstrap_lock (lock_key, client_id) VALUES('bootstrap', ?) "
                + "ON DUPLICATE KEY UPDATE lock_key = lock_key";
        try {
            boolean ret = SQLUtils.execute(dataSource(), sql, clientId) > 0;
            if (ret) {
                log.info("BootstrapLock locked");
                Runtime.getRuntime().addShutdownHook(new Thread(this::unlock, "unlock BootstrapLock"));
            } else {
                String owner = getOwner();
                if (clientId.equals(owner)) {
                    log.info("is locked by myself");
                    return true;
                }
                log.info("get BootstrapLock failed. current owner: {}", owner);
            }
            return ret;
        } catch (SQLException e) {
            throw new IllegalStateException("tryLock got exception", e);
        }
    }

    public boolean unlock() {
        init();
        String sql = "DELETE FROM bootstrap_lock WHERE lock_key = 'bootstrap' AND client_id = ?";
        try {
            boolean ret = SQLUtils.execute(dataSource(), sql, clientId) > 0;
            if (ret) {
                log.info("BootstrapLock unlocked");
            } else {
                String owner = getOwner();
                log.info("unlock BootstrapLock failed. current owner: {}", owner);
            }
            return ret;
        } catch (SQLException e) {
            throw new IllegalStateException("unlock got exception", e);
        }
    }

    public String getOwner() {
        init();
        String sql = "SELECT client_id FROM bootstrap_lock WHERE lock_key = 'bootstrap'";
        try {
            List<Row> rows = SQLUtils.queryRows(dataSource(), sql);
            if (rows.isEmpty()) {
                return null;
            }
            return (String) rows.get(0).getFirstField();
        } catch (SQLException e) {
            throw new IllegalStateException("getOwner got exception", e);
        }
    }

}
