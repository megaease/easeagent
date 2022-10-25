/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.config;

import com.megaease.easeagent.plugin.api.config.ConfigConst;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class JarFileConfigLoaderTest {

    @After
    public void after() {
        System.clearProperty(ConfigConst.AGENT_JAR_PATH);
    }


    @Test
    public void load() throws URISyntaxException {
        String fileName = "agent.properties";
        assertNull(JarFileConfigLoader.load(null));
        assertNull(JarFileConfigLoader.load(fileName));
        String jarPath = new File(this.getClass().getClassLoader().getResource("easeagent_config.jar").toURI()).getPath();
        System.setProperty(ConfigConst.AGENT_JAR_PATH, jarPath);

        GlobalConfigs config = JarFileConfigLoader.load(fileName);
        assertNotNull(config);
        assertEquals("demo-jar-service", config.getString("name"));
        assertEquals("demo-system", config.getString("system"));

        config = JarFileConfigLoader.load("agent.yaml");
        assertNotNull(config);
        assertEquals("test-jar-service", config.getString("name"));
        assertEquals("demo-system", config.getString("system"));

        config = JarFileConfigLoader.load("agentaaaa.yaml");
        assertNull(config);


        String nullJarPath = new File("easeagent_config_null.jar").getPath();
        System.setProperty(ConfigConst.AGENT_JAR_PATH, nullJarPath);

        config = JarFileConfigLoader.load("agent.yaml");
        assertNull(config);

    }
}
