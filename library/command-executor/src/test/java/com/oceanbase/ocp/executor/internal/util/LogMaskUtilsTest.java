package com.oceanbase.ocp.executor.internal.util;

import org.junit.Assert;
import org.junit.Test;

public class LogMaskUtilsTest {

    @Test
    public void mask() {
        String mask = LogMaskUtils.mask("password=123456");
        Assert.assertEquals("password=xxx", mask);
    }
}
