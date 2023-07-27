package com.oceanbase.ocp.bootstrap.config.env;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;

public class TransformTest {

    @Test
    public void toDataMap() {
        Yaml yaml = new Yaml();
        Node node = yaml.compose(new StringReader("key1: s1\n"
                + "key2:\n"
                + "  - name: n1\n"
                + "    value: 11\n"
                + "  - name:  n2\n"
                + "    value: 22\n"));
        Map<String, Object> o = (Map<String, Object>) new Transform().toDataMap(node);
        assertEquals("s1", o.get("key1"));
        List<Map<String, Object>> l = (List<Map<String, Object>>) o.get("key2");
        assertEquals(2, l.size());
        Map<String, Object> m1 = l.get(0);
        assertEquals("n1", m1.get("name"));
        assertEquals(11, m1.get("value"));
        Map<String, Object> m2 = l.get(1);
        assertEquals("n2", m2.get("name"));
        assertEquals(22, m2.get("value"));
    }
}
