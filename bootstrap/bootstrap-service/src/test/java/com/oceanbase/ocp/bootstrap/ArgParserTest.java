package com.oceanbase.ocp.bootstrap;

import org.junit.Assert;
import org.junit.Test;

import com.oceanbase.ocp.bootstrap.core.Action;

import junit.framework.TestCase;

public class ArgParserTest extends TestCase {

    @Test
    public void testParse() {
        ArgParser ap = new ArgParser();
        String[] args = {"--bootstrap", "--install", "--port=1001", "--auth=admin:root", "--meta-address=127.0.0.1",
                "--meta-database=ocp",
                "--meta-password=***", "--meta-user=meta_ocp", "--with-property=logging.file.name:express.log",
                "--with-property=logging.file.total-size-cap:10G", "--progress-log=/data/logs/progress.log"};
        Params params = ap.parse(args);
        Assert.assertTrue(params.isEnabled());
        Assert.assertEquals("127.0.0.1", params.getMetaAddress());
        Assert.assertEquals("ocp", params.getMetaDatabase());
        Assert.assertEquals("***", params.getMetaPassword());
        Assert.assertEquals("meta_ocp", params.getMetaUsername());
        Assert.assertTrue(params.getProgressLogPath().length() > 0);
        Assert.assertEquals(1001, params.getPort());
        Assert.assertEquals(2, params.getConfigProperties().size());
        Assert.assertEquals("logging.file.name", params.getConfigProperties().get(0).getName());
        Assert.assertEquals("express.log", params.getConfigProperties().get(0).getValue());
        Assert.assertEquals("logging.file.total-size-cap", params.getConfigProperties().get(1).getName());
        Assert.assertEquals("10G", params.getConfigProperties().get(1).getValue());
        Assert.assertEquals(Action.INSTALL, params.getAction());
        Assert.assertEquals("admin", params.getAuth().getUsername());
        Assert.assertEquals("root", params.getAuth().getPassword());
    }
}
