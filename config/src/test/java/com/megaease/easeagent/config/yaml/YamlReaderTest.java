/*
 * Copyright (c) 2017, MegaEase
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
 */

package com.megaease.easeagent.config.yaml;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class YamlReaderTest {

    private static final String CONFIG_FILE_YAML = "agent.yaml";
    private static final String CONFIG_FILE_PROPERTIES = "agent.properties";

    @Test
    public void testCompress() {
        InputStream inputYaml = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_FILE_YAML);
        Map<String, String> yamlMap = new YamlReader().load(inputYaml).compress();

        InputStream inputProperties = YamlReaderTest.class.getClassLoader().getResourceAsStream(CONFIG_FILE_PROPERTIES);
        Map<String, String> yamlProperties = extractPropsMap(inputProperties);

        yamlMap.forEach((k, v) -> {
            Assert.assertTrue(yamlProperties.containsKey(k));
            if (!k.equals("name")) {
                Assert.assertEquals(v, yamlProperties.get(k));
            }
        });

        yamlProperties.forEach((k, v) -> {
            Assert.assertTrue(yamlMap.containsKey(k));
            if (!k.equals("name")) {
                Assert.assertEquals(v, yamlMap.get(k));
            }
        });
    }

    private static Map<String, String> extractPropsMap(InputStream in) {
        Properties properties = new Properties();
        try {
            properties.load(in);
            HashMap<String, String> map = new HashMap<>();
            for (String one : properties.stringPropertyNames()) {
                map.put(one, properties.getProperty(one));
            }
            return map;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }

}
