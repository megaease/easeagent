package com.megaease.easeagent.plugin.api.trace;

public interface Tracer {
    Span currentSpan();

    Span nextSpan(Request request);
}
