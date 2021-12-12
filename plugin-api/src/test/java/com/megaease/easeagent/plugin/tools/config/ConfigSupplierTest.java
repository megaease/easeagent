package com.megaease.easeagent.plugin.tools.config;

import com.megaease.easeagent.plugin.api.config.Config;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;

import static org.junit.Assert.*;

public class ConfigSupplierTest {


    @Test
    public void getType() {
        ConfigSupplier<TestAutoRefreshConfig> supplier = TestAutoRefreshConfig::new;
        ConfigSupplier<TestAutoRefreshConfig> supplier2 = TestAutoRefreshConfig::new;
        Assert.assertEquals(supplier.getType(), supplier2.getType());
        Type type1 = supplier.getType();
        Type type2 = supplier2.getType();
        System.out.println(type1.equals(type2));
    }

    @Test
    public void newInstance() {
        ConfigSupplier<TestAutoRefreshConfig> supplier = TestAutoRefreshConfig::new;
        ConfigSupplier<TestAutoRefreshConfig> supplier2 = TestAutoRefreshConfig::new;
        Assert.assertEquals(supplier.newInstance().getClass(), supplier2.newInstance().getClass());
    }

    class TestAutoRefreshConfig implements AutoRefreshConfig {

        @Override
        public void onChange(Config oldConfig, Config newConfig) {

        }


    }
}
