/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.plugin;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;

public interface AgentPlugin extends ConfigChangeListener {
    void load(Config config);

    /**
     * when the plugin is disabled by configuration, this method will be called
     */
    void unload(Config config);

    /**
     * define the plugin name, avoiding conflicts with others
     */
    String getName();

    /**
     * define the plugin domain,
     * it will be use to get configuration when loaded:
     * like: load(EaseAgent.configFactory(getDomain(), getName())
     */
    String getDomain();

    /**
     * Higher value initiate latter,
     * action plugins with the order 0
     * sniffer plugins' order start from 100
     */
    default int order() {
        return 100;
    }
}

