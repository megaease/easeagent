package com.megaease.easeagent.plugin.api.context;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.api.trace.Tracing;

import java.util.Map;

public interface AsyncContext {

    boolean isNoop();

    Tracing getTracer();

    Context getContext();

    Span importToCurr();

    Map<Object, Object> getAll();

    void putAll(Map<Object, Object> context);

}
