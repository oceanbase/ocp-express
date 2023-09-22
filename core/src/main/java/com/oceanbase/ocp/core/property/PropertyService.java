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
package com.oceanbase.ocp.core.property;

import com.oceanbase.ocp.common.util.HostUtils;
import com.oceanbase.ocp.core.event.SystemParameterNotTakenEffectEvent;
import com.oceanbase.ocp.core.exception.NotFoundException;
import com.oceanbase.ocp.core.property.dao.PropertyRepository;
import com.oceanbase.ocp.core.property.entity.PropertyEntity;
import com.oceanbase.ocp.core.property.model.PropertyMeta;
import com.oceanbase.ocp.core.util.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.Sanitizer;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PropertyService {

    private final Sanitizer sanitizer = new Sanitizer();

    private final Map<String, String> runningProperties = new HashMap<>();

    @Autowired
    Environment environment;
    @Autowired
    private PropertyRepository propertyRepository;
    @Autowired
    private RefreshEndpoint refreshEndpoint;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @PostConstruct
    private void init() {
        propertyRepository.findAll().forEach(pair -> runningProperties.put(pair.getKey(), getPropertyValue(pair)));
    }

    /**
     * Refresh properties every 3 minutes.
     */
    @Scheduled(cron = "0 */3 * * * *")
    public void refreshConfig() {
        log.info("Refresh OCP properties.");
        refresh();
    }

    /**
     * Find all visible properties.
     */
    public Page<PropertyMeta> findAllVisibleProperties(String keyLike, Pageable pageable) {
        return propertyRepository.findAllByVisible(keyLike, pageable).map(this::toDto);
    }

    /**
     * Update an existing, non-fatal property.
     */
    public PropertyMeta updateProperty(long id, String newValue) throws NotFoundException {
        return updateProperty(id, newValue, false);
    }

    /**
     * Update an existing, non-fatal property.
     */
    public PropertyMeta updateProperty(long id, String newValue, boolean withFatal) throws NotFoundException {
        Optional<PropertyEntity> propertyEntity;
        if (withFatal) {
            propertyEntity = propertyRepository.findById(id);
        } else {
            propertyEntity = propertyRepository.findByIdAndFatalFalse(id);
        }
        ExceptionUtils.notFound(propertyEntity.isPresent(), PropertyEntity.class.getSimpleName(), id);
        String sanitizedValue =
                (String) sanitizer.sanitize(propertyEntity.get().getKey(), getPropertyValue(propertyEntity.get()));
        if (sanitizedValue != null && sanitizedValue.equals(newValue)) {
            return toDto(propertyEntity.get());
        }
        PropertyEntity p = propertyEntity.get();
        p.setValue(format(p.getKey(), newValue));
        PropertyEntity updated = propertyRepository.saveAndFlush(p);
        log.info("Updated property, key={}, previousValue={}, updatedValue={}",
                p.getKey(), p.getValue(), updated.getValue());
        return toDto(updated);
    }

    public String getProperty(String key) {
        return getProperty(key, "ocp", "default", "master");
    }

    public String getProperty(String key, String application, String profile, String label) {
        Optional<PropertyEntity> optionalEntity =
                propertyRepository.findByKeyAndApplicationAndProfileAndLabel(key, application, profile, label);
        if (optionalEntity.isPresent()) {
            PropertyEntity entity = optionalEntity.get();
            return getPropertyValue(entity);
        } else {
            return null;
        }
    }

    public void refresh() {
        log.info("Refresh config properties");
        refreshEndpoint.refresh();

        List<PropertyEntity> propertyEntities = propertyRepository.findAll();

        // update runningProperties
        propertyEntities.stream()
                .filter(t -> !t.needRestart())
                .forEach(t -> runningProperties.put(t.getKey(), getPropertyValue(t)));

        // filter need restart parameters
        List<String> needRestartParams = propertyEntities.stream()
                .filter(PropertyEntity::needRestart)
                .filter(t -> !StringUtils.equals(getPropertyValue(t), runningProperties.get(t.getKey())))
                .map(PropertyEntity::getKey)
                .collect(Collectors.toList());

        // send alarm
        if (CollectionUtils.isNotEmpty(needRestartParams)) {
            HashMap<String, String> labels = new HashMap<>();
            labels.put("svr_ip", HostUtils.getLocalIp());
            labels.put("system_parameter", needRestartParams.toString());
            eventPublisher.publishEvent(new SystemParameterNotTakenEffectEvent(labels));
        }

        log.info("Refresh config properties done");
    }

    public Optional<PropertyEntity> safeGetByKey(String key) {
        return propertyRepository.findByKeyAndApplicationAndProfileAndLabel(key, "ocp", "default", "master");
    }

    private String format(String name, String value) {
        // Don't allow name to be null, but value could be null
        Validate.notNull(name);

        return StringUtils.isNotBlank(value) ? value.trim() : null;
    }

    private PropertyMeta toDto(PropertyEntity entity) {
        String runningValue = runningProperties.get(entity.getKey());
        return PropertyMeta.builder()
                .id(entity.getId())
                .key(entity.getKey())
                .application(entity.getApplication())
                .profile(entity.getProfile())
                .label(entity.getLabel())
                .value((String) sanitizer.sanitize(entity.getKey(), getPropertyValue(entity)))
                .runningValue(runningValue)
                .needRestart(entity.getNeedRestart())
                .fatal(entity.getFatal())
                .description(entity.getDescription())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    private String getPropertyValue(PropertyEntity entity) {
        return entity.getValue() == null ? entity.getDefaultValue() : entity.getValue();
    }

}
