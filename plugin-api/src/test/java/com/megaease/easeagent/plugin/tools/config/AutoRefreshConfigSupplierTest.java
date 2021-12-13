package com.megaease.easeagent.plugin.tools.config;

import com.megaease.easeagent.plugin.api.config.AutoRefreshConfig;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.AutoRefreshConfigSupplier;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;

public class AutoRefreshConfigSupplierTest {


    @Test
    public void getType() {
        AutoRefreshConfigSupplier<TestAutoRefreshConfig> supplier = TestAutoRefreshConfig::new;
        AutoRefreshConfigSupplier<TestAutoRefreshConfig> supplier2 = TestAutoRefreshConfig::new;
        Assert.assertEquals(supplier.getType(), supplier2.getType());
        Type type1 = supplier.getType();
        Type type2 = supplier2.getType();
        System.out.println(type1.equals(type2));
    }

    @Test
    public void newInstance() {
        AutoRefreshConfigSupplier<TestAutoRefreshConfig> supplier = TestAutoRefreshConfig::new;
        AutoRefreshConfigSupplier<TestAutoRefreshConfig> supplier2 = TestAutoRefreshConfig::new;
        Assert.assertEquals(supplier.newInstance().getClass(), supplier2.newInstance().getClass());
    }

    class TestAutoRefreshConfig implements AutoRefreshConfig {

        @Override
        public void onChange(Config oldConfig, Config newConfig) {

        }


    }
}
