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

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BufferedQueue<E> extends ConcurrentLinkedQueue<E> {

    private static final long serialVersionUID = -4687564300116973018L;

    private int maxBuffSize = Integer.MAX_VALUE;
    private final AtomicInteger buffSize = new AtomicInteger();

    public BufferedQueue(int maxBuffSize) {
        this.maxBuffSize = maxBuffSize;
    }

    @Override
    public boolean add(E e) {
        return offer(e);
    }

    @Override
    public boolean offer(E e) {
        if (buffSize.get() > maxBuffSize) {
            log.warn("Queue is full, maxSize={}, currentSize={}", maxBuffSize, buffSize.get());
            return false;
        }
        boolean added = super.offer(e);
        if (added) {
            buffSize.incrementAndGet();
        }
        return added;
    }

    @Override
    public E poll() {
        E poll = super.poll();
        if (poll != null) {
            buffSize.decrementAndGet();
        }
        return poll;
    }

    @Override
    public E peek() {
        return super.peek();
    }

    public int getBufSize() {
        return buffSize.get();
    }

}
