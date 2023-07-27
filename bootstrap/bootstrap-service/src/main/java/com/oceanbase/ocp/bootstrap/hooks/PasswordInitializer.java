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

package com.oceanbase.ocp.bootstrap.hooks;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.bootstrap.Bootstrap;
import com.oceanbase.ocp.bootstrap.config.env.Functions;
import com.oceanbase.ocp.bootstrap.core.Action;
import com.oceanbase.ocp.bootstrap.core.def.Row;
import com.oceanbase.ocp.bootstrap.db.DataSourceName;
import com.oceanbase.ocp.bootstrap.spi.AfterDataInitializationHook;
import com.oceanbase.ocp.bootstrap.util.SQLUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PasswordInitializer implements AfterDataInitializationHook {

    private static final String OCP_INITIAL_PASSWORD_ENV = "OCP_EXPRESS_ADMIN_PASSWD";

    /**
     * Password length 8-32. At least contains 2 digits, 2 lower letters, 2 upper
     * letters, two special characters.
     *
     * <ul>
     * <li>(?=(.*\\d){2,}) : at least two digits</li>
     * <li>(?=(.*[a-z]){2,}) : at least two lower case letters</li>
     * <li>(?=(.*[A-Z]){2,}) : at least two upper case letters</li>
     * <li>(?=(.*[~!@#%^&*_\\-+=|(){}\\[\\]:;,.?/]){2,}) : at least two special
     * characters</li>
     * </ul>
     */
    private static final String PASSWORD_PATTERN_EXPRESSION = "(" +
            "(?=(.*\\d){2,})" +
            "(?=(.*[a-z]){2,})" +
            "(?=(.*[A-Z]){2,})" +
            "(?=(.*[~!@#%^&*_\\-+=|(){}\\[\\]:;,.?/]){2,})" +
            "[0-9a-zA-Z~!@#%^&*_\\-+=|(){}\\[\\]:;,.?/]{8,32})";

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_PATTERN_EXPRESSION);

    @Override
    public void initialized(Action action, String dataSourceName) {
        if (!needChangeAdminPassword()) {
            log.info("Admin user password already changed, ignore set admin password.");
            return;
        }
        String adminRawPasswd = System.getenv(OCP_INITIAL_PASSWORD_ENV);
        if (StringUtils.isEmpty(adminRawPasswd)) {
            adminRawPasswd = System.getProperty(OCP_INITIAL_PASSWORD_ENV);
        }
        if (StringUtils.isEmpty(adminRawPasswd)) {
            throw new IllegalArgumentException(
                    "Admin password not specified, please check whether 'OCP_EXPRESS_ADMIN_PASSWD' " +
                            "existed in environment variable or VM property.");
        }

        validateAndSetPassword(adminRawPasswd);
    }

    private boolean needChangeAdminPassword() {
        DataSource dataSource = getDataSource();
        try {
            String sql = "SELECT username FROM `user` WHERE `id`=? and `need_change_password` = true;";
            List<Row> rows = SQLUtils.queryRows(dataSource, sql, 100L);
            return rows.size() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("Set initial password based on environment variables failed", e);
        }
    }

    private void validateAndSetPassword(String rawPasswd) {
        Validate.isTrue(PASSWORD_PATTERN.matcher(rawPasswd).matches(),
                "Password from environment variables or VM properties is not valid.");
        String encryptedPassword = Functions.bcryptHash(rawPasswd);

        log.info("Set initial password based on environment variables");
        DataSource dataSource = getDataSource();
        try {
            String sql = "UPDATE `user` SET `password` = ?, `need_change_password` = false WHERE `id` = ? " +
                    "AND `need_change_password` = true;";
            int execute = SQLUtils.execute(dataSource, sql, encryptedPassword, 100L);
            log.info("Update admin passwd, result={}", execute);
        } catch (SQLException e) {
            throw new IllegalStateException("Set initial password based on environment variables failed", e);
        }
    }

    private DataSource getDataSource() {
        Bootstrap bootstrap = Bootstrap.getInstance();
        return bootstrap.getDataSourceProvider().getDataSource(DataSourceName.SPRING);
    }

}
