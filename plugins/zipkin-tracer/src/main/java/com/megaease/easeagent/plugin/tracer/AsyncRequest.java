package com.megaease.easeagent.plugin.tracer;

import com.megaease.easeagent.plugin.api.trace.Request;

import java.util.HashMap;
import java.util.Map;

public class AsyncRequest implements Request {
    private final Request request;
    private final Map<String, String> header;

    public AsyncRequest(Request request) {
        this.request = request;
        this.header = new HashMap<>();
    }

    @Override
    public String operation() {
        return request.operation();
    }

    @Override
    public String channelKind() {
        return request.channelKind();
    }

    @Override
    public String channelName() {
        return request.channelName();
    }

    @Override
    public Object unwrap() {
        return request.unwrap();
    }

    @Override
    public String header(String name) {
        String value = request.header(name);
        return value == null ? header.get(name) : value;
    }

    @Override
    public void setHeader(String name, String value) {
        request.setHeader(name, value);
        header.put(name, value);
    }
}
