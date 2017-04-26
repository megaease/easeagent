package com.megaease.easeagent.requests;

import brave.sampler.Sampler;
import com.megaease.easeagent.common.NamedDaemonThreadFactory;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private final AtomicBoolean sampled;

    private JmxSampler() {
        sampled = new AtomicBoolean(false);
        service = Executors.newSingleThreadScheduledExecutor(new NamedDaemonThreadFactory("easeagent-jmx-sampler"));
    }

    @Override
    public boolean isSampled(long traceId) {
        return sampled.get();
    }

    @Override
    public boolean enable(int timeoutSeconds) {
        if (!sampled.compareAndSet(false, true)) return false;

        service.schedule(new Runnable() {
            @Override
            public void run() {
                sampled.set(false);
            }
        }, timeoutSeconds, TimeUnit.SECONDS);

        return true;
    }
}
