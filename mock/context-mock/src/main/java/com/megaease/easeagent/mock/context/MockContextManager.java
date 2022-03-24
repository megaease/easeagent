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

package com.megaease.easeagent.mock.context;

import com.megaease.easeagent.context.ContextManager;
import com.megaease.easeagent.mock.config.MockConfig;
import com.megaease.easeagent.mock.utils.MockProvider;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.metric.MetricProvider;
import com.megaease.easeagent.plugin.api.trace.TracingProvider;
import com.megaease.easeagent.plugin.bridge.EaseAgent;

import java.util.Iterator;
import java.util.ServiceLoader;

public class MockContextManager {
    private static final ContextManager CONTEXT_MANAGER_MOCK = ContextManager.build(MockConfig.getCONFIGS());

    static {
        ServiceLoader<MockProvider> loader = ServiceLoader.load(MockProvider.class);
        Iterator<MockProvider> iterator = loader.iterator();
        while (iterator.hasNext()) {
            MockProvider mockProvider = iterator.next();
            Object o = mockProvider.get();
            if (o == null) {
                continue;
            }
            if (o instanceof TracingProvider) {
                CONTEXT_MANAGER_MOCK.setTracing((TracingProvider) o);
            } else if (o instanceof MetricProvider) {
                CONTEXT_MANAGER_MOCK.setMetric((MetricProvider) o);
            }
        }
    }

    public static ContextManager getContextManagerMock() {
        return CONTEXT_MANAGER_MOCK;
    }

    public static Context getContext() {
        return EaseAgent.getOrCreateTracingContext();
    }
}
