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

import com.megaease.easeagent.config.ConfigFactory;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.bridge.AgentInfo;
import org.junit.Test;

import static org.junit.Assert.*;

public class AgentInfoFactoryTest {

    @Test
    public void loadAgentInfo() {
        AgentInfo config = AgentInfoFactory.loadAgentInfo(this.getClass().getClassLoader());
        assertEquals(AgentInfoFactory.AGENT_TYPE, config.getType());
        assertTrue(config.getVersion().matches(".*\\d+\\.\\d+\\.\\d+.*"));
    }
}
