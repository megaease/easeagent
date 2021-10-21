package com.megaease.easeagent.plugin.metrics;


import com.codahale.metrics.Gauge;

public class GaugeImpl<T> implements Gauge<T> {
    private final T t;

    public GaugeImpl(T t) {
        this.t = t;
    }


    @Override
    public T getValue() {
        return t;
    }
}
