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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadPrintStream extends PrintStream {

    /** Thread specific storage to hold a PrintStream for each thread */
    private static final ThreadLocal<PrintStream> OUT = new ThreadLocal<>();
    private static final PrintStream CONSOLE = System.out;

    /**
     * Changes System.out to a ThreadPrintStream which will send output to a
     * separate file for each thread.
     */
    public static void replaceSystemOut() {
        ThreadPrintStream threadOut = new ThreadPrintStream();
        System.setOut(threadOut);
        threadOut.setThreadOut(CONSOLE);
        log.info("replaceSystemOut done");
    }

    private ThreadPrintStream() {
        super(new ByteArrayOutputStream(0));
    }

    /** Sets the PrintStream for the currently executing thread. */
    public void setThreadOut(PrintStream out) {
        ThreadPrintStream.OUT.set(out);
    }

    /** Returns the PrintStream for the currently executing thread. */
    public PrintStream getThreadOut() {
        PrintStream ps = OUT.get();
        // if not set, use console
        if (ps == null) {
            return CONSOLE;
        }
        // if set as this, means duplicated replaceSystemOut may called,
        // use console for avoid StackOverFlow while call write/flush/close
        if (ps == this) {
            return CONSOLE;
        }
        return ps;
    }

    @Override
    public boolean checkError() {
        return getThreadOut().checkError();
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        getThreadOut().write(buf, off, len);
    }

    @Override
    public void write(int b) {
        getThreadOut().write(b);
    }

    @Override
    public void flush() {
        getThreadOut().flush();
    }

    @Override
    public void close() {
        getThreadOut().close();
    }
}
