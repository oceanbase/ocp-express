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
package com.oceanbase.ocp.monitor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.collections4.CollectionUtils;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oceanbase.ocp.monitor.ExporterService;
import com.oceanbase.ocp.monitor.MonitorProperties;
import com.oceanbase.ocp.monitor.constants.ExporterStatus;
import com.oceanbase.ocp.monitor.entity.ExporterAddressEntity;
import com.oceanbase.ocp.monitor.helper.ExporterRequestHelper;
import com.oceanbase.ocp.monitor.model.exporter.ExporterAddress;
import com.oceanbase.ocp.monitor.repository.ExporterRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ExporterServiceImpl implements ExporterService {

    @Autowired
    private ExporterRepository exporterRepository;
    @Autowired
    private MonitorProperties monitorProperties;
    @Autowired
    private ExporterRequestHelper exporterRequestHelper;

    private final Map<String, Integer> offlineCount = new ConcurrentHashMap<>();
    private final Map<String, ExporterStatus> exporterRecentStatus = new ConcurrentHashMap<>();

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void registerExporters(List<String> exporterUrls) {
        if (CollectionUtils.isEmpty(exporterUrls)) {
            return;
        }
        log.info("Register exporters, urls={}", exporterUrls);

        Set<String> existExporters = exporterRepository.findAll().stream()
                .map(ExporterAddressEntity::getExporterUrl)
                .collect(Collectors.toSet());

        Set<String> newExporters = exporterUrls.stream()
                .filter(s -> !existExporters.contains(s))
                .collect(Collectors.toSet());
        Set<String> toBeDeletedUrls = existExporters.stream()
                .filter(s -> !exporterUrls.contains(s))
                .collect(Collectors.toSet());
        List<ExporterAddressEntity> exporterEntities = new ArrayList<>();
        for (String url : newExporters) {
            ExporterAddressEntity exporter = ExporterAddressEntity.builder()
                    .exporterUrl(url)
                    .status(ExporterStatus.ONLINE)
                    .build();
            exporterEntities.add(exporter);
        }
        exporterRepository.saveAll(exporterEntities);
        exporterRepository.deleteAllByExporterUrlIn(toBeDeletedUrls);
    }

    @Override
    public List<ExporterAddress> getAllActiveExporters() {
        return exporterRepository.findAllByStatus(ExporterStatus.ONLINE).stream()
                .map(this::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void inactiveExporter(String exporterUrl) {
        if (exporterUrl == null) {
            return;
        }
        exporterRecentStatus.put(exporterUrl, ExporterStatus.OFFLINE);
        int alreadyOfflineCnt = offlineCount.getOrDefault(exporterUrl, 0);
        if (alreadyOfflineCnt >= monitorProperties.getMaxContinuousInactiveCount()) {
            log.info("Inactive exporter, exporterUrl = {}", exporterUrl);
            exporterRepository.updateStatusByExporterUrl(exporterUrl, ExporterStatus.OFFLINE);
            offlineCount.remove(exporterUrl);
        } else {
            offlineCount.put(exporterUrl, alreadyOfflineCnt + 1);
        }
    }

    @Override
    public void activeExporter(String exporterUrl) {
        if (exporterUrl == null) {
            return;
        }
        offlineCount.remove(exporterUrl);
        if (exporterRecentStatus.containsKey(exporterUrl)
                && exporterRecentStatus.get(exporterUrl) == ExporterStatus.ONLINE) {
            return;
        }
        exporterRecentStatus.put(exporterUrl, ExporterStatus.ONLINE);
        exporterRepository.updateStatusByExporterUrl(exporterUrl, ExporterStatus.ONLINE);
    }

    @Override
    public void validateInactiveExporters() {
        List<ExporterAddressEntity> inactiveExporters = exporterRepository.findAllByStatus(ExporterStatus.OFFLINE);
        if (inactiveExporters.isEmpty()) {
            return;
        }
        for (ExporterAddressEntity entity : inactiveExporters) {
            log.info("Validate inactive exporter, exporter={}, updateTime={}",
                    entity.getExporterUrl(), entity.getUpdateTime());
            String url = entity.getExporterUrl();
            if (healthCheck(url)) {
                log.info("Exporter is active, exporter={}, updateTime={}",
                        entity.getExporterUrl(), entity.getUpdateTime());
                exporterRepository.updateStatusByExporterUrl(url, ExporterStatus.ONLINE);
            }
        }
    }

    private boolean healthCheck(String exporterUrl) {
        try {
            ListenableFuture<Response> future = exporterRequestHelper.get(exporterUrl);
            return 200 == future.get().getStatusCode();
        } catch (Exception e) {
            return false;
        }
    }

    private ExporterAddress fromEntity(ExporterAddressEntity entity) {
        return ExporterAddress.builder()
                .id(entity.getId())
                .exporterUrl(entity.getExporterUrl())
                .status(entity.getStatus())
                .build();
    }

}
