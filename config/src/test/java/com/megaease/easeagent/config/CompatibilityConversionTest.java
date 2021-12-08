package com.megaease.easeagent.config;

import com.megaease.easeagent.plugin.api.ProgressFields;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static com.megaease.easeagent.config.CompatibilityConversion.REQUEST_NAMESPACE;
import static org.junit.Assert.*;

public class CompatibilityConversionTest {

    @Test
    public void transform() {
        Map<String, String> newMap = CompatibilityConversion.transform(Collections.singletonMap(ConfigConst.Observability.METRICS_ENABLED, "false"));
        assertEquals(newMap.size(), 1);
        assertTrue(newMap.containsKey(ConfigConst.Plugin.OBSERVABILITY_GLOBAL_METRIC_ENABLED));
        newMap = CompatibilityConversion.transform(Collections.singletonMap(ConfigConst.Observability.TRACE_ENABLED, "false"));
        assertEquals(newMap.size(), 1);
        assertTrue(newMap.containsKey(ConfigConst.Plugin.OBSERVABILITY_GLOBAL_TRACING_ENABLED));
        newMap = CompatibilityConversion.transform(Collections.singletonMap("globalCanaryHeaders.serviceHeaders.mesh-app-backend.0", "X-canary"));
        assertEquals(newMap.size(), 1);
        assertTrue(newMap.containsKey(ProgressFields.EASEAGENT_PROGRESS_PENETRATION_FIELDS_CONFIG + ".mesh-app-backend.0"));

        newMap = CompatibilityConversion.transform(Collections.singletonMap("observability.outputServer.bootstrapServer", "tstatssta"));
        assertEquals(newMap.size(), 1);
        assertTrue(newMap.containsKey("observability.outputServer.bootstrapServer"));


        newMap = CompatibilityConversion.transform(Collections.singletonMap("observability.metrics.access.enabled", "true"));
        assertEquals(newMap.size(), 1);
        assertEquals(newMap.get("plugin.observability.access.metric.enabled"), "true");


        newMap = CompatibilityConversion.transform(Collections.singletonMap("observability.metrics.access.interval", "30"));
        assertEquals(newMap.size(), 1);
        assertEquals(newMap.get("plugin.observability.access.metric.interval"), "30");

        newMap = CompatibilityConversion.transform(Collections.singletonMap("observability.tracings.remoteInvoke.enabled", "true"));
        assertEquals(newMap.size(), 1);
        assertEquals(newMap.get("plugin.observability.webclient.tracing.enabled"), "true");

        newMap = CompatibilityConversion.transform(Collections.singletonMap("observability.tracings.request.enabled", "true"));
        assertEquals(newMap.size(), REQUEST_NAMESPACE.length);
        for (String s : REQUEST_NAMESPACE) {
            assertEquals(newMap.get("plugin.observability." + s + ".tracing.enabled"), "true");
        }

        newMap = CompatibilityConversion.transform(Collections.singletonMap("observability.metrics.jvmGc.enabled", "true"));
        assertEquals(newMap.size(), 1);
        assertEquals(newMap.get("observability.metrics.jvmGc.enabled"), "true");


        newMap = CompatibilityConversion.transform(Collections.singletonMap("observability.tracings.sampledByQPS", "100"));
        assertEquals(newMap.size(), 1);
        assertEquals(newMap.get("observability.tracings.sampledByQPS"), "100");


        newMap = CompatibilityConversion.transform(Collections.singletonMap("observability.tracings.output.enabled", "true"));
        assertEquals(newMap.size(), 1);
        assertEquals(newMap.get("observability.tracings.output.enabled"), "true");
    }
}
