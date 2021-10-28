package com.megaease.easeagent.core;

import com.megaease.easeagent.plugin.api.trace.Tracing;

import java.util.function.Function;
import java.util.function.Supplier;

public interface TracingProvider {
    Supplier<Tracing> tracingSupplier();

    void setRootSpanFinishCall(Function rootSpanFinish);
}
