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

package com.oceanbase.ocp.monitor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import com.oceanbase.ocp.monitor.constants.MonitorConstants;
import com.oceanbase.ocp.monitor.helper.MonitorMetaMapContainer;
import com.oceanbase.ocp.monitor.model.OcpPrometheusQueryParam;
import com.oceanbase.ocp.monitor.query.MetricQueryServiceImpl;
import com.oceanbase.ocp.monitor.service.SeriesIdKeyService;
import com.oceanbase.ocp.monitor.storage.IRollupMetricDataDao;
import com.oceanbase.ocp.monitor.storage.MetricDataDao;
import com.oceanbase.ocp.monitor.storage.MetricDataWriteQueue;
import com.oceanbase.ocp.monitor.storage.RollupMetricDataDao;
import com.oceanbase.ocp.monitor.store.IntervalMetricDataCache;
import com.oceanbase.ocp.monitor.store.MetricDataStore;
import com.oceanbase.ocp.monitor.store.RollupMetricDataStore;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

@AutoConfigureAfter({DataSourceAutoConfiguration.class})
@Configuration
@ConditionalOnProperty(value = "ocp.monitor.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(MonitorProperties.class)
@Slf4j
public class MonitorAutoConfiguration {

    @PostConstruct
    public void init() {
        log.info("ocp-monitor bean configuration done");
    }

    @Bean("metricQueryService")
    public MetricQueryService metricQueryService(MonitorMetaMapContainer monitorMetaMapContainer) {
        return new MetricQueryServiceImpl(monitorMetaMapContainer);
    }

    @Bean("scanSeriesIdsCache")
    public Cache<OcpPrometheusQueryParam, List<Long>> scanSeriesIdsCache() {
        return Caffeine.newBuilder().maximumSize(MonitorConstants.SCAN_SERIES_ID_CACHE_SIZE)
                .expireAfterWrite(MonitorConstants.SCAN_SERIES_ID_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES).build();
    }

    @Configuration
    static class SecondConfiguration {

        @Bean
        @Profile("!test")
        public IRollupMetricDataDao secondPersistent(@Qualifier("jdbcTemplate") JdbcTemplate monitorJdbcTemplate,
                @Qualifier("namedJdbcTemplate") NamedParameterJdbcTemplate monitorNamedJdbcTemplate) {
            return new RollupMetricDataDao(MonitorConstants.TABLE_NAME_SECOND_DATA, monitorJdbcTemplate,
                    monitorNamedJdbcTemplate);
        }

        @Bean("secondStore")
        public IIntervalMetricDataStore secondStore(IntervalMetricDataCache secondWriteCache,
                IRollupMetricDataDao secondPersistent, SeriesIdKeyService seriesIdKeyService,
                MeterRegistry meterRegistry) {
            return new RollupMetricDataStore(secondWriteCache, secondPersistent, seriesIdKeyService, meterRegistry);
        }

        @Bean
        @Profile("!test")
        public IntervalMetricDataCache secondWriteCache(MetricDataWriteQueue secondWriteQueue,
                MeterRegistry meterRegistry) {
            IntervalMetricDataCache cache = new IntervalMetricDataCache(MonitorConstants.SECOND_READ_CACHE_SIZE,
                    MonitorConstants.MINUTE_SECONDS, secondWriteQueue, 10);
            cache.startMeter(meterRegistry);
            return cache;
        }

        @Bean
        public MetricDataWriteQueue secondWriteQueue(MeterRegistry meterRegistry) {
            MetricDataWriteQueue writeQueue = new MetricDataWriteQueue(MonitorConstants.SECOND_WRITE_QUEUE_CAPACITY);
            writeQueue.startMeter(meterRegistry);
            return writeQueue;
        }

    }

    @Configuration
    static class MinuteConfiguration {

        @Bean
        @Profile("!test")
        public IRollupMetricDataDao minutePersistent(@Qualifier("jdbcTemplate") JdbcTemplate monitorJdbcTemplate,
                @Qualifier("namedJdbcTemplate") NamedParameterJdbcTemplate monitorNamedJdbcTemplate) {
            return new MetricDataDao(MonitorConstants.TABLE_NAME_MINUTE_DATA, monitorJdbcTemplate,
                    monitorNamedJdbcTemplate);
        }

        @Bean("minuteStore")
        public IIntervalMetricDataStore minuteStore(IRollupMetricDataDao minutePersistent) {
            return new MetricDataStore(minutePersistent);
        }

    }

}
