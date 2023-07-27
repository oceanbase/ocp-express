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
package com.oceanbase.ocp.common.pattern;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Retry {

    public static <T> T executeUntilWithLimit(Supplier<T> supplier, Predicate<T> endPredicate,
            int intervalSeconds, final int maxRetryTimes) {
        Validate.isTrue(intervalSeconds >= 0, "intervalSeconds must >= 0");
        Validate.isTrue(maxRetryTimes > 0, "maxRetryTimes must > 0");
        int tryTimes = 0;
        do {
            T result = supplier.get();
            if (endPredicate.test(result)) {
                return result;
            }
            if (intervalSeconds > 0) {
                waitFor(intervalSeconds);
            }
        } while (++tryTimes < maxRetryTimes);
        throw new RuntimeException("result not match after try " + tryTimes + " times");
    }

    public static <T> T executeUntilWithTimeout(Supplier<T> supplier, Predicate<T> endPredicate,
            int intervalSeconds, final Duration timeout) {
        Validate.isTrue(intervalSeconds >= 0, "intervalSeconds must >= 0");
        Validate.isTrue(timeout != null, "timeout must > 0");
        long timeToDie = System.currentTimeMillis() + timeout.toMillis();
        do {
            T result = supplier.get();
            if (endPredicate.test(result)) {
                return result;
            }
            if (intervalSeconds > 0) {
                waitFor(intervalSeconds);
            }
        } while (System.currentTimeMillis() < timeToDie);
        throw new RuntimeException("result not match until timeout: " + timeout.toMillis() + " ms");
    }

    private static void waitFor(int seconds) {
        try {
            log.info("wait for {} seconds", seconds);
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            throw new RuntimeException("interrupted, msg:" + e.getMessage());
        }
    }
}
