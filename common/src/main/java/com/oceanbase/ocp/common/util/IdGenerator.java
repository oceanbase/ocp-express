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

package com.oceanbase.ocp.common.util;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.util.Enumeration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IdGenerator {

    private static final int TOTAL_BITS = 64;
    private static final int EPOCH_BITS = 42;
    private static final int NODE_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;
    private static final long MAX_NODE_ID = (1 << NODE_ID_BITS) - 1;
    private static final long MAX_SEQUENCE = (1 << SEQUENCE_BITS) - 1;
    private static final long CUSTOM_EPOCH = 1561439518000L;
    private final long nodeId;
    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;

    private static final IdGenerator ID_GENERATOR = new IdGenerator();

    public static IdGenerator getInstance() {
        return ID_GENERATOR;
    }

    public IdGenerator() {
        this.nodeId = createNodeId();
    }

    private long waitNextTimestamp() {
        log.info("4k id alread used for currentTimestamp, need to wait for next timestamp");
        long currentTimestamp = System.currentTimeMillis() - CUSTOM_EPOCH;
        while (currentTimestamp == lastTimestamp) {
            currentTimestamp = System.currentTimeMillis() - CUSTOM_EPOCH;
        }
        return currentTimestamp;
    }

    private long createNodeId() {
        long nodeId;
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    for (byte b : mac) {
                        sb.append(String.format("%02X", b));
                    }
                }
            }
            nodeId = sb.toString().hashCode();
        } catch (Exception ex) {
            nodeId = (new SecureRandom().nextInt());
        }
        nodeId = nodeId & MAX_NODE_ID;
        return nodeId;
    }

    public synchronized long getNextId() {
        long currentTimestamp = System.currentTimeMillis() - CUSTOM_EPOCH;
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                currentTimestamp = waitNextTimestamp();
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = currentTimestamp;
        long id = currentTimestamp << (TOTAL_BITS - EPOCH_BITS);
        id |= ((nodeId & MAX_NODE_ID) << (TOTAL_BITS - EPOCH_BITS - NODE_ID_BITS));
        id |= sequence;
        return id;
    }

}
