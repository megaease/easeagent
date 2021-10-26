package com.megaease.easeagent.plugin.api.metric;

public interface Gauge<T> extends Metric {
    T getValue();
}
