package com.megaease.easeagent.metrics.impl;

import com.codahale.metrics.Snapshot;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;

import java.io.OutputStream;

public class SnapshotImpl implements com.megaease.easeagent.plugin.api.metric.Snapshot {
    private final Snapshot snapshot;

    private SnapshotImpl(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    public static com.megaease.easeagent.plugin.api.metric.Snapshot build(Snapshot snapshot) {
        return snapshot == null ? NoOpMetrics.NO_OP_SNAPSHOT : new SnapshotImpl(snapshot);
    }


    @Override
    public double getValue(double quantile) {
        return snapshot.getValue(quantile);
    }

    @Override
    public long[] getValues() {
        return snapshot.getValues();
    }

    @Override
    public int size() {
        return snapshot.size();
    }

    @Override
    public long getMax() {
        return snapshot.getMax();
    }

    @Override
    public double getMean() {
        return snapshot.getMean();
    }

    @Override
    public long getMin() {
        return snapshot.getMin();
    }

    @Override
    public double getStdDev() {
        return snapshot.getStdDev();
    }

    @Override
    public void dump(OutputStream output) {
        snapshot.dump(output);
    }
}
