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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Delegating for lazy-loading objects.
 *
 * @param <T> type of object
 */
public final class Lazy<T> implements Supplier<T> {

    private final Supplier<T> supplier;
    private final AtomicBoolean initialized = new AtomicBoolean();
    private volatile T value;

    public Lazy(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        this.supplier = supplier;
    }

    /**
     * Whether object initialized.
     *
     * @return boolean, true for initialized
     */
    public boolean isInitialized() {
        return initialized.get();
    }

    /**
     * Get initialized object.
     */
    public T get() {
        if (initialized.get()) {
            return value;
        }
        synchronized (initialized) {
            if (initialized.get()) {
                return value;
            }
            value = supplier.get();
            initialized.set(true);
        }
        return value;
    }

    /**
     * Delegating method for construct lazy-loading object.
     *
     * @param supplier supplier to construct object
     * @param <T> type of object
     * @return Lazy-loading object
     */
    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<T>(supplier);
    }
}
