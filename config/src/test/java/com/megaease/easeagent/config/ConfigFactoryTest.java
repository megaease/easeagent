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
