package com.megaease.easeagent.plugin.tracer;

import brave.propagation.TraceContext;
import com.megaease.easeagent.plugin.api.trace.Extractor;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;

public class ZipkinExtractor implements Extractor {
    private final TraceContext.Extractor<Request> extractor;

    private ZipkinExtractor(TraceContext.Extractor<Request> extractor) {
        this.extractor = extractor;
    }

    public static Extractor<?> build(TraceContext.Extractor<Request> extractor) {
        if (extractor == null) {
            return NoOpTracer.NO_OP_EXTRACTOR;
        }
        return new ZipkinExtractor(extractor);
    }

    @Override
    public Object extract(Request request) {
        return extractor.extract(request);
    }
}
