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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import com.oceanbase.ocp.task.dao.SubtaskLogRepo;
import com.oceanbase.ocp.task.entity.SubtaskLogEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubtaskOutputStream extends BufferedOutputStream {

    private static final int BUF_SIZE = 2048;

    private final long subtaskId;
    private final int runTime;
    private final SubtaskLogRepo subtaskLogRepo;

    public SubtaskOutputStream(long subtaskId, int runtime, OutputStream out, SubtaskLogRepo subtaskLogRepo) {
        super(out, BUF_SIZE);
        this.subtaskId = subtaskId;
        this.runTime = runtime;
        this.subtaskLogRepo = subtaskLogRepo;
    }

    @Override
    public synchronized void flush() throws IOException {
        if (this.count == 0) {
            return;
        }
        this.out.write(buf, 0, count);
        try {
            SubtaskLogEntity subtaskLogEntity = new SubtaskLogEntity();
            subtaskLogEntity.setSubtaskInstanceId(subtaskId);
            subtaskLogEntity.setRunTime(runTime);
            subtaskLogEntity.setLogContent(Arrays.copyOfRange(this.buf, 0, this.count));
            subtaskLogRepo.save(subtaskLogEntity);
        } catch (Exception ignore) {
            this.count = 0;
        } finally {
            this.count = 0;
        }
    }

    @Override
    public synchronized void write(int i) throws IOException {
        this.write((byte) (i & 0xFF));
    }

    private synchronized void write(byte b) throws IOException {
        if (this.count == this.buf.length) {
            this.flush();
        }
        this.buf[this.count++] = b;
    }

    @Override
    public synchronized void write(byte[] buf, int offset, int len) throws IOException {
        int count = len;
        int idx = offset;
        while (count-- > 0) {
            this.write(buf[idx++]);
        }
    }

}
