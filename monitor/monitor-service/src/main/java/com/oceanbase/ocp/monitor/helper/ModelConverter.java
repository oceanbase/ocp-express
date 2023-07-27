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
package com.oceanbase.ocp.monitor.helper;

import com.oceanbase.ocp.monitor.model.MetricClass;
import com.oceanbase.ocp.monitor.model.MetricGroup;
import com.oceanbase.ocp.monitor.model.MetricMeta;
import com.oceanbase.ocp.monitor.storage.repository.MetricClassEntity;
import com.oceanbase.ocp.monitor.storage.repository.MetricGroupEntity;
import com.oceanbase.ocp.monitor.storage.repository.MetricMetaEntity;

public class ModelConverter {

    public static MetricClass fromEntity(MetricClassEntity entity) {
        if (entity == null) {
            return null;
        }
        MetricClass metricClass = new MetricClass();
        metricClass.setId(entity.getId());
        metricClass.setKey(entity.getKey());
        metricClass.setName(entity.getName());
        metricClass.setNameEn(entity.getNameEn());
        metricClass.setDescription(entity.getDescription());
        metricClass.setDescriptionEn(entity.getDescriptionEn());
        metricClass.setScope(entity.getScope());
        metricClass.setType(entity.getType());
        return metricClass;
    }

    public static MetricGroup fromEntity(MetricGroupEntity entity) {
        if (entity == null) {
            return null;
        }
        MetricGroup group = new MetricGroup();
        group.setId(entity.getId());
        group.setKey(entity.getKey());
        group.setClassKey(entity.getClassKey());
        group.setName(entity.getName());
        group.setDescription(entity.getDescription());
        group.setWithLabel(entity.getWithLabel());
        return group;
    }

    public static MetricMeta fromEntity(MetricMetaEntity entity) {
        if (entity == null) {
            return null;
        }
        MetricMeta meta = new MetricMeta();
        meta.setId(entity.getId());
        meta.setKey(entity.getKey());
        meta.setName(entity.getName());
        meta.setDescription(entity.getDescription());
        meta.setUnit(entity.getUnit());
        meta.setDisplayByDefault(entity.isDisplayByDefault());
        meta.setMinObVersion(entity.getMinObVersion());
        meta.setMaxObVersion(entity.getMaxObVersion());
        return meta;
    }

}
