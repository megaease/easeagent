package com.megaease.easeagent.config;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

public class ConfigUtilsTest {
    @Test
    public void test_bindProp() throws Exception {
        Configs configs = new Configs(Collections.singletonMap("hello", "world"));
        String[] rst = new String[]{null};
        ConfigUtils.bindProp("hello", configs, Config::getString, v -> rst[0] = v);
        Assert.assertEquals("world", rst[0]);
        configs.updateConfigs(Collections.singletonMap("hello", "test"));
        Assert.assertEquals("test", rst[0]);
        configs.updateConfigs(Collections.singletonMap("hello", "one"));
        Assert.assertEquals("one", rst[0]);
    }

    @Test
    public void test_json2KVMap() throws Exception {
        Map<String, String> map = ConfigUtils.json2KVMap("{\n" +
                "  \"output\": {\n" +
                "    \"servers\": \"127.0.0.1\",\n" +
                "    \"timeout\": 1000,\n" +
                "    \"enabled\": true,\n" +
                "    \"arr\": [\"x\", { \"test\": 0 }]\n" +
                "  },\n" +
                "  \"hello\":null,\n" +
                "  \"metrics\": {\n" +
                "    \"obj\": {\n" +
                "      \"a\": 1,\n" +
                "      \"b\": \"2\",\n" +
                "      \"c\": false\n" +
                "    },\n" +
                "    \"request\": {\n" +
                "      \"topic\": \"hello\",\n" +
                "      \"enabled\": false\n" +
                "    }\n" +
                "  }\n" +
                "}");
        Assert.assertEquals("127.0.0.1", map.get("output.servers"));
        Assert.assertEquals("1000", map.get("output.timeout"));
        Assert.assertEquals("true", map.get("output.enabled"));
        Assert.assertEquals("x", map.get("output.arr.0"));
        Assert.assertEquals("0", map.get("output.arr.1.test"));
        Assert.assertEquals("", map.get("hello"));
        Assert.assertEquals("1", map.get("metrics.obj.a"));
        Assert.assertEquals("2", map.get("metrics.obj.b"));
        Assert.assertEquals("false", map.get("metrics.obj.c"));
        Assert.assertEquals("hello", map.get("metrics.request.topic"));
        Assert.assertEquals("false", map.get("metrics.request.enabled"));
    }
}