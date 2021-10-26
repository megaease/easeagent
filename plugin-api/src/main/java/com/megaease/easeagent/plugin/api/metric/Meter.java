package com.megaease.easeagent.plugin.api.metric;

public interface Meter extends Metric {

    void mark();

    void mark(long n);

    long getCount();

    double getFifteenMinuteRate();

    double getFiveMinuteRate();

    double getMeanRate();

    double getOneMinuteRate();
}

