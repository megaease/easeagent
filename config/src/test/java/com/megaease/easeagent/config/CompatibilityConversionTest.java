/*
 * Copyright (c) 2017, MegaEase
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
package com.megaease.easeagent.config;

import com.megaease.easeagent.plugin.api.ProgressFields;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static com.megaease.easeagent.config.CompatibilityConversion.REQUEST_NAMESPACE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompatibilityConversionTest {

    @Test
    public void transform() {
        Map<String, String> newMap = CompatibilityConversion.transform(Collections.singletonMap(ConfigConst.Observability.METRICS_ENABLED, "false"));
        assertEquals(2, newMap.size());
        assertTrue(newMap.containsKey(ConfigConst.Plugin.OBSERVABILITY_GLOBAL_METRIC_ENABLED));
        assertTrue(newMap.containsKey(ConfigConst.Observability.METRICS_ENABLED));
        newMap = CompatibilityConversion.transform(Collections.singletonMap(ConfigConst.Observability.TRACE_ENABLED, "false"));
        assertEquals(1, newMap.size());
        assertTrue(newMap.containsKey(ConfigConst.Plugin.OBSERVABILITY_GLOBAL_TRACING_ENABLED));
        newMap = CompatibilityConversion.transform(Collections.singletonMap("globalCanaryHeaders.serviceHeaders.mesh-app-backend.0", "X-canary"));
        assertEquals(1, newMap.size());
        assertTrue(newMap.containsKey(ProgressFields.EASEAGENT_PROGRESS_FORWARDED_HEADERS_CONFIG + ".mesh-app-backend.0"));

        newMap = CompatibilityConversion.transform(Collections.singletonMap("observability.outputServer.bootstrapServer", "tstatssta"));
        assertEquals(1, newMap.size());
        assertTrue(newMap.containsKey("observability.outputServer.bootstrapServer"));


        newMap = CompatibilityConversion.transform(Collections.singletonMap("observability.metrics.access.enabled", "true"));
        assertEquals(1, newMap.size());
        assertEquals("true", newMap.get("plugin.observability.access.metric.enabled"));


        newMap = CompatibilityConversion.transform(Collections.singletonMap("observability.metrics.access.interval", "30"));
        assertEquals(1, newMap.size());
        assertEquals("30", newMap.get("plugin.observability.access.metric.interval"));

        newMap = CompatibilityConversion.transform(Collections.singletonMap("observability.tracings.remoteInvoke.enabled", "true"));
        assertEquals(1, newMap.size());
        assertEquals("true", newMap.get("plugin.observability.webclient.tracing.enabled"));

        newMap = CompatibilityConversion.transform(Collections.singletonMap("observability.tracings.request.enabled", "true"));
        assertEquals(REQUEST_NAMESPACE.length, newMap.size());
        for (String s : REQUEST_NAMESPACE) {
            assertEquals("true", newMap.get("plugin.observability." + s + ".tracing.enabled"));
        }

        newMap = CompatibilityConversion.transform(Collections.singletonMap("observability.metrics.jvmGc.enabled", "true"));
        assertEquals(1, newMap.size());
        assertEquals("true", newMap.get("observability.metrics.jvmGc.enabled"));


        newMap = CompatibilityConversion.transform(Collections.singletonMap("observability.tracings.sampledByQPS", "100"));
        assertEquals(1, newMap.size());
        assertEquals("100", newMap.get("observability.tracings.sampledByQPS"));


        newMap = CompatibilityConversion.transform(Collections.singletonMap("observability.tracings.output.enabled", "true"));
        assertEquals(1, newMap.size());
        assertEquals("true", newMap.get("observability.tracings.output.enabled"));

        newMap = CompatibilityConversion.transform(Collections.singletonMap("observability.metrics.jdbcConnection.interval", "30"));
        assertEquals(1, newMap.size());
        assertEquals("30", newMap.get("plugin.observability.jdbcConnection.metric.interval"));

        newMap = CompatibilityConversion.transform(Collections.singletonMap("observability.metrics.aaaaaaaaaa.interval", "30"));
        assertEquals(1, newMap.size());
        assertEquals("30", newMap.get("plugin.observability.aaaaaaaaaa.metric.interval"));
    }
}
