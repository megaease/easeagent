package com.megaease.easeagent.plugin.api.context;

import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.api.trace.Tracing;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;

import java.util.Collections;
import java.util.Map;

public interface AsyncContext {

    boolean isNoop();

    Tracing getTracer();

    Map<Object, Object> getContext();

    void putContext(Map<Object, Object> context);

}
