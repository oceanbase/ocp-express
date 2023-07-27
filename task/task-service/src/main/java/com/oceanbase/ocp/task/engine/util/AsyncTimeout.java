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
package com.oceanbase.ocp.task.engine.util;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import com.oceanbase.ocp.task.entity.SubtaskInstanceOverview;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncTimeout {

    private static final long IDLE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(120);
    private static final long IDLE_TIMEOUT_NANOS = TimeUnit.MILLISECONDS.toNanos(IDLE_TIMEOUT_MILLIS);

    private final long timeoutNanos;

    @Getter
    private final long subtaskInstanceId;
    @Getter
    private final Future<SubtaskInstanceOverview> future;

    private boolean inQueue;

    static AsyncTimeout head;

    private AsyncTimeout next;

    private long startedAt;

    private long timeoutAt;

    public AsyncTimeout(long subtaskInstanceId, Future<SubtaskInstanceOverview> future, long timeout, TimeUnit unit) {
        validateArg(timeout, t -> t >= 0, "Timeout must be positive.");
        validateArg(unit, Objects::nonNull, "Timeunit must be not null.");
        this.subtaskInstanceId = subtaskInstanceId;
        this.future = future;
        this.timeoutNanos = unit.toNanos(timeout);
    }

    /**
     * Overwrite this method to execute operations after timeout.
     */
    public void callback() {}

    /**
     * Start the count-down.
     */
    public final void enter() {
        validateArg(inQueue, t -> !t, "Node already enqueued.");
        if (timeoutNanos == 0) {
            return;
        }
        inQueue = true;
        scheduleTimeout(this, timeoutNanos);
    }

    private long remainingNanos(long now) {
        return timeoutAt - now;
    }

    private <T> void validateArg(T t, Predicate<T> predicate, String errorMsg) {
        if (!predicate.test(t)) {
            throw new IllegalArgumentException(errorMsg);
        }
    }

    private static synchronized void scheduleTimeout(AsyncTimeout node, long timeoutNanos) {
        if (head == null) {
            head = new AsyncTimeout(-1L, null, 0, TimeUnit.NANOSECONDS);
            new Watchdog().start();
        }
        long now = System.nanoTime();
        node.timeoutAt = now + timeoutNanos;
        long remainingNanos = node.remainingNanos(now);

        // Insert and sort.
        for (AsyncTimeout prev = head; true; prev = prev.next) {
            if (prev.next == null || remainingNanos < prev.next.remainingNanos(now)) {
                node.next = prev.next;
                prev.next = node;
                if (prev == head) {
                    // If current timeout is shortest, then wake up queue.
                    AsyncTimeout.class.notify();
                }
                break;
            }
        }
        node.startedAt = System.nanoTime();
    }

    /**
     * Cancel subtask timeout by subtask instance id.
     *
     * @param subtaskInstanceId id
     */
    public static synchronized void cancelScheduledTimeout(long subtaskInstanceId) {
        log.debug("Cancel schedule timeout, subtaskInstanceId={}", subtaskInstanceId);
        for (AsyncTimeout prev = head; prev != null; prev = prev.next) {
            if (prev.next != null && prev.next.getSubtaskInstanceId() == subtaskInstanceId) {
                AsyncTimeout tmp = prev.next;
                prev.next = prev.next.next;
                tmp.next = null;
                return;
            }
        }
    }

    /**
     * Get subtask timeout by subtask instance id.
     *
     * @param subtaskInstanceId id
     * @return Subtask timeout
     */
    public static Optional<AsyncTimeout> getBySubtaskInstanceId(long subtaskInstanceId) {
        for (AsyncTimeout prev = head; prev != null; prev = prev.next) {
            if (prev.next != null && prev.next.getSubtaskInstanceId() == subtaskInstanceId) {
                return Optional.of(prev.next);
            }
        }
        return Optional.empty();
    }

    static AsyncTimeout awaitTimeout() throws InterruptedException {
        AsyncTimeout node = head.next;

        if (node == null) {
            long startNanos = System.nanoTime();
            AsyncTimeout.class.wait(IDLE_TIMEOUT_MILLIS);
            return head.next == null && (System.nanoTime() - startNanos) >= IDLE_TIMEOUT_NANOS ? head : null;
        }

        long waitNanos = node.remainingNanos(System.nanoTime());
        if (waitNanos > 0) {
            long waitMillis = waitNanos / 1000_000L;
            waitNanos -= (waitMillis * 1000_000L);
            AsyncTimeout.class.wait(waitMillis, (int) waitNanos);
            return null;
        }

        head.next = node.next;
        node.next = null;
        return node;
    }

    private static final class Watchdog extends Thread {

        Watchdog() {
            super("Subtask timeout Watchdog");
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                AsyncTimeout timeout;
                try {
                    synchronized (AsyncTimeout.class) {
                        timeout = awaitTimeout();
                        if (timeout == null) {
                            continue;
                        }
                        // If queue is empty, WatchDog exist and wait for next AsyncTimeout event.
                        if (timeout == head) {
                            head = null;
                            return;
                        }
                    }
                    log.debug("Node timeout, startAtNano={}, timeoutNano={}, deviationMillis={}",
                            timeout.startedAt, timeout.timeoutNanos,
                            (System.nanoTime() - timeout.startedAt - timeout.timeoutNanos) / 1000_000);
                    try {
                        timeout.callback();
                    } catch (Throwable t) {
                        log.warn("Run callback failed.", t);
                    }
                } catch (InterruptedException e) {
                    log.warn("Unexpected interrupted.", e);
                }
            }
        }

    }

}
