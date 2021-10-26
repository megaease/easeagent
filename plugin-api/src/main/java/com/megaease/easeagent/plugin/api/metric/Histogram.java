package com.megaease.easeagent.plugin.api.metric;

public interface Histogram extends Metric {

    void update(int value);

    void update(long value);

    long getCount();

    Snapshot getSnapshot();
}
