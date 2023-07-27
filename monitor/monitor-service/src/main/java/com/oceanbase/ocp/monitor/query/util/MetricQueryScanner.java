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
package com.oceanbase.ocp.monitor.query.util;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.oceanbase.ocp.common.concurrent.OcpThreadFactory;
import com.oceanbase.ocp.monitor.model.OcpPrometheusQueryParam;
import com.oceanbase.ocp.monitor.model.OcpPrometheusScanResp;
import com.oceanbase.ocp.monitor.service.OcpPrometheusScanBusinessImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MetricQueryScanner {

    @Autowired
    private OcpPrometheusScanBusinessImpl scanBusiness;

    private static final AtomicLong POOL_ID = new AtomicLong();

    public Future<List<OcpPrometheusScanResp>> scan(OcpPrometheusQueryParam param) {
        ThreadPoolExecutor scanPool = getScanPool();
        return scanPool.submit(() -> scanBusiness.scan(param));
    }

    private ThreadPoolExecutor getScanPool() {
        return new ThreadPoolExecutor(0, 1, 0, TimeUnit.SECONDS, new SynchronousQueue<>(),
                new OcpThreadFactory("scanner-" + POOL_ID.getAndIncrement() + "-"));
    }

}
