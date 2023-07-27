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

package com.oceanbase.ocp.obops.internal.cluster;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.core.ob.tenant.TenantMode;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.obops.cluster.ClusterCharsetService;
import com.oceanbase.ocp.obops.cluster.model.Charset;
import com.oceanbase.ocp.obops.cluster.model.Collation;
import com.oceanbase.ocp.obsdk.operator.ClusterOperator;
import com.oceanbase.ocp.obsdk.operator.ObOperator;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObCharset;
import com.oceanbase.ocp.obsdk.operator.cluster.model.ObCollation;

@Service
public class ClusterCharsetServiceImpl implements ClusterCharsetService {

    @Autowired
    private ObOperatorFactory obOperatorFactory;

    @Override
    public List<Charset> listCharsets(TenantMode tenantMode) {
        ClusterOperator operator = obOperatorFactory.createObOperator().cluster();
        List<ObCollation> obCollations = operator.showCollation();
        return operator.showCharset()
                .stream()
                .filter(c -> filterCharset(c, tenantMode))
                .map(c -> buildCharset(c, obCollations))
                .collect(Collectors.toList());
    }

    @Override
    public List<ObCollation> listCollations() {
        ObOperator operator = obOperatorFactory.createObOperator();
        return operator.cluster().showCollation();
    }

    @Override
    public Map<Long, ObCollation> getCollationMap() {
        List<ObCollation> collations = listCollations();
        return collations.stream().collect(Collectors.toMap(ObCollation::getId, t -> t));
    }

    private Collation buildCollation(ObCollation obCollation) {
        Collation collation = new Collation();
        collation.setName(obCollation.getCollation());
        collation.setIsDefault("Yes".equalsIgnoreCase(obCollation.getIsDefault()));
        return collation;
    }

    private Charset buildCharset(ObCharset obCharset, List<ObCollation> obCollations) {
        Charset charset = new Charset();
        charset.setName(obCharset.getCharset());
        charset.setDescription(obCharset.getDescription());
        charset.setMaxLen(obCharset.getMaxLen());
        List<Collation> collations = obCollations.stream()
                .filter(c -> StringUtils.equals(c.getCharset(), obCharset.getCharset()))
                .map(this::buildCollation)
                .collect(Collectors.toList());
        charset.setCollations(collations);
        return charset;
    }

    private boolean filterCharset(ObCharset charset, TenantMode tenantMode) {
        if (TenantMode.ORACLE.equals(tenantMode)) {
            return supportOracle(charset);
        } else {
            return supportMysql(charset);
        }
    }

    private boolean supportOracle(ObCharset charset) {
        if (StringUtils.equals("binary", charset.getCharset())) {
            return false;
        }
        if (StringUtils.equals("utf16", charset.getCharset())) {
            return false;
        }
        return true;
    }

    private boolean supportMysql(ObCharset charset) {
        if (StringUtils.equals("utf16", charset.getCharset())) {
            return false;
        }
        return true;
    }

}
