package com.megaease.easeagent.plugin.api.metric;

import java.io.OutputStream;

public interface Snapshot {
    double getValue(double quantile);

    long[] getValues();

    int size();

    long getMax();

    double getMean();

    long getMin();

    double getStdDev();

    void dump(OutputStream output);
}

