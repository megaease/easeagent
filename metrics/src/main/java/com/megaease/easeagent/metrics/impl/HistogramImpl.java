package com.megaease.easeagent.metrics.impl;

import com.codahale.metrics.Histogram;
import com.megaease.easeagent.plugin.api.metric.Snapshot;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;

import javax.annotation.Nonnull;

public class HistogramImpl implements com.megaease.easeagent.plugin.api.metric.Histogram {
    private final Histogram histogram;

    private HistogramImpl(@Nonnull Histogram histogram) {
        this.histogram = histogram;
    }

    public static com.megaease.easeagent.plugin.api.metric.Histogram build(Histogram histogram){
        return histogram==null?NoOpMetrics.NO_OP_HISTOGRAM:new HistogramImpl(histogram);
    }

    @Override
    public void update(int value) {
        histogram.update(value);
    }

    @Override
    public void update(long value) {
        histogram.update(value);
    }

    @Override
    public long getCount() {
        return histogram.getCount();
    }

    @Override
    public Snapshot getSnapshot() {
        return SnapshotImpl.build(histogram.getSnapshot());
    }
}
