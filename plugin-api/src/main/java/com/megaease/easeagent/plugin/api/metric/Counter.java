package com.megaease.easeagent.plugin.api.metric;

public interface Counter {
    void inc();

    void inc(long n);

    void dec();

    void dec(long n);

    long getCount();
}

