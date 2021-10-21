package com.megaease.easeagent.plugin.api.trace;

public interface Injector<R extends Request> {
    void inject(Span span, R request);
}
