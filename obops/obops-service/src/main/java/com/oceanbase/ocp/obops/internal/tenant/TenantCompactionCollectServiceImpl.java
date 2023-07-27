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

package com.oceanbase.ocp.obops.internal.tenant;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oceanbase.ocp.core.ob.constant.TenantCompactionResult;
import com.oceanbase.ocp.core.ob.tenant.ObTenantCompactionEntity;
import com.oceanbase.ocp.core.ob.tenant.ObTenantEntity;
import com.oceanbase.ocp.core.obsdk.ObOperatorFactory;
import com.oceanbase.ocp.obops.internal.tenant.repository.ObTenantCompactionRepository;
import com.oceanbase.ocp.obops.tenant.TenantCompactionCollectService;
import com.oceanbase.ocp.obsdk.operator.CompactionOperator;
import com.oceanbase.ocp.obsdk.operator.compaction.model.ObTenantCompaction;
import com.oceanbase.ocp.obsdk.operator.compaction.model.ObTenantCompactionStatus;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TenantCompactionCollectServiceImpl implements TenantCompactionCollectService {

    @Autowired
    private ObTenantCompactionRepository compactionRepository;

    @Autowired
    private ObOperatorFactory obOperatorFactory;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectMajorCompaction(ObTenantEntity tenant) {
        log.info("Begin to collect major compaction info for tenant:{}", tenant.getName());
        CompactionOperator compactionOperator = obOperatorFactory.createObOperator().compaction();
        ObTenantCompaction compaction = compactionOperator.getObTenantCompaction(tenant.getObTenantId());
        List<ObTenantCompactionEntity> entities = compactionRepository.listRecentCompactions(tenant.getObTenantId(), 1);
        ObTenantCompactionEntity entity = null;
        boolean createNewOne = false;
        if (compaction.getFrozenScn() == 1L || compaction.getStartTime().toEpochSecond() == 0) {
            log.info("Cluster never major compaction, just skip");
            return;
        }
        if (CollectionUtils.isEmpty(entities)) {
            createNewOne = true;
        } else {
            entity = entities.get(0);
            log.info("current compaction frozen scn:{}, broadcast scn:{}, saved frozen scn:{}",
                    compaction.getFrozenScn(), compaction.getBroadcastScn(), entity.getFrozenScn());
            if (entity.getFrozenScn().compareTo(compaction.getFrozenScn()) < 0) {
                log.info("attach new compaction, ready to create compaction info");
                createNewOne = true;
            }
        }

        if (createNewOne) {
            entity = createMajorCompaction(tenant.getObTenantId(), compaction);
            log.info("create compaction info success, compaction info:{}", entity);
        }

        if (ObTenantCompactionStatus.IDLE.equals(compaction.getStatus())) {
            entity.setLastFinishTime(compaction.getLastFinishTime());
        }
        entity.setStatus(TenantCompactionResult.fromCompaction(compaction));
        compactionRepository.save(entity);
        log.info("Finish to collect major compaction info for tenant:{}", tenant.getName());
    }

    private ObTenantCompactionEntity createMajorCompaction(Long obTenantId, ObTenantCompaction compaction) {
        ObTenantCompactionEntity compactionEntity = mapToEntity(obTenantId, compaction);
        return compactionRepository.save(compactionEntity);
    }

    public static ObTenantCompactionEntity mapToEntity(Long obTenantId, ObTenantCompaction compaction) {
        ObTenantCompactionEntity entity = new ObTenantCompactionEntity();
        entity.setObTenantId(obTenantId);
        entity.setStatus(TenantCompactionResult.fromCompaction(compaction));
        entity.setFrozenScn(compaction.getFrozenScn());
        entity.setStartTime(compaction.getStartTime());
        entity.setLastFinishTime(compaction.getLastFinishTime());
        return entity;
    }
}
