package com.megaease.easeagent.plugin.api.trace;

public interface Injector<R extends MessagingRequest> {
    void inject(Span span, R request);
}
