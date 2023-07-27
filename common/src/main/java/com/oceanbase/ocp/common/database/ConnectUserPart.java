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

package com.oceanbase.ocp.common.database;

import org.apache.commons.lang3.Validate;

import lombok.Data;

@Data
public class ConnectUserPart {

    /**
     * Username of OB, not-empty.
     */
    public final String username;

    /**
     * Tenant name of OB, not-empty.
     */
    public final String tenant;
    /**
     * Cluster name of OB.
     */
    public final String cluster;

    public static ConnectUserPart of(String connectUser) {
        Validate.notBlank(connectUser, "Connect-User require not blank");
        int at = connectUser.indexOf("@");
        if (at < 0) {
            throw new IllegalArgumentException("Illegal Connect-User '%s': missing '@'");
        } else if (at == 0) {
            throw new IllegalArgumentException("Illegal Connect-User '%s': can't starts with '@'");
        } else if (at == connectUser.length() - 1) {
            throw new IllegalArgumentException("Illegal Connect-User '%s': can't ends with '@'");
        }
        int hash = connectUser.indexOf("#");
        String username = connectUser.substring(0, at);
        if (hash < 0) {
            String tenant = connectUser.substring(at + 1);
            return new ConnectUserPart(username, tenant, null);
        } else if (hash < at) {
            throw new IllegalArgumentException("Illegal Connect-User '%s': '#' before '@'");
        } else if (hash == connectUser.length() - 1) {
            throw new IllegalArgumentException("Illegal Connect-User '%s': can't end with '#'");
        } else {
            String tenant = connectUser.substring(at + 1, hash);
            String cluster = connectUser.substring(hash + 1);
            return new ConnectUserPart(username, tenant, cluster);
        }
    }
}
