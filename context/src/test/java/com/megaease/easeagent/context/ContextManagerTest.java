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

package com.megaease.easeagent.context;

import com.megaease.easeagent.mock.config.ConfigMock;
import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.MetricRegistrySupplier;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ContextManagerTest {

    @Test
    public void build() {
        ContextManager contextManager = ContextManager.build(ConfigMock.getCONFIGS());
        assertNotNull(contextManager);
        contextManager.setTracing(() -> contextSupplier -> null);
    }

    @Test
    public void setTracing() {
        ContextManager contextManager = ContextManager.build(ConfigMock.getCONFIGS());
        assertNotNull(contextManager);
        contextManager.setTracing(() -> contextSupplier -> null);
    }

    @Test
    public void setMetric() {
        ContextManager contextManager = ContextManager.build(ConfigMock.getCONFIGS());
        assertNotNull(contextManager);
        contextManager.setMetric(() -> new MetricRegistrySupplier() {
            @Override
            public MetricRegistry newMetricRegistry(IPluginConfig config, NameFactory nameFactory, Tags tags) {
                return null;
            }

            @Override
            public Reporter reporter(IPluginConfig config) {
                return null;
            }
        });
    }
}
