package com.megaease.easeagent.metrics.impl;


import com.codahale.metrics.Counter;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;

import javax.annotation.Nonnull;

public class CounterImpl implements com.megaease.easeagent.plugin.api.metric.Counter {
    private final Counter counter;

    private CounterImpl(@Nonnull Counter counter) {
        this.counter = counter;
    }

    public static com.megaease.easeagent.plugin.api.metric.Counter build(Counter counter) {
        return counter == null ? NoOpMetrics.NO_OP_COUNTER : new CounterImpl(counter);
    }

    @Override
    public void inc() {
        counter.inc();
    }

    @Override
    public void inc(long n) {
        counter.inc(n);
    }

    @Override
    public void dec() {
        counter.dec();
    }

    @Override
    public void dec(long n) {
        counter.dec(n);
    }

    @Override
    public long getCount() {
        return counter.getCount();
    }
}
