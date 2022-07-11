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

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ConfigManagerMXBean {
    void updateConfigs(Map<String, String> configs);

    void updateService(String json, String version) throws IOException;

    void updateCanary(String json, String version) throws IOException;

    void updateService2(Map<String, String> configs, String version);

    void updateCanary2(Map<String, String> configs, String version);

    Map<String, String> getConfigs();

    List<String> availableConfigNames();

    default void healthz() {
    }
}
