package com.megaease.easeagent.plugin.api.trace;

public interface Request {
    String operation();

    String channelKind();

    String channelName();

    Object unwrap();

    String header(String name);

    void setHeader(String name, String value);
}
