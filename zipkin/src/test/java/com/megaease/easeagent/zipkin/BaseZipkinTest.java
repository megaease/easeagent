package com.megaease.easeagent.zipkin;

import brave.Tracer;
import brave.Tracing;
import brave.propagation.StrictCurrentTraceContext;
import org.junit.After;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

public class BaseZipkinTest {
    StrictCurrentTraceContext currentTraceContext = StrictCurrentTraceContext.create();

    @After
    public void close() {
        Tracing current = Tracing.current();
        if (current != null) current.close();
        currentTraceContext.close();
    }

    protected Tracer tracer(Reporter<Span> reporter) {
        return Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .spanReporter(reporter).build().tracer();
    }
}
