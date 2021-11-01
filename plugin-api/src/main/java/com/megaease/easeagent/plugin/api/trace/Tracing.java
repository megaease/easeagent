package com.megaease.easeagent.plugin.api.trace;

import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ProgressContext;

import javax.annotation.Nonnull;
import java.util.Map;

public interface Tracing {
    boolean isNoop();

    Span currentSpan();

    Span nextSpan();

    Span nextSpan(Message message);

    AsyncContext exportAsync(Request request);

    Span importAsync(AsyncContext snapshot);

    ProgressContext nextProgress(Request request);

    ProgressContext importProgress(Request request);

    MessagingTracing<? extends Request> messagingTracing();
}
