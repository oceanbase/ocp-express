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

package com.oceanbase.ocp.monitor.query;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheLoader;

import com.oceanbase.ocp.common.concurrent.OcpThreadFactory;
import com.oceanbase.ocp.common.util.ExecutorUtils;
import com.oceanbase.ocp.common.util.trace.TraceDecorator;
import com.oceanbase.ocp.monitor.MetricQueryService;
import com.oceanbase.ocp.monitor.calculate.OcpPrometheusAstNode;
import com.oceanbase.ocp.monitor.calculate.parser.OcpPrometheusQlParser;
import com.oceanbase.ocp.monitor.helper.MonitorMetaMapContainer;
import com.oceanbase.ocp.monitor.model.OcpPrometheusQueryResult;
import com.oceanbase.ocp.monitor.param.MetricQueryParams;
import com.oceanbase.ocp.monitor.param.OcpPrometheusLabel;
import com.oceanbase.ocp.monitor.query.util.QueryParamAdapter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricQueryServiceImpl implements MetricQueryService {

    private final MonitorMetaMapContainer monitorMetaMapContainer;

    public MetricQueryServiceImpl(MonitorMetaMapContainer monitorMetaMapContainer) {
        this.monitorMetaMapContainer = monitorMetaMapContainer;
    }

    @Override
    public Map<String, List<OcpPrometheusQueryResult>> queryMetricBatch(MetricQueryParams params) {
        Map<String, Future<List<OcpPrometheusQueryResult>>> futureMap = new HashMap<>();
        Map<String, List<OcpPrometheusQueryResult>> resultMap = new HashMap<>();
        ThreadPoolExecutor executor = null;
        try {
            executor = getExecutor(params.getMetrics().size());
            for (String metric : params.getMetrics()) {
                try {
                    QueryParamAdapter.AdaptedParams adaptedParams =
                            getAdaptedParams(metric, params.getLabels(), params.getPrometheusLabels(),
                                    params.getInterval(), params.getGroupBy(), params.getStartTimestamp(),
                                    params.getEndTimestamp(), params.getMinStep(), params.getMaxPoints());
                    Future<List<OcpPrometheusQueryResult>> future = executor.submit(new TraceDecorator()
                            .decorate(() -> doQuery(adaptedParams.getExpression(), adaptedParams.getStartTime(),
                                    adaptedParams.getEndTime(), adaptedParams.getStep())));
                    futureMap.put(metric, future);
                } catch (CacheLoader.InvalidCacheLoadException e) {
                    log.error("query metric failed, metric key may not exists, message={}", e.getMessage());
                } catch (Exception e) {
                    log.error("query metric async :{}, labelMap:{}, prometheusLabels:{}, gbLabels:{}. ex: {}",
                            metric, params.getLabels(), params.getPrometheusLabels(), params.getGroupBy(), e);
                }
            }
            for (String k : futureMap.keySet()) {
                try {
                    resultMap.put(k, futureMap.get(k).get(2, TimeUnit.MINUTES));
                } catch (Exception e) {
                    log.error("get metric data metric:{}, labelMap:{}, prometheusLabels:{}, gbLabels:{}. ex: {}",
                            k, params.getLabels(), params.getPrometheusLabels(), params.getGroupBy(), e);
                }
            }
        } finally {
            if (executor != null) {
                ExecutorUtils.shutdown(executor, 1);
            }
        }
        return resultMap;
    }

    private QueryParamAdapter.AdaptedParams getAdaptedParams(String metric, Map<String, String> labelMap,
            List<OcpPrometheusLabel> prometheusLables,
            long interval, List<String> gbLabels, long startTime, long endTime, long minStep, Long maxPoints) {
        String expr = monitorMetaMapContainer.getExprByMetric(metric);
        return new QueryParamAdapter(expr, labelMap, prometheusLables, interval, gbLabels, startTime, endTime, minStep,
                maxPoints).adapt();
    }

    private ThreadPoolExecutor getExecutor(int coreSize) {
        return new ThreadPoolExecutor(coreSize, coreSize, 1, TimeUnit.SECONDS, new SynchronousQueue<>(),
                new OcpThreadFactory("metric-query"));
    }

    private List<OcpPrometheusQueryResult> doQuery(String expr, long startTime, long endTime, long step)
            throws Exception {
        OcpPrometheusQlParser ocpPrometheusQlParser = new OcpPrometheusQlParser(new StringReader(expr));
        OcpPrometheusAstNode parseTree = ocpPrometheusQlParser.parse();
        return parseTree.eval(startTime, endTime, step);
    }

}
