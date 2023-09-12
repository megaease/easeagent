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
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.net.URISyntaxException;

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
        try (MockedStatic<SystemEnv> mock = Mockito.mockStatic(SystemEnv.class)) {
            mock.when(() -> SystemEnv.get(ConfigFactory.EASEAGENT_ENV_CONFIG)).thenReturn("{\"name\":\"env-service\"}");

            Configs config = ConfigFactory.loadConfigs(null, this.getClass().getClassLoader());
            assertEquals("env-service", config.getString(AGENT_SERVICE));
            assertEquals("demo-system", config.getString(AGENT_SYSTEM));
        }
    }


    @Test
    public void test_loadConfigs() {
        try (MockedStatic<SystemEnv> mockSystemEnv = Mockito.mockStatic(SystemEnv.class)) {
            mockSystemEnv.when(() -> SystemEnv.get("EASEAGENT_NAME")).thenReturn("service1");
            mockSystemEnv.when(() -> SystemEnv.get("EASEAGENT_SYSTEM")).thenReturn("system1");

            Configs config = ConfigFactory.loadConfigs(this.getClass().getClassLoader());
            assertEquals("service1", config.getString(AGENT_SERVICE));
            assertEquals("system1", config.getString(AGENT_SYSTEM));

            System.setProperty("easeagent.name", "service2");
            System.setProperty("easeagent.system", "system2");
            config = ConfigFactory.loadConfigs(this.getClass().getClassLoader());
            assertEquals("service2", config.getString(AGENT_SERVICE));
            assertEquals("system2", config.getString(AGENT_SYSTEM));
        }

    }

    @Test
    public void test_loadConfigsFromUserSpec() throws URISyntaxException {
        String userSpec = new File(this.getClass().getClassLoader().getResource("user-spec.properties").toURI()).getPath();

        try (MockedStatic<SystemEnv> mockSystemEnv = Mockito.mockStatic(SystemEnv.class)) {
            mockSystemEnv.when(() -> SystemEnv.get("EASEAGENT_CONFIG_PATH")).thenReturn(userSpec);
            Configs config = ConfigFactory.loadConfigs(this.getClass().getClassLoader());
            assertEquals("user-spec", config.getString(AGENT_SERVICE));
            assertEquals("system-spec", config.getString(AGENT_SYSTEM));
        }
    }


    @Test
    public void test_loadConfigsFromOtelUserSpec() throws URISyntaxException {
        String userSpec = new File(this.getClass().getClassLoader().getResource("user-spec.properties").toURI()).getPath();
        try (MockedStatic<SystemEnv> mockSystemEnv = Mockito.mockStatic(SystemEnv.class)) {
            mockSystemEnv.when(() -> SystemEnv.get("OTEL_JAVAAGENT_CONFIGURATION_FILE")).thenReturn(userSpec);
            Configs config = ConfigFactory.loadConfigs(this.getClass().getClassLoader());
            assertEquals("user-spec", config.getString(AGENT_SERVICE));
            assertEquals("system-spec", config.getString(AGENT_SYSTEM));

        }
    }
}
