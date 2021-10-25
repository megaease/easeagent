package com.megaease.easeagent.plugin.api;

import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.metric.Metric;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Tracing;

import java.util.Map;

public interface Context {
    boolean isNoop();

    Tracing currentTracing();

    <V> V getValue(Object key);

    <V> V remove(Object key);

    AsyncContext exportAsync(Request request);

    void importAsync(AsyncContext snapshot);

    ProgressContext nextProgress(Request request);

    void importProgress(Request request);

    Map<Object, Object>  clear();
}
