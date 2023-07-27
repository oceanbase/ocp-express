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

package com.oceanbase.ocp.bootstrap.progress;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.time.LocalDateTime;

public class ProgressWriter implements Closeable {

    private final Writer writer;

    private static final ProgressWriter NOP_PROGRESS_WRITER = new ProgressWriter(new Writer() {

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {

        }

        @Override
        public void flush() throws IOException {

        }

        @Override
        public void close() throws IOException {

        }
    });

    public static ProgressWriter nopProgressWriter() {
        return NOP_PROGRESS_WRITER;
    }

    public ProgressWriter(String path) throws IOException {
        this(new FileWriter(path, true));
    }

    public ProgressWriter(Writer writer) {
        this.writer = writer;
    }

    void begin(String dataSource, String stage, String name, String message) {
        LocalDateTime now = LocalDateTime.now();
        String msg = String.format("[%s] BEGIN %s %s %s %s\n", now, dataSource, stage, name, message);
        write(msg);
    }

    void end(String dataSource, String stage, String name, String message, Throwable throwable) {
        LocalDateTime now = LocalDateTime.now();
        String msg;
        if (throwable != null) {
            String errorStack = errorStack(throwable);
            msg = String.format("[%s] END %s %s %s %s\n%s\n", now, dataSource, stage, name, message, errorStack);
        } else {
            msg = String.format("[%s] END %s %s %s %s\n", now, dataSource, stage, name, message);
        }
        write(msg);
    }

    private void write(String s) {
        try {
            writer.write(s);
            writer.flush();
        } catch (IOException e) {
            // ignore error!
        }
    }

    public static String errorStack(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter, true);
        throwable.printStackTrace(printWriter);
        printWriter.flush();
        return stringWriter.toString();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
