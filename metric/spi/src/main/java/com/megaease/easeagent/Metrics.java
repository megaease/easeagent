package com.megaease.easeagent;

import com.codahale.metrics.*;

import java.util.Map;
import java.util.concurrent.Callable;

public interface Metrics {

    void iterate(Consumer consumer);

    Counter counter(String name, Map<String, String> tags);

    Meter meter(String name, Map<String, String> tags);

    Timer timer(String name, Map<String, String> tags);

    void registerIfAbsent(String name, Callable<Gauge<Object>> supplier) throws Exception;

    interface Consumer {
        void accept(String name, Map<String, String> tags, Metric metric);
    }

}
