package com.megaease.easeagent.metrics;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.metrics.jvm.memory.JVMMemoryMetric;
import org.junit.Test;

public class JVMMemoryMetricTest {

    @Test
    public void success() {
        MetricRegistry metricRegistry = new MetricRegistry();
        JVMMemoryMetric jvmMemoryMetric = new JVMMemoryMetric(metricRegistry, false);

        jvmMemoryMetric.doJob();

        // no exception is success
    }

}
