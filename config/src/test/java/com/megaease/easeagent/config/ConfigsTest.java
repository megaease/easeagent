package com.megaease.easeagent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ConfigsTest {

    @Test
    public void test_check_change_count1() throws Exception {
        final ObjectMapper json = new ObjectMapper();
        Configs configs = new Configs(Collections.singletonMap("hello", "world"));
        List<ChangeItem> rst = addListener(configs);

        configs.updateService("{}", null);
        Assert.assertEquals(0, rst.size());

        configs.updateService(json.writeValueAsString(Collections.singletonMap("hello", "world")), null);
        Assert.assertEquals(0, rst.size());

        configs.updateService(json.writeValueAsString(Collections.singletonMap("hello", "world2")), null);
        Assert.assertEquals(1, rst.size());
    }

    @Test
    public void test_check_change_count() {
        Configs configs = new Configs(Collections.singletonMap("hello", "world"));
        List<ChangeItem> rst = addListener(configs);

        configs.updateConfigs(Collections.emptyMap());
        Assert.assertEquals(0, rst.size());

        configs.updateConfigs(Collections.singletonMap("hello", "world"));
        Assert.assertEquals(0, rst.size());

        configs.updateConfigs(Collections.singletonMap("hello", "world2"));
        Assert.assertEquals(1, rst.size());
    }

    @Test
    public void test_check_old() {
        Configs configs = new Configs(Collections.singletonMap("hello", "world"));
        List<ChangeItem> rst = addListener(configs);
        configs.updateConfigs(Collections.singletonMap("hello", "test"));
        ChangeItem first = rst.get(0);
        Assert.assertEquals(first.getFullName(), "hello");
        Assert.assertEquals(first.getName(), "hello");
        Assert.assertEquals(first.getOldValue(), "world");
        Assert.assertEquals(first.getNewValue(), "test");
    }


    @Test
    public void test_check_new() {
        Configs configs = new Configs(Collections.singletonMap("hello", "world"));
        List<ChangeItem> rst = addListener(configs);
        configs.updateConfigs(Collections.singletonMap("name", "666"));
        ChangeItem first = rst.get(0);
        Assert.assertEquals(first.getFullName(), "name");
        Assert.assertEquals(first.getName(), "name");
        Assert.assertNull(first.getOldValue());
        Assert.assertEquals(first.getNewValue(), "666");
    }


    private List<ChangeItem> addListener(Config config) {
        List<ChangeItem> rst = new LinkedList<>();
        config.addChangeListener(rst::addAll);
        return rst;
    }
}