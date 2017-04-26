package com.megaease.easeagent.requests;

import brave.sampler.Sampler;
import com.megaease.easeagent.common.NamedDaemonThreadFactory;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class JmxSampler extends Sampler implements JmxSamplerMBean {

    static class Lazy {
        static final JmxSampler SINGLETON;
        static final ObjectName NAME;

        static {
            SINGLETON = new JmxSampler();
            try {
                NAME = new ObjectName("com.megaease.easeagent:type=JmxSampler");
                ManagementFactory.getPlatformMBeanServer().registerMBean(SINGLETON, NAME);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

    }

    static Sampler create() {
        return Lazy.SINGLETON;
    }

    private final ScheduledExecutorService service;

    private volatile boolean sampled = false;

    private JmxSampler() {
        service = Executors.newSingleThreadScheduledExecutor(new NamedDaemonThreadFactory("easeagent-jmx-sampler"));
    }

    @Override
    public boolean isSampled(long traceId) {
        return sampled;
    }

    @Override
    public boolean enable(int timeoutSeconds) {
        if (sampled) return false;

        sampled = true;

        service.schedule(new Runnable() {
            @Override
            public void run() {
                sampled = false;
            }
        }, timeoutSeconds, TimeUnit.SECONDS);

        return true;
    }
}
