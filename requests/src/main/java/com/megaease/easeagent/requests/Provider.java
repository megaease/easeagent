package com.megaease.easeagent.requests;

import brave.sampler.CountingSampler;
import brave.sampler.Sampler;
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.common.LocalhostAddress;
import com.megaease.easeagent.core.Configurable;
import com.megaease.easeagent.core.Injection;
import org.slf4j.LoggerFactory;

@Configurable(bind = "requests.report")
abstract class Provider {
    @Injection.Bean
    public CallTrace callStack() {
        return new CallTrace();
    }

    @Injection.Bean
    public Sampler sampler() {
        return capture_rate() > 0.0 ? CountingSampler.create((float) capture_rate()) : JmxSampler.create();
    }

    @Injection.Bean
    public Reporter reporter() {
        return new AsyncLogReporter(LoggerFactory.getLogger(reporter_name()), reporter_queue_capacity(), hostipv4(),
                                    hostname(), system(), application(), type());
    }

    @Configurable.Item
    int reporter_queue_capacity() {
        return 1024;
    }

    @Configurable.Item
    String type() {
        return "http_request";
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
    String reporter_name() {
        return "requests";
    }

    @Configurable.Item
    abstract String system();

    @Configurable.Item
    abstract String application();

    @Configurable.Item
    double capture_rate() {
        return 1.0;
    }

}
