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

import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class JarFileConfigLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(JarFileConfigLoader.class);

    static GlobalConfigs load(String file) {
        String agentJarPath = System.getProperty(ConfigConst.AGENT_JAR_PATH);
        if (agentJarPath == null) {
            return null;
        }
        try {
            JarFile jarFile = new JarFile(new File(agentJarPath));
            ZipEntry zipEntry = jarFile.getEntry(file);
            if (zipEntry == null) {
                return null;
            }
            try (InputStream in = jarFile.getInputStream(zipEntry)) {
                return ConfigLoader.loadFromStream(in, file);
            } catch (IOException e) {
                LOGGER.debug("Load config file:{} failure: {}", file, e);
            }
        } catch (IOException e) {
            LOGGER.debug("create JarFile:{} failure: {}", agentJarPath, e);
        }
        return null;
    }
}
