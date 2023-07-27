package com.oceanbase.ocp.bootstrap.progress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Writer;

import org.junit.Test;

import com.oceanbase.ocp.bootstrap.core.Action;
import com.oceanbase.ocp.bootstrap.core.ProgressHandler;
import com.oceanbase.ocp.bootstrap.core.Stage;
import com.oceanbase.ocp.bootstrap.progress.Progress.InstallUpgradeProgress;

public class ProgressHandlerTest {

    @Test
    public void installUpgrade() throws IOException {
        StringBuilder sb = new StringBuilder();
        ProgressWriter writer = new ProgressWriter(new Writer() {

            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                sb.append(cbuf, off, len);
            }

            @Override
            public void flush() throws IOException {

            }

            @Override
            public void close() throws IOException {

            }
        });
        Progress progress = new Progress();
        ProgressHandler handler = new ProgressHandlerImpl(progress, writer);
        handler.beginAction("test", Action.INSTALL);
        handler.beginStage("test", Stage.DEFAULT_DATA, 100);
        handler.beginTask("test", Stage.DEFAULT_DATA, "a", "a1", "...");
        handler.endTask("test", Stage.DEFAULT_DATA, "a", "a1", new IOException());
        handler.endStage("test", Stage.DEFAULT_DATA);
        handler.endAction("test", Action.INSTALL, null);
        writer.close();
        String log = sb.toString();
        assertEquals(6, log.chars().filter(c -> c == '[').count());
        assertTrue(log.contains("IOException"));
        System.out.println(log);
        InstallUpgradeProgress progress1 = progress.getAllInstallUpgradeProgress().getProgress("test");
        assertTrue(progress1.isDone());
        assertNull(progress1.getError());
        assertEquals(100, progress1.getProgress(Stage.DEFAULT_DATA).getTotalTasks());
        assertEquals(1, progress1.getProgress(Stage.DEFAULT_DATA).getFinishedTasks());
    }

    @Test
    public void bean() {
        Progress progress = new Progress();
        ProgressHandlerImpl handler = new ProgressHandlerImpl(progress, ProgressWriter.nopProgressWriter());
        handler.beginBean("b1", "com.oceanbase.ocp.Bean1");
        handler.beginBean("b2", "com.oceanbase.ocp.Bean2");
        assertEquals(2, progress.getBeanProgress().getPending().size());
        assertEquals(0, progress.getBeanProgress().getInitializedAfter(0).size());
        handler.endBean("b2", "com.oceanbase.ocp.Bean2");
        assertEquals(1, progress.getBeanProgress().getInitializedBeans());
        assertEquals(1, progress.getBeanProgress().getPending().size());
        handler.endBean("b1", "com.oceanbase.ocp.Bean1");
        assertEquals(0, progress.getBeanProgress().getPending().size());
        assertEquals(2, progress.getBeanProgress().getInitializedBeans());
        assertNull(progress.getApplicationError());
        assertFalse(progress.isApplicationReady());
        handler.onApplicationReady();
        assertTrue(progress.isApplicationReady());
    }

}
