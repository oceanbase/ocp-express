package com.oceanbase.ocp.bootstrap.config.env;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

public class ELEnvTest {

    @Test
    public void eval() {
        ELEnv elEnv = new JavaxELEnv();
        long n = elEnv.eval("1+2+3");
        assertEquals(6, n);
        Object val = elEnv.eval("[1,2,3]");
        System.out.println(val);
        val = elEnv.eval("{\"a\":\"b\"}");
        System.out.println(val);
        System.out.println(val.getClass());
        HashMap<String, Integer> m = new HashMap<>();
        m.put("a", 1);
        elEnv.set("m", m);
        val = elEnv.eval("m.put('b',2);m");
        System.out.println(val);
        System.out.println(val.getClass());
    }
}
