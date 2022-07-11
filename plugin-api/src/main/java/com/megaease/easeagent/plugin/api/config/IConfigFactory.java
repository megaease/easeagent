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

package com.megaease.easeagent.plugin.api.config;

public interface IConfigFactory {
    /**
     * Returns the global configuration of this Java agent.
     *
     * @return The global configuration of this Java agent.
     */
    Config getConfig();

    /**
     * Returns a configuration property from the agent's all configuration.
     *
     * @return The configuration of this Java agent.
     */
    String getConfig(String property);

    String getConfig(String property, String defaultValue);

    /**
     * Returns the agent's plugin configuration.
     *
     * @return The configuration of a special plugin of Java agent.
     */
    IPluginConfig getConfig(String domain, String namespace, String name);
}
