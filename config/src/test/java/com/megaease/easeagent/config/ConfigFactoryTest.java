/*
 * Copyright (c) 2022, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.megaease.easeagent.config;

import com.megaease.easeagent.plugin.utils.SystemEnv;
import org.junit.Test;

import static com.megaease.easeagent.config.ConfigFactory.AGENT_SERVICE;
import static com.megaease.easeagent.config.ConfigFactory.AGENT_SYSTEM;
import static org.junit.Assert.assertEquals;

public class ConfigFactoryTest {
    @Test
    public void test_yaml() {
        Configs config = ConfigFactory.loadConfigs(null, this.getClass().getClassLoader());
        assertEquals("test-service", config.getString(AGENT_SERVICE));
        assertEquals("demo-system", config.getString(AGENT_SYSTEM));
    }

    @Test
    public void test_env() {
        SystemEnv.set(ConfigFactory.EASEAGENT_ENV_CONFIG, "{\"name\":\"env-service\"}");
        Configs config = ConfigFactory.loadConfigs(null, this.getClass().getClassLoader());
        assertEquals("env-service", config.getString(AGENT_SERVICE));
        assertEquals("demo-system", config.getString(AGENT_SYSTEM));
    }

    @Test
    public void test_loadConfigs() {
        SystemEnv.set("EASEAGENT_NAME", "service1");
        SystemEnv.set("EASEAGENT_SYSTEM", "system1");
        Configs config = ConfigFactory.loadConfigs(this.getClass().getClassLoader());
        assertEquals("service1", config.getString(AGENT_SERVICE));
        assertEquals("system1", config.getString(AGENT_SYSTEM));

        // override by jvm properties
        System.setProperty("easeagent.name", "service2");
        System.setProperty("easeagent.system", "system2");
        config = ConfigFactory.loadConfigs(this.getClass().getClassLoader());
        assertEquals("service2", config.getString(AGENT_SERVICE));
        assertEquals("system2", config.getString(AGENT_SYSTEM));
    }

    @Test
    public void test_loadConfigsFromUserSpec() {
        SystemEnv.set("EASEAGENT_CONFIG_PATH", "src/test/resources/user-spec.properties");
        Configs config = ConfigFactory.loadConfigs(this.getClass().getClassLoader());
        assertEquals("user-spec", config.getString(AGENT_SERVICE));
        assertEquals("system-spec", config.getString(AGENT_SYSTEM));

        // override by jvm properties
        System.setProperty("easeagent.config.path", "src/test/resources/user-spec2.properties");
        config = ConfigFactory.loadConfigs(this.getClass().getClassLoader());
        assertEquals("user-spec2", config.getString(AGENT_SERVICE));
        assertEquals("system-spec2", config.getString(AGENT_SYSTEM));
    }

    @Test
    public void test_loadConfigsFromOtelUserSpec() {
        SystemEnv.set("OTEL_JAVAAGENT_CONFIGURATION_FILE", "src/test/resources/user-spec.properties");
        Configs config = ConfigFactory.loadConfigs(this.getClass().getClassLoader());
        assertEquals("user-spec", config.getString(AGENT_SERVICE));
        assertEquals("system-spec", config.getString(AGENT_SYSTEM));

        // override by jvm properties
        System.setProperty("otel.javaagent.configuration-file", "src/test/resources/user-spec2.properties");
        config = ConfigFactory.loadConfigs(this.getClass().getClassLoader());
        assertEquals("user-spec2", config.getString(AGENT_SERVICE));
        assertEquals("system-spec2", config.getString(AGENT_SYSTEM));
    }
}
