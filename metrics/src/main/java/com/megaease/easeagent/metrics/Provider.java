package com.megaease.easeagent.metrics;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.common.LocalhostAddress;
import com.megaease.easeagent.common.NamedDaemonThreadFactory;
import com.megaease.easeagent.core.Configurable;
import com.megaease.easeagent.core.Injection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configurable(bind = "metrics.report")
public abstract class Provider {

    @Injection.Bean
    public CallTrace callTrace() {
        return new CallTrace();
    }

    @Injection.Bean
    public Metrics metrics() {
        final MetricRegistry registry = new MetricRegistry();
        final Logger logger = LoggerFactory.getLogger(reporter_name());
        final Map<String, String> hostInfo = ImmutableMap.<String, String>builder()
                .put("system", system())
                .put("application", application())
                .put("hostname", hostname())
                .put("hostipv4",hostipv4())
                .build();
        Executors.newSingleThreadScheduledExecutor(new NamedDaemonThreadFactory("easeagent-metrics-report"))
                 .scheduleWithFixedDelay(
                         new LogReporter(logger, registry, hostInfo, TimeUnit.valueOf(rate_unit()), TimeUnit.valueOf(duration_unit())),
                         period_seconds(), period_seconds(), TimeUnit.SECONDS
                 );
        return new Metrics(registry);
    }

    @Configurable.Item
    String reporter_name() {
        return "metrics";
    }

    @Configurable.Item
    String rate_unit() {
        return TimeUnit.SECONDS.toString();
    }

    @Configurable.Item
    String duration_unit(){
        return TimeUnit.MILLISECONDS.toString();
    }

    @Configurable.Item
    long period_seconds() {
        return 30;
    }

    @Configurable.Item
    String hostipv4() {
        return LocalhostAddress.getLocalhostAddr().getHostAddress();
    }

    @Configurable.Item
    String hostname() {
        return LocalhostAddress.getLocalhostName();
    }

    @Configurable.Item
    abstract String system();

    @Configurable.Item
    abstract String application();


}
