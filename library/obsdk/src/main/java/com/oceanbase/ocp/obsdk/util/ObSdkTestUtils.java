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

package com.oceanbase.ocp.obsdk.util;

import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServer;

public class ObSdkTestUtils {

    public static ObServer buildNormalObServer(String svrIp, Integer svrPort) {
        ObServer obServer = new ObServer();
        obServer.setZone("zone1");
        obServer.setSvrIp(svrIp);
        obServer.setSvrPort(svrPort);
        obServer.setStatus("active");
        obServer.setStartServiceTime(1643096545746848L);
        obServer.setStopTime(0L);
        return obServer;
    }

    public static ObServer buildStoppedObServer(String svrIp, Integer svrPort) {
        ObServer obServer = new ObServer();
        obServer.setZone("zone1");
        obServer.setSvrIp(svrIp);
        obServer.setSvrPort(svrPort);
        obServer.setStatus("active");
        obServer.setStartServiceTime(1643096545746848L);
        obServer.setStopTime(1643096547746848L);
        return obServer;
    }

    public static ObServer buildInactiveObServer(String svrIp, Integer svrPort) {
        ObServer obServer = new ObServer();
        obServer.setZone("zone1");
        obServer.setSvrIp(svrIp);
        obServer.setSvrPort(svrPort);
        obServer.setStatus("inactive");
        obServer.setStartServiceTime(0L);
        obServer.setStopTime(0L);
        return obServer;
    }
}
