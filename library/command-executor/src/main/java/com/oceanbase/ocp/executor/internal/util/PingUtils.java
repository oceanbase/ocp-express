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

package com.oceanbase.ocp.executor.internal.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.Validate;

import com.oceanbase.ocp.executor.executor.AgentExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PingUtils {

    /**
     * Get average network rtt by http.
     */
    public static Optional<Long> getAvgRttByHttp(AgentExecutor executor, int loopCount, int minSuccessCount) {
        String ipAddress = executor.getConnectProperties().getHostAddress();
        Integer httpPort = executor.getConnectProperties().getHttpPort();
        log.info("Rtt by http, ip={}, port={}, loopCount={}, minSuccessCount={}",
                ipAddress, httpPort, loopCount, minSuccessCount);
        validateInputArgs(loopCount, minSuccessCount);
        List<Long> rawRttList = loopGetRtt(() -> PingUtils.trySendRequest(executor), loopCount);
        log.info("Rtt by http, rawList={}", rawRttList);
        return getPositiveValueAverage(rawRttList, minSuccessCount);
    }

    private static void validateInputArgs(int loopCount, int minSuccessCount) {
        Validate.isTrue(loopCount > 0, "Loop count must be greater than zero.");
        Validate.isTrue(minSuccessCount >= 0, "Minimum success count must be non-negative.");
        Validate.isTrue(loopCount >= minSuccessCount, "Minimum success count must less than or equal to loop count.");
    }

    private static List<Long> loopGetRtt(Supplier<Boolean> function, int loopCount) {
        Validate.isTrue(loopCount > 0, "Loop count must be positive.");
        return IntStream.rangeClosed(1, loopCount).mapToObj(value -> {
            long rtt = -1L;
            try {
                long startTs = System.currentTimeMillis();
                boolean success = function.get();
                long endTs = System.currentTimeMillis();
                if (success) {
                    rtt = endTs - startTs;
                }
            } catch (Exception e) {
                log.warn("Get rtt fail.", e);
            }
            return rtt;
        }).collect(Collectors.toList());
    }

    private static boolean trySendRequest(AgentExecutor executor) {
        try {
            executor.getHostTime();
            return true;
        } catch (Exception ex) {
            log.warn("Http Request failed, reason:{}, cause:{}", ex.getMessage(), ex);
            return false;
        }
    }

    private static Optional<Long> getPositiveValueAverage(List<Long> rawList, int minSuccessCount) {
        Validate.isTrue(minSuccessCount >= 0, "Loop count must be non-negative.");
        List<Long> legalList = rawList.stream().filter(aLong -> aLong >= 0).collect(Collectors.toList());
        if (legalList.size() < minSuccessCount) {
            log.warn("Check success rtt count fail. minSuccessCount={}, successCount={}",
                    minSuccessCount, rawList.size());
            return Optional.empty();
        }
        return legalList.stream().reduce(Long::sum).map(aLong -> aLong / legalList.size());
    }

}
