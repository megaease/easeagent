package com.megaease.easeagent.plugin.api.trace;

import javax.annotation.Nullable;

public interface Span {
    enum Kind {
        CLIENT,
        SERVER,
        PRODUCER,
        CONSUMER
    }

    Span name(String name);

    Span tag(String key, String value);

    Span annotate(String value);

    boolean isNoop();

    Span start();

    Span start(long timestamp);

    Span kind(@Nullable Kind kind);

    Span annotate(long timestamp, String value);

    Span error(Throwable throwable);

    Span remoteServiceName(String remoteServiceName);

    boolean remoteIpAndPort(@Nullable String remoteIp, int remotePort);

    void abandon();

    void finish();

    void finish(long timestamp);

    void flush();

    void inject(Request request);

    Scope maybeScope();

    Span cacheScope();
}
