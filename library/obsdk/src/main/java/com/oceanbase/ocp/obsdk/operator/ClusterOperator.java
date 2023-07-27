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

package com.oceanbase.ocp.obsdk.operator;

import java.sql.Timestamp;
import java.util.List;

import com.oceanbase.ocp.obsdk.operator.cluster.model.ObCharset;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObCollation;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObServer;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObZone;
import com.oceanbase.ocp.obsdk.operator.cluster.model.RootService;
import com.oceanbase.ocp.obsdk.operator.cluster.model.RootServiceEvent;

/**
 * Under the system tenant, cluster management related operations
 *
 */
public interface ClusterOperator {

    /**
     * Get the version
     */
    String getObVersion();

    /**
     * List all zones
     *
     * @return all zones in cluster
     */
    List<ObZone> listZones();

    /**
     * List all servers
     *
     * @return all servers in cluster
     */
    List<ObServer> listServers();

    /**
     * Obtain the root service list
     *
     * @return
     */
    List<RootService> listRootService();

    /**
     * show charset of cluster
     *
     * @return list of {@link ObCharset}
     */
    List<ObCharset> showCharset();

    /**
     * show collation of cluster
     *
     * @return list of {@link ObCollation}
     */
    List<ObCollation> showCollation();


    /**
     * List the operation records of the specified UNIT within the time range
     *
     * @param startTime if not null, where event.gmt_create > :startTime
     * @param endTime if not null, where event.gmt_create < :endTIme
     */
    List<RootServiceEvent> listUnitEventDesc(List<Long> obUnitIds, Timestamp startTime, Timestamp endTime);

    /**
     * Check ob cluster is community edition
     */
    boolean isCommunityEdition();
}
