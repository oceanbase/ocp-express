package com.oceanbase.ocp.bootstrap.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.oceanbase.ocp.bootstrap.core.def.Const;

public class YamlLoaderTest {

    @Test
    public void loadResource() {
        String text = "test: !resource test_resource.txt";
        YamlLoader yamlLoader = new YamlLoader();
        Map<String, String> obj = yamlLoader.load(text);
        assertEquals("hello world", obj.get("test"));
    }

    @Test
    public void expr() {
        String text = "test: !expr 1+2+3";
        YamlLoader yamlLoader = new YamlLoader();
        Map<String, String> obj = yamlLoader.load(text);
        assertEquals(6L, obj.get("test"));
    }

    @Test
    public void constant() {
        String text = "test: !const CURRENT_TIMESTAMP";
        YamlLoader yamlLoader = new YamlLoader();
        Map<String, String> obj = yamlLoader.load(text);
        assertEquals(Const.CURRENT_TIMESTAMP, obj.get("test"));

        System.out.println(yamlLoader.toYaml(obj));

        text = "test: !const CURRENT_TIMESTAMP(6)";
        obj = yamlLoader.load(text);
        assertEquals(Const.CURRENT_TIMESTAMP_6, obj.get("test"));
    }


    @Test
    public void transform() {
        String text = "test: !transform\n"
                + "  with:\n"
                + "    obj: 3\n"
                + "  expr: 'data+obj'\n"
                + "  data: 4\n";
        YamlLoader yamlLoader = new YamlLoader();
        Map<String, Object> obj = yamlLoader.load(text);
        assertEquals(7L, (long) obj.get("test"));
    }

    @Test
    public void resourceYaml() {
        String text = "test: !resourceYaml test_import.yaml";
        YamlLoader yamlLoader = new YamlLoader();
        Map<String, Object> obj = yamlLoader.load(text);
        assertTrue(obj.get("test") instanceof Map);
        Map<String, String> m = (Map<String, String>) obj.get("test");
        assertEquals(123, m.get("foo"));
        assertEquals(456, m.get("bar"));
    }
}
