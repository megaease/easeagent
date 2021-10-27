package com.megaease.easeagent.plugin.api.trace;

public interface Request {
    Span.Kind kind();

    String name();

    String header(String name);

    void setHeader(String name, String value);
}
