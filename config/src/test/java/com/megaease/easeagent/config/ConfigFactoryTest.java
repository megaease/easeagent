package com.megaease.easeagent.config;

import com.megaease.easeagent.plugin.utils.SystemEnv;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConfigFactoryTest {
    @Test
    public void test_yaml() {
        Configs config = ConfigFactory.loadConfigs(null, this.getClass().getClassLoader());
        assertEquals("test-service", config.getString("name"));
        assertEquals("demo-system", config.getString("system"));
    }

    @Test
    public void test_env() {
        SystemEnv.set(ConfigFactory.EASEAGENT_ENV_CONFIG, "{\"name\":\"env-service\"}");
        Configs config = ConfigFactory.loadConfigs(null, this.getClass().getClassLoader());
        assertEquals("env-service", config.getString("name"));
        assertEquals("demo-system", config.getString("system"));
    }
}
