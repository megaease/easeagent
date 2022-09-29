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

package com.megaease.easeagent.core.info;

import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.bridge.AgentInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AgentInfoFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentInfoFactory.class);
    public static final String AGENT_TYPE = "EaseAgent";
    private static final String VERSION_FILE = "version.txt";


    public static AgentInfo loadAgentInfo(ClassLoader classLoader) {
        return new AgentInfo(AGENT_TYPE, loadVersion(classLoader, VERSION_FILE));
    }


    private static String loadVersion(ClassLoader classLoader, String file) {
        try (InputStream in = classLoader.getResourceAsStream(file)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String version = reader.readLine();
            reader.close();
            return version;
        } catch (IOException e) {
            LOGGER.warn("Load config file:{} by classloader:{} failure: {}", file, classLoader.toString(), e);
        }
        return "";
    }
}
