package com.oceanbase.ocp.bootstrap.config.env;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class FunctionsTest {

    @Test
    public void bcryptHash() {
        String hash = Functions.bcryptHash("password");
        assertTrue(hash.length() > 0);
        System.out.println(hash);
    }

    @Test
    public void aes() {
        String text = "abcdefg";
        String key = "test";
        String result = Functions.aesDecrypt(key, Functions.aesEncrypt(key, text));
        assertEquals(text, result);
    }

    @Test
    public void json() {
        String j = "{\"a\":\"1\",\"b\":[\"1\",\"2\"]}";
        String j2 = Functions.jsonEncode(Functions.jsonDecode(j));
        Map<String, Object> o = (Map<String, Object>) Functions.jsonDecode(j2);
        assertEquals("1", o.get("a"));
        assertEquals(Arrays.asList("1", "2"), o.get("b"));
    }

    @Test
    public void property() {
        System.setProperty("__test__", "123");
        assertEquals("123", Functions.systemProperty("__test__"));
        System.clearProperty("__test__");
    }


    @Test
    public void util() {
        Set<Object> set = Functions.toSet(Arrays.asList("a", "b", "c"));
        assertEquals(3, set.size());
        List<Object> chain = Functions.chain(Arrays.asList("a", "b"), Arrays.asList("c", "d"));
        assertEquals(4, chain.size());
        assertEquals("abc", Functions.concat("a", "b", "c"));
        assertEquals("a,b,c", Functions.join(",", Arrays.asList("a", "b", "c")));
        Map<String, Object> flatten =
                Functions.flattenMap("b", ImmutableMap.of("a", 1, "b", ImmutableMap.of("f0", 1, "f1", 2)));
        assertEquals(3, flatten.size());
        List<Object> flattenList =
                Functions.flatten("b", ImmutableMap.of("a", 1, "b", Arrays.asList("f0", "f1"))).toList();
        assertEquals(2, flattenList.size());

        assertEquals(Collections.emptyMap(), Functions.emptyMap());
        assertEquals(Collections.emptySet(), Functions.emptySet());

    }
}
