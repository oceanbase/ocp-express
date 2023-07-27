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

package com.oceanbase.ocp.obops.cluster;

import java.util.List;
import java.util.Map;

import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.obops.cluster.model.Charset;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObCollation;

public interface ClusterCharsetService {

    List<Charset> listCharsets(TenantMode tenantMode);

    List<ObCollation> listCollations();

    Map<Long, ObCollation> getCollationMap();
}
