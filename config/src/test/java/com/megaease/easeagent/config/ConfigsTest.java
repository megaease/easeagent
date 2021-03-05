package com.megaease.easeagent.config;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class ConfigsTest {

    @Test
    public void test_check_change_count() throws Exception {
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
    public void test_check_old() throws Exception {
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
    public void test_check_new() throws Exception {
        Configs configs = new Configs(Collections.singletonMap("hello", "world"));
        List<ChangeItem> rst = addListener(configs);
        configs.updateConfigs(Collections.singletonMap("name", "666"));
        ChangeItem first = rst.get(0);
        Assert.assertEquals(first.getFullName(), "name");
        Assert.assertEquals(first.getName(), "name");
        Assert.assertEquals(first.getOldValue(), null);
        Assert.assertEquals(first.getNewValue(), "666");
    }

    @Test
    public void test_check_config() throws Exception {
        Configs configs = new Configs(Collections.singletonMap("hello.one", "world"));
        Config hello = configs.getConfig("hello");
        List<ChangeItem> rst = addListener(hello);
        Map<String, String> newValues = new HashMap<>();
        newValues.put("hello.two", "666");
        newValues.put("hello.one", "test");
        configs.updateConfigs(newValues);
        Assert.assertEquals(2, rst.size());

        ChangeItem one = rst.stream().filter(e -> e.getFullName().equals("hello.one")).findFirst().orElse(null);
        Assert.assertEquals(one.getFullName(), "hello.one");
        Assert.assertEquals(one.getName(), "one");
        Assert.assertEquals(one.getOldValue(), "world");
        Assert.assertEquals(one.getNewValue(), "test");

        ChangeItem two = rst.stream().filter(e -> e.getFullName().equals("hello.two")).findFirst().orElse(null);
        Assert.assertEquals(two.getFullName(), "hello.two");
        Assert.assertEquals(two.getName(), "two");
        Assert.assertEquals(two.getOldValue(), null);
        Assert.assertEquals(two.getNewValue(), "666");
    }


    private List<ChangeItem> addListener(Config config) {
        List<ChangeItem> rst = new LinkedList<>();
        config.addChangeListener(new ConfigChangeListener() {
            @Override
            public void onChange(List<ChangeItem> list) {
                rst.addAll(list);
            }
        });
        return rst;
    }
}