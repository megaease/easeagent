package com.megaease.easeagent.plugin.api.trace;

import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ProgressContext;

public interface Tracing {
    boolean isNoop();

    Span currentSpan();

    Span nextSpan(Object message);

    AsyncContext exportAsync(Request request);

    void importAsync(AsyncContext snapshot);

    ProgressContext nextProgress(Request request);

    void importProgress(Request request);

    MessagingTracing<? extends Request> messagingTracing();
}
