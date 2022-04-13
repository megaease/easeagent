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

package com.megaease.easeagent.mock.plugin.api.utils;

import com.megaease.easeagent.mock.metrics.MockMetricProvider;
import com.megaease.easeagent.mock.report.MockReport;
import com.megaease.easeagent.mock.zipkin.MockTracingProvider;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;

import java.util.HashMap;
import java.util.Map;

public class ContextUtils {

    /**
     * reset all of context
     */
    public static void resetAll() {
        EaseAgent.initializeContextSupplier.getContext().clear();
        MockMetricProvider.clearAll();
        OldRedirect.resetRedirect();
        MockTracingProvider.cleanCurrentSpan();
        MockTracingProvider.cleanPendingSpans();
        MockReport.cleanLastSpan();
        MockReport.cleanLastAccessLog();
        MockReport.cleanSkipSpan();
    }

    static class OldRedirect {
        static final Map<Redirect, ResourceConfig> OLD_CONFIG;
        static final Map<String, String> OLD_EASEMESH_TAGS = RedirectProcessor.tags();

        static {
            Map<Redirect, ResourceConfig> oldConfig = new HashMap<>();
            for (Redirect redirect : Redirect.values()) {
                oldConfig.put(redirect, redirect.getConfig());
            }
            OLD_CONFIG = oldConfig;

        }

        private static void resetRedirect() {
            for (Map.Entry<Redirect, ResourceConfig> entry : OLD_CONFIG.entrySet()) {
                if (entry.getKey().getConfig() == entry.getValue()) {
                    continue;
                }
                AgentFieldReflectAccessor.setFieldValue(entry.getKey(), "config", entry.getValue());
            }
            AgentFieldReflectAccessor.setFieldValue(RedirectProcessor.INSTANCE, "tags", OLD_EASEMESH_TAGS);
        }
    }
}
