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

package com.megaease.easeagent.plugin.api;

import com.megaease.easeagent.api.config.Config;

public interface AgentPlugin {
    // current plugin name
    String getName();

    boolean isEnable();

    Config getConfig();

    void initConfig(Config cfg);

    /**
     * Higher value initiate latter,
     * action plugins with the order 0
     * sniffer plugins' order start from 100
     */
    default int order() {
        return 100;
    }

    /**
     * Generally, action plugins do not contain any class or method matchers
     */
    public interface ActionPlugin extends AgentPlugin {
    }

    public interface ProbePlugin extends AgentPlugin {
    }
}
