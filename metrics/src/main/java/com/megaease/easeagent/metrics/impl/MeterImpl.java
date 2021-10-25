package com.megaease.easeagent.metrics.impl;

import com.codahale.metrics.Meter;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;

import javax.annotation.Nonnull;

public class MeterImpl implements com.megaease.easeagent.plugin.api.metric.Meter {
    private final Meter meter;

    private MeterImpl(@Nonnull Meter meter) {
        this.meter = meter;
    }

    public static com.megaease.easeagent.plugin.api.metric.Meter build(Meter meter) {
        return meter == null ? NoOpMetrics.NO_OP_METER : new MeterImpl(meter);
    }

    @Override
    public void mark() {
        meter.mark();
    }

    @Override
    public void mark(long n) {
        meter.mark(n);
    }

    @Override
    public long getCount() {
        return meter.getCount();
    }

    @Override
    public double getFifteenMinuteRate() {
        return meter.getFifteenMinuteRate();
    }

    @Override
    public double getFiveMinuteRate() {
        return meter.getFiveMinuteRate();
    }

    @Override
    public double getMeanRate() {
        return meter.getMeanRate();
    }

    @Override
    public double getOneMinuteRate() {
        return meter.getOneMinuteRate();
    }
}
